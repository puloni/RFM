package com.example.rfm.engine

import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

enum class MatchPhase(val label: String, val icon: String) {
    BUILD_UP("Build-up from Defence", "🛡️"),
    MIDFIELD_DUEL("Midfield Battle & Passing", "⚔️"),
    WING_OVERLAP("Wing Overlap & Through Ball", "💨"),
    BOX_DELIVERY("Cross & Box Delivery", "🎯"),
    FINAL_EFFORT("Shot on Goal & Save", "💥")
}

enum class CommentaryHighlight(val colorHex: Long) {
    NORMAL(0xFFE0E0E0),     // White/Light Grey
    CHANCE(0xFFFFEB3B),     // Yellow
    CARD(0xFFFF5252),       // Red
    GOAL(0xFFFFD700),       // Gold
    SAVE(0xFF40C4FF)        // Cyan
}

data class CommentaryItem(
    val minute: Int,
    val text: String,
    val type: CommentaryHighlight
)

data class PitchPlayer(
    val id: String,
    val name: String,
    val number: Int,
    val position: PlayerPosition,
    var x: Float, // 0.0 (left goal) to 1.0 (right goal)
    var y: Float, // 0.0 (top touchline) to 1.0 (bottom touchline)
    val isHome: Boolean,
    var liveRating: Float = 6.5f,
    var condition: Int = 100,
    var goals: Int = 0,
    var assists: Int = 0,
    var yellowCards: Int = 0,
    var targetX: Float = x,
    var targetY: Float = y,
    var prevX: Float = x,
    var prevY: Float = y,
    var controlX: Float = x,
    var controlY: Float = y,
    var isDiving: Boolean = false,
    var diveTargetY: Float = y,
    var actionAnimation: String = "IDLE", // "IDLE", "KICK_PULSE", "COLLISION_SHAKE", "GK_DIVE"
    var actionAnimTimer: Float = 0f,
    var tacticalRole: PlayerTacticalRole = PlayerTacticalRole.defaultForPosition(position)
)

data class OffTheBallArrow(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val type: String, // "OVERLAP", "RUN_IN_BEHIND", "DEFENSIVE_SHIFT"
    val label: String = ""
)

data class CardNotice(
    val minute: Int,
    val playerName: String,
    val playerNumber: Int,
    val teamShortName: String,
    val isRed: Boolean,
    val teamColorHex: Long
)

data class GoalNotice(
    val minute: Int,
    val scorerName: String,
    val scorerNumber: Int,
    val assisterName: String?,
    val assisterNumber: Int?,
    val teamShortName: String,
    val teamColorHex: Long,
    val xg: Float
)

data class ReplayMoment(
    val minute: Int,
    val title: String, // "GOAL", "WOODWORK HIT"
    val primaryPlayer: String,
    val ballPath: List<Pair<Float, Float>>,
    val isGoal: Boolean = true
)

data class PitchState(
    var ballX: Float = 0.5f,
    var ballY: Float = 0.5f,
    var ballZ: Float = 0.0f, // 0.0 (ground) to 1.0 (high arc)
    var ballTargetX: Float = 0.5f,
    var ballTargetY: Float = 0.5f,
    var ballPrevX: Float = 0.5f,
    var ballPrevY: Float = 0.5f,
    var ballPrevZ: Float = 0.0f,
    var ballControlX: Float = 0.5f,
    var ballControlY: Float = 0.5f,
    var ballFlightProgress: Float = 1.0f,
    val homePlayers: List<PitchPlayer>,
    val awayPlayers: List<PitchPlayer>,
    var possessionHome: Boolean = true,
    var isShot: Boolean = false,
    var isGoal: Boolean = false,
    var lastScorer: String = "",
    var currentPhase: MatchPhase = MatchPhase.BUILD_UP,
    var ballCurve: Float = 0f,
    var isCross: Boolean = false,
    var gkDiveDirection: String = "NONE", // "LEFT", "RIGHT", "UP", "DOWN", "NONE"
    var defensivePushOffset: Float = 0f,
    var lastKeyActionText: String = "",
    // Tactical Indicators & TV Graphics
    var homeMomentum: Float = 0.5f, // 0.0 to 1.0 (0.5 is even)
    var pressingZoneLineX: Float = 0.5f,
    var offTheBallArrows: List<OffTheBallArrow> = emptyList(),
    var activeMidfieldTriangles: List<List<PitchPlayer>> = emptyList(),
    var activeGoalNotice: GoalNotice? = null,
    var activeCardNotice: CardNotice? = null,
    var injuryTimeMinutes: Int = 0,
    var isReplayActive: Boolean = false,
    var replayMoment: ReplayMoment? = null,
    val recentBallPath: MutableList<Pair<Float, Float>> = mutableListOf()
)

class MatchSimulationEngine(
    val fixture: Fixture,
    val homeTeam: Team,
    val awayTeam: Team,
    val modSettings: ModSettings = ModSettings()
) {
    var currentMinute: Int = 0
    var homeScore: Int = 0
    var awayScore: Int = 0
    var isHalfTime: Boolean = false
    var isFullTime: Boolean = false
    var isStarted: Boolean = false

    var homeSubsUsed: Int = 0
    var awaySubsUsed: Int = 0

    var activeTouchlineShout: TouchlineShout = TouchlineShout.NONE
    var halfTimeTeamTalkDone: Boolean = false
    var fullTimeTeamTalkDone: Boolean = false
    var lastTeamTalkFeedback: String = ""
    private var secondHalfUserAttackBonus: Int = 0

    var isHighlightOnlyMode: Boolean = false
    var isKeyHighlightActive: Boolean = false
    var assistantManagerAdvice: String = "Tactical shape looks solid, boss. Keep up the disciplined pressing."

    val stats = MatchStats()
    val events = mutableListOf<MatchEvent>()
    val detailedCommentary = mutableListOf<CommentaryItem>()
    var latestCommentary: String = "Welcome to the match! Both teams are taking the pitch."

    // Pitch visual representation
    lateinit var pitchState: PitchState

    init {
        initializePitch()
    }

    private fun initializePitch() {
        val homeP = mutableListOf<PitchPlayer>()
        val homeStarting = homeTeam.getStartingXI()
        homeStarting.forEachIndexed { index, p ->
            val (x, y) = getFormationCoordinates(index, homeTeam.formation, isHome = true)
            homeP.add(
                PitchPlayer(
                    id = p.id,
                    name = p.name,
                    number = index + 1,
                    position = p.position,
                    x = x,
                    y = y,
                    isHome = true,
                    liveRating = 6.5f,
                    condition = p.condition,
                    goals = p.goalsScored,
                    assists = p.assists,
                    yellowCards = p.yellowCards,
                    tacticalRole = p.tacticalRole
                )
            )
        }

        val awayP = mutableListOf<PitchPlayer>()
        val awayStarting = awayTeam.getStartingXI()
        awayStarting.forEachIndexed { index, p ->
            val (x, y) = getFormationCoordinates(index, awayTeam.formation, isHome = false)
            awayP.add(
                PitchPlayer(
                    id = p.id,
                    name = p.name,
                    number = index + 1,
                    position = p.position,
                    x = x,
                    y = y,
                    isHome = false,
                    liveRating = 6.5f,
                    condition = p.condition,
                    goals = p.goalsScored,
                    assists = p.assists,
                    yellowCards = p.yellowCards,
                    tacticalRole = p.tacticalRole
                )
            )
        }

        pitchState = PitchState(
            ballX = 0.5f,
            ballY = 0.5f,
            ballTargetX = 0.5f,
            ballTargetY = 0.5f,
            ballPrevX = 0.5f,
            ballPrevY = 0.5f,
            ballControlX = 0.5f,
            ballControlY = 0.5f,
            homePlayers = homeP,
            awayPlayers = awayP,
            possessionHome = true,
            homeMomentum = 0.5f,
            pressingZoneLineX = calculatePressingLineX(homeTeam.pressingStyle, isHome = true)
        )
    }

    private fun calculatePressingLineX(style: PressingStyle, isHome: Boolean): Float {
        return when (style) {
            PressingStyle.HIGH_PRESS -> if (isHome) 0.65f else 0.35f
            PressingStyle.OWN_HALF -> if (isHome) 0.36f else 0.64f
            else -> 0.50f
        }
    }

    /**
     * Accurate Horizontal Pitch Coordinates:
     * - Field width (X): 0.0 is Home goal (Left), 1.0 is Away goal (Right). Center circle is at X=0.5.
     * - Field height (Y): 0.0 is Top touchline, 1.0 is Bottom touchline. Center is at Y=0.5.
     * Home defends Left and attacks Right. Away defends Right and attacks Left.
     */
    private fun getFormationCoordinates(slotIndex: Int, formation: Formation, isHome: Boolean): Pair<Float, Float> {
        val homeCoords = when (formation) {
            Formation.F_433 -> when (slotIndex) {
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.20f, 0.16f) // LB
                2 -> Pair(0.18f, 0.38f) // CB
                3 -> Pair(0.18f, 0.62f) // CB
                4 -> Pair(0.20f, 0.84f) // RB
                5 -> Pair(0.31f, 0.28f) // LCM
                6 -> Pair(0.28f, 0.50f) // CDM
                7 -> Pair(0.31f, 0.72f) // RCM
                8 -> Pair(0.44f, 0.18f) // LW
                9 -> Pair(0.47f, 0.50f) // ST
                10 -> Pair(0.44f, 0.82f) // RW
                else -> Pair(0.35f, 0.50f)
            }
            Formation.F_4231 -> when (slotIndex) {
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.20f, 0.16f) // LB
                2 -> Pair(0.18f, 0.38f) // CB
                3 -> Pair(0.18f, 0.62f) // CB
                4 -> Pair(0.20f, 0.84f) // RB
                5 -> Pair(0.29f, 0.36f) // LDM
                6 -> Pair(0.29f, 0.64f) // RDM
                7 -> Pair(0.39f, 0.20f) // LAM
                8 -> Pair(0.38f, 0.50f) // CAM
                9 -> Pair(0.39f, 0.80f) // RAM
                10 -> Pair(0.47f, 0.50f) // ST
                else -> Pair(0.35f, 0.50f)
            }
            Formation.F_352 -> when (slotIndex) {
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.18f, 0.26f) // LCB
                2 -> Pair(0.17f, 0.50f) // CB
                3 -> Pair(0.18f, 0.74f) // RCB
                4 -> Pair(0.31f, 0.13f) // LWB
                5 -> Pair(0.30f, 0.36f) // CM
                6 -> Pair(0.35f, 0.50f) // CAM
                7 -> Pair(0.30f, 0.64f) // CM
                8 -> Pair(0.31f, 0.87f) // RWB
                9 -> Pair(0.46f, 0.38f) // ST
                10 -> Pair(0.46f, 0.62f) // ST
                else -> Pair(0.35f, 0.50f)
            }
            Formation.F_532 -> when (slotIndex) {
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.21f, 0.14f) // LWB
                2 -> Pair(0.18f, 0.32f) // LCB
                3 -> Pair(0.17f, 0.50f) // CB
                4 -> Pair(0.18f, 0.68f) // RCB
                5 -> Pair(0.21f, 0.86f) // RWB
                6 -> Pair(0.32f, 0.30f) // LCM
                7 -> Pair(0.31f, 0.50f) // CM
                8 -> Pair(0.32f, 0.70f) // RCM
                9 -> Pair(0.46f, 0.38f) // ST
                10 -> Pair(0.46f, 0.62f) // ST
                else -> Pair(0.35f, 0.50f)
            }
            Formation.F_4312 -> when (slotIndex) {
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.20f, 0.16f) // LB
                2 -> Pair(0.18f, 0.38f) // CB
                3 -> Pair(0.18f, 0.62f) // CB
                4 -> Pair(0.20f, 0.84f) // RB
                5 -> Pair(0.30f, 0.30f) // LCM
                6 -> Pair(0.28f, 0.50f) // CDM
                7 -> Pair(0.30f, 0.70f) // RCM
                8 -> Pair(0.38f, 0.50f) // CAM
                9 -> Pair(0.46f, 0.38f) // ST
                10 -> Pair(0.46f, 0.62f) // ST
                else -> Pair(0.35f, 0.50f)
            }
            else -> when (slotIndex) { // Standard 4-4-2
                0 -> Pair(0.07f, 0.50f) // GK
                1 -> Pair(0.20f, 0.16f) // LB
                2 -> Pair(0.18f, 0.38f) // CB
                3 -> Pair(0.18f, 0.62f) // CB
                4 -> Pair(0.20f, 0.84f) // RB
                5 -> Pair(0.34f, 0.16f) // LM
                6 -> Pair(0.32f, 0.38f) // CM
                7 -> Pair(0.32f, 0.62f) // CM
                8 -> Pair(0.34f, 0.84f) // RM
                9 -> Pair(0.46f, 0.38f) // ST
                10 -> Pair(0.46f, 0.62f) // ST
                else -> Pair(0.35f, 0.50f)
            }
        }

        return if (isHome) {
            homeCoords
        } else {
            Pair(1.0f - homeCoords.first, homeCoords.second)
        }
    }

    fun startMatch() {
        isStarted = true
        RfmAudioEngine.playWhistle(longWhistle = false)
        val event = MatchEvent(
            minute = 0,
            type = MatchEventType.KICKOFF,
            teamId = homeTeam.id,
            description = "Referee blows the whistle for kickoff! The match is underway.",
            homeScore = 0,
            awayScore = 0
        )
        events.add(event)
        latestCommentary = event.description
        detailedCommentary.add(CommentaryItem(0, event.description, CommentaryHighlight.NORMAL))
    }

    private fun addCommentary(text: String, type: CommentaryHighlight) {
        latestCommentary = text
        detailedCommentary.add(0, CommentaryItem(currentMinute, text, type))
        if (detailedCommentary.size > 60) {
            detailedCommentary.removeAt(detailedCommentary.lastIndex)
        }
    }

    private fun updateAssistantAdvice() {
        val userStarting = homeTeam.getStartingXI()
        val tiredPlayer = userStarting.find { it.condition < 60 }
        val cardedPlayer = userStarting.find { it.yellowCards > 0 }

        assistantManagerAdvice = when {
            currentMinute in 75..90 && homeScore > awayScore ->
                "Boss, we have the lead! Consider switching mentality to Cautious or Defensive to lock down the 3 points."
            currentMinute in 70..90 && homeScore < awayScore ->
                "Time is slipping away, boss! Throw men forward and shout 'Demand More' from the touchline!"
            tiredPlayer != null ->
                "Boss, ${tiredPlayer.name}'s stamina has dropped to ${tiredPlayer.condition}%. Consider subbing him before he pulls a muscle."
            cardedPlayer != null && cardedPlayer.condition < 72 ->
                "Boss, ${cardedPlayer.name} is on a yellow card and looking fatigued. A rash tackle could leave us with 10 men."
            stats.awayShots > stats.homeShots + 3 ->
                "Their forwards are finding too much space behind our defensive line, boss. Let's drop our block deeper."
            stats.homePossession > 65 ->
                "Dominating possession, boss! Let's up the tempo and turn this control into clean-cut chances."
            else ->
                "Good tactical shape, boss. Keep pressing high and maintaining positional discipline."
        }
    }

    /**
     * Advance simulation by 1 minute.
     */
    fun tickMinute(): MatchEvent? {
        if (!isStarted || isFullTime) return null

        currentMinute++

        // Reset transient notices after a couple minutes
        if (pitchState.activeGoalNotice != null && currentMinute > pitchState.activeGoalNotice!!.minute + 2) {
            pitchState.activeGoalNotice = null
        }
        if (pitchState.activeCardNotice != null && currentMinute > pitchState.activeCardNotice!!.minute + 2) {
            pitchState.activeCardNotice = null
        }
        if (pitchState.isReplayActive && currentMinute > (pitchState.replayMoment?.minute ?: 0) + 1) {
            pitchState.isReplayActive = false
        }

        // Half-time check
        if (currentMinute == 45 && !isHalfTime) {
            isHalfTime = true
            pitchState.injuryTimeMinutes = Random.nextInt(1, 4)
            RfmAudioEngine.playWhistle(longWhistle = true)
            val event = MatchEvent(
                minute = 45,
                type = MatchEventType.HALF_TIME,
                teamId = "",
                description = "Half-Time whistle! (+${pitchState.injuryTimeMinutes}') Score is ${homeTeam.shortName} $homeScore - $awayScore ${awayTeam.shortName}.",
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(event.description, CommentaryHighlight.NORMAL)
            updateAssistantAdvice()

            // AI Manager evaluates tactics at Half-Time
            triggerAiTacticalEvaluation()
            return event
        }

        // Full-time check
        if (currentMinute >= 90) {
            isFullTime = true
            pitchState.injuryTimeMinutes = Random.nextInt(2, 6)
            RfmAudioEngine.playWhistle(longWhistle = true)
            val event = MatchEvent(
                minute = 90,
                type = MatchEventType.FULL_TIME,
                teamId = "",
                description = "Full-Time! Final score: ${homeTeam.shortName} $homeScore - $awayScore ${awayTeam.shortName}.",
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(event.description, CommentaryHighlight.NORMAL)
            finalizeMatch()
            return event
        }

        // AI Manager checks for counter-tactics in the 70th-75th minute
        if (currentMinute in 70..75) {
            triggerAiTacticalEvaluation()
        }

        // 1. Fatigue degradation curves: accelerating after minute 65 under high press
        updateStaminaAndFatigue()

        // 2. Spatial Defensive Line & Pressing Zone positioning
        updatePressingAndDefensiveLines()

        // 3. Possession & Momentum computation
        val isHomeAttacking = updatePossessionAndMomentum()

        // 4. Update 30-zone positional grid movements, inverted winger runs, box-to-box surging
        updateSpatialZonePositions(isHomeAttacking)

        // 5. Periodic Assistant Advice Update
        if (currentMinute % 8 == 0) {
            updateAssistantAdvice()
        }

        // 6. Chance of key event this minute (~20% chance)
        val eventRoll = Random.nextInt(100)
        if (eventRoll < 20) {
            pitchState.currentPhase = MatchPhase.FINAL_EFFORT
            isKeyHighlightActive = true
            return simulateKeyAction(isHomeAttacking)
        } else {
            isKeyHighlightActive = false
        }

        // Background commentary
        if (currentMinute % 5 == 0) {
            val circDesc = if (isHomeAttacking) {
                "${homeTeam.name} patient in buildup, circulating the ball into half-space channels."
            } else {
                "${awayTeam.name} probing the defensive lines with structured positional play."
            }
            addCommentary(circDesc, CommentaryHighlight.NORMAL)
        }

        return null
    }

    private fun updateStaminaAndFatigue() {
        if (modSettings.noPlayerFatigue || modSettings.disableFatigueEffects) {
            homeTeam.players.forEach { it.condition = 100 }
            awayTeam.players.forEach { it.condition = 100 }
            return
        }

        val isLateMatch = currentMinute >= 65
        val homePressMultiplier = if (homeTeam.pressingStyle == PressingStyle.HIGH_PRESS) 1.8f else 1.0f
        val awayPressMultiplier = if (awayTeam.pressingStyle == PressingStyle.HIGH_PRESS) 1.8f else 1.0f
        val shoutDrain = if (homeTeam.id == "man_utd") activeTouchlineShout.staminaDrain else 1.0f

        // Progressively higher drain after 65th min
        val latePenalty = if (isLateMatch) 1.6f else 1.0f

        if (currentMinute % 6 == 0) {
            homeTeam.getStartingXI().forEach { p ->
                if (p.condition > 35) {
                    val roleMultiplier = p.tacticalRole.staminaDrainMultiplier
                    val drain = (Random.nextInt(1, 3) * homePressMultiplier * shoutDrain * latePenalty * roleMultiplier).toInt().coerceAtLeast(1)
                    p.condition = (p.condition - drain).coerceAtLeast(35)
                }
            }

            awayTeam.getStartingXI().forEach { p ->
                if (p.condition > 35) {
                    val roleMultiplier = p.tacticalRole.staminaDrainMultiplier
                    val drain = (Random.nextInt(1, 3) * awayPressMultiplier * latePenalty * roleMultiplier).toInt().coerceAtLeast(1)
                    p.condition = (p.condition - drain).coerceAtLeast(35)
                }
            }
        }
    }

    private fun updatePressingAndDefensiveLines() {
        pitchState.defensivePushOffset = when (homeTeam.pressingStyle) {
            PressingStyle.HIGH_PRESS -> 0.08f
            PressingStyle.OWN_HALF -> -0.06f
            else -> 0.0f
        }

        pitchState.pressingZoneLineX = when (homeTeam.pressingStyle) {
            PressingStyle.HIGH_PRESS -> 0.66f
            PressingStyle.OWN_HALF -> 0.38f
            else -> 0.50f
        }
    }

    private fun updatePossessionAndMomentum(): Boolean {
        val homeMid = if (modSettings.maxPlayerStats && homeTeam.id == "man_utd") 99 else homeTeam.calculateMidfieldRating()
        val awayMid = awayTeam.calculateMidfieldRating()

        // Momentum influences possession contest
        val momentumDelta = (pitchState.homeMomentum - 0.5f) * 20f
        val shoutBonus = if (homeTeam.id == "man_utd") activeTouchlineShout.possessionBonus else -activeTouchlineShout.possessionBonus
        val homePossessionChance = (50 + ((homeMid - awayMid) / 3) + shoutBonus + momentumDelta).toInt().coerceIn(18, 82)

        val isHomeAttacking = Random.nextInt(100) < homePossessionChance
        pitchState.possessionHome = isHomeAttacking

        // Drift momentum slightly toward attacking team
        val momentumAdjustment = if (isHomeAttacking) 0.025f else -0.025f
        pitchState.homeMomentum = (pitchState.homeMomentum + momentumAdjustment).coerceIn(0.10f, 0.90f)

        // Accumulate stats possession
        if (isHomeAttacking) {
            stats.homePossession = ((stats.homePossession * currentMinute + 60) / (currentMinute + 1)).coerceIn(20, 80)
            stats.awayPossession = 100 - stats.homePossession
        } else {
            stats.homePossession = ((stats.homePossession * currentMinute + 40) / (currentMinute + 1)).coerceIn(20, 80)
            stats.awayPossession = 100 - stats.homePossession
        }

        return isHomeAttacking
    }

    private fun updateSpatialZonePositions(isHomeAttacking: Boolean) {
        val phaseCycle = (currentMinute % 4)
        pitchState.currentPhase = when (phaseCycle) {
            0 -> MatchPhase.BUILD_UP
            1 -> MatchPhase.MIDFIELD_DUEL
            2 -> MatchPhase.WING_OVERLAP
            else -> MatchPhase.BOX_DELIVERY
        }

        val arrows = mutableListOf<OffTheBallArrow>()

        // Store previous ball coordinates
        pitchState.ballPrevX = pitchState.ballX
        pitchState.ballPrevY = pitchState.ballY
        pitchState.ballPrevZ = pitchState.ballZ

        val activeTeam = if (isHomeAttacking) homeTeam else awayTeam
        val activePlayers = if (isHomeAttacking) pitchState.homePlayers else pitchState.awayPlayers

        // Record recent path for ball trail lines
        pitchState.recentBallPath.add(Pair(pitchState.ballX, pitchState.ballY))
        if (pitchState.recentBallPath.size > 8) {
            pitchState.recentBallPath.removeAt(0)
        }

        // Positional Play: Inverted Winger & Box-to-Box Midfielder movement
        activePlayers.forEach { p ->
            p.prevX = p.x
            p.prevY = p.y

            when (p.tacticalRole) {
                PlayerTacticalRole.INVERTED_WINGER, PlayerTacticalRole.INSIDE_FORWARD -> {
                    // Inverted Winger moves inside toward central and half-space zones
                    val targetChannelY = if (p.y < 0.5f) 0.32f else 0.68f // cut inside from flanks
                    val targetBandX = if (isHomeAttacking) (0.65f + Random.nextFloat() * 0.12f) else (0.35f - Random.nextFloat() * 0.12f)
                    p.targetX = targetBandX
                    p.targetY = targetChannelY
                    p.controlX = (p.prevX + p.targetX) / 2f
                    p.controlY = targetChannelY
                    arrows.add(OffTheBallArrow(p.prevX, p.prevY, p.targetX, p.targetY, "CUT_INSIDE", "INVERT"))
                }
                PlayerTacticalRole.BOX_TO_BOX -> {
                    // Box-to-Box midfielder covers deep midfield and makes forward runs into the box
                    val targetBandX = if (isHomeAttacking) 0.82f else 0.18f
                    val targetChannelY = 0.45f + Random.nextFloat() * 0.10f
                    p.targetX = targetBandX
                    p.targetY = targetChannelY
                    p.controlX = (p.prevX + p.targetX) / 2f
                    p.controlY = p.targetY
                    arrows.add(OffTheBallArrow(p.prevX, p.prevY, p.targetX, p.targetY, "RUN_IN_BEHIND", "BOX RUN"))
                }
                PlayerTacticalRole.ATTACKING_FULLBACK -> {
                    // Overlaps down wide flank
                    val targetBandX = if (isHomeAttacking) 0.68f else 0.32f
                    val targetChannelY = if (p.y < 0.5f) 0.12f else 0.88f
                    p.targetX = targetBandX
                    p.targetY = targetChannelY
                    arrows.add(OffTheBallArrow(p.prevX, p.prevY, p.targetX, p.targetY, "OVERLAP", "OVERLAP"))
                }
                PlayerTacticalRole.POACHER -> {
                    // Striker makes run on the shoulder behind defense
                    val targetBandX = if (isHomeAttacking) 0.88f else 0.12f
                    val targetChannelY = 0.50f + (Random.nextFloat() - 0.5f) * 0.20f
                    p.targetX = targetBandX
                    p.targetY = targetChannelY
                    arrows.add(OffTheBallArrow(p.prevX, p.prevY, p.targetX, p.targetY, "RUN_IN_BEHIND", "ST RUN"))
                }
                else -> {
                    p.targetX = p.x + (Random.nextFloat() - 0.5f) * 0.04f
                    p.targetY = p.y + (Random.nextFloat() - 0.5f) * 0.04f
                }
            }
        }

        // Set Midfield Passing Triangles between 3 central midfielders
        val mids = activePlayers.filter { it.position.roleCategory == "Midfielder" }
        if (mids.size >= 3) {
            pitchState.activeMidfieldTriangles = listOf(mids.take(3))
        }

        // Ball target and trajectory setting
        pitchState.isCross = false
        pitchState.ballZ = 0.0f

        if (isHomeAttacking) {
            when (pitchState.currentPhase) {
                MatchPhase.BUILD_UP -> {
                    pitchState.ballTargetX = (0.24f + Random.nextFloat() * 0.16f)
                    pitchState.ballTargetY = (0.25f + Random.nextFloat() * 0.50f)
                    pitchState.ballZ = 0.0f
                }
                MatchPhase.MIDFIELD_DUEL -> {
                    pitchState.ballTargetX = (0.44f + Random.nextFloat() * 0.18f)
                    pitchState.ballTargetY = (0.22f + Random.nextFloat() * 0.56f)
                    pitchState.ballZ = 0.0f
                }
                MatchPhase.WING_OVERLAP -> {
                    pitchState.ballTargetX = (0.68f + Random.nextFloat() * 0.14f)
                    pitchState.ballTargetY = if (Random.nextBoolean()) 0.14f else 0.86f
                    pitchState.isCross = true
                    pitchState.ballZ = 0.75f // Aerial rise for cross
                    pitchState.ballCurve = if (pitchState.ballTargetY < 0.5f) 0.35f else -0.35f
                }
                else -> {
                    pitchState.ballTargetX = (0.80f + Random.nextFloat() * 0.12f)
                    pitchState.ballTargetY = (0.36f + Random.nextFloat() * 0.28f)
                    pitchState.ballZ = 0.35f
                }
            }
        } else {
            when (pitchState.currentPhase) {
                MatchPhase.BUILD_UP -> {
                    pitchState.ballTargetX = (0.76f - Random.nextFloat() * 0.16f)
                    pitchState.ballTargetY = (0.25f + Random.nextFloat() * 0.50f)
                    pitchState.ballZ = 0.0f
                }
                MatchPhase.MIDFIELD_DUEL -> {
                    pitchState.ballTargetX = (0.56f - Random.nextFloat() * 0.18f)
                    pitchState.ballTargetY = (0.22f + Random.nextFloat() * 0.56f)
                    pitchState.ballZ = 0.0f
                }
                MatchPhase.WING_OVERLAP -> {
                    pitchState.ballTargetX = (0.32f - Random.nextFloat() * 0.14f)
                    pitchState.ballTargetY = if (Random.nextBoolean()) 0.14f else 0.86f
                    pitchState.isCross = true
                    pitchState.ballZ = 0.75f // Aerial rise for cross
                    pitchState.ballCurve = if (pitchState.ballTargetY < 0.5f) 0.35f else -0.35f
                }
                else -> {
                    pitchState.ballTargetX = (0.20f - Random.nextFloat() * 0.12f)
                    pitchState.ballTargetY = (0.36f + Random.nextFloat() * 0.28f)
                    pitchState.ballZ = 0.35f
                }
            }
        }

        // Set control points for smooth trajectory
        pitchState.ballControlX = (pitchState.ballPrevX + pitchState.ballTargetX) / 2f
        pitchState.ballControlY = (pitchState.ballPrevY + pitchState.ballTargetY) / 2f + pitchState.ballCurve * 0.1f
        pitchState.ballX = pitchState.ballTargetX
        pitchState.ballY = pitchState.ballTargetY

        pitchState.offTheBallArrows = arrows
    }

    private fun triggerAiTacticalEvaluation() {
        val aiTeam = awayTeam
        val userTeam = homeTeam

        val shift = AiAdaptiveManager.evaluateAndAdapt(
            currentMinute = currentMinute,
            aiTeam = aiTeam,
            userTeam = userTeam,
            aiScore = awayScore,
            userScore = homeScore,
            userPossession = stats.homePossession,
            aiSubsUsed = awaySubsUsed
        )

        if (shift != null) {
            if (shift.subMade) {
                awaySubsUsed++
            }
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.TACTICAL_CHANGE,
                teamId = aiTeam.id,
                description = shift.announcement,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(shift.announcement, CommentaryHighlight.NORMAL)
            initializePitch()
        }
    }

    private fun simulateKeyAction(isHomeAttacking: Boolean): MatchEvent {
        val attackingTeam = if (isHomeAttacking) homeTeam else awayTeam
        val defendingTeam = if (isHomeAttacking) awayTeam else homeTeam
        val isUserTeamAttacking = attackingTeam.id == "man_utd"
        val isUserTeamDefending = defendingTeam.id == "man_utd"

        var attackRating = if (modSettings.maxPlayerStats && isUserTeamAttacking) 99 else attackingTeam.calculateAttackRating()
        var defenseRating = if (modSettings.maxPlayerStats && isUserTeamDefending) 99 else defendingTeam.calculateDefenseRating()

        if (isUserTeamAttacking) {
            attackRating += activeTouchlineShout.attackBonus + secondHalfUserAttackBonus
        }
        if (isUserTeamDefending) {
            defenseRating += activeTouchlineShout.defenseBonus
        }

        val actionRoll = Random.nextInt(100)
        return when {
            actionRoll < 55 -> {
                simulateGoalOrShot(attackingTeam, defendingTeam, attackRating, defenseRating, isHomeAttacking)
            }
            actionRoll < 80 -> {
                simulateFoulOrCard(attackingTeam, defendingTeam, isHomeAttacking)
            }
            actionRoll < 95 -> {
                simulateCorner(attackingTeam, isHomeAttacking)
            }
            else -> {
                if (!modSettings.noPlayerInjury && Random.nextInt(100) < 30) {
                    simulateInjury(defendingTeam)
                } else {
                    simulateCorner(attackingTeam, isHomeAttacking)
                }
            }
        }
    }

    private fun simulateGoalOrShot(
        attackingTeam: Team,
        defendingTeam: Team,
        attackRating: Int,
        defenseRating: Int,
        isHome: Boolean
    ): MatchEvent {
        if (isHome) stats.homeShots++ else stats.awayShots++
        RfmAudioEngine.playBallKick()

        val startingAttackers = attackingTeam.getStartingXI().filter {
            it.position.roleCategory == "Forward" || it.position.roleCategory == "Midfielder"
        }.ifEmpty { attackingTeam.getStartingXI() }
        val shooter = startingAttackers.random()

        // Micro-animation: kick-recoil pulse on shooter
        val shooterPitchPlayer = (pitchState.homePlayers + pitchState.awayPlayers).find { it.id == shooter.id }
        shooterPitchPlayer?.actionAnimation = "KICK_PULSE"
        shooterPitchPlayer?.actionAnimTimer = 1.0f

        // Shot Coordinates on pitch
        val pitchX = if (isHome) (0.80f + Random.nextFloat() * 0.15f) else (0.05f + Random.nextFloat() * 0.15f)
        val pitchY = (0.35f + Random.nextFloat() * 0.30f)

        // Classify authentic Shot Type
        val finishRoll = Random.nextInt(100)
        val isPenaltyOpportunity = finishRoll < 7
        val isFreeKickOpportunity = finishRoll in 7..14
        val isHeader = finishRoll in 15..28
        val isVolley = finishRoll in 29..38
        val isOneOnOne = finishRoll in 39..50
        val isTapIn = finishRoll in 51..58

        val shotType = when {
            isPenaltyOpportunity || isOneOnOne -> ShotType.ONE_ON_ONE
            isFreeKickOpportunity -> ShotType.DIRECT_FREE_KICK
            isHeader -> ShotType.HEADER
            isVolley -> ShotType.VOLLEY
            isTapIn -> ShotType.TAP_IN
            else -> ShotType.OPEN_PLAY
        }

        val actualShooter = when {
            isPenaltyOpportunity -> attackingTeam.getStartingXI().firstOrNull { it.id == attackingTeam.penaltyTakerId } ?: shooter
            isFreeKickOpportunity -> attackingTeam.getStartingXI().firstOrNull { it.id == attackingTeam.freeKickTakerId } ?: shooter
            else -> shooter
        }

        // Calculate defenders in cone and pressure
        val defendersInCone = if (isPenaltyOpportunity || isOneOnOne) 0 else Random.nextInt(1, 4)
        val defensivePressure = if (isOneOnOne || isPenaltyOpportunity) 0.1f else (0.3f + Random.nextFloat() * 0.5f)
        val isWeakFoot = finishRoll % 3 == 0

        // True Expected Goals (xG) calculation via ShotQualityModel
        val xgResult = ShotQualityModel.calculateXg(
            shotX = pitchX,
            shotY = pitchY,
            isAttackingRight = isHome,
            shotType = shotType,
            defendersInCone = defendersInCone,
            defensivePressure = defensivePressure,
            isWeakFoot = isWeakFoot,
            shooter = actualShooter
        )

        val roundedXg = xgResult.xg
        if (isHome) stats.homeXg += roundedXg else stats.awayXg += roundedXg

        // Goal Probability weighted by true xG and attribute matchups
        val baseGoalChance = (roundedXg * 100).toInt()
        val ratingAdvantage = ((attackRating - defenseRating) / 3).coerceIn(-15, 15)
        val goalThreshold = (baseGoalChance + ratingAdvantage).coerceIn(12, 75)

        val isGoal = finishRoll < goalThreshold
        val isWoodwork = !isGoal && (finishRoll in goalThreshold until goalThreshold + 12)
        val isSaved = !isGoal && !isWoodwork && (finishRoll in (goalThreshold + 12) until (goalThreshold + 55))

        // Record Shot in Shot Map
        val outcome = when {
            isGoal -> ShotOutcome.GOAL
            isWoodwork -> ShotOutcome.WOODWORK
            isSaved -> ShotOutcome.SAVED
            else -> ShotOutcome.OFF_TARGET
        }
        fixture.shotMap.add(ShotDetail(currentMinute, isHome, actualShooter.name, roundedXg, outcome, pitchX, pitchY))

        // Trajectory for Instant Replay
        val shotPath = listOf(
            Pair(pitchX, pitchY),
            Pair((pitchX + if (isHome) 0.98f else 0.02f) / 2f, pitchY),
            Pair(if (isHome) 0.97f else 0.03f, 0.50f)
        )

        return if (isGoal) {
            // GOAL SCORED!
            if (isHome) {
                homeScore++
                stats.homeShotsOnTarget++
                pitchState.ballX = 0.96f
                pitchState.ballY = 0.48f + Random.nextFloat() * 0.04f
                pitchState.homeMomentum = (pitchState.homeMomentum + 0.22f).coerceAtMost(0.95f)
            } else {
                awayScore++
                stats.awayShotsOnTarget++
                pitchState.ballX = 0.04f
                pitchState.ballY = 0.48f + Random.nextFloat() * 0.04f
                pitchState.homeMomentum = (pitchState.homeMomentum - 0.22f).coerceAtLeast(0.05f)
            }
            pitchState.isGoal = true
            pitchState.isShot = true
            pitchState.ballZ = 0.4f
            pitchState.lastScorer = actualShooter.name
            actualShooter.goalsScored++

            // Live rating boost
            (pitchState.homePlayers + pitchState.awayPlayers).find { it.id == actualShooter.id }?.let {
                it.liveRating = (it.liveRating + 1.2f).coerceAtMost(9.9f)
                it.goals++
            }

            RfmAudioEngine.playGoalCheer()

            val assistCandidates = attackingTeam.getStartingXI().filter { it.id != actualShooter.id }
            val assister = if (!isPenaltyOpportunity && !isFreeKickOpportunity && assistCandidates.isNotEmpty() && Random.nextBoolean()) {
                assistCandidates.random().also {
                    it.assists++
                    (pitchState.homePlayers + pitchState.awayPlayers).find { ap -> ap.id == it.id }?.let { ap ->
                        ap.liveRating = (ap.liveRating + 0.6f).coerceAtMost(9.9f)
                        ap.assists++
                    }
                }
            } else null

            // TV Presentation Goal Notification Banner
            pitchState.activeGoalNotice = GoalNotice(
                minute = currentMinute,
                scorerName = actualShooter.name,
                scorerNumber = actualShooter.number,
                assisterName = assister?.name,
                assisterNumber = assister?.number,
                teamShortName = attackingTeam.shortName,
                teamColorHex = attackingTeam.primaryColorHex,
                xg = roundedXg
            )

            // Trigger Instant Replay
            pitchState.isReplayActive = true
            pitchState.replayMoment = ReplayMoment(
                minute = currentMinute,
                title = "GOAL!",
                primaryPlayer = actualShooter.name,
                ballPath = shotPath,
                isGoal = true
            )

            val desc = when {
                isPenaltyOpportunity -> "PENALTY GOAL! ${actualShooter.name} buries the spot kick into the bottom corner! (xG ${String.format("%.2f", roundedXg)})"
                isFreeKickOpportunity -> "DIRECT FREE KICK GOAL! Magnificent curling free kick from ${actualShooter.name}! (xG ${String.format("%.2f", roundedXg)})"
                assister != null -> "GOAL! ${actualShooter.name} finishes clinically after brilliant vision from ${assister.name}! (xG ${String.format("%.2f", roundedXg)})"
                else -> "GOAL! Stunning strike by ${actualShooter.name} finds the back of the net! (xG ${String.format("%.2f", roundedXg)})"
            }

            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.GOAL,
                teamId = attackingTeam.id,
                primaryPlayerName = actualShooter.name,
                secondaryPlayerName = assister?.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.GOAL)
            updateAssistantAdvice()
            event
        } else if (isWoodwork) {
            // WOODWORK HIT!
            pitchState.isGoal = false
            pitchState.isShot = true
            pitchState.ballX = if (isHome) 0.98f else 0.02f
            pitchState.ballY = 0.50f
            pitchState.ballZ = 0.90f // Hit high bar

            // Trigger Instant Replay for dramatic woodwork hit!
            pitchState.isReplayActive = true
            pitchState.replayMoment = ReplayMoment(
                minute = currentMinute,
                title = "OFF THE WOODWORK!",
                primaryPlayer = actualShooter.name,
                ballPath = shotPath,
                isGoal = false
            )

            val desc = "OFF THE WOODWORK! ${actualShooter.name}'s thunderous effort rattles off the crossbar! (xG ${String.format("%.2f", roundedXg)})"
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.SHOT_WOODWORK,
                teamId = attackingTeam.id,
                primaryPlayerName = actualShooter.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.CHANCE)
            event
        } else if (isSaved) {
            // GOALKEEPER SAVE
            val gk = defendingTeam.getStartingXI().firstOrNull { it.position == PlayerPosition.GK }
                ?: defendingTeam.getStartingXI().first()

            val gkPitchPlayer = (pitchState.homePlayers + pitchState.awayPlayers).find { it.id == gk.id }
            gkPitchPlayer?.actionAnimation = "GK_DIVE"
            gkPitchPlayer?.actionAnimTimer = 1.0f
            gkPitchPlayer?.diveTargetY = if (Random.nextBoolean()) 0.40f else 0.60f

            pitchState.gkDiveDirection = if (Random.nextBoolean()) "UP" else "DOWN"
            if (isHome) {
                stats.homeShotsOnTarget++
                pitchState.ballX = 0.92f
                pitchState.ballY = 0.50f
            } else {
                stats.awayShotsOnTarget++
                pitchState.ballX = 0.08f
                pitchState.ballY = 0.50f
            }
            pitchState.isGoal = false
            pitchState.isShot = true

            gkPitchPlayer?.liveRating = (gkPitchPlayer?.liveRating?.plus(0.5f) ?: 6.5f).coerceAtMost(9.9f)

            val desc = "Sensational save by ${gk.name}! Denies ${actualShooter.name}'s goalbound effort! (xG ${String.format("%.2f", roundedXg)})"
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.SHOT_SAVED,
                teamId = attackingTeam.id,
                primaryPlayerName = actualShooter.name,
                secondaryPlayerName = gk.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.SAVE)
            event
        } else {
            // OFF TARGET
            pitchState.isGoal = false
            pitchState.isShot = true
            pitchState.ballX = if (isHome) 0.98f else 0.02f
            pitchState.ballY = if (Random.nextBoolean()) 0.22f else 0.78f

            val desc = "${actualShooter.name} unleashes a shot, but it sails wide of the post. (xG ${String.format("%.2f", roundedXg)})"
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.SHOT_OFF_TARGET,
                teamId = attackingTeam.id,
                primaryPlayerName = actualShooter.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.CHANCE)
            event
        }
    }

    private fun simulateFoulOrCard(
        attackingTeam: Team,
        defendingTeam: Team,
        isHomeAttacking: Boolean
    ): MatchEvent {
        if (isHomeAttacking) stats.awayFouls++ else stats.homeFouls++
        val fouler = defendingTeam.getStartingXI().random()

        // Micro-animation: collision & jostle shake on defender
        val foulerPitchPlayer = (pitchState.homePlayers + pitchState.awayPlayers).find { it.id == fouler.id }
        foulerPitchPlayer?.actionAnimation = "COLLISION_SHAKE"
        foulerPitchPlayer?.actionAnimTimer = 1.0f

        val isUserDefending = defendingTeam.id == "man_utd"
        val foulCardMultiplier = if (isUserDefending) activeTouchlineShout.foulMultiplier else 1.0f
        val cardRoll = Random.nextInt(100)

        val isRedCard = cardRoll < (5 * foulCardMultiplier).toInt()
        val isYellowCard = !isRedCard && cardRoll < (28 * foulCardMultiplier).toInt()

        return if (isRedCard) {
            // RED CARD
            if (isHomeAttacking) stats.awayRedCards++ else stats.homeRedCards++
            fouler.isRedCard = true
            fouler.suspensionMatchesRemaining = 1

            // TV Presentation Card Notice
            pitchState.activeCardNotice = CardNotice(
                minute = currentMinute,
                playerName = fouler.name,
                playerNumber = fouler.number,
                teamShortName = defendingTeam.shortName,
                isRed = true,
                teamColorHex = defendingTeam.primaryColorHex
            )

            // Huge momentum shift
            if (isHomeAttacking) {
                pitchState.homeMomentum = (pitchState.homeMomentum + 0.30f).coerceAtMost(0.95f)
            } else {
                pitchState.homeMomentum = (pitchState.homeMomentum - 0.30f).coerceAtLeast(0.05f)
            }

            RfmAudioEngine.playWhistle(longWhistle = true)
            val desc = "STRAIGHT RED CARD! ${fouler.name} is sent off for a dangerous high challenge!"
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.RED_CARD,
                teamId = defendingTeam.id,
                primaryPlayerName = fouler.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.CARD)
            updateAssistantAdvice()
            event
        } else if (isYellowCard) {
            // YELLOW CARD
            if (isHomeAttacking) stats.awayYellowCards++ else stats.homeYellowCards++
            fouler.yellowCards++

            // TV Presentation Card Notice
            pitchState.activeCardNotice = CardNotice(
                minute = currentMinute,
                playerName = fouler.name,
                playerNumber = fouler.number,
                teamShortName = defendingTeam.shortName,
                isRed = false,
                teamColorHex = defendingTeam.primaryColorHex
            )

            RfmAudioEngine.playWhistle(longWhistle = false)
            val desc = "Yellow card shown to ${fouler.name} for a cynical tactical foul."
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.YELLOW_CARD,
                teamId = defendingTeam.id,
                primaryPlayerName = fouler.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.CARD)
            updateAssistantAdvice()
            event
        } else {
            // NORMAL FOUL
            val desc = "Foul called! ${fouler.name} halts the counter-attack with a clumsy tackle."
            val event = MatchEvent(
                minute = currentMinute,
                type = MatchEventType.FOUL,
                teamId = defendingTeam.id,
                primaryPlayerName = fouler.name,
                description = desc,
                homeScore = homeScore,
                awayScore = awayScore
            )
            events.add(event)
            addCommentary(desc, CommentaryHighlight.NORMAL)
            event
        }
    }

    private fun simulateCorner(attackingTeam: Team, isHome: Boolean): MatchEvent {
        if (isHome) stats.homeCorners++ else stats.awayCorners++
        val taker = attackingTeam.getStartingXI().firstOrNull { it.id == attackingTeam.cornerTakerId }
            ?: attackingTeam.getStartingXI().random()

        // Ball rises into the air for corner delivery
        pitchState.ballZ = 0.85f
        pitchState.isCross = true

        val desc = "Corner kick awarded to ${attackingTeam.name}. ${taker.name} steps up to deliver into the box."
        val event = MatchEvent(
            minute = currentMinute,
            type = MatchEventType.CORNER,
            teamId = attackingTeam.id,
            primaryPlayerName = taker.name,
            description = desc,
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
        addCommentary(desc, CommentaryHighlight.NORMAL)
        return event
    }

    private fun simulateInjury(team: Team): MatchEvent {
        val player = team.getStartingXI().random()
        player.injuryWeeks = Random.nextInt(1, 4)
        val desc = "Injury concern! ${player.name} is down on the turf clutching his hamstring."
        val event = MatchEvent(
            minute = currentMinute,
            type = MatchEventType.INJURY,
            teamId = team.id,
            primaryPlayerName = player.name,
            description = desc,
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
        addCommentary(desc, CommentaryHighlight.NORMAL)
        updateAssistantAdvice()
        return event
    }

    fun makeSubstitution(team: Team, playerOutId: String, playerInId: String): Boolean {
        val playerOut = team.players.find { it.id == playerOutId && it.isStarting } ?: return false
        val playerIn = team.players.find { it.id == playerInId && it.isSubstitute } ?: return false

        playerOut.isStarting = false
        playerOut.isSubstitute = false
        playerIn.isStarting = true
        playerIn.isSubstitute = false

        if (team.id == homeTeam.id) homeSubsUsed++ else awaySubsUsed++

        val desc = "Substitution for ${team.name}: ${playerIn.name} comes on to replace ${playerOut.name}."
        val event = MatchEvent(
            minute = currentMinute,
            type = MatchEventType.SUBSTITUTION,
            teamId = team.id,
            primaryPlayerName = playerIn.name,
            secondaryPlayerName = playerOut.name,
            description = desc,
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
        latestCommentary = desc
        initializePitch()
        return true
    }

    fun applyTouchlineShout(shout: TouchlineShout) {
        activeTouchlineShout = shout
        val event = MatchEvent(
            minute = currentMinute,
            type = MatchEventType.TACTICAL_CHANGE,
            teamId = homeTeam.id,
            description = "Touchline Shout: Manager yells '${shout.label}'! (${shout.description})",
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
        latestCommentary = event.description
        RfmAudioEngine.playWhistle(longWhistle = false)
    }

    fun applyHalfTimeTeamTalk(talk: TeamTalkOption) {
        halfTimeTeamTalkDone = true
        secondHalfUserAttackBonus = talk.secondHalfAttackBonus
        lastTeamTalkFeedback = "${talk.label} (${talk.tone}): ${talk.description}"
        homeTeam.getStartingXI().forEach {
            it.morale = (it.morale + talk.moraleDelta).coerceIn(10, 100)
            if (it.condition < 90) it.condition += 5
        }
        isHalfTime = false
        latestCommentary = "Second half underway! Manager's team talk: \"${talk.label}\" - ${talk.description}"
        val event = MatchEvent(
            minute = 45,
            type = MatchEventType.TACTICAL_CHANGE,
            teamId = homeTeam.id,
            description = "Half-Time Talk [${talk.tone}]: ${talk.description}",
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
    }

    fun applyFullTimeTeamTalk(talk: TeamTalkOption) {
        fullTimeTeamTalkDone = true
        lastTeamTalkFeedback = "${talk.label} (${talk.tone}): ${talk.description}"
        homeTeam.getStartingXI().forEach {
            it.morale = (it.morale + talk.moraleDelta).coerceIn(10, 100)
        }
        val event = MatchEvent(
            minute = 90,
            type = MatchEventType.TACTICAL_CHANGE,
            teamId = homeTeam.id,
            description = "Full-Time Talk [${talk.tone}]: ${talk.description}",
            homeScore = homeScore,
            awayScore = awayScore
        )
        events.add(event)
        latestCommentary = "Full-Time debrief: ${talk.label}. Squad responds to feedback."
    }

    fun applyHalfTimeTalk(boostMorale: Boolean) {
        if (!isHalfTime) return
        homeTeam.getStartingXI().forEach {
            it.morale = (it.morale + (if (boostMorale) 8 else 4)).coerceAtMost(100)
            if (it.condition < 90) it.condition += 5
        }
        isHalfTime = false
        latestCommentary = "Second half begins! Team looks revitalized after the manager's talk."
    }

    fun simulateToEnd() {
        while (!isFullTime) {
            if (isHalfTime) {
                applyHalfTimeTalk(boostMorale = true)
            }
            tickMinute()
        }
    }

    private fun finalizeMatch() {
        fixture.homeScore = homeScore
        fixture.awayScore = awayScore
        fixture.isPlayed = true
        fixture.stats = stats
        fixture.events.clear()
        fixture.events.addAll(events)

        var topRating = 0.0f
        var motm: Player? = null

        (homeTeam.getStartingXI() + awayTeam.getStartingXI()).forEach { p ->
            p.matchesPlayed++
            var base = 6.0f + (p.goalsScored * 1.5f) + (p.assists * 0.8f) + (p.form * 0.1f)
            if (p.isRedCard) base -= 2.0f
            if (p.yellowCards > 0) base -= 0.4f
            val rounded = round(base.coerceIn(4.2f, 9.8f) * 10) / 10f
            p.matchRating = rounded
            fixture.playerRatings[p.id] = rounded

            if (rounded > topRating) {
                topRating = rounded
                motm = p
            }
        }

        fixture.manOfTheMatchId = motm?.id
        fixture.manOfTheMatchName = motm?.name
    }
}
