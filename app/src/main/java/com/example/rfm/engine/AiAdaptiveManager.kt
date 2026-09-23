package com.example.rfm.engine

import com.example.rfm.model.*

data class AiTacticalShift(
    val newMentality: Mentality,
    val newFormation: Formation,
    val newPressingStyle: PressingStyle,
    val subMade: Boolean,
    val subInPlayerName: String? = null,
    val subOutPlayerName: String? = null,
    val announcement: String
)

object AiAdaptiveManager {

    /**
     * Evaluates match state and applies AI counter-tactics:
     * - Checkpoint 1: Half-Time (minute 45)
     * - Checkpoint 2: After 70th minute (minute 70-75)
     */
    fun evaluateAndAdapt(
        currentMinute: Int,
        aiTeam: Team,
        userTeam: Team,
        aiScore: Int,
        userScore: Int,
        userPossession: Int,
        aiSubsUsed: Int
    ): AiTacticalShift? {
        val isHalfTimeCheckpoint = currentMinute == 45
        val isLateGameCheckpoint = currentMinute in 70..75

        if (!isHalfTimeCheckpoint && !isLateGameCheckpoint) return null

        val scoreDiff = aiScore - userScore // negative means AI is trailing

        // CASE 1: AI Trailing by 1 goal (or more late in the match)
        // Switch to All-Out Attack, push men forward, introduce extra striker/forward, play direct
        if (scoreDiff < 0) {
            aiTeam.mentality = Mentality.ALL_OUT_ATTACK
            aiTeam.pressingStyle = PressingStyle.HIGH_PRESS

            var subSuccess = false
            var subInName: String? = null
            var subOutName: String? = null

            if (aiSubsUsed < 3) {
                // Find a defender or defensive midfielder to sub off for an extra forward
                val startingXI = aiTeam.getStartingXI()
                val playerOut = startingXI.find { it.position.roleCategory == "Defender" && it.position != PlayerPosition.CB }
                    ?: startingXI.find { it.position == PlayerPosition.CDM || it.position == PlayerPosition.CM }

                val benchAttackers = aiTeam.getSubstitutes().filter {
                    (it.position == PlayerPosition.ST || it.position == PlayerPosition.LW || it.position == PlayerPosition.RW) && it.canPlay()
                }
                val playerIn = benchAttackers.maxByOrNull { it.overall }

                if (playerOut != null && playerIn != null) {
                    playerOut.isStarting = false
                    playerOut.isSubstitute = false
                    playerIn.isStarting = true
                    playerIn.isSubstitute = false
                    subSuccess = true
                    subInName = playerIn.name
                    subOutName = playerOut.name
                }
            }

            // Adjust formation to an offensive 4-3-3 or 4-2-3-1 if not already
            if (aiTeam.formation != Formation.F_433) {
                aiTeam.formation = Formation.F_433
            }

            val subDesc = if (subSuccess) " Bringing on striker $subInName for $subOutName." else ""
            val announcement = "TACTICAL SHIFT (${aiTeam.shortName}): Trailing $userScore-$aiScore at ${currentMinute}', manager orders ALL-OUT ATTACK with high pressing line and direct deliveries!$subDesc"

            return AiTacticalShift(
                newMentality = Mentality.ALL_OUT_ATTACK,
                newFormation = aiTeam.formation,
                newPressingStyle = PressingStyle.HIGH_PRESS,
                subMade = subSuccess,
                subInPlayerName = subInName,
                subOutPlayerName = subOutName,
                announcement = announcement
            )
        }

        // CASE 2: AI Leading Against Dominant User Team (e.g. user possession >= 55% or user shots high)
        // Switch to 5-at-the-back Low Block, defend deep, waste time, protect lead
        if (scoreDiff > 0 && (userPossession >= 54 || isLateGameCheckpoint)) {
            aiTeam.mentality = Mentality.DEFENSIVE
            aiTeam.formation = Formation.F_532
            aiTeam.pressingStyle = PressingStyle.OWN_HALF

            var subSuccess = false
            var subInName: String? = null
            var subOutName: String? = null

            if (aiSubsUsed < 3) {
                // Find a tired forward/winger to replace with a defensive reinforcement
                val startingXI = aiTeam.getStartingXI()
                val playerOut = startingXI.find { it.position.roleCategory == "Forward" }
                    ?: startingXI.minByOrNull { it.condition }

                val benchDefenders = aiTeam.getSubstitutes().filter {
                    (it.position.roleCategory == "Defender" || it.position == PlayerPosition.CDM) && it.canPlay()
                }
                val playerIn = benchDefenders.maxByOrNull { it.defending }

                if (playerOut != null && playerIn != null) {
                    playerOut.isStarting = false
                    playerOut.isSubstitute = false
                    playerIn.isStarting = true
                    playerIn.isSubstitute = false
                    subSuccess = true
                    subInName = playerIn.name
                    subOutName = playerOut.name
                }
            }

            val subDesc = if (subSuccess) " Reinforcing defense: $subInName replaces $subOutName." else ""
            val announcement = "TACTICAL SHIFT (${aiTeam.shortName}): Protecting the $aiScore-$userScore lead at ${currentMinute}', manager adopts a 5-AT-THE-BACK LOW BLOCK to absorb pressure and slow the tempo.$subDesc"

            return AiTacticalShift(
                newMentality = Mentality.DEFENSIVE,
                newFormation = Formation.F_532,
                newPressingStyle = PressingStyle.OWN_HALF,
                subMade = subSuccess,
                subInPlayerName = subInName,
                subOutPlayerName = subOutName,
                announcement = announcement
            )
        }

        return null
    }
}
