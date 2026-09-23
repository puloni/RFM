package com.example.rfm.engine.service

import com.example.rfm.model.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Result data returned by the service simulation.
 */
data class MatchSimulationResult(
    val fixtureId: String,
    val homeScore: Int,
    val awayScore: Int,
    val penaltyShootoutHomeScore: Int? = null,
    val penaltyShootoutAwayScore: Int? = null,
    val homeXg: Float,
    val awayXg: Float,
    val events: List<MatchEvent>,
    val stats: MatchStats,
    val shotMap: List<ShotDetail>,
    val playerRatings: Map<String, Float>,
    val manOfTheMatchId: String?,
    val manOfTheMatchName: String?,
    val weather: MatchWeather
)

/**
 * Probabilistic odds estimation for a match before kickoff.
 */
data class MatchOdds(
    val homeWinProbability: Float, // e.g. 0.52 (52%)
    val drawProbability: Float,    // e.g. 0.26 (26%)
    val awayWinProbability: Float, // e.g. 0.22 (22%)
    val expectedHomeGoals: Float,
    val expectedAwayGoals: Float
)

/**
 * Service-based football match simulation engine.
 * Simulates outcomes with a randomized probability model taking into account:
 * - Team strength (overall, positional units, role synergy)
 * - Player fatigue (condition decay curves, stamina penalties, weather friction)
 * - Tactical settings (formations, mentalities, pressing, passing style, aggression)
 */
class MatchSimulationService {

    /**
     * Estimates pre-match odds and projected scores based on team ratings and tactics.
     */
    fun calculateOdds(home: Team, away: Team, weather: MatchWeather = MatchWeather.CLEAR): MatchOdds {
        val homePwr = calculateTeamPower(home, isHome = true, weather = weather)
        val awayPwr = calculateTeamPower(away, isHome = false, weather = weather)

        val diff = (homePwr - awayPwr).toFloat()
        // Logistic / sigmoid probability distribution
        val homeWinP = (1.0f / (1.0f + exp(-0.06f * (diff + 6f)))).coerceIn(0.10f, 0.85f)
        val awayWinP = (1.0f / (1.0f + exp(0.06f * (diff - 4f)))).coerceIn(0.08f, 0.80f)
        val drawP = (1.0f - homeWinP - awayWinP).coerceIn(0.12f, 0.40f)

        val total = homeWinP + drawP + awayWinP
        val normHome = homeWinP / total
        val normDraw = drawP / total
        val normAway = awayWinP / total

        val xgH = (1.4f + (diff * 0.04f)).coerceIn(0.3f, 4.2f)
        val xgA = (1.0f - (diff * 0.035f)).coerceIn(0.2f, 3.8f)

        return MatchOdds(
            homeWinProbability = normHome,
            drawProbability = normDraw,
            awayWinProbability = normAway,
            expectedHomeGoals = xgH,
            expectedAwayGoals = xgA
        )
    }

    /**
     * Executes a full match simulation through the probability model.
     */
    fun simulateMatch(
        fixture: Fixture,
        homeTeam: Team,
        awayTeam: Team,
        modSettings: ModSettings,
        weather: MatchWeather = fixture.weather
    ): MatchSimulationResult {
        var currentHomeScore = 0
        var currentAwayScore = 0
        var homeXg = 0.0f
        var awayXg = 0.0f

        val events = mutableListOf<MatchEvent>()
        val stats = MatchStats()
        val shotMap = mutableListOf<ShotDetail>()

        // Copy players' starting conditions to simulate in-match fatigue degradation
        val homeStarting = homeTeam.getStartingXI()
        val awayStarting = awayTeam.getStartingXI()

        // Track player match rating accumulators
        val playerImpactPoints = mutableMapOf<String, Float>()
        (homeStarting + awayStarting).forEach { playerImpactPoints[it.id] = 6.0f }

        events.add(
            MatchEvent(
                minute = 0,
                type = MatchEventType.KICKOFF,
                teamId = homeTeam.id,
                description = "Kickoff in ${weather.icon} ${weather.label} conditions (${weather.tempCelsius}°C). ${homeTeam.name} vs ${awayTeam.name} begins!",
                homeScore = 0,
                awayScore = 0
            )
        )

        val maxMinutes = if (fixture.competitionType.hasExtraTimeAndPenalties) 90 else 90

        // Minute-by-minute stochastic simulation loop
        for (minute in 1..maxMinutes) {
            // 1. Fatigue degradation
            updateFatigue(homeTeam, awayTeam, minute, weather, modSettings)

            // Half time milestone
            if (minute == 45) {
                events.add(
                    MatchEvent(
                        minute = 45,
                        type = MatchEventType.HALF_TIME,
                        teamId = homeTeam.id,
                        description = "Half-Time whistle blown by referee! Score: ${homeTeam.shortName} $currentHomeScore - $currentAwayScore ${awayTeam.shortName}.",
                        homeScore = currentHomeScore,
                        awayScore = currentAwayScore
                    )
                )
                // Half-time rest recovery (+4% condition)
                recoverHalfTimeFatigue(homeStarting, awayStarting, modSettings)
                continue
            }

            // 2. Midfield duel & Possession dominance
            val homeMid = calculateUnitRating(homeTeam, "Midfielder", isHome = true, weather, modSettings)
            val awayMid = calculateUnitRating(awayTeam, "Midfielder", isHome = false, weather, modSettings)

            val midfieldDominance = (homeMid.toDouble().pow(1.4) / (homeMid.toDouble().pow(1.4) + awayMid.toDouble().pow(1.4))).toFloat()
            val isHomeAttacking = Random.nextFloat() < midfieldDominance

            // Accumulate passing metrics
            if (isHomeAttacking) {
                stats.homePassesAttempted += 4
                if (Random.nextInt(100) < (78 + weather.passAccuracyModifier)) stats.homePassesCompleted += 4
            } else {
                stats.awayPassesAttempted += 4
                if (Random.nextInt(100) < (76 + weather.passAccuracyModifier)) stats.awayPassesCompleted += 4
            }

            // 3. Chance Occurrence check (Poisson stochastic probability: ~17% chance per minute)
            val chanceFrequency = calculateChanceFrequency(
                team = if (isHomeAttacking) homeTeam else awayTeam,
                opponent = if (isHomeAttacking) awayTeam else homeTeam
            )

            if (Random.nextFloat() < chanceFrequency) {
                val attackingTeam = if (isHomeAttacking) homeTeam else awayTeam
                val defendingTeam = if (isHomeAttacking) awayTeam else homeTeam

                val roll = Random.nextInt(100)
                when {
                    // Goal / Shot Opportunity (60%)
                    roll < 60 -> {
                        val outcome = processShotChance(
                            isHome = isHomeAttacking,
                            minute = minute,
                            attackingTeam = attackingTeam,
                            defendingTeam = defendingTeam,
                            weather = weather,
                            modSettings = modSettings,
                            stats = stats,
                            shotMap = shotMap,
                            playerImpact = playerImpactPoints
                        )

                        if (outcome.isGoal) {
                            if (isHomeAttacking) currentHomeScore++ else currentAwayScore++
                        }
                        if (isHomeAttacking) homeXg += outcome.xG else awayXg += outcome.xG

                        outcome.event?.let { events.add(it.copy(homeScore = currentHomeScore, awayScore = currentAwayScore)) }
                    }

                    // Tackling Duel / Foul / Cards (25%)
                    roll in 60..84 -> {
                        val event = processDiscipline(
                            isHomeAttacking = isHomeAttacking,
                            minute = minute,
                            attackingTeam = attackingTeam,
                            defendingTeam = defendingTeam,
                            weather = weather,
                            stats = stats,
                            playerImpact = playerImpactPoints,
                            homeScore = currentHomeScore,
                            awayScore = currentAwayScore
                        )
                        event?.let { events.add(it) }
                    }

                    // Corner Kick (15%)
                    else -> {
                        if (isHomeAttacking) stats.homeCorners++ else stats.awayCorners++
                        val taker = attackingTeam.getStartingXI().find { it.id == attackingTeam.cornerTakerId }
                            ?: attackingTeam.getStartingXI().random()
                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = MatchEventType.CORNER,
                                teamId = attackingTeam.id,
                                primaryPlayerName = taker.name,
                                description = "Corner kick awarded to ${attackingTeam.name}. Curled in by ${taker.name}.",
                                homeScore = currentHomeScore,
                                awayScore = currentAwayScore
                            )
                        )
                    }
                }
            }
        }

        // Finalize 90 minutes possession
        val totalPasses = max(1, stats.homePassesCompleted + stats.awayPassesCompleted)
        stats.homePossession = ((stats.homePassesCompleted.toFloat() / totalPasses) * 100).toInt().coerceIn(25, 75)
        stats.awayPossession = 100 - stats.homePossession
        stats.homeXg = (round(homeXg * 100) / 100f)
        stats.awayXg = (round(awayXg * 100) / 100f)

        // Extra Time & Penalty Shootout for Knockout Cup Matches if drawn
        var penaltyH: Int? = null
        var penaltyA: Int? = null
        if (fixture.competitionType.hasExtraTimeAndPenalties && currentHomeScore == currentAwayScore) {
            events.add(
                MatchEvent(
                    minute = 90,
                    type = MatchEventType.EXTRA_TIME_START,
                    teamId = "",
                    description = "90 minutes end in a draw! Extra Time (30 mins) begins.",
                    homeScore = currentHomeScore,
                    awayScore = currentAwayScore
                )
            )

            // Simulate Extra Time chances (105 and 120 min)
            if (Random.nextInt(100) < 35) {
                currentHomeScore++
                events.add(
                    MatchEvent(
                        minute = 108,
                        type = MatchEventType.GOAL,
                        teamId = homeTeam.id,
                        description = "EXTRA TIME DRAMA! ${homeTeam.name} score a sensational header!",
                        homeScore = currentHomeScore,
                        awayScore = currentAwayScore
                    )
                )
            } else if (Random.nextInt(100) < 30) {
                currentAwayScore++
                events.add(
                    MatchEvent(
                        minute = 114,
                        type = MatchEventType.GOAL,
                        teamId = awayTeam.id,
                        description = "EXTRA TIME DRAMA! ${awayTeam.name} find the net from long range!",
                        homeScore = currentHomeScore,
                        awayScore = currentAwayScore
                    )
                )
            }

            // If still drawn after Extra Time -> Penalty Shootout!
            if (currentHomeScore == currentAwayScore) {
                events.add(
                    MatchEvent(
                        minute = 120,
                        type = MatchEventType.PENALTY_SHOOTOUT,
                        teamId = "",
                        description = "Extra Time ends ${currentHomeScore}-${currentAwayScore}. Proceeding to Penalty Shootout!",
                        homeScore = currentHomeScore,
                        awayScore = currentAwayScore
                    )
                )

                // Simulate 5 penalties each
                var penH = 0
                var penA = 0
                for (k in 1..5) {
                    if (Random.nextInt(100) < 78) penH++
                    if (Random.nextInt(100) < 76) penA++
                }
                while (penH == penA) {
                    if (Random.nextInt(100) < 70) penH++
                    if (Random.nextInt(100) < 70) penA++
                }
                penaltyH = penH
                penaltyA = penA

                events.add(
                    MatchEvent(
                        minute = 120,
                        type = MatchEventType.PENALTY_GOAL,
                        teamId = if (penH > penA) homeTeam.id else awayTeam.id,
                        description = "PENALTY SHOOTOUT FINISHED! ${homeTeam.shortName} $penH - $penA ${awayTeam.shortName}.",
                        homeScore = currentHomeScore,
                        awayScore = currentAwayScore
                    )
                )
            }
        }

        events.add(
            MatchEvent(
                minute = 90,
                type = MatchEventType.FULL_TIME,
                teamId = homeTeam.id,
                description = "Full-Time whistle! ${homeTeam.name} $currentHomeScore - $currentAwayScore ${awayTeam.name} (xG: ${stats.homeXg} - ${stats.awayXg}).",
                homeScore = currentHomeScore,
                awayScore = currentAwayScore
            )
        )

        // Calculate final player ratings (1.0 to 10.0 scale)
        val finalRatings = mutableMapOf<String, Float>()
        var topRating = 0.0f
        var motmPlayer: Player? = null

        (homeStarting + awayStarting).forEach { p ->
            val basePts = playerImpactPoints[p.id] ?: 6.0f
            val rating = (basePts).coerceIn(4.0f, 9.9f)
            val rounded = round(rating * 10) / 10f
            finalRatings[p.id] = rounded
            p.matchRating = rounded
            p.matchesPlayed++

            if (rounded > topRating) {
                topRating = rounded
                motmPlayer = p
            }
        }

        // Apply result to fixture
        fixture.homeScore = currentHomeScore
        fixture.awayScore = currentAwayScore
        fixture.penaltyShootoutHomeScore = penaltyH
        fixture.penaltyShootoutAwayScore = penaltyA
        fixture.weather = weather
        fixture.isPlayed = true
        fixture.events.clear()
        fixture.events.addAll(events)
        fixture.stats = stats
        fixture.shotMap.clear()
        fixture.shotMap.addAll(shotMap)
        fixture.playerRatings.clear()
        fixture.playerRatings.putAll(finalRatings)
        fixture.manOfTheMatchId = motmPlayer?.id
        fixture.manOfTheMatchName = motmPlayer?.name

        return MatchSimulationResult(
            fixtureId = fixture.id,
            homeScore = currentHomeScore,
            awayScore = currentAwayScore,
            penaltyShootoutHomeScore = penaltyH,
            penaltyShootoutAwayScore = penaltyA,
            homeXg = stats.homeXg,
            awayXg = stats.awayXg,
            events = events,
            stats = stats,
            shotMap = shotMap,
            playerRatings = finalRatings,
            manOfTheMatchId = motmPlayer?.id,
            manOfTheMatchName = motmPlayer?.name,
            weather = weather
        )
    }

    private data class ShotOutcomeResult(
        val isGoal: Boolean,
        val xG: Float,
        val event: MatchEvent?
    )

    private fun processShotChance(
        isHome: Boolean,
        minute: Int,
        attackingTeam: Team,
        defendingTeam: Team,
        weather: MatchWeather,
        modSettings: ModSettings,
        stats: MatchStats,
        shotMap: MutableList<ShotDetail>,
        playerImpact: MutableMap<String, Float>
    ): ShotOutcomeResult {
        if (isHome) stats.homeShots++ else stats.awayShots++

        val shooters = attackingTeam.getStartingXI().filter {
            it.position.roleCategory == "Forward" || it.position.roleCategory == "Midfielder"
        }.ifEmpty { attackingTeam.getStartingXI() }
        val shooter = shooters.random()

        val role = shooter.tacticalRole
        val shooterFinishing = shooter.shooting + role.shotBonus
        val fatigueFactor = (shooter.condition / 100f).pow(1.2f)

        // Pitch location calculation (normalized x, y)
        val pitchY = if (isHome) Random.nextFloat() * 0.22f + 0.78f else Random.nextFloat() * 0.22f + 0.02f
        val pitchX = Random.nextFloat() * 0.6f + 0.2f

        // Distance to goal center (0.5, 1.0 or 0.5, 0.0)
        val distToGoal = sqrt((pitchX - 0.5f).pow(2) + (if (isHome) 1.0f - pitchY else pitchY).pow(2))
        // Base xG calculated from distance and angle
        var chanceXg = (0.55f / (1.0f + (distToGoal * 10f))).coerceIn(0.04f, 0.75f)
        if (role == PlayerTacticalRole.POACHER && distToGoal < 0.15f) chanceXg *= 1.35f
        if (role == PlayerTacticalRole.INSIDE_FORWARD) chanceXg *= 1.15f

        val gk = defendingTeam.getStartingXI().firstOrNull { it.position == PlayerPosition.GK } ?: defendingTeam.getStartingXI().first()
        val gkShotStopping = (gk.overall * (gk.condition / 100f)).toInt() + gk.tacticalRole.defBonus

        val goalProbability = (chanceXg * (shooterFinishing.toFloat() / max(1, gkShotStopping)) * fatigueFactor)
            .coerceIn(0.05f, 0.85f)

        val roll = Random.nextFloat()

        return if (roll < goalProbability) {
            // GOAL
            shooter.goalsScored++
            if (isHome) stats.homeShotsOnTarget++ else stats.awayShotsOnTarget++
            playerImpact[shooter.id] = (playerImpact[shooter.id] ?: 6.0f) + 1.4f
            playerImpact[gk.id] = (playerImpact[gk.id] ?: 6.0f) - 0.3f

            val assister = attackingTeam.getStartingXI().filter { it.id != shooter.id }.randomOrNull()
            assister?.let {
                it.assists++
                playerImpact[it.id] = (playerImpact[it.id] ?: 6.0f) + 0.8f
            }

            shotMap.add(ShotDetail(minute, isHome, shooter.name, chanceXg, ShotOutcome.GOAL, pitchX, pitchY))

            val desc = if (assister != null) {
                "GOAL! ${shooter.name} (${attackingTeam.shortName}) finishes clinically into the bottom corner! Assist by ${assister.name}."
            } else {
                "GOAL! A magnificent solo strike by ${shooter.name} (${attackingTeam.shortName}) finds the roof of the net!"
            }

            ShotOutcomeResult(
                isGoal = true,
                xG = chanceXg,
                event = MatchEvent(
                    minute = minute,
                    type = MatchEventType.GOAL,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    secondaryPlayerName = assister?.name,
                    description = desc,
                    homeScore = 0,
                    awayScore = 0
                )
            )
        } else if (roll < goalProbability + 0.35f) {
            // SAVED
            if (isHome) stats.homeShotsOnTarget++ else stats.awayShotsOnTarget++
            playerImpact[gk.id] = (playerImpact[gk.id] ?: 6.0f) + 0.4f
            shotMap.add(ShotDetail(minute, isHome, shooter.name, chanceXg, ShotOutcome.SAVED, pitchX, pitchY))

            ShotOutcomeResult(
                isGoal = false,
                xG = chanceXg,
                event = MatchEvent(
                    minute = minute,
                    type = MatchEventType.SHOT_SAVED,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    secondaryPlayerName = gk.name,
                    description = "Brilliant diving save by ${gk.name}! Parries away ${shooter.name}'s shot.",
                    homeScore = 0,
                    awayScore = 0
                )
            )
        } else if (roll < goalProbability + 0.45f) {
            // WOODWORK
            shotMap.add(ShotDetail(minute, isHome, shooter.name, chanceXg, ShotOutcome.WOODWORK, pitchX, pitchY))
            ShotOutcomeResult(
                isGoal = false,
                xG = chanceXg,
                event = MatchEvent(
                    minute = minute,
                    type = MatchEventType.SHOT_WOODWORK,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    description = "Off the post! ${shooter.name}'s stinging drive strikes the upright.",
                    homeScore = 0,
                    awayScore = 0
                )
            )
        } else {
            // OFF TARGET / BLOCKED
            shotMap.add(ShotDetail(minute, isHome, shooter.name, chanceXg, ShotOutcome.OFF_TARGET, pitchX, pitchY))
            ShotOutcomeResult(
                isGoal = false,
                xG = chanceXg,
                event = MatchEvent(
                    minute = minute,
                    type = MatchEventType.SHOT_OFF_TARGET,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    description = "${shooter.name} unleashes a shot from range, but it flies wide of the target.",
                    homeScore = 0,
                    awayScore = 0
                )
            )
        }
    }

    private fun processDiscipline(
        isHomeAttacking: Boolean,
        minute: Int,
        attackingTeam: Team,
        defendingTeam: Team,
        weather: MatchWeather,
        stats: MatchStats,
        playerImpact: MutableMap<String, Float>,
        homeScore: Int,
        awayScore: Int
    ): MatchEvent? {
        if (isHomeAttacking) stats.awayFouls++ else stats.homeFouls++
        val fouler = defendingTeam.getStartingXI().random()

        val cardMultiplier = defendingTeam.tacklingStyle.cardRisk * weather.injuryRiskMultiplier
        val cardRoll = Random.nextFloat() * 100f

        return if (cardRoll < (18f * cardMultiplier)) {
            // Yellow Card
            if (isHomeAttacking) stats.awayYellowCards++ else stats.homeYellowCards++
            fouler.yellowCards++
            playerImpact[fouler.id] = (playerImpact[fouler.id] ?: 6.0f) - 0.5f

            MatchEvent(
                minute = minute,
                type = MatchEventType.YELLOW_CARD,
                teamId = defendingTeam.id,
                primaryPlayerName = fouler.name,
                description = "Yellow card shown to ${fouler.name} (${defendingTeam.shortName}) for a cynical challenge.",
                homeScore = homeScore,
                awayScore = awayScore
            )
        } else {
            MatchEvent(
                minute = minute,
                type = MatchEventType.FOUL,
                teamId = defendingTeam.id,
                primaryPlayerName = fouler.name,
                description = "Foul committed by ${fouler.name}. Free kick given to ${attackingTeam.shortName}.",
                homeScore = homeScore,
                awayScore = awayScore
            )
        }
    }

    private fun calculateChanceFrequency(team: Team, opponent: Team): Float {
        var base = 0.17f
        when (team.mentality) {
            Mentality.ULTRA_DEFENSIVE -> base -= 0.06f
            Mentality.DEFENSIVE -> base -= 0.03f
            Mentality.BALANCED -> {}
            Mentality.ATTACKING -> base += 0.04f
            Mentality.ALL_OUT_ATTACK -> base += 0.08f
        }
        if (team.pressingStyle == PressingStyle.HIGH_PRESS) base += 0.03f
        if (opponent.pressingStyle == PressingStyle.HIGH_PRESS) base += 0.02f // Gegenpress creates back-and-forth chances
        return base.coerceIn(0.08f, 0.32f)
    }

    private fun updateFatigue(home: Team, away: Team, minute: Int, weather: MatchWeather, modSettings: ModSettings) {
        if (modSettings.noPlayerFatigue || modSettings.disableFatigueEffects) return
        if (minute % 8 != 0) return

        val weatherMultiplier = weather.fatigueDrainMultiplier

        applyTeamFatigue(home, weatherMultiplier)
        applyTeamFatigue(away, weatherMultiplier)
    }

    private fun applyTeamFatigue(team: Team, weatherMultiplier: Float) {
        val pressingMult = when (team.pressingStyle) {
            PressingStyle.HIGH_PRESS -> 1.4f
            PressingStyle.STANDARD -> 1.0f
            PressingStyle.OWN_HALF -> 0.8f
        }

        team.getStartingXI().forEach { p ->
            val roleDrain = p.tacticalRole.staminaDrainMultiplier
            val drain = (1.5f * pressingMult * weatherMultiplier * roleDrain).toInt().coerceAtLeast(1)
            p.condition = (p.condition - drain).coerceAtLeast(35)
        }
    }

    private fun recoverHalfTimeFatigue(home: List<Player>, away: List<Player>, modSettings: ModSettings) {
        if (modSettings.noPlayerFatigue || modSettings.disableFatigueEffects) return
        home.forEach { it.condition = (it.condition + 5).coerceAtMost(100) }
        away.forEach { it.condition = (it.condition + 5).coerceAtMost(100) }
    }

    private fun calculateTeamPower(team: Team, isHome: Boolean, weather: MatchWeather): Int {
        val starting = team.getStartingXI()
        if (starting.isEmpty()) return 70

        var base = (starting.sumOf { it.overall } / starting.size.toDouble()).toInt()
        base += if (isHome) 3 else 0
        base += team.mentality.attackBonus / 2
        return base
    }

    private fun calculateUnitRating(
        team: Team,
        category: String,
        isHome: Boolean,
        weather: MatchWeather,
        modSettings: ModSettings
    ): Int {
        val starting = team.getStartingXI()
        val unit = starting.filter { it.position.roleCategory == category }.ifEmpty { starting }
        val avg = unit.sumOf {
            val condFactor = (it.condition / 100f)
            (it.overall * condFactor).toInt()
        } / unit.size
        return avg + (if (isHome) 2 else 0)
    }
}
