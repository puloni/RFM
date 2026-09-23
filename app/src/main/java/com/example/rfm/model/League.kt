package com.example.rfm.model

data class TeamStanding(
    val teamId: String,
    val teamName: String,
    val shortName: String,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0,
    val formHistory: MutableList<Char> = mutableListOf() // 'W', 'D', 'L'
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

data class League(
    val id: String,
    val name: String,
    val country: String,
    val teams: List<Team>,
    val standings: MutableList<TeamStanding> = mutableListOf()
) {
    fun sortStandings() {
        standings.sortWith(
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )
    }
}
