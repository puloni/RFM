package com.example.rfm.model

data class YouthProspect(
    val id: String,
    val name: String,
    val position: PlayerPosition,
    val age: Int,
    val nationality: String,
    val overall: Int,
    val potential: Int,
    val valueEuro: Long,
    val wageWeeklyEuro: Long,
    val specialty: String = "Promising Talent"
) {
    fun toPlayer(): Player {
        return Player(
            id = id,
            name = name,
            position = position,
            age = age,
            nationality = nationality,
            overall = overall,
            pace = (overall + (-3..5).random()).coerceIn(55, 95),
            shooting = (overall + (-5..4).random()).coerceIn(50, 92),
            passing = (overall + (-4..4).random()).coerceIn(55, 92),
            dribbling = (overall + (-3..5).random()).coerceIn(55, 94),
            defending = if (position.roleCategory == "Defender") (overall + 2).coerceAtMost(92) else (overall - 15).coerceAtLeast(35),
            physical = (overall + (-4..6).random()).coerceIn(55, 94),
            potential = potential,
            valueEuro = valueEuro,
            wageWeeklyEuro = wageWeeklyEuro,
            contractYears = 4,
            isStarting = false,
            isSubstitute = false
        )
    }
}
