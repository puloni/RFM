package com.example.rfm.model

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class PitchBall(
    var x: Float = 50f, // 0 to 100%
    var y: Float = 50f  // 0 to 100%
)

data class PitchPlayerPosition(
    val playerId: String,
    val name: String,
    val isHome: Boolean,
    var x: Float, // 0 to 100%
    var y: Float  // 0 to 100%
)

class MatchEngine(
    val homeTeam: Team,
    val awayTeam: Team,
    val isUserHome: Boolean,
    val isUserAway: Boolean,
    val modSettings: ModSettings
) {
    var currentMinute: Int = 0
    var homeScore: Int = 0
    var awayScore: Int = 0
    var isHalfTime: Boolean = false
    var isFullTime: Boolean = false
    val events: MutableList<MatchEvent> = mutableListOf()
    val stats: MatchStats = MatchStats()

    var ball: PitchBall = PitchBall(50f, 50f)
    val pitchPlayers: MutableList<PitchPlayerPosition> = mutableListOf()

    var userSubstitutionsLeft: Int = 3
    val matchBookings: MutableMap<String, Int> = mutableMapOf()

    init {
        initPitchPlayers()
        // Record Kickoff
        events.add(
            MatchEvent(
                minute = 0,
                type = MatchEventType.KICKOFF,
                teamId = homeTeam.id,
                description = "Referee signals kickoff! ${homeTeam.name} vs ${awayTeam.name} is underway.",
                homeScore = 0,
                awayScore = 0
            )
        )
    }

    private fun initPitchPlayers() {
        pitchPlayers.clear()
        // Home starting players on left side (x: 10% to 45%)
        val homeStarting = homeTeam.getStartingXI()
        homeStarting.forEachIndexed { index, p ->
            val pos = getFormationCoords(index, homeStarting.size, isHome = true)
            pitchPlayers.add(PitchPlayerPosition(p.id, p.name, isHome = true, x = pos.first, y = pos.second))
        }

        // Away starting players on right side (x: 55% to 90%)
        val awayStarting = awayTeam.getStartingXI()
        awayStarting.forEachIndexed { index, p ->
            val pos = getFormationCoords(index, awayStarting.size, isHome = false)
            pitchPlayers.add(PitchPlayerPosition(p.id, p.name, isHome = false, x = pos.first, y = pos.second))
        }
    }

    private fun getFormationCoords(index: Int, total: Int, isHome: Boolean): Pair<Float, Float> {
        val ySpread = (index + 1) * (90f / (total + 1)) + 5f
        val xBase = when {
            index == 0 -> if (isHome) 8f else 92f // GK
            index in 1..4 -> if (isHome) 22f else 78f // Defenders
            index in 5..8 -> if (isHome) 36f else 64f // Midfielders
            else -> if (isHome) 46f else 54f // Forwards
        }
        return Pair(xBase, ySpread)
    }

    fun stepMinute(): MatchEvent? {
        if (isFullTime) return null

        currentMinute++

        // Update fatigue unless mod enabled
        updatePlayerFatigue()

        // Animate ball & player movements on pitch
        animatePitchAction()

        var newEvent: MatchEvent? = null

        // Half Time check
        if (currentMinute == 45 && !isHalfTime) {
            isHalfTime = true
            newEvent = MatchEvent(
                minute = 45,
                type = MatchEventType.HALF_TIME,
                teamId = homeTeam.id,
                description = "Half Time whistle! Score: ${homeTeam.shortName} $homeScore - $awayScore ${awayTeam.shortName}.",
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(newEvent)
            return newEvent
        }

        // Full Time check
        if (currentMinute >= 90) {
            isFullTime = true
            newEvent = MatchEvent(
                minute = 90,
                type = MatchEventType.FULL_TIME,
                teamId = homeTeam.id,
                description = "Full Time! Final result: ${homeTeam.name} $homeScore - $awayScore ${awayTeam.name}.",
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(newEvent)
            return newEvent
        }

        // Simulate phase of play
        newEvent = simulateMinuteChance()
        if (newEvent != null) {
            events.add(newEvent)
        }

        // Update live possession
        val homePower = getEffectivePower(homeTeam, isHome = true)
        val awayPower = getEffectivePower(awayTeam, isHome = false)
        val totalPower = max(1, homePower + awayPower)
        stats.homePossession = min(75, max(25, ((homePower.toDouble() / totalPower) * 100).toInt()))
        stats.awayPossession = 100 - stats.homePossession

        return newEvent
    }

    private fun getEffectivePower(team: Team, isHome: Boolean): Int {
        val isUser = (isHome && isUserHome) || (!isHome && isUserAway)
        var strength = team.calculateTeamStrength()
        if (isUser && modSettings.maxPlayerStats) {
            strength = 99
        }
        return strength + if (isHome) 3 else 0
    }

    private fun updatePlayerFatigue() {
        val userActiveNoFatigue = modSettings.noPlayerFatigue || modSettings.disableFatigueEffects
        if (isUserHome && userActiveNoFatigue) {
            homeTeam.getStartingXI().forEach { it.condition = 100 }
        } else {
            homeTeam.getStartingXI().forEach { it.condition = max(40, it.condition - if (Random.nextInt(10) == 0) 1 else 0) }
        }

        if (isUserAway && userActiveNoFatigue) {
            awayTeam.getStartingXI().forEach { it.condition = 100 }
        } else {
            awayTeam.getStartingXI().forEach { it.condition = max(40, it.condition - if (Random.nextInt(10) == 0) 1 else 0) }
        }
    }

    private fun animatePitchAction() {
        // Move ball towards attacking side
        val targetX = if (Random.nextBoolean()) Random.nextFloat() * 40f + 10f else Random.nextFloat() * 40f + 50f
        val targetY = Random.nextFloat() * 80f + 10f
        ball.x = ball.x * 0.7f + targetX * 0.3f
        ball.y = ball.y * 0.7f + targetY * 0.3f

        // Small jitter movement for players near ball
        pitchPlayers.forEach { p ->
            val dx = (ball.x - p.x) * 0.05f
            val dy = (ball.y - p.y) * 0.05f
            p.x = min(95f, max(5f, p.x + dx + (Random.nextFloat() - 0.5f) * 1.5f))
            p.y = min(95f, max(5f, p.y + dy + (Random.nextFloat() - 0.5f) * 1.5f))
        }
    }

    private fun simulateMinuteChance(): MatchEvent? {
        // ~1 in 7 chance of a notable action occurring per minute
        if (Random.nextInt(7) != 0) return null

        val homeAtt = if (isUserHome && modSettings.maxPlayerStats) 99 else homeTeam.calculateAttackRating()
        val awayDef = if (isUserAway && modSettings.maxPlayerStats) 99 else awayTeam.calculateDefenseRating()
        val awayAtt = if (isUserAway && modSettings.maxPlayerStats) 99 else awayTeam.calculateAttackRating()
        val homeDef = if (isUserHome && modSettings.maxPlayerStats) 99 else homeTeam.calculateDefenseRating()

        val homeChanceWeight = max(10, homeAtt - awayDef / 2 + 15)
        val awayChanceWeight = max(10, awayAtt - homeDef / 2 + 10)

        val isHomeAction = Random.nextInt(homeChanceWeight + awayChanceWeight) < homeChanceWeight
        val attackingTeam = if (isHomeAction) homeTeam else awayTeam
        val defendingTeam = if (isHomeAction) awayTeam else homeTeam
        val isUserAttacking = (isHomeAction && isUserHome) || (!isHomeAction && isUserAway)

        val shooters = attackingTeam.getStartingXI().filter {
            it.position.roleCategory == "Forward" || it.position.roleCategory == "Midfielder"
        }.ifEmpty { attackingTeam.getStartingXI() }
        val shooter = shooters.randomOrNull() ?: return null

        // Offside Trap Check
        if (defendingTeam.offsideTrap && Random.nextInt(100) < 22) {
            return MatchEvent(
                minute = currentMinute,
                type = MatchEventType.FOUL,
                teamId = defendingTeam.id,
                primaryPlayerName = shooter.name,
                description = "Offside flag raised! ${defendingTeam.shortName}'s well-timed offside trap catches ${shooter.name} offside.",
                homeScore = homeScore,
                awayScore = awayScore
            )
        }

        val roll = Random.nextInt(100)

        return when {
            // Foul / Card chance (15%)
            roll < 15 -> {
                val foulers = defendingTeam.getStartingXI().filter { it.position.roleCategory == "Defender" || it.position.roleCategory == "Midfielder" }
                val fouler = foulers.randomOrNull() ?: defendingTeam.getStartingXI().random()
                if (isHomeAction) stats.awayFouls++ else stats.homeFouls++

                val cardProb = (30 * defendingTeam.tacklingStyle.cardRisk).toInt()
                val isCard = Random.nextInt(100) < cardProb

                if (isCard) {
                    val currentYellows = matchBookings.getOrDefault(fouler.id, 0) + 1
                    matchBookings[fouler.id] = currentYellows
                    fouler.yellowCards++

                    if (currentYellows >= 2) {
                        fouler.isRedCard = true
                        fouler.suspensionMatchesRemaining = 1
                        if (isHomeAction) stats.awayRedCards++ else stats.homeRedCards++
                        MatchEvent(
                            minute = currentMinute,
                            type = MatchEventType.RED_CARD,
                            teamId = defendingTeam.id,
                            primaryPlayerName = fouler.name,
                            description = "RED CARD! Second yellow shown to ${fouler.name} (${defendingTeam.shortName}) who is sent off!",
                            homeScore = homeScore,
                            awayScore = awayScore
                        )
                    } else {
                        if (isHomeAction) stats.awayYellowCards++ else stats.homeYellowCards++
                        MatchEvent(
                            minute = currentMinute,
                            type = MatchEventType.YELLOW_CARD,
                            teamId = defendingTeam.id,
                            primaryPlayerName = fouler.name,
                            description = "Yellow card shown to ${fouler.name} (${defendingTeam.shortName}) for a rough tackle.",
                            homeScore = homeScore,
                            awayScore = awayScore
                        )
                    }
                } else {
                    MatchEvent(
                        minute = currentMinute,
                        type = MatchEventType.FOUL,
                        teamId = defendingTeam.id,
                        primaryPlayerName = fouler.name,
                        secondaryPlayerName = shooter.name,
                        description = "Free kick awarded. Foul committed on ${shooter.name} by ${fouler.name}.",
                        homeScore = homeScore,
                        awayScore = awayScore
                    )
                }
            }

            // Corner chance (20%)
            roll in 15..34 -> {
                if (isHomeAction) stats.homeCorners++ else stats.awayCorners++
                MatchEvent(
                    minute = currentMinute,
                    type = MatchEventType.SHOT_SAVED,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    description = "Deflected out for a corner kick after a drive from ${shooter.name}.",
                    homeScore = homeScore,
                    awayScore = awayScore
                )
            }

            // Shot saved / woodwork (35%)
            roll in 35..69 -> {
                if (isHomeAction) {
                    stats.homeShots++
                    stats.homeShotsOnTarget++
                } else {
                    stats.awayShots++
                    stats.awayShotsOnTarget++
                }
                val gk = defendingTeam.getStartingXI().firstOrNull { it.position == PlayerPosition.GK } ?: defendingTeam.getStartingXI().first()
                MatchEvent(
                    minute = currentMinute,
                    type = MatchEventType.SHOT_SAVED,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    secondaryPlayerName = gk.name,
                    description = "Great save by ${gk.name} denying a powerful shot from ${shooter.name}!",
                    homeScore = homeScore,
                    awayScore = awayScore
                )
            }

            // Shot missed (15%)
            roll in 70..84 -> {
                if (isHomeAction) stats.homeShots++ else stats.awayShots++
                MatchEvent(
                    minute = currentMinute,
                    type = MatchEventType.SHOT_OFF_TARGET,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    description = "${shooter.name} attempts a curling shot but it flies narrowly wide of the post.",
                    homeScore = homeScore,
                    awayScore = awayScore
                )
            }

            // GOAL! (15% chance when chance is triggered)
            else -> {
                if (isHomeAction) {
                    homeScore++
                    stats.homeShots++
                    stats.homeShotsOnTarget++
                } else {
                    awayScore++
                    stats.awayShots++
                    stats.awayShotsOnTarget++
                }
                shooter.goalsScored++

                val assister = attackingTeam.getStartingXI().filter { it.id != shooter.id }.randomOrNull()
                assister?.assists = (assister?.assists ?: 0) + 1

                val desc = if (assister != null) {
                    "GOAL! ${shooter.name} scores with a clinical finish after an exquisite assist from ${assister.name}!"
                } else {
                    "GOAL! A magnificent solo strike from ${shooter.name} finds the top corner!"
                }

                MatchEvent(
                    minute = currentMinute,
                    type = MatchEventType.GOAL,
                    teamId = attackingTeam.id,
                    primaryPlayerName = shooter.name,
                    secondaryPlayerName = assister?.name,
                    description = desc,
                    homeScore = homeScore,
                    awayScore = awayScore
                )
            }
        }
    }

    fun makeSubstitution(isHome: Boolean, playerOutId: String, playerInId: String): Boolean {
        val team = if (isHome) homeTeam else awayTeam
        val pOut = team.players.find { it.id == playerOutId } ?: return false
        val pIn = team.players.find { it.id == playerInId } ?: return false

        pOut.isStarting = false
        pOut.isSubstitute = false
        pIn.isStarting = true
        pIn.isSubstitute = false

        initPitchPlayers()

        val subEvent = MatchEvent(
            minute = currentMinute,
            type = MatchEventType.SUBSTITUTION,
            teamId = team.id,
            primaryPlayerName = pIn.name,
            secondaryPlayerName = pOut.name,
            description = "Substitution (${team.shortName}): ${pIn.name} comes ON, replacing ${pOut.name}.",
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(subEvent)
        return true
    }

    fun simulateRestOfMatch() {
        while (!isFullTime) {
            stepMinute()
        }
    }
}
