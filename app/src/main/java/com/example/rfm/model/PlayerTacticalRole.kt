package com.example.rfm.model

/**
 * Tactical roles that define individual player behavior, positioning,
 * and statistical influence in the probabilistic match engine.
 */
enum class PlayerTacticalRole(
    val label: String,
    val description: String,
    val compatiblePositions: List<PlayerPosition>,
    val shotBonus: Int = 0,
    val passBonus: Int = 0,
    val defBonus: Int = 0,
    val staminaDrainMultiplier: Float = 1.0f
) {
    // Goalkeepers
    TRADITIONAL_GK(
        label = "Traditional GK",
        description = "Focuses purely on shot-stopping and holding the goal line.",
        compatiblePositions = listOf(PlayerPosition.GK),
        defBonus = 4,
        staminaDrainMultiplier = 0.8f
    ),
    SWEEPER_KEEPER(
        label = "Sweeper Keeper",
        description = "Acts as an extra defender, rushing out to intercept through balls.",
        compatiblePositions = listOf(PlayerPosition.GK),
        passBonus = 5,
        defBonus = 2,
        staminaDrainMultiplier = 1.1f
    ),

    // Defenders
    NO_NONSENSE_DEFENDER(
        label = "No-Nonsense Defender",
        description = "Safety-first clearances, high tackle aggression, low risk in possession.",
        compatiblePositions = listOf(PlayerPosition.CB, PlayerPosition.LB, PlayerPosition.RB),
        defBonus = 6,
        staminaDrainMultiplier = 0.9f
    ),
    BALL_PLAYING_DEFENDER(
        label = "Ball-Playing Defender",
        description = "Comfortable with the ball, triggers long switches and initiates attacks from the back.",
        compatiblePositions = listOf(PlayerPosition.CB),
        passBonus = 6,
        defBonus = 2,
        staminaDrainMultiplier = 1.0f
    ),
    ATTACKING_FULLBACK(
        label = "Attacking Fullback",
        description = "Provides width by overlapping forward, delivers crosses into the box.",
        compatiblePositions = listOf(PlayerPosition.LB, PlayerPosition.RB),
        shotBonus = 2,
        passBonus = 5,
        staminaDrainMultiplier = 1.3f
    ),

    // Midfielders
    BALL_WINNING_MIDFIELDER(
        label = "Ball-Winning Midfielder",
        description = "Hounds opponents, intercepts passes, and wins tackles in the middle third.",
        compatiblePositions = listOf(PlayerPosition.CDM, PlayerPosition.CM),
        defBonus = 7,
        staminaDrainMultiplier = 1.25f
    ),
    DEEP_LYING_PLAYMAKER(
        label = "Deep-Lying Playmaker",
        description = "Controls match tempo, dictates play with pinpoint passing from deep.",
        compatiblePositions = listOf(PlayerPosition.CDM, PlayerPosition.CM),
        passBonus = 8,
        staminaDrainMultiplier = 0.95f
    ),
    BOX_TO_BOX(
        label = "Box-to-Box Midfielder",
        description = "Relentlessly transitions between penalty boxes to assist in defense and attack.",
        compatiblePositions = listOf(PlayerPosition.CM),
        shotBonus = 3,
        passBonus = 3,
        defBonus = 3,
        staminaDrainMultiplier = 1.4f
    ),
    ADVANCED_PLAYMAKER(
        label = "Advanced Playmaker",
        description = "Operates between the lines, unpicking defenses with through balls and key passes.",
        compatiblePositions = listOf(PlayerPosition.CAM, PlayerPosition.CM),
        shotBonus = 3,
        passBonus = 8,
        staminaDrainMultiplier = 1.05f
    ),

    // Forwards & Wingers
    WINGER(
        label = "Traditional Winger",
        description = "Hugs the touchline, beats fullbacks on the outside and whips crosses in.",
        compatiblePositions = listOf(PlayerPosition.LW, PlayerPosition.RW, PlayerPosition.LM, PlayerPosition.RM),
        passBonus = 6,
        staminaDrainMultiplier = 1.2f
    ),
    INSIDE_FORWARD(
        label = "Inside Forward",
        description = "Cuts inside from wide channels onto stronger foot to shoot on goal.",
        compatiblePositions = listOf(PlayerPosition.LW, PlayerPosition.RW),
        shotBonus = 7,
        passBonus = 2,
        staminaDrainMultiplier = 1.15f
    ),
    INVERTED_WINGER(
        label = "Inverted Winger",
        description = "Moves inside toward central and half-space zones to combine or shoot.",
        compatiblePositions = listOf(PlayerPosition.LW, PlayerPosition.RW, PlayerPosition.LM, PlayerPosition.RM),
        shotBonus = 5,
        passBonus = 5,
        staminaDrainMultiplier = 1.2f
    ),
    TARGET_MAN(
        label = "Target Man",
        description = "Uses aerial strength to hold up play, flick on headers, and bring teammates in.",
        compatiblePositions = listOf(PlayerPosition.ST),
        shotBonus = 4,
        passBonus = 4,
        staminaDrainMultiplier = 1.05f
    ),
    POACHER(
        label = "Poacher",
        description = "Lurks on the shoulder of the last defender, lethal one-touch finisher in the box.",
        compatiblePositions = listOf(PlayerPosition.ST),
        shotBonus = 9,
        passBonus = -2,
        defBonus = -5,
        staminaDrainMultiplier = 0.95f
    );

    companion object {
        fun defaultForPosition(position: PlayerPosition): PlayerTacticalRole {
            return when (position) {
                PlayerPosition.GK -> TRADITIONAL_GK
                PlayerPosition.CB -> NO_NONSENSE_DEFENDER
                PlayerPosition.LB, PlayerPosition.RB -> ATTACKING_FULLBACK
                PlayerPosition.CDM -> BALL_WINNING_MIDFIELDER
                PlayerPosition.CM -> BOX_TO_BOX
                PlayerPosition.CAM -> ADVANCED_PLAYMAKER
                PlayerPosition.LM, PlayerPosition.RM -> WINGER
                PlayerPosition.LW, PlayerPosition.RW -> INSIDE_FORWARD
                PlayerPosition.ST -> POACHER
            }
        }
    }
}
