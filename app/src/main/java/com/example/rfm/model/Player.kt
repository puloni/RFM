package com.example.rfm.model

enum class PlayerPosition(val label: String, val roleCategory: String) {
    GK("GK", "Goalkeeper"),
    LB("LB", "Defender"),
    CB("CB", "Defender"),
    RB("RB", "Defender"),
    CDM("CDM", "Midfielder"),
    CM("CM", "Midfielder"),
    CAM("CAM", "Midfielder"),
    LM("LM", "Midfielder"),
    RM("RM", "Midfielder"),
    LW("LW", "Forward"),
    RW("RW", "Forward"),
    ST("ST", "Forward")
}

enum class TrainingFocus(val label: String, val description: String, val icon: String) {
    BALANCED("Balanced", "Overall technical & physical development", "⚖️"),
    FINISHING("Finishing", "Focus on shooting, volleys, & composure in front of goal", "⚽"),
    PLAYMAKING("Playmaking", "Focus on vision, passing range, & set-piece delivery", "🎯"),
    PACE("Pace & Agility", "Focus on sprint speed, acceleration, & sharpness", "⚡"),
    DEFENDING("Defending", "Focus on standing tackles, interceptions, & marking", "🛡️"),
    PHYSICAL("Stamina & Gym", "Focus on endurance, injury prevention, & strength", "💪"),
    GOALKEEPING("Goalkeeping", "Focus on diving reflexes, handling, & 1v1 stopping", "🧤")
}

data class Player(
    val id: String,
    val name: String,
    val position: PlayerPosition,
    val age: Int,
    val nationality: String,
    var overall: Int,
    var pace: Int,
    var shooting: Int,
    var passing: Int,
    var dribbling: Int,
    var defending: Int,
    var physical: Int,
    var condition: Int = 100, // 0 to 100%
    var morale: Int = 90,     // 0 to 100%
    var form: Int = 7,        // 1 to 10
    var valueEuro: Long = 10_000_000L,
    var wageWeeklyEuro: Long = 50_000L,
    var contractYears: Int = 3,
    var yellowCards: Int = 0,
    var isRedCard: Boolean = false,
    var suspensionMatchesRemaining: Int = 0,
    var injuryWeeks: Int = 0,
    var injuryType: String = "",
    var potential: Int = (overall + 4).coerceAtMost(99),
    var matchesPlayed: Int = 0,
    var goalsScored: Int = 0,
    var assists: Int = 0,
    var avgRating: Float = 6.8f,
    var matchRating: Float = 6.0f,
    var liveRating: Float = 6.0f,
    var number: Int = 11,
    var tacticalRole: PlayerTacticalRole = PlayerTacticalRole.defaultForPosition(position),
    var isStarting: Boolean = false,
    var isSubstitute: Boolean = false,
    var isLoan: Boolean = false,
    var consecutiveBenchedMatches: Int = 0,
    var grievanceStatus: String? = null,
    var trainingFocus: TrainingFocus = TrainingFocus.BALANCED,
    var trainingProgressPoints: Int = 0
) {
    val isInjured: Boolean get() = injuryWeeks > 0
    val isSuspended: Boolean get() = isRedCard || yellowCards >= 5 || suspensionMatchesRemaining > 0
    val hasGrievance: Boolean get() = grievanceStatus != null

    fun canPlay(): Boolean = !isInjured && !isSuspended

    fun resolveGrievance(resolutionPromise: String) {
        grievanceStatus = null
        consecutiveBenchedMatches = 0
    }

    fun copyPlayer(): Player = this.copy()
}
