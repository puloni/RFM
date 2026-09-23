package com.example.rfm.data

import android.content.Context
import com.example.rfm.model.GameState
import com.example.rfm.model.ModSettings
import org.json.JSONObject

/**
 * Manages persistent storage for Real Football Manager 2013 game saves.
 * Provides multiple save slots, autosave, and mod settings persistence.
 */
class SaveManager(private val context: Context? = null) {
    private val memoryStorage = mutableMapOf<String, String>()
    private val prefs: android.content.SharedPreferences? by lazy {
        context?.getSharedPreferences("rfm_2013_saves", Context.MODE_PRIVATE)
    }

    fun saveGame(slot: String, gameState: GameState) {
        val json = JSONObject()
        json.put("userTeamId", gameState.userTeamId)
        json.put("managerName", gameState.managerName)
        json.put("currentSeasonYear", gameState.currentSeasonYear)
        json.put("currentMatchDay", gameState.currentMatchDay)
        json.put("boardConfidence", gameState.boardConfidence)
        json.put("managerReputation", gameState.managerReputation)
        json.put("isRetroKeypadEnabled", gameState.isRetroKeypadEnabled)
        json.put("isSoundEnabled", gameState.isSoundEnabled)
        json.put("currentTrainingFocus", gameState.currentTrainingFocus)

        // Save user team finances & facility levels
        val userTeam = gameState.getUserTeam()
        if (userTeam != null) {
            json.put("balanceEuro", userTeam.balanceEuro)
            json.put("transferBudgetEuro", userTeam.transferBudgetEuro)
            json.put("stadiumLevel", userTeam.stadiumLevel)
            json.put("stadiumCapacity", userTeam.stadiumCapacity)
            json.put("trainingFacilityLevel", userTeam.trainingFacilityLevel)
            json.put("medicalFacilityLevel", userTeam.medicalFacilityLevel)
            json.put("youthFacilityLevel", userTeam.youthFacilityLevel)
            json.put("formation", userTeam.formation.name)
            json.put("mentality", userTeam.mentality.name)
        }

        // Trophies Won
        val trophiesArray = org.json.JSONArray()
        gameState.trophiesWon.forEach { trophiesArray.put(it) }
        json.put("trophiesWon", trophiesArray)

        // Save Mod Settings
        val modJson = JSONObject()
        val m = gameState.modSettings
        modJson.put("unlimitedMoney", m.unlimitedMoney)
        modJson.put("unlimitedTransferBudget", m.unlimitedTransferBudget)
        modJson.put("unlimitedClubResources", m.unlimitedClubResources)
        modJson.put("maxPlayerStats", m.maxPlayerStats)
        modJson.put("maxPlayerDevelopment", m.maxPlayerDevelopment)
        modJson.put("noPlayerFatigue", m.noPlayerFatigue)
        modJson.put("noPlayerInjury", m.noPlayerInjury)
        modJson.put("unlimitedTraining", m.unlimitedTraining)
        modJson.put("instantTraining", m.instantTraining)
        modJson.put("maxClubReputation", m.maxClubReputation)
        modJson.put("maxManagerReputation", m.maxManagerReputation)
        modJson.put("unlockContent", m.unlockContent)
        modJson.put("disableResourceConsumption", m.disableResourceConsumption)
        modJson.put("disableFinancialPenalties", m.disableFinancialPenalties)
        modJson.put("disableFatigueEffects", m.disableFatigueEffects)
        json.put("modSettings", modJson)

        val p = prefs
        if (p != null) {
            p.edit().putString("slot_$slot", json.toString()).apply()
        } else {
            memoryStorage["slot_$slot"] = json.toString()
        }
    }

    fun hasSave(slot: String): Boolean {
        val p = prefs
        return p?.contains("slot_$slot") ?: memoryStorage.containsKey("slot_$slot")
    }

    fun loadGame(slot: String, baseState: GameState): Boolean {
        val p = prefs
        val dataStr = (p?.getString("slot_$slot", null) ?: memoryStorage["slot_$slot"]) ?: return false
        try {
            val json = JSONObject(dataStr)
            baseState.userTeamId = json.optString("userTeamId", baseState.userTeamId)
            baseState.managerName = json.optString("managerName", baseState.managerName)
            baseState.currentSeasonYear = json.optInt("currentSeasonYear", baseState.currentSeasonYear)
            baseState.currentMatchDay = json.optInt("currentMatchDay", baseState.currentMatchDay)
            baseState.boardConfidence = json.optInt("boardConfidence", baseState.boardConfidence)
            baseState.managerReputation = json.optInt("managerReputation", baseState.managerReputation)
            baseState.isRetroKeypadEnabled = json.optBoolean("isRetroKeypadEnabled", baseState.isRetroKeypadEnabled)
            baseState.isSoundEnabled = json.optBoolean("isSoundEnabled", baseState.isSoundEnabled)
            baseState.currentTrainingFocus = json.optString("currentTrainingFocus", baseState.currentTrainingFocus)

            val userTeam = baseState.getUserTeam()
            if (userTeam != null) {
                userTeam.balanceEuro = json.optLong("balanceEuro", userTeam.balanceEuro)
                userTeam.transferBudgetEuro = json.optLong("transferBudgetEuro", userTeam.transferBudgetEuro)
                userTeam.stadiumLevel = json.optInt("stadiumLevel", userTeam.stadiumLevel)
                userTeam.stadiumCapacity = json.optInt("stadiumCapacity", userTeam.stadiumCapacity)
                userTeam.trainingFacilityLevel = json.optInt("trainingFacilityLevel", userTeam.trainingFacilityLevel)
                userTeam.medicalFacilityLevel = json.optInt("medicalFacilityLevel", userTeam.medicalFacilityLevel)
                userTeam.youthFacilityLevel = json.optInt("youthFacilityLevel", userTeam.youthFacilityLevel)
            }

            // Restore mod settings
            if (json.has("trophiesWon")) {
                val trophiesArray = json.getJSONArray("trophiesWon")
                baseState.trophiesWon.clear()
                for (i in 0 until trophiesArray.length()) {
                    baseState.trophiesWon.add(trophiesArray.getString(i))
                }
            }

            if (json.has("modSettings")) {
                val modJson = json.getJSONObject("modSettings")
                val m = ModSettings(
                    unlimitedMoney = modJson.optBoolean("unlimitedMoney", false),
                    unlimitedTransferBudget = modJson.optBoolean("unlimitedTransferBudget", false),
                    unlimitedClubResources = modJson.optBoolean("unlimitedClubResources", false),
                    maxPlayerStats = modJson.optBoolean("maxPlayerStats", false),
                    maxPlayerDevelopment = modJson.optBoolean("maxPlayerDevelopment", false),
                    noPlayerFatigue = modJson.optBoolean("noPlayerFatigue", false),
                    noPlayerInjury = modJson.optBoolean("noPlayerInjury", false),
                    unlimitedTraining = modJson.optBoolean("unlimitedTraining", false),
                    instantTraining = modJson.optBoolean("instantTraining", false),
                    maxClubReputation = modJson.optBoolean("maxClubReputation", false),
                    maxManagerReputation = modJson.optBoolean("maxManagerReputation", false),
                    unlockContent = modJson.optBoolean("unlockContent", false),
                    disableResourceConsumption = modJson.optBoolean("disableResourceConsumption", false),
                    disableFinancialPenalties = modJson.optBoolean("disableFinancialPenalties", false),
                    disableFatigueEffects = modJson.optBoolean("disableFatigueEffects", false)
                )
                baseState.modSettings = m
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun clearSave(slot: String) {
        val p = prefs
        if (p != null) {
            p.edit().remove("slot_$slot").apply()
        } else {
            memoryStorage.remove("slot_$slot")
        }
    }
}
