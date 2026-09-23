package com.example.rfm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rfm.engine.CommentaryHighlight
import com.example.rfm.engine.MatchSimulationEngine
import com.example.rfm.engine.PitchPlayer
import com.example.rfm.model.PressingStyle
import com.example.rfm.model.TeamTalkOption
import com.example.rfm.model.TouchlineShout
import com.example.rfm.ui.components.MatchAnalyticsDialog
import com.example.rfm.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

enum class PitchRadarMode(val label: String, val icon: String) {
    RADAR("Radar", "📡"),
    HEATMAP("Heatmap", "🔥"),
    PASS_NET("Pass Net", "🕸️"),
    TACTICAL_ZONES("Zones", "📐")
}

enum class MatchDeckTab(val label: String) {
    RATINGS_SUBS("Ratings & Subs"),
    COMMENTARY("Commentary Log"),
    STATS("Match Stats")
}

@Composable
fun MatchScreen(
    engine: MatchSimulationEngine,
    isSimulating: Boolean,
    onTogglePlayback: (speedMs: Long) -> Unit,
    onInstantSimulate: () -> Unit,
    onMakeSubstitution: (playerOutId: String, playerInId: String) -> Boolean,
    onFinishMatch: () -> Unit
) {
    var playbackSpeedMs by remember { mutableStateOf(300L) }
    var showSubDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showHalfTimeTalkDialog by remember { mutableStateOf(false) }
    var showFullTimeTalkDialog by remember { mutableStateOf(false) }
    var radarMode by remember { mutableStateOf(PitchRadarMode.RADAR) }
    var preselectedSubOutId by remember { mutableStateOf<String?>(null) }
    var activeDeckTab by remember { mutableStateOf(MatchDeckTab.RATINGS_SUBS) }
    var selectedStarterForSubId by remember { mutableStateOf<String?>(null) }

    // 60 FPS smooth interpolation animation clock
    var animClock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameNanos ->
                animClock = (frameNanos / 1_000_000L).toFloat()
            }
        }
    }

    // Normalized smooth progress (0.0 to 1.0) for the current playback cycle
    val smoothProgress = remember(animClock, playbackSpeedMs) {
        val cycle = playbackSpeedMs.toFloat().coerceAtLeast(50f)
        val rawT = ((animClock % cycle) / cycle).coerceIn(0f, 1f)
        // Ease in-out interpolation curve for natural acceleration and deceleration
        0.5f * (1f - cos(rawT * Math.PI.toFloat()))
    }

    // Auto-dismiss replay after 2.5 seconds
    LaunchedEffect(engine.pitchState.isReplayActive) {
        if (engine.pitchState.isReplayActive) {
            delay(2800)
            engine.pitchState.isReplayActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("match_screen")
    ) {
        // TOP SCOREBOARD BUG & TV GRAPHICS BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TV SCOREBOARD BUG: Home Team & Colors
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(engine.homeTeam.primaryColorHex), CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Text(
                        text = engine.homeTeam.shortName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Broadcast Score & Clock Display
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = RfmNavyDark,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RfmGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${engine.homeScore} - ${engine.awayScore}",
                                color = RfmGold,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Match Clock & Stoppage Time Badge
                    Surface(
                        color = if (engine.isFullTime) RfmPitchGreen else if (engine.isHalfTime) RfmAmber else Color(0xFF1E293B),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (engine.isFullTime) RfmPitchGreen else RfmAmber)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = when {
                                    engine.isFullTime -> "FULL TIME"
                                    engine.isHalfTime -> "HALF TIME"
                                    else -> "${engine.currentMinute}'"
                                },
                                color = if (engine.isFullTime) Color.White else if (engine.isHalfTime) Color.Black else RfmNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // TV Injury Time indicator (+3')
                            if (engine.pitchState.injuryTimeMinutes > 0 && (engine.currentMinute >= 45 || engine.currentMinute >= 90)) {
                                Surface(
                                    color = RfmDangerRed,
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        text = "+${engine.pitchState.injuryTimeMinutes}'",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Away Team & Colors
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = engine.awayTeam.shortName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(engine.awayTeam.primaryColorHex), CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Match Controls (Play/Pause, Speed, Skip, Stats, Finish)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Highlights vs Full speed toggle
                    Surface(
                        color = if (engine.isHighlightOnlyMode) RfmAmber else RfmNavyCard,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable {
                            engine.isHighlightOnlyMode = !engine.isHighlightOnlyMode
                        }
                    ) {
                        Text(
                            text = if (engine.isHighlightOnlyMode) "🎬 HIGHLIGHTS" else "⚡ FULL SPEED",
                            color = if (engine.isHighlightOnlyMode) Color.Black else RfmGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Play / Pause
                    Button(
                        onClick = { onTogglePlayback(playbackSpeedMs) },
                        enabled = !engine.isFullTime,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("match_play_pause_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSimulating) RfmDangerRed else RfmPitchGreen
                        )
                    ) {
                        Text(
                            text = if (isSimulating) "PAUSE" else "PLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Speed Toggle
                    OutlinedButton(
                        onClick = {
                            playbackSpeedMs = when (playbackSpeedMs) {
                                300L -> 150L
                                150L -> 50L
                                else -> 300L
                            }
                            if (isSimulating) {
                                onTogglePlayback(playbackSpeedMs)
                                onTogglePlayback(playbackSpeedMs)
                            }
                        },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = when (playbackSpeedMs) {
                                300L -> "1x"
                                150L -> "2x"
                                else -> "4x"
                            },
                            color = RfmGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Instant Skip
                    Button(
                        onClick = onInstantSimulate,
                        enabled = !engine.isFullTime,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("match_instant_sim_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("SKIP >>", fontSize = 9.sp, color = RfmAmber, fontWeight = FontWeight.Bold)
                    }

                    // Stats
                    IconButton(
                        onClick = { showStatsDialog = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(RfmNavyCard, RoundedCornerShape(4.dp))
                    ) {
                        Text("📊", fontSize = 12.sp)
                    }

                    // Done / Finish Match Button
                    if (engine.isFullTime) {
                        Button(
                            onClick = onFinishMatch,
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("match_finish_continue_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("FINISH", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // PITCH MOMENTUM BAR (Showing Territorial Dominance in Final Third)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
                .border(0.8.dp, RfmNavyBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val homePct = (engine.pitchState.homeMomentum * 100).toInt()
            val awayPct = 100 - homePct

            Text(
                text = "⚡ ${engine.homeTeam.shortName} $homePct%",
                color = Color(engine.homeTeam.primaryColorHex),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(end = 4.dp)
            )

            // Dynamic Gradient Momentum Meter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(engine.pitchState.homeMomentum.coerceIn(0.05f, 0.95f))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(engine.homeTeam.primaryColorHex),
                                        RfmGold
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - engine.pitchState.homeMomentum).coerceIn(0.05f, 0.95f))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        RfmCyan,
                                        Color(engine.awayTeam.primaryColorHex)
                                    )
                                )
                            )
                    )
                }
            }

            Text(
                text = "$awayPct% ${engine.awayTeam.shortName} ⚡",
                color = Color(engine.awayTeam.primaryColorHex),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // MAIN LANDSCAPE SPLIT VIEW: LEFT (65% Pitch) & RIGHT (35% Touchline Deck)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT SIDE: FULL-WIDTH HORIZONTAL 2D PITCH (65%)
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
            ) {
                // PITCH VIEW BOX
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, RfmPitchLine, RoundedCornerShape(8.dp))
                        .background(RfmPitchGreen)
                        .testTag("match_live_pitch")
                ) {
                    // Pitch Markings, Overlays, Passing Triangles & Ball Trails
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Lawn mowing stripes (alternating greens)
                        val stripeCount = 10
                        val stripeW = w / stripeCount
                        for (i in 0 until stripeCount) {
                            val stripeColor = if (i % 2 == 0) Color(0xFF2E7D32) else Color(0xFF388E3C)
                            drawRect(
                                color = stripeColor,
                                topLeft = Offset(i * stripeW, 0f),
                                size = Size(stripeW, h)
                            )
                        }

                        // Touchlines & Halfway line
                        drawRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(8f, 8f),
                            size = Size(w - 16f, h - 16f),
                            style = Stroke(width = 2.5f)
                        )

                        // Halfway line
                        drawLine(
                            color = Color.White.copy(alpha = 0.75f),
                            start = Offset(w / 2, 8f),
                            end = Offset(w / 2, h - 8f),
                            strokeWidth = 2.5f
                        )

                        // Center Circle & Spot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = h * 0.22f,
                            center = Offset(w / 2, h / 2),
                            style = Stroke(width = 2.5f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = Offset(w / 2, h / 2)
                        )

                        // Left Penalty Box (Home)
                        drawRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(8f, h * 0.22f),
                            size = Size(w * 0.16f, h * 0.56f),
                            style = Stroke(width = 2.5f)
                        )
                        // Left 6-Yard Box
                        drawRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(8f, h * 0.35f),
                            size = Size(w * 0.06f, h * 0.30f),
                            style = Stroke(width = 2.5f)
                        )
                        // Left Goal
                        drawRect(
                            color = Color.White.copy(alpha = 0.9f),
                            topLeft = Offset(2f, h * 0.42f),
                            size = Size(6f, h * 0.16f)
                        )

                        // Right Penalty Box (Away)
                        drawRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(w - 8f - (w * 0.16f), h * 0.22f),
                            size = Size(w * 0.16f, h * 0.56f),
                            style = Stroke(width = 2.5f)
                        )
                        // Right 6-Yard Box
                        drawRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(w - 8f - (w * 0.06f), h * 0.35f),
                            size = Size(w * 0.06f, h * 0.30f),
                            style = Stroke(width = 2.5f)
                        )
                        // Right Goal
                        drawRect(
                            color = Color.White.copy(alpha = 0.9f),
                            topLeft = Offset(w - 8f, h * 0.42f),
                            size = Size(6f, h * 0.16f)
                        )

                        // 30-ZONE POSITIONAL GRID OVERLAY
                        if (radarMode == PitchRadarMode.TACTICAL_ZONES) {
                            // 6 Longitudinal Bands
                            val bands = listOf(0.16f, 0.32f, 0.48f, 0.64f, 0.82f)
                            bands.forEach { bx ->
                                drawLine(
                                    color = Color.White.copy(alpha = 0.25f),
                                    start = Offset(w * bx, 8f),
                                    end = Offset(w * bx, h - 8f),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }
                            // 5 Lateral Channels (Flanks, Half-Spaces, Center)
                            val chans = listOf(0.20f, 0.40f, 0.60f, 0.80f)
                            chans.forEach { cy ->
                                drawLine(
                                    color = Color.White.copy(alpha = 0.25f),
                                    start = Offset(8f, h * cy),
                                    end = Offset(w - 8f, h * cy),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }
                        }

                        // DYNAMIC PRESSING ZONE LINE (shifts higher or lower based on tactics)
                        val pressX = w * engine.pitchState.pressingZoneLineX
                        drawLine(
                            color = RfmGold.copy(alpha = 0.85f),
                            start = Offset(pressX, 8f),
                            end = Offset(pressX, h - 8f),
                            strokeWidth = 2.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )

                        // MIDFIELD PASSING TRIANGLES (lights up when in possession)
                        val activeMids = engine.pitchState.activeMidfieldTriangles.firstOrNull()
                        if (activeMids != null && activeMids.size >= 3) {
                            val p1 = activeMids[0]
                            val p2 = activeMids[1]
                            val p3 = activeMids[2]

                            val path = Path().apply {
                                moveTo(p1.x * w, p1.y * h)
                                lineTo(p2.x * w, p2.y * h)
                                lineTo(p3.x * w, p3.y * h)
                                close()
                            }
                            // Subtle filled glow
                            drawPath(
                                path = path,
                                color = Color(0xFF00E5FF).copy(alpha = 0.16f)
                            )
                            // Glowing border edges
                            drawPath(
                                path = path,
                                color = Color(0xFF00E5FF).copy(alpha = 0.70f),
                                style = Stroke(width = 1.8f)
                            )
                        }

                        // OFF-THE-BALL MOVEMENT TRAILS & DASHED ARROWS
                        engine.pitchState.offTheBallArrows.forEach { arrow ->
                            val start = Offset(arrow.startX * w, arrow.startY * h)
                            val end = Offset(arrow.endX * w, arrow.endY * h)

                            val arrowColor = when (arrow.type) {
                                "OVERLAP" -> Color(0xFFFFD700).copy(alpha = 0.75f)
                                "RUN_IN_BEHIND" -> Color(0xFF00E5FF).copy(alpha = 0.75f)
                                else -> Color.White.copy(alpha = 0.60f)
                            }

                            drawLine(
                                color = arrowColor,
                                start = start,
                                end = end,
                                strokeWidth = 2.0f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f))
                            )
                            // Arrowhead at destination
                            drawCircle(
                                color = arrowColor,
                                radius = 3.2f,
                                center = end
                            )
                        }

                        // BALL TRAJECTORY TRAILS (Visible trail lines during crosses, shots & replays)
                        if (engine.pitchState.recentBallPath.size >= 2) {
                            for (idx in 0 until engine.pitchState.recentBallPath.size - 1) {
                                val pt1 = engine.pitchState.recentBallPath[idx]
                                val pt2 = engine.pitchState.recentBallPath[idx + 1]
                                val trailAlpha = ((idx + 1).toFloat() / engine.pitchState.recentBallPath.size) * 0.7f
                                drawLine(
                                    color = if (engine.pitchState.isReplayActive) RfmAmber.copy(alpha = trailAlpha) else Color.White.copy(alpha = trailAlpha),
                                    start = Offset(pt1.first * w, pt1.second * h),
                                    end = Offset(pt2.first * w, pt2.second * h),
                                    strokeWidth = if (engine.pitchState.isReplayActive) 3.5f else 2.0f
                                )
                            }
                        }

                        // HEATMAP OVERLAY MODE
                        if (radarMode == PitchRadarMode.HEATMAP) {
                            val heatCenters = listOf(
                                Offset(w * engine.pitchState.ballX, h * engine.pitchState.ballY),
                                Offset(w * 0.5f, h * 0.5f),
                                Offset(w * if (engine.pitchState.possessionHome) 0.65f else 0.35f, h * 0.4f),
                                Offset(w * if (engine.pitchState.possessionHome) 0.75f else 0.25f, h * 0.6f)
                            )
                            heatCenters.forEach { center ->
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF3D00).copy(alpha = 0.40f),
                                            Color(0xFFFFEA00).copy(alpha = 0.25f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = w * 0.18f
                                    ),
                                    radius = w * 0.18f,
                                    center = center
                                )
                            }
                        }

                        // PASS NETWORK MODE
                        if (radarMode == PitchRadarMode.PASS_NET) {
                            val activeTeam = if (engine.pitchState.possessionHome) engine.pitchState.homePlayers else engine.pitchState.awayPlayers
                            for (i in activeTeam.indices) {
                                for (j in i + 1 until activeTeam.size) {
                                    val p1 = activeTeam[i]
                                    val p2 = activeTeam[j]
                                    val dx = (p1.x - p2.x) * w
                                    val dy = (p1.y - p2.y) * h
                                    val dist = sqrt(dx * dx + dy * dy)
                                    if (dist < w * 0.28f) {
                                        drawLine(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                                            start = Offset(p1.x * w, p1.y * h),
                                            end = Offset(p2.x * w, p2.y * h),
                                            strokeWidth = 1.8f
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TOP OVERLAY BADGES & CHIPS
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val pw = maxWidth
                        val ph = maxHeight

                        // Phase Badge & Tactical Line Info (Top Left)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = RfmNavyDark.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RfmGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = engine.pitchState.currentPhase.icon, fontSize = 10.sp)
                                    Text(text = engine.pitchState.currentPhase.label, color = RfmGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Pressing Line Label
                            Surface(
                                color = RfmNavyDark.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, RfmCyan)
                            ) {
                                Text(
                                    text = "PRESS: ${engine.homeTeam.pressingStyle.label}",
                                    color = RfmCyan,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Radar View Modes (Top Right of Pitch)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PitchRadarMode.values().forEach { mode ->
                                val isSelected = radarMode == mode
                                Surface(
                                    color = if (isSelected) RfmPitchGreen else RfmNavyDark.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RfmGold else RfmNavyBorder),
                                    modifier = Modifier.clickable { radarMode = mode }
                                ) {
                                    Text(
                                        text = "${mode.icon} ${mode.label}",
                                        color = if (isSelected) Color.White else RfmTextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // SLOW-MOTION INSTANT REPLAY WATERMARK
                        if (engine.pitchState.isReplayActive) {
                            Surface(
                                color = RfmDangerRed.copy(alpha = 0.90f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🔴 SLOW-MOTION REPLAY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    Text(
                                        text = engine.pitchState.replayMoment?.title ?: "REPLAY",
                                        color = RfmGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // RENDER HOME PLAYERS (with 60 FPS Bézier interpolation & micro-animations)
                        engine.pitchState.homePlayers.forEach { p ->
                            // Quadratic Bézier interpolation
                            val interpX = quadraticBezier(p.prevX, p.controlX, p.targetX, smoothProgress)
                            val interpY = quadraticBezier(p.prevY, p.controlY, p.targetY, smoothProgress)
                            val effectiveX = (interpX + engine.pitchState.defensivePushOffset).coerceIn(0.04f, 0.96f)

                            // Micro-animations: kick pulse & collision shake
                            val kickScale = if (p.actionAnimation == "KICK_PULSE") (1f + 0.35f * sin(smoothProgress * Math.PI.toFloat())) else 1f
                            val shakeX = if (p.actionAnimation == "COLLISION_SHAKE") (sin(animClock * 0.05f) * 3f).dp else 0.dp
                            val shakeY = if (p.actionAnimation == "COLLISION_SHAKE") (cos(animClock * 0.05f) * 3f).dp else 0.dp

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .offset(x = pw * effectiveX - 11.dp + shakeX, y = ph * interpY - 11.dp + shakeY)
                                    .scale(kickScale)
                                    .clickable { selectedStarterForSubId = p.id }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(19.dp)
                                        .background(Color(engine.homeTeam.primaryColorHex), CircleShape)
                                        .border(
                                            if (selectedStarterForSubId == p.id) 2.dp else 1.2.dp,
                                            if (selectedStarterForSubId == p.id) RfmAmber else Color.White,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${p.number}",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    color = RfmNavyDark.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", p.liveRating),
                                        color = if (p.liveRating >= 7.5f) RfmGold else if (p.liveRating >= 6.8f) RfmPitchGreen else Color.White,
                                        fontSize = 6.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }

                        // RENDER AWAY PLAYERS (with 60 FPS Bézier interpolation & micro-animations)
                        engine.pitchState.awayPlayers.forEach { p ->
                            val interpX = quadraticBezier(p.prevX, p.controlX, p.targetX, smoothProgress).coerceIn(0.04f, 0.96f)
                            val interpY = quadraticBezier(p.prevY, p.controlY, p.targetY, smoothProgress).coerceIn(0.04f, 0.96f)

                            val kickScale = if (p.actionAnimation == "KICK_PULSE") (1f + 0.35f * sin(smoothProgress * Math.PI.toFloat())) else 1f
                            val shakeX = if (p.actionAnimation == "COLLISION_SHAKE") (sin(animClock * 0.05f) * 3f).dp else 0.dp
                            val shakeY = if (p.actionAnimation == "COLLISION_SHAKE") (cos(animClock * 0.05f) * 3f).dp else 0.dp

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .offset(x = pw * interpX - 11.dp + shakeX, y = ph * interpY - 11.dp + shakeY)
                                    .scale(kickScale)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(19.dp)
                                        .background(Color(engine.awayTeam.primaryColorHex), CircleShape)
                                        .border(1.2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${p.number}",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    color = RfmNavyDark.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", p.liveRating),
                                        color = if (p.liveRating >= 7.5f) RfmGold else Color.White,
                                        fontSize = 6.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }

                        // TRUE BALL TRAJECTORY & PSEUDO-3D ELEVATION (Z-AXIS)
                        val interpBallX = quadraticBezier(
                            engine.pitchState.ballPrevX,
                            engine.pitchState.ballControlX,
                            engine.pitchState.ballTargetX,
                            smoothProgress
                        )
                        val interpBallY = quadraticBezier(
                            engine.pitchState.ballPrevY,
                            engine.pitchState.ballControlY,
                            engine.pitchState.ballTargetY,
                            smoothProgress
                        )

                        // Elevation arc (Z)
                        val ballZ = if (engine.pitchState.ballZ > 0.05f) {
                            sin(smoothProgress * Math.PI.toFloat()) * engine.pitchState.ballZ
                        } else {
                            0f
                        }

                        // 1. Dynamic Pitch Shadow underneath the ball
                        val shadowSize = (10.dp * (1.0f - ballZ * 0.35f)).coerceAtLeast(4.dp)
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = pw * interpBallX - shadowSize / 2,
                                    y = ph * (interpBallY + ballZ * 0.02f) - shadowSize / 2
                                )
                                .size(shadowSize, shadowSize * 0.6f)
                                .background(
                                    Color.Black.copy(alpha = (0.45f - ballZ * 0.30f).coerceIn(0.12f, 0.45f)),
                                    CircleShape
                                )
                        )

                        // 2. Ball itself (grows larger as it rises on Z axis)
                        val ballSize = 10.dp * (1.0f + ballZ * 0.80f)
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = pw * interpBallX - ballSize / 2,
                                    y = ph * (interpBallY - ballZ * 0.045f) - ballSize / 2
                                )
                                .size(ballSize)
                                .background(Color.White, CircleShape)
                                .border(1.2.dp, Color.Black, CircleShape)
                        )

                        // GOAL CELEBRATION LOWER-THIRD & BANNER
                        androidx.compose.animation.AnimatedVisibility(
                            visible = engine.pitchState.activeGoalNotice != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(12.dp)
                        ) {
                            val notice = engine.pitchState.activeGoalNotice
                            if (notice != null) {
                                Surface(
                                    color = RfmNavyDark.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, RfmGold)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = "⚽ GOAL!", color = RfmGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                            Surface(
                                                color = Color(notice.teamColorHex),
                                                shape = RoundedCornerShape(3.dp)
                                            ) {
                                                Text(
                                                    text = notice.teamShortName,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        // Scorer & Assister Details with Live xG
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(Color(notice.teamColorHex), CircleShape)
                                                    .border(1.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "${notice.scorerNumber}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(text = notice.scorerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                                            Surface(color = RfmPitchGreen, shape = RoundedCornerShape(3.dp)) {
                                                Text(
                                                    text = "xG ${String.format("%.2f", notice.xg)}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (notice.assisterName != null) {
                                            Text(
                                                text = "Assist: #${notice.assisterNumber} ${notice.assisterName}",
                                                color = RfmTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // TV CARD NOTIFICATION BANNER (Yellow / Red Cards)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = engine.pitchState.activeCardNotice != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        ) {
                            val card = engine.pitchState.activeCardNotice
                            if (card != null) {
                                Surface(
                                    color = RfmNavyDark.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (card.isRed) RfmDangerRed else RfmAmber
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (card.isRed) RfmDangerRed else RfmAmber,
                                            shape = RoundedCornerShape(2.dp),
                                            modifier = Modifier.size(14.dp, 18.dp)
                                        ) {}

                                        Text(
                                            text = if (card.isRed) "RED CARD: ${card.playerName} (${card.teamShortName})" else "YELLOW CARD: ${card.playerName} (${card.teamShortName})",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${card.minute}'",
                                            color = RfmGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // BOTTOM COMMENTARY TICKER BAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(RfmNavyCard)
                        .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📢 ", fontSize = 11.sp)
                        Text(
                            text = engine.latestCommentary,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // RIGHT SIDE: TOUCHLINE MANAGEMENT DECK (35%)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RfmNavySurface)
                    .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ASSISTANT MANAGER LIVE ADVICE BUBBLE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RfmGold)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "🗣️", fontSize = 11.sp)
                            Text(
                                text = "ASSISTANT ADVICE",
                                color = RfmGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = engine.assistantManagerAdvice,
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // TOUCHLINE SHOUT BUTTONS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf<TouchlineShout>(
                        TouchlineShout.TAKE_MORE_RISKS,
                        TouchlineShout.RETAIN_POSSESSION,
                        TouchlineShout.PUMP_BALL_INTO_BOX,
                        TouchlineShout.CLEAR_BALL_TO_FLANKS
                    ).forEach { shout ->
                        val isActive = engine.activeTouchlineShout == shout
                        Button(
                            onClick = {
                                engine.activeTouchlineShout = if (isActive) TouchlineShout.NONE else shout
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) RfmAmber else RfmNavyCard
                            )
                        ) {
                            Text(
                                text = shout.label,
                                color = if (isActive) Color.Black else Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // DECK SEGMENTED TABS (Ratings & Subs / Commentary / Stats)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(RfmNavyCard)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MatchDeckTab.values().forEach { tab ->
                        val isSelected = activeDeckTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) RfmPitchGreen else Color.Transparent)
                                .clickable { activeDeckTab = tab }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color.White else RfmTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // DYNAMIC CONTENT ACCORDING TO SELECTED TAB
                when (activeDeckTab) {
                    MatchDeckTab.RATINGS_SUBS -> {
                        Column(modifier = Modifier.weight(1f)) {
                            // Section: Starters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedStarterForSubId != null) "STARTER SELECTED: TAP BENCH TO SUB" else "STARTERS (TAP TO SELECT FOR SUB)",
                                    color = if (selectedStarterForSubId != null) RfmAmber else RfmTextSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Subs: ${engine.homeSubsUsed}/3",
                                    color = RfmGold,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(engine.homeTeam.getStartingXI()) { p ->
                                    val isSelected = selectedStarterForSubId == p.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isSelected) RfmAmber.copy(alpha = 0.25f) else RfmNavyDark)
                                            .border(1.dp, if (isSelected) RfmAmber else Color.Transparent, RoundedCornerShape(3.dp))
                                            .clickable {
                                                selectedStarterForSubId = if (isSelected) null else p.id
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = "${p.number}", color = RfmTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(text = p.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = "${p.condition}%", color = if (p.condition < 65) RfmDangerRed else RfmNeonGreen, fontSize = 9.sp)
                                            Surface(
                                                color = RfmNavyBorder,
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Text(
                                                    text = String.format("%.1f", p.liveRating),
                                                    color = if (p.liveRating >= 7.5f) RfmGold else Color.White,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Bench section
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "BENCH PLAYERS (TAP TO BRING ON)",
                                        color = RfmPitchGreen,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                items(engine.homeTeam.getSubstitutes()) { b ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(RfmNavyDark)
                                            .border(1.dp, RfmNavyBorder, RoundedCornerShape(3.dp))
                                            .clickable {
                                                if (selectedStarterForSubId != null) {
                                                    val success = onMakeSubstitution(selectedStarterForSubId!!, b.id)
                                                    if (success) {
                                                        selectedStarterForSubId = null
                                                    }
                                                } else {
                                                    showSubDialog = true
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = b.position.label, color = RfmGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(text = b.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                        }
                                        Text(
                                            text = "OVR ${b.overall} • ${b.condition}%",
                                            color = RfmTextSecondary,
                                            fontSize = 8.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    MatchDeckTab.COMMENTARY -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(engine.detailedCommentary) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(RfmNavyDark, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${item.minute}'",
                                        color = RfmGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.text,
                                        color = Color(item.type.colorHex),
                                        fontSize = 9.5.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    MatchDeckTab.STATS -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatComparisonRow("Possession", "${engine.stats.homePossession}%", "${engine.stats.awayPossession}%")
                            StatComparisonRow("Expected Goals (xG)", String.format("%.2f", engine.stats.homeXg), String.format("%.2f", engine.stats.awayXg))
                            StatComparisonRow("Total Shots", "${engine.stats.homeShots}", "${engine.stats.awayShots}")
                            StatComparisonRow("Shots on Target", "${engine.stats.homeShotsOnTarget}", "${engine.stats.awayShotsOnTarget}")
                            StatComparisonRow("Corners", "${engine.stats.homeCorners}", "${engine.stats.awayCorners}")
                            StatComparisonRow("Fouls", "${engine.stats.homeFouls}", "${engine.stats.awayFouls}")
                            StatComparisonRow("Yellow Cards", "${engine.stats.homeYellowCards}", "${engine.stats.awayYellowCards}")
                            StatComparisonRow("Red Cards", "${engine.stats.homeRedCards}", "${engine.stats.awayRedCards}")
                        }
                    }
                }

                // HALF-TIME TALK ACTION BANNER
                if (engine.isHalfTime && !engine.halfTimeTeamTalkDone) {
                    Button(
                        onClick = { showHalfTimeTalkDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmAmber),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("🗣️ HALF-TIME DRESSING ROOM TALK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                    }
                }

                // FULL-TIME TALK ACTION BANNER
                if (engine.isFullTime && !engine.fullTimeTeamTalkDone) {
                    Button(
                        onClick = { showFullTimeTalkDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("🏆 FULL-TIME TEAM TALK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                    }
                }
            }
        }
    }

    // STATS MODAL
    if (showStatsDialog) {
        MatchAnalyticsDialog(
            fixture = engine.fixture,
            homeTeam = engine.homeTeam,
            awayTeam = engine.awayTeam,
            onDismiss = { showStatsDialog = false }
        )
    }

    // SUBSTITUTION FULL DIALOG
    if (showSubDialog) {
        Dialog(onDismissRequest = {
            showSubDialog = false
            preselectedSubOutId = null
        }) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, RfmGold, RoundedCornerShape(10.dp))
                    .testTag("in_match_substitution_dialog")
            ) {
                var selectedOutId by remember { mutableStateOf<String?>(preselectedSubOutId ?: engine.homeTeam.getStartingXI().firstOrNull()?.id) }
                var selectedInId by remember { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔄 SUBSTITUTION (${engine.homeSubsUsed}/3 Used)",
                            color = RfmGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                showSubDialog = false
                                preselectedSubOutId = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", color = RfmTextSecondary, fontSize = 13.sp)
                        }
                    }

                    Text(text = "1. Select Player to Replace:", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(engine.homeTeam.getStartingXI()) { p ->
                            val isSelected = selectedOutId == p.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) RfmDangerRed.copy(alpha = 0.4f) else RfmNavyCard, RoundedCornerShape(3.dp))
                                    .border(1.dp, if (isSelected) RfmDangerRed else RfmNavyBorder, RoundedCornerShape(3.dp))
                                    .clickable { selectedOutId = p.id }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${p.position.label} ${p.name}", color = Color.White, fontSize = 10.sp)
                                Text(text = "Rating: ${String.format("%.1f", p.liveRating)} • ${p.condition}%", color = RfmTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }

                    Text(text = "2. Select Bench Player to Bring On:", color = RfmNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(engine.homeTeam.getSubstitutes()) { p ->
                            val isSelected = selectedInId == p.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) RfmPitchGreen.copy(alpha = 0.4f) else RfmNavyCard, RoundedCornerShape(3.dp))
                                    .border(1.dp, if (isSelected) RfmPitchGreen else RfmNavyBorder, RoundedCornerShape(3.dp))
                                    .clickable { selectedInId = p.id }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${p.position.label} ${p.name}", color = Color.White, fontSize = 10.sp)
                                Text(text = "OVR: ${p.overall} • ${p.condition}%", color = RfmTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedOutId != null && selectedInId != null) {
                                onMakeSubstitution(selectedOutId!!, selectedInId!!)
                                showSubDialog = false
                                preselectedSubOutId = null
                            }
                        },
                        enabled = selectedOutId != null && selectedInId != null && engine.homeSubsUsed < 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen)
                    ) {
                        Text("CONFIRM SUBSTITUTION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // HALF-TIME TEAM TALK DIALOG
    if (showHalfTimeTalkDialog) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(2.dp, RfmAmber, RoundedCornerShape(10.dp))
                    .testTag("match_ht_talk_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🗣️ HALF-TIME DRESSING ROOM TALK",
                        color = RfmAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Score: ${engine.homeTeam.shortName} ${engine.homeScore} - ${engine.awayScore} ${engine.awayTeam.shortName}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    listOf(
                        TeamTalkOption.DEMAND_MORE,
                        TeamTalkOption.ENCOURAGE,
                        TeamTalkOption.STAY_FOCUSED,
                        TeamTalkOption.HAIRDRYER
                    ).forEach { option ->
                        Button(
                            onClick = {
                                engine.applyHalfTimeTeamTalk(option)
                                showHalfTimeTalkDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = option.label, color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "[${option.tone}]", color = RfmNeonGreen, fontSize = 9.sp)
                                }
                                Text(text = option.description, color = RfmTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // FULL-TIME TEAM TALK DIALOG
    if (showFullTimeTalkDialog) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(2.dp, RfmPitchGreen, RoundedCornerShape(10.dp))
                    .testTag("match_ft_talk_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🏆 FULL-TIME TEAM DEBRIEF",
                        color = RfmPitchGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Final: ${engine.homeTeam.shortName} ${engine.homeScore} - ${engine.awayScore} ${engine.awayTeam.shortName}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    listOf(
                        TeamTalkOption.PRAISE_WIN,
                        TeamTalkOption.BALANCED_REVIEW,
                        TeamTalkOption.HAIRDRYER
                    ).forEach { option ->
                        Button(
                            onClick = {
                                engine.applyFullTimeTeamTalk(option)
                                showFullTimeTalkDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = option.label, color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "[${option.tone}]", color = RfmNeonGreen, fontSize = 9.sp)
                                }
                                Text(text = option.description, color = RfmTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quadratic Bézier interpolation for smooth curve runs and trajectories.
 */
private fun quadraticBezier(p0: Float, pControl: Float, p1: Float, t: Float): Float {
    val oneMinusT = 1f - t
    return (oneMinusT * oneMinusT * p0) + (2f * oneMinusT * t * pControl) + (t * t * p1)
}

@Composable
private fun StatComparisonRow(label: String, homeVal: String, awayVal: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = homeVal, color = RfmGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White, fontSize = 10.sp)
        Text(text = awayVal, color = RfmGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
