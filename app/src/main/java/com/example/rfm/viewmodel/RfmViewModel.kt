package com.example.rfm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.data.RfmDatabase
import com.example.rfm.data.SaveManager
import com.example.rfm.engine.MatchSimulationEngine
import com.example.rfm.model.*
import com.example.rfm.navigation.RfmScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class RfmViewModel(application: Application) : AndroidViewModel(application) {
    private val saveManager = SaveManager(application)

    private val _currentScreen = MutableStateFlow(RfmScreen.SPLASH)
    val currentScreen: StateFlow<RfmScreen> = _currentScreen.asStateFlow()

    private val _gameState = MutableStateFlow(RfmDatabase.createInitialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _activeEngine = MutableStateFlow<MatchSimulationEngine?>(null)
    val activeEngine: StateFlow<MatchSimulationEngine?> = _activeEngine.asStateFlow()

    private val _showModDialog = MutableStateFlow(false)
    val showModDialog: StateFlow<Boolean> = _showModDialog.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Apply sound preference
        RfmAudioEngine.setMuted(!_gameState.value.isSoundEnabled)
    }

    fun navigateTo(screen: RfmScreen) {
        RfmAudioEngine.playMenuBlip()
        _currentScreen.value = screen
    }

    fun openModDialog() {
        RfmAudioEngine.playMenuBlip()
        _showModDialog.value = true
    }

    fun closeModDialog() {
        _showModDialog.value = false
    }

    fun toggleRetroKeypad() {
        val current = _gameState.value
        current.isRetroKeypadEnabled = !current.isRetroKeypadEnabled
        _gameState.value = current.copy()
    }

    fun toggleSound() {
        val current = _gameState.value
        current.isSoundEnabled = !current.isSoundEnabled
        RfmAudioEngine.setMuted(!current.isSoundEnabled)
        _gameState.value = current.copy()
    }

    fun startNewGame(selectedTeamId: String, managerName: String) {
        val freshState = RfmDatabase.createInitialGameState()
        freshState.userTeamId = selectedTeamId
        freshState.managerName = managerName.ifBlank { "Sir Manager" }
        freshState.totalMatchDays = freshState.getUserLeague()?.teams?.size?.let { (it - 1) * 2 } ?: 38
        _gameState.value = freshState
        saveManager.saveGame("autosave", freshState)
        navigateTo(RfmScreen.DASHBOARD)
    }

    fun saveGame(slot: String): Boolean {
        saveManager.saveGame(slot, _gameState.value)
        RfmAudioEngine.playMenuBlip()
        return true
    }

    fun loadGame(slot: String): Boolean {
        val current = _gameState.value
        val success = saveManager.loadGame(slot, current)
        if (success) {
            applyModSideEffects(current.modSettings)
            _gameState.value = current.copy()
            navigateTo(RfmScreen.DASHBOARD)
        }
        return success
    }

    fun hasSave(slot: String): Boolean = saveManager.hasSave(slot)

    fun applyModSettings(newSettings: ModSettings) {
        val state = _gameState.value
        state.modSettings = newSettings
        applyModSideEffects(newSettings)
        _gameState.value = state.copy()
        _showModDialog.value = false
        RfmAudioEngine.playWhistle(longWhistle = false)
    }

    private fun applyModSideEffects(mods: ModSettings) {
        val state = _gameState.value
        val userTeam = state.getUserTeam()

        if (userTeam != null) {
            if (mods.unlimitedMoney) {
                userTeam.balanceEuro = 999_999_999L
            }
            if (mods.unlimitedTransferBudget) {
                userTeam.transferBudgetEuro = 500_000_000L
                userTeam.wageBudgetEuroWeekly = 10_000_000L
            }
            if (mods.unlimitedClubResources) {
                userTeam.stadiumLevel = 5
                userTeam.stadiumCapacity = 95_000
                userTeam.trainingFacilityLevel = 5
                userTeam.medicalFacilityLevel = 5
                userTeam.youthFacilityLevel = 5
            }
            if (mods.maxClubReputation) {
                userTeam.reputationStars = 5
            }
            if (mods.maxManagerReputation) {
                state.managerReputation = 100
                state.boardConfidence = 100
            }
            if (mods.maxPlayerStats) {
                userTeam.players.forEach { p ->
                    p.overall = 99
                    p.pace = 99
                    p.shooting = 99
                    p.passing = 99
                    p.dribbling = 99
                    p.defending = 99
                    p.physical = 99
                }
            }
            if (mods.noPlayerFatigue || mods.disableFatigueEffects) {
                userTeam.players.forEach { it.condition = 100 }
            }
            if (mods.noPlayerInjury) {
                userTeam.players.forEach { it.injuryWeeks = 0 }
            }
        }
    }

    fun startMatch(fixture: Fixture) {
        val state = _gameState.value
        val home = state.getTeamById(fixture.homeTeamId) ?: return
        val away = state.getTeamById(fixture.awayTeamId) ?: return

        // Auto-bench unavailable players (injured or suspended) from starting XI
        val userTeam = state.getUserTeam()
        if (userTeam != null && (userTeam.id == home.id || userTeam.id == away.id)) {
            val unavailableStarters = userTeam.players.filter { it.isStarting && (!it.canPlay()) }
            val availableBench = userTeam.players.filter { !it.isStarting && it.canPlay() }
            var benchIdx = 0
            for (unavail in unavailableStarters) {
                unavail.isStarting = false
                unavail.isSubstitute = true
                if (benchIdx < availableBench.size) {
                    val subIn = availableBench[benchIdx]
                    subIn.isStarting = true
                    subIn.isSubstitute = false
                    benchIdx++
                }
            }
        }

        val engine = MatchSimulationEngine(fixture, home, away, state.modSettings)
        engine.startMatch()
        _activeEngine.value = engine
        navigateTo(RfmScreen.MATCH_DAY)
    }

    fun toggleSimulationPlayback(speedMs: Long = 350L) {
        val engine = _activeEngine.value ?: return
        if (_isSimulating.value) {
            simulationJob?.cancel()
            _isSimulating.value = false
        } else {
            _isSimulating.value = true
            simulationJob = viewModelScope.launch {
                while (_isSimulating.value && !engine.isFullTime) {
                    if (engine.isHalfTime) {
                        // Pause at half time for user tactical talk
                        _isSimulating.value = false
                        break
                    }
                    delay(speedMs)
                    engine.tickMinute()
                }
                if (engine.isFullTime) {
                    _isSimulating.value = false
                    updateStandingsAfterMatch(engine.fixture, engine.homeTeam, engine.awayTeam)
                }
            }
        }
    }

    fun instantSimulateMatch() {
        val engine = _activeEngine.value ?: return
        simulationJob?.cancel()
        _isSimulating.value = false
        engine.simulateToEnd()
        updateStandingsAfterMatch(engine.fixture, engine.homeTeam, engine.awayTeam)
    }

    fun postMatchReturnToDashboard() {
        val state = _gameState.value
        // Simulate other matches for this matchday across all leagues
        simulateAiMatchesForRound(state.currentMatchDay)
        state.currentMatchDay++

        val userTeam = state.getUserTeam()
        if (userTeam != null) {
            // Ticket revenue for matchday
            val baseAttendance = (userTeam.stadiumCapacity * 0.85f * userTeam.ticketPriceTier.attendanceMultiplier).toInt().coerceAtMost(userTeam.stadiumCapacity)
            val matchTicketIncome = baseAttendance.toLong() * userTeam.ticketPriceTier.priceEuro
            val sponsorWeekly = userTeam.shirtSponsor.weeklyIncomeEuro + userTeam.stadiumSponsor.weeklyIncomeEuro
            val totalMatchdayIncome = matchTicketIncome + sponsorWeekly
            userTeam.balanceEuro += totalMatchdayIncome

            // Deduct weekly player wages
            val weeklyWageBill = userTeam.players.sumOf { it.wageWeeklyEuro }
            userTeam.balanceEuro -= weeklyWageBill
        }

        // Decrement injuries, suspensions and restore condition for players
        for (league in state.leagues) {
            for (team in league.teams) {
                for (p in team.players) {
                    if (p.injuryWeeks > 0) {
                        p.injuryWeeks--
                        if (p.injuryWeeks == 0) {
                            p.injuryType = ""
                        }
                    }
                    if (p.suspensionMatchesRemaining > 0) {
                        p.suspensionMatchesRemaining--
                        if (p.suspensionMatchesRemaining == 0) {
                            p.isRedCard = false
                            p.yellowCards = 0
                        }
                    }
                    // Natural post-match rest recovery
                    p.condition = (p.condition + 15).coerceAtMost(100)
                }
            }
        }

        // Board Confidence & League Standing Evaluation
        val userLeague = state.getUserLeague()
        if (userTeam != null && userLeague != null) {
            val userStanding = userLeague.standings.find { it.teamId == userTeam.id }
            val currentPos = if (userStanding != null) userLeague.standings.indexOf(userStanding) + 1 else 10
            val targetPos = userTeam.seasonObjective.targetPosition

            if (currentPos <= targetPos) {
                state.boardConfidence = (state.boardConfidence + 2).coerceAtMost(100)
                state.managerReputation = (state.managerReputation + 1).coerceAtMost(100)
            } else {
                state.boardConfidence = (state.boardConfidence - 3).coerceAtLeast(5)
                if (state.boardConfidence < 30) {
                    state.newsInbox.add(0, NewsMessage(
                        id = "board_warn_${state.currentMatchDay}",
                        title = "URGENT: Board Warning on Club Performance",
                        content = "The board of directors is deeply concerned with the club's current standing (${currentPos}th place). Target is ${userTeam.seasonObjective.label}. Immediate improvement required.",
                        dateString = "Matchday ${state.currentMatchDay}",
                        isImportant = true
                    ))
                }
            }
        }

        // Transfer Deadline Day / AI Offers Generation (Every 4 rounds)
        if (state.currentMatchDay % 4 == 0 && userTeam != null) {
            val candidate = userTeam.players.filter { it.overall >= 78 && !it.isStarting }.randomOrNull()
                ?: userTeam.players.filter { it.overall >= 76 }.randomOrNull()
            if (candidate != null && state.activeTransferOffers.none { it.playerId == candidate.id && it.status == "PENDING" }) {
                val otherLeagues = state.leagues.filter { it.id != userTeam.leagueId }
                val biddingTeam = otherLeagues.randomOrNull()?.teams?.randomOrNull()
                if (biddingTeam != null && biddingTeam.id != userTeam.id) {
                    val bidAmount = (candidate.valueEuro * (0.95f + Random.nextFloat() * 0.35f)).toLong()
                    state.activeTransferOffers.add(0, TransferOffer(
                        id = "offer_${System.currentTimeMillis()}",
                        playerId = candidate.id,
                        playerName = candidate.name,
                        playerPosition = candidate.position,
                        playerOverall = candidate.overall,
                        fromTeamId = biddingTeam.id,
                        fromTeamName = biddingTeam.name,
                        toTeamId = userTeam.id,
                        toTeamName = userTeam.name,
                        offerAmountEuro = bidAmount,
                        proposedWageEuroWeekly = candidate.wageWeeklyEuro + 15_000L,
                        isIncomingToUser = true,
                        status = "PENDING"
                    ))
                    state.newsInbox.add(0, NewsMessage(
                        id = "news_bid_${System.currentTimeMillis()}",
                        title = "Transfer Bid: ${biddingTeam.name} target ${candidate.name}!",
                        content = "${biddingTeam.name} have submitted an official offer of €${bidAmount / 1_000_000}M for ${candidate.name}. Check your Transfers Hub to respond.",
                        dateString = "Matchday ${state.currentMatchDay}",
                        isImportant = true
                    ))
                }
            }
        }

        _activeEngine.value = null
        _gameState.value = state.copy()
        saveManager.saveGame("autosave", state)
        navigateTo(RfmScreen.DASHBOARD)
    }

    private fun simulateAiMatchesForRound(round: Int) {
        val state = _gameState.value
        val unplayedRoundFixtures = state.fixtures.filter { it.round == round && !it.isPlayed }

        for (fix in unplayedRoundFixtures) {
            val home = state.getTeamById(fix.homeTeamId) ?: continue
            val away = state.getTeamById(fix.awayTeamId) ?: continue
            val sim = MatchSimulationEngine(fix, home, away, ModSettings())
            sim.startMatch()
            sim.simulateToEnd()
            updateStandingsAfterMatch(fix, home, away)
        }
    }

    private fun updateStandingsAfterMatch(fixture: Fixture, home: Team, away: Team) {
        val state = _gameState.value
        val league = state.leagues.find { it.id == home.leagueId } ?: return

        val homeStanding = league.standings.find { it.teamId == home.id } ?: return
        val awayStanding = league.standings.find { it.teamId == away.id } ?: return

        val hScore = fixture.homeScore ?: 0
        val aScore = fixture.awayScore ?: 0

        homeStanding.played++
        awayStanding.played++
        homeStanding.goalsFor += hScore
        homeStanding.goalsAgainst += aScore
        awayStanding.goalsFor += aScore
        awayStanding.goalsAgainst += hScore

        when {
            hScore > aScore -> {
                homeStanding.won++
                homeStanding.points += 3
                homeStanding.formHistory.add('W')
                awayStanding.lost++
                awayStanding.formHistory.add('L')
            }
            hScore < aScore -> {
                awayStanding.won++
                awayStanding.points += 3
                awayStanding.formHistory.add('W')
                homeStanding.lost++
                homeStanding.formHistory.add('L')
            }
            else -> {
                homeStanding.drawn++
                awayStanding.drawn++
                homeStanding.points += 1
                awayStanding.points += 1
                homeStanding.formHistory.add('D')
                awayStanding.formHistory.add('D')
            }
        }

        league.sortStandings()
    }

    fun makeSubstitutionInMatch(playerOutId: String, playerInId: String): Boolean {
        val engine = _activeEngine.value ?: return false
        val userTeam = _gameState.value.getUserTeam() ?: return false
        val success = engine.makeSubstitution(userTeam, playerOutId, playerInId)
        if (success) {
            _activeEngine.value = engine
        }
        return success
    }

    fun buyPlayer(player: Player, fee: Long, weeklyWage: Long): Boolean {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return false

        if (!state.modSettings.unlimitedTransferBudget && !state.modSettings.disableFinancialPenalties) {
            if (userTeam.transferBudgetEuro < fee) return false
            userTeam.transferBudgetEuro -= fee
            userTeam.balanceEuro -= fee
        }

        val newSigning = player.copyPlayer()
        newSigning.wageWeeklyEuro = weeklyWage
        newSigning.isStarting = false
        newSigning.isSubstitute = true
        userTeam.players.add(newSigning)
        state.transferMarket.remove(player)

        state.newsInbox.add(0, NewsMessage(
            id = "news_trans_${System.currentTimeMillis()}",
            title = "Deal Done: ${player.name} Signs!",
            content = "The club has completed the signing of ${player.name} for an estimated fee of €${fee / 1_000_000}M.",
            dateString = "Matchday ${state.currentMatchDay}",
            isImportant = true
        ))

        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
        return true
    }

    fun signFreeAgent(player: Player, weeklyWage: Long): Boolean {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return false

        val newSigning = player.copyPlayer()
        newSigning.wageWeeklyEuro = weeklyWage
        newSigning.isStarting = false
        newSigning.isSubstitute = true
        newSigning.contractYears = 2
        newSigning.isLoan = false
        userTeam.players.add(newSigning)
        state.freeAgents.remove(player)

        state.newsInbox.add(0, NewsMessage(
            id = "news_fa_${System.currentTimeMillis()}",
            title = "Free Agent Secured: ${player.name} Signs!",
            content = "The club has completed the free transfer of veteran ${player.name} on a 2-year Bosman deal (€${weeklyWage / 1_000}k/wk).",
            dateString = "Matchday ${state.currentMatchDay}",
            isImportant = true
        ))

        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
        return true
    }

    fun loanPlayer(player: Player, loanFee: Long, weeklyWageContribution: Long): Boolean {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return false

        if (!state.modSettings.unlimitedTransferBudget && !state.modSettings.disableFinancialPenalties) {
            if (userTeam.transferBudgetEuro < loanFee) return false
            userTeam.transferBudgetEuro -= loanFee
            userTeam.balanceEuro -= loanFee
        }

        val loanedIn = player.copyPlayer()
        loanedIn.isLoan = true
        loanedIn.contractYears = 1
        loanedIn.wageWeeklyEuro = weeklyWageContribution
        loanedIn.isStarting = false
        loanedIn.isSubstitute = true
        userTeam.players.add(loanedIn)
        state.loanMarket.remove(player)

        state.newsInbox.add(0, NewsMessage(
            id = "news_loan_${System.currentTimeMillis()}",
            title = "Loan Agreement: ${player.name} Arrives!",
            content = "${player.name} has joined the squad on a season-long loan with a €${loanFee / 1_000_000}M fee contribution.",
            dateString = "Matchday ${state.currentMatchDay}",
            isImportant = true
        ))

        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
        return true
    }

    fun upgradeFacility(type: String): Boolean {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return false
        val cost = 5_000_000L

        if (!state.modSettings.unlimitedClubResources && !state.modSettings.disableFinancialPenalties) {
            if (userTeam.balanceEuro < cost) return false
            userTeam.balanceEuro -= cost
        }

        when (type) {
            "stadium" -> {
                userTeam.stadiumLevel++
                userTeam.stadiumCapacity += 5000
            }
            "training" -> userTeam.trainingFacilityLevel++
            "medical" -> userTeam.medicalFacilityLevel++
            "youth" -> userTeam.youthFacilityLevel++
        }

        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
        return true
    }

    fun runWeeklyTraining(focus: String) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        state.currentTrainingFocus = focus

        val boost = if (state.modSettings.maxPlayerDevelopment || state.modSettings.instantTraining) 2 else 1
        userTeam.players.forEach { p ->
            when (focus) {
                "Attacking & Finishing" -> p.shooting = (p.shooting + boost).coerceAtMost(99)
                "Defending & Tactics" -> p.defending = (p.defending + boost).coerceAtMost(99)
                "Passing & Vision" -> p.passing = (p.passing + boost).coerceAtMost(99)
                "Physical Fitness" -> p.physical = (p.physical + boost).coerceAtMost(99)
                else -> {
                    p.form = (p.form + 1).coerceAtMost(10)
                    p.morale = (p.morale + 3).coerceAtMost(100)
                }
            }
        }
        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
    }

    fun promoteYouthProspect(prospect: YouthProspect) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        if (state.youthAcademyProspects.remove(prospect)) {
            val player = prospect.toPlayer()
            userTeam.players.add(player)
            state.newsInbox.add(0, NewsMessage(
                id = "prom_${System.currentTimeMillis()}",
                title = "Academy Graduate Promoted: ${player.name}",
                content = "${player.name} (${player.position.label}, Age ${player.age}) has signed professional terms with the first team.",
                dateString = "Week ${state.currentMatchDay}"
            ))
            _gameState.value = state.copy()
            RfmAudioEngine.playMenuBlip()
        }
    }

    fun sendScoutToRegion(region: String, cost: Long) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        if (userTeam.balanceEuro < cost) return

        userTeam.balanceEuro -= cost
        val timestamp = System.currentTimeMillis()
        val scoutedResults = when (region) {
            "South America" -> listOf(
                Player("scout_${timestamp}_1", "Gabriel Barbosa", PlayerPosition.ST, 17, "Brazil", 74, 85, 78, 70, 82, 35, 75, valueEuro = 7_500_000L, wageWeeklyEuro = 25_000L, potential = 88),
                Player("scout_${timestamp}_2", "Rodrigo Bentancur", PlayerPosition.CM, 16, "Uruguay", 71, 72, 65, 78, 75, 68, 72, valueEuro = 4_200_000L, wageWeeklyEuro = 15_000L, potential = 86)
            )
            "Western Europe" -> listOf(
                Player("scout_${timestamp}_3", "Youri Tielemans", PlayerPosition.CM, 16, "Belgium", 73, 70, 75, 80, 76, 62, 70, valueEuro = 6_000_000L, wageWeeklyEuro = 20_000L, potential = 89),
                Player("scout_${timestamp}_4", "Anthony Martial", PlayerPosition.LW, 17, "France", 74, 88, 76, 72, 84, 38, 72, valueEuro = 7_000_000L, wageWeeklyEuro = 22_000L, potential = 88)
            )
            "Eastern Europe" -> listOf(
                Player("scout_${timestamp}_5", "Mateo Kovacic", PlayerPosition.CM, 18, "Croatia", 76, 78, 68, 82, 85, 60, 71, valueEuro = 9_000_000L, wageWeeklyEuro = 30_000L, potential = 88),
                Player("scout_${timestamp}_6", "Jan Vertonghen", PlayerPosition.CB, 22, "Belgium", 78, 72, 55, 74, 70, 81, 80, valueEuro = 11_000_000L, wageWeeklyEuro = 35_000L, potential = 85)
            )
            else -> listOf(
                Player("scout_${timestamp}_7", "Sadio Mane", PlayerPosition.LW, 20, "Senegal", 75, 90, 74, 73, 81, 40, 74, valueEuro = 8_500_000L, wageWeeklyEuro = 28_000L, potential = 87),
                Player("scout_${timestamp}_8", "Kalidou Koulibaly", PlayerPosition.CB, 21, "Senegal", 76, 75, 45, 65, 62, 82, 86, valueEuro = 9_500_000L, wageWeeklyEuro = 30_000L, potential = 88)
            )
        }

        state.scoutedPlayers.addAll(scoutedResults)
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
    }

    fun signScoutedPlayer(player: Player) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        if (userTeam.transferBudgetEuro < player.valueEuro || userTeam.balanceEuro < player.valueEuro) return

        userTeam.transferBudgetEuro -= player.valueEuro
        userTeam.balanceEuro -= player.valueEuro
        state.scoutedPlayers.remove(player)
        userTeam.players.add(player)

        state.newsInbox.add(0, NewsMessage(
            id = "scout_sign_${System.currentTimeMillis()}",
            title = "Scouted Signing: ${player.name}",
            content = "${userTeam.name} has signed scouted wonderkid ${player.name} for €${player.valueEuro / 1_000_000}M!",
            dateString = "Week ${state.currentMatchDay}"
        ))
        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
    }

    fun respondToTransferOffer(offer: TransferOffer, accept: Boolean, counterFeeEuro: Long? = null) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val player = userTeam.players.find { it.id == offer.playerId } ?: return

        if (accept) {
            offer.status = "ACCEPTED"
            val fee = counterFeeEuro ?: offer.offerAmountEuro
            userTeam.players.remove(player)
            userTeam.balanceEuro += fee
            userTeam.transferBudgetEuro += (fee * 0.85f).toLong()

            state.newsInbox.add(0, NewsMessage(
                id = "sold_${System.currentTimeMillis()}",
                title = "Confirmed Transfer: ${player.name} Sold to ${offer.fromTeamName}",
                content = "${player.name} has departed ${userTeam.name} to join ${offer.fromTeamName} for a confirmed fee of €${fee / 1_000_000}M.",
                dateString = "Matchday ${state.currentMatchDay}",
                isImportant = true
            ))
            RfmAudioEngine.playWhistle(longWhistle = false)
        } else if (counterFeeEuro != null) {
            // Counter-offer evaluation
            val aiAccepts = counterFeeEuro <= (offer.offerAmountEuro * 1.25f).toLong()
            if (aiAccepts) {
                offer.status = "ACCEPTED"
                userTeam.players.remove(player)
                userTeam.balanceEuro += counterFeeEuro
                userTeam.transferBudgetEuro += (counterFeeEuro * 0.85f).toLong()

                state.newsInbox.add(0, NewsMessage(
                    id = "counter_ok_${System.currentTimeMillis()}",
                    title = "Counter-Offer Accepted: ${player.name} Sold!",
                    content = "${offer.fromTeamName} accepted your counter-demand of €${counterFeeEuro / 1_000_000}M for ${player.name}.",
                    dateString = "Matchday ${state.currentMatchDay}",
                    isImportant = true
                ))
            } else {
                offer.status = "REJECTED"
                state.newsInbox.add(0, NewsMessage(
                    id = "counter_no_${System.currentTimeMillis()}",
                    title = "Negotiations Collapsed for ${player.name}",
                    content = "${offer.fromTeamName} deemed your counter-demand of €${counterFeeEuro / 1_000_000}M too high and ended negotiations.",
                    dateString = "Matchday ${state.currentMatchDay}"
                ))
            }
        } else {
            offer.status = "REJECTED"
            state.newsInbox.add(0, NewsMessage(
                id = "rejected_${System.currentTimeMillis()}",
                title = "Bid Rejected for ${player.name}",
                content = "${userTeam.name} rejected ${offer.fromTeamName}'s offer of €${offer.offerAmountEuro / 1_000_000}M.",
                dateString = "Matchday ${state.currentMatchDay}"
            ))
        }
        state.activeTransferOffers.remove(offer)
        _gameState.value = state.copy()
    }

    fun negotiateContract(player: Player, proposedWeeklyWage: Long, additionalYears: Int): Boolean {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return false
        val signingBonus = proposedWeeklyWage * 4

        if (userTeam.balanceEuro < signingBonus) return false
        // Player demands at least equal to or higher than current wage
        if (proposedWeeklyWage < player.wageWeeklyEuro) return false

        userTeam.balanceEuro -= signingBonus
        player.wageWeeklyEuro = proposedWeeklyWage
        player.contractYears += additionalYears
        player.morale = 100
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
        return true
    }

    fun setTempoStyle(tempo: TempoStyle) {
        val state = _gameState.value
        state.getUserTeam()?.tempoStyle = tempo
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
    }

    fun setMarkingStyle(marking: MarkingStyle) {
        val state = _gameState.value
        state.getUserTeam()?.markingStyle = marking
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
    }

    fun toggleOffsideTrap() {
        val state = _gameState.value
        val team = state.getUserTeam() ?: return
        team.offsideTrap = !team.offsideTrap
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
    }

    fun setTicketPriceTier(tier: TicketPriceTier) {
        val state = _gameState.value
        state.getUserTeam()?.ticketPriceTier = tier
        _gameState.value = state.copy()
        RfmAudioEngine.playMenuBlip()
    }

    fun signNewSponsor(category: String, sponsor: SponsorContract) {
        val state = _gameState.value
        val team = state.getUserTeam() ?: return
        if (category == "Shirt Sponsor") {
            team.shirtSponsor = sponsor
        } else {
            team.stadiumSponsor = sponsor
        }
        state.newsInbox.add(0, NewsMessage(
            id = "sponsor_${System.currentTimeMillis()}",
            title = "Commercial Deal Signed: ${sponsor.sponsorName}",
            content = "${team.name} has signed a ${sponsor.contractDurationYears}-year commercial partnership with ${sponsor.sponsorName} generating €${sponsor.weeklyIncomeEuro / 1_000}k per week.",
            dateString = "Matchday ${state.currentMatchDay}",
            isImportant = true
        ))
        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = false)
    }

    fun renewPlayerContract(player: Player) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val renewalCost = player.wageWeeklyEuro * 10
        if (userTeam.balanceEuro >= renewalCost) {
            userTeam.balanceEuro -= renewalCost
            player.contractYears += 2
            player.morale = 100
            _gameState.value = state.copy()
            RfmAudioEngine.playMenuBlip()
        }
    }

    fun physioTreatPlayer(player: Player) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val cost = 50_000L
        if (userTeam.balanceEuro >= cost && player.isInjured) {
            userTeam.balanceEuro -= cost
            player.injuryWeeks = (player.injuryWeeks - 1).coerceAtLeast(0)
            if (player.injuryWeeks == 0) {
                player.injuryType = ""
            }
            player.condition = (player.condition + 10).coerceAtMost(100)
            _gameState.value = state.copy()
            RfmAudioEngine.playMenuBlip()
        }
    }

    fun simulateCupRound(cup: CupCompetition) {
        val state = _gameState.value
        val unplayedMatches = cup.matches.filter { !it.isPlayed }
        if (unplayedMatches.isEmpty()) return

        for (match in unplayedMatches) {
            val home = state.getTeamById(match.homeTeamId)
            val away = state.getTeamById(match.awayTeamId)
            val homeStrength = home?.averageOverall ?: 78
            val awayStrength = away?.averageOverall ?: 78

            val hGoals = ((homeStrength / 30) + (-1..2).random()).coerceAtLeast(0)
            val aGoals = ((awayStrength / 30) + (-1..2).random()).coerceAtLeast(0)

            match.homeScore = hGoals
            match.awayScore = aGoals
            match.isPlayed = true

            if (hGoals > aGoals) {
                match.winnerTeamId = match.homeTeamId
            } else if (aGoals > hGoals) {
                match.winnerTeamId = match.awayTeamId
            } else {
                // Decided on penalty shootout
                val pHome = (3..5).random()
                val pAway = if (Random.nextBoolean()) pHome + 1 else (pHome - 1).coerceAtLeast(2)
                match.penaltiesHome = pHome
                match.penaltiesAway = pAway
                match.winnerTeamId = if (pHome > pAway) match.homeTeamId else match.awayTeamId
            }
        }

        // If Quarter-Finals just finished, generate Semi-Finals
        val winners = cup.matches.filter { it.isPlayed }.mapNotNull { it.winnerTeamId }
        if (cup.matches.size == 4 && winners.size == 4) {
            cup.matches.add(CupMatch("semi_1", cup.name, "Semi-Final", winners[0], winners[1]))
            cup.matches.add(CupMatch("semi_2", cup.name, "Semi-Final", winners[2], winners[3]))
        } else if (cup.matches.size == 6 && winners.size == 6) {
            // Semi-Finals finished, generate Final
            val semiWinners = listOf(winners[4], winners[5])
            cup.matches.add(CupMatch("final", cup.name, "Grand Final", semiWinners[0], semiWinners[1]))
        } else if (cup.matches.size == 7 && winners.size == 7) {
            // Grand Final completed!
            val champId = winners[6]
            cup.isCompleted = true
            cup.winnerTeamId = champId

            if (champId == state.userTeamId) {
                state.trophiesWon.add("${cup.name} Winners (${state.currentSeasonYear})")
                val userTeam = state.getUserTeam()
                userTeam?.balanceEuro = (userTeam?.balanceEuro ?: 0L) + cup.prizeMoneyEuro
            }
        }

        _gameState.value = state.copy()
        RfmAudioEngine.playWhistle(longWhistle = true)
    }

    fun advanceToNextSeason() {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val userLeague = state.getUserLeague() ?: return

        // 1. Prize money based on position
        val userStanding = userLeague.standings.find { it.teamId == userTeam.id }
        val position = userLeague.standings.indexOf(userStanding) + 1
        val prizeMoney = when (position) {
            1 -> {
                state.trophiesWon.add("${userLeague.name} Champions (${state.currentSeasonYear})")
                40_000_000L
            }
            in 2..4 -> 25_000_000L
            in 5..8 -> 15_000_000L
            else -> 10_000_000L
        }

        userTeam.balanceEuro += prizeMoney
        userTeam.transferBudgetEuro += (prizeMoney * 0.7).toLong()

        // 2. Advance season year
        state.currentSeasonYear++
        state.currentMatchDay = 1

        // 3. Reset league standings
        for (league in state.leagues) {
            for (std in league.standings) {
                std.played = 0
                std.won = 0
                std.drawn = 0
                std.lost = 0
                std.goalsFor = 0
                std.goalsAgainst = 0
                std.points = 0
                std.formHistory.clear()
            }
            league.sortStandings()
        }

        // 4. Age players and progress youth
        for (league in state.leagues) {
            for (team in league.teams) {
                for (p in team.players) {
                    p.goalsScored = 0
                    p.assists = 0
                    p.matchesPlayed = 0
                    p.condition = 100
                    p.injuryWeeks = 0
                    p.injuryType = ""
                    p.contractYears = (p.contractYears - 1).coerceAtLeast(1)
                    if (p.age < 23 && p.overall < p.potential) {
                        p.overall = (p.overall + (1..3).random()).coerceAtMost(p.potential)
                    } else if (p.age > 32) {
                        p.pace = (p.pace - 2).coerceAtLeast(40)
                    }
                }
            }
        }

        // 5. Generate fresh fixtures for all leagues
        val newFixtures = mutableListOf<Fixture>()
        for (league in state.leagues) {
            newFixtures.addAll(RfmDatabase.generateRoundRobinFixtures(league.teams))
        }
        state.fixtures.clear()
        state.fixtures.addAll(newFixtures)

        // 6. Refresh youth prospects
        state.youthAcademyProspects.clear()
        state.youthAcademyProspects.addAll(listOf(
            YouthProspect("yp_gen_${state.currentSeasonYear}_1", "Marcus Rashford", PlayerPosition.ST, 16, "England", 68, 89, 2_500_000L, 8_000L, "Clinical Finisher"),
            YouthProspect("yp_gen_${state.currentSeasonYear}_2", "Trent Alexander-Arnold", PlayerPosition.RB, 15, "England", 67, 88, 2_200_000L, 7_000L, "Crossing Specialist"),
            YouthProspect("yp_gen_${state.currentSeasonYear}_3", "Phil Foden", PlayerPosition.CAM, 16, "England", 71, 91, 4_000_000L, 12_000L, "Silky Dribbler"),
            YouthProspect("yp_gen_${state.currentSeasonYear}_4", "Eduardo Camavinga", PlayerPosition.CM, 16, "France", 72, 90, 4_500_000L, 14_000L, "Engine Room")
        ))

        // 7. Refresh Cups
        state.cupCompetitions.clear()
        state.cupCompetitions.add(CupCompetition(
            id = "domestic_cup",
            name = "National FA Cup",
            trophyIcon = "🏆",
            prizeMoneyEuro = 15_000_000L,
            matches = mutableListOf(
                CupMatch("cup_qf_1", "National FA Cup", "Quarter-Final", "man_utd", "chelsea"),
                CupMatch("cup_qf_2", "National FA Cup", "Quarter-Final", "arsenal", "liverpool"),
                CupMatch("cup_qf_3", "National FA Cup", "Quarter-Final", "man_city", "tottenham"),
                CupMatch("cup_qf_4", "National FA Cup", "Quarter-Final", "everton", "newcastle")
            )
        ))
        state.cupCompetitions.add(CupCompetition(
            id = "champions_cup",
            name = "European Champions Trophy",
            trophyIcon = "🌟",
            prizeMoneyEuro = 35_000_000L,
            matches = mutableListOf(
                CupMatch("champ_qf_1", "European Champions Trophy", "Quarter-Final", "real_madrid", "bayern"),
                CupMatch("champ_qf_2", "European Champions Trophy", "Quarter-Final", "barcelona", "psg"),
                CupMatch("champ_qf_3", "European Champions Trophy", "Quarter-Final", "juventus", "dortmund"),
                CupMatch("champ_qf_4", "European Champions Trophy", "Quarter-Final", "man_utd", "milan")
            )
        ))

        state.newsInbox.add(0, NewsMessage(
            id = "news_season_${state.currentSeasonYear}",
            title = "New Season ${state.currentSeasonYear}/${state.currentSeasonYear + 1} Kicks Off!",
            content = "Board expectations are high. Prize money of €${prizeMoney / 1_000_000}M has been deposited to club funds.",
            dateString = "Aug 1, ${state.currentSeasonYear}",
            isImportant = true
        ))

        _gameState.value = state.copy()
        saveManager.saveGame("autosave", state)
        RfmAudioEngine.playWhistle(longWhistle = true)
    }

    fun answerPressConference(optionIndex: Int, fixture: Fixture) {
        val state = _gameState.value
        val userTeam = state.getUserTeam()
        when (optionIndex) {
            0 -> {
                // Attacking statement: boost morale & confidence
                userTeam?.players?.forEach { it.morale = (it.morale + 5).coerceAtMost(100) }
                state.boardConfidence = (state.boardConfidence + 2).coerceAtMost(100)
            }
            1 -> {
                // Tactical respect: boost defending
                userTeam?.players?.forEach { it.defending = (it.defending + 2).coerceAtMost(99) }
            }
            else -> {
                // Neutral
                state.managerReputation = (state.managerReputation + 1).coerceAtMost(100)
            }
        }
        _gameState.value = state.copy()
        startMatch(fixture)
    }

    fun handleKeypadInput(key: String) {
        RfmAudioEngine.playMenuBlip()
        // Virtual Samsung GT-S8000 keypad handling
        when (key) {
            "UP", "DOWN", "LEFT", "RIGHT", "OK" -> {
                // Tactical shortcut or quick navigation
            }
            "SOFT_LEFT" -> {
                // Contextual Back or Menu
                if (_currentScreen.value != RfmScreen.MAIN_MENU && _currentScreen.value != RfmScreen.DASHBOARD) {
                    navigateTo(RfmScreen.DASHBOARD)
                }
            }
            "SOFT_RIGHT" -> {
                // Quick options / Mod menu
                openModDialog()
            }
        }
    }

    fun upgradeBackroomStaff(staff: BackroomStaff) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val upgradeCost = staff.upgradeCostEuro
        if (staff.level < 5 && userTeam.balanceEuro >= upgradeCost) {
            userTeam.balanceEuro -= upgradeCost
            staff.level += 1
            staff.weeklySalaryEuro += 5_000L
            staff.perkDescription = when (staff.role) {
                "Assistant Manager" -> "Tactical opposition reports, auto-pick advice, +${staff.level * 2}% coaching tactical boost"
                "Head Scout" -> "Global scouting rating accuracy ${(70 + staff.level * 6)}%, reveals hidden wonderkid attributes"
                "Chief Physio" -> "Injury prevention bonus -${staff.level * 8}%, rapid recovery clinic (-1 week treatment)"
                else -> "Match stamina drain reduced by ${staff.level * 5}%, accelerates recovery between fixtures"
            }
            RfmAudioEngine.playCashChime()
            _gameState.value = state.copy()
            saveManager.saveGame("autosave", state)
        }
    }

    fun playPreSeasonTourMatch(tourFixture: PreSeasonTourFixture) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val opponent = Team(
            id = "tour_opp_${tourFixture.id}",
            name = tourFixture.opponentName,
            shortName = tourFixture.opponentName.take(3).uppercase(),
            leagueId = "INT",
            formation = Formation.F_442,
            mentality = Mentality.BALANCED
        )
        if (opponent.players.isEmpty()) {
            val oppSquad = List(16) { i ->
                val pos = if (i == 0) PlayerPosition.GK else if (i < 5) PlayerPosition.CB else if (i < 9) PlayerPosition.CM else PlayerPosition.ST
                Player(
                    id = "tour_p_${tourFixture.id}_$i",
                    name = "Player $i",
                    position = pos,
                    age = 26,
                    nationality = tourFixture.opponentCountry,
                    overall = tourFixture.opponentStrength,
                    pace = tourFixture.opponentStrength,
                    shooting = tourFixture.opponentStrength,
                    passing = tourFixture.opponentStrength,
                    dribbling = tourFixture.opponentStrength,
                    defending = tourFixture.opponentStrength,
                    physical = tourFixture.opponentStrength,
                    isStarting = i < 11,
                    isSubstitute = i >= 11
                )
            }
            opponent.players.addAll(oppSquad)
        }

        val fixture = Fixture(
            id = "fixture_tour_${tourFixture.id}",
            round = 0,
            homeTeamId = userTeam.id,
            awayTeamId = opponent.id
        )

        userTeam.balanceEuro += tourFixture.commercialBonusEuro
        tourFixture.isPlayed = true

        val engine = MatchSimulationEngine(
            fixture = fixture,
            homeTeam = userTeam,
            awayTeam = opponent,
            modSettings = state.modSettings
        )
        _activeEngine.value = engine
        navigateTo(RfmScreen.MATCH_DAY)
    }

    fun simulatePreSeasonTourMatch(tourFixture: PreSeasonTourFixture) {
        val state = _gameState.value
        val userTeam = state.getUserTeam() ?: return
        val userStrength = userTeam.averageOverall
        val oppStrength = tourFixture.opponentStrength

        val userGoals = if (userStrength >= oppStrength) Random.nextInt(1, 5) else Random.nextInt(0, 3)
        val oppGoals = if (oppStrength > userStrength) Random.nextInt(1, 4) else Random.nextInt(0, 2)

        tourFixture.isPlayed = true
        tourFixture.userScore = userGoals
        tourFixture.opponentScore = oppGoals
        userTeam.balanceEuro += tourFixture.commercialBonusEuro

        userTeam.players.forEach {
            it.condition = (it.condition + 10).coerceAtMost(100)
            it.morale = (it.morale + 5).coerceAtMost(100)
        }

        RfmAudioEngine.playWhistle()
        _gameState.value = state.copy()
        saveManager.saveGame("autosave", state)
    }
}
