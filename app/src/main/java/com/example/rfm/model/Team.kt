package com.example.rfm.model

enum class Formation(val label: String, val slots: List<PlayerPosition>) {
    F_442("4-4-2", listOf(
        PlayerPosition.GK,
        PlayerPosition.LB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.RB,
        PlayerPosition.LM, PlayerPosition.CM, PlayerPosition.CM, PlayerPosition.RM,
        PlayerPosition.ST, PlayerPosition.ST
    )),
    F_433("4-3-3", listOf(
        PlayerPosition.GK,
        PlayerPosition.LB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.RB,
        PlayerPosition.CM, PlayerPosition.CDM, PlayerPosition.CM,
        PlayerPosition.LW, PlayerPosition.ST, PlayerPosition.RW
    )),
    F_4231("4-2-3-1", listOf(
        PlayerPosition.GK,
        PlayerPosition.LB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.RB,
        PlayerPosition.CDM, PlayerPosition.CDM,
        PlayerPosition.LM, PlayerPosition.CAM, PlayerPosition.RM,
        PlayerPosition.ST
    )),
    F_352("3-5-2", listOf(
        PlayerPosition.GK,
        PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.CB,
        PlayerPosition.LM, PlayerPosition.CM, PlayerPosition.CDM, PlayerPosition.CM, PlayerPosition.RM,
        PlayerPosition.ST, PlayerPosition.ST
    )),
    F_532("5-3-2", listOf(
        PlayerPosition.GK,
        PlayerPosition.LB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.RB,
        PlayerPosition.CM, PlayerPosition.CM, PlayerPosition.CM,
        PlayerPosition.ST, PlayerPosition.ST
    )),
    F_4312("4-3-1-2", listOf(
        PlayerPosition.GK,
        PlayerPosition.LB, PlayerPosition.CB, PlayerPosition.CB, PlayerPosition.RB,
        PlayerPosition.CM, PlayerPosition.CDM, PlayerPosition.CM,
        PlayerPosition.CAM,
        PlayerPosition.ST, PlayerPosition.ST
    ))
}

enum class Mentality(val label: String, val attackBonus: Int, val defBonus: Int) {
    ULTRA_DEFENSIVE("Ultra Defensive", -15, 20),
    DEFENSIVE("Defensive", -7, 10),
    BALANCED("Balanced", 0, 0),
    ATTACKING("Attacking", 10, -7),
    ALL_OUT_ATTACK("All-Out Attack", 20, -15)
}

enum class PassingStyle(val label: String) {
    SHORT("Short (Tiki-Taka)"),
    DIRECT("Direct"),
    LONG_BALL("Long Ball"),
    MIXED("Mixed")
}

enum class PressingStyle(val label: String) {
    OWN_HALF("Own Half"),
    STANDARD("Standard"),
    HIGH_PRESS("High Press")
}

enum class TacklingStyle(val label: String, val cardRisk: Float) {
    CAUTIOUS("Cautious", 0.5f),
    NORMAL("Normal", 1.0f),
    AGGRESSIVE("Aggressive", 1.8f)
}

enum class TempoStyle(val label: String) {
    SLOWER("Slower Tempo"),
    BALANCED("Standard Tempo"),
    FAST("Fast Tempo"),
    VERY_FAST("High Tempo")
}

enum class MarkingStyle(val label: String) {
    ZONAL("Zonal Marking"),
    MAN_TO_MAN("Man to Man")
}

enum class TicketPriceTier(val label: String, val priceEuro: Int, val attendanceMultiplier: Float) {
    ECONOMY("Economy (€25)", 25, 1.15f),
    STANDARD("Standard (€40)", 40, 1.00f),
    PREMIUM("Premium (€65)", 65, 0.88f),
    VIP("VIP Corporate (€110)", 110, 0.72f)
}

data class SponsorContract(
    val type: String,
    val sponsorName: String,
    val weeklyIncomeEuro: Long,
    val contractDurationYears: Int
)

enum class SeasonObjective(val label: String, val targetPosition: Int) {
    TITLE_CONTENDER("Win the League / Title Contender", 1),
    TOP_FOUR("Champions Cup Qualification (Top 4)", 4),
    MID_TABLE("Top 10 Finish", 10),
    AVOID_RELEGATION("Avoid Relegation", 17)
}

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val leagueId: String,
    var reputationStars: Int = 4, // 1 to 5
    var balanceEuro: Long = 50_000_000L,
    var transferBudgetEuro: Long = 30_000_000L,
    var wageBudgetEuroWeekly: Long = 1_500_000L,
    val stadiumName: String = "Arena",
    var stadiumCapacity: Int = 45_000,
    var stadiumLevel: Int = 2,
    var trainingFacilityLevel: Int = 2,
    var medicalFacilityLevel: Int = 2,
    var youthFacilityLevel: Int = 2,
    var formation: Formation = Formation.F_442,
    var mentality: Mentality = Mentality.BALANCED,
    var passingStyle: PassingStyle = PassingStyle.MIXED,
    var pressingStyle: PressingStyle = PressingStyle.STANDARD,
    var tacklingStyle: TacklingStyle = TacklingStyle.NORMAL,
    var tempoStyle: TempoStyle = TempoStyle.BALANCED,
    var markingStyle: MarkingStyle = MarkingStyle.ZONAL,
    var offsideTrap: Boolean = false,
    var ticketPriceTier: TicketPriceTier = TicketPriceTier.STANDARD,
    var shirtSponsor: SponsorContract = SponsorContract("Shirt Sponsor", "Aon Commercial", 350_000L, 3),
    var stadiumSponsor: SponsorContract = SponsorContract("Stadium Naming", "DHL Logistics", 200_000L, 4),
    var seasonObjective: SeasonObjective = SeasonObjective.TITLE_CONTENDER,
    val primaryColorHex: Long = 0xFF0D47A1,
    val secondaryColorHex: Long = 0xFFFFFFFF,
    val players: MutableList<Player> = mutableListOf(),
    var captainPlayerId: String? = null,
    var penaltyTakerId: String? = null,
    var freeKickTakerId: String? = null,
    var cornerTakerId: String? = null
) {
    fun getStartingXI(): List<Player> = players.filter { it.isStarting }.take(11)
    fun getSubstitutes(): List<Player> = players.filter { it.isSubstitute }.take(7)
    fun getReserves(): List<Player> = players.filter { !it.isStarting && !it.isSubstitute }

    fun calculateTeamStrength(): Int {
        val starting = getStartingXI()
        if (starting.isEmpty()) return 70
        return (starting.sumOf { it.overall } / starting.size.toDouble()).toInt()
    }

    val averageOverall: Int get() = calculateTeamStrength()

    fun calculateAttackRating(): Int {
        val starting = getStartingXI()
        if (starting.isEmpty()) return 70
        val attackers = starting.filter { it.position.roleCategory == "Forward" || it.position == PlayerPosition.CAM }
        val pool = attackers.ifEmpty { starting }
        return (pool.sumOf { (it.shooting + it.pace + it.dribbling) / 3 } / pool.size) + mentality.attackBonus
    }

    fun calculateMidfieldRating(): Int {
        val starting = getStartingXI()
        if (starting.isEmpty()) return 70
        val mids = starting.filter { it.position.roleCategory == "Midfielder" }
        val pool = mids.ifEmpty { starting }
        return pool.sumOf { (it.passing + it.dribbling + it.overall) / 3 } / pool.size
    }

    fun calculateDefenseRating(): Int {
        val starting = getStartingXI()
        if (starting.isEmpty()) return 70
        val defs = starting.filter { it.position.roleCategory == "Defender" || it.position == PlayerPosition.GK }
        val pool = defs.ifEmpty { starting }
        return (pool.sumOf { (it.defending + it.physical + it.overall) / 3 } / pool.size) + mentality.defBonus
    }
}
