package com.example.rfm.engine

import com.example.rfm.model.Player
import com.example.rfm.model.TacklingStyle
import kotlin.random.Random

data class MicroDuelResult(
    val attackerWon: Boolean,
    val margin: Float,
    val narrative: String,
    val foulCommitted: Boolean = false,
    val cardWarranted: Boolean = false
)

data class PenetrationOpportunity(
    val passer: Player,
    val receiver: Player,
    val isThroughBallBehindDefense: Boolean,
    val targetZoneId: Int,
    val valueRating: Float, // 0.0 to 1.0
    val narrative: String
)

object MicroDuelEngine {

    /**
     * Resolves a 1v1 take-on duel between an attacking winger and a defending fullback.
     * Attacker: PAC + DRI adjusted for fatigue
     * Full-back: PAC + DEF + Marking style aggression adjusted for fatigue
     */
    fun resolveWingerVsFullback(
        winger: Player,
        fullback: Player,
        fullbackTacklingStyle: TacklingStyle = TacklingStyle.NORMAL
    ): MicroDuelResult {
        val wingerFatigue = (winger.condition / 100f).coerceIn(0.4f, 1.0f)
        val fullbackFatigue = (fullback.condition / 100f).coerceIn(0.4f, 1.0f)

        // Winger power: 50% Pace, 50% Dribbling
        val wingerPower = ((winger.pace * 0.5f) + (winger.dribbling * 0.5f)) * wingerFatigue

        // Fullback power: 40% Pace, 60% Defending + marking style bonus
        val markingBonus = when (fullbackTacklingStyle) {
            TacklingStyle.AGGRESSIVE -> 6f
            TacklingStyle.CAUTIOUS -> -3f
            else -> 0f
        }
        val fullbackPower = (((fullback.pace * 0.4f) + (fullback.defending * 0.6f)) + markingBonus) * fullbackFatigue

        val randomVariance = (Random.nextFloat() - 0.5f) * 18f
        val diff = wingerPower - fullbackPower + randomVariance

        return if (diff > 0) {
            val narrative = if (winger.pace > fullback.pace) {
                "${winger.name} burns past ${fullback.name} with blistering acceleration down the flank!"
            } else {
                "${winger.name} drops a shoulder and skips past ${fullback.name} with brilliant footwork!"
            }
            MicroDuelResult(
                attackerWon = true,
                margin = diff,
                narrative = narrative
            )
        } else {
            // Check for tackle foul based on fullback aggression
            val foulRisk = (fullbackTacklingStyle.cardRisk * 22f).toInt()
            val isFoul = Random.nextInt(100) < foulRisk
            val isCard = isFoul && (Random.nextInt(100) < (fullbackTacklingStyle.cardRisk * 35f).toInt())

            val narrative = when {
                isCard -> "Tactical foul! ${fullback.name} lunges in recklessly to trip ${winger.name} after being beaten."
                isFoul -> "Foul whistled! ${fullback.name} clips ${winger.name}'s heels to break up the attack."
                fullback.defending >= 80 -> "Impeccable timed tackle! ${fullback.name} dispossesses ${winger.name} with absolute precision."
                else -> "${fullback.name} holds his ground and shields the ball out against ${winger.name}."
            }

            MicroDuelResult(
                attackerWon = false,
                margin = -diff,
                narrative = narrative,
                foulCommitted = isFoul,
                cardWarranted = isCard
            )
        }
    }

    /**
     * Resolves an aerial & physical duel on a cross or set-piece:
     * Attacker: JUM (Physical) + PHY + SHO
     * Defender: DEF + PHY + HEA (Physical/Defending hybrid)
     */
    fun resolveAerialPhysicalDuel(
        attacker: Player,
        defender: Player,
        isSetPiece: Boolean = false
    ): MicroDuelResult {
        val attFatigue = (attacker.condition / 100f).coerceIn(0.5f, 1.0f)
        val defFatigue = (defender.condition / 100f).coerceIn(0.5f, 1.0f)

        // Attacker aerial rating: 35% Jumping/Phys, 35% Physical, 30% Shooting technique
        val attackerAerial = ((attacker.physical * 0.40f) + (attacker.physical * 0.30f) + (attacker.shooting * 0.30f)) * attFatigue

        // Defender aerial rating: 40% Defending, 35% Physical, 25% Overall aerial awareness
        val defenderAerial = ((defender.defending * 0.40f) + (defender.physical * 0.35f) + (defender.overall * 0.25f)) * defFatigue

        val setPieceBonus = if (isSetPiece) 4f else 0f
        val variance = (Random.nextFloat() - 0.5f) * 20f
        val diff = (attackerAerial + setPieceBonus) - defenderAerial + variance

        return if (diff > 0) {
            val narrative = if (attacker.physical > defender.physical) {
                "${attacker.name} out-muscles ${defender.name} in the air to power a towering header!"
            } else {
                "${attacker.name} times his leap to perfection, beating ${defender.name} to the delivery!"
            }
            MicroDuelResult(
                attackerWon = true,
                margin = diff,
                narrative = narrative
            )
        } else {
            val narrative = if (defender.defending > 82) {
                "Commanding aerial clearance by ${defender.name}, rising above ${attacker.name} to nod the danger away."
            } else {
                "${defender.name} puts in a physical challenge to disturb ${attacker.name}'s aerial attempt."
            }
            MicroDuelResult(
                attackerWon = false,
                margin = -diff,
                narrative = narrative
            )
        }
    }

    /**
     * Playmaker Vision & Penetration Recognition:
     * Midfielders with high PAS + DRI recognize and attempt higher-value penetrating opportunities
     * (through balls into half-spaces or behind defense) rather than safe sideways passes.
     */
    fun evaluatePlaymakerVision(
        passer: Player,
        teammates: List<Player>,
        opponents: List<Player>
    ): PenetrationOpportunity? {
        // Vision score derived from Passing & Dribbling
        val visionScore = (passer.passing * 0.65f + passer.dribbling * 0.35f)

        // Threshold: High vision midfielders (> 78) actively seek penetrating passes
        if (visionScore < 76f) {
            return null
        }

        val forwardOptions = teammates.filter {
            it.id != passer.id && (it.position.roleCategory == "Forward" || it.position.label in listOf("CAM", "LW", "RW"))
        }
        if (forwardOptions.isEmpty()) return null

        val targetReceiver = forwardOptions.maxByOrNull { it.pace + it.shooting } ?: forwardOptions.random()
        val roll = Random.nextInt(100)

        // Vision check against opponent midfield/defense awareness
        val avgOppDef = opponents.filter { it.position.roleCategory == "Defender" }.map { it.defending }.average().toFloat().coerceIn(60f, 95f)
        val penetratingSuccessThreshold = (50 + (visionScore - avgOppDef)).coerceIn(25f, 85f)

        return if (roll < penetratingSuccessThreshold) {
            val isThroughBall = targetReceiver.pace >= 80
            val narrative = if (isThroughBall) {
                "Incisive vision from ${passer.name}! Spots the run of ${targetReceiver.name} and threads an exquisite through-ball behind the high defensive line!"
            } else {
                "Visionary pass by ${passer.name}! Splits the defense with a laser-guided ball into the half-space for ${targetReceiver.name}!"
            }

            PenetrationOpportunity(
                passer = passer,
                receiver = targetReceiver,
                isThroughBallBehindDefense = isThroughBall,
                targetZoneId = if (isThroughBall) 27 else 22, // Near or in attacking box
                valueRating = (visionScore / 100f).coerceIn(0.6f, 0.98f),
                narrative = narrative
            )
        } else {
            null
        }
    }
}
