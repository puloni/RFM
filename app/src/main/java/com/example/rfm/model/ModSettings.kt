package com.example.rfm.model

data class ModSettings(
    // Economy
    var unlimitedMoney: Boolean = false,
    var unlimitedTransferBudget: Boolean = false,
    var unlimitedClubResources: Boolean = false,

    // Player
    var maxPlayerStats: Boolean = false,
    var maxPlayerDevelopment: Boolean = false,
    var noPlayerFatigue: Boolean = false,
    var noPlayerInjury: Boolean = false,

    // Management
    var unlimitedTraining: Boolean = false,
    var instantTraining: Boolean = false,
    var maxClubReputation: Boolean = false,
    var maxManagerReputation: Boolean = false,

    // Unlocks
    var unlockContent: Boolean = false,

    // Gameplay
    var disableResourceConsumption: Boolean = false,
    var disableFinancialPenalties: Boolean = false,
    var disableFatigueEffects: Boolean = false,

    // Visuals & Retro Feel
    var crtRetroFilter: Boolean = false
) {
    fun activeModCount(): Int {
        var count = 0
        if (unlimitedMoney) count++
        if (unlimitedTransferBudget) count++
        if (unlimitedClubResources) count++
        if (maxPlayerStats) count++
        if (maxPlayerDevelopment) count++
        if (noPlayerFatigue) count++
        if (noPlayerInjury) count++
        if (unlimitedTraining) count++
        if (instantTraining) count++
        if (maxClubReputation) count++
        if (maxManagerReputation) count++
        if (unlockContent) count++
        if (disableResourceConsumption) count++
        if (disableFinancialPenalties) count++
        if (disableFatigueEffects) count++
        if (crtRetroFilter) count++
        return count
    }
}
