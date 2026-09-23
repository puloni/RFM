package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rfm.model.GameState
import com.example.rfm.navigation.RfmScreen
import com.example.rfm.ui.components.ModMenuDialog
import com.example.rfm.ui.components.RetroKeypadOverlay
import com.example.rfm.ui.components.RfmTopBar
import com.example.rfm.ui.screens.*
import com.example.rfm.ui.theme.RfmNavyDark
import com.example.rfm.viewmodel.RfmViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: RfmViewModel = viewModel()
                RfmApp(viewModel)
            }
        }
    }
}

@Composable
fun RfmApp(viewModel: RfmViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val activeEngine by viewModel.activeEngine.collectAsStateWithLifecycle()
    val showModDialog by viewModel.showModDialog.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RfmNavyDark)
        ) {
            // Top Bar for non-splash screens
            if (currentScreen != RfmScreen.SPLASH && currentScreen != RfmScreen.MAIN_MENU) {
                RfmTopBar(
                    gameState = gameState,
                    currentScreen = currentScreen,
                    onNavigateHome = { viewModel.navigateTo(RfmScreen.DASHBOARD) },
                    onOpenModMenu = { viewModel.openModDialog() },
                    onToggleKeypad = { viewModel.toggleRetroKeypad() },
                    onToggleSound = { viewModel.toggleSound() }
                )
            }

            // Screen Container
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    RfmScreen.SPLASH -> {
                        SplashScreen(
                            onContinue = { viewModel.navigateTo(RfmScreen.MAIN_MENU) }
                        )
                    }

                    RfmScreen.MAIN_MENU -> {
                        MainMenuScreen(
                            hasExistingSave = viewModel.hasSave("autosave"),
                            onNewCareer = { viewModel.navigateTo(RfmScreen.TEAM_SELECT) },
                            onContinueCareer = { viewModel.loadGame("autosave") },
                            onLoadSave = { viewModel.loadGame("autosave") },
                            onQuickMatch = { viewModel.navigateTo(RfmScreen.EXHIBITION_SETUP) },
                            onOpenModMenu = { viewModel.openModDialog() },
                            onToggleKeypad = { viewModel.toggleRetroKeypad() },
                            onToggleSound = { viewModel.toggleSound() },
                            isKeypadOn = gameState.isRetroKeypadEnabled,
                            isSoundOn = gameState.isSoundEnabled
                        )
                    }

                    RfmScreen.TEAM_SELECT -> {
                        TeamSelectScreen(
                            leagues = gameState.leagues,
                            onTeamSelected = { teamId, managerName ->
                                viewModel.startNewGame(teamId, managerName)
                            },
                            onBack = { viewModel.navigateTo(RfmScreen.MAIN_MENU) }
                        )
                    }

                    RfmScreen.DASHBOARD -> {
                        DashboardScreen(
                            gameState = gameState,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onStartMatch = { fixture -> viewModel.startMatch(fixture) },
                            onAnswerPressConference = { opt, fix -> viewModel.answerPressConference(opt, fix) },
                            onAdvanceToNextSeason = { viewModel.advanceToNextSeason() }
                        )
                    }

                    RfmScreen.SQUAD -> {
                        val userTeam = gameState.getUserTeam()
                        if (userTeam != null) {
                            SquadScreen(
                                userTeam = userTeam,
                                onRenewContract = { player -> viewModel.renewPlayerContract(player) },
                                onPhysioTreat = { player -> viewModel.physioTreatPlayer(player) },
                                onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                            )
                        }
                    }

                    RfmScreen.TACTICS -> {
                        val userTeam = gameState.getUserTeam()
                        if (userTeam != null) {
                            TacticsScreen(
                                userTeam = userTeam,
                                onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                            )
                        }
                    }

                    RfmScreen.MATCH_DAY -> {
                        val engine = activeEngine
                        if (engine != null) {
                            MatchScreen(
                                engine = engine,
                                isSimulating = isSimulating,
                                onTogglePlayback = { speed -> viewModel.toggleSimulationPlayback(speed) },
                                onInstantSimulate = { viewModel.instantSimulateMatch() },
                                onMakeSubstitution = { outId, inId ->
                                    viewModel.makeSubstitutionInMatch(outId, inId)
                                },
                                onFinishMatch = { viewModel.postMatchReturnToDashboard() }
                            )
                        }
                    }

                    RfmScreen.TRANSFERS -> {
                        val userTeam = gameState.getUserTeam()
                        if (userTeam != null) {
                            TransfersScreen(
                                userTeam = userTeam,
                                marketPlayers = gameState.transferMarket,
                                freeAgents = gameState.freeAgents,
                                loanMarket = gameState.loanMarket,
                                incomingOffers = gameState.activeTransferOffers.filter { it.isIncomingToUser && it.status == "PENDING" },
                                onBuyPlayer = { player, fee, wage ->
                                    viewModel.buyPlayer(player, fee, wage)
                                },
                                onSignFreeAgent = { player, wage ->
                                    viewModel.signFreeAgent(player, wage)
                                },
                                onLoanPlayer = { player, loanFee, wage ->
                                    viewModel.loanPlayer(player, loanFee, wage)
                                },
                                onRespondOffer = { offer, accept, counterFee ->
                                    viewModel.respondToTransferOffer(offer, accept, counterFee)
                                },
                                onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                            )
                        }
                    }

                    RfmScreen.TRAINING -> {
                        TrainingScreen(
                            gameState = gameState,
                            onRunTraining = { focus -> viewModel.runWeeklyTraining(focus) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.FINANCES -> {
                        val userTeam = gameState.getUserTeam()
                        if (userTeam != null) {
                            FinancesScreen(
                                userTeam = userTeam,
                                onUpgradeFacility = { type -> viewModel.upgradeFacility(type) },
                                onSetTicketPriceTier = { tier -> viewModel.setTicketPriceTier(tier) },
                                onSignSponsor = { cat, sponsor -> viewModel.signNewSponsor(cat, sponsor) },
                                onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                            )
                        }
                    }

                    RfmScreen.TABLE -> {
                        TableScreen(
                            leagues = gameState.leagues,
                            userTeamId = gameState.userTeamId,
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.FIXTURES -> {
                        FixturesScreen(
                            gameState = gameState,
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.LOAD_SAVE -> {
                        LoadSaveScreen(
                            onSaveSlot = { slot -> viewModel.saveGame(slot) },
                            onLoadSlot = { slot -> viewModel.loadGame(slot) },
                            hasSaveCheck = { slot -> viewModel.hasSave(slot) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.EXHIBITION_SETUP -> {
                        ExhibitionSetupScreen(
                            gameState = gameState,
                            onBack = { viewModel.navigateTo(RfmScreen.MAIN_MENU) },
                            onStartMatch = { homeTeam, awayTeam ->
                                val fixture = com.example.rfm.model.Fixture(
                                    id = "exhibition_${System.currentTimeMillis()}",
                                    round = 0,
                                    homeTeamId = homeTeam.id,
                                    awayTeamId = awayTeam.id
                                )
                                viewModel.startMatch(fixture)
                            }
                        )
                    }

                    RfmScreen.TROPHY_CABINET -> {
                        TrophyCabinetScreen(
                            gameState = gameState,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.YOUTH_ACADEMY -> {
                        YouthAcademyScreen(
                            gameState = gameState,
                            onPromoteProspect = { prospect -> viewModel.promoteYouthProspect(prospect) },
                            onSendScout = { region, cost -> viewModel.sendScoutToRegion(region, cost) },
                            onSignScoutedPlayer = { player -> viewModel.signScoutedPlayer(player) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.CUPS -> {
                        CupsScreen(
                            gameState = gameState,
                            onSimulateCupRound = { cup -> viewModel.simulateCupRound(cup) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.STAFF_MANAGEMENT -> {
                        StaffManagementScreen(
                            gameState = gameState,
                            onUpgradeStaff = { staff -> viewModel.upgradeBackroomStaff(staff) },
                            onHireStaff = { staff -> viewModel.upgradeBackroomStaff(staff) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }

                    RfmScreen.PRE_SEASON_TOUR -> {
                        PreSeasonTourScreen(
                            gameState = gameState,
                            onPlayTourMatch = { fixture -> viewModel.playPreSeasonTourMatch(fixture) },
                            onSimulateTourMatch = { fixture -> viewModel.simulatePreSeasonTourMatch(fixture) },
                            onBack = { viewModel.navigateTo(RfmScreen.DASHBOARD) }
                        )
                    }
                }

                // CRT / Scanline Retro Display Filter
                if (gameState.modSettings.crtRetroFilter) {
                    com.example.rfm.ui.components.RetroCrtOverlay()
                }
            }

            // Samsung GT-S8000 Virtual Hardware Keypad Overlay
            if (gameState.isRetroKeypadEnabled && currentScreen != RfmScreen.SPLASH) {
                RetroKeypadOverlay(
                    onKeyPress = { key ->
                        viewModel.handleKeypadInput(key)
                    }
                )
            }
        }

        // Mod Menu Dialog
        if (showModDialog) {
            ModMenuDialog(
                initialSettings = gameState.modSettings,
                onDismiss = { viewModel.closeModDialog() },
                onApplySettings = { newSettings ->
                    viewModel.applyModSettings(newSettings)
                }
            )
        }
    }
}
