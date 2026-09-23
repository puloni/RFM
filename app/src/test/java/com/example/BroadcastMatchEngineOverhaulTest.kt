package com.example

import com.example.rfm.data.RfmDatabase
import com.example.rfm.engine.*
import com.example.rfm.model.*
import org.junit.Assert.*
import org.junit.Test

class BroadcastMatchEngineOverhaulTest {

    @Test
    fun test30ZonePositionalGrid() {
        // 1. Verify 30 distinct zones (6 bands x 5 channels)
        assertEquals(30, PitchSpatialGrid.zones.size)

        // 2. Verify Zone 14 exists centrally in attacking third
        val zone14 = PitchSpatialGrid.zones.find { it.band == PitchZoneBand.ATT_THIRD && it.channel == PitchZoneChannel.CENTRAL_POCKET }
        assertNotNull("Zone 14 must exist in attacking central pocket", zone14)
        assertTrue(zone14!!.isCentral)
        assertEquals("Zone 14 (Central Pocket)", zone14.name)

        // 3. Verify half-space zones
        val leftHalfSpaceAtt = PitchSpatialGrid.zones.find { it.band == PitchZoneBand.ATT_THIRD && it.channel == PitchZoneChannel.LEFT_HALF_SPACE }
        assertNotNull(leftHalfSpaceAtt)
        assertTrue(leftHalfSpaceAtt!!.isHalfSpace)

        // 4. Verify coordinate mapping
        val centerZone = PitchSpatialGrid.getZoneForCoordinates(0.5f, 0.5f)
        assertEquals(PitchZoneBand.MID_ATT, centerZone.band)
        assertEquals(PitchZoneChannel.CENTRAL_POCKET, centerZone.channel)

        // 5. Test zone overload calculation
        val homeCoords = listOf(Pair(0.70f, 0.30f), Pair(0.72f, 0.32f), Pair(0.68f, 0.28f)) // 3 players in Left Half-Space
        val awayCoords = listOf(Pair(0.70f, 0.30f)) // 1 player
        val overloads = PitchSpatialGrid.calculateZoneOverload(leftHalfSpaceAtt, homeCoords, awayCoords)
        assertTrue("Home should overload Left Half-Space (3 vs 1)", overloads.first > overloads.second)
    }

    @Test
    fun testTrueExpectedGoalsModel() {
        val gameState = RfmDatabase.createInitialGameState()
        val rvp = gameState.leagues.first().teams.first { it.id == "man_utd" }.players.first { it.name.contains("Persie") }

        // Test 1: Close range tap-in vs Long-range shot
        val tapInXg = ShotQualityModel.calculateXg(
            shotX = 0.96f,
            shotY = 0.50f,
            isAttackingRight = true,
            shotType = ShotType.TAP_IN,
            defendersInCone = 0,
            defensivePressure = 0.0f,
            isWeakFoot = false,
            shooter = rvp
        )

        val longRangeXg = ShotQualityModel.calculateXg(
            shotX = 0.65f,
            shotY = 0.20f,
            isAttackingRight = true,
            shotType = ShotType.OPEN_PLAY,
            defendersInCone = 3,
            defensivePressure = 0.8f,
            isWeakFoot = true,
            shooter = rvp
        )

        assertTrue("Tap-in xG (${tapInXg.xg}) must be significantly higher than contested long-range shot (${longRangeXg.xg})", tapInXg.xg > longRangeXg.xg)
        assertTrue("Tap-in xG should be >= 0.50", tapInXg.xg >= 0.50f)
        assertTrue("Long range contested shot xG should be <= 0.15", longRangeXg.xg <= 0.15f)

        // Test 2: Angle factor
        val centerBoxXg = ShotQualityModel.calculateXg(
            shotX = 0.88f,
            shotY = 0.50f,
            isAttackingRight = true,
            shotType = ShotType.OPEN_PLAY,
            defendersInCone = 1,
            defensivePressure = 0.3f,
            isWeakFoot = false,
            shooter = rvp
        )

        val tightAngleXg = ShotQualityModel.calculateXg(
            shotX = 0.88f,
            shotY = 0.15f, // very tight angle on the wing
            isAttackingRight = true,
            shotType = ShotType.OPEN_PLAY,
            defendersInCone = 1,
            defensivePressure = 0.3f,
            isWeakFoot = false,
            shooter = rvp
        )

        assertTrue("Central shot must have higher xG than tight angle shot from same distance", centerBoxXg.xg > tightAngleXg.xg)
    }

    @Test
    fun testAttributeWeightedMicroDuels() {
        val gameState = RfmDatabase.createInitialGameState()
        val epl = gameState.leagues.first { it.id == "epl" }
        val manUtd = epl.teams.first { it.id == "man_utd" }

        // Fast dribbling winger vs slow defender
        val fastWinger = Player(
            id = "winger_1",
            name = "Speedy Winger",
            position = PlayerPosition.LW,
            age = 22,
            nationality = "Spain",
            overall = 85,
            pace = 94,
            shooting = 78,
            passing = 80,
            dribbling = 92,
            defending = 35,
            physical = 65,
            condition = 100
        )

        val slowFullback = Player(
            id = "fb_1",
            name = "Slow Fullback",
            position = PlayerPosition.RB,
            age = 33,
            nationality = "England",
            overall = 72,
            pace = 58,
            shooting = 50,
            passing = 65,
            dribbling = 60,
            defending = 74,
            physical = 70,
            condition = 60
        )

        // Multiple duel iterations should heavily favor the fast winger
        var wingerWins = 0
        for (i in 1..25) {
            val result = MicroDuelEngine.resolveWingerVsFullback(fastWinger, slowFullback, TacklingStyle.NORMAL)
            if (result.attackerWon) wingerWins++
        }
        assertTrue("Fast dribbling winger should win vast majority of duels against slow tired defender", wingerWins >= 18)

        // Playmaker Vision test
        val playmaker = Player(
            id = "playmaker_1",
            name = "Maestro CM",
            position = PlayerPosition.CAM,
            age = 26,
            nationality = "Germany",
            overall = 89,
            pace = 72,
            shooting = 82,
            passing = 93,
            dribbling = 88,
            defending = 55,
            physical = 68
        )

        val striker = Player(
            id = "striker_1",
            name = "Goal Poacher",
            position = PlayerPosition.ST,
            age = 24,
            nationality = "France",
            overall = 84,
            pace = 88,
            shooting = 86,
            passing = 70,
            dribbling = 80,
            defending = 30,
            physical = 76
        )

        var penetrationsFound = 0
        for (i in 1..25) {
            val opp = MicroDuelEngine.evaluatePlaymakerVision(playmaker, listOf(playmaker, striker), manUtd.players)
            if (opp != null) penetrationsFound++
        }
        assertTrue("High PAS + DRI playmaker should recognize and attempt penetrating through balls", penetrationsFound > 0)
    }

    @Test
    fun testAdaptiveAiManagerCounterTactics() {
        val gameState = RfmDatabase.createInitialGameState()
        val epl = gameState.leagues.first { it.id == "epl" }
        val userTeam = epl.teams.first { it.id == "man_utd" }
        val aiTeam = epl.teams.first { it.id == "chelsea" }

        // Test Scenario 1: AI trailing by 1 at minute 72 -> All-Out Attack
        val trailingShift = AiAdaptiveManager.evaluateAndAdapt(
            currentMinute = 72,
            aiTeam = aiTeam,
            userTeam = userTeam,
            aiScore = 0,
            userScore = 1,
            userPossession = 50,
            aiSubsUsed = 0
        )

        assertNotNull("AI Manager must adapt when trailing late in match", trailingShift)
        assertEquals(Mentality.ALL_OUT_ATTACK, trailingShift!!.newMentality)
        assertEquals(PressingStyle.HIGH_PRESS, trailingShift.newPressingStyle)
        assertTrue(trailingShift.announcement.contains("ALL-OUT ATTACK"))

        // Test Scenario 2: AI leading 1-0 against dominant user team -> 5-at-the-back Low Block
        aiTeam.mentality = Mentality.BALANCED
        val leadingShift = AiAdaptiveManager.evaluateAndAdapt(
            currentMinute = 73,
            aiTeam = aiTeam,
            userTeam = userTeam,
            aiScore = 1,
            userScore = 0,
            userPossession = 62, // user dominant in possession
            aiSubsUsed = 0
        )

        assertNotNull("AI Manager must adapt to protect lead", leadingShift)
        assertEquals(Mentality.DEFENSIVE, leadingShift!!.newMentality)
        assertEquals(Formation.F_532, leadingShift.newFormation)
        assertEquals(PressingStyle.OWN_HALF, leadingShift.newPressingStyle)
        assertTrue(leadingShift.announcement.contains("LOW BLOCK"))
    }

    @Test
    fun testMatchSimulationEngineFullMatchWithNewFeatures() {
        val gameState = RfmDatabase.createInitialGameState()
        val epl = gameState.leagues.first { it.id == "epl" }
        val manUtd = epl.teams.first { it.id == "man_utd" }
        val arsenal = epl.teams.first { it.id == "arsenal" }

        val fixture = Fixture(
            id = "test_broadcast_fixture",
            round = 1,
            homeTeamId = manUtd.id,
            awayTeamId = arsenal.id
        )

        val engine = MatchSimulationEngine(fixture, manUtd, arsenal, ModSettings())
        engine.startMatch()

        // Step through simulation
        while (!engine.isFullTime) {
            engine.tickMinute()
        }

        assertTrue("Match should be full time", engine.isFullTime)
        assertEquals(90, engine.currentMinute)
        assertTrue("Live stats should track shots", engine.stats.homeShots >= 0)
        assertTrue("Live stats should track Expected Goals (xG)", engine.stats.homeXg >= 0.0f)
        assertTrue("Momentum should be within bounds", engine.pitchState.homeMomentum in 0.05f..0.95f)
        assertTrue("Pressing zone line should be defined", engine.pitchState.pressingZoneLineX in 0.20f..0.80f)
        assertTrue("Recent ball path should be captured for trajectory trails", engine.pitchState.recentBallPath.isNotEmpty())
    }
}
