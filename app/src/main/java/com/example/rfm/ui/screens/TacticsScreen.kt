package com.example.rfm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.*
import com.example.rfm.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TacticsScreen(
    userTeam: Team,
    onBack: () -> Unit
) {
    var selectedFormation by remember { mutableStateOf(userTeam.formation) }
    var selectedMentality by remember { mutableStateOf(userTeam.mentality) }
    var selectedPassing by remember { mutableStateOf(userTeam.passingStyle) }
    var selectedPressing by remember { mutableStateOf(userTeam.pressingStyle) }
    var selectedTackling by remember { mutableStateOf(userTeam.tacklingStyle) }
    var selectedTempo by remember { mutableStateOf(userTeam.tempoStyle) }
    var selectedMarking by remember { mutableStateOf(userTeam.markingStyle) }
    var offsideTrapEnabled by remember { mutableStateOf(userTeam.offsideTrap) }

    var captainId by remember { mutableStateOf(userTeam.captainPlayerId ?: userTeam.getStartingXI().firstOrNull()?.id) }
    var penTakerId by remember { mutableStateOf(userTeam.penaltyTakerId ?: userTeam.getStartingXI().maxByOrNull { it.shooting }?.id) }
    var fkTakerId by remember { mutableStateOf(userTeam.freeKickTakerId ?: userTeam.getStartingXI().maxByOrNull { it.passing }?.id) }
    var crnTakerId by remember { mutableStateOf(userTeam.cornerTakerId ?: userTeam.getStartingXI().maxByOrNull { it.passing }?.id) }

    var selectedPlayerForSwap by remember { mutableStateOf<Player?>(null) }
    var squadChangeCounter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("tactics_screen")
    ) {
        // TOP HEADER BAR: TITLE, FORMATION CHIPS, AUTO-PICK & BACK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TACTICAL BOARD",
                    color = RfmGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                // Formation chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Formation.values().forEach { f ->
                        val isSelected = selectedFormation == f
                        Surface(
                            color = if (isSelected) RfmPitchGreen else RfmNavyCard,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RfmGold else RfmNavyBorder),
                            modifier = Modifier.clickable {
                                selectedFormation = f
                                userTeam.formation = f
                                squadChangeCounter++
                            }
                        ) {
                            Text(
                                text = f.label,
                                color = if (isSelected) Color.White else RfmTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        autoSelectBestXI(userTeam)
                        squadChangeCounter++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("⚡ AUTO BEST XI", color = RfmAmber, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = RfmNavySurface),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("DONE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // LANDSCAPE SPLIT VIEW: LEFT (Pitch Board 56%) & RIGHT (Side Panel 44%)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT PANEL: 2D PITCH TACTICAL BOARD (56%)
            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Instruction banner
                Surface(
                    color = if (selectedPlayerForSwap != null) RfmAmber.copy(alpha = 0.2f) else RfmNavySurface,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPlayerForSwap != null) RfmAmber else RfmNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedPlayerForSwap != null)
                                "SWAP SELECTED: ${selectedPlayerForSwap?.name} (Tap another player or bench to swap)"
                            else
                                "Drag player token to reposition/swap; tap player to swap with bench",
                            color = if (selectedPlayerForSwap != null) RfmAmber else RfmTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (selectedPlayerForSwap != null) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { selectedPlayerForSwap = null }
                            )
                        }
                    }
                }

                // 2D Tactical Pitch Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, RfmPitchLine, RoundedCornerShape(8.dp))
                        .background(RfmPitchGreen)
                        .testTag("pitch_tactics_board_container")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Mowed pitch horizontal stripes
                        val stripes = 8
                        val stripeH = h / stripes
                        for (i in 0 until stripes) {
                            if (i % 2 == 0) {
                                drawRect(
                                    color = Color(0x14000000),
                                    topLeft = Offset(0f, i * stripeH),
                                    size = androidx.compose.ui.geometry.Size(w, stripeH)
                                )
                            }
                        }

                        // Touchlines & Halfway line
                        drawLine(RfmPitchLine, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 2f)
                        drawCircle(RfmPitchLine, radius = 36f, center = Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawCircle(RfmPitchLine, radius = 3f, center = Offset(w / 2, h / 2))

                        // Top Opponent Penalty Area
                        drawRect(RfmPitchLine, Offset(w * 0.22f, 0f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.17f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawRect(RfmPitchLine, Offset(w * 0.36f, 0f), androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.07f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))

                        // Bottom Penalty Area (Our Goal)
                        drawRect(RfmPitchLine, Offset(w * 0.22f, h * 0.83f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.17f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawRect(RfmPitchLine, Offset(w * 0.36f, h * 0.93f), androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.07f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawCircle(RfmPitchLine, radius = 3f, center = Offset(w / 2, h * 0.89f))
                    }

                    // Interactive Player Tokens
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val pw = maxWidth
                        val ph = maxHeight
                        val density = androidx.compose.ui.platform.LocalDensity.current.density
                        val startingXI = remember(userTeam.players, selectedFormation, squadChangeCounter) { userTeam.getStartingXI() }

                        startingXI.forEachIndexed { slotIndex, player ->
                            val (baseXRatio, baseYRatio) = getTacticsFormationCoords(slotIndex, selectedFormation)
                            var dragDelta by remember { mutableStateOf(Offset.Zero) }
                            var isBeingDragged by remember { mutableStateOf(false) }
                            val isSelected = selectedPlayerForSwap?.id == player.id

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = ((pw.value * baseXRatio).dp + (if (isBeingDragged) dragDelta.x.dp else 0.dp) - 22.dp).roundToPx(),
                                            y = ((ph.value * baseYRatio).dp + (if (isBeingDragged) dragDelta.y.dp else 0.dp) - 22.dp).roundToPx()
                                        )
                                    }
                                    .zIndex(if (isBeingDragged) 10f else 1f)
                                    .pointerInput(player.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isBeingDragged = true
                                                dragDelta = Offset.Zero
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                dragDelta += amount
                                            },
                                            onDragEnd = {
                                                // Calculate nearest starter to drop location
                                                val dropPixelX = (pw.value * baseXRatio) * density + dragDelta.x
                                                val dropPixelY = (ph.value * baseYRatio) * density + dragDelta.y

                                                var targetPlayer: Player? = null
                                                var minDistance = Float.MAX_VALUE

                                                startingXI.forEachIndexed { targetIdx, otherPlayer ->
                                                    if (otherPlayer.id != player.id) {
                                                        val (targetXRatio, targetYRatio) = getTacticsFormationCoords(targetIdx, selectedFormation)
                                                        val targetPxX = (pw.value * targetXRatio) * density
                                                        val targetPxY = (ph.value * targetYRatio) * density
                                                        val dist = kotlin.math.hypot(dropPixelX - targetPxX, dropPixelY - targetPxY)
                                                        if (dist < minDistance && dist < 120f) {
                                                            minDistance = dist
                                                            targetPlayer = otherPlayer
                                                        }
                                                    }
                                                }

                                                if (targetPlayer != null) {
                                                    swapPlayerRoles(userTeam, player, targetPlayer!!)
                                                    RfmAudioEngine.playButtonClick()
                                                    squadChangeCounter++
                                                }

                                                isBeingDragged = false
                                                dragDelta = Offset.Zero
                                            },
                                            onDragCancel = {
                                                isBeingDragged = false
                                                dragDelta = Offset.Zero
                                            }
                                        )
                                    }
                                    .clickable {
                                        if (selectedPlayerForSwap == null) {
                                            selectedPlayerForSwap = player
                                        } else {
                                            if (selectedPlayerForSwap!!.id != player.id) {
                                                performSquadSwap(userTeam, selectedPlayerForSwap!!, player)
                                                RfmAudioEngine.playButtonClick()
                                                squadChangeCounter++
                                            }
                                            selectedPlayerForSwap = null
                                        }
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(48.dp)
                                ) {
                                    // Token Circle
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (isSelected) RfmGold else Color(userTeam.primaryColorHex),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = if (isSelected || isBeingDragged) 2.dp else 1.2.dp,
                                                color = if (isSelected || isBeingDragged) RfmAmber else Color.White,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = player.position.label,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(1.dp))

                                    // Name & OVR badge
                                    Surface(
                                        color = RfmNavyDark.copy(alpha = 0.88f),
                                        shape = RoundedCornerShape(2.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, RfmNavyBorder)
                                    ) {
                                        Text(
                                            text = "${player.name.substringAfterLast(" ")} ${player.overall}",
                                            color = Color.White,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT PANEL: SIDE PANEL WITH BENCH, RESERVES & STYLE SLIDERS (44%)
            Column(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. BENCH & RESERVES TRAY
                Text(
                    text = "BENCH & RESERVES (TAP TO SWAP INTO 11)",
                    color = RfmAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                val benchAndReserves = remember(userTeam.players, squadChangeCounter) {
                    userTeam.players.filter { !it.isStarting }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(benchAndReserves) { benchPlayer ->
                        val isSelected = selectedPlayerForSwap?.id == benchPlayer.id
                        Box(
                            modifier = Modifier
                                .width(88.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) RfmAmber.copy(alpha = 0.25f) else RfmNavyCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) RfmAmber else RfmNavyBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    if (selectedPlayerForSwap == null) {
                                        selectedPlayerForSwap = benchPlayer
                                    } else {
                                        performSquadSwap(userTeam, selectedPlayerForSwap!!, benchPlayer)
                                        RfmAudioEngine.playButtonClick()
                                        squadChangeCounter++
                                        selectedPlayerForSwap = null
                                    }
                                }
                                .padding(5.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = RfmNavyBorder,
                                        shape = RoundedCornerShape(2.dp)
                                    ) {
                                        Text(text = benchPlayer.position.label, color = RfmGold, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(1.dp))
                                    }
                                    Text(text = "${benchPlayer.overall}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Text(
                                    text = benchPlayer.name,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${benchPlayer.condition}% fit",
                                    color = if (benchPlayer.condition > 80) RfmPitchGreen else RfmAmber,
                                    fontSize = 7.5.sp
                                )
                            }
                        }
                    }
                }

                // 2. TEAM MENTALITY SLIDER
                Text(text = "TEAM MENTALITY", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Mentality.values().forEach { m ->
                        val isSelected = selectedMentality == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) RfmGold else RfmNavyCard)
                                .border(1.dp, if (isSelected) RfmAmber else RfmNavyBorder, RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedMentality = m
                                    userTeam.mentality = m
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when(m) {
                                    Mentality.ULTRA_DEFENSIVE -> "Def+"
                                    Mentality.DEFENSIVE -> "Def"
                                    Mentality.BALANCED -> "Bal"
                                    Mentality.ATTACKING -> "Att"
                                    Mentality.ALL_OUT_ATTACK -> "Att+"
                                },
                                color = if (isSelected) Color.Black else RfmTextSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. MATCH TEMPO & PASSING STYLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Tempo
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "TEMPO", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TempoStyle.values().forEach { tempo ->
                                val isSelected = selectedTempo == tempo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isSelected) RfmPitchGreen else RfmNavyCard)
                                        .clickable {
                                            selectedTempo = tempo
                                            userTeam.tempoStyle = tempo
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tempo.label.split(" ").first(),
                                        color = if (isSelected) Color.White else RfmTextSecondary,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Passing
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PASSING", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            PassingStyle.values().forEach { style ->
                                val isSelected = selectedPassing == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isSelected) RfmPitchGreen else RfmNavyCard)
                                        .clickable {
                                            selectedPassing = style
                                            userTeam.passingStyle = style
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style.label.split(" ").first(),
                                        color = if (isSelected) Color.White else RfmTextSecondary,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. PRESSING & TACKLING
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pressing
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PRESSING", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        PressingStyle.values().forEach { press ->
                            val isSelected = selectedPressing == press
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) RfmNavyCard else RfmNavySurface)
                                    .border(1.dp, if (isSelected) RfmGold else RfmNavyBorder, RoundedCornerShape(3.dp))
                                    .clickable {
                                        selectedPressing = press
                                        userTeam.pressingStyle = press
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = press.label,
                                    color = if (isSelected) RfmGold else RfmTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Tackling
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "TACKLING", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        TacklingStyle.values().forEach { tack ->
                            val isSelected = selectedTackling == tack
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) RfmNavyCard else RfmNavySurface)
                                    .border(1.dp, if (isSelected) RfmGold else RfmNavyBorder, RoundedCornerShape(3.dp))
                                    .clickable {
                                        selectedTackling = tack
                                        userTeam.tacklingStyle = tack
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tack.label,
                                    color = if (isSelected) RfmGold else RfmTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // 5. SET-PIECE TAKERS & LEADERSHIP
                Text(text = "SET-PIECE TAKERS & LEADERSHIP", color = RfmAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                val startingXI = userTeam.getStartingXI()

                Card(
                    colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RfmNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Captain
                        val captain = startingXI.find { it.id == captainId } ?: startingXI.firstOrNull()
                        SetPieceSelectorRow(
                            roleLabel = "👑 Team Captain",
                            currentName = captain?.name ?: "Unassigned",
                            detail = "Leadership & Morale",
                            onClick = {
                                val nextIdx = (startingXI.indexOfFirst { it.id == captainId } + 1) % startingXI.size.coerceAtLeast(1)
                                captainId = startingXI.getOrNull(nextIdx)?.id
                                userTeam.captainPlayerId = captainId
                            }
                        )

                        // Penalty Taker
                        val penTaker = startingXI.find { it.id == penTakerId } ?: startingXI.firstOrNull()
                        SetPieceSelectorRow(
                            roleLabel = "⚽ Penalty Taker",
                            currentName = penTaker?.name ?: "Unassigned",
                            detail = "SHO ${penTaker?.shooting ?: 0}",
                            onClick = {
                                val nextIdx = (startingXI.indexOfFirst { it.id == penTakerId } + 1) % startingXI.size.coerceAtLeast(1)
                                penTakerId = startingXI.getOrNull(nextIdx)?.id
                                userTeam.penaltyTakerId = penTakerId
                            }
                        )

                        // Free Kick Taker
                        val fkTaker = startingXI.find { it.id == fkTakerId } ?: startingXI.firstOrNull()
                        SetPieceSelectorRow(
                            roleLabel = "🎯 Direct Free Kicks",
                            currentName = fkTaker?.name ?: "Unassigned",
                            detail = "PAS ${fkTaker?.passing ?: 0} • SHO ${fkTaker?.shooting ?: 0}",
                            onClick = {
                                val nextIdx = (startingXI.indexOfFirst { it.id == fkTakerId } + 1) % startingXI.size.coerceAtLeast(1)
                                fkTakerId = startingXI.getOrNull(nextIdx)?.id
                                userTeam.freeKickTakerId = fkTakerId
                            }
                        )

                        // Corner Kick Taker
                        val crnTaker = startingXI.find { it.id == crnTakerId } ?: startingXI.firstOrNull()
                        SetPieceSelectorRow(
                            roleLabel = "🚩 Corner Kicks",
                            currentName = crnTaker?.name ?: "Unassigned",
                            detail = "PAS ${crnTaker?.passing ?: 0}",
                            onClick = {
                                val nextIdx = (startingXI.indexOfFirst { it.id == crnTakerId } + 1) % startingXI.size.coerceAtLeast(1)
                                crnTakerId = startingXI.getOrNull(nextIdx)?.id
                                userTeam.cornerTakerId = crnTakerId
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetPieceSelectorRow(
    roleLabel: String,
    currentName: String,
    detail: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RfmNavyCard, RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = roleLabel, color = RfmGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = detail, color = RfmTextSecondary, fontSize = 7.5.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = currentName, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "↻", color = RfmAmber, fontSize = 11.sp)
        }
    }
}

/**
 * Handles swapping between two players:
 * - If one is starter and one is bench -> swaps starting/substitute flags.
 * - If both are starters -> reorders team list so slots are swapped.
 */
private fun performSquadSwap(team: Team, playerA: Player, playerB: Player) {
    if (playerA.id == playerB.id) return

    val startingXI = team.getStartingXI()
    val aIsStarter = startingXI.any { it.id == playerA.id }
    val bIsStarter = startingXI.any { it.id == playerB.id }

    if (aIsStarter && !bIsStarter) {
        playerA.isStarting = false
        playerA.isSubstitute = true
        playerB.isStarting = true
        playerB.isSubstitute = false
    } else if (!aIsStarter && bIsStarter) {
        playerA.isStarting = true
        playerA.isSubstitute = false
        playerB.isStarting = false
        playerB.isSubstitute = true
    } else if (aIsStarter && bIsStarter) {
        swapPlayerRoles(team, playerA, playerB)
    }
}

private fun swapPlayerRoles(team: Team, playerA: Player, playerB: Player) {
    val idxA = team.players.indexOfFirst { it.id == playerA.id }
    val idxB = team.players.indexOfFirst { it.id == playerB.id }
    if (idxA != -1 && idxB != -1) {
        val temp = team.players[idxA]
        team.players[idxA] = team.players[idxB]
        team.players[idxB] = temp
    }
}

private fun autoSelectBestXI(team: Team) {
    team.players.forEach {
        it.isStarting = false
        it.isSubstitute = false
    }

    val gk = team.players.filter { it.position == PlayerPosition.GK && it.canPlay() }.maxByOrNull { it.overall }
        ?: team.players.first { it.position == PlayerPosition.GK }
    gk.isStarting = true

    val defs = team.players.filter { it.position.roleCategory == "Defender" && it.id != gk.id && it.canPlay() }
        .sortedByDescending { it.overall }
        .take(4)
    defs.forEach { it.isStarting = true }

    val mids = team.players.filter { it.position.roleCategory == "Midfielder" && !it.isStarting && it.canPlay() }
        .sortedByDescending { it.overall }
        .take(4)
    mids.forEach { it.isStarting = true }

    val fwds = team.players.filter { it.position.roleCategory == "Forward" && !it.isStarting && it.canPlay() }
        .sortedByDescending { it.overall }
        .take(2)
    fwds.forEach { it.isStarting = true }

    while (team.getStartingXI().size < 11) {
        val nextBest = team.players.filter { !it.isStarting && it.canPlay() }.maxByOrNull { it.overall }
        if (nextBest != null) nextBest.isStarting = true else break
    }

    team.players.filter { !it.isStarting && it.canPlay() }
        .sortedByDescending { it.overall }
        .take(7)
        .forEach { it.isSubstitute = true }
}

/**
 * Accurate vertical pitch coordinates (Top = Opponent Goal, Bottom = Our Goal)
 * GK at slot 0 stands at y=0.90 inside our penalty box!
 */
private fun getTacticsFormationCoords(slot: Int, formation: Formation): Pair<Float, Float> {
    return when (formation) {
        Formation.F_433 -> when (slot) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.14f, 0.74f) // LB
            2 -> Pair(0.38f, 0.77f) // CB
            3 -> Pair(0.62f, 0.77f) // CB
            4 -> Pair(0.86f, 0.74f) // RB
            5 -> Pair(0.28f, 0.52f) // LCM
            6 -> Pair(0.50f, 0.60f) // CDM
            7 -> Pair(0.72f, 0.52f) // RCM
            8 -> Pair(0.18f, 0.24f) // LW
            9 -> Pair(0.50f, 0.18f) // ST
            10 -> Pair(0.82f, 0.24f) // RW
            else -> Pair(0.50f, 0.50f)
        }
        Formation.F_4231 -> when (slot) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.14f, 0.74f) // LB
            2 -> Pair(0.38f, 0.77f) // CB
            3 -> Pair(0.62f, 0.77f) // CB
            4 -> Pair(0.86f, 0.74f) // RB
            5 -> Pair(0.36f, 0.62f) // LDM
            6 -> Pair(0.64f, 0.62f) // RDM
            7 -> Pair(0.20f, 0.38f) // LAM
            8 -> Pair(0.50f, 0.38f) // CAM
            9 -> Pair(0.80f, 0.38f) // RAM
            10 -> Pair(0.50f, 0.18f) // ST
            else -> Pair(0.50f, 0.50f)
        }
        Formation.F_352 -> when (slot) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.26f, 0.77f) // LCB
            2 -> Pair(0.50f, 0.78f) // CB
            3 -> Pair(0.74f, 0.77f) // RCB
            4 -> Pair(0.13f, 0.54f) // LWB
            5 -> Pair(0.36f, 0.54f) // CM
            6 -> Pair(0.50f, 0.42f) // CAM
            7 -> Pair(0.64f, 0.54f) // CM
            8 -> Pair(0.87f, 0.54f) // RWB
            9 -> Pair(0.38f, 0.20f) // ST
            10 -> Pair(0.62f, 0.20f) // ST
            else -> Pair(0.50f, 0.50f)
        }
        Formation.F_532 -> when (slot) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.13f, 0.70f) // LWB
            2 -> Pair(0.31f, 0.77f) // LCB
            3 -> Pair(0.50f, 0.78f) // CB
            4 -> Pair(0.69f, 0.77f) // RCB
            5 -> Pair(0.87f, 0.70f) // RWB
            6 -> Pair(0.30f, 0.50f) // LCM
            7 -> Pair(0.50f, 0.50f) // CM
            8 -> Pair(0.70f, 0.50f) // RCM
            9 -> Pair(0.38f, 0.20f) // ST
            10 -> Pair(0.62f, 0.20f) // ST
            else -> Pair(0.50f, 0.50f)
        }
        Formation.F_4312 -> when (slot) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.14f, 0.74f) // LB
            2 -> Pair(0.38f, 0.77f) // CB
            3 -> Pair(0.62f, 0.77f) // CB
            4 -> Pair(0.86f, 0.74f) // RB
            5 -> Pair(0.28f, 0.56f) // LCM
            6 -> Pair(0.50f, 0.60f) // CDM
            7 -> Pair(0.72f, 0.56f) // RCM
            8 -> Pair(0.50f, 0.38f) // CAM
            9 -> Pair(0.38f, 0.20f) // ST
            10 -> Pair(0.62f, 0.20f) // ST
            else -> Pair(0.50f, 0.50f)
        }
        else -> when (slot) { // 4-4-2
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.14f, 0.74f) // LB
            2 -> Pair(0.38f, 0.77f) // CB
            3 -> Pair(0.62f, 0.77f) // CB
            4 -> Pair(0.86f, 0.74f) // RB
            5 -> Pair(0.14f, 0.50f) // LM
            6 -> Pair(0.38f, 0.52f) // CM
            7 -> Pair(0.62f, 0.52f) // CM
            8 -> Pair(0.86f, 0.50f) // RM
            9 -> Pair(0.38f, 0.20f) // ST
            10 -> Pair(0.62f, 0.20f) // ST
            else -> Pair(0.50f, 0.50f)
        }
    }
}
