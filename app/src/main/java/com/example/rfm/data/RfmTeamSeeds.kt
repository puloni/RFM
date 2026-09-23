package com.example.rfm.data

import com.example.rfm.model.*

data class ClubSeed(
    val id: String,
    val name: String,
    val shortName: String,
    val leagueId: String,
    val stars: Int,
    val balanceM: Long,
    val transferM: Long,
    val stadium: String,
    val capacity: Int,
    val color1: Long,
    val color2: Long,
    val formation: Formation,
    val baseOvr: Int,
    val primaryNat: String,
    val starsData: List<Triple<String, PlayerPosition, Int>>,
    val squadNames: List<String>
)

object RfmTeamSeeds {

    private val backupRealNamesByNation = mapOf(
        "England" to listOf("Jack Butland", "Craig Dawson", "Kieran Gibbs", "Josh McEachran", "James Tomkins", "Callum McManaman", "Connor Wickham", "Sam Johnstone", "Tom Lees", "Will Hughes", "Luke Garbutt", "Nick Powell"),
        "Spain" to listOf("Joel Robles", "Marc Bartra", "Martin Montoya", "Sergi Roberto", "Pablo Sarabia", "Gerard Deulofeu", "Alvaro Morata", "Ruben Blanco", "Oliver Torres", "Jairo Samperio", "Denis Suarez", "Suso"),
        "Italy" to listOf("Simone Scuffet", "Davide Zappacosta", "Daniele Rugani", "Cristiano Biraghi", "Marco Benassi", "Bryan Cristante", "Lorenzo Insigne", "Nicola Leali", "Simone Zaza", "Ciro Immobile", "Jacopo Sala", "Alberto Masi"),
        "Germany" to listOf("Timo Horn", "Robin Knoche", "Christoph Kramer", "Johannes Geis", "Moritz Leitner", "Leonardo Bittencourt", "Maximilian Arnold", "Marc Stendera", "Timo Werner", "Philipp Hofmann", "Koray Gunter"),
        "France" to listOf("Alphonse Areola", "Dimitri Foulquier", "Lucas Digne", "Willy Boly", "Jordan Veretout", "Geoffrey Kondogbia", "Florian Thauvin", "Yaya Sanogo", "Nampalys Mendy", "Jean-Christophe Bahebeck", "Valentin Eysseric")
    )

    fun buildSeedTeam(seed: ClubSeed): Team {
        val team = Team(
            id = seed.id,
            name = seed.name,
            shortName = seed.shortName,
            leagueId = seed.leagueId,
            reputationStars = seed.stars,
            balanceEuro = seed.balanceM * 1_000_000L,
            transferBudgetEuro = seed.transferM * 1_000_000L,
            wageBudgetEuroWeekly = seed.stars * 350_000L,
            stadiumName = seed.stadium,
            stadiumCapacity = seed.capacity,
            primaryColorHex = seed.color1,
            secondaryColorHex = seed.color2,
            formation = seed.formation,
            seasonObjective = when {
                seed.stars >= 5 -> SeasonObjective.TITLE_CONTENDER
                seed.stars == 4 -> SeasonObjective.TOP_FOUR
                seed.stars == 3 -> SeasonObjective.MID_TABLE
                else -> SeasonObjective.AVOID_RELEGATION
            }
        )

        val slots = seed.formation.slots
        val subPositions = listOf(
            PlayerPosition.GK,
            PlayerPosition.CB,
            PlayerPosition.LB,
            PlayerPosition.CM,
            PlayerPosition.CAM,
            PlayerPosition.RM,
            PlayerPosition.ST
        )
        val reservePositions = listOf(
            PlayerPosition.GK,
            PlayerPosition.CB,
            PlayerPosition.LB,
            PlayerPosition.RB,
            PlayerPosition.CM,
            PlayerPosition.CAM,
            PlayerPosition.ST
        )

        val players = mutableListOf<Player>()
        val usedNames = mutableSetOf<String>()
        val starMap = seed.starsData.associateBy({ it.first }, { it })

        // Assemble pool of real names
        val realNamePool = mutableListOf<String>()
        realNamePool.addAll(seed.squadNames)
        for (star in seed.starsData) {
            if (!realNamePool.contains(star.first)) {
                realNamePool.add(0, star.first)
            }
        }
        val nationBackups = backupRealNamesByNation[seed.primaryNat] ?: backupRealNamesByNation["England"]!!
        realNamePool.addAll(nationBackups)

        fun getNextRealName(preferredStarPos: PlayerPosition? = null): Pair<String, Int> {
            if (preferredStarPos != null) {
                val star = seed.starsData.firstOrNull { it.second == preferredStarPos && it.first !in usedNames }
                if (star != null) {
                    usedNames.add(star.first)
                    return star.first to star.third
                }
            }
            for (candidate in realNamePool) {
                if (candidate !in usedNames) {
                    usedNames.add(candidate)
                    val starCheck = starMap[candidate]
                    val ovr = starCheck?.third ?: (seed.baseOvr + (usedNames.size % 3 - 1))
                    return candidate to ovr
                }
            }
            val fallback = "${seed.shortName} Legend"
            usedNames.add(fallback)
            return fallback to seed.baseOvr
        }

        // 11 Starters
        for (i in 0 until 11) {
            val pos = if (i < slots.size) slots[i] else PlayerPosition.CM
            val (name, ovr) = getNextRealName(pos)

            val isGk = pos == PlayerPosition.GK
            val isDef = pos.roleCategory == "Defender"
            val isMid = pos.roleCategory == "Midfielder"
            val isFwd = pos.roleCategory == "Forward"

            val pac = when {
                isGk -> 50
                pos in listOf(PlayerPosition.LB, PlayerPosition.RB, PlayerPosition.LW, PlayerPosition.RW, PlayerPosition.LM, PlayerPosition.RM) -> ovr + 4
                isDef -> ovr - 6
                else -> ovr - 1
            }.coerceIn(40, 95)

            val sho = when {
                isGk -> 20
                isFwd -> ovr + 4
                pos == PlayerPosition.CAM -> ovr - 1
                isMid -> ovr - 8
                else -> 48
            }.coerceIn(15, 95)

            val pas = when {
                isGk -> 65
                isMid -> ovr + 3
                pos == PlayerPosition.CAM -> ovr + 4
                isFwd -> ovr - 6
                else -> ovr - 8
            }.coerceIn(30, 96)

            val dri = when {
                isGk -> 40
                isFwd || pos in listOf(PlayerPosition.LW, PlayerPosition.RW, PlayerPosition.CAM) -> ovr + 3
                isMid -> ovr
                else -> ovr - 10
            }.coerceIn(25, 96)

            val def = when {
                isGk -> ovr + 3
                isDef -> ovr + 4
                pos == PlayerPosition.CDM -> ovr + 2
                isMid -> ovr - 6
                else -> 38
            }.coerceIn(20, 95)

            val phy = if (isDef || pos == PlayerPosition.CDM || pos == PlayerPosition.ST) ovr + 2 else ovr - 3
            val value = (ovr.toLong() * ovr * 2200L * seed.stars).coerceAtLeast(1_000_000L)
            val wage = (ovr.toLong() * ovr * 11L * seed.stars).coerceAtLeast(15_000L)

            players.add(
                Player(
                    id = "${seed.id}_s_${i + 1}",
                    name = name,
                    position = pos,
                    age = 22 + (i * 3) % 10,
                    nationality = seed.primaryNat,
                    overall = ovr,
                    pace = pac,
                    shooting = sho,
                    passing = pas,
                    dribbling = dri,
                    defending = def,
                    physical = phy.coerceIn(40, 96),
                    valueEuro = value,
                    wageWeeklyEuro = wage,
                    isStarting = true
                )
            )
        }

        // 7 Substitutes
        for (i in 0 until 7) {
            val pos = subPositions[i]
            val (name, ovr) = getNextRealName(pos)

            val isGk = pos == PlayerPosition.GK
            val isDef = pos.roleCategory == "Defender"
            val isFwd = pos.roleCategory == "Forward"

            val pac = if (isGk) 48 else if (isDef) ovr - 7 else ovr + 1
            val sho = if (isGk) 18 else if (isFwd) ovr + 2 else 45
            val pas = if (isGk) 60 else ovr
            val dri = if (isGk) 35 else ovr
            val def = if (isGk) ovr + 2 else if (isDef) ovr + 3 else 36
            val phy = if (isDef || isFwd) ovr + 1 else ovr - 4

            val value = (ovr.toLong() * ovr * 1800L * seed.stars).coerceAtLeast(800_000L)
            val wage = (ovr.toLong() * ovr * 9L * seed.stars).coerceAtLeast(10_000L)

            players.add(
                Player(
                    id = "${seed.id}_sub_${i + 1}",
                    name = name,
                    position = pos,
                    age = 21 + (i * 4) % 12,
                    nationality = seed.primaryNat,
                    overall = ovr.coerceAtLeast(70),
                    pace = pac.coerceIn(40, 92),
                    shooting = sho.coerceIn(15, 92),
                    passing = pas.coerceIn(30, 92),
                    dribbling = dri.coerceIn(25, 92),
                    defending = def.coerceIn(20, 92),
                    physical = phy.coerceIn(40, 92),
                    valueEuro = value,
                    wageWeeklyEuro = wage,
                    isSubstitute = true
                )
            )
        }

        // 7 Reserves (creating a complete 25-man authentic registered squad)
        for (i in 0 until reservePositions.size) {
            val pos = reservePositions[i]
            val (name, ovr) = getNextRealName(pos)

            val isGk = pos == PlayerPosition.GK
            val isDef = pos.roleCategory == "Defender"
            val isFwd = pos.roleCategory == "Forward"

            val pac = if (isGk) 45 else if (isDef) ovr - 6 else ovr
            val sho = if (isGk) 16 else if (isFwd) ovr + 1 else 42
            val pas = if (isGk) 55 else ovr - 2
            val dri = if (isGk) 32 else ovr - 2
            val def = if (isGk) ovr + 1 else if (isDef) ovr + 2 else 34
            val phy = if (isDef || isFwd) ovr else ovr - 5

            val value = (ovr.toLong() * ovr * 1400L * seed.stars).coerceAtLeast(500_000L)
            val wage = (ovr.toLong() * ovr * 7L * seed.stars).coerceAtLeast(8_000L)

            players.add(
                Player(
                    id = "${seed.id}_res_${i + 1}",
                    name = name,
                    position = pos,
                    age = 19 + (i * 3) % 9,
                    nationality = seed.primaryNat,
                    overall = (ovr - 2).coerceAtLeast(68),
                    pace = pac.coerceIn(40, 90),
                    shooting = sho.coerceIn(15, 90),
                    passing = pas.coerceIn(30, 90),
                    dribbling = dri.coerceIn(25, 90),
                    defending = def.coerceIn(20, 90),
                    physical = phy.coerceIn(40, 90),
                    valueEuro = value,
                    wageWeeklyEuro = wage,
                    isStarting = false,
                    isSubstitute = false
                )
            )
        }

        team.players.addAll(players)
        team.captainPlayerId = players.getOrNull(2)?.id ?: players.firstOrNull()?.id
        team.penaltyTakerId = players.getOrNull(10)?.id ?: players.lastOrNull()?.id
        team.freeKickTakerId = players.getOrNull(8)?.id ?: players.firstOrNull()?.id
        team.cornerTakerId = players.getOrNull(5)?.id ?: players.firstOrNull()?.id
        return team
    }

    // --- PREMIER LEAGUE (14 NEW TEAMS) ---
    val eplSeeds = listOf(
        ClubSeed("everton", "Everton", "EVE", "epl", 4, 25, 14, "Goodison Park", 39572, 0xFF003399, 0xFFFFFFFF, Formation.F_442, 78, "England",
            listOf(Triple("Marouane Fellaini", PlayerPosition.CM, 83), Triple("Leighton Baines", PlayerPosition.LB, 83), Triple("Phil Jagielka", PlayerPosition.CB, 81), Triple("Tim Howard", PlayerPosition.GK, 81), Triple("Nikica Jelavic", PlayerPosition.ST, 80)),
            listOf("Tim Howard", "Leighton Baines", "Phil Jagielka", "Sylvain Distin", "Seamus Coleman", "Steven Pienaar", "Marouane Fellaini", "Darron Gibson", "Kevin Mirallas", "Nikica Jelavic", "Leon Osman", "Jan Mucha", "John Heitinga", "Bryan Oviedo", "Thomas Hitzlsperger", "Ross Barkley", "Steven Naismith", "Victor Anichebe")
        ),
        ClubSeed("newcastle", "Newcastle United", "NEW", "epl", 4, 28, 16, "St James' Park", 52387, 0xFF241F20, 0xFFFFFFFF, Formation.F_4231, 78, "France",
            listOf(Triple("Yohan Cabaye", PlayerPosition.CDM, 82), Triple("Hatem Ben Arfa", PlayerPosition.LM, 82), Triple("Fabricio Coloccini", PlayerPosition.CB, 81), Triple("Tim Krul", PlayerPosition.GK, 81), Triple("Papiss Cisse", PlayerPosition.ST, 81)),
            listOf("Tim Krul", "Mathieu Debuchy", "Fabricio Coloccini", "Steven Taylor", "Davide Santon", "Yohan Cabaye", "Cheick Tiote", "Hatem Ben Arfa", "Moussa Sissoko", "Jonas Gutierrez", "Papiss Cisse", "Rob Elliot", "Mapou Yanga-Mbiwa", "Massadio Haidara", "Vurnon Anita", "Sylvain Marveaux", "Yoan Gouffran", "Shola Ameobi")
        ),
        ClubSeed("aston_villa", "Aston Villa", "AVL", "epl", 3, 20, 10, "Villa Park", 42788, 0xFF670E36, 0xFF95BFE5, Formation.F_433, 76, "England",
            listOf(Triple("Christian Benteke", PlayerPosition.ST, 82), Triple("Ron Vlaar", PlayerPosition.CB, 78), Triple("Gabriel Agbonlahor", PlayerPosition.LW, 78)),
            listOf("Brad Guzan", "Matthew Lowton", "Ron Vlaar", "Ciaran Clark", "Nathan Baker", "Ashley Westwood", "Fabian Delph", "Barry Bannan", "Gabriel Agbonlahor", "Christian Benteke", "Andreas Weimann", "Shay Given", "Joe Bennett", "Eric Lichaj", "Karim El Ahmadi", "Charles N'Zogbia", "Darren Bent", "Jordan Bowery")
        ),
        ClubSeed("fulham", "Fulham", "FUL", "epl", 3, 18, 8, "Craven Cottage", 25700, 0xFFFFFFFF, 0xFF000000, Formation.F_442, 76, "England",
            listOf(Triple("Dimitar Berbatov", PlayerPosition.ST, 82), Triple("Bryan Ruiz", PlayerPosition.ST, 79), Triple("Brede Hangeland", PlayerPosition.CB, 79)),
            listOf("Mark Schwarzer", "Sascha Riether", "Brede Hangeland", "Philippe Senderos", "John Arne Riise", "Damien Duff", "Steve Sidwell", "Giorgos Karagounis", "Alexander Kacaniklic", "Bryan Ruiz", "Dimitar Berbatov", "David Stockdale", "Aaron Hughes", "Chris Baird", "Emmanuel Frimpong", "Ashkan Dejagah", "Mladen Petric", "Hugo Rodallega")
        ),
        ClubSeed("west_ham", "West Ham United", "WHU", "epl", 3, 22, 10, "Boleyn Ground", 35016, 0xFF7A263A, 0xFF1BB1E7, Formation.F_4231, 76, "England",
            listOf(Triple("Andy Carroll", PlayerPosition.ST, 80), Triple("Kevin Nolan", PlayerPosition.CAM, 79), Triple("Mark Noble", PlayerPosition.CDM, 78)),
            listOf("Jussi Jaaskelainen", "Guy Demel", "Winston Reid", "James Collins", "Joey O'Brien", "Mark Noble", "Mohamed Diame", "Matt Jarvis", "Kevin Nolan", "Ricardo Vaz Te", "Andy Carroll", "Stephen Henderson", "James Tomkins", "Emanuel Pogatetz", "Joe Cole", "Jack Collison", "Carlton Cole", "Marouane Chamakh")
        ),
        ClubSeed("sunderland", "Sunderland", "SUN", "epl", 3, 20, 9, "Stadium of Light", 49000, 0xFFEB172B, 0xFFFFFFFF, Formation.F_442, 75, "England",
            listOf(Triple("Simon Mignolet", PlayerPosition.GK, 81), Triple("Steven Fletcher", PlayerPosition.ST, 79), Triple("Stephane Sessegnon", PlayerPosition.ST, 79)),
            listOf("Simon Mignolet", "Craig Gardner", "John O'Shea", "Carlos Cuellar", "Danny Rose", "Sebastian Larsson", "Lee Cattermole", "Jack Colback", "Adam Johnson", "Stephane Sessegnon", "Steven Fletcher", "Keiren Westwood", "Phil Bardsley", "Titus Bramble", "Alfred N'Diaye", "David Vaughan", "James McClean", "Danny Graham")
        ),
        ClubSeed("stoke_city", "Stoke City", "STK", "epl", 3, 19, 8, "Britannia Stadium", 27740, 0xFFE03A3E, 0xFFFFFFFF, Formation.F_442, 75, "England",
            listOf(Triple("Asmir Begovic", PlayerPosition.GK, 81), Triple("Ryan Shawcross", PlayerPosition.CB, 80), Triple("Robert Huth", PlayerPosition.CB, 79), Triple("Peter Crouch", PlayerPosition.ST, 78)),
            listOf("Asmir Begovic", "Ryan Shotton", "Ryan Shawcross", "Robert Huth", "Marc Wilson", "Jonathan Walters", "Glenn Whelan", "Steven Nzonzi", "Matthew Etherington", "Peter Crouch", "Kenwyne Jones", "Thomas Sorensen", "Andy Wilkinson", "Matthew Upson", "Charlie Adam", "Dean Whitehead", "Michael Kightly", "Cameron Jerome")
        ),
        ClubSeed("west_brom", "West Bromwich Albion", "WBA", "epl", 3, 21, 10, "The Hawthorns", 26850, 0xFF122F67, 0xFFFFFFFF, Formation.F_4231, 76, "England",
            listOf(Triple("Romelu Lukaku", PlayerPosition.ST, 81), Triple("Ben Foster", PlayerPosition.GK, 80), Triple("Youssouf Mulumbu", PlayerPosition.CDM, 78)),
            listOf("Ben Foster", "Billy Jones", "Gareth McAuley", "Jonas Olsson", "Liam Ridgewell", "Youssouf Mulumbu", "Claudio Yacob", "Chris Brunt", "James Morrison", "Graham Dorrans", "Romelu Lukaku", "Boaz Myhill", "Yassine El Ghanassy", "Gabriel Tamas", "Zoltan Gera", "Jerome Thomas", "Shane Long", "Peter Odemwingie")
        ),
        ClubSeed("swansea", "Swansea City", "SWA", "epl", 3, 20, 10, "Liberty Stadium", 20750, 0xFFFFFFFF, 0xFF000000, Formation.F_4231, 76, "Spain",
            listOf(Triple("Michu", PlayerPosition.CAM, 82), Triple("Ashley Williams", PlayerPosition.CB, 79), Triple("Michel Vorm", PlayerPosition.GK, 79)),
            listOf("Michel Vorm", "Angel Rangel", "Chico Flores", "Ashley Williams", "Ben Davies", "Leon Britton", "Ki Sung-yueng", "Nathan Dyer", "Michu", "Wayne Routledge", "Danny Graham", "Gerhard Tremmel", "Garry Monk", "Dwight Tiendalli", "Jonathan de Guzman", "Kemy Agustien", "Pablo Hernandez", "Itay Shechter")
        ),
        ClubSeed("norwich", "Norwich City", "NOR", "epl", 3, 16, 6, "Carrow Road", 27244, 0xFF00A651, 0xFFFFF200, Formation.F_442, 74, "England",
            listOf(Triple("Robert Snodgrass", PlayerPosition.RM, 78), Triple("Sebastien Bassong", PlayerPosition.CB, 77), Triple("John Ruddy", PlayerPosition.GK, 77)),
            listOf("John Ruddy", "Russell Martin", "Sebastien Bassong", "Michael Turner", "Javier Garrido", "Robert Snodgrass", "Bradley Johnson", "Alexander Tettey", "Anthony Pilkington", "Wes Hoolahan", "Grant Holt", "Mark Bunn", "Ryan Bennett", "Marc Tierney", "Jonny Howson", "David Fox", "Elliott Bennett", "Kei Kamara")
        ),
        ClubSeed("southampton", "Southampton", "SOU", "epl", 3, 19, 9, "St Mary's Stadium", 32505, 0xFFD71920, 0xFFFFFFFF, Formation.F_4231, 76, "England",
            listOf(Triple("Rickie Lambert", PlayerPosition.ST, 79), Triple("Adam Lallana", PlayerPosition.LM, 79), Triple("Morgan Schneiderlin", PlayerPosition.CDM, 79), Triple("Luke Shaw", PlayerPosition.LB, 78)),
            listOf("Artur Boruc", "Nathaniel Clyne", "Jose Fonte", "Maya Yoshida", "Luke Shaw", "Morgan Schneiderlin", "Jack Cork", "Jason Puncheon", "Steven Davis", "Adam Lallana", "Rickie Lambert", "Kelvin Davis", "Jos Hooiveld", "Danny Fox", "Guly do Prado", "James Ward-Prowse", "Jay Rodriguez", "Emmanuel Mayuka")
        ),
        ClubSeed("qpr", "Queens Park Rangers", "QPR", "epl", 3, 24, 12, "Loftus Road", 18439, 0xFF1D5BA4, 0xFFFFFFFF, Formation.F_442, 76, "England",
            listOf(Triple("Julio Cesar", PlayerPosition.GK, 82), Triple("Loic Remy", PlayerPosition.ST, 81), Triple("Adel Taarabt", PlayerPosition.LM, 79)),
            listOf("Julio Cesar", "Jose Bosingwa", "Christopher Samba", "Clint Hill", "Armand Traore", "Shaun Wright-Phillips", "Stephane Mbia", "Esteban Granero", "Adel Taarabt", "Park Ji-sung", "Loic Remy", "Robert Green", "Nedum Onuoha", "Fabio da Silva", "Jermaine Jenas", "Shaun Derry", "Andros Townsend", "Bobby Zamora")
        ),
        ClubSeed("reading", "Reading", "REA", "epl", 2, 14, 5, "Madejski Stadium", 24161, 0xFF004494, 0xFFFFFFFF, Formation.F_442, 73, "England",
            listOf(Triple("Pavel Pogrebnyak", PlayerPosition.ST, 76), Triple("Adam Le Fondre", PlayerPosition.ST, 76), Triple("Adam Federici", PlayerPosition.GK, 75)),
            listOf("Adam Federici", "Chris Gunter", "Sean Morrison", "Adrian Mariappa", "Ian Harte", "Garath McCleary", "Mikele Leigertwood", "Danny Guthrie", "Jobi McAnuff", "Pavel Pogrebnyak", "Adam Le Fondre", "Alex McCarthy", "Alex Pearce", "Stephen Kelly", "Jem Karacan", "Hal Robson-Kanu", "Noel Hunt", "Nick Blackman")
        ),
        ClubSeed("wigan", "Wigan Athletic", "WIG", "epl", 2, 15, 6, "DW Stadium", 25138, 0xFF0038A8, 0xFFFFFFFF, Formation.F_352, 74, "England",
            listOf(Triple("Arouna Kone", PlayerPosition.ST, 78), Triple("James McCarthy", PlayerPosition.CM, 78), Triple("Shaun Maloney", PlayerPosition.RM, 77), Triple("Ali Al Habsi", PlayerPosition.GK, 77)),
            listOf("Ali Al Habsi", "Emmerson Boyce", "Gary Caldwell", "Maynor Figueroa", "Ronnie Stam", "James McCarthy", "James McArthur", "Jean Beausejour", "Shaun Maloney", "Arouna Kone", "Franco Di Santo", "Mike Pollitt", "Roman Golobart", "Roger Espinoza", "Jordi Gomez", "Ben Watson", "Ryo Miyaichi", "Mauro Boselli")
        )
    )

    // --- LA LIGA (16 NEW TEAMS) ---
    val laLigaSeeds = listOf(
        ClubSeed("sevilla", "Sevilla FC", "SEV", "laliga", 4, 40, 22, "Ramon Sanchez Pizjuan", 43883, 0xFFFFFFFF, 0xFFD40026, Formation.F_4231, 80, "Spain",
            listOf(Triple("Jesus Navas", PlayerPosition.RM, 84), Triple("Alvaro Negredo", PlayerPosition.ST, 83), Triple("Ivan Rakitic", PlayerPosition.CAM, 83), Triple("Gary Medel", PlayerPosition.CDM, 80)),
            listOf("Andres Palop", "Coke", "Federico Fazio", "Emir Spahic", "Fernando Navarro", "Gary Medel", "Geoffrey Kondogbia", "Jesus Navas", "Ivan Rakitic", "Jose Antonio Reyes", "Alvaro Negredo", "Beto", "Juan Cala", "Cicinho", "Hedwiges Maduro", "Piotr Trochowski", "Diego Perotti", "Baba Diawara")
        ),
        ClubSeed("athletic_bilbao", "Athletic Bilbao", "ATH", "laliga", 4, 35, 18, "San Mames", 53289, 0xFFEE2524, 0xFFFFFFFF, Formation.F_4231, 80, "Spain",
            listOf(Triple("Iker Muniain", PlayerPosition.LM, 82), Triple("Ander Herrera", PlayerPosition.CAM, 81), Triple("Aritz Aduriz", PlayerPosition.ST, 81), Triple("Markel Susaeta", PlayerPosition.RM, 80)),
            listOf("Gorka Iraizoz", "Andoni Iraola", "Carlos Gurpegui", "Aymeric Laporte", "Jon Aurtenetxe", "Ander Iturraspe", "Ander Herrera", "Markel Susaeta", "Oscar De Marcos", "Iker Muniain", "Aritz Aduriz", "Raul Fernandez", "Mikel San Jose", "Borja Ekiza", "Inigo Perez", "Ibai Gomez", "Gaizka Toquero", "Fernando Llorente")
        ),
        ClubSeed("real_sociedad", "Real Sociedad", "RSO", "laliga", 4, 28, 14, "Anoeta", 39500, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 79, "Spain",
            listOf(Triple("Antoine Griezmann", PlayerPosition.LM, 83), Triple("Carlos Vela", PlayerPosition.RM, 82), Triple("Asier Illarramendi", PlayerPosition.CDM, 81), Triple("Claudio Bravo", PlayerPosition.GK, 80)),
            listOf("Claudio Bravo", "Carlos Martinez", "Mikel Gonzalez", "Inigo Martinez", "Alberto De la Bella", "Markel Bergara", "Asier Illarramendi", "Xabi Prieto", "Carlos Vela", "Antoine Griezmann", "Imanol Agirretxe", "Enaut Zubikarai", "Ion Ansotegi", "Liassine Cadamuro", "Ruben Pardo", "David Zurutuza", "Chory Castro", "Diego Ifran")
        ),
        ClubSeed("malaga", "Malaga CF", "MAL", "laliga", 4, 32, 16, "La Rosaleda", 30044, 0xFF1976D2, 0xFFFFFFFF, Formation.F_4231, 80, "Spain",
            listOf(Triple("Isco", PlayerPosition.CAM, 83), Triple("Joaquin", PlayerPosition.RM, 81), Triple("Jeremy Toulalan", PlayerPosition.CDM, 81), Triple("Willy Caballero", PlayerPosition.GK, 81)),
            listOf("Willy Caballero", "Jesus Gamez", "Martin Demichelis", "Weligton", "Vitorino Antunes", "Jeremy Toulalan", "Ignacio Camacho", "Joaquin", "Isco", "Eliseu", "Javier Saviola", "Carlos Kameni", "Sergio Sanchez", "Diego Lugano", "Manuel Iturra", "Duda", "Lucas Piazon", "Roque Santa Cruz")
        ),
        ClubSeed("real_betis", "Real Betis", "BET", "laliga", 3, 22, 10, "Benito Villamarin", 60720, 0xFF009540, 0xFFFFFFFF, Formation.F_442, 77, "Spain",
            listOf(Triple("Benat", PlayerPosition.CM, 81), Triple("Ruben Castro", PlayerPosition.ST, 80), Triple("Adrian", PlayerPosition.GK, 78)),
            listOf("Adrian", "Javi Chica", "Paulao", "Antonio Amaya", "Nacho", "Benat", "Jose Canas", "Joel Campbell", "Juan Carlos", "Ruben Castro", "Jorge Molina", "Casto", "Damien Perquis", "Angel Lopez", "Salva Sevilla", "Nono", "Dorlan Pabon", "Alejandro Pozuelo")
        ),
        ClubSeed("espanyol", "RCD Espanyol", "ESP", "laliga", 3, 20, 9, "Cornella-El Prat", 40500, 0xFF0070B8, 0xFFFFFFFF, Formation.F_4231, 76, "Spain",
            listOf(Triple("Joan Verdu", PlayerPosition.CAM, 79), Triple("Sergio Garcia", PlayerPosition.ST, 79), Triple("Hector Moreno", PlayerPosition.CB, 78)),
            listOf("Kiko Casilla", "Javi Lopez", "Diego Colotto", "Hector Moreno", "Joan Capdevila", "Victor Sanchez", "Juan Forlin", "Christian Stuani", "Joan Verdu", "Simao Sabrosa", "Sergio Garcia", "Cristian Alvarez", "Raul Rodriguez", "Felipe Mattioni", "Raul Baena", "Martin Petrov", "Wakaso Mubarak", "Samuele Longo")
        ),
        ClubSeed("getafe", "Getafe CF", "GET", "laliga", 3, 18, 8, "Coliseum Alfonso Perez", 17393, 0xFF005BAC, 0xFFFFFFFF, Formation.F_4231, 76, "Spain",
            listOf(Triple("Pedro Leon", PlayerPosition.RM, 79), Triple("Miguel Angel Moya", PlayerPosition.GK, 78), Triple("Abdelaziz Barrada", PlayerPosition.CAM, 78)),
            listOf("Miguel Angel Moya", "Juan Valera", "Alexis Ruano", "Alberto Lopo", "Miguel Torres", "Xavi Torres", "Medhi Lacen", "Pedro Leon", "Abdelaziz Barrada", "Diego Castro", "Alvaro Vazquez", "Jordi Codina", "Rafa Lopez", "Mane", "Borja Fernandez", "Jaime Gavilan", "Angel Lafita", "Adrian Colunga")
        ),
        ClubSeed("levante", "Levante UD", "LEV", "laliga", 3, 18, 8, "Ciutat de Valencia", 26354, 0xFF003366, 0xFFCC0033, Formation.F_4231, 76, "Spain",
            listOf(Triple("Keylor Navas", PlayerPosition.GK, 80), Triple("Obafemi Martins", PlayerPosition.ST, 79), Triple("Vicente Iborra", PlayerPosition.CDM, 78)),
            listOf("Gustavo Munua", "Pedro Lopez", "Sergio Ballesteros", "David Navarro", "Juanfran", "Papakouli Diop", "Vicente Iborra", "Pedro Rios", "Jose Javier Barkero", "Ruben Garcia", "Obafemi Martins", "Keylor Navas", "Loukas Vyntra", "Nikos Karabelas", "Dariusz Dudka", "Michel Herrero", "Valdo", "Robert Acquafresca")
        ),
        ClubSeed("granada", "Granada CF", "GRA", "laliga", 2, 15, 6, "Los Carmenes", 19336, 0xFFD40026, 0xFFFFFFFF, Formation.F_4231, 74, "Spain",
            listOf(Triple("Yacine Brahimi", PlayerPosition.CAM, 78), Triple("Guilherme Siqueira", PlayerPosition.LB, 78), Triple("Nolito", PlayerPosition.LM, 77)),
            listOf("Tono", "Allan Nyom", "Diego Mainz", "Pape Diakhate", "Guilherme Siqueira", "Mikel Rico", "Recio", "Gabriel Torje", "Yacine Brahimi", "Nolito", "Youssef El-Arabi", "Roberto", "Inigo Lopez", "Juanma Ortiz", "Moisés Iriney", "Diego Buonanotte", "Dani Benitez", "Odion Ighalo")
        ),
        ClubSeed("osasuna", "CA Osasuna", "OSA", "laliga", 2, 14, 5, "El Sadar", 23576, 0xFFD40026, 0xFF00205B, Formation.F_4231, 74, "Spain",
            listOf(Triple("Andres Fernandez", PlayerPosition.GK, 78), Triple("Alejandro Arribas", PlayerPosition.CB, 76), Triple("Kike Sola", PlayerPosition.ST, 76)),
            listOf("Andres Fernandez", "Marc Bertran", "Alejandro Arribas", "Miguel Flano", "Damia", "Lolo", "Patxi Punal", "Alvaro Cejudo", "Emiliano Armenteros", "Miguel de las Cuevas", "Kike Sola", "Asier Riesgo", "Oier Sanjurjo", "Nano", "Gato Silva", "Masoud Shojaei", "Nino", "Joseba Llorente")
        ),
        ClubSeed("valladolid", "Real Valladolid", "VLD", "laliga", 2, 14, 5, "Jose Zorrilla", 27618, 0xFF5B2B82, 0xFFFFFFFF, Formation.F_4231, 74, "Spain",
            listOf(Triple("Patrick Ebert", PlayerPosition.RM, 78), Triple("Oscar Gonzalez", PlayerPosition.CAM, 77), Triple("Antonio Rukavina", PlayerPosition.RB, 76)),
            listOf("Dani Hernandez", "Antonio Rukavina", "Henrique Sereno", "Marc Valiente", "Mikel Balenziaga", "Alvaro Rubio", "Victor Perez", "Patrick Ebert", "Oscar Gonzalez", "Alberto Bueno", "Javi Guerra", "Jaime Jimenez", "Jesus Rueda", "Carlos Pena", "Lluis Sastre", "Omar Ramos", "Valdet Rama", "Manucho")
        ),
        ClubSeed("zaragoza", "Real Zaragoza", "ZAR", "laliga", 2, 15, 6, "La Romareda", 33608, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 74, "Spain",
            listOf(Triple("Helder Postiga", PlayerPosition.ST, 78), Triple("Apono", PlayerPosition.CDM, 77), Triple("Roberto Jimenez", PlayerPosition.GK, 77)),
            listOf("Roberto Jimenez", "Cristian Sapunaru", "Alvaro Gonzalez", "Glenn Loovens", "Abraham Minero", "Jose Maria Movilla", "Apono", "Paco Montanes", "Rodri", "Ruben Rochina", "Helder Postiga", "Leo Franco", "Javier Paredes", "Victor Laguardia", "Lucas Pinter", "Jose Mari", "Edu Oriol", "Stefan Babovic")
        ),
        ClubSeed("rayo_vallecano", "Rayo Vallecano", "RAY", "laliga", 2, 14, 5, "Vallecas", 14708, 0xFFFFFFFF, 0xFFE31B23, Formation.F_4231, 75, "Spain",
            listOf(Triple("Piti", PlayerPosition.CAM, 78), Triple("Leo Baptistao", PlayerPosition.ST, 78), Triple("Roberto Trashorras", PlayerPosition.CDM, 77)),
            listOf("Ruben Martinez", "Tito", "Alejandro Galvez", "Jordi Amat", "Jose Manuel Casado", "Javi Fuego", "Roberto Trashorras", "Piti", "Chori Dominguez", "Lass Bangoura", "Leo Baptistao", "David Cobeno", "Anaitz Arbilla", "Inigo Labaka", "Adrian Gonzalez", "Jose Carlos", "Andrija Delibasic", "Raul Tamudo")
        ),
        ClubSeed("celta_vigo", "RC Celta Vigo", "CEL", "laliga", 2, 15, 6, "Balaidos", 29000, 0xFF87CEEB, 0xFFFFFFFF, Formation.F_433, 75, "Spain",
            listOf(Triple("Iago Aspas", PlayerPosition.ST, 80), Triple("Michael Krohn-Dehli", PlayerPosition.LW, 78), Triple("Augusto Fernandez", PlayerPosition.RW, 77)),
            listOf("Javi Varas", "Jonny Castro", "Gustavo Cabral", "Andres Tunez", "Roberto Lago", "Borja Oubina", "Alex Lopez", "Enrique de Lucas", "Augusto Fernandez", "Iago Aspas", "Michael Krohn-Dehli", "Sergio Alvarez", "Carlos Bellvis", "Jonathan Vila", "Natxo Insa", "Fabian Orellana", "Mario Bermejo", "Park Chu-young")
        ),
        ClubSeed("mallorca", "RCD Mallorca", "MLL", "laliga", 2, 14, 5, "Iberostar Stadium", 23142, 0xFFCC0000, 0xFF000000, Formation.F_442, 74, "Spain",
            listOf(Triple("Giovani dos Santos", PlayerPosition.ST, 79), Triple("Tomer Hemed", PlayerPosition.ST, 77), Triple("Dudu Aouate", PlayerPosition.GK, 76)),
            listOf("Dudu Aouate", "Alan Hutton", "Pedro Geromel", "Jose Carlos Nunes", "Pedro Bigas", "Tomas Pina", "Jose Luis Marti", "Emilio Nsue", "Giovani dos Santos", "Victor Casadesus", "Tomer Hemed", "Juan Calatayud", "Andreu Fontas", "Kevin Garcia", "Fernando Tissone", "Javi Marquez", "Alejandro Alfaro", "Javier Arizmendi")
        ),
        ClubSeed("deportivo", "Deportivo La Coruna", "DEP", "laliga", 2, 15, 6, "Riazor", 32660, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 74, "Spain",
            listOf(Triple("Juan Carlos Valeron", PlayerPosition.CAM, 78), Triple("Pizzi", PlayerPosition.LM, 78), Triple("Riki", PlayerPosition.ST, 77)),
            listOf("Daniel Aranzubia", "Manuel Pablo", "Ze Castro", "Carlos Marchena", "Ayoze Diaz", "Abel Aguilar", "Alex Bergantinos", "Bruno Gama", "Juan Carlos Valeron", "Pizzi", "Riki", "German Lux", "Kaka", "Silvio", "Paulo Assuncao", "Javier Camunas", "Diogo Salomao", "Nelson Oliveira")
        )
    )

    // --- SERIE A (16 NEW TEAMS) ---
    val serieASeeds = listOf(
        ClubSeed("roma", "AS Roma", "ROM", "seriea", 4, 45, 24, "Stadio Olimpico", 70634, 0xFF8E1F2F, 0xFFF0B323, Formation.F_433, 81, "Italy",
            listOf(Triple("Daniele De Rossi", PlayerPosition.CM, 85), Triple("Francesco Totti", PlayerPosition.ST, 84), Triple("Erik Lamela", PlayerPosition.RW, 82), Triple("Miralem Pjanic", PlayerPosition.CM, 82), Triple("Marquinhos", PlayerPosition.CB, 81)),
            listOf("Maarten Stekelenburg", "Ivan Piris", "Marquinhos", "Nicolas Burdisso", "Federico Balzaretti", "Daniele De Rossi", "Michael Bradley", "Miralem Pjanic", "Erik Lamela", "Francesco Totti", "Pablo Osvaldo", "Mauro Goicoechea", "Leandro Castan", "Dodo", "Alessandro Florenzi", "Marquinho", "Simone Perrotta", "Mattia Destro")
        ),
        ClubSeed("lazio", "SS Lazio", "LAZ", "seriea", 4, 35, 18, "Stadio Olimpico", 70634, 0xFF87CEEB, 0xFFFFFFFF, Formation.F_4231, 80, "Italy",
            listOf(Triple("Miroslav Klose", PlayerPosition.ST, 83), Triple("Hernanes", PlayerPosition.CAM, 83), Triple("Antonio Candreva", PlayerPosition.RM, 82), Triple("Federico Marchetti", PlayerPosition.GK, 82)),
            listOf("Federico Marchetti", "Abdoulay Konko", "Giuseppe Biava", "Andre Dias", "Stefan Radu", "Cristian Ledesma", "Alvaro Gonzalez", "Antonio Candreva", "Hernanes", "Senad Lulic", "Miroslav Klose", "Albano Bizzarri", "Michael Ciani", "Lorik Cana", "Ogenyi Onazi", "Ederson", "Stefano Mauri", "Sergio Floccari")
        ),
        ClubSeed("fiorentina", "ACF Fiorentina", "FIO", "seriea", 4, 36, 18, "Artemio Franchi", 43147, 0xFF4B0082, 0xFFFFFFFF, Formation.F_352, 80, "Italy",
            listOf(Triple("Stevan Jovetic", PlayerPosition.ST, 83), Triple("Borja Valero", PlayerPosition.CM, 82), Triple("Juan Cuadrado", PlayerPosition.RM, 81), Triple("David Pizarro", PlayerPosition.CDM, 80)),
            listOf("Emiliano Viviano", "Facundo Roncaglia", "Gonzalo Rodriguez", "Stefan Savic", "Juan Cuadrado", "Borja Valero", "David Pizarro", "Alberto Aquilani", "Manuel Pasqual", "Stevan Jovetic", "Luca Toni", "Neto", "Nenad Tomovic", "Marvin Compper", "Giulio Migliaccio", "Romulo", "Mati Fernandez", "Marcelo Larrondo")
        ),
        ClubSeed("udinese", "Udinese Calcio", "UDI", "seriea", 3, 25, 12, "Stadio Friuli", 25144, 0xFF000000, 0xFFFFFFFF, Formation.F_352, 78, "Italy",
            listOf(Triple("Antonio Di Natale", PlayerPosition.ST, 85), Triple("Mehdi Benatia", PlayerPosition.CB, 81), Triple("Luis Muriel", PlayerPosition.ST, 80), Triple("Dusan Basta", PlayerPosition.RM, 79)),
            listOf("Zeljko Brkic", "Mehdi Benatia", "Danilo", "Maurizio Domizzi", "Dusan Basta", "Allan", "Giampiero Pinzi", "Roberto Pereyra", "Gabriel Silva", "Luis Muriel", "Antonio Di Natale", "Daniele Padelli", "Thomas Heurtaux", "Gabriele Angella", "Emmanuel Agyemang-Badu", "Andrea Lazzari", "Maicosuel", "Mathias Ranegie")
        ),
        ClubSeed("parma", "Parma FC", "PAR", "seriea", 3, 20, 10, "Ennio Tardini", 22352, 0xFFFFFF00, 0xFF000080, Formation.F_433, 76, "Italy",
            listOf(Triple("Gabriel Paletta", PlayerPosition.CB, 80), Triple("Jonathan Biabiany", PlayerPosition.RW, 79), Triple("Marco Parolo", PlayerPosition.CM, 79)),
            listOf("Antonio Mirante", "Aleandro Rosi", "Gabriel Paletta", "Alessandro Lucarelli", "Massimo Gobbi", "Marco Marchionni", "Jaime Valdes", "Marco Parolo", "Jonathan Biabiany", "Ishak Belfodil", "Amauri", "Nicola Pavarini", "Yohan Benalouane", "Fabiano Santacroce", "Daniele Galloppa", "Sotiris Ninis", "Nicola Sansone", "Raffaele Palladino")
        ),
        ClubSeed("sampdoria", "UC Sampdoria", "SAM", "seriea", 3, 22, 10, "Luigi Ferraris", 36599, 0xFF0000FF, 0xFFFFFFFF, Formation.F_433, 76, "Italy",
            listOf(Triple("Mauro Icardi", PlayerPosition.ST, 80), Triple("Andrea Poli", PlayerPosition.CM, 79), Triple("Sergio Romero", PlayerPosition.GK, 79)),
            listOf("Sergio Romero", "Lorenzo De Silvestri", "Daniele Gastaldello", "Angelo Palombo", "Andrea Costa", "Pedro Obiang", "Nenad Krsticic", "Andrea Poli", "Marcelo Estigarribia", "Mauro Icardi", "Eder", "Junior Da Costa", "Shkodran Mustafi", "Gaetano Berardi", "Gianni Munari", "Enzo Maresca", "Gianluca Sansone", "Maxi Lopez")
        ),
        ClubSeed("torino", "Torino FC", "TOR", "seriea", 3, 18, 8, "Stadio Olimpico Torino", 28140, 0xFF800000, 0xFFFFFFFF, Formation.F_442, 75, "Italy",
            listOf(Triple("Angelo Ogbonna", PlayerPosition.CB, 80), Triple("Alessio Cerci", PlayerPosition.RM, 80), Triple("Matteo Darmian", PlayerPosition.RB, 78)),
            listOf("Jean-Francois Gillet", "Matteo Darmian", "Kamil Glik", "Angelo Ogbonna", "Danilo D'Ambrosio", "Matteo Brighi", "Alessandro Gazzi", "Alessio Cerci", "Mario Santana", "Rolando Bianchi", "Paulo Barreto", "Ferdinando Coppola", "Guillermo Rodriguez", "Salvatore Masiello", "Migjen Basha", "Giuseppe Vives", "Valter Birsa", "Jonathas")
        ),
        ClubSeed("genoa", "Genoa CFC", "GEN", "seriea", 3, 20, 9, "Luigi Ferraris", 36599, 0xFF800000, 0xFF000080, Formation.F_433, 76, "Italy",
            listOf(Triple("Sebastien Frey", PlayerPosition.GK, 80), Triple("Marco Borriello", PlayerPosition.ST, 79), Triple("Juraj Kucka", PlayerPosition.CM, 78)),
            listOf("Sebastien Frey", "Andreas Granqvist", "Daniele Portanova", "Thomas Manfredini", "Emiliano Moretti", "Juraj Kucka", "Matuzalem", "Juan Vargas", "Bosko Jankovic", "Marco Borriello", "Ciro Immobile", "Alexandros Tzorvas", "Cesare Bovo", "Luca Antonelli", "Marco Rossi", "Cristobal Jorquera", "Andrea Bertolacci", "Antonio Floro Flores")
        ),
        ClubSeed("cagliari", "Cagliari Calcio", "CAG", "seriea", 3, 18, 8, "Is Arenas", 16200, 0xFF800000, 0xFF000080, Formation.F_433, 76, "Italy",
            listOf(Triple("Radja Nainggolan", PlayerPosition.CM, 81), Triple("Davide Astori", PlayerPosition.CB, 80), Triple("Victor Ibarbo", PlayerPosition.RW, 79)),
            listOf("Michael Agazzi", "Francesco Pisano", "Luca Rossettini", "Davide Astori", "Danilo Avelar", "Daniele Dessena", "Daniele Conti", "Radja Nainggolan", "Andrea Cossu", "Marco Sau", "Victor Ibarbo", "Vlada Avramov", "Lorenzo Ariaudo", "Gabriele Perico", "Albin Ekdal", "Federico Casarini", "Thiago Ribeiro", "Mauricio Pinilla")
        ),
        ClubSeed("bologna", "Bologna FC", "BOL", "seriea", 3, 18, 8, "Renato Dall'Ara", 38279, 0xFF000080, 0xFF800000, Formation.F_4231, 76, "Italy",
            listOf(Triple("Alessandro Diamanti", PlayerPosition.CAM, 82), Triple("Alberto Gilardino", PlayerPosition.ST, 80), Triple("Manolo Gabbiadini", PlayerPosition.RM, 78)),
            listOf("Gianluca Curci", "Gyorgy Garics", "Mikael Antonsson", "Nicolo Cherubin", "Archimede Morleo", "Diego Perez", "Saphir Taider", "Panagiotis Kone", "Alessandro Diamanti", "Manolo Gabbiadini", "Alberto Gilardino", "Federico Agliardi", "Frederik Sorensen", "Roger Carvalho", "Tiberio Guarente", "Michele Pazienza", "Lazaros Christodoulopoulos", "Cristian Pasquato")
        ),
        ClubSeed("atalanta", "Atalanta BC", "ATA", "seriea", 3, 17, 7, "Atleti Azzurri d'Italia", 21300, 0xFF000000, 0xFF0000FF, Formation.F_442, 75, "Italy",
            listOf(Triple("German Denis", PlayerPosition.ST, 80), Triple("Giacomo Bonaventura", PlayerPosition.LM, 79), Triple("Luca Cigarini", PlayerPosition.CM, 79)),
            listOf("Andrea Consigli", "Gianpaolo Bellini", "Guglielmo Stendardo", "Stefano Lucchini", "Davide Brivio", "Ezequiel Schelotto", "Luca Cigarini", "Carlos Carmona", "Giacomo Bonaventura", "Maxi Moralez", "German Denis", "Ciro Polito", "Michele Canini", "Cristian Raimondi", "Riccardo Cazzola", "Ivan Radovanovic", "Guido De Luca", "Facundo Parra")
        ),
        ClubSeed("chievo", "Chievo Verona", "CHI", "seriea", 2, 14, 5, "Marcantonio Bentegodi", 31045, 0xFFFFFF00, 0xFF000080, Formation.F_433, 74, "Italy",
            listOf(Triple("Cyril Thereau", PlayerPosition.ST, 78), Triple("Marco Andreolli", PlayerPosition.CB, 77), Triple("Alberto Paloschi", PlayerPosition.ST, 76)),
            listOf("Christian Puggioni", "Gennaro Sardo", "Dario Dainelli", "Marco Andreolli", "Boukary Drame", "Roberto Guana", "Luca Rigoni", "Perparim Hetemaj", "Isaac Cofie", "Cyril Thereau", "Alberto Paloschi", "Samir Ujkani", "Bostjan Cesar", "Paul Papp", "Felipe Seymour", "Luciano", "Adrian Stoian", "Sergio Pellissier")
        ),
        ClubSeed("catania", "Calcio Catania", "CAT", "seriea", 3, 17, 7, "Angelo Massimino", 20266, 0xFFCC0000, 0xFF0066CC, Formation.F_433, 76, "Argentina",
            listOf(Triple("Alejandro Gomez", PlayerPosition.LW, 80), Triple("Francesco Lodi", PlayerPosition.CDM, 80), Triple("Gonzalo Bergessio", PlayerPosition.ST, 78)),
            listOf("Mariano Andujar", "Pablo Alvarez", "Giuseppe Bellusci", "Nicolas Spolli", "Giovanni Marchese", "Mariano Izco", "Francesco Lodi", "Sergio Almiron", "Pablo Barrientos", "Gonzalo Bergessio", "Alejandro Gomez", "Alberto Frison", "Nicola Legrottaglie", "Ciro Capuano", "Marco Biagianti", "Amidu Salifu", "Lucas Castro", "Souleymane Doukara")
        ),
        ClubSeed("palermo", "US Palermo", "PAL", "seriea", 2, 16, 6, "Renzo Barbera", 36349, 0xFFFFC0CB, 0xFF000000, Formation.F_352, 75, "Italy",
            listOf(Triple("Fabrizio Miccoli", PlayerPosition.ST, 81), Triple("Josip Ilicic", PlayerPosition.CAM, 80), Triple("Paulo Dybala", PlayerPosition.ST, 78)),
            listOf("Stefano Sorrentino", "Ezequiel Munoz", "Steve von Bergen", "Salvatore Aronica", "Michel Morganella", "Edgar Barreto", "Massimo Donati", "Jasmin Kurtic", "Andrea Dossena", "Josip Ilicic", "Fabrizio Miccoli", "Francesco Benussi", "Santiago Garcia", "Nelson", "Egidio Arevalo Rios", "Nicolas Viola", "Paulo Dybala", "Abel Hernandez")
        ),
        ClubSeed("siena", "AC Siena", "SIE", "seriea", 2, 13, 4, "Artemio Franchi", 15373, 0xFFFFFFFF, 0xFF000000, Formation.F_352, 73, "Italy",
            listOf(Triple("Gianluca Pegolo", PlayerPosition.GK, 77), Triple("Alessandro Rosina", PlayerPosition.CAM, 76), Triple("Innocent Emeghara", PlayerPosition.ST, 76)),
            listOf("Gianluca Pegolo", "Claudio Terzi", "Massimo Paci", "Felipe", "Angelo", "Simone Vergassola", "Francesco Della Rocca", "Matteo Rubin", "Alessandro Rosina", "Alessio Sestu", "Innocent Emeghara", "Simone Farelli", "Nicola Belmonte", "Bruno Uvini", "Francesco Bolzoni", "Francesco Valiani", "Reginaldo", "Erjon Bogdani")
        ),
        ClubSeed("pescara", "Delfino Pescara", "PES", "seriea", 2, 12, 4, "Adriatico", 20515, 0xFFFFFFFF, 0xFF0080FF, Formation.F_433, 72, "Italy",
            listOf(Triple("Mattia Perin", PlayerPosition.GK, 78), Triple("Juan Fernando Quintero", PlayerPosition.CAM, 77), Triple("Vladimir Weiss", PlayerPosition.LW, 76)),
            listOf("Mattia Perin", "Luciano Zauri", "Uros Cosic", "Marco Capuano", "Francesco Modesto", "Giuseppe Rizzo", "Romulo Togni", "Emmanuel Cascione", "Vladimir Weiss", "Juan Fernando Quintero", "Ante Vukusic", "Ivan Pelizzoli", "Nicolas Bianchi Arce", "Antonio Bocchetti", "Manuele Blasi", "Birkir Bjarnason", "Giuseppe Sculli", "Ferdinando Sforzini")
        )
    )

    // --- BUNDESLIGA (16 NEW TEAMS) ---
    val bundesligaSeeds = listOf(
        ClubSeed("leverkusen", "Bayer Leverkusen", "B04", "bundesliga", 4, 42, 22, "BayArena", 30210, 0xFF000000, 0xFFE32219, Formation.F_433, 81, "Germany",
            listOf(Triple("Stefan Kiessling", PlayerPosition.ST, 83), Triple("Andre Schurrle", PlayerPosition.LW, 83), Triple("Bernd Leno", PlayerPosition.GK, 82), Triple("Dani Carvajal", PlayerPosition.RB, 81), Triple("Lars Bender", PlayerPosition.CM, 81)),
            listOf("Bernd Leno", "Dani Carvajal", "Philipp Wollscheid", "Omer Toprak", "Sebastian Boenisch", "Lars Bender", "Simon Rolfes", "Gonzalo Castro", "Sidney Sam", "Stefan Kiessling", "Andre Schurrle", "Michael Rensing", "Manuel Friedrich", "Hajime Hosogai", "Jens Hegeler", "Stefan Reinartz", "Karim Bellarabi", "Junior Fernandes")
        ),
        ClubSeed("schalke", "FC Schalke 04", "S04", "bundesliga", 4, 45, 24, "Veltins-Arena", 62271, 0xFF004D9D, 0xFFFFFFFF, Formation.F_4231, 81, "Germany",
            listOf(Triple("Klaas-Jan Huntelaar", PlayerPosition.ST, 84), Triple("Benedikt Howedes", PlayerPosition.CB, 82), Triple("Jefferson Farfan", PlayerPosition.RM, 82), Triple("Julian Draxler", PlayerPosition.CAM, 82)),
            listOf("Timo Hildebrand", "Atsuto Uchida", "Benedikt Howedes", "Joel Matip", "Christian Fuchs", "Roman Neustadter", "Jermaine Jones", "Jefferson Farfan", "Julian Draxler", "Michel Bastos", "Klaas-Jan Huntelaar", "Ralf Fahrmann", "Christoph Metzelder", "Sead Kolasinac", "Marco Hoger", "Tranquillo Barnetta", "Chinedu Obasi", "Teemu Pukki")
        ),
        ClubSeed("monchengladbach", "Borussia Monchengladbach", "BMG", "bundesliga", 4, 30, 15, "Borussia-Park", 54057, 0xFFFFFFFF, 0xFF000000, Formation.F_442, 79, "Germany",
            listOf(Triple("Marc-Andre ter Stegen", PlayerPosition.GK, 83), Triple("Juan Arango", PlayerPosition.LM, 81), Triple("Granit Xhaka", PlayerPosition.CM, 80), Triple("Patrick Herrmann", PlayerPosition.RM, 80)),
            listOf("Marc-Andre ter Stegen", "Tony Jantschke", "Martin Stranzl", "Roel Brouwers", "Oscar Wendt", "Patrick Herrmann", "Havard Nordtveit", "Granit Xhaka", "Juan Arango", "Luuk de Jong", "Mike Hanke", "Christofer Heimeroth", "Alvaro Dominguez", "Filip Daems", "Tolga Cigerci", "Thorben Marx", "Lukas Rupp", "Amin Younes")
        ),
        ClubSeed("wolfsburg", "VfL Wolfsburg", "WOB", "bundesliga", 4, 35, 18, "Volkswagen Arena", 30000, 0xFF009900, 0xFFFFFFFF, Formation.F_4231, 79, "Germany",
            listOf(Triple("Diego", PlayerPosition.CAM, 83), Triple("Naldo", PlayerPosition.CB, 81), Triple("Diego Benaglio", PlayerPosition.GK, 81), Triple("Ricardo Rodriguez", PlayerPosition.LB, 80)),
            listOf("Diego Benaglio", "Fagner", "Naldo", "Simon Kjaer", "Ricardo Rodriguez", "Josue", "Jan Polak", "Vieirinha", "Diego", "Ivica Olic", "Bas Dost", "Marwin Hitz", "Alexander Madlung", "Marcel Schafer", "Christian Trasch", "Makoto Hasebe", "Ivan Perisic", "Patrick Helmes")
        ),
        ClubSeed("werder_bremen", "SV Werder Bremen", "BRE", "bundesliga", 3, 24, 12, "Weserstadion", 42100, 0xFF007A3D, 0xFFFFFFFF, Formation.F_4231, 77, "Germany",
            listOf(Triple("Kevin De Bruyne", PlayerPosition.CAM, 82), Triple("Sokratis", PlayerPosition.CB, 80), Triple("Aaron Hunt", PlayerPosition.LM, 79)),
            listOf("Sebastian Mielitz", "Theodor Gebre Selassie", "Sebastian Prodl", "Sokratis", "Lukas Schmitz", "Zlatko Junuzovic", "Clemens Fritz", "Marko Arnautovic", "Kevin De Bruyne", "Aaron Hunt", "Nils Petersen", "Richard Strebinger", "Assani Lukimya", "Aleksandar Ignjovski", "Mehmet Ekici", "Ozkan Yildirim", "Eljero Elia", "Joseph Akpala")
        ),
        ClubSeed("stuttgart", "VfB Stuttgart", "STU", "bundesliga", 3, 25, 12, "Mercedes-Benz Arena", 60449, 0xFFFFFFFF, 0xFFE32219, Formation.F_4231, 77, "Germany",
            listOf(Triple("Vedad Ibisevic", PlayerPosition.ST, 81), Triple("Martin Harnik", PlayerPosition.RM, 79), Triple("Serdar Tasci", PlayerPosition.CB, 79)),
            listOf("Sven Ulreich", "Gotoku Sakai", "Serdar Tasci", "Georg Niedermeier", "Cristian Molinaro", "William Kvist", "Christian Gentner", "Martin Harnik", "Alexandru Maxim", "Ibrahima Traore", "Vedad Ibisevic", "Marc Ziegler", "Antonio Rudiger", "Arthur Boka", "Zdravko Kuzmanovic", "Daniel Didavi", "Tunay Torun", "Cacau")
        ),
        ClubSeed("hsv", "Hamburger SV", "HSV", "bundesliga", 3, 26, 12, "Imtech Arena", 57000, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 78, "Germany",
            listOf(Triple("Rafael van der Vaart", PlayerPosition.CAM, 83), Triple("René Adler", PlayerPosition.GK, 83), Triple("Son Heung-min", PlayerPosition.LM, 81), Triple("Marcell Jansen", PlayerPosition.LB, 79)),
            listOf("René Adler", "Dennis Diekmeier", "Heiko Westermann", "Michael Mancienne", "Marcell Jansen", "Milan Badelj", "Tomas Rincon", "Son Heung-min", "Rafael van der Vaart", "Ivo Ilicevic", "Artjoms Rudnevs", "Jaroslav Drobny", "Jeffrey Bruma", "Dennis Aogo", "Tolgay Arslan", "Petr Jiracek", "Maximilian Beister", "Marcus Berg")
        ),
        ClubSeed("hannover", "Hannover 96", "H96", "bundesliga", 3, 22, 10, "HDI-Arena", 49000, 0xFF000000, 0xFF009900, Formation.F_442, 76, "Germany",
            listOf(Triple("Ron-Robert Zieler", PlayerPosition.GK, 80), Triple("Szabolcs Huszti", PlayerPosition.LM, 80), Triple("Mame Biram Diouf", PlayerPosition.ST, 79)),
            listOf("Ron-Robert Zieler", "Steve Cherundolo", "Mario Eggimann", "Christian Schulz", "Christian Pander", "Lars Stindl", "Manuel Schmiedebach", "Andre Hoffmann", "Szabolcs Huszti", "Mame Biram Diouf", "Mohammed Abdellaoue", "Markus Miller", "Johan Djourou", "Sebastien Pocognoli", "Sergio Pinto", "Jan Schlaudraff", "Didier Ya Konan", "Artur Sobiech")
        ),
        ClubSeed("frankfurt", "Eintracht Frankfurt", "SGE", "bundesliga", 3, 20, 9, "Commerzbank-Arena", 51500, 0xFF000000, 0xFFE32219, Formation.F_4231, 76, "Germany",
            listOf(Triple("Alexander Meier", PlayerPosition.CAM, 80), Triple("Kevin Trapp", PlayerPosition.GK, 79), Triple("Sebastian Rode", PlayerPosition.CDM, 78)),
            listOf("Kevin Trapp", "Sebastian Jung", "Carlos Zambrano", "Bamba Anderson", "Bastian Oczipka", "Pirmin Schwegler", "Sebastian Rode", "Stefan Aigner", "Alexander Meier", "Takashi Inui", "Srdjan Lakic", "Oka Nikolov", "Heiko Butscher", "Constant Djakpa", "Martin Lanig", "Sonny Kittel", "Karim Matmour", "Olivier Occean")
        ),
        ClubSeed("mainz", "1. FSV Mainz 05", "M05", "bundesliga", 3, 19, 8, "Coface Arena", 34000, 0xFFE32219, 0xFFFFFFFF, Formation.F_433, 76, "Germany",
            listOf(Triple("Adam Szalai", PlayerPosition.ST, 79), Triple("Nicolai Muller", PlayerPosition.RW, 78), Triple("Nikolce Noveski", PlayerPosition.CB, 77)),
            listOf("Heinz Muller", "Zdenek Pospech", "Bo Svensson", "Nikolce Noveski", "Junior Diaz", "Julian Baumgartlinger", "Elkin Soto", "Niki Zimling", "Nicolai Muller", "Adam Szalai", "Andreas Ivanschitz", "Christian Wetklo", "Stefan Bell", "Jan Kirchhoff", "Eugen Polanski", "Yunus Malli", "Shawn Parker", "Ivan Klasnic")
        ),
        ClubSeed("hoffenheim", "TSG 1899 Hoffenheim", "TSG", "bundesliga", 3, 22, 10, "Wirsol Rhein-Neckar-Arena", 30150, 0xFF005BAC, 0xFFFFFFFF, Formation.F_4231, 76, "Germany",
            listOf(Triple("Roberto Firmino", PlayerPosition.CAM, 80), Triple("Kevin Volland", PlayerPosition.RM, 79), Triple("Sejad Salihovic", PlayerPosition.LM, 79)),
            listOf("Koen Casteels", "Andreas Beck", "Matthieu Delpierre", "Jannik Vestergaard", "Fabian Johnson", "Eugen Polanski", "Sebastian Rudy", "Kevin Volland", "Roberto Firmino", "Sejad Salihovic", "Joselu", "Heurelho Gomes", "David Abraham", "Chris Thesker", "Danny Williams", "Tobias Weis", "Takashi Usami", "Eren Derdiyok")
        ),
        ClubSeed("freiburg", "SC Freiburg", "SCF", "bundesliga", 3, 18, 8, "Mage Solar Stadion", 24000, 0xFF000000, 0xFFFFFFFF, Formation.F_442, 76, "Germany",
            listOf(Triple("Max Kruse", PlayerPosition.ST, 80), Triple("Oliver Baumann", PlayerPosition.GK, 79), Triple("Matthias Ginter", PlayerPosition.CB, 78)),
            listOf("Oliver Baumann", "Mensur Mujdza", "Pavel Krmas", "Fallou Diagne", "Oliver Sorg", "Jonathan Schmid", "Julian Schuster", "Cedric Makiadi", "Daniel Caligiuri", "Max Kruse", "Jan Rosenthal", "Alexander Schwolow", "Matthias Ginter", "Vegar Hedenstad", "Johannes Flum", "Marco Terrazzino", "Karim Guede", "Ivan Santini")
        ),
        ClubSeed("augsburg", "FC Augsburg", "FCA", "bundesliga", 2, 15, 6, "SGL arena", 30660, 0xFFBA0C2F, 0xFF006633, Formation.F_4231, 74, "Germany",
            listOf(Triple("Alexander Manninger", PlayerPosition.GK, 77), Triple("Daniel Baier", PlayerPosition.CDM, 77), Triple("Koo Ja-cheol", PlayerPosition.CAM, 76)),
            listOf("Alexander Manninger", "Paul Verhaegh", "Gibril Sankoh", "Ragnar Klavan", "Matthias Ostrzolek", "Daniel Baier", "Jan-Ingwer Callsen-Bracker", "Tobias Werner", "Koo Ja-cheol", "Ji Dong-won", "Sascha Molders", "Mohamed Amsif", "Dominik Reinhardt", "Jan Moravek", "Milan Petrzela", "Stephan Hain", "Aristide Bance", "Knowledge Musona")
        ),
        ClubSeed("dusseldorf", "Fortuna Dusseldorf", "F95", "bundesliga", 2, 14, 5, "Esprit Arena", 54600, 0xFFD40026, 0xFFFFFFFF, Formation.F_4231, 73, "Germany",
            listOf(Triple("Fabian Giefer", PlayerPosition.GK, 77), Triple("Stefan Reisinger", PlayerPosition.ST, 75), Triple("Axel Bellinghausen", PlayerPosition.LM, 75)),
            listOf("Fabian Giefer", "Leon Balogun", "Stelios Malezas", "Adam Bodzek", "Johannes van den Bergh", "Oliver Fink", "Andreas Lambertz", "Robbie Kruse", "Ken Ilsø", "Axel Bellinghausen", "Stefan Reisinger", "Robert Almer", "Tobias Levels", "Jens Langeneke", "Robert Tesche", "Ronny Garbuschewski", "Nando Rafael", "Dani Schahin")
        ),
        ClubSeed("nurnberg", "1. FC Nurnberg", "FCN", "bundesliga", 2, 15, 6, "Grundig Stadion", 50000, 0xFF8A1538, 0xFFFFFFFF, Formation.F_4231, 74, "Germany",
            listOf(Triple("Hiroshi Kiyotake", PlayerPosition.CAM, 78), Triple("Timmy Simons", PlayerPosition.CDM, 77), Triple("Raphael Schafer", PlayerPosition.GK, 77)),
            listOf("Raphael Schafer", "Timothy Chandler", "Per Nilsson", "Timm Klose", "Javier Pinola", "Hanno Balitsch", "Timmy Simons", "Markus Feulner", "Hiroshi Kiyotake", "Alexander Esswein", "Sebastian Polter", "Patrick Rakovsky", "Berkay Dabanli", "Marvin Plattenhardt", "Almog Cohen", "Timo Gebhart", "Robert Mak", "Tomas Pekhart")
        ),
        ClubSeed("furth", "SpVgg Greuther Furth", "SGF", "bundesliga", 2, 12, 4, "Trolli Arena", 18000, 0xFF008000, 0xFFFFFFFF, Formation.F_442, 72, "Germany",
            listOf(Triple("Baba Rahman", PlayerPosition.LB, 76), Triple("Sercan Sararer", PlayerPosition.RM, 75), Triple("Nikola Djurdjic", PlayerPosition.ST, 74)),
            listOf("Max Grun", "Bernd Nehrig", "Thomas Kleine", "Mergim Mavraj", "Baba Rahman", "Stephan Furstner", "Milorad Pekovic", "Sercan Sararer", "Zoltan Stieber", "Nikola Djurdjic", "Gerald Asamoah", "Wolfgang Hesl", "Lasse Sobiech", "Heinrich Schmidtgal", "Stipe Vranjes", "Tayfun Pekturk", "Felix Klaus", "Ilir Azemi")
        )
    )

    // --- LIGUE 1 (19 NEW TEAMS) ---
    val ligue1Seeds = listOf(
        ClubSeed("marseille", "Olympique Marseille", "OM", "ligue1", 4, 40, 20, "Stade Velodrome", 67394, 0xFF00A3E0, 0xFFFFFFFF, Formation.F_4231, 80, "France",
            listOf(Triple("Steve Mandanda", PlayerPosition.GK, 83), Triple("Mathieu Valbuena", PlayerPosition.CAM, 83), Triple("Nicolas N'Koulou", PlayerPosition.CB, 81), Triple("Andre Ayew", PlayerPosition.LM, 81), Triple("Andre-Pierre Gignac", PlayerPosition.ST, 81)),
            listOf("Steve Mandanda", "Rod Fanni", "Nicolas N'Koulou", "Lucas Mendes", "Jeremy Morel", "Alaixys Romao", "Joey Barton", "Morgan Amalfitano", "Mathieu Valbuena", "Andre Ayew", "Andre-Pierre Gignac", "Brice Samba", "Souleymane Diawara", "Rafidine Abdullah", "Benoit Cheyrou", "Foued Kadir", "Jordan Ayew", "Modou Sougou")
        ),
        ClubSeed("lyon", "Olympique Lyonnais", "OL", "ligue1", 4, 42, 22, "Stade de Gerland", 41044, 0xFFFFFFFF, 0xFF0038A8, Formation.F_433, 80, "France",
            listOf(Triple("Lisandro Lopez", PlayerPosition.ST, 83), Triple("Maxime Gonalons", PlayerPosition.CDM, 81), Triple("Bafetimbi Gomis", PlayerPosition.ST, 81), Triple("Alexandre Lacazette", PlayerPosition.RW, 80), Triple("Samuel Umtiti", PlayerPosition.CB, 80)),
            listOf("Remy Vercoutre", "Mouhamadou Dabo", "Milan Bisevac", "Samuel Umtiti", "Anthony Reveillere", "Maxime Gonalons", "Steed Malbranque", "Clement Grenier", "Alexandre Lacazette", "Bafetimbi Gomis", "Lisandro Lopez", "Anthony Lopes", "Bakary Kone", "Gueida Fofana", "Jordan Ferri", "Yoann Gourcuff", "Jimmy Briand", "Rachid Ghezzal")
        ),
        ClubSeed("lille", "Lille OSC", "LOSC", "ligue1", 4, 35, 18, "Grand Stade Lille", 50186, 0xFFE2001A, 0xFFFFFFFF, Formation.F_433, 79, "France",
            listOf(Triple("Dimitri Payet", PlayerPosition.LW, 82), Triple("Rio Mavuba", PlayerPosition.CDM, 80), Triple("Aurelien Chedjou", PlayerPosition.CB, 80), Triple("Salomon Kalou", PlayerPosition.RW, 79)),
            listOf("Steeve Elana", "Franck Beria", "Marko Basa", "Aurelien Chedjou", "Lucas Digne", "Rio Mavuba", "Florent Balmont", "Marvin Martin", "Salomon Kalou", "Nolan Roux", "Dimitri Payet", "Barel Mouko", "David Rozehnal", "Djibril Sidibe", "Idrissa Gueye", "Benoit Pedretti", "Ronny Rodelin", "Tulio de Melo")
        ),
        ClubSeed("saint_etienne", "AS Saint-Etienne", "ASSE", "ligue1", 4, 30, 15, "Geoffroy-Guichard", 41965, 0xFF008000, 0xFFFFFFFF, Formation.F_4231, 79, "France",
            listOf(Triple("Pierre-Emerick Aubameyang", PlayerPosition.ST, 83), Triple("Stephane Ruffier", PlayerPosition.GK, 82), Triple("Loic Perrin", PlayerPosition.CB, 80), Triple("Kurt Zouma", PlayerPosition.CB, 78)),
            listOf("Stephane Ruffier", "Francois Clerc", "Loic Perrin", "Moustapha Bayal Sall", "Faouzi Ghoulam", "Josuha Guilavogui", "Jeremy Clement", "Romain Hamouma", "Renaud Cohade", "Yohan Mollo", "Pierre-Emerick Aubameyang", "Jessy Moulin", "Kurt Zouma", "Jonathan Brison", "Fabien Lemoine", "Mathieu Bodmer", "Brandao", "Kevin Mayi")
        ),
        ClubSeed("bordeaux", "Girondins de Bordeaux", "FCGB", "ligue1", 3, 25, 12, "Chaban-Delmas", 34694, 0xFF001844, 0xFFFFFFFF, Formation.F_4231, 78, "France",
            listOf(Triple("Cedric Carrasso", PlayerPosition.GK, 81), Triple("Benoit Tremoulinas", PlayerPosition.LB, 80), Triple("Jaroslav Plasil", PlayerPosition.CM, 79)),
            listOf("Cedric Carrasso", "Mariano", "Henrique", "Lamine Sane", "Benoit Tremoulinas", "Jaroslav Plasil", "Landry N'Guemo", "Henri Saivet", "Ludovic Obraniak", "Nicolas Maurice-Belay", "Cheick Diabate", "Kevin Olimpa", "Marc Planus", "Florian Marange", "Gregory Sertic", "Andre Biyogo Poko", "Julien Faubert", "Diego Rolan")
        ),
        ClubSeed("montpellier", "Montpellier HSC", "MHSC", "ligue1", 3, 24, 12, "Stade de la Mosson", 32900, 0xFF002349, 0xFFFF6600, Formation.F_4231, 78, "France",
            listOf(Triple("Younes Belhanda", PlayerPosition.CAM, 81), Triple("Remy Cabella", PlayerPosition.LM, 80), Triple("Henri Bedimo", PlayerPosition.LB, 79)),
            listOf("Geoffrey Jourdren", "Garry Bocaly", "Vitorino Hilton", "Daniel Congre", "Henri Bedimo", "Jamel Saihi", "Marco Estrada", "Souleymane Camara", "Younes Belhanda", "Remy Cabella", "John Utaka", "Laurent Pionnier", "Abdelhamid El Kaoutari", "Cyril Jeunechamp", "Benjamin Stambouli", "Anthony Mounier", "Romain Pitau", "Gaetan Charbonnier")
        ),
        ClubSeed("nice", "OGC Nice", "OGCN", "ligue1", 3, 20, 10, "Stade du Ray", 18696, 0xFFCC0000, 0xFF000000, Formation.F_4231, 77, "France",
            listOf(Triple("Dario Cvitanich", PlayerPosition.ST, 80), Triple("David Ospina", PlayerPosition.GK, 80), Triple("Didier Digard", PlayerPosition.CDM, 78)),
            listOf("David Ospina", "Romain Genevois", "Renato Civelli", "Nemanja Pejcinovic", "Timothee Kolodziejczak", "Didier Digard", "Mahamane Traore", "Alassane Plea", "Valentin Eysseric", "Eric Bautheac", "Dario Cvitanich", "Joris Delle", "Kevin Gomis", "Lloyd Palun", "Fabrice Abriel", "Camel Meriem", "Luigi Bruins", "Neal Maupay")
        ),
        ClubSeed("rennes", "Stade Rennais", "SRFC", "ligue1", 3, 22, 11, "Route de Lorient", 29778, 0xFFCC0000, 0xFF000000, Formation.F_433, 77, "France",
            listOf(Triple("Romain Alessandrini", PlayerPosition.LW, 80), Triple("Benoit Costil", PlayerPosition.GK, 79), Triple("Julien Feret", PlayerPosition.CM, 79)),
            listOf("Benoit Costil", "Romain Danze", "John Boye", "Jean-Armel Kana-Biyik", "Kevin Theophile-Catherine", "Jean Makoun", "Vincent Pajot", "Julien Feret", "Jonathan Pitroipa", "Mevlut Erding", "Romain Alessandrini", "Abdoulaye Diallo", "Onyekachi Apam", "Steven Moreira", "Sadio Diallo", "Abdoulaye Doucoure", "Salif Sane", "Victor Hugo Montano")
        ),
        ClubSeed("toulouse", "Toulouse FC", "TFC", "ligue1", 3, 20, 9, "Stadium de Toulouse", 33150, 0xFF4B0082, 0xFFFFFFFF, Formation.F_352, 76, "France",
            listOf(Triple("Etienne Capoue", PlayerPosition.CDM, 81), Triple("Aymen Abdennour", PlayerPosition.CB, 80), Triple("Wissam Ben Yedder", PlayerPosition.ST, 79), Triple("Serge Aurier", PlayerPosition.RM, 79)),
            listOf("Ali Ahamada", "Pavle Ninkov", "Aymen Abdennour", "Jonathan Zebina", "Serge Aurier", "Etienne Capoue", "Etienne Didot", "Franck Tabanou", "Pantxi Sirieix", "Wissam Ben Yedder", "Daniel Braaten", "Olivier Blondel", "Steeve Yago", "Cheikh M'Bengue", "Adil Hermach", "Adrien Regattin", "Yannick Djalo", "Emmanuel Riviere")
        ),
        ClubSeed("lorient", "FC Lorient", "FCL", "ligue1", 3, 18, 8, "Stade du Moustoir", 18110, 0xFFFF6600, 0xFF000000, Formation.F_442, 76, "France",
            listOf(Triple("Jeremie Aliadiere", PlayerPosition.ST, 79), Triple("Yann Jouffre", PlayerPosition.RM, 78), Triple("Bruno Ecuele Manga", PlayerPosition.CB, 78)),
            listOf("Fabien Audard", "Lamine Gassama", "Lamine Kone", "Gregory Bourillon", "Lucas Mareque", "Yann Jouffre", "Alaixys Romao", "Benjamin Corgnet", "Kevin Monnet-Paquet", "Alain Traore", "Jeremie Aliadiere", "Benjamin Lecomte", "Bruno Ecuele Manga", "Arnaud Le Lan", "Mario Lemina", "Maxime Barthelme", "Enzo Reale", "Ludovic Giuly")
        ),
        ClubSeed("reims", "Stade de Reims", "SDR", "ligue1", 2, 15, 6, "Auguste Delaune", 21684, 0xFFCC0000, 0xFFFFFFFF, Formation.F_4231, 74, "France",
            listOf(Triple("Grzegorz Krychowiak", PlayerPosition.CDM, 78), Triple("Aissa Mandi", PlayerPosition.RB, 76), Triple("Kossi Agassa", PlayerPosition.GK, 76)),
            listOf("Kossi Agassa", "Aissa Mandi", "Mickael Tacalfred", "Mohamed Fofana", "Franck Signorino", "Grzegorz Krychowiak", "Antoine Devaux", "Odaïr Fortes", "Johann Ramare", "Diego Rigonato", "Gaetan Courtet", "Johny Placide", "Anthony Weber", "Alexandre Ghisolfi", "Bocundji Ca", "Floyd Ayite", "Alexis Deaux", "Nicolas Fauvergue")
        ),
        ClubSeed("bastia", "SC Bastia", "SCB", "ligue1", 2, 15, 6, "Armand Cesari", 16078, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 75, "France",
            listOf(Triple("Florian Thauvin", PlayerPosition.RM, 79), Triple("Mickael Landreau", PlayerPosition.GK, 79), Triple("Anthony Modeste", PlayerPosition.ST, 78), Triple("Wahbi Khazri", PlayerPosition.CAM, 78)),
            listOf("Mickael Landreau", "Gilles Cioni", "Sylvain Marchal", "Jeremy Choplin", "Fethi Harek", "Yannick Cahuzac", "Jacques Faty", "Wahbi Khazri", "Jerome Rothen", "Florian Thauvin", "Anthony Modeste", "Landry Bonnefoi", "Francois Modesto", "Matthieu Sans", "Julien Sable", "Julian Palmieri", "Ilan", "Toifilou Maoulida")
        ),
        ClubSeed("sochaux", "FC Sochaux", "FCSM", "ligue1", 2, 14, 5, "Auguste Bonal", 20005, 0xFFFFFF00, 0xFF000080, Formation.F_4231, 74, "France",
            listOf(Triple("Ryad Boudebouz", PlayerPosition.CAM, 78), Triple("Sebastien Corchia", PlayerPosition.RB, 78), Triple("Cedric Bakambu", PlayerPosition.ST, 76)),
            listOf("Simon Pouplin", "Sebastien Corchia", "Cedric Kante", "Mathieu Peybernes", "Jerome Roussillon", "Thierry Doubai", "Vincent Nogueira", "Ryad Boudebouz", "Sebastien Roudet", "Cedric Bakambu", "Sloan Privat", "Yohann Pele", "David Sauget", "Carlao", "Kalilou Traore", "Joseph Lopy", "Rafaël Dias", "Edouard Butin")
        ),
        ClubSeed("valenciennes", "Valenciennes FC", "VAFC", "ligue1", 2, 15, 6, "Stade du Hainaut", 25172, 0xFFCC0000, 0xFFFFFFFF, Formation.F_4231, 74, "France",
            listOf(Triple("Gael Danic", PlayerPosition.LM, 77), Triple("Nicolas Isimat-Mirin", PlayerPosition.CB, 76), Triple("Vincent Aboubakar", PlayerPosition.ST, 76)),
            listOf("Nicolas Penneteau", "Rudy Mater", "Nicolas Isimat-Mirin", "Benjamin Angoua", "Gaetan Bong", "Carlos Sanchez", "Remi Gomis", "Matthieu Dossevi", "Gael Danic", "Maor Melikson", "Gregory Pujol", "Jean-Louis Leca", "Lindsay Rose", "Kenny Lala", "Jose Saez", "Abdou Camara", "Opa Nguette", "Vincent Aboubakar")
        ),
        ClubSeed("ajaccio", "AC Ajaccio", "ACA", "ligue1", 2, 13, 4, "Francois Coty", 10446, 0xFFFFFFFF, 0xFFCC0000, Formation.F_442, 73, "France",
            listOf(Triple("Guillermo Ochoa", PlayerPosition.GK, 80), Triple("Adrian Mutu", PlayerPosition.ST, 78), Triple("Johan Cavalli", PlayerPosition.CM, 75)),
            listOf("Guillermo Ochoa", "Fousseni Diawara", "Yoann Poulard", "Ronald Zubar", "Samuel Bouhours", "Mehdi Mostefa", "Jean-Baptiste Pierazzi", "Johan Cavalli", "Benjamin Andre", "Chahir Belghazouani", "Adrian Mutu", "David Oberhauser", "Felipe Saad", "Anthony Lippini", "Ricardo Faty", "Paul Lasne", "Frederic Sammaritano", "Eduardo")
        ),
        ClubSeed("evian", "Evian TG", "ETG", "ligue1", 2, 14, 5, "Parc des Sports Annecy", 15660, 0xFFFF69B4, 0xFFFFFFFF, Formation.F_4231, 74, "France",
            listOf(Triple("Saber Khelifa", PlayerPosition.ST, 78), Triple("Daniel Wass", PlayerPosition.RM, 77), Triple("Brice Dja Djedje", PlayerPosition.RB, 76)),
            listOf("Bertrand Laquait", "Brice Dja Djedje", "Cedric Cambon", "Betao", "Daniel Wass", "Olivier Sorlin", "Mohammed Rabiu", "Cedric Barbosa", "Milos Ninkovic", "Kevin Berigaud", "Saber Khelifa", "Stephan Andersen", "Cedric Mongongu", "Guillaume Lacour", "Fernando Tissone", "Yannick Sagbo", "Youssef Adnane", "Sidney Govou")
        ),
        ClubSeed("troyes", "ESTAC Troyes", "ESTAC", "ligue1", 2, 12, 4, "Stade de l'Aube", 20400, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4231, 73, "France",
            listOf(Triple("Benjamin Nivet", PlayerPosition.CAM, 77), Triple("Yohann Thuram", PlayerPosition.GK, 76), Triple("Fabrice N'Sakala", PlayerPosition.LB, 75)),
            listOf("Yohann Thuram", "Julien Faussurier", "Matthieu Saunier", "Rincon", "Fabrice N'Sakala", "Thiago Xavier", "Eloge Enza-Yamissi", "Fabien Camus", "Benjamin Nivet", "Stephane Darbion", "Marcos dos Santos", "Matthieu Dreyer", "Florian Jarjat", "Mael L'Hostis", "Granddi Ngoyi", "Maxime Colin", "Jean-Christophe Bahebeck", "Corentin Jean")
        ),
        ClubSeed("brest", "Stade Brestois 29", "SB29", "ligue1", 2, 13, 4, "Francis-Le Ble", 15931, 0xFFCC0000, 0xFFFFFFFF, Formation.F_442, 73, "France",
            listOf(Triple("Paul Baysse", PlayerPosition.CB, 76), Triple("Bruno Grougi", PlayerPosition.CM, 76), Triple("Alexis Thebaux", PlayerPosition.GK, 75)),
            listOf("Alexis Thebaux", "Bernard Mendy", "Ismael Traore", "Johan Martial", "Paul Baysse", "Abdoulwhaid Sissoko", "Bruno Grougi", "Kamel Chafni", "Benoit Lesoimier", "Charlison Benschop", "Florian Raspentino", "Lionel Cappone", "Ahmed Kantari", "Tripy Makonda", "Mario Licka", "Geoffrey Dernis", "Larsen Toure", "Richard Soumah")
        ),
        ClubSeed("nancy", "AS Nancy", "ASNL", "ligue1", 2, 13, 4, "Marcel Picot", 20087, 0xFFCC0000, 0xFFFFFFFF, Formation.F_433, 73, "France",
            listOf(Triple("Sebastien Puygrenier", PlayerPosition.CB, 77), Triple("Benjamin Moukandjo", PlayerPosition.ST, 76), Triple("Thomas Mangani", PlayerPosition.CM, 75)),
            listOf("Damien Gregorini", "Yassine Jebbour", "Sebastien Puygrenier", "Jordan Loties", "Vincent Muratori", "Salif Sane", "Thomas Mangani", "Lossemy Karaboue", "Florian Grange", "Djamel Bakar", "Benjamin Moukandjo", "Paul Nardi", "Helder", "Massadio Haidara", "Florent Zitte", "Romain Grange", "Paul Alo'o Efoulou", "Jo-Gook Jung")
        )
    )

    // --- REST OF EUROPE (CHAMPIONS LEAGUE REGULARS) ---
    val restOfEuropeSeeds = listOf(
        ClubSeed("porto", "FC Porto", "FCP", "europe", 4, 38, 20, "Estadio do Dragao", 50033, 0xFF0038A8, 0xFFFFFFFF, Formation.F_433, 81, "Portugal",
            listOf(Triple("Jackson Martinez", PlayerPosition.ST, 83), Triple("James Rodriguez", PlayerPosition.RW, 83), Triple("Joao Moutinho", PlayerPosition.CM, 83), Triple("Nicolas Otamendi", PlayerPosition.CB, 81)),
            listOf("Helton", "Danilo", "Nicolas Otamendi", "Eliaquim Mangala", "Alex Sandro", "Fernando", "Lucho Gonzalez", "Joao Moutinho", "James Rodriguez", "Jackson Martinez", "Silvestre Varela", "Fabiano", "Maicon", "Abdoulaye Ba", "Steven Defour", "Marat Izmailov", "Christian Atsu", "Liedson")
        ),
        ClubSeed("benfica", "SL Benfica", "BEN", "europe", 4, 35, 18, "Estadio da Luz", 64642, 0xFFCC0000, 0xFFFFFFFF, Formation.F_433, 81, "Portugal",
            listOf(Triple("Oscar Cardozo", PlayerPosition.ST, 83), Triple("Nicolas Gaitan", PlayerPosition.LW, 82), Triple("Nemanja Matic", PlayerPosition.CDM, 82), Triple("Ezequiel Garay", PlayerPosition.CB, 82)),
            listOf("Artur", "Maxi Pereira", "Luisao", "Ezequiel Garay", "Lorenzo Melgarejo", "Nemanja Matic", "Enzo Perez", "Nicolas Gaitan", "Eduardo Salvio", "Oscar Cardozo", "Lima", "Paulo Lopes", "Jardel", "Roderick Miranda", "Andre Almeida", "Carlos Martins", "Ola John", "Rodrigo")
        ),
        ClubSeed("ajax", "AFC Ajax", "AJX", "europe", 4, 30, 16, "Amsterdam ArenA", 54990, 0xFFCC0000, 0xFFFFFFFF, Formation.F_433, 79, "Netherlands",
            listOf(Triple("Christian Eriksen", PlayerPosition.CAM, 83), Triple("Toby Alderweireld", PlayerPosition.CB, 81), Triple("Siem de Jong", PlayerPosition.ST, 80)),
            listOf("Kenneth Vermeer", "Ricardo van Rhijn", "Toby Alderweireld", "Niklas Moisander", "Daley Blind", "Christian Poulsen", "Lasse Schone", "Christian Eriksen", "Derk Boerrigter", "Siem de Jong", "Viktor Fischer", "Jasper Cillessen", "Joel Veltman", "Nicolai Boilesen", "Eyong Enoh", "Thulani Serero", "Ryan Babel", "Kolbeinn Sigthorsson")
        ),
        ClubSeed("galatasaray", "Galatasaray SK", "GAL", "europe", 4, 32, 18, "Turk Telekom Arena", 52280, 0xFFFFB612, 0xFFA90432, Formation.F_4312, 80, "Turkey",
            listOf(Triple("Didier Drogba", PlayerPosition.ST, 83), Triple("Wesley Sneijder", PlayerPosition.CAM, 84), Triple("Burak Yilmaz", PlayerPosition.ST, 82), Triple("Fernando Muslera", PlayerPosition.GK, 82)),
            listOf("Fernando Muslera", "Emmanuel Eboue", "Semih Kaya", "Dany Nounkeu", "Albert Riera", "Felipe Melo", "Selcuk Inan", "Hamit Altintop", "Wesley Sneijder", "Burak Yilmaz", "Didier Drogba", "Eray Iscan", "Gokhan Zan", "Hakan Balta", "Yekta Kurtulus", "Nordin Amrabat", "Emre Colak", "Umut Bulut")
        ),
        ClubSeed("shakhtar", "Shakhtar Donetsk", "SHK", "europe", 4, 36, 18, "Donbass Arena", 52187, 0xFFFF6600, 0xFF000000, Formation.F_4231, 80, "Ukraine",
            listOf(Triple("Fernandinho", PlayerPosition.CM, 83), Triple("Darijo Srna", PlayerPosition.RB, 82), Triple("Luiz Adriano", PlayerPosition.ST, 80), Triple("Douglas Costa", PlayerPosition.RW, 81)),
            listOf("Andriy Pyatov", "Darijo Srna", "Olexandr Kucher", "Yaroslav Rakitskiy", "Razvan Rat", "Tomas Hubschman", "Fernandinho", "Douglas Costa", "Alex Teixeira", "Taison", "Luiz Adriano", "Anton Kanibolotskiy", "Dmytro Chygrynskiy", "Serhiy Kryvtsov", "Taras Stepanenko", "Ilsinho", "Eduardo", "Marko Devic")
        ),
        ClubSeed("celtic", "Celtic FC", "CEL", "europe", 3, 24, 12, "Celtic Park", 60411, 0xFF008000, 0xFFFFFFFF, Formation.F_442, 78, "Scotland",
            listOf(Triple("Victor Wanyama", PlayerPosition.CDM, 81), Triple("Fraser Forster", PlayerPosition.GK, 80), Triple("Gary Hooper", PlayerPosition.ST, 79)),
            listOf("Fraser Forster", "Mikael Lustig", "Efe Ambrose", "Kelvin Wilson", "Emilio Izaguirre", "James Forrest", "Scott Brown", "Victor Wanyama", "Kris Commons", "Georgios Samaras", "Gary Hooper", "Lukasz Zaluska", "Charlie Mulgrew", "Adam Matthews", "Joe Ledley", "Beram Kayal", "Tony Watt", "Anthony Stokes")
        )
    )

    // --- NATIONAL TEAMS (INTERNATIONAL TOURNAMENT) ---
    val nationalTeamSeeds = listOf(
        ClubSeed("nat_brazil", "Brazil", "BRA", "international", 5, 50, 0, "Maracana", 78838, 0xFFFFDF00, 0xFF009B3A, Formation.F_4231, 86, "Brazil",
            listOf(Triple("Neymar Jr", PlayerPosition.LW, 88), Triple("Thiago Silva", PlayerPosition.CB, 87), Triple("Dani Alves", PlayerPosition.RB, 85), Triple("Oscar", PlayerPosition.CAM, 83)),
            listOf("Julio Cesar", "Dani Alves", "Thiago Silva", "David Luiz", "Marcelo", "Paulinho", "Luiz Gustavo", "Hulk", "Oscar", "Neymar Jr", "Fred", "Jefferson", "Dante", "Filipe Luis", "Hernanes", "Lucas Moura", "Bernard", "Jo")
        ),
        ClubSeed("nat_spain", "Spain", "ESP", "international", 5, 50, 0, "Santiago Bernabeu", 81044, 0xFFD40026, 0xFFF1BF00, Formation.F_433, 88, "Spain",
            listOf(Triple("Andres Iniesta", PlayerPosition.CM, 90), Triple("Xavi", PlayerPosition.CM, 89), Triple("Sergio Ramos", PlayerPosition.CB, 87), Triple("Iker Casillas", PlayerPosition.GK, 88)),
            listOf("Iker Casillas", "Alvaro Arbeloa", "Sergio Ramos", "Gerard Pique", "Jordi Alba", "Sergio Busquets", "Xabi Alonso", "Xavi", "David Silva", "Cesc Fabregas", "Andres Iniesta", "Victor Valdes", "Raul Albiol", "Nacho Monreal", "Javi Martinez", "Santi Cazorla", "Pedro", "Fernando Torres")
        ),
        ClubSeed("nat_germany", "Germany", "GER", "international", 5, 50, 0, "Olympiastadion Berlin", 74475, 0xFFFFFFFF, 0xFF000000, Formation.F_4231, 87, "Germany",
            listOf(Triple("Manuel Neuer", PlayerPosition.GK, 89), Triple("Bastian Schweinsteiger", PlayerPosition.CM, 88), Triple("Philipp Lahm", PlayerPosition.RB, 88), Triple("Mesut Ozil", PlayerPosition.CAM, 87)),
            listOf("Manuel Neuer", "Philipp Lahm", "Jerome Boateng", "Mats Hummels", "Marcel Schmelzer", "Sami Khedira", "Bastian Schweinsteiger", "Thomas Muller", "Mesut Ozil", "Marco Reus", "Mario Gotze", "Rene Adler", "Per Mertesacker", "Benedikt Howedes", "Ilkay Gundogan", "Toni Kroos", "Lukas Podolski", "Mario Gomez")
        ),
        ClubSeed("nat_argentina", "Argentina", "ARG", "international", 5, 50, 0, "El Monumental", 70074, 0xFF75AADB, 0xFFFFFFFF, Formation.F_433, 86, "Argentina",
            listOf(Triple("Lionel Messi", PlayerPosition.RW, 94), Triple("Sergio Aguero", PlayerPosition.ST, 88), Triple("Angel Di Maria", PlayerPosition.LW, 86), Triple("Javier Mascherano", PlayerPosition.CDM, 85)),
            listOf("Sergio Romero", "Pablo Zabaleta", "Federico Fernandez", "Ezequiel Garay", "Marcos Rojo", "Javier Mascherano", "Fernando Gago", "Angel Di Maria", "Lionel Messi", "Gonzalo Higuain", "Sergio Aguero", "Mariano Andujar", "Hugo Campagnaro", "Jose Basanta", "Lucas Biglia", "Maxi Rodriguez", "Ezequiel Lavezzi", "Rodrigo Palacio")
        ),
        ClubSeed("nat_england", "England", "ENG", "international", 4, 50, 0, "Wembley Stadium", 90000, 0xFFFFFFFF, 0xFFD40026, Formation.F_442, 83, "England",
            listOf(Triple("Wayne Rooney", PlayerPosition.ST, 88), Triple("Steven Gerrard", PlayerPosition.CM, 85), Triple("Ashley Cole", PlayerPosition.LB, 84), Triple("Joe Hart", PlayerPosition.GK, 84)),
            listOf("Joe Hart", "Glen Johnson", "Gary Cahill", "Phil Jagielka", "Ashley Cole", "Theo Walcott", "Steven Gerrard", "Frank Lampard", "James Milner", "Wayne Rooney", "Danny Welbeck", "Ben Foster", "Joleon Lescott", "Leighton Baines", "Michael Carrick", "Jack Wilshere", "Alex Oxlade-Chamberlain", "Daniel Sturridge")
        ),
        ClubSeed("nat_france", "France", "FRA", "international", 4, 50, 0, "Stade de France", 81338, 0xFF002395, 0xFFFFFFFF, Formation.F_433, 84, "France",
            listOf(Triple("Franck Ribery", PlayerPosition.LW, 89), Triple("Karim Benzema", PlayerPosition.ST, 87), Triple("Hugo Lloris", PlayerPosition.GK, 86)),
            listOf("Hugo Lloris", "Mathieu Debuchy", "Raphael Varane", "Laurent Koscielny", "Patrice Evra", "Yohan Cabaye", "Blaise Matuidi", "Paul Pogba", "Mathieu Valbuena", "Karim Benzema", "Franck Ribery", "Steve Mandanda", "Mamadou Sakho", "Bacary Sagna", "Moussa Sissoko", "Samir Nasri", "Olivier Giroud", "Loic Remy")
        ),
        ClubSeed("nat_italy", "Italy", "ITA", "international", 4, 50, 0, "Stadio Olimpico", 70634, 0xFF0038A8, 0xFFFFFFFF, Formation.F_4312, 85, "Italy",
            listOf(Triple("Andrea Pirlo", PlayerPosition.CDM, 88), Triple("Gianluigi Buffon", PlayerPosition.GK, 88), Triple("Giorgio Chiellini", PlayerPosition.CB, 86), Triple("Mario Balotelli", PlayerPosition.ST, 84)),
            listOf("Gianluigi Buffon", "Ignazio Abate", "Andrea Barzagli", "Leonardo Bonucci", "Giorgio Chiellini", "Andrea Pirlo", "Daniele De Rossi", "Claudio Marchisio", "Riccardo Montolivo", "Mario Balotelli", "Stephan El Shaarawy", "Salvatore Sirigu", "Christian Maggio", "Mattia De Sciglio", "Thiago Motta", "Alessandro Florenzi", "Pablo Osvaldo", "Sebastian Giovinco")
        ),
        ClubSeed("nat_netherlands", "Netherlands", "NED", "international", 4, 50, 0, "De Kuip", 51117, 0xFFFF6600, 0xFFFFFFFF, Formation.F_433, 84, "Netherlands",
            listOf(Triple("Robin van Persie", PlayerPosition.ST, 89), Triple("Arjen Robben", PlayerPosition.RW, 88), Triple("Wesley Sneijder", PlayerPosition.CAM, 84)),
            listOf("Tim Krul", "Daryl Janmaat", "Stefan de Vrij", "Bruno Martins Indi", "Daley Blind", "Kevin Strootman", "Nigel de Jong", "Wesley Sneijder", "Arjen Robben", "Robin van Persie", "Jeremain Lens", "Michel Vorm", "Ron Vlaar", "Paul Verhaegh", "Jordy Clasie", "Rafael van der Vaart", "Dirk Kuyt", "Klaas-Jan Huntelaar")
        )
    )
}
