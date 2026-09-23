package com.example.rfm.model

enum class CompetitionType(val label: String, val hasExtraTimeAndPenalties: Boolean) {
    LEAGUE("League Match", false),
    DOMESTIC_CUP("Domestic Cup", true),
    CHAMPIONS_CUP("Champions Cup", true),
    EUROPA_CUP("Europa Cup", true),
    SUPER_CUP("Super Cup", true),
    WORLD_CUP("World Trophy", true),
    EXHIBITION("Exhibition Friendly", false)
}

enum class ShotOutcome {
    GOAL,
    SAVED,
    WOODWORK,
    OFF_TARGET,
    BLOCKED
}

data class ShotDetail(
    val minute: Int,
    val isHome: Boolean,
    val shooterName: String,
    val xG: Float,
    val outcome: ShotOutcome,
    val pitchX: Float, // 0.0 - 1.0
    val pitchY: Float  // 0.0 - 1.0
)

enum class MatchEventType {
    KICKOFF,
    GOAL,
    PENALTY_GOAL,
    PENALTY_MISSED,
    SHOT_SAVED,
    SHOT_WOODWORK,
    SHOT_OFF_TARGET,
    CORNER,
    FREE_KICK,
    FOUL,
    YELLOW_CARD,
    RED_CARD,
    INJURY,
    SUBSTITUTION,
    EXTRA_TIME_START,
    PENALTY_SHOOTOUT,
    HALF_TIME,
    FULL_TIME,
    TACTICAL_CHANGE
}

data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val teamId: String,
    val primaryPlayerName: String? = null,
    val secondaryPlayerName: String? = null,
    val description: String,
    val homeScore: Int,
    val awayScore: Int
)

data class MatchStats(
    var homeShots: Int = 0,
    var awayShots: Int = 0,
    var homeShotsOnTarget: Int = 0,
    var awayShotsOnTarget: Int = 0,
    var homePossession: Int = 50,
    var awayPossession: Int = 50,
    var homeXg: Float = 0.0f,
    var awayXg: Float = 0.0f,
    var homePassesAttempted: Int = 380,
    var homePassesCompleted: Int = 310,
    var awayPassesAttempted: Int = 370,
    var awayPassesCompleted: Int = 300,
    var homeTacklesWon: Int = 18,
    var homeTacklesTotal: Int = 24,
    var awayTacklesWon: Int = 17,
    var awayTacklesTotal: Int = 23,
    var homeFouls: Int = 0,
    var awayFouls: Int = 0,
    var homeCorners: Int = 0,
    var awayCorners: Int = 0,
    var homeYellowCards: Int = 0,
    var awayYellowCards: Int = 0,
    var homeRedCards: Int = 0,
    var awayRedCards: Int = 0
) {
    val homePassAccuracy: Int get() = if (homePassesAttempted > 0) ((homePassesCompleted.toFloat() / homePassesAttempted) * 100).toInt() else 80
    val awayPassAccuracy: Int get() = if (awayPassesAttempted > 0) ((awayPassesCompleted.toFloat() / awayPassesAttempted) * 100).toInt() else 80
    val homeTackleSuccessRate: Int get() = if (homeTacklesTotal > 0) ((homeTacklesWon.toFloat() / homeTacklesTotal) * 100).toInt() else 75
    val awayTackleSuccessRate: Int get() = if (awayTacklesTotal > 0) ((awayTacklesWon.toFloat() / awayTacklesTotal) * 100).toInt() else 75
}

data class Fixture(
    val id: String,
    val round: Int,
    val homeTeamId: String,
    val awayTeamId: String,
    var homeScore: Int? = null,
    var awayScore: Int? = null,
    var penaltyShootoutHomeScore: Int? = null,
    var penaltyShootoutAwayScore: Int? = null,
    var competitionType: CompetitionType = CompetitionType.LEAGUE,
    var competitionName: String = "League Match",
    var weather: MatchWeather = MatchWeather.CLEAR,
    var isPlayed: Boolean = false,
    val events: MutableList<MatchEvent> = mutableListOf(),
    var stats: MatchStats = MatchStats(),
    val shotMap: MutableList<ShotDetail> = mutableListOf(),
    val playerRatings: MutableMap<String, Float> = mutableMapOf(),
    var manOfTheMatchId: String? = null,
    var manOfTheMatchName: String? = null
)
