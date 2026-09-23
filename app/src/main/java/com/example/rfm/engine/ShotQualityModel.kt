package com.example.rfm.engine

import com.example.rfm.model.Player
import kotlin.math.*

enum class ShotType(val label: String, val baseMultiplier: Float) {
    OPEN_PLAY("Open Play Shot", 1.0f),
    VOLLEY("First-Time Volley", 0.72f),
    TAP_IN("Close-Range Tap-in", 2.2f),
    HEADER("Contested Header", 0.62f),
    ONE_ON_ONE("1-on-1 Breakaway", 1.75f),
    DIRECT_FREE_KICK("Direct Free Kick", 0.55f)
}

data class XgShotResult(
    val xg: Float,
    val shotType: ShotType,
    val distanceMeters: Float,
    val angleRadians: Float,
    val defendersInCone: Int,
    val isWeakFoot: Boolean,
    val defensivePressureScore: Float, // 0.0 (unmarked) to 1.0 (heavily pressured)
    val narrativeReason: String
)

object ShotQualityModel {

    /**
     * Calculates authentic Expected Goals (xG) based on spatial and physical factors:
     * - Distance from goal center in meters (assuming standard 105m pitch length, 68m width)
     * - Visible angle subtended by the goal posts (7.32m goal mouth)
     * - Number of defenders between ball and goal mouth
     * - Shot type: Open play, Volley, Tap-in, Header, 1-on-1 breakaway, Direct free-kick
     * - Weak foot penalty
     * - Closest defensive pressure
     * - Shooter technical attribute rating
     */
    fun calculateXg(
        shotX: Float, // 0.0 to 1.0
        shotY: Float, // 0.0 to 1.0
        isAttackingRight: Boolean,
        shotType: ShotType,
        defendersInCone: Int,
        defensivePressure: Float, // 0.0f (no pressure) to 1.0f (tightly marked)
        isWeakFoot: Boolean,
        shooter: Player
    ): XgShotResult {
        val goalX = if (isAttackingRight) 1.0f else 0.0f
        val goalY = 0.5f

        // Convert normalized coordinates to pitch meters (105m x 68m)
        val dxMeters = abs(shotX - goalX) * 105f
        val dyMeters = (shotY - goalY) * 68f
        val distanceMeters = sqrt(dxMeters * dxMeters + dyMeters * dyMeters).coerceAtLeast(1.0f)

        // Goal width is 7.32 meters (approx 0.108 in normalized width)
        // Posts are at (goalX, 0.446) and (goalX, 0.554)
        val post1Y = (0.446f - shotY) * 68f
        val post2Y = (0.554f - shotY) * 68f
        val d1 = sqrt(dxMeters * dxMeters + post1Y * post1Y)
        val d2 = sqrt(dxMeters * dxMeters + post2Y * post2Y)

        // Angle in radians subtended by the goal using Law of Cosines
        val postDistance = 7.32f
        val cosAngle = ((d1 * d1 + d2 * d2 - postDistance * postDistance) / (2f * d1 * d2)).coerceIn(-1.0f, 1.0f)
        val angleRadians = acos(cosAngle)

        // Base logistic xG model: xG = 1 / (1 + exp(-logit))
        // Distance and angle are primary drivers
        var logit = -0.15f * distanceMeters + 1.85f * angleRadians - 1.1f

        // Apply shot type modifiers
        logit += when (shotType) {
            ShotType.TAP_IN -> 2.4f
            ShotType.ONE_ON_ONE -> 1.2f
            ShotType.OPEN_PLAY -> 0.0f
            ShotType.VOLLEY -> -0.4f
            ShotType.HEADER -> -0.6f
            ShotType.DIRECT_FREE_KICK -> -0.8f
        }

        // Defensive cone density penalty
        logit -= (defendersInCone.coerceIn(0, 5) * 0.35f)

        // Defensive pressure (closing down / physical contest)
        logit -= (defensivePressure.coerceIn(0f, 1f) * 0.55f)

        // Weak-foot penalty
        if (isWeakFoot) {
            logit -= 0.30f
        }

        // Shooter composure & shooting attribute modifier (+/- up to 0.4)
        val shooterAbilityMod = ((shooter.shooting - 70) / 100f) * 0.5f
        val fatiguePenalty = (100 - shooter.condition) * 0.005f
        logit += (shooterAbilityMod - fatiguePenalty)

        // Convert logit to probability (0.01 to 0.96)
        val rawXg = (1.0f / (1.0f + exp(-logit))).coerceIn(0.02f, 0.94f)

        // Round to 2 decimal places
        val roundedXg = round(rawXg * 100f) / 100f

        val narrative = buildString {
            append("${shotType.label} from ${String.format("%.1f", distanceMeters)}m ")
            if (defendersInCone > 1) append("through traffic ($defendersInCone defenders) ")
            if (defensivePressure > 0.6f) append("under heavy pressure ")
            if (isWeakFoot) append("on weaker foot ")
            append("• xG ${String.format("%.2f", roundedXg)}")
        }

        return XgShotResult(
            xg = roundedXg,
            shotType = shotType,
            distanceMeters = distanceMeters,
            angleRadians = angleRadians,
            defendersInCone = defendersInCone,
            isWeakFoot = isWeakFoot,
            defensivePressureScore = defensivePressure,
            narrativeReason = narrative
        )
    }
}
