package com.example.rfm.data

import com.example.rfm.model.*

object RfmDatabase {

    fun createInitialGameState(): GameState {
        val leagues = mutableListOf(
            createPremierLeague(),
            createLaLiga(),
            createSerieA(),
            createBundesliga(),
            createLigue1(),
            createRestOfEurope(),
            createInternationalLeague()
        )

        // Initialize standings for all leagues
        for (league in leagues) {
            league.standings.clear()
            for (team in league.teams) {
                league.standings.add(
                    TeamStanding(
                        teamId = team.id,
                        teamName = team.name,
                        shortName = team.shortName
                    )
                )
            }
        }

        // Generate full season fixtures for ALL leagues
        val allFixtures = mutableListOf<Fixture>()
        for (league in leagues) {
            allFixtures.addAll(generateRoundRobinFixtures(league.teams))
        }

        // Welcome news messages
        val initialNews = mutableListOf(
            NewsMessage(
                id = "news_1",
                title = "Welcome to Real Football Manager 2013!",
                content = "The board welcomes you to the club. Your objective this season is to challenge for silverware and maintain strict financial discipline.",
                dateString = "Aug 15, 2012",
                isImportant = true
            ),
            NewsMessage(
                id = "news_2",
                title = "Transfer Window Open",
                content = "The summer transfer market is buzzing. Scouts have highlighted top targets across Europe available for negotiations.",
                dateString = "Aug 16, 2012"
            ),
            NewsMessage(
                id = "news_3",
                title = "Pre-season Training Completed",
                content = "Squad fitness levels are primed for the season opener. Set your tactics and starting XI before matchday.",
                dateString = "Aug 17, 2012"
            )
        )

        // Transfer Market Pool
        val marketPlayers = mutableListOf(
            createPlayer("mkt_1", "Neymar Jr", PlayerPosition.LW, 20, "Brazil", 86, 92, 83, 79, 94, 38, 68, 48_000_000L, 110_000L),
            createPlayer("mkt_2", "Isco", PlayerPosition.CAM, 20, "Spain", 82, 78, 77, 85, 87, 45, 65, 22_000_000L, 65_000L),
            createPlayer("mkt_3", "Christian Eriksen", PlayerPosition.CAM, 20, "Denmark", 81, 75, 78, 85, 83, 50, 68, 19_000_000L, 60_000L),
            createPlayer("mkt_4", "Pierre-Emerick Aubameyang", PlayerPosition.ST, 23, "Gabon", 80, 95, 80, 72, 80, 40, 76, 17_000_000L, 55_000L),
            createPlayer("mkt_5", "Kevin De Bruyne", PlayerPosition.CM, 21, "Belgium", 80, 76, 78, 84, 80, 56, 72, 16_500_000L, 50_000L),
            createPlayer("mkt_6", "David Alaba", PlayerPosition.LB, 20, "Austria", 81, 86, 72, 79, 81, 78, 77, 18_000_000L, 60_000L),
            createPlayer("mkt_7", "Jan Oblak", PlayerPosition.GK, 19, "Slovenia", 78, 50, 25, 65, 45, 80, 75, 12_000_000L, 40_000L),
            createPlayer("mkt_8", "Raphael Varane", PlayerPosition.CB, 19, "France", 81, 80, 48, 68, 65, 83, 79, 18_500_000L, 55_000L)
        )

        // Free Agents (Bosman Free Transfers)
        val freeAgentPlayers = mutableListOf(
            createPlayer("fa_1", "Michael Owen", PlayerPosition.ST, 33, "England", 75, 78, 80, 68, 74, 30, 62, 0L, 20_000L),
            createPlayer("fa_2", "Alessandro Del Piero", PlayerPosition.ST, 38, "Italy", 78, 64, 82, 85, 83, 35, 65, 0L, 25_000L),
            createPlayer("fa_3", "Robert Pires", PlayerPosition.LM, 39, "France", 73, 62, 74, 83, 80, 42, 60, 0L, 15_000L),
            createPlayer("fa_4", "Thomas Hitzlsperger", PlayerPosition.CM, 30, "Germany", 74, 68, 79, 76, 72, 70, 78, 0L, 18_000L),
            createPlayer("fa_5", "Mikael Silvestre", PlayerPosition.CB, 35, "France", 73, 66, 42, 65, 60, 76, 75, 0L, 16_000L),
            createPlayer("fa_6", "Craig Gordon", PlayerPosition.GK, 30, "Scotland", 74, 45, 20, 65, 40, 76, 74, 0L, 14_000L)
        )

        // Loan Market Prospects
        val loanMarketPlayers = mutableListOf(
            createPlayer("loan_1", "Romelu Lukaku", PlayerPosition.ST, 19, "Belgium", 78, 84, 80, 68, 76, 40, 88, 14_000_000L, 45_000L).apply { isLoan = true },
            createPlayer("loan_2", "Thibaut Courtois", PlayerPosition.GK, 20, "Belgium", 82, 50, 20, 70, 45, 84, 80, 20_000_000L, 50_000L).apply { isLoan = true },
            createPlayer("loan_3", "Wilfried Zaha", PlayerPosition.RW, 20, "England", 76, 90, 70, 72, 84, 35, 72, 10_000_000L, 30_000L).apply { isLoan = true },
            createPlayer("loan_4", "Casemiro", PlayerPosition.CDM, 20, "Brazil", 76, 68, 65, 75, 72, 80, 84, 11_000_000L, 35_000L).apply { isLoan = true },
            createPlayer("loan_5", "Dani Carvajal", PlayerPosition.RB, 20, "Spain", 77, 83, 50, 74, 76, 78, 77, 12_000_000L, 35_000L).apply { isLoan = true },
            createPlayer("loan_6", "Denis Cheryshev", PlayerPosition.LW, 21, "Russia", 75, 86, 73, 72, 78, 40, 70, 8_000_000L, 25_000L).apply { isLoan = true }
        )

        // Initial Youth Academy Prospects
        val initialProspects = mutableListOf(
            YouthProspect("yp_1", "Adnan Januzaj", PlayerPosition.CAM, 17, "Belgium", 70, 88, 3_500_000L, 12_000L, "Tricky Playmaker"),
            YouthProspect("yp_2", "Paul Pogba", PlayerPosition.CM, 19, "France", 75, 91, 8_000_000L, 25_000L, "Midfield Powerhouse"),
            YouthProspect("yp_3", "Raheem Sterling", PlayerPosition.RW, 17, "England", 73, 89, 6_000_000L, 20_000L, "Pacey Winger"),
            YouthProspect("yp_4", "Jesse Lingard", PlayerPosition.CAM, 19, "England", 68, 84, 2_000_000L, 8_000L, "Academy Graduate"),
            YouthProspect("yp_5", "Gerard Deulofeu", PlayerPosition.LW, 18, "Spain", 72, 87, 5_000_000L, 15_000L, "La Masia Prodigy"),
            YouthProspect("yp_6", "Serge Gnabry", PlayerPosition.RW, 17, "Germany", 69, 88, 2_800_000L, 10_000L, "Explosive Forward")
        )

        // Domestic Cup (National Cup)
        val domesticCup = CupCompetition(
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
        )

        // European Champions Cup
        val championsCup = CupCompetition(
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
        )

        // European Europa Cup
        val europaCup = CupCompetition(
            id = "europa_cup",
            name = "European Europa Trophy",
            trophyIcon = "🛡️",
            prizeMoneyEuro = 20_000_000L,
            matches = mutableListOf(
                CupMatch("eur_qf_1", "European Europa Trophy", "Quarter-Final", "tottenham", "atletico"),
                CupMatch("eur_qf_2", "European Europa Trophy", "Quarter-Final", "inter", "valencia"),
                CupMatch("eur_qf_3", "European Europa Trophy", "Quarter-Final", "porto", "benfica"),
                CupMatch("eur_qf_4", "European Europa Trophy", "Quarter-Final", "ajax", "napoli")
            )
        )

        // European Super Cup
        val superCup = CupCompetition(
            id = "super_cup",
            name = "European Super Cup",
            trophyIcon = "⭐",
            prizeMoneyEuro = 10_000_000L,
            matches = mutableListOf(
                CupMatch("super_final_1", "European Super Cup", "Grand Final", "chelsea", "atletico")
            )
        )

        // International World Championship
        val worldCup = CupCompetition(
            id = "world_cup",
            name = "International World Trophy",
            trophyIcon = "🌍",
            prizeMoneyEuro = 50_000_000L,
            matches = mutableListOf(
                CupMatch("wc_qf_1", "International World Trophy", "Quarter-Final", "nat_brazil", "nat_england"),
                CupMatch("wc_qf_2", "International World Trophy", "Quarter-Final", "nat_spain", "nat_france"),
                CupMatch("wc_qf_3", "International World Trophy", "Quarter-Final", "nat_germany", "nat_italy"),
                CupMatch("wc_qf_4", "International World Trophy", "Quarter-Final", "nat_argentina", "nat_netherlands")
            )
        )

        val initialOffers = mutableListOf(
            TransferOffer(
                id = "offer_init_1",
                playerId = "man_utd_s_9",
                playerName = "Nani",
                playerPosition = PlayerPosition.RW,
                playerOverall = 85,
                fromTeamId = "juventus",
                fromTeamName = "Juventus",
                toTeamId = "man_utd",
                toTeamName = "Manchester United",
                offerAmountEuro = 24_000_000L,
                proposedWageEuroWeekly = 110_000L,
                isIncomingToUser = true,
                status = "PENDING"
            )
        )

        val state = GameState(
            userTeamId = "man_utd",
            managerName = "Sir Alex",
            currentSeasonYear = 2013,
            currentMatchDay = 1,
            totalMatchDays = 38,
            leagues = leagues,
            fixtures = allFixtures,
            newsInbox = initialNews,
            transferMarket = marketPlayers
        )
        state.youthAcademyProspects.addAll(initialProspects)
        state.cupCompetitions.addAll(listOf(domesticCup, championsCup, europaCup, superCup, worldCup))
        state.freeAgents.addAll(freeAgentPlayers)
        state.loanMarket.addAll(loanMarketPlayers)
        state.activeTransferOffers.addAll(initialOffers)
        return state
    }

    fun generateRoundRobinFixtures(teams: List<Team>): List<Fixture> {
        val fixtures = mutableListOf<Fixture>()
        val n = teams.size
        if (n < 2) return fixtures

        val teamList = teams.toList()
        val halfRounds = n - 1
        val matchesPerRound = n / 2
        val rotation = teamList.subList(1, n).toMutableList()

        for (r in 0 until halfRounds) {
            val roundNum = r + 1
            for (m in 0 until matchesPerRound) {
                val home: Team
                val away: Team
                if (m == 0) {
                    val fixed = teamList[0]
                    val opponent = rotation[0]
                    if (r % 2 == 0) {
                        home = fixed
                        away = opponent
                    } else {
                        home = opponent
                        away = fixed
                    }
                } else {
                    val t1 = rotation[m]
                    val t2 = rotation[rotation.size - m]
                    if (m % 2 == 0) {
                        home = t1
                        away = t2
                    } else {
                        home = t2
                        away = t1
                    }
                }
                fixtures.add(
                    Fixture(
                        id = "fix_${home.leagueId}_${roundNum}_${home.id}_${away.id}",
                        round = roundNum,
                        homeTeamId = home.id,
                        awayTeamId = away.id
                    )
                )
            }
            val last = rotation.removeAt(rotation.size - 1)
            rotation.add(0, last)
        }

        val firstHalfSize = fixtures.size
        for (i in 0 until firstHalfSize) {
            val f = fixtures[i]
            val secondHalfRound = f.round + halfRounds
            fixtures.add(
                Fixture(
                    id = "fix_${f.id}_rev",
                    round = secondHalfRound,
                    homeTeamId = f.awayTeamId,
                    awayTeamId = f.homeTeamId
                )
            )
        }

        return fixtures
    }

    private fun createPremierLeague(): League {
        val existing = listOf(
            createManUnited(),
            createManCity(),
            createChelsea(),
            createArsenal(),
            createLiverpool(),
            createTottenham()
        )
        val seeds = RfmTeamSeeds.eplSeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("epl", "Premier League", "England", existing + seeds)
    }

    private fun createLaLiga(): League {
        val existing = listOf(
            createRealMadrid(),
            createBarcelona(),
            createAtleticoMadrid(),
            createValencia()
        )
        val seeds = RfmTeamSeeds.laLigaSeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("laliga", "La Liga", "Spain", existing + seeds)
    }

    private fun createSerieA(): League {
        val existing = listOf(
            createJuventus(),
            createMilan(),
            createInter(),
            createNapoli()
        )
        val seeds = RfmTeamSeeds.serieASeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("seriea", "Serie A", "Italy", existing + seeds)
    }

    private fun createBundesliga(): League {
        val existing = listOf(
            createBayern(),
            createDortmund()
        )
        val seeds = RfmTeamSeeds.bundesligaSeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("bundesliga", "Bundesliga", "Germany", existing + seeds)
    }

    private fun createLigue1(): League {
        val existing = listOf(
            createPsg()
        )
        val seeds = RfmTeamSeeds.ligue1Seeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("ligue1", "Ligue 1", "France", existing + seeds)
    }

    private fun createRestOfEurope(): League {
        val seeds = RfmTeamSeeds.restOfEuropeSeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("europe", "Rest of Europe", "Europe", seeds)
    }

    private fun createInternationalLeague(): League {
        val seeds = RfmTeamSeeds.nationalTeamSeeds.map { RfmTeamSeeds.buildSeedTeam(it) }
        return League("international", "International Nations", "World", seeds)
    }

    // --- TEAMS CREATION (2012-2013 Authentic Squads) ---

    private fun createManUnited(): Team {
        val team = Team(
            id = "man_utd",
            name = "Manchester United",
            shortName = "MUN",
            leagueId = "epl",
            reputationStars = 5,
            balanceEuro = 85_000_000L,
            transferBudgetEuro = 55_000_000L,
            wageBudgetEuroWeekly = 2_200_000L,
            stadiumName = "Old Trafford",
            stadiumCapacity = 75_635,
            primaryColorHex = 0xFFD81A21,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_442
        )
        val players = listOf(
            // Starting XI
            createPlayer("mun_1", "David de Gea", PlayerPosition.GK, 22, "Spain", 84, 52, 20, 72, 45, 86, 75, 25_000_000L, 90_000L, isStarting = true),
            createPlayer("mun_2", "Rafael da Silva", PlayerPosition.RB, 22, "Brazil", 79, 86, 56, 75, 78, 77, 76, 12_000_000L, 60_000L, isStarting = true),
            createPlayer("mun_3", "Rio Ferdinand", PlayerPosition.CB, 33, "England", 85, 68, 45, 72, 66, 88, 82, 14_000_000L, 110_000L, isStarting = true),
            createPlayer("mun_4", "Nemanja Vidic", PlayerPosition.CB, 31, "Serbia", 89, 65, 40, 68, 55, 93, 91, 26_000_000L, 125_000L, isStarting = true),
            createPlayer("mun_5", "Patrice Evra", PlayerPosition.LB, 31, "France", 83, 80, 62, 78, 80, 82, 79, 15_000_000L, 95_000L, isStarting = true),
            createPlayer("mun_6", "Antonio Valencia", PlayerPosition.RM, 27, "Ecuador", 83, 91, 74, 80, 82, 72, 85, 18_000_000L, 85_000L, isStarting = true),
            createPlayer("mun_7", "Michael Carrick", PlayerPosition.CM, 31, "England", 84, 62, 73, 89, 79, 81, 76, 17_000_000L, 95_000L, isStarting = true),
            createPlayer("mun_8", "Paul Scholes", PlayerPosition.CM, 37, "England", 82, 50, 84, 91, 80, 74, 70, 8_000_000L, 90_000L, isStarting = true),
            createPlayer("mun_9", "Ryan Giggs", PlayerPosition.LM, 38, "Wales", 80, 70, 76, 84, 82, 60, 68, 6_000_000L, 80_000L, isStarting = true),
            createPlayer("mun_10", "Wayne Rooney", PlayerPosition.ST, 27, "England", 89, 82, 89, 84, 85, 70, 87, 48_000_000L, 180_000L, isStarting = true),
            createPlayer("mun_11", "Robin van Persie", PlayerPosition.ST, 29, "Netherlands", 90, 81, 92, 82, 87, 45, 76, 52_000_000L, 190_000L, isStarting = true),
            // Bench
            createPlayer("mun_12", "Anders Lindegaard", PlayerPosition.GK, 28, "Denmark", 77, 45, 20, 65, 40, 79, 74, 5_000_000L, 40_000L, isSub = true),
            createPlayer("mun_13", "Jonny Evans", PlayerPosition.CB, 24, "N. Ireland", 80, 69, 42, 68, 60, 82, 80, 10_000_000L, 55_000L, isSub = true),
            createPlayer("mun_14", "Chris Smalling", PlayerPosition.CB, 23, "England", 79, 75, 38, 62, 55, 80, 82, 9_500_000L, 50_000L, isSub = true),
            createPlayer("mun_15", "Tom Cleverley", PlayerPosition.CM, 23, "England", 78, 74, 72, 80, 78, 68, 73, 8_000_000L, 45_000L, isSub = true),
            createPlayer("mun_16", "Nani", PlayerPosition.LW, 26, "Portugal", 83, 89, 79, 81, 88, 48, 69, 20_000_000L, 85_000L, isSub = true),
            createPlayer("mun_17", "Shinji Kagawa", PlayerPosition.CAM, 23, "Japan", 82, 80, 75, 84, 86, 42, 62, 19_000_000L, 80_000L, isSub = true),
            createPlayer("mun_18", "Javier Hernandez", PlayerPosition.ST, 24, "Mexico", 82, 85, 83, 68, 77, 36, 70, 18_000_000L, 75_000L, isSub = true),
            // Reserves
            createPlayer("mun_19", "Danny Welbeck", PlayerPosition.ST, 22, "England", 79, 84, 76, 73, 80, 45, 76, 11_000_000L, 50_000L),
            createPlayer("mun_20", "Phil Jones", PlayerPosition.CB, 21, "England", 78, 72, 52, 65, 60, 80, 84, 9_000_000L, 45_000L)
        )
        team.players.addAll(players)
        team.captainPlayerId = "mun_4"
        team.penaltyTakerId = "mun_11"
        team.freeKickTakerId = "mun_10"
        team.cornerTakerId = "mun_9"
        return team
    }

    private fun createRealMadrid(): Team {
        val team = Team(
            id = "real_madrid",
            name = "Real Madrid",
            shortName = "RMA",
            leagueId = "laliga",
            reputationStars = 5,
            balanceEuro = 110_000_000L,
            transferBudgetEuro = 75_000_000L,
            wageBudgetEuroWeekly = 2_800_000L,
            stadiumName = "Santiago Bernabeu",
            stadiumCapacity = 81_044,
            primaryColorHex = 0xFFFFFFFF,
            secondaryColorHex = 0xFF0B1B2B,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("rma_1", "Iker Casillas", PlayerPosition.GK, 31, "Spain", 90, 58, 20, 75, 45, 92, 80, 32_000_000L, 140_000L, isStarting = true),
            createPlayer("rma_2", "Alvaro Arbeloa", PlayerPosition.RB, 29, "Spain", 80, 76, 50, 72, 70, 81, 79, 10_000_000L, 65_000L, isStarting = true),
            createPlayer("rma_3", "Sergio Ramos", PlayerPosition.CB, 26, "Spain", 87, 80, 68, 76, 72, 88, 86, 36_000_000L, 130_000L, isStarting = true),
            createPlayer("rma_4", "Pepe", PlayerPosition.CB, 29, "Portugal", 85, 75, 52, 66, 60, 87, 88, 22_000_000L, 100_000L, isStarting = true),
            createPlayer("rma_5", "Marcelo", PlayerPosition.LB, 24, "Brazil", 84, 85, 70, 80, 86, 79, 78, 25_000_000L, 95_000L, isStarting = true),
            createPlayer("rma_6", "Xabi Alonso", PlayerPosition.CDM, 30, "Spain", 87, 60, 78, 93, 80, 84, 80, 24_000_000L, 120_000L, isStarting = true),
            createPlayer("rma_7", "Sami Khedira", PlayerPosition.CDM, 25, "Germany", 83, 72, 70, 78, 75, 84, 87, 18_000_000L, 85_000L, isStarting = true),
            createPlayer("rma_8", "Angel Di Maria", PlayerPosition.RM, 24, "Argentina", 86, 88, 78, 83, 88, 62, 71, 35_000_000L, 110_000L, isStarting = true),
            createPlayer("rma_9", "Mesut Ozil", PlayerPosition.CAM, 24, "Germany", 87, 76, 74, 91, 89, 45, 65, 42_000_000L, 130_000L, isStarting = true),
            createPlayer("rma_10", "Cristiano Ronaldo", PlayerPosition.LW, 27, "Portugal", 94, 93, 95, 82, 92, 52, 88, 90_000_000L, 250_000L, isStarting = true),
            createPlayer("rma_11", "Karim Benzema", PlayerPosition.ST, 25, "France", 87, 83, 86, 78, 85, 40, 79, 40_000_000L, 130_000L, isStarting = true),
            // Bench
            createPlayer("rma_12", "Diego Lopez", PlayerPosition.GK, 31, "Spain", 81, 48, 20, 68, 38, 83, 78, 8_000_000L, 50_000L, isSub = true),
            createPlayer("rma_13", "Raphael Varane", PlayerPosition.CB, 19, "France", 81, 80, 48, 68, 65, 83, 79, 18_000_000L, 55_000L, isSub = true),
            createPlayer("rma_14", "Fabio Coentrao", PlayerPosition.LB, 24, "Portugal", 80, 84, 65, 75, 79, 78, 75, 12_000_000L, 65_000L, isSub = true),
            createPlayer("rma_15", "Luka Modric", PlayerPosition.CM, 27, "Croatia", 86, 77, 75, 88, 87, 70, 68, 32_000_000L, 110_000L, isSub = true),
            createPlayer("rma_16", "Kaka", PlayerPosition.CAM, 30, "Brazil", 82, 75, 78, 82, 83, 44, 68, 14_000_000L, 120_000L, isSub = true),
            createPlayer("rma_17", "Jose Callejon", PlayerPosition.RW, 25, "Spain", 80, 86, 77, 73, 79, 48, 72, 13_000_000L, 60_000L, isSub = true),
            createPlayer("rma_18", "Gonzalo Higuain", PlayerPosition.ST, 25, "Argentina", 85, 82, 87, 72, 81, 38, 76, 32_000_000L, 110_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "rma_1"
        team.penaltyTakerId = "rma_10"
        team.freeKickTakerId = "rma_10"
        team.cornerTakerId = "rma_9"
        return team
    }

    private fun createBarcelona(): Team {
        val team = Team(
            id = "barcelona",
            name = "FC Barcelona",
            shortName = "BAR",
            leagueId = "laliga",
            reputationStars = 5,
            balanceEuro = 100_000_000L,
            transferBudgetEuro = 65_000_000L,
            wageBudgetEuroWeekly = 2_700_000L,
            stadiumName = "Camp Nou",
            stadiumCapacity = 99_354,
            primaryColorHex = 0xFF004D98,
            secondaryColorHex = 0xFFA50044,
            formation = Formation.F_433
        )
        val players = listOf(
            createPlayer("bar_1", "Victor Valdes", PlayerPosition.GK, 30, "Spain", 85, 55, 20, 78, 50, 87, 76, 18_000_000L, 100_000L, isStarting = true),
            createPlayer("bar_2", "Dani Alves", PlayerPosition.RB, 29, "Brazil", 86, 88, 70, 83, 85, 80, 82, 28_000_000L, 115_000L, isStarting = true),
            createPlayer("bar_3", "Gerard Pique", PlayerPosition.CB, 25, "Spain", 86, 68, 55, 78, 68, 88, 82, 30_000_000L, 115_000L, isStarting = true),
            createPlayer("bar_4", "Carles Puyol", PlayerPosition.CB, 34, "Spain", 86, 64, 48, 68, 58, 92, 88, 12_000_000L, 110_000L, isStarting = true),
            createPlayer("bar_5", "Jordi Alba", PlayerPosition.LB, 23, "Spain", 83, 92, 66, 77, 82, 78, 76, 22_000_000L, 85_000L, isStarting = true),
            createPlayer("bar_6", "Sergio Busquets", PlayerPosition.CDM, 24, "Spain", 86, 58, 65, 87, 80, 87, 80, 32_000_000L, 110_000L, isStarting = true),
            createPlayer("bar_7", "Xavi Hernandez", PlayerPosition.CM, 32, "Spain", 89, 65, 74, 95, 86, 75, 68, 28_000_000L, 150_000L, isStarting = true),
            createPlayer("bar_8", "Andres Iniesta", PlayerPosition.CM, 28, "Spain", 90, 78, 76, 92, 93, 68, 65, 52_000_000L, 160_000L, isStarting = true),
            createPlayer("bar_9", "Pedro Rodriguez", PlayerPosition.RW, 25, "Spain", 84, 87, 81, 78, 83, 50, 68, 24_000_000L, 90_000L, isStarting = true),
            createPlayer("bar_10", "Lionel Messi", PlayerPosition.ST, 25, "Argentina", 95, 92, 94, 89, 96, 40, 72, 100_000_000L, 260_000L, isStarting = true),
            createPlayer("bar_11", "David Villa", PlayerPosition.LW, 31, "Spain", 86, 80, 88, 78, 84, 42, 70, 24_000_000L, 120_000L, isStarting = true),
            // Bench
            createPlayer("bar_12", "Jose Manuel Pinto", PlayerPosition.GK, 37, "Spain", 74, 40, 15, 60, 35, 75, 72, 1_500_000L, 30_000L, isSub = true),
            createPlayer("bar_13", "Javier Mascherano", PlayerPosition.CB, 28, "Argentina", 85, 74, 52, 78, 70, 89, 84, 22_000_000L, 100_000L, isSub = true),
            createPlayer("bar_14", "Adriano Correia", PlayerPosition.LB, 28, "Brazil", 79, 82, 70, 75, 78, 75, 74, 8_500_000L, 55_000L, isSub = true),
            createPlayer("bar_15", "Alex Song", PlayerPosition.CDM, 25, "Cameroon", 82, 72, 66, 78, 75, 83, 85, 14_000_000L, 70_000L, isSub = true),
            createPlayer("bar_16", "Cesc Fabregas", PlayerPosition.CAM, 25, "Spain", 87, 72, 82, 89, 84, 65, 70, 38_000_000L, 130_000L, isSub = true),
            createPlayer("bar_17", "Thiago Alcantara", PlayerPosition.CM, 21, "Spain", 81, 76, 72, 84, 86, 62, 65, 18_000_000L, 60_000L, isSub = true),
            createPlayer("bar_18", "Alexis Sanchez", PlayerPosition.RW, 24, "Chile", 85, 88, 80, 77, 87, 45, 74, 30_000_000L, 105_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "bar_4"
        team.penaltyTakerId = "bar_10"
        team.freeKickTakerId = "bar_10"
        team.cornerTakerId = "bar_7"
        return team
    }

    private fun createChelsea(): Team {
        val team = Team(
            id = "chelsea",
            name = "Chelsea",
            shortName = "CHE",
            leagueId = "epl",
            reputationStars = 5,
            balanceEuro = 95_000_000L,
            transferBudgetEuro = 60_000_000L,
            wageBudgetEuroWeekly = 2_400_000L,
            stadiumName = "Stamford Bridge",
            stadiumCapacity = 41_631,
            primaryColorHex = 0xFF034694,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("che_1", "Petr Cech", PlayerPosition.GK, 30, "Czech Rep", 88, 55, 20, 72, 40, 90, 84, 25_000_000L, 110_000L, isStarting = true),
            createPlayer("che_2", "Branislav Ivanovic", PlayerPosition.RB, 28, "Serbia", 83, 76, 62, 70, 68, 86, 88, 16_000_000L, 85_000L, isStarting = true),
            createPlayer("che_3", "John Terry", PlayerPosition.CB, 32, "England", 86, 60, 52, 68, 58, 90, 86, 16_000_000L, 120_000L, isStarting = true),
            createPlayer("che_4", "Gary Cahill", PlayerPosition.CB, 27, "England", 82, 68, 45, 65, 58, 84, 82, 14_000_000L, 75_000L, isStarting = true),
            createPlayer("che_5", "Ashley Cole", PlayerPosition.LB, 31, "England", 84, 82, 58, 77, 78, 84, 76, 14_000_000L, 100_000L, isStarting = true),
            createPlayer("che_6", "Ramires", PlayerPosition.CM, 25, "Brazil", 82, 88, 70, 76, 80, 79, 82, 18_000_000L, 80_000L, isStarting = true),
            createPlayer("che_7", "Frank Lampard", PlayerPosition.CM, 34, "England", 84, 65, 87, 85, 78, 74, 78, 12_000_000L, 130_000L, isStarting = true),
            createPlayer("che_8", "Oscar", PlayerPosition.CAM, 21, "Brazil", 82, 79, 76, 82, 83, 55, 62, 22_000_000L, 75_000L, isStarting = true),
            createPlayer("che_9", "Juan Mata", PlayerPosition.RW, 24, "Spain", 87, 78, 80, 89, 87, 42, 64, 38_000_000L, 120_000L, isStarting = true),
            createPlayer("che_10", "Eden Hazard", PlayerPosition.LW, 21, "Belgium", 87, 89, 80, 84, 91, 40, 68, 45_000_000L, 130_000L, isStarting = true),
            createPlayer("che_11", "Fernando Torres", PlayerPosition.ST, 28, "Spain", 83, 84, 83, 72, 82, 38, 75, 20_000_000L, 140_000L, isStarting = true),
            // Bench
            createPlayer("che_12", "Ross Turnbull", PlayerPosition.GK, 27, "England", 72, 40, 18, 58, 30, 72, 70, 1_500_000L, 25_000L, isSub = true),
            createPlayer("che_13", "David Luiz", PlayerPosition.CB, 25, "Brazil", 83, 77, 68, 75, 74, 82, 80, 20_000_000L, 85_000L, isSub = true),
            createPlayer("che_14", "Cesar Azpilicueta", PlayerPosition.RB, 23, "Spain", 79, 80, 52, 73, 72, 79, 76, 10_000_000L, 50_000L, isSub = true),
            createPlayer("che_15", "John Obi Mikel", PlayerPosition.CDM, 25, "Nigeria", 80, 62, 58, 76, 70, 83, 84, 11_000_000L, 65_000L, isSub = true),
            createPlayer("che_16", "Victor Moses", PlayerPosition.RW, 22, "Nigeria", 78, 86, 72, 70, 80, 48, 76, 9_000_000L, 45_000L, isSub = true),
            createPlayer("che_17", "Marko Marin", PlayerPosition.LW, 23, "Germany", 76, 83, 70, 77, 82, 35, 55, 7_000_000L, 45_000L, isSub = true),
            createPlayer("che_18", "Demba Ba", PlayerPosition.ST, 27, "Senegal", 82, 80, 83, 66, 75, 42, 84, 16_000_000L, 75_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "che_3"
        team.penaltyTakerId = "che_7"
        team.freeKickTakerId = "che_9"
        team.cornerTakerId = "che_9"
        return team
    }

    private fun createManCity(): Team {
        val team = Team(
            id = "man_city",
            name = "Manchester City",
            shortName = "MCI",
            leagueId = "epl",
            reputationStars = 5,
            balanceEuro = 120_000_000L,
            transferBudgetEuro = 80_000_000L,
            wageBudgetEuroWeekly = 2_600_000L,
            stadiumName = "Etihad Stadium",
            stadiumCapacity = 53_400,
            primaryColorHex = 0xFF6CABDD,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("mci_1", "Joe Hart", PlayerPosition.GK, 25, "England", 85, 58, 20, 70, 45, 87, 80, 22_000_000L, 95_000L, isStarting = true),
            createPlayer("mci_2", "Pablo Zabaleta", PlayerPosition.RB, 27, "Argentina", 82, 78, 55, 75, 74, 83, 82, 14_000_000L, 75_000L, isStarting = true),
            createPlayer("mci_3", "Vincent Kompany", PlayerPosition.CB, 26, "Belgium", 88, 74, 52, 72, 65, 90, 88, 35_000_000L, 130_000L, isStarting = true),
            createPlayer("mci_4", "Joleon Lescott", PlayerPosition.CB, 30, "England", 81, 68, 42, 62, 55, 83, 84, 10_000_000L, 75_000L, isStarting = true),
            createPlayer("mci_5", "Gael Clichy", PlayerPosition.LB, 27, "France", 81, 86, 52, 74, 78, 80, 75, 12_000_000L, 75_000L, isStarting = true),
            createPlayer("mci_6", "Yaya Toure", PlayerPosition.CM, 29, "Ivory Coast", 87, 78, 84, 86, 84, 82, 92, 36_000_000L, 160_000L, isStarting = true),
            createPlayer("mci_7", "Gareth Barry", PlayerPosition.CDM, 31, "England", 81, 58, 68, 80, 74, 82, 79, 9_000_000L, 75_000L, isStarting = true),
            createPlayer("mci_8", "James Milner", PlayerPosition.RM, 26, "England", 81, 77, 74, 81, 78, 76, 83, 14_000_000L, 75_000L, isStarting = true),
            createPlayer("mci_9", "David Silva", PlayerPosition.CAM, 26, "Spain", 88, 76, 78, 91, 90, 52, 60, 45_000_000L, 145_000L, isStarting = true),
            createPlayer("mci_10", "Samir Nasri", PlayerPosition.LM, 25, "France", 84, 80, 77, 84, 86, 50, 65, 24_000_000L, 110_000L, isStarting = true),
            createPlayer("mci_11", "Sergio Aguero", PlayerPosition.ST, 24, "Argentina", 88, 88, 89, 78, 90, 36, 75, 55_000_000L, 170_000L, isStarting = true),
            // Bench
            createPlayer("mci_12", "Costel Pantilimon", PlayerPosition.GK, 25, "Romania", 75, 40, 18, 60, 35, 77, 85, 3_000_000L, 30_000L, isSub = true),
            createPlayer("mci_13", "Matija Nastasic", PlayerPosition.CB, 19, "Serbia", 79, 70, 40, 65, 58, 82, 78, 12_000_000L, 45_000L, isSub = true),
            createPlayer("mci_14", "Aleksandar Kolarov", PlayerPosition.LB, 27, "Serbia", 80, 78, 76, 82, 76, 77, 82, 11_000_000L, 70_000L, isSub = true),
            createPlayer("mci_15", "Javi Garcia", PlayerPosition.CDM, 25, "Spain", 79, 65, 68, 75, 72, 81, 84, 10_000_000L, 60_000L, isSub = true),
            createPlayer("mci_16", "Jack Rodwell", PlayerPosition.CM, 21, "England", 77, 72, 68, 75, 74, 76, 78, 8_000_000L, 45_000L, isSub = true),
            createPlayer("mci_17", "Carlos Tevez", PlayerPosition.ST, 28, "Argentina", 86, 82, 87, 78, 86, 55, 83, 30_000_000L, 150_000L, isSub = true),
            createPlayer("mci_18", "Edin Dzeko", PlayerPosition.ST, 26, "Bosnia", 83, 74, 85, 68, 75, 40, 86, 20_000_000L, 100_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "mci_3"
        team.penaltyTakerId = "mci_11"
        team.freeKickTakerId = "mci_14"
        team.cornerTakerId = "mci_9"
        return team
    }

    private fun createArsenal(): Team {
        val team = Team(
            id = "arsenal",
            name = "Arsenal",
            shortName = "ARS",
            leagueId = "epl",
            reputationStars = 4,
            balanceEuro = 65_000_000L,
            transferBudgetEuro = 40_000_000L,
            wageBudgetEuroWeekly = 1_600_000L,
            stadiumName = "Emirates Stadium",
            stadiumCapacity = 60_260,
            primaryColorHex = 0xFFEF0107,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("ars_1", "Wojciech Szczesny", PlayerPosition.GK, 22, "Poland", 81, 52, 20, 68, 40, 83, 77, 12_000_000L, 60_000L, isStarting = true),
            createPlayer("ars_2", "Bacary Sagna", PlayerPosition.RB, 29, "France", 82, 80, 55, 75, 76, 83, 80, 12_000_000L, 75_000L, isStarting = true),
            createPlayer("ars_3", "Per Mertesacker", PlayerPosition.CB, 28, "Germany", 81, 38, 42, 68, 52, 85, 84, 10_000_000L, 70_000L, isStarting = true),
            createPlayer("ars_4", "Thomas Vermaelen", PlayerPosition.CB, 27, "Belgium", 83, 72, 65, 70, 65, 84, 82, 16_000_000L, 80_000L, isStarting = true),
            createPlayer("ars_5", "Kieran Gibbs", PlayerPosition.LB, 23, "England", 78, 85, 55, 72, 77, 77, 72, 9_000_000L, 50_000L, isStarting = true),
            createPlayer("ars_6", "Mikel Arteta", PlayerPosition.CM, 30, "Spain", 82, 62, 76, 87, 82, 79, 74, 12_000_000L, 85_000L, isStarting = true),
            createPlayer("ars_7", "Jack Wilshere", PlayerPosition.CM, 20, "England", 82, 78, 74, 84, 86, 72, 74, 24_000_000L, 70_000L, isStarting = true),
            createPlayer("ars_8", "Theo Walcott", PlayerPosition.RM, 23, "England", 82, 96, 78, 74, 83, 40, 68, 22_000_000L, 80_000L, isStarting = true),
            createPlayer("ars_9", "Santi Cazorla", PlayerPosition.CAM, 28, "Spain", 85, 78, 82, 88, 89, 58, 62, 28_000_000L, 95_000L, isStarting = true),
            createPlayer("ars_10", "Lukas Podolski", PlayerPosition.LM, 27, "Germany", 82, 84, 88, 75, 80, 45, 79, 18_000_000L, 85_000L, isStarting = true),
            createPlayer("ars_11", "Olivier Giroud", PlayerPosition.ST, 26, "France", 80, 72, 81, 68, 73, 45, 86, 14_000_000L, 70_000L, isStarting = true),
            // Bench
            createPlayer("ars_12", "Lukasz Fabianski", PlayerPosition.GK, 27, "Poland", 76, 45, 18, 62, 38, 78, 74, 3_500_000L, 35_000L, isSub = true),
            createPlayer("ars_13", "Laurent Koscielny", PlayerPosition.CB, 27, "France", 82, 77, 45, 68, 62, 84, 80, 15_000_000L, 75_000L, isSub = true),
            createPlayer("ars_14", "Nacho Monreal", PlayerPosition.LB, 26, "Spain", 79, 78, 55, 75, 75, 78, 74, 9_000_000L, 55_000L, isSub = true),
            createPlayer("ars_15", "Aaron Ramsey", PlayerPosition.CM, 22, "Wales", 78, 74, 72, 79, 78, 72, 75, 10_000_000L, 50_000L, isSub = true),
            createPlayer("ars_16", "Tomas Rosicky", PlayerPosition.CAM, 32, "Czech Rep", 79, 74, 74, 82, 83, 55, 60, 6_000_000L, 60_000L, isSub = true),
            createPlayer("ars_17", "Alex Oxlade-Chamberlain", PlayerPosition.RW, 19, "England", 77, 88, 72, 73, 82, 50, 72, 12_000_000L, 40_000L, isSub = true),
            createPlayer("ars_18", "Gervinho", PlayerPosition.LW, 25, "Ivory Coast", 79, 89, 70, 72, 85, 38, 68, 10_000_000L, 55_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "ars_4"
        team.penaltyTakerId = "ars_6"
        team.freeKickTakerId = "ars_9"
        team.cornerTakerId = "ars_9"
        return team
    }

    private fun createLiverpool(): Team {
        val team = Team(
            id = "liverpool",
            name = "Liverpool",
            shortName = "LIV",
            leagueId = "epl",
            reputationStars = 4,
            balanceEuro = 55_000_000L,
            transferBudgetEuro = 35_000_000L,
            wageBudgetEuroWeekly = 1_500_000L,
            stadiumName = "Anfield",
            stadiumCapacity = 45_276,
            primaryColorHex = 0xFFC8102E,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_433
        )
        val players = listOf(
            createPlayer("liv_1", "Pepe Reina", PlayerPosition.GK, 30, "Spain", 83, 50, 20, 75, 45, 84, 76, 14_000_000L, 85_000L, isStarting = true),
            createPlayer("liv_2", "Glen Johnson", PlayerPosition.RB, 28, "England", 80, 82, 65, 75, 79, 78, 78, 10_000_000L, 70_000L, isStarting = true),
            createPlayer("liv_3", "Daniel Agger", PlayerPosition.CB, 28, "Denmark", 83, 68, 64, 76, 68, 85, 80, 16_000_000L, 80_000L, isStarting = true),
            createPlayer("liv_4", "Martin Skrtel", PlayerPosition.CB, 28, "Slovakia", 81, 66, 42, 62, 54, 84, 88, 12_000_000L, 75_000L, isStarting = true),
            createPlayer("liv_5", "Jose Enrique", PlayerPosition.LB, 26, "Spain", 79, 82, 58, 74, 77, 78, 82, 9_500_000L, 60_000L, isStarting = true),
            createPlayer("liv_6", "Lucas Leiva", PlayerPosition.CDM, 25, "Brazil", 81, 65, 58, 79, 74, 84, 80, 13_000_000L, 70_000L, isStarting = true),
            createPlayer("liv_7", "Steven Gerrard", PlayerPosition.CM, 32, "England", 86, 72, 88, 90, 82, 79, 84, 22_000_000L, 140_000L, isStarting = true),
            createPlayer("liv_8", "Jordan Henderson", PlayerPosition.CM, 22, "England", 78, 76, 70, 78, 76, 72, 80, 10_000_000L, 50_000L, isStarting = true),
            createPlayer("liv_9", "Raheem Sterling", PlayerPosition.RW, 18, "England", 77, 92, 68, 72, 85, 38, 58, 15_000_000L, 35_000L, isStarting = true),
            createPlayer("liv_10", "Luis Suarez", PlayerPosition.ST, 25, "Uruguay", 88, 84, 88, 80, 90, 52, 82, 52_000_000L, 150_000L, isStarting = true),
            createPlayer("liv_11", "Philippe Coutinho", PlayerPosition.LW, 20, "Brazil", 80, 80, 74, 83, 88, 45, 56, 18_000_000L, 55_000L, isStarting = true),
            // Bench
            createPlayer("liv_12", "Brad Jones", PlayerPosition.GK, 30, "Australia", 72, 40, 15, 58, 30, 73, 72, 1_500_000L, 25_000L, isSub = true),
            createPlayer("liv_13", "Jamie Carragher", PlayerPosition.CB, 34, "England", 79, 45, 40, 62, 50, 84, 80, 4_000_000L, 70_000L, isSub = true),
            createPlayer("liv_14", "Sebastian Coates", PlayerPosition.CB, 22, "Uruguay", 75, 55, 38, 58, 50, 78, 84, 5_000_000L, 35_000L, isSub = true),
            createPlayer("liv_15", "Joe Allen", PlayerPosition.CM, 22, "Wales", 78, 70, 65, 82, 78, 72, 68, 9_000_000L, 45_000L, isSub = true),
            createPlayer("liv_16", "Stewart Downing", PlayerPosition.LM, 28, "England", 77, 80, 72, 78, 78, 52, 70, 7_000_000L, 55_000L, isSub = true),
            createPlayer("liv_17", "Daniel Sturridge", PlayerPosition.ST, 23, "England", 81, 88, 82, 72, 84, 35, 72, 16_000_000L, 65_000L, isSub = true),
            createPlayer("liv_18", "Fabio Borini", PlayerPosition.ST, 21, "Italy", 76, 80, 75, 68, 76, 40, 70, 7_500_000L, 40_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "liv_7"
        team.penaltyTakerId = "liv_7"
        team.freeKickTakerId = "liv_7"
        team.cornerTakerId = "liv_7"
        return team
    }

    private fun createTottenham(): Team {
        val team = Team(
            id = "tottenham",
            name = "Tottenham Hotspur",
            shortName = "TOT",
            leagueId = "epl",
            reputationStars = 4,
            balanceEuro = 50_000_000L,
            transferBudgetEuro = 30_000_000L,
            wageBudgetEuroWeekly = 1_400_000L,
            stadiumName = "White Hart Lane",
            stadiumCapacity = 36_284,
            primaryColorHex = 0xFF132257,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("tot_1", "Hugo Lloris", PlayerPosition.GK, 26, "France", 86, 62, 20, 72, 45, 88, 78, 22_000_000L, 85_000L, isStarting = true),
            createPlayer("tot_2", "Kyle Walker", PlayerPosition.RB, 22, "England", 81, 92, 60, 74, 78, 79, 82, 16_000_000L, 60_000L, isStarting = true),
            createPlayer("tot_3", "Jan Vertonghen", PlayerPosition.CB, 25, "Belgium", 83, 72, 68, 76, 72, 85, 82, 18_000_000L, 75_000L, isStarting = true),
            createPlayer("tot_4", "Michael Dawson", PlayerPosition.CB, 29, "England", 79, 58, 45, 62, 52, 82, 84, 8_000_000L, 60_000L, isStarting = true),
            createPlayer("tot_5", "Benoit Assou-Ekotto", PlayerPosition.LB, 28, "Cameroon", 78, 79, 60, 74, 76, 77, 76, 7_500_000L, 55_000L, isStarting = true),
            createPlayer("tot_6", "Sandro", PlayerPosition.CDM, 23, "Brazil", 81, 68, 60, 74, 70, 84, 87, 12_000_000L, 60_000L, isStarting = true),
            createPlayer("tot_7", "Mousa Dembele", PlayerPosition.CM, 25, "Belgium", 83, 78, 72, 82, 88, 79, 86, 18_000_000L, 75_000L, isStarting = true),
            createPlayer("tot_8", "Aaron Lennon", PlayerPosition.RM, 25, "England", 80, 93, 68, 74, 84, 45, 62, 13_000_000L, 65_000L, isStarting = true),
            createPlayer("tot_9", "Gareth Bale", PlayerPosition.LW, 23, "Wales", 89, 94, 88, 83, 89, 68, 82, 65_000_000L, 160_000L, isStarting = true),
            createPlayer("tot_10", "Clint Dempsey", PlayerPosition.CAM, 29, "USA", 81, 74, 82, 76, 78, 55, 78, 12_000_000L, 70_000L, isStarting = true),
            createPlayer("tot_11", "Jermain Defoe", PlayerPosition.ST, 30, "England", 81, 84, 84, 68, 80, 36, 68, 12_000_000L, 75_000L, isStarting = true),
            // Bench
            createPlayer("tot_12", "Brad Friedel", PlayerPosition.GK, 41, "USA", 77, 35, 18, 62, 30, 80, 78, 2_000_000L, 40_000L, isSub = true),
            createPlayer("tot_13", "William Gallas", PlayerPosition.CB, 35, "France", 78, 62, 45, 64, 55, 81, 77, 3_000_000L, 50_000L, isSub = true),
            createPlayer("tot_14", "Kyle Naughton", PlayerPosition.RB, 24, "England", 75, 80, 52, 68, 72, 74, 74, 5_000_000L, 35_000L, isSub = true),
            createPlayer("tot_15", "Scott Parker", PlayerPosition.CM, 32, "England", 80, 65, 62, 78, 72, 82, 80, 7_000_000L, 65_000L, isSub = true),
            createPlayer("tot_16", "Gylfi Sigurdsson", PlayerPosition.CAM, 23, "Iceland", 79, 72, 80, 82, 78, 56, 72, 11_000_000L, 50_000L, isSub = true),
            createPlayer("tot_17", "Lewis Holtby", PlayerPosition.CM, 22, "Germany", 78, 75, 72, 80, 80, 60, 68, 9_000_000L, 45_000L, isSub = true),
            createPlayer("tot_18", "Emmanuel Adebayor", PlayerPosition.ST, 28, "Togo", 80, 78, 80, 70, 76, 42, 84, 11_000_000L, 80_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "tot_4"
        team.penaltyTakerId = "tot_9"
        team.freeKickTakerId = "tot_9"
        team.cornerTakerId = "tot_9"
        return team
    }

    private fun createAtleticoMadrid(): Team {
        val team = Team(
            id = "atletico",
            name = "Atletico Madrid",
            shortName = "ATM",
            leagueId = "laliga",
            reputationStars = 4,
            balanceEuro = 55_000_000L,
            transferBudgetEuro = 35_000_000L,
            wageBudgetEuroWeekly = 1_500_000L,
            stadiumName = "Vicente Calderon",
            stadiumCapacity = 54_907,
            primaryColorHex = 0xFFCB3524,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_442
        )
        val players = listOf(
            createPlayer("atm_1", "Thibaut Courtois", PlayerPosition.GK, 20, "Belgium", 84, 52, 20, 70, 42, 86, 82, 22_000_000L, 65_000L, isStarting = true),
            createPlayer("atm_2", "Juanfran", PlayerPosition.RB, 27, "Spain", 80, 82, 60, 75, 77, 79, 78, 10_000_000L, 60_000L, isStarting = true),
            createPlayer("atm_3", "Diego Godin", PlayerPosition.CB, 26, "Uruguay", 84, 68, 50, 66, 62, 88, 86, 18_000_000L, 80_000L, isStarting = true),
            createPlayer("atm_4", "Miranda", PlayerPosition.CB, 28, "Brazil", 82, 70, 45, 65, 60, 85, 83, 13_000_000L, 70_000L, isStarting = true),
            createPlayer("atm_5", "Filipe Luis", PlayerPosition.LB, 27, "Brazil", 82, 80, 62, 76, 80, 82, 78, 14_000_000L, 70_000L, isStarting = true),
            createPlayer("atm_6", "Arda Turan", PlayerPosition.RM, 25, "Turkey", 83, 78, 76, 83, 86, 62, 74, 20_000_000L, 75_000L, isStarting = true),
            createPlayer("atm_7", "Gabi", PlayerPosition.CM, 29, "Spain", 81, 68, 72, 82, 76, 82, 80, 11_000_000L, 65_000L, isStarting = true),
            createPlayer("atm_8", "Mario Suarez", PlayerPosition.CM, 25, "Spain", 80, 68, 64, 78, 74, 82, 82, 10_000_000L, 55_000L, isStarting = true),
            createPlayer("atm_9", "Koke", PlayerPosition.LM, 20, "Spain", 81, 74, 75, 84, 81, 72, 74, 18_000_000L, 50_000L, isStarting = true),
            createPlayer("atm_10", "Radamel Falcao", PlayerPosition.ST, 26, "Colombia", 89, 82, 92, 70, 82, 45, 85, 55_000_000L, 160_000L, isStarting = true),
            createPlayer("atm_11", "Diego Costa", PlayerPosition.ST, 24, "Spain", 82, 82, 82, 68, 78, 55, 89, 18_000_000L, 65_000L, isStarting = true),
            // Bench
            createPlayer("atm_12", "Sergio Asenjo", PlayerPosition.GK, 23, "Spain", 76, 50, 18, 65, 40, 78, 74, 4_500_000L, 35_000L, isSub = true),
            createPlayer("atm_13", "Cata Diaz", PlayerPosition.CB, 33, "Argentina", 77, 55, 42, 58, 50, 80, 84, 3_000_000L, 40_000L, isSub = true),
            createPlayer("atm_14", "Tiago", PlayerPosition.CDM, 31, "Portugal", 79, 62, 68, 80, 75, 80, 77, 6_000_000L, 50_000L, isSub = true),
            createPlayer("atm_15", "Cristian Rodriguez", PlayerPosition.LM, 27, "Uruguay", 78, 82, 72, 74, 80, 52, 76, 7_500_000L, 45_000L, isSub = true),
            createPlayer("atm_16", "Adrian Lopez", PlayerPosition.ST, 24, "Spain", 79, 83, 78, 72, 80, 40, 70, 9_500_000L, 45_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "atm_7"
        team.penaltyTakerId = "atm_10"
        team.freeKickTakerId = "atm_7"
        team.cornerTakerId = "atm_9"
        return team
    }

    private fun createValencia(): Team {
        val team = Team(
            id = "valencia",
            name = "Valencia CF",
            shortName = "VAL",
            leagueId = "laliga",
            reputationStars = 4,
            balanceEuro = 40_000_000L,
            transferBudgetEuro = 22_000_000L,
            wageBudgetEuroWeekly = 1_100_000L,
            stadiumName = "Mestalla",
            stadiumCapacity = 49_500,
            primaryColorHex = 0xFFFFFFFF,
            secondaryColorHex = 0xFF000000,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("val_1", "Diego Alves", PlayerPosition.GK, 27, "Brazil", 82, 55, 20, 70, 42, 84, 76, 12_000_000L, 55_000L, isStarting = true),
            createPlayer("val_2", "Joao Pereira", PlayerPosition.RB, 28, "Portugal", 79, 82, 52, 72, 75, 78, 77, 7_500_000L, 45_000L, isStarting = true),
            createPlayer("val_3", "Adil Rami", PlayerPosition.CB, 27, "France", 82, 68, 50, 65, 58, 85, 87, 13_000_000L, 65_000L, isStarting = true),
            createPlayer("val_4", "Ricardo Costa", PlayerPosition.CB, 31, "Portugal", 78, 62, 42, 62, 52, 80, 82, 5_000_000L, 45_000L, isStarting = true),
            createPlayer("val_5", "Jeremy Mathieu", PlayerPosition.LB, 29, "France", 80, 80, 68, 74, 75, 80, 84, 9_000_000L, 55_000L, isStarting = true),
            createPlayer("val_6", "Tino Costa", PlayerPosition.CM, 27, "Argentina", 80, 72, 78, 83, 78, 75, 78, 10_000_000L, 55_000L, isStarting = true),
            createPlayer("val_7", "Ever Banega", PlayerPosition.CM, 24, "Argentina", 82, 74, 72, 86, 85, 76, 75, 16_000_000L, 65_000L, isStarting = true),
            createPlayer("val_8", "Sofiane Feghouli", PlayerPosition.RM, 23, "Algeria", 80, 85, 74, 76, 83, 50, 72, 12_000_000L, 50_000L, isStarting = true),
            createPlayer("val_9", "Jonas", PlayerPosition.CAM, 28, "Brazil", 81, 75, 82, 78, 81, 45, 72, 12_500_000L, 60_000L, isStarting = true),
            createPlayer("val_10", "Andres Guardado", PlayerPosition.LM, 26, "Mexico", 79, 82, 72, 78, 80, 65, 72, 10_000_000L, 50_000L, isStarting = true),
            createPlayer("val_11", "Roberto Soldado", PlayerPosition.ST, 27, "Spain", 84, 82, 87, 72, 80, 42, 78, 24_000_000L, 85_000L, isStarting = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "val_11"
        team.penaltyTakerId = "val_11"
        team.freeKickTakerId = "val_6"
        team.cornerTakerId = "val_7"
        return team
    }

    private fun createJuventus(): Team {
        val team = Team(
            id = "juventus",
            name = "Juventus",
            shortName = "JUV",
            leagueId = "seriea",
            reputationStars = 5,
            balanceEuro = 80_000_000L,
            transferBudgetEuro = 50_000_000L,
            wageBudgetEuroWeekly = 2_100_000L,
            stadiumName = "Juventus Stadium",
            stadiumCapacity = 41_507,
            primaryColorHex = 0xFF000000,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_352
        )
        val players = listOf(
            createPlayer("juv_1", "Gianluigi Buffon", PlayerPosition.GK, 34, "Italy", 89, 52, 20, 72, 45, 91, 82, 22_000_000L, 130_000L, isStarting = true),
            createPlayer("juv_2", "Andrea Barzagli", PlayerPosition.CB, 31, "Italy", 85, 68, 40, 68, 60, 89, 84, 16_000_000L, 95_000L, isStarting = true),
            createPlayer("juv_3", "Leonardo Bonucci", PlayerPosition.CB, 25, "Italy", 82, 68, 52, 79, 68, 84, 83, 17_000_000L, 80_000L, isStarting = true),
            createPlayer("juv_4", "Giorgio Chiellini", PlayerPosition.CB, 28, "Italy", 87, 75, 48, 66, 60, 92, 90, 28_000_000L, 120_000L, isStarting = true),
            createPlayer("juv_5", "Stephan Lichtsteiner", PlayerPosition.RM, 28, "Switzerland", 81, 84, 62, 75, 77, 81, 84, 12_000_000L, 70_000L, isStarting = true),
            createPlayer("juv_6", "Arturo Vidal", PlayerPosition.CM, 25, "Chile", 86, 78, 80, 82, 82, 86, 88, 36_000_000L, 120_000L, isStarting = true),
            createPlayer("juv_7", "Andrea Pirlo", PlayerPosition.CDM, 33, "Italy", 89, 55, 82, 95, 86, 74, 68, 22_000_000L, 140_000L, isStarting = true),
            createPlayer("juv_8", "Claudio Marchisio", PlayerPosition.CM, 26, "Italy", 85, 79, 78, 84, 83, 80, 82, 30_000_000L, 110_000L, isStarting = true),
            createPlayer("juv_9", "Kwadwo Asamoah", PlayerPosition.LM, 24, "Ghana", 80, 83, 72, 78, 82, 77, 83, 14_000_000L, 65_000L, isStarting = true),
            createPlayer("juv_10", "Mirko Vucinic", PlayerPosition.ST, 29, "Montenegro", 83, 78, 82, 80, 85, 42, 74, 16_000_000L, 90_000L, isStarting = true),
            createPlayer("juv_11", "Fabio Quagliarella", PlayerPosition.ST, 29, "Italy", 81, 76, 84, 74, 80, 40, 75, 12_000_000L, 80_000L, isStarting = true),
            // Bench
            createPlayer("juv_12", "Marco Storari", PlayerPosition.GK, 35, "Italy", 77, 45, 18, 62, 35, 79, 75, 2_000_000L, 35_000L, isSub = true),
            createPlayer("juv_13", "Martin Caceres", PlayerPosition.CB, 25, "Uruguay", 79, 82, 55, 70, 72, 80, 80, 9_500_000L, 50_000L, isSub = true),
            createPlayer("juv_14", "Paul Pogba", PlayerPosition.CM, 19, "France", 79, 76, 78, 80, 82, 74, 86, 18_000_000L, 45_000L, isSub = true),
            createPlayer("juv_15", "Emanuele Giaccherini", PlayerPosition.LM, 27, "Italy", 77, 84, 70, 75, 80, 58, 66, 7_000_000L, 45_000L, isSub = true),
            createPlayer("juv_16", "Alessandro Matri", PlayerPosition.ST, 28, "Italy", 80, 80, 81, 65, 74, 38, 78, 10_000_000L, 65_000L, isSub = true),
            createPlayer("juv_17", "Sebastian Giovinco", PlayerPosition.ST, 25, "Italy", 80, 88, 76, 78, 88, 32, 52, 13_000_000L, 60_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "juv_1"
        team.penaltyTakerId = "juv_6"
        team.freeKickTakerId = "juv_7"
        team.cornerTakerId = "juv_7"
        return team
    }

    private fun createMilan(): Team {
        val team = Team(
            id = "milan",
            name = "AC Milan",
            shortName = "MIL",
            leagueId = "seriea",
            reputationStars = 4,
            balanceEuro = 50_000_000L,
            transferBudgetEuro = 30_000_000L,
            wageBudgetEuroWeekly = 1_600_000L,
            stadiumName = "San Siro",
            stadiumCapacity = 80_018,
            primaryColorHex = 0xFFFB090B,
            secondaryColorHex = 0xFF000000,
            formation = Formation.F_433
        )
        val players = listOf(
            createPlayer("mil_1", "Christian Abbiati", PlayerPosition.GK, 35, "Italy", 81, 45, 18, 65, 38, 83, 76, 4_000_000L, 60_000L, isStarting = true),
            createPlayer("mil_2", "Ignazio Abate", PlayerPosition.RB, 26, "Italy", 80, 93, 52, 72, 76, 78, 79, 11_000_000L, 55_000L, isStarting = true),
            createPlayer("mil_3", "Philippe Mexes", PlayerPosition.CB, 30, "France", 81, 65, 54, 68, 60, 83, 84, 9_000_000L, 70_000L, isStarting = true),
            createPlayer("mil_4", "Cristian Zapata", PlayerPosition.CB, 26, "Colombia", 79, 78, 40, 62, 58, 80, 83, 8_500_000L, 50_000L, isStarting = true),
            createPlayer("mil_5", "Kevin Constant", PlayerPosition.LB, 25, "Guinea", 77, 80, 65, 74, 76, 75, 78, 6_500_000L, 45_000L, isStarting = true),
            createPlayer("mil_6", "Riccardo Montolivo", PlayerPosition.CM, 27, "Italy", 83, 70, 76, 87, 82, 76, 74, 18_000_000L, 85_000L, isStarting = true),
            createPlayer("mil_7", "Massimo Ambrosini", PlayerPosition.CDM, 35, "Italy", 79, 55, 62, 75, 70, 82, 82, 3_000_000L, 60_000L, isStarting = true),
            createPlayer("mil_8", "Kevin-Prince Boateng", PlayerPosition.CAM, 25, "Ghana", 83, 81, 80, 78, 84, 72, 85, 20_000_000L, 85_000L, isStarting = true),
            createPlayer("mil_9", "Robinho", PlayerPosition.RW, 28, "Brazil", 81, 84, 75, 79, 89, 38, 62, 14_000_000L, 90_000L, isStarting = true),
            createPlayer("mil_10", "Mario Balotelli", PlayerPosition.ST, 22, "Italy", 84, 83, 86, 75, 83, 40, 86, 28_000_000L, 110_000L, isStarting = true),
            createPlayer("mil_11", "Stephan El Shaarawy", PlayerPosition.LW, 20, "Italy", 83, 88, 82, 76, 86, 42, 68, 25_000_000L, 75_000L, isStarting = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "mil_7"
        team.penaltyTakerId = "mil_10"
        team.freeKickTakerId = "mil_10"
        team.cornerTakerId = "mil_6"
        return team
    }

    private fun createInter(): Team {
        val team = Team(
            id = "inter",
            name = "Inter Milan",
            shortName = "INT",
            leagueId = "seriea",
            reputationStars = 4,
            balanceEuro = 55_000_000L,
            transferBudgetEuro = 32_000_000L,
            wageBudgetEuroWeekly = 1_700_000L,
            stadiumName = "Giuseppe Meazza",
            stadiumCapacity = 80_018,
            primaryColorHex = 0xFF001EA0,
            secondaryColorHex = 0xFF000000,
            formation = Formation.F_433
        )
        val players = listOf(
            createPlayer("int_1", "Samir Handanovic", PlayerPosition.GK, 28, "Slovenia", 85, 52, 20, 70, 42, 87, 80, 20_000_000L, 80_000L, isStarting = true),
            createPlayer("int_2", "Javier Zanetti", PlayerPosition.RB, 39, "Argentina", 82, 74, 58, 78, 76, 84, 82, 4_000_000L, 85_000L, isStarting = true),
            createPlayer("int_3", "Andrea Ranocchia", PlayerPosition.CB, 24, "Italy", 79, 64, 45, 66, 58, 82, 84, 10_000_000L, 55_000L, isStarting = true),
            createPlayer("int_4", "Walter Samuel", PlayerPosition.CB, 34, "Argentina", 83, 55, 48, 64, 54, 88, 87, 7_000_000L, 80_000L, isStarting = true),
            createPlayer("int_5", "Yuto Nagatomo", PlayerPosition.LB, 26, "Japan", 79, 87, 60, 74, 78, 76, 75, 9_500_000L, 50_000L, isStarting = true),
            createPlayer("int_6", "Esteban Cambiasso", PlayerPosition.CDM, 32, "Argentina", 84, 65, 74, 84, 78, 86, 82, 14_000_000L, 100_000L, isStarting = true),
            createPlayer("int_7", "Fredy Guarin", PlayerPosition.CM, 26, "Colombia", 82, 78, 84, 80, 80, 77, 88, 17_000_000L, 75_000L, isStarting = true),
            createPlayer("int_8", "Mateo Kovacic", PlayerPosition.CM, 18, "Croatia", 78, 80, 68, 82, 85, 60, 68, 14_000_000L, 40_000L, isStarting = true),
            createPlayer("int_9", "Rodrigo Palacio", PlayerPosition.RW, 30, "Argentina", 83, 84, 82, 76, 83, 48, 74, 15_000_000L, 80_000L, isStarting = true),
            createPlayer("int_10", "Diego Milito", PlayerPosition.ST, 33, "Argentina", 84, 74, 88, 72, 82, 40, 77, 12_000_000L, 110_000L, isStarting = true),
            createPlayer("int_11", "Antonio Cassano", PlayerPosition.LW, 30, "Italy", 83, 70, 82, 86, 88, 38, 72, 14_000_000L, 95_000L, isStarting = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "int_2"
        team.penaltyTakerId = "int_10"
        team.freeKickTakerId = "int_11"
        team.cornerTakerId = "int_11"
        return team
    }

    private fun createNapoli(): Team {
        val team = Team(
            id = "napoli",
            name = "SSC Napoli",
            shortName = "NAP",
            leagueId = "seriea",
            reputationStars = 4,
            balanceEuro = 60_000_000L,
            transferBudgetEuro = 38_000_000L,
            wageBudgetEuroWeekly = 1_400_000L,
            stadiumName = "Stadio San Paolo",
            stadiumCapacity = 60_240,
            primaryColorHex = 0xFF12A0D7,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_352
        )
        val players = listOf(
            createPlayer("nap_1", "Morgan De Sanctis", PlayerPosition.GK, 35, "Italy", 81, 48, 20, 65, 38, 83, 76, 4_000_000L, 50_000L, isStarting = true),
            createPlayer("nap_2", "Hugo Campagnaro", PlayerPosition.CB, 32, "Argentina", 81, 72, 45, 68, 62, 83, 84, 7_000_000L, 55_000L, isStarting = true),
            createPlayer("nap_3", "Paolo Cannavaro", PlayerPosition.CB, 31, "Italy", 80, 65, 45, 65, 58, 82, 82, 6_000_000L, 55_000L, isStarting = true),
            createPlayer("nap_4", "Miguel Britos", PlayerPosition.CB, 27, "Uruguay", 78, 64, 40, 62, 52, 80, 83, 6_500_000L, 45_000L, isStarting = true),
            createPlayer("nap_5", "Christian Maggio", PlayerPosition.RM, 30, "Italy", 82, 86, 68, 76, 78, 78, 82, 12_000_000L, 70_000L, isStarting = true),
            createPlayer("nap_6", "Gokhan Inler", PlayerPosition.CM, 28, "Switzerland", 81, 68, 80, 83, 77, 79, 82, 13_000_000L, 65_000L, isStarting = true),
            createPlayer("nap_7", "Valon Behrami", PlayerPosition.CDM, 27, "Switzerland", 80, 78, 60, 74, 74, 82, 84, 10_000_000L, 55_000L, isStarting = true),
            createPlayer("nap_8", "Marek Hamsik", PlayerPosition.CAM, 25, "Slovakia", 86, 80, 84, 86, 85, 68, 77, 36_000_000L, 110_000L, isStarting = true),
            createPlayer("nap_9", "Juan Zuniga", PlayerPosition.LM, 27, "Colombia", 79, 84, 62, 75, 80, 75, 76, 9_000_000L, 50_000L, isStarting = true),
            createPlayer("nap_10", "Goran Pandev", PlayerPosition.ST, 29, "Macedonia", 80, 75, 80, 78, 82, 45, 72, 9_500_000L, 60_000L, isStarting = true),
            createPlayer("nap_11", "Edinson Cavani", PlayerPosition.ST, 25, "Uruguay", 88, 84, 90, 72, 82, 55, 86, 55_000_000L, 150_000L, isStarting = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "nap_3"
        team.penaltyTakerId = "nap_11"
        team.freeKickTakerId = "nap_8"
        team.cornerTakerId = "nap_8"
        return team
    }

    private fun createBayern(): Team {
        val team = Team(
            id = "bayern",
            name = "Bayern Munich",
            shortName = "BAY",
            leagueId = "bundesliga",
            reputationStars = 5,
            balanceEuro = 115_000_000L,
            transferBudgetEuro = 70_000_000L,
            wageBudgetEuroWeekly = 2_700_000L,
            stadiumName = "Allianz Arena",
            stadiumCapacity = 75_024,
            primaryColorHex = 0xFFDC052D,
            secondaryColorHex = 0xFFFFFFFF,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("bay_1", "Manuel Neuer", PlayerPosition.GK, 26, "Germany", 89, 60, 25, 78, 55, 90, 84, 32_000_000L, 130_000L, isStarting = true),
            createPlayer("bay_2", "Philipp Lahm", PlayerPosition.RB, 29, "Germany", 87, 82, 64, 85, 84, 88, 72, 28_000_000L, 130_000L, isStarting = true),
            createPlayer("bay_3", "Jerome Boateng", PlayerPosition.CB, 24, "Germany", 83, 78, 48, 72, 65, 85, 86, 20_000_000L, 85_000L, isStarting = true),
            createPlayer("bay_4", "Dante", PlayerPosition.CB, 29, "Brazil", 82, 65, 50, 70, 62, 84, 85, 14_000_000L, 80_000L, isStarting = true),
            createPlayer("bay_5", "David Alaba", PlayerPosition.LB, 20, "Austria", 82, 86, 74, 80, 82, 79, 78, 24_000_000L, 75_000L, isStarting = true),
            createPlayer("bay_6", "Javi Martinez", PlayerPosition.CDM, 24, "Spain", 84, 68, 65, 78, 74, 86, 87, 26_000_000L, 100_000L, isStarting = true),
            createPlayer("bay_7", "Bastian Schweinsteiger", PlayerPosition.CM, 28, "Germany", 87, 72, 82, 89, 83, 83, 84, 38_000_000L, 140_000L, isStarting = true),
            createPlayer("bay_8", "Arjen Robben", PlayerPosition.RW, 28, "Netherlands", 88, 92, 86, 82, 92, 38, 68, 45_000_000L, 150_000L, isStarting = true),
            createPlayer("bay_9", "Thomas Muller", PlayerPosition.CAM, 23, "Germany", 85, 80, 84, 82, 80, 58, 76, 35_000_000L, 110_000L, isStarting = true),
            createPlayer("bay_10", "Franck Ribery", PlayerPosition.LW, 29, "France", 90, 91, 82, 87, 93, 45, 68, 54_000_000L, 160_000L, isStarting = true),
            createPlayer("bay_11", "Mario Mandzukic", PlayerPosition.ST, 26, "Croatia", 83, 76, 84, 70, 76, 52, 87, 22_000_000L, 95_000L, isStarting = true),
            // Bench
            createPlayer("bay_12", "Tom Starke", PlayerPosition.GK, 31, "Germany", 76, 42, 18, 62, 35, 78, 78, 2_500_000L, 35_000L, isSub = true),
            createPlayer("bay_13", "Holger Badstuber", PlayerPosition.CB, 23, "Germany", 81, 62, 60, 76, 62, 83, 82, 14_000_000L, 65_000L, isSub = true),
            createPlayer("bay_14", "Rafinha", PlayerPosition.RB, 27, "Brazil", 78, 80, 58, 74, 77, 76, 72, 7_500_000L, 50_000L, isSub = true),
            createPlayer("bay_15", "Luiz Gustavo", PlayerPosition.CDM, 25, "Brazil", 81, 74, 62, 75, 75, 83, 85, 14_000_000L, 70_000L, isSub = true),
            createPlayer("bay_16", "Toni Kroos", PlayerPosition.CAM, 22, "Germany", 84, 68, 82, 89, 82, 65, 72, 28_000_000L, 85_000L, isSub = true),
            createPlayer("bay_17", "Xherdan Shaqiri", PlayerPosition.RW, 21, "Switzerland", 80, 86, 78, 78, 84, 45, 74, 16_000_000L, 55_000L, isSub = true),
            createPlayer("bay_18", "Mario Gomez", PlayerPosition.ST, 27, "Germany", 83, 75, 86, 65, 74, 38, 84, 22_000_000L, 110_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "bay_2"
        team.penaltyTakerId = "bay_7"
        team.freeKickTakerId = "bay_8"
        team.cornerTakerId = "bay_10"
        return team
    }

    private fun createDortmund(): Team {
        val team = Team(
            id = "dortmund",
            name = "Borussia Dortmund",
            shortName = "BVB",
            leagueId = "bundesliga",
            reputationStars = 5,
            balanceEuro = 75_000_000L,
            transferBudgetEuro = 45_000_000L,
            wageBudgetEuroWeekly = 1_700_000L,
            stadiumName = "Signal Iduna Park",
            stadiumCapacity = 81_365,
            primaryColorHex = 0xFFFDE100,
            secondaryColorHex = 0xFF000000,
            formation = Formation.F_4231
        )
        val players = listOf(
            createPlayer("bvb_1", "Roman Weidenfeller", PlayerPosition.GK, 32, "Germany", 84, 52, 20, 68, 42, 86, 80, 12_000_000L, 75_000L, isStarting = true),
            createPlayer("bvb_2", "Lukasz Piszczek", PlayerPosition.RB, 27, "Poland", 82, 82, 64, 76, 77, 81, 82, 14_000_000L, 70_000L, isStarting = true),
            createPlayer("bvb_3", "Neven Subotic", PlayerPosition.CB, 24, "Serbia", 82, 70, 42, 65, 58, 84, 85, 15_000_000L, 70_000L, isStarting = true),
            createPlayer("bvb_4", "Mats Hummels", PlayerPosition.CB, 24, "Germany", 86, 68, 62, 80, 72, 88, 84, 30_000_000L, 100_000L, isStarting = true),
            createPlayer("bvb_5", "Marcel Schmelzer", PlayerPosition.LB, 24, "Germany", 79, 82, 54, 74, 76, 78, 77, 9_500_000L, 55_000L, isStarting = true),
            createPlayer("bvb_6", "Sven Bender", PlayerPosition.CDM, 23, "Germany", 81, 68, 55, 75, 72, 85, 83, 14_000_000L, 60_000L, isStarting = true),
            createPlayer("bvb_7", "Ilkay Gundogan", PlayerPosition.CM, 22, "Germany", 84, 76, 75, 86, 85, 74, 72, 25_000_000L, 80_000L, isStarting = true),
            createPlayer("bvb_8", "Jakub Blaszczykowski", PlayerPosition.RM, 27, "Poland", 82, 89, 76, 78, 82, 60, 75, 15_000_000L, 70_000L, isStarting = true),
            createPlayer("bvb_9", "Mario Gotze", PlayerPosition.CAM, 20, "Germany", 86, 82, 78, 86, 90, 48, 65, 40_000_000L, 100_000L, isStarting = true),
            createPlayer("bvb_10", "Marco Reus", PlayerPosition.LM, 23, "Germany", 86, 90, 85, 82, 87, 45, 70, 42_000_000L, 110_000L, isStarting = true),
            createPlayer("bvb_11", "Robert Lewandowski", PlayerPosition.ST, 24, "Poland", 87, 82, 88, 75, 83, 48, 84, 45_000_000L, 120_000L, isStarting = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "bvb_4"
        team.penaltyTakerId = "bvb_11"
        team.freeKickTakerId = "bvb_10"
        team.cornerTakerId = "bvb_10"
        return team
    }

    private fun createPsg(): Team {
        val team = Team(
            id = "psg",
            name = "Paris Saint-Germain",
            shortName = "PSG",
            leagueId = "ligue1",
            reputationStars = 5,
            balanceEuro = 105_000_000L,
            transferBudgetEuro = 70_000_000L,
            wageBudgetEuroWeekly = 2_400_000L,
            stadiumName = "Parc des Princes",
            stadiumCapacity = 47_929,
            primaryColorHex = 0xFF001C38,
            secondaryColorHex = 0xFFDA291C,
            formation = Formation.F_433
        )
        val players = listOf(
            createPlayer("psg_1", "Salvatore Sirigu", PlayerPosition.GK, 25, "Italy", 84, 52, 20, 68, 42, 86, 80, 18_000_000L, 80_000L, isStarting = true),
            createPlayer("psg_2", "Christophe Jallet", PlayerPosition.RB, 29, "France", 78, 78, 58, 72, 74, 76, 76, 6_000_000L, 50_000L, isStarting = true),
            createPlayer("psg_3", "Thiago Silva", PlayerPosition.CB, 28, "Brazil", 89, 78, 55, 78, 72, 92, 85, 42_000_000L, 160_000L, isStarting = true),
            createPlayer("psg_4", "Alex", PlayerPosition.CB, 30, "Brazil", 81, 62, 68, 62, 55, 83, 88, 9_000_000L, 75_000L, isStarting = true),
            createPlayer("psg_5", "Maxwell", PlayerPosition.LB, 31, "Brazil", 79, 78, 62, 78, 80, 77, 72, 7_000_000L, 65_000L, isStarting = true),
            createPlayer("psg_6", "Thiago Motta", PlayerPosition.CDM, 30, "Italy", 82, 60, 74, 85, 78, 83, 82, 12_000_000L, 90_000L, isStarting = true),
            createPlayer("psg_7", "Blaise Matuidi", PlayerPosition.CM, 25, "France", 83, 82, 68, 78, 78, 85, 88, 22_000_000L, 85_000L, isStarting = true),
            createPlayer("psg_8", "Marco Verratti", PlayerPosition.CM, 20, "Italy", 80, 72, 65, 84, 85, 76, 72, 20_000_000L, 60_000L, isStarting = true),
            createPlayer("psg_9", "Ezequiel Lavezzi", PlayerPosition.LW, 27, "Argentina", 84, 89, 78, 78, 87, 45, 75, 26_000_000L, 110_000L, isStarting = true),
            createPlayer("psg_10", "Zlatan Ibrahimovic", PlayerPosition.ST, 31, "Sweden", 91, 78, 93, 84, 89, 45, 92, 60_000_000L, 220_000L, isStarting = true),
            createPlayer("psg_11", "Javier Pastore", PlayerPosition.CAM, 23, "Argentina", 83, 78, 78, 85, 86, 52, 68, 25_000_000L, 95_000L, isStarting = true),
            // Bench
            createPlayer("psg_12", "Nicolas Douchez", PlayerPosition.GK, 32, "France", 75, 42, 18, 62, 35, 76, 75, 2_000_000L, 35_000L, isSub = true),
            createPlayer("psg_13", "Mamadou Sakho", PlayerPosition.CB, 22, "France", 80, 72, 45, 66, 60, 82, 86, 12_000_000L, 60_000L, isSub = true),
            createPlayer("psg_14", "Gregory van der Wiel", PlayerPosition.RB, 24, "Netherlands", 78, 84, 58, 73, 76, 75, 74, 8_000_000L, 50_000L, isSub = true),
            createPlayer("psg_15", "Clément Chantôme", PlayerPosition.CM, 25, "France", 77, 72, 68, 77, 76, 74, 75, 6_000_000L, 45_000L, isSub = true),
            createPlayer("psg_16", "Lucas Moura", PlayerPosition.RW, 20, "Brazil", 82, 92, 74, 77, 88, 40, 68, 26_000_000L, 80_000L, isSub = true),
            createPlayer("psg_17", "David Beckham", PlayerPosition.CM, 37, "England", 80, 52, 80, 92, 78, 62, 70, 5_000_000L, 85_000L, isSub = true),
            createPlayer("psg_18", "Kevin Gameiro", PlayerPosition.ST, 25, "France", 79, 84, 80, 70, 78, 38, 70, 9_500_000L, 55_000L, isSub = true)
        )
        team.players.addAll(players)
        team.captainPlayerId = "psg_3"
        team.penaltyTakerId = "psg_10"
        team.freeKickTakerId = "psg_10"
        team.cornerTakerId = "psg_11"
        return team
    }

    private fun createPlayer(
        id: String,
        name: String,
        pos: PlayerPosition,
        age: Int,
        nat: String,
        ovr: Int,
        pac: Int,
        sho: Int,
        pas: Int,
        dri: Int,
        def: Int,
        phy: Int,
        value: Long,
        wage: Long,
        isStarting: Boolean = false,
        isSub: Boolean = false
    ): Player {
        return Player(
            id = id,
            name = name,
            position = pos,
            age = age,
            nationality = nat,
            overall = ovr,
            pace = pac,
            shooting = sho,
            passing = pas,
            dribbling = dri,
            defending = def,
            physical = phy,
            valueEuro = value,
            wageWeeklyEuro = wage,
            isStarting = isStarting,
            isSubstitute = isSub
        )
    }
}
