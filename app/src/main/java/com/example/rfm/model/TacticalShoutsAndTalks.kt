package com.example.rfm.model

/**
 * Real-time manager instructions shouted from the touchline during live match simulation.
 */
enum class TouchlineShout(
    val label: String,
    val description: String,
    val icon: String,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val possessionBonus: Int = 0,
    val foulMultiplier: Float = 1.0f,
    val staminaDrain: Float = 1.0f
) {
    NONE(
        label = "Normal Play",
        description = "Balanced tactical execution as pre-set before kickoff.",
        icon = "⚽"
    ),
    PUMP_BALL_INTO_BOX(
        label = "Pump Ball into Box",
        description = "Direct aerial bombardment into the penalty area for late goal chances.",
        icon = "📦",
        attackBonus = 16,
        defenseBonus = -8,
        staminaDrain = 1.15f
    ),
    CLEAR_BALL_TO_FLANKS(
        label = "Clear Ball to Flanks",
        description = "Relieves pressure by safety clearances out wide to wingers.",
        icon = "🛡️",
        attackBonus = -6,
        defenseBonus = 18,
        possessionBonus = -8
    ),
    TAKE_MORE_RISKS(
        label = "Take More Risks",
        description = "Urgent high-tempo pressing and forward surging to chase the game.",
        icon = "⚡",
        attackBonus = 22,
        defenseBonus = -12,
        possessionBonus = 5,
        staminaDrain = 1.35f
    ),
    RETAIN_POSSESSION(
        label = "Retain Possession",
        description = "Patient, low-risk circulation to starve opponent of possession and manage lead.",
        icon = "⏳",
        attackBonus = -10,
        defenseBonus = 6,
        possessionBonus = 18,
        staminaDrain = 0.9f
    ),
    COMMIT_MORE_FOULS(
        label = "Commit More Fouls",
        description = "Cynical tactical fouls in midfield to disrupt dangerous counter-attacks.",
        icon = "🟨",
        defenseBonus = 14,
        foulMultiplier = 2.2f
    )
}

/**
 * Dressing room team talks for half-time and full-time moments.
 */
enum class TeamTalkOption(
    val label: String,
    val tone: String,
    val description: String,
    val moraleDelta: Int,
    val secondHalfAttackBonus: Int = 0
) {
    DEMAND_MORE(
        label = "Demand More / Passion",
        tone = "Passionate",
        description = "Demands that players show more hunger, fight, and intensity.",
        moraleDelta = 8,
        secondHalfAttackBonus = 12
    ),
    ENCOURAGE(
        label = "Encourage / Keep Faith",
        tone = "Calm",
        description = "Reassures the squad to trust their quality and stick to the game plan.",
        moraleDelta = 6,
        secondHalfAttackBonus = 5
    ),
    STAY_FOCUSED(
        label = "Stay Focused / No Complacency",
        tone = "Assertive",
        description = "Warns players against getting cocky or relaxing with the lead.",
        moraleDelta = 4,
        secondHalfAttackBonus = 4
    ),
    HAIRDRYER(
        label = "Hairdryer Treatment / Angry",
        tone = "Furious",
        description = "An explosive dressing-room blast to shake lazy players awake.",
        moraleDelta = -4,
        secondHalfAttackBonus = 16
    ),
    PRAISE_WIN(
        label = "Praise & Congratulate",
        tone = "Delighted",
        description = "Commends the squad for an outstanding display and victory.",
        moraleDelta = 12,
        secondHalfAttackBonus = 0
    ),
    BALANCED_REVIEW(
        label = "Constructive Assessment",
        tone = "Measured",
        description = "A professional, calm breakdown of performance areas to improve.",
        moraleDelta = 5,
        secondHalfAttackBonus = 0
    )
}

/**
 * Backroom personnel assisting the manager.
 */
data class BackroomStaff(
    val role: String, // "Assistant Manager", "Head Scout", "Chief Physio", "Fitness Coach"
    var name: String,
    var level: Int, // 1 to 5
    var weeklySalaryEuro: Long,
    var perkDescription: String
) {
    val upgradeCostEuro: Long get() = (level + 1) * 500_000L

    var ratingLevel: Int
        get() = level
        set(value) { level = value }

    var weeklyWageEuro: Long
        get() = weeklySalaryEuro
        set(value) { weeklySalaryEuro = value }

    var specialtyBonus: String
        get() = perkDescription
        set(value) { perkDescription = value }
}

/**
 * Pre-season friendly tour fixture.
 */
data class PreSeasonTourFixture(
    val id: String,
    val opponentName: String,
    val opponentCountry: String,
    val stadiumName: String,
    val appearanceFeeEuro: Long,
    var isPlayed: Boolean = false,
    var homeScore: Int = 0,
    var awayScore: Int = 0
) {
    val tourLocation: String get() = "$opponentCountry ($stadiumName)"
    val commercialBonusEuro: Long get() = appearanceFeeEuro
    var userScore: Int
        get() = homeScore
        set(value) { homeScore = value }
    var opponentScore: Int
        get() = awayScore
        set(value) { awayScore = value }
    val opponentStrength: Int get() = when (opponentName) {
        "Santos FC" -> 82
        "LA Galaxy" -> 76
        "New York Red Bulls" -> 75
        "Shanghai Shenhua" -> 70
        else -> 74
    }
}
