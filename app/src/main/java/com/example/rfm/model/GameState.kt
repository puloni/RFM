package com.example.rfm.model

data class NewsMessage(
    val id: String,
    val title: String,
    val content: String,
    val dateString: String,
    val isRead: Boolean = false,
    val isImportant: Boolean = false
)

data class TransferOffer(
    val id: String,
    val playerId: String,
    val playerName: String,
    val playerPosition: PlayerPosition,
    val playerOverall: Int,
    val fromTeamId: String,
    val fromTeamName: String,
    val toTeamId: String,
    val toTeamName: String,
    val offerAmountEuro: Long,
    val proposedWageEuroWeekly: Long,
    val isIncomingToUser: Boolean,
    var status: String = "PENDING" // PENDING, ACCEPTED, REJECTED
)

data class GameState(
    var userTeamId: String = "man_utd",
    var managerName: String = "Sir Manager",
    var currentSeasonYear: Int = 2013,
    var currentMatchDay: Int = 1,
    var totalMatchDays: Int = 38,
    var boardConfidence: Int = 85, // 0 - 100%
    var managerReputation: Int = 75, // 0 - 100%
    val leagues: MutableList<League> = mutableListOf(),
    val fixtures: MutableList<Fixture> = mutableListOf(),
    val newsInbox: MutableList<NewsMessage> = mutableListOf(),
    val transferMarket: MutableList<Player> = mutableListOf(),
    val freeAgents: MutableList<Player> = mutableListOf(),
    val loanMarket: MutableList<Player> = mutableListOf(),
    val activeTransferOffers: MutableList<TransferOffer> = mutableListOf(),
    var currentTrainingFocus: String = "Balanced Training",
    val youthAcademyProspects: MutableList<YouthProspect> = mutableListOf(),
    val scoutedPlayers: MutableList<Player> = mutableListOf(),
    val cupCompetitions: MutableList<CupCompetition> = mutableListOf(),
    val trophiesWon: MutableList<String> = mutableListOf(),
    val backroomStaff: MutableList<BackroomStaff> = mutableListOf(
        BackroomStaff("Assistant Manager", "Mike Phelan", 3, 25_000L, "Auto-picks optimal starting XI and gives tactical advice."),
        BackroomStaff("Head Scout", "Jim Lawlor", 3, 20_000L, "Discovers high-potential wonderkids with accurate ability ratings."),
        BackroomStaff("Chief Physio", "Rob Swire", 3, 18_000L, "Reduces squad injury risk by 40% and speeds recovery time."),
        BackroomStaff("Fitness Coach", "Tony Strudwick", 3, 16_000L, "Reduces match fatigue by 20% and accelerates stamina recovery.")
    ),
    val preSeasonFixtures: MutableList<PreSeasonTourFixture> = mutableListOf(
        PreSeasonTourFixture("tour_1", "New York Red Bulls", "USA", "Red Bull Arena", 2_500_000L),
        PreSeasonTourFixture("tour_2", "LA Galaxy", "USA", "Dignity Health Sports Park", 3_000_000L),
        PreSeasonTourFixture("tour_3", "Santos FC", "Brazil", "Vila Belmiro", 3_500_000L)
    ),
    var modSettings: ModSettings = ModSettings(),
    var isRetroKeypadEnabled: Boolean = false,
    var isSoundEnabled: Boolean = true
) {
    fun getUserTeam(): Team? {
        for (league in leagues) {
            val team = league.teams.find { it.id == userTeamId }
            if (team != null) return team
        }
        return null
    }

    fun getUserLeague(): League? {
        return leagues.find { it.teams.any { t -> t.id == userTeamId } }
    }

    fun getTeamById(teamId: String): Team? {
        for (league in leagues) {
            val team = league.teams.find { it.id == teamId }
            if (team != null) return team
        }
        return null
    }

    fun getNextFixtureForUser(): Fixture? {
        return fixtures.firstOrNull {
            !it.isPlayed && (it.homeTeamId == userTeamId || it.awayTeamId == userTeamId)
        }
    }
}
