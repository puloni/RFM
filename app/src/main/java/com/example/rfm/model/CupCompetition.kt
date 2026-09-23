package com.example.rfm.model

data class CupMatch(
    val id: String,
    val competitionName: String,
    val roundName: String, // "Quarter-Final", "Semi-Final", "Grand Final"
    val homeTeamId: String,
    val awayTeamId: String,
    var homeScore: Int? = null,
    var awayScore: Int? = null,
    var extraTimeScoreHome: Int? = null,
    var extraTimeScoreAway: Int? = null,
    var penaltiesHome: Int? = null,
    var penaltiesAway: Int? = null,
    var isPlayed: Boolean = false,
    var winnerTeamId: String? = null
)

data class CupCompetition(
    val id: String,
    val name: String,
    val trophyIcon: String = "🏆",
    val prizeMoneyEuro: Long = 15_000_000L,
    val matches: MutableList<CupMatch> = mutableListOf(),
    var isCompleted: Boolean = false,
    var winnerTeamId: String? = null
)
