package com.example.rfm.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.rfm.data.RfmDatabase
import com.example.rfm.model.GameState
import com.example.rfm.model.ModSettings
import org.json.JSONArray
import org.json.JSONObject

object SaveManager {

    private const val PREFS_NAME = "rfm_2013_save_data"
    private const val KEY_SLOT_PREFIX = "save_slot_"
    private const val KEY_AUTOSAVE = "autosave_slot"
    private const val KEY_MOD_SETTINGS = "mod_settings"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveGame(context: Context, state: GameState, slotId: Int = 0): Boolean {
        return try {
            val prefs = getPrefs(context)
            val json = serializeState(state)
            val key = if (slotId == 0) KEY_AUTOSAVE else "$KEY_SLOT_PREFIX$slotId"
            prefs.edit().putString(key, json.toString()).apply()
            saveModSettings(context, state.modSettings)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasSave(context: Context, slotId: Int = 0): Boolean {
        val prefs = getPrefs(context)
        val key = if (slotId == 0) KEY_AUTOSAVE else "$KEY_SLOT_PREFIX$slotId"
        return prefs.contains(key)
    }

    fun loadGame(context: Context, slotId: Int = 0): GameState? {
        val prefs = getPrefs(context)
        val key = if (slotId == 0) KEY_AUTOSAVE else "$KEY_SLOT_PREFIX$slotId"
        val raw = prefs.getString(key, null) ?: return null
        return try {
            val json = JSONObject(raw)
            val state = RfmDatabase.createInitialGameState()
            state.userTeamId = json.optString("userTeamId", state.userTeamId)
            state.managerName = json.optString("managerName", state.managerName)
            state.currentMatchDay = json.optInt("currentMatchDay", state.currentMatchDay)
            state.boardConfidence = json.optInt("boardConfidence", state.boardConfidence)
            state.managerReputation = json.optInt("managerReputation", state.managerReputation)

            // Load user team balance and budgets
            val userTeam = state.getUserTeam()
            if (userTeam != null) {
                userTeam.balanceEuro = json.optLong("userBalance", userTeam.balanceEuro)
                userTeam.transferBudgetEuro = json.optLong("userTransferBudget", userTeam.transferBudgetEuro)
                userTeam.stadiumLevel = json.optInt("stadiumLevel", userTeam.stadiumLevel)
                userTeam.trainingFacilityLevel = json.optInt("trainingLevel", userTeam.trainingFacilityLevel)
                userTeam.medicalFacilityLevel = json.optInt("medicalLevel", userTeam.medicalFacilityLevel)
                userTeam.youthFacilityLevel = json.optInt("youthLevel", userTeam.youthFacilityLevel)
            }

            // Load mod settings
            state.modSettings = loadModSettings(context)
            state
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveModSettings(context: Context, mod: ModSettings) {
        val prefs = getPrefs(context)
        val json = JSONObject().apply {
            put("unlimitedMoney", mod.unlimitedMoney)
            put("unlimitedTransferBudget", mod.unlimitedTransferBudget)
            put("unlimitedClubResources", mod.unlimitedClubResources)
            put("maxPlayerStats", mod.maxPlayerStats)
            put("maxPlayerDevelopment", mod.maxPlayerDevelopment)
            put("noPlayerFatigue", mod.noPlayerFatigue)
            put("noPlayerInjury", mod.noPlayerInjury)
            put("unlimitedTraining", mod.unlimitedTraining)
            put("instantTraining", mod.instantTraining)
            put("maxClubReputation", mod.maxClubReputation)
            put("maxManagerReputation", mod.maxManagerReputation)
            put("unlockContent", mod.unlockContent)
            put("disableResourceConsumption", mod.disableResourceConsumption)
            put("disableFinancialPenalties", mod.disableFinancialPenalties)
            put("disableFatigueEffects", mod.disableFatigueEffects)
        }
        prefs.edit().putString(KEY_MOD_SETTINGS, json.toString()).apply()
    }

    fun loadModSettings(context: Context): ModSettings {
        val prefs = getPrefs(context)
        val raw = prefs.getString(KEY_MOD_SETTINGS, null) ?: return ModSettings()
        return try {
            val json = JSONObject(raw)
            ModSettings(
                unlimitedMoney = json.optBoolean("unlimitedMoney", false),
                unlimitedTransferBudget = json.optBoolean("unlimitedTransferBudget", false),
                unlimitedClubResources = json.optBoolean("unlimitedClubResources", false),
                maxPlayerStats = json.optBoolean("maxPlayerStats", false),
                maxPlayerDevelopment = json.optBoolean("maxPlayerDevelopment", false),
                noPlayerFatigue = json.optBoolean("noPlayerFatigue", false),
                noPlayerInjury = json.optBoolean("noPlayerInjury", false),
                unlimitedTraining = json.optBoolean("unlimitedTraining", false),
                instantTraining = json.optBoolean("instantTraining", false),
                maxClubReputation = json.optBoolean("maxClubReputation", false),
                maxManagerReputation = json.optBoolean("maxManagerReputation", false),
                unlockContent = json.optBoolean("unlockContent", false),
                disableResourceConsumption = json.optBoolean("disableResourceConsumption", false),
                disableFinancialPenalties = json.optBoolean("disableFinancialPenalties", false),
                disableFatigueEffects = json.optBoolean("disableFatigueEffects", false)
            )
        } catch (_: Exception) {
            ModSettings()
        }
    }

    private fun serializeState(state: GameState): JSONObject {
        val json = JSONObject()
        json.put("userTeamId", state.userTeamId)
        json.put("managerName", state.managerName)
        json.put("currentMatchDay", state.currentMatchDay)
        json.put("boardConfidence", state.boardConfidence)
        json.put("managerReputation", state.managerReputation)

        val userTeam = state.getUserTeam()
        if (userTeam != null) {
            json.put("userBalance", userTeam.balanceEuro)
            json.put("userTransferBudget", userTeam.transferBudgetEuro)
            json.put("stadiumLevel", userTeam.stadiumLevel)
            json.put("trainingLevel", userTeam.trainingFacilityLevel)
            json.put("medicalLevel", userTeam.medicalFacilityLevel)
            json.put("youthLevel", userTeam.youthFacilityLevel)
        }
        return json
    }
}
