package com.example.rfm.model

import kotlin.random.Random

/**
 * Weather conditions during match simulation, impacting ball roll,
 * stamina degradation, passing accuracy, and injury risks.
 */
enum class MatchWeather(
    val label: String,
    val icon: String,
    val description: String,
    val tempCelsius: Int,
    val ballSpeedMultiplier: Float = 1.0f,
    val fatigueDrainMultiplier: Float = 1.0f,
    val passAccuracyModifier: Int = 0,
    val deflectionChanceBonus: Float = 0.0f,
    val injuryRiskMultiplier: Float = 1.0f
) {
    CLEAR(
        label = "Clear",
        icon = "☀️",
        description = "Ideal conditions with a crisp, dry pitch and optimal ball control.",
        tempCelsius = 19,
        ballSpeedMultiplier = 1.0f,
        fatigueDrainMultiplier = 1.0f,
        passAccuracyModifier = 0,
        deflectionChanceBonus = 0.0f,
        injuryRiskMultiplier = 1.0f
    ),
    RAIN(
        label = "Heavy Rain",
        icon = "🌧️",
        description = "Slick, wet turf causing ball skidding, spilled saves, and defensive slips.",
        tempCelsius = 12,
        ballSpeedMultiplier = 1.2f,
        fatigueDrainMultiplier = 1.15f,
        passAccuracyModifier = -5,
        deflectionChanceBonus = 0.25f,
        injuryRiskMultiplier = 1.2f
    ),
    SNOW(
        label = "Snow & Freezing",
        icon = "❄️",
        description = "Frozen, heavy ground demanding intense physical endurance and slowing down play.",
        tempCelsius = -2,
        ballSpeedMultiplier = 0.85f,
        fatigueDrainMultiplier = 1.35f,
        passAccuracyModifier = -8,
        deflectionChanceBonus = 0.15f,
        injuryRiskMultiplier = 1.3f
    ),
    MUDDY(
        label = "Muddy Pitch",
        icon = "🌧️",
        description = "Cut-up, waterlogged pitch hindering dribblers and favoring long balls.",
        tempCelsius = 10,
        ballSpeedMultiplier = 0.8f,
        fatigueDrainMultiplier = 1.3f,
        passAccuracyModifier = -10,
        deflectionChanceBonus = 0.2f,
        injuryRiskMultiplier = 1.35f
    ),
    HOT(
        label = "Sweltering Heat",
        icon = "🔥",
        description = "Oppressive summer heat draining player stamina rapidly in the second half.",
        tempCelsius = 31,
        ballSpeedMultiplier = 1.0f,
        fatigueDrainMultiplier = 1.45f,
        passAccuracyModifier = -2,
        deflectionChanceBonus = 0.0f,
        injuryRiskMultiplier = 1.15f
    );

    companion object {
        fun generateRandom(): MatchWeather {
            val roll = Random.nextInt(100)
            return when {
                roll < 55 -> CLEAR
                roll < 80 -> RAIN
                roll < 88 -> MUDDY
                roll < 95 -> HOT
                else -> SNOW
            }
        }
    }
}
