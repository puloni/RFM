package com.example.rfm.engine

import kotlin.math.sqrt

/**
 * 30-zone positional grid system covering the pitch:
 * 6 Longitudinal Bands (from home goal line X=0.0 to away goal line X=1.0)
 * 5 Lateral Channels (from top touchline Y=0.0 to bottom touchline Y=1.0)
 * Total: 6 x 5 = 30 Spatial Zones.
 */
enum class PitchZoneBand(
    val index: Int,
    val label: String,
    val xMin: Float,
    val xMax: Float
) {
    DEF_BOX(0, "Defensive Box / 6-Yard", 0.00f, 0.16f),
    DEF_THIRD(1, "Defensive Third", 0.16f, 0.32f),
    MID_DEF(2, "Midfield Defensive Half", 0.32f, 0.48f),
    MID_ATT(3, "Midfield Attacking Half", 0.48f, 0.64f),
    ATT_THIRD(4, "Attacking Third / Zone 14", 0.64f, 0.82f),
    ATT_BOX(5, "Opponent Box / 6-Yard", 0.82f, 1.00f);

    val centerX: Float get() = (xMin + xMax) / 2f
}

enum class PitchZoneChannel(
    val index: Int,
    val label: String,
    val yMin: Float,
    val yMax: Float
) {
    LEFT_FLANK(0, "Left Flank", 0.00f, 0.20f),
    LEFT_HALF_SPACE(1, "Left Half-Space", 0.20f, 0.40f),
    CENTRAL_POCKET(2, "Central Pocket / Channel", 0.40f, 0.60f),
    RIGHT_HALF_SPACE(3, "Right Half-Space", 0.60f, 0.80f),
    RIGHT_FLANK(4, "Right Flank", 0.80f, 1.00f);

    val centerY: Float get() = (yMin + yMax) / 2f
}

data class PitchZone(
    val id: Int, // 0 to 29
    val band: PitchZoneBand,
    val channel: PitchZoneChannel,
    val name: String,
    val isHalfSpace: Boolean,
    val isFlank: Boolean,
    val isCentral: Boolean,
    val isBox: Boolean
) {
    val centerX: Float get() = band.centerX
    val centerY: Float get() = channel.centerY

    fun distanceTo(other: PitchZone): Float {
        val dx = centerX - other.centerX
        val dy = centerY - other.centerY
        return sqrt(dx * dx + dy * dy)
    }

    fun isAdjacent(other: PitchZone): Boolean {
        val bandDiff = kotlin.math.abs(band.index - other.band.index)
        val chanDiff = kotlin.math.abs(channel.index - other.channel.index)
        return bandDiff <= 1 && chanDiff <= 1
    }
}

object PitchSpatialGrid {
    val zones: List<PitchZone> = run {
        val list = mutableListOf<PitchZone>()
        var counter = 0
        for (b in PitchZoneBand.values()) {
            for (c in PitchZoneChannel.values()) {
                val isHs = c == PitchZoneChannel.LEFT_HALF_SPACE || c == PitchZoneChannel.RIGHT_HALF_SPACE
                val isFl = c == PitchZoneChannel.LEFT_FLANK || c == PitchZoneChannel.RIGHT_FLANK
                val isCent = c == PitchZoneChannel.CENTRAL_POCKET
                val isBx = b == PitchZoneBand.DEF_BOX || b == PitchZoneBand.ATT_BOX

                val zoneName = when {
                    b == PitchZoneBand.ATT_THIRD && isCent -> "Zone 14 (Central Pocket)"
                    isHs && b == PitchZoneBand.ATT_THIRD -> "Attacking ${c.label}"
                    isHs && b == PitchZoneBand.DEF_THIRD -> "Defensive ${c.label}"
                    isBx && isCent -> if (b == PitchZoneBand.ATT_BOX) "Opponent Box (Central)" else "Own Box (Central)"
                    else -> "${b.label} - ${c.label}"
                }

                list.add(
                    PitchZone(
                        id = counter++,
                        band = b,
                        channel = c,
                        name = zoneName,
                        isHalfSpace = isHs,
                        isFlank = isFl,
                        isCentral = isCent,
                        isBox = isBx
                    )
                )
            }
        }
        list
    }

    fun getZoneForCoordinates(x: Float, y: Float): PitchZone {
        val clampedX = x.coerceIn(0f, 1f)
        val clampedY = y.coerceIn(0f, 1f)

        val band = PitchZoneBand.values().firstOrNull { clampedX >= it.xMin && (clampedX < it.xMax || it == PitchZoneBand.ATT_BOX) }
            ?: PitchZoneBand.MID_ATT

        val channel = PitchZoneChannel.values().firstOrNull { clampedY >= it.yMin && (clampedY < it.yMax || it == PitchZoneChannel.RIGHT_FLANK) }
            ?: PitchZoneChannel.CENTRAL_POCKET

        return zones.firstOrNull { it.band == band && it.channel == channel } ?: zones[12]
    }

    fun calculateZoneOverload(
        zone: PitchZone,
        homePlayerCoords: List<Pair<Float, Float>>,
        awayPlayerCoords: List<Pair<Float, Float>>
    ): Pair<Int, Int> {
        var homeCount = 0
        var awayCount = 0

        homePlayerCoords.forEach { (x, y) ->
            val pZone = getZoneForCoordinates(x, y)
            if (pZone.id == zone.id || pZone.isAdjacent(zone)) {
                homeCount++
            }
        }

        awayPlayerCoords.forEach { (x, y) ->
            val pZone = getZoneForCoordinates(x, y)
            if (pZone.id == zone.id || pZone.isAdjacent(zone)) {
                awayCount++
            }
        }

        return Pair(homeCount, awayCount)
    }
}
