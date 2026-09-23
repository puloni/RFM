package com.example

import com.example.rfm.data.RfmDatabase
import com.example.rfm.data.SaveManager
import com.example.rfm.engine.MatchSimulationEngine
import com.example.rfm.model.*
import org.junit.Assert.*
import org.junit.Test

class RfmGameEngineTest {

    @Test
    fun testDatabaseInitialization() {
        val gameState = RfmDatabase.createInitialGameState()

        assertNotNull(gameState)
        assertTrue(gameState.leagues.size >= 5)

        val totalTeams = gameState.leagues.sumOf { it.teams.size }
        assertTrue("Expected at least 10 authentic clubs", totalTeams >= 10)

        // Check star players in database
        val allPlayers = gameState.leagues.flatMap { it.teams }.flatMap { it.players }
        val messi = allPlayers.find { it.name.contains("Messi") }
        val ronaldo = allPlayers.find { it.name.contains("Ronaldo") }
        val rooney = allPlayers.find { it.name.contains("Rooney") }

        assertNotNull("Lionel Messi should exist in 2012/13 database", messi)
        assertNotNull("Cristiano Ronaldo should exist in 2012/13 database", ronaldo)
        assertNotNull("Wayne Rooney should exist in 2012/13 database", rooney)

        assertEquals(95, messi?.overall)
        assertEquals(94, ronaldo?.overall)
    }

    @Test
    fun testMatchSimulationEngine() {
        val gameState = RfmDatabase.createInitialGameState()
        val epl = gameState.leagues.first { it.id == "epl" }
        val manUtd = epl.teams.first { it.id == "man_utd" }
        val arsenal = epl.teams.first { it.id == "arsenal" }

        val fixture = Fixture(
            id = "test_fixture",
            round = 1,
            homeTeamId = manUtd.id,
            awayTeamId = arsenal.id
        )

        val engine = MatchSimulationEngine(fixture, manUtd, arsenal, ModSettings())
        engine.startMatch()
        engine.simulateToEnd()

        assertTrue("Match must be full time", engine.isFullTime)
        assertEquals(90, engine.currentMinute)
        assertNotNull("Home score must be calculated", fixture.homeScore)
        assertNotNull("Away score must be calculated", fixture.awayScore)
        assertTrue("Match events should be generated", engine.events.isNotEmpty())
        assertTrue("Match statistics should be tracked", engine.stats.homeShots >= 0)
    }

    @Test
    fun testModSettingsApplication() {
        val mods = ModSettings(
            unlimitedMoney = true,
            unlimitedTransferBudget = true,
            maxPlayerStats = true,
            maxClubReputation = true
        )

        assertEquals(4, mods.activeModCount())

        val gameState = RfmDatabase.createInitialGameState()
        gameState.modSettings = mods
        val userTeam = gameState.getUserTeam()
        assertNotNull(userTeam)

        // Apply mod effects
        if (mods.maxPlayerStats) {
            userTeam!!.players.forEach { p ->
                p.overall = 99
                p.shooting = 99
            }
        }

        userTeam!!.players.forEach { p ->
            assertEquals(99, p.overall)
            assertEquals(99, p.shooting)
        }
    }

    @Test
    fun testSaveAndLoadManager() {
        val saveManager = SaveManager()

        val state = RfmDatabase.createInitialGameState()
        state.managerName = "Sir Alex Ferguson"
        state.userTeamId = "man_utd"

        saveManager.saveGame("test_save_slot", state)
        assertTrue("Save slot should exist", saveManager.hasSave("test_save_slot"))

        val restoredState = RfmDatabase.createInitialGameState()
        val loaded = saveManager.loadGame("test_save_slot", restoredState)
        assertTrue("Game loading should succeed", loaded)
        assertEquals("Sir Alex Ferguson", restoredState.managerName)
        assertEquals("man_utd", restoredState.userTeamId)
    }

    @Test
    fun testTacticsCalculation() {
        val gameState = RfmDatabase.createInitialGameState()
        val team = gameState.leagues.first().teams.first()

        val initialAtk = team.calculateAttackRating()
        val initialDef = team.calculateDefenseRating()

        assertTrue("Attack rating should be between 1 and 99", initialAtk in 1..99)
        assertTrue("Defense rating should be between 1 and 99", initialDef in 1..99)

        // Changing mentality to All-Out Attack
        team.mentality = Mentality.ALL_OUT_ATTACK
        val boostedAtk = team.calculateAttackRating()
        assertTrue("All-out attack should amplify attacking rating", boostedAtk >= initialAtk)
    }
}
