package com.example.chess.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chess.data.entity.GameHistory
import com.example.chess.data.entity.UserStats
import com.example.chess.data.entity.UserAccount
import com.example.chess.model.*
import java.text.SimpleDateFormat
import java.util.*

// Luxurious Color Palette
val GoldenAccent = Color(0xFFFFD700)
val DarkCharcoal = Color(0xFF1E2022)
val DeepGold = Color(0xFFC5A059)
val WoodLight = Color(0xFFF0D9B5)
val WoodDark = Color(0xFFB58863)
val DarkLobbyBg = Color(0xFF121416)
val CardSurfaceColor = Color(0xFF23272A)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChessApp(viewModel: ChessViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val gameHistory by viewModel.gameHistory.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkLobbyBg
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() + slideInVertically { it / 2 } with fadeOut() + slideOutVertically { -it / 2 }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.SPLASH -> SplashScreen(viewModel = viewModel)
                Screen.AUTH -> AuthScreen(viewModel = viewModel)
                Screen.LOBBY -> LobbyScreen(
                    stats = userStats ?: UserStats(),
                    history = gameHistory,
                    viewModel = viewModel
                )
                Screen.MATCHMAKING -> MatchmakingScreen(viewModel = viewModel)
                Screen.GAME -> GameScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: ChessViewModel) {
    // Elegant entrance animation state
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 1200),
        label = "SplashScale"
    )

    val alpha = animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1200),
        label = "SplashAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkLobbyBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Chess Arena Official Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Chess Arena Official Logo",
                modifier = Modifier
                    .size(130.dp)
                    .background(CardSurfaceColor.copy(alpha = 0.6f), shape = RoundedCornerShape(24.dp))
                    .border(2.dp, GoldenAccent, shape = RoundedCornerShape(24.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CHESS ARENA",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Play • Compete • Conquer",
                color = GoldenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Professional Loading Effect
        CircularProgressIndicator(
            color = GoldenAccent,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Loading Secure Arena Shell...",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun AuthScreen(viewModel: ChessViewModel) {
    val authMode by viewModel.authMode.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkLobbyBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Official Chess Arena Logo for Authentication
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Chess Arena Official Logo",
            modifier = Modifier
                .size(90.dp)
                .background(CardSurfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                .border(1.5.dp, GoldenAccent, shape = RoundedCornerShape(20.dp))
                .padding(8.dp)
                .testTag("auth_screen_logo")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (authMode) {
                AuthMode.LOGIN -> "Welcome to Chess Arena"
                AuthMode.REGISTER -> "Create your Chess Arena Account"
                AuthMode.FORGOT_PASSWORD -> "Reset Chess Arena Password"
            },
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = when (authMode) {
                AuthMode.LOGIN -> "Enter credentials to unlock private tables"
                AuthMode.REGISTER -> "Register to start betting and climb leagues"
                AuthMode.FORGOT_PASSWORD -> "Reset link will be simulated securely"
            },
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Feedback indicators
        if (authError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = authError ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (authSuccessMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldenAccent.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, GoldenAccent, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = authSuccessMessage ?: "",
                    color = GoldenAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Form Fields
        if (authMode == AuthMode.REGISTER) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Unique Username", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldenAccent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("auth_username_field"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldenAccent,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("auth_email_field"),
            shape = RoundedCornerShape(10.dp)
        )

        if (authMode != AuthMode.FORGOT_PASSWORD) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6 chars)", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldenAccent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("auth_password_field"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (authMode == AuthMode.REGISTER) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldenAccent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("auth_confirm_password_field"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Main Submit Action
        Button(
            onClick = {
                when (authMode) {
                    AuthMode.LOGIN -> viewModel.login(email.trim(), password)
                    AuthMode.REGISTER -> viewModel.register(username.trim(), email.trim(), password, confirmPassword)
                    AuthMode.FORGOT_PASSWORD -> viewModel.forgotPassword(email.trim())
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_primary_button")
        ) {
            Text(
                text = when (authMode) {
                    AuthMode.LOGIN -> "SIGN IN SECURELY"
                    AuthMode.REGISTER -> "CREATE NEW WALLET & REGISTER"
                    AuthMode.FORGOT_PASSWORD -> "SEND SECURE LINK"
                },
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode toggling row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (authMode == AuthMode.LOGIN) {
                Text(
                    text = "No account? Register wallet",
                    color = GoldenAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.setAuthMode(AuthMode.REGISTER) }
                        .testTag("auth_toggle_mode_button")
                )
                Text(
                    text = "Forgot password?",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) }
                        .testTag("auth_forgot_password_button")
                )
            } else {
                Text(
                    text = "Back to Sign In",
                    color = GoldenAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                        .testTag("auth_toggle_mode_button"),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

enum class LobbyTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val testTag: String) {
    ARENA("Arena", Icons.Default.PlayArrow, "tab_arena"),
    REWARDS("Rewards", Icons.Default.Star, "tab_rewards"),
    LEADERBOARD("Leagues", Icons.Default.List, "tab_leaderboard"),
    SHOP("Shop", Icons.Default.ShoppingCart, "tab_shop"),
    CAREER("Career", Icons.Default.AccountCircle, "tab_career")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LobbyScreen(
    stats: UserStats,
    history: List<GameHistory>,
    viewModel: ChessViewModel
) {
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showNewSeasonConfirmation by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(LobbyTab.ARENA) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurfaceColor)
                    .statusBarsPadding()
            ) {
                // First Row: Official Crest Logo, Title, and Logout Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Chess Arena Official Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                .border(0.5.dp, GoldenAccent, shape = RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Chess Arena",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(36.dp).testTag("logout_toolbar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Sub-divider for extra visual depth
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), thickness = 0.5.dp)

                // Second Row (Visual Player Hub Header): Avatar, Coins, and ELO Rank
                val eloRankTitle = when {
                    stats.rating < 100 -> "Beginner ♟️"
                    stats.rating < 300 -> "Bronze 🛡️"
                    stats.rating < 600 -> "Silver ⚔️"
                    stats.rating < 1000 -> "Gold 🏆"
                    stats.rating < 1500 -> "Platinum 💎"
                    stats.rating < 2500 -> "Diamond 👑"
                    else -> "Grandmaster 🌟"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player Avatar & Nickname Profile
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldenAccent.copy(alpha = 0.15f), shape = CircleShape)
                                .border(1.dp, GoldenAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stats.username.ifEmpty { "P" }.take(1).uppercase(),
                                color = GoldenAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stats.username.ifEmpty { "Player" },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rank: $eloRankTitle",
                                color = GoldenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Coins and ELO Chips Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp))
                            .border(0.5.dp, GoldenAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Gold Coins Balance",
                                tint = GoldenAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.coinBalance}",
                                color = GoldenAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(12.dp)
                                .background(Color.DarkGray)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "${stats.rating} ELO",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardSurfaceColor,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lobby_bottom_nav")
            ) {
                LobbyTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldenAccent,
                            indicatorColor = GoldenAccent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = DarkLobbyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Elegant top general stats header (always visible so player knows rating and gold wallet!)
            LobbyHeader(
                stats = stats,
                onResetClick = { showResetConfirmation = true },
                onGoldClick = { viewModel.addFreeGoldCheat() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content with beautiful slide/fade transition
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (targetState.ordinal > initialState.ordinal) it else -it } togetherWith
                            fadeOut() + slideOutHorizontally { if (targetState.ordinal > initialState.ordinal) -it else it }
                },
                label = "TabTransition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    LobbyTab.ARENA -> {
                        PlayModesSelector(
                            stats = stats,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LobbyTab.REWARDS -> {
                        RewardsTabContent(
                            stats = stats,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LobbyTab.LEADERBOARD -> {
                        LeaderboardTabContent(
                            stats = stats,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LobbyTab.SHOP -> {
                        ShopTabContent(
                            stats = stats,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LobbyTab.CAREER -> {
                        CareerStatsSection(
                            stats = stats,
                            history = history,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Progress?", color = Color.White) },
            text = { Text("This will clear your local active board saved progress, game logs, and local settings.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetProfileCheat()
                        showResetConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = CardSurfaceColor
        )
    }

    if (showNewSeasonConfirmation) {
        AlertDialog(
            onDismissRequest = { showNewSeasonConfirmation = false },
            title = { Text("Start New Season?", color = Color.White) },
            text = { Text("This will clear all rating pools, leaderboard standings, and battle win/loss/draw historic scores for every user to start the tournament brand new. Accounts, purchases, and coin balance are safely retained.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSeason()
                        showNewSeasonConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldenAccent)
                ) {
                    Text("START FRESH SEASON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSeasonConfirmation = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = CardSurfaceColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerStatsSection(
    stats: UserStats,
    history: List<GameHistory>,
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var currentInputName by remember(stats.username) { mutableStateOf(stats.username) }
    var showNewSeasonConfirmInCareer by remember { mutableStateOf(false) }
    
    var matchSFXEnabled by remember { mutableStateOf(true) }
    var matchNotifyEnabled by remember { mutableStateOf(true) }

    if (showNewSeasonConfirmInCareer) {
        AlertDialog(
            onDismissRequest = { showNewSeasonConfirmInCareer = false },
            title = { Text("Start New Season?", color = Color.White) },
            text = { Text("This will clear all rating pools, leaderboard standings, and battle win/loss/draw historic scores for every user to start the tournament brand new. Accounts, purchases, and coin balance are safely retained.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSeason()
                        showNewSeasonConfirmInCareer = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldenAccent)
                ) {
                    Text("START FRESH SEASON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSeasonConfirmInCareer = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = CardSurfaceColor
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Arena Nickname", color = Color.White) },
            text = {
                Column {
                    Text("Enter your new public gaming moniker:", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentInputName,
                        onValueChange = { currentInputName = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldenAccent,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentInputName.isNotBlank()) {
                            viewModel.updateUsername(currentInputName)
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldenAccent)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = CardSurfaceColor
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Profile details Card with Pen pencil edit tool
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Chess Arena Profile",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stats.username,
                            color = GoldenAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showEditNameDialog = true },
                            modifier = Modifier.size(24.dp).testTag("edit_profile_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Nickname",
                                tint = GoldenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(text = "Rating: ${stats.rating} ELO", color = Color.White, fontSize = 13.sp)
                }

                Box(
                    modifier = Modifier
                        .background(GoldenAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GOLD WALLET", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${stats.coinBalance}", color = GoldenAccent, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // ELO Tier Card
        val eloRankTitle = when {
            stats.rating < 100 -> "Beginner ♟️"
            stats.rating < 300 -> "Bronze 🛡️"
            stats.rating < 600 -> "Silver ⚔️"
            stats.rating < 1000 -> "Gold 🏆"
            stats.rating < 1500 -> "Platinum 💎"
            stats.rating < 2500 -> "Diamond 👑"
            else -> "Grandmaster 🌟"
        }

        val rankSubText = when {
            stats.rating < 100 -> "Practice with Novice AI to build chess foundations!"
            stats.rating < 300 -> "You have sharp tactics. Keep beating Medium AI!"
            stats.rating < 600 -> "Elite positional skills. Try taking down Hard AI!"
            stats.rating < 1000 -> "Show off your golden master strategies in the arenas!"
            stats.rating < 1500 -> "Incredible performance, Platinum tier master!"
            stats.rating < 2500 -> "Close to chess legend. Dominate your opponents!"
            else -> "Ultimate legends status. Defend your throne in high stakes!"
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CURRENT CHESS RANK",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = eloRankTitle,
                    color = GoldenAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rankSubText,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // Season Management Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SEASON TOURNAMENT MANAGEMENT",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "A new cycle clears all standings and ratings (resetting ELO rating to 0, Rank to Beginner, and cleaning wins/losses/draws counters) for every single user in the system to launch a competitive starting line.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { showNewSeasonConfirmInCareer = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("start_new_season_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Season Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START NEW TOURNAMENT SEASON",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Settings adjustments block
        Text(
            text = "Chess Arena Settings",
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gameplay SFX", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Play sounds during moves, capture, and check", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = matchSFXEnabled,
                        onCheckedChange = { matchSFXEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldenAccent,
                            checkedTrackColor = GoldenAccent.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("match_sfx_switch")
                    )
                }

                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Chess Arena Notifications", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Alert when a Chess Arena challenger connects", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = matchNotifyEnabled,
                        onCheckedChange = { matchNotifyEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldenAccent,
                            checkedTrackColor = GoldenAccent.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("match_notify_switch")
                    )
                }
            }
        }

        // Detailed Statistics Grid (Record Counters)
        Text(
            text = "CAREER BREAKDOWN",
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Local AI / PVP Box
            val totalLocal = stats.localWins + stats.localLosses + stats.localDraws
            val localWinRate = if (totalLocal > 0) (stats.localWins * 100f / totalLocal).toInt() else 0
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LOCAL AI & PVP",
                        color = GoldenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$totalLocal Played",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Won: ${stats.localWins}", color = Color(0xFF66BB6A), fontSize = 12.sp)
                    Text(text = "Lost: ${stats.localLosses}", color = Color(0xFFEF5350), fontSize = 12.sp)
                    Text(text = "Drawn: ${stats.localDraws}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { localWinRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Color(0xFF66BB6A),
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$localWinRate% Win Rate",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Multiplayer Arenas Box
            val totalMulti = stats.multiWins + stats.multiLosses + stats.multiDraws
            val multiWinRate = if (totalMulti > 0) (stats.multiWins * 100f / totalMulti).toInt() else 0
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BATTLE MULTIPLAYER",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$totalMulti Played",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Won: ${stats.multiWins}", color = Color(0xFF66BB6A), fontSize = 12.sp)
                    Text(text = "Lost: ${stats.multiLosses}", color = Color(0xFFEF5350), fontSize = 12.sp)
                    Text(text = "Drawn: ${stats.multiDraws}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { multiWinRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Color(0xFF66BB6A),
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$multiWinRate% Win Rate",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Match Logbook History lists
        Text(
            text = "MATCH PLAY HISTORY",
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (history.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matches stored in local database yet.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            history.forEach { match ->
                val dateString = remember(match.timestamp) {
                    val date = java.util.Date(match.timestamp)
                    val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                    format.format(date)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val winColor = when (match.result) {
                                "WON", "WIN" -> Color(0xFF66BB6A)
                                "LOST", "LOSS" -> Color(0xFFEF5350)
                                else -> Color.Gray
                            }
                            Text(
                                text = match.result,
                                color = winColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(text = "vs ${match.operandName}", color = Color.White, fontSize = 12.sp)
                            Text(text = "Delta: ${if (match.coinsDelta >= 0) "+" else ""}${match.coinsDelta} Coins ($dateString)", color = GoldenAccent, fontSize = 10.sp)
                        }
                        
                        Text(
                            text = match.mode,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Chest Rewards / Simulated Booster Box (Interactive Option)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentServerTime by viewModel.currentServerTimeFlow.collectAsStateWithLifecycle()
            val isClaimedThisWeek = currentServerTime != -1L && currentServerTime < (stats.lastWeeklyChestClaimTime + 7 * 24 * 60 * 60 * 1000L)
            val remainingMs = (stats.lastWeeklyChestClaimTime + 7 * 24 * 60 * 60 * 1000L) - currentServerTime
            val remainingText = if (remainingMs > 0L) {
                val seconds = (remainingMs / 1000) % 60
                val minutes = (remainingMs / (1000 * 60)) % 60
                val hours = (remainingMs / (1000 * 60 * 60)) % 24
                val days = (remainingMs / (1000 * 60 * 60 * 24))
                "${days}d ${hours}h ${minutes}m"
            } else ""

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GOLD CHEST BONUS",
                        color = GoldenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isClaimedThisWeek) "Chest claimed! Come back next week." else "Claim once per week only for +150 test-coins!",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.addFreeGoldCheat() },
                    enabled = !isClaimedThisWeek,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenAccent,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isClaimedThisWeek) Icons.Default.Lock else Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = if (isClaimedThisWeek) Color.Gray else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isClaimedThisWeek) remainingText else "+150",
                            color = if (isClaimedThisWeek) Color.Gray else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // About Chess Arena Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered About Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Chess Arena Official Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                        .border(1.dp, GoldenAccent, shape = RoundedCornerShape(12.dp))
                        .padding(6.dp)
                        .testTag("about_page_logo")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "About Chess Arena",
                    color = GoldenAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Welcome to Chess Arena, the ultimate high-stakes interactive battle ground for chess masterminds! Challenge advanced chess engines, customize luxurious game modes, scale rankings in real-time, and compete in premium multiplayer arenas with digital coin tokens.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Version: 1.0.0 (Chess Arena Edition)",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Secure Sandbox Protocol",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LobbyHeader(
    stats: UserStats,
    onResetClick: () -> Unit,
    onGoldClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldenAccent, DeepGold)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stats.username,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GoldenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.rating} Rating",
                            color = GoldenAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Coin counter wallet click triggers quick free cheat gold adding! Let's make it highly satisfying
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onGoldClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Coins balance",
                    tint = GoldenAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stats.coinBalance.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("coin_balance")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Get coins",
                    tint = GoldenAccent.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Minimal Reset Option
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Progress",
                    tint = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PlayModesSelector(
    stats: UserStats,
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (!stats.savedBoardStateRaw.isNullOrEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldenAccent.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.5.dp, GoldenAccent, RoundedCornerShape(12.dp))
                    .clickable { viewModel.resumeSavedGame() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RESUME SAVED MATCH",
                            color = GoldenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You have a suspended game: ${stats.savedGameMode} vs ${stats.savedOpponentName}",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.resumeSavedGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Text(
            text = "BATTLE ARENA",
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Horizontal scroll/grid list of Game modes
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "EARN GOLD (VS COMPUTER)",
                    color = GoldenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            // Local AI Options
            item {
                PlayModeCard(
                    title = "Local AI - Novice (Easy)",
                    description = "Excellent for practicing openings. Win base coins and scoring bonuses!",
                    entryFee = 0,
                    prize = "Coin Reward: Score based + 10 Bonus",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.startLocalMatch("Local AI Easy", true) }
                )
            }
            item {
                PlayModeCard(
                    title = "Local AI - Tactician (Medium)",
                    description = "Provides standard positional challenges. Earn 1.5x score coins!",
                    entryFee = 0,
                    prize = "Coin Reward: Score based + 25 Bonus",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.startLocalMatch("Local AI Medium", true) }
                )
            }
            item {
                PlayModeCard(
                    title = "Local AI - Grandmaster (Hard)",
                    description = "Plays deep minimax foresight. High stakes score multiplier!",
                    entryFee = 0,
                    prize = "Coin Reward: Score based + 60 Bonus",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.startLocalMatch("Local AI Hard", true) }
                )
            }

            // PvP Local
            item {
                Text(
                    text = "FRIENDLY MATCHES",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }
            item {
                PlayModeCard(
                    title = "Pass & Play (Local PvP)",
                    description = "Take turns on a single screen with your friends. Zero cost, standard fun!",
                    entryFee = 0,
                    prize = "No Coins Reward",
                    icon = Icons.Default.Person,
                    onClick = { viewModel.startLocalMatch("Local PvP", false) }
                )
            }

            // Multiplayer Arenas
            item {
                Text(
                    text = "HIGH STAKES PREMIUM MULTIPLAYER",
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }
            items(viewModel.arenas) { arena ->
                val hasFunds = stats.coinBalance >= arena.entryFee
                PlayModeCard(
                    title = arena.name,
                    description = "Risk ${arena.entryFee} coins. Beat other grandmaster bots to seize the jackpot!",
                    entryFee = arena.entryFee,
                    prize = "Winner Prize: ${arena.prizeMoney} Gold (+${arena.ratingGain} rating)",
                    icon = Icons.Default.Star,
                    hasFunds = hasFunds,
                    onClick = { viewModel.startMatchmaking(arena) }
                )
            }

            // Room-ID Private custom multiplayer matches
            item {
                Text(
                    text = "PRIVATE GAME WITH ROOM ID",
                    color = GoldenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, GoldenAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Create or Join Private Arena Room",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Risk 10 Coins. Winner grabs 100 Coins! Draw refunds 40.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val inputVal by viewModel.roomCodeInput.collectAsStateWithLifecycle()
                        
                        OutlinedTextField(
                            value = inputVal,
                            onValueChange = { viewModel.onRoomCodeInputChange(it) },
                            placeholder = { Text("Enter 6-8 digit Room Code", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldenAccent,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("room_id_input_field"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.createCustomRoom() },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_room_btn")
                            ) {
                                Text("CREATE ROOM", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.joinCustomRoom(inputVal) },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceColor),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GoldenAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("join_room_btn")
                            ) {
                                Text("JOIN ROOM", color = GoldenAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayModeCard(
    title: String,
    description: String,
    entryFee: Int,
    prize: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasFunds: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasFunds, onClick = onClick)
            .shadow(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasFunds) CardSurfaceColor else CardSurfaceColor.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.2f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (entryFee > 0) GoldenAccent else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (hasFunds) Color.White else Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stakes",
                        tint = GoldenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = prize,
                        color = GoldenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (entryFee > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ENTRY", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = "$entryFee Gold",
                        color = if (hasFunds) GoldenAccent else Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MatchesHistorySection(
    history: List<GameHistory>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MATCH LOGS",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (history.isNotEmpty()) {
                Text(
                    text = "Clear stats",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onClearHistory() }
                        .padding(4.dp)
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "History empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matches played yet.\nEnter against AI to earn your first coins!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { record ->
                    HistoryItem(record = record)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: GameHistory) {
    val dateStr = try {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    } catch (e: Exception) {
        "Just Now"
    }

    val resultColor = when (record.result) {
        "WON", "WHITE WON" -> Color(0xFF66BB6A)
        "LOST", "BLACK WON" -> Color(0xFFEF5350)
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(resultColor, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = record.result.take(1),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "vs ${record.operandName}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${record.mode} • $dateStr",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (record.coinsDelta != 0) {
                    val sign = if (record.coinsDelta > 0) "+" else ""
                    val coinColor = if (record.coinsDelta > 0) GoldenAccent else Color.Red
                    Text(
                        text = "$sign${record.coinsDelta} Gold",
                        color = coinColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (record.ratingDelta != 0) {
                    val sign = if (record.ratingDelta > 0) "+" else ""
                    val ratingColor = if (record.ratingDelta > 0) Color(0xFF66BB6A) else Color(0xFFEF5350)
                    Text(
                        text = "$sign${record.ratingDelta} ELO",
                        color = ratingColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MatchmakingScreen(viewModel: ChessViewModel) {
    val arena by viewModel.matchmakingArena.collectAsStateWithLifecycle()
    val timerSec by viewModel.matchmakingTimeSec.collectAsStateWithLifecycle()
    val createdRoomCode by viewModel.createdRoomCode.collectAsStateWithLifecycle()
    val isHost by viewModel.isCustomRoomHost.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkLobbyBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                color = GoldenAccent,
                strokeWidth = 6.dp
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Searching",
                tint = GoldenAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (createdRoomCode != null) {
            Text(
                text = if (isHost) "PRIVATE ROOM CREATED" else "CONNECTING TO PRIVATE ROOM",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .border(2.dp, GoldenAccent, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ROOM CODE",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = createdRoomCode ?: "",
                        color = GoldenAccent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isHost) "Waiting for guest... Share this room code!" else "Connecting to multiplayer channel...",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            if (isHost) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.triggerBotForCustomRoom() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_quick_bot_match_btn")
                ) {
                    Text("START QUICK BOT MATCH", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "ENTERING ${arena?.name?.uppercase() ?: "BATTLE ARENA"}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Matchmaking in progress... (${timerSec}s)",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Prize Pool: ${if (createdRoomCode != null) 100 else (arena?.prizeMoney ?: 0)} Gold Coins",
            color = GoldenAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.leaveCustomRoom() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("cancel_matchmaking")
        ) {
            Text("CANCEL & REFUND ENTRY FEE", color = Color.White)
        }
    }
}

@Composable
fun GameScreen(viewModel: ChessViewModel) {
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val boardState by viewModel.boardState.collectAsStateWithLifecycle()
    val gameMode by viewModel.gameMode.collectAsStateWithLifecycle()
    val playerColor by viewModel.playerColor.collectAsStateWithLifecycle()
    val selectedSquare by viewModel.selectedSquare.collectAsStateWithLifecycle()
    val validMoves by viewModel.validMovesForSelected.collectAsStateWithLifecycle()
    val opponentName by viewModel.opponentName.collectAsStateWithLifecycle()
    val opponentRating by viewModel.opponentRating.collectAsStateWithLifecycle()
    
    val gameScore by viewModel.gameScore.collectAsStateWithLifecycle()
    val coinsToEarn by viewModel.coinsToEarn.collectAsStateWithLifecycle()

    val capturedByWhite by viewModel.capturedByWhite.collectAsStateWithLifecycle()
    val capturedByBlack by viewModel.capturedByBlack.collectAsStateWithLifecycle()

    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val opponentChat by viewModel.opponentChat.collectAsStateWithLifecycle()
    val isThinking by viewModel.isOpponentThinking.collectAsStateWithLifecycle()

    // Game Over stats
    val showGameOver by viewModel.showGameOverDialog.collectAsStateWithLifecycle()
    val gameOverWinner by viewModel.gameOverWinner.collectAsStateWithLifecycle()
    val gameOverReason by viewModel.gameOverReason.collectAsStateWithLifecycle()
    val coinsEarnedResult by viewModel.coinsEarnedResult.collectAsStateWithLifecycle()
    val ratingDeltaResult by viewModel.ratingDeltaResult.collectAsStateWithLifecycle()

    // Flipping the view: Black player is at the bottom, White at top when playing as Black
    val isFlipped = playerColor == PieceColor.BLACK

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideLayout = maxWidth > 600.dp

        if (isWideLayout) {
            // Tablet Landscape: Board on the left, player details and logs on the right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Board on left (equal aspect box)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoardView(
                        boardState = boardState,
                        isFlipped = isFlipped,
                        selectedSquare = selectedSquare,
                        validMoves = validMoves,
                        onSquareClick = { viewModel.handleSquareClick(it) },
                        selectedTheme = userStats?.selectedTheme ?: "Classic",
                        selectedPieces = userStats?.selectedPieces ?: "Standard"
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Sidebar controls on right
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile/Match header
                    Column {
                        MatchStatsHeader(
                            opponentName = opponentName,
                            opponentRating = opponentRating,
                            gameMode = gameMode,
                            isThinking = isThinking,
                            opponentChatMsg = opponentChat
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CapturedPiecesIndicator(
                            capturedByWhite = capturedByWhite,
                            capturedByBlack = capturedByBlack,
                            playerColor = playerColor
                        )
                    }

                    // Score tracker & Exit buttons
                    Column {
                        ScoresDashboard(
                            gameScore = gameScore,
                            coinsToEarn = coinsToEarn,
                            statusMessage = statusMessage,
                            isMultiplayer = gameMode.contains("Multiplayer")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GameControlButtons(
                            onResign = { viewModel.navigateToLobby() },
                            onUndo = { viewModel.undoLastMove() },
                            onSave = { viewModel.saveCurrentGame() },
                            onRestart = { viewModel.restartCurrentMatch() },
                            isMultiplayer = gameMode.contains("Arena") || gameMode.contains("Multiplayer")
                        )
                    }
                }
            }
        } else {
            // Standard Mobile Layout: Vertical Stacking
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Opponent Details
                MatchStatsHeader(
                    opponentName = opponentName,
                    opponentRating = opponentRating,
                    gameMode = gameMode,
                    isThinking = isThinking,
                    opponentChatMsg = opponentChat
                )

                // Top Captured list (captured by opponent, or captured by player)
                CapturedPiecesIndicator(
                    capturedByWhite = capturedByWhite,
                    capturedByBlack = capturedByBlack,
                    playerColor = playerColor
                )

                // The Chessboard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoardView(
                        boardState = boardState,
                        isFlipped = isFlipped,
                        selectedSquare = selectedSquare,
                        validMoves = validMoves,
                        onSquareClick = { viewModel.handleSquareClick(it) },
                        selectedTheme = userStats?.selectedTheme ?: "Classic",
                        selectedPieces = userStats?.selectedPieces ?: "Standard"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live scores tracker dashboard
                ScoresDashboard(
                    gameScore = gameScore,
                    coinsToEarn = coinsToEarn,
                    statusMessage = statusMessage,
                    isMultiplayer = gameMode.contains("Multiplayer")
                )

                // Game Control Buttons
                Spacer(modifier = Modifier.height(8.dp))
                GameControlButtons(
                    onResign = { viewModel.navigateToLobby() },
                    onUndo = { viewModel.undoLastMove() },
                    onSave = { viewModel.saveCurrentGame() },
                    onRestart = { viewModel.restartCurrentMatch() },
                    isMultiplayer = gameMode.contains("Arena") || gameMode.contains("Multiplayer")
                )
            }
        }
    }

    if (showGameOver) {
        AlertDialog(
            onDismissRequest = { /* forced action */ },
            title = {
                Text(
                    text = "MATCH RESULTS",
                    color = GoldenAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val message = when {
                        gameOverWinner == null -> "Perfect stalemate! Match resulted in a DRAW."
                        gameMode == "Local PvP" -> "Victory achieved by ${if (gameOverWinner == PieceColor.WHITE) "WHITE" else "BLACK"}!"
                        gameOverWinner == playerColor -> "VICTORY! You checkmated the opponent perfectly."
                        else -> "DEFEAT! Your king was cornered."
                    }
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Reason: $gameOverReason", color = Color.Gray, fontSize = 13.sp)

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GOLD TRANSACTION", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = "${if (coinsEarnedResult >= 0) "+" else ""}$coinsEarnedResult Coins",
                                color = if (coinsEarnedResult >= 0) GoldenAccent else Color.Red,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!gameMode.contains("Local PvP") && gameMode.contains("Multiplayer")) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RATING ACCREDITATION", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    text = "${if (ratingDeltaResult >= 0) "+" else ""}$ratingDeltaResult ELO",
                                    color = if (ratingDeltaResult >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissGameOverAndLobby() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_game_over_button")
                ) {
                    Text("OKAY", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardSurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun MatchStatsHeader(
    opponentName: String,
    opponentRating: Int,
    gameMode: String,
    isThinking: Boolean,
    opponentChatMsg: String?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (gameMode == "Local PvP") Icons.Default.Person else Icons.Default.Build,
                            contentDescription = "Opponent type icon",
                            tint = GoldenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = opponentName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (gameMode == "Local PvP") "Hotseat PvP Local" else "Rating: $opponentRating",
                            color = Color.LightGray.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                if (isThinking) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = GoldenAccent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Thinking...",
                            color = GoldenAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Simulated Online Multiplayer Chat Bubble
            opponentChatMsg?.let { msg ->
                Card(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldenAccent),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 42.dp, y = (-26).dp)
                        .shadow(2.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CapturedPiecesIndicator(
    capturedByWhite: List<ChessPiece>,
    capturedByBlack: List<ChessPiece>,
    playerColor: PieceColor
) {
    // Show what is captured in the perspective of the player.
    // If player is white, we captured Black pieces (represented under capturedByWhite).
    // If player is black, we captured White pieces (represented under capturedByBlack).
    val playerCaptures = if (playerColor == PieceColor.WHITE) capturedByWhite else capturedByBlack
    val opponentCaptures = if (playerColor == PieceColor.WHITE) capturedByBlack else capturedByWhite

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player's trophies
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your Captures: ", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                playerCaptures.take(12).forEach { piece ->
                    Text(
                        text = piece.unicode,
                        color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black,
                        fontSize = 16.sp,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(color = Color.Gray, blurRadius = 1f)
                        )
                    )
                }
                if (playerCaptures.size > 12) {
                    Text("+${playerCaptures.size - 12}", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // Opponent's trophies
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Opponent's: ", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                opponentCaptures.take(12).forEach { piece ->
                    Text(
                        text = piece.unicode,
                        color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black,
                        fontSize = 16.sp,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(color = Color.Gray, blurRadius = 1f)
                        )
                    )
                }
                if (opponentCaptures.size > 12) {
                    Text("+${opponentCaptures.size - 12}", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ScoresDashboard(
    gameScore: Int,
    coinsToEarn: Int,
    statusMessage: String,
    isMultiplayer: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "CURRENT MATCH STATS", color = Color.Gray, fontSize = 10.sp)
                Text(
                    text = statusMessage,
                    color = if (statusMessage.contains("Check")) Color.Red else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("status_message")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MATCH SCORE", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = gameScore.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isMultiplayer) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("EST. EARN", color = Color.Gray, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldenAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$coinsToEarn Gold",
                                color = GoldenAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("coins_to_earn")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameControlButtons(
    onResign: () -> Unit,
    onUndo: () -> Unit,
    onSave: () -> Unit,
    onRestart: () -> Unit,
    isMultiplayer: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isMultiplayer) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUndo,
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("game_undo_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Undo", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("UNDO", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("game_restart_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESTART", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).testTag("game_save_button")
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Save Match", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        OutlinedButton(
            onClick = onResign,
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("resign_button")
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESIGN / EXIT")
        }
    }
}

@Composable
fun ChessBoardView(
    boardState: BoardState,
    isFlipped: Boolean,
    selectedSquare: Position?,
    validMoves: List<Move>,
    onSquareClick: (Position) -> Unit,
    selectedTheme: String = "Classic",
    selectedPieces: String = "Standard"
) {
    // Dynamic theme coloring mapping
    val themeColors = when (selectedTheme) {
        "Wooden" -> Pair(Color(0xFFEADBCA), Color(0xFF8A5A36)) // Walnut
        "Neon" -> Pair(Color(0xFF2B3A4A), Color(0xFF0F172A))   // Cyber grid
        "Gold" -> Pair(Color(0xFFFFF5CC), Color(0xFF9F7A1A))   // Imperial gold
        "Diamond" -> Pair(Color(0xFFE0F7FA), Color(0xFF00ACC1)) // Diamond cyan
        else -> Pair(WoodLight, WoodDark) // Classic traditional
    }
    val themeLightCell = themeColors.first
    val themeDarkCell = themeColors.second

    val borderThemeColor = when (selectedTheme) {
        "Wooden" -> Color(0xFF5C3C24)
        "Neon" -> Color(0xFF00FFCC)
        "Gold" -> Color(0xFFFFD700)
        "Diamond" -> Color(0xFF00E5FF)
        else -> WoodDark
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(4.dp, borderThemeColor, RoundedCornerShape(4.dp))
            .shadow(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val range = if (isFlipped) 0..7 else 7 downTo 0
            for (row in range) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..7) {
                        val pos = Position(row, col)
                        val piece = boardState.getPiece(pos)
                        val isLight = (row + col) % 2 == 0
                        
                        val isSelected = selectedSquare == pos
                        val isValidMoveTarget = validMoves.any { it.to == pos }
                        val isKingInCheck = piece != null && piece.type == PieceType.KING && piece.color == boardState.turn && boardState.checkStatus == BoardState.ChessStatus.CHECK

                        // Choose cell color
                        val squareColor = when {
                            isSelected -> Color(0x66FFD700) // Glowing Gold selection
                            isKingInCheck -> Color(0x7FEE2C2C) // Soft red pulsing check indicator
                            isLight -> themeLightCell
                            else -> themeDarkCell
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(squareColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null, // Disable default intrusive ripple for perfect visual custom square clicks!
                                    onClick = { onSquareClick(pos) }
                                )
                                .testTag("square_${row}_${col}"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render coordinates labels on bottom and left columns for authenticity!
                            val showRankLabel = col == if (isFlipped) 7 else 0
                            val showFileLabel = row == if (isFlipped) 0 else 7

                            if (showRankLabel) {
                                Text(
                                    text = (row + 1).toString(),
                                    color = if (isLight) themeDarkCell else themeLightCell,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(2.dp)
                                )
                            }

                            if (showFileLabel) {
                                val fileChar = ('a' + col).toString()
                                Text(
                                    text = fileChar,
                                    color = if (isLight) themeDarkCell else themeLightCell,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp)
                                )
                            }

                            // Render chess Piece
                            piece?.let { p ->
                                val pieceSymbol = when (selectedPieces) {
                                    "Fantasy" -> {
                                        when (p.type) {
                                            PieceType.KING -> "👑"
                                            PieceType.QUEEN -> "🔮"
                                            PieceType.ROOK -> "🏰"
                                            PieceType.BISHOP -> "🧙"
                                            PieceType.KNIGHT -> "🦄"
                                            PieceType.PAWN -> "🛡️"
                                        }
                                    }
                                    "Modern" -> {
                                        when (p.type) {
                                            PieceType.KING -> "K"
                                            PieceType.QUEEN -> "Q"
                                            PieceType.ROOK -> "R"
                                            PieceType.BISHOP -> "B"
                                            PieceType.KNIGHT -> "N"
                                            PieceType.PAWN -> "P"
                                        }
                                    }
                                    else -> p.unicode
                                }

                                val isModern = selectedPieces == "Modern"
                                val pieceColor = if (isModern) {
                                    if (p.color == PieceColor.WHITE) Color(0xFF00FFCC) else Color(0xFFFF007F)
                                } else {
                                    if (p.color == PieceColor.WHITE) Color.White else Color(0xFF1E1E1E)
                                }

                                val shadowColor = if (p.color == PieceColor.WHITE) Color(0xFF333333) else Color(0x99FFFFFF)
                                Text(
                                    text = pieceSymbol,
                                    color = pieceColor,
                                    fontSize = if (selectedPieces == "Fantasy") 28.sp else 32.sp,
                                    fontWeight = if (isModern) FontWeight.ExtraBold else FontWeight.Bold,
                                    style = LocalTextStyle.current.copy(
                                        shadow = Shadow(color = shadowColor, blurRadius = 1f)
                                    ),
                                    modifier = Modifier.testTag("piece_${p.color}_${p.type}")
                                )
                            }

                            // Render valid move dot selector
                            if (isValidMoveTarget) {
                                if (piece == null) {
                                    // Empty square move target
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color(0x7F4CAF50), shape = CircleShape)
                                    )
                                } else {
                                    // Attack capture spot (outward circle outline)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .border(3.dp, Color(0xCCFF5252), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// NEW MENUS TAB COMPONENTS
// ==========================================

@Composable
fun RewardsTabContent(
    stats: UserStats,
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Login Banner
        val currentServerTime by viewModel.currentServerTimeFlow.collectAsStateWithLifecycle()
        
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "INSTANT CLAIM REWARD SYSTEM",
                    color = GoldenAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Claim your daily reward of 50 gold coins once every 24 hours. Features premium anti-cheat network clock verification.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (currentServerTime == -1L) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = GoldenAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connecting to Chess Arena servers...",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    val remainingMs = stats.nextClaimTimestamp - currentServerTime
                    val isClaimable = remainingMs <= 0L

                    if (isClaimable) {
                        // Before claim UI
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Available Indicator",
                                tint = Color(0xFF66BB6A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Reward Available",
                                color = Color(0xFF66BB6A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // After claim UI
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Pending Indicator",
                                    tint = GoldenAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reward Claimed",
                                    color = GoldenAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            val seconds = (remainingMs / 1000) % 60
                            val minutes = (remainingMs / (1000 * 60)) % 60
                            val hours = (remainingMs / (1000 * 60 * 60))
                            val countdownStr = String.format("%02dh %02dm %02ds", hours, minutes, seconds)
                            
                            Text(
                                text = "Next Reward In: $countdownStr",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.claimDailyReward() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isClaimable) GoldenAccent else Color.Gray,
                            contentColor = if (isClaimable) Color.Black else Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_daily_button"),
                        enabled = isClaimable
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isClaimable) Color.Black else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isClaimable) "Claim 50 Coins" else "CLAIM 50 COINS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Display DB Statistics
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL CLAIMED REWARDS",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.totalClaimedRewards} Claims",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (stats.rewardHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val historyCount = stats.rewardHistory.split(",").size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VERIFIED LOGS RECORDED",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$historyCount Entry Logs",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }


                }
            }
        }
        
        // Chess Achievements
        Text(
            text = "CHESS ARENA ACHIEVEMENTS",
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        val achievements = listOf(
            AchievementData("First Blood", "Win your first chess game against local AI", "localWins >= 1", 30),
            AchievementData("Tactical Master", "Win 5 games against local AI", "localWins >= 5", 100),
            AchievementData("Gladiator", "Win a premium multiplayer match", "multiWins >= 1", 200),
            AchievementData("Rank Climber", "Reach an ELO rating of 1250", "rating >= 1250", 150),
            AchievementData("Coin Magnet", "Accumulate a balance of 500 gold coins", "coinBalance >= 500", 100)
        )
        
        achievements.forEach { achievement ->
            val isCompleted = when (achievement.criteria) {
                "localWins >= 1" -> stats.localWins >= 1
                "localWins >= 5" -> stats.localWins >= 5
                "multiWins >= 1" -> stats.multiWins >= 1
                "rating >= 1250" -> stats.rating >= 1250
                "coinBalance >= 500" -> stats.coinBalance >= 500
                else -> false
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.title,
                            color = if (isCompleted) GoldenAccent else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = achievement.desc,
                            color = Color.LightGray.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Reward: +${achievement.reward} Gold", color = GoldenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isCompleted) Color(0xFF2E7D32) else Color.DarkGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "COMPLETED" else "LOCKED",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class AchievementData(val title: String, val desc: String, val criteria: String, val reward: Int)

@Composable
fun ShopTabContent(
    stats: UserStats,
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GoldenAccent.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, GoldenAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("COSMETICS SKIN SHOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Spend your hard-earned gold coins on luxury boards and pieces!", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        // Section: Themes
        Text("BOARD THEThemes", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        
        val themeItems = listOf(
            ShopItemData("Classic", "Traditional dark/light wooden board squares", 0),
            ShopItemData("Wooden", "Premium rich mahogany & maple board finish", 150),
            ShopItemData("Neon", "Futuristic high-contrast grid lines & glows", 300),
            ShopItemData("Gold", "Majestic royal golden frame and cells", 600),
            ShopItemData("Diamond", "Pristine glaciated luxury cyan diamonds", 1000)
        )

        themeItems.forEach { item ->
            val unlockedList = stats.unlockedThemes.split(",")
            val isUnlocked = unlockedList.contains(item.name)
            val isEquipped = stats.selectedTheme == item.name

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isEquipped) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.background(GoldenAccent, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("EQUIPPED", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(item.desc, color = Color.LightGray, fontSize = 11.sp)
                        if (!isUnlocked && item.price > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("${item.price} Gold Coins", color = GoldenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isUnlocked) {
                                viewModel.equipItem("theme", item.name)
                            } else {
                                viewModel.purchaseItem("theme", item.name, item.price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color.Gray else if (isUnlocked) Color(0xFF4CAF50) else GoldenAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = if (isEquipped) "Equipped" else if (isUnlocked) "EQUIP" else "BUY",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Section: Pieces style
        Spacer(Modifier.height(8.dp))
        Text("CHESS PIECES STYLE", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

        val pieceItems = listOf(
            ShopItemData("Standard", "Traditional unicode wooden chess figurines", 0),
            ShopItemData("Modern", "Sleek, high-tech alphabetic typography initials", 200),
            ShopItemData("Fantasy", "Unique high-fidelity roleplay emoji avatars", 500)
        )

        pieceItems.forEach { item ->
            val unlockedList = stats.unlockedPieces.split(",")
            val isUnlocked = unlockedList.contains(item.name)
            val isEquipped = stats.selectedPieces == item.name

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isEquipped) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.background(GoldenAccent, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("EQUIPPED", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(item.desc, color = Color.LightGray, fontSize = 11.sp)
                        if (!isUnlocked && item.price > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("${item.price} Gold Coins", color = GoldenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isUnlocked) {
                                viewModel.equipItem("pieces", item.name)
                            } else {
                                viewModel.purchaseItem("pieces", item.name, item.price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color.Gray else if (isUnlocked) Color(0xFF4CAF50) else GoldenAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = if (isEquipped) "Equipped" else if (isUnlocked) "EQUIP" else "BUY",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

data class ShopItemData(val name: String, val desc: String, val price: Int)

enum class LeaderboardSortOption {
    RATING, WINS, COINS
}

data class LeaderboardPlayer(
    val name: String,
    val rating: Int,
    val coins: Int,
    val wins: Int,
    val isUser: Boolean = false
)

@Composable
fun LeaderboardTabContent(
    stats: UserStats,
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    var sortOption by remember { mutableStateOf(LeaderboardSortOption.RATING) }
    val leaderboardAccounts by viewModel.leaderboardPlayers.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshLeaderboard()
    }

    val sortedPlayers = remember(leaderboardAccounts, sortOption, stats.username) {
        val mapped = leaderboardAccounts.map { acc ->
            val isCurrentUser = (acc.username == stats.username || acc.isLoggedIn)
            LeaderboardPlayer(
                name = acc.username,
                rating = acc.rating,
                coins = acc.coinBalance,
                wins = acc.localWins + acc.multiWins,
                isUser = isCurrentUser
            )
        }
        when (sortOption) {
            LeaderboardSortOption.RATING -> mapped.sortedByDescending { it.rating }
            LeaderboardSortOption.WINS -> mapped.sortedByDescending { it.wins }
            LeaderboardSortOption.COINS -> mapped.sortedByDescending { it.coins }
        }
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Chess Arena Rankings",
            color = GoldenAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Mode Selector Toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val options = listOf(
                Pair(LeaderboardSortOption.RATING, "GLOBAL ELO"),
                Pair(LeaderboardSortOption.WINS, "WINS"),
                Pair(LeaderboardSortOption.COINS, "COINS")
            )
            options.forEach { (option, label) ->
                val selected = sortOption == option
                Button(
                    onClick = { sortOption = option },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) CardSurfaceColor else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = when (option) {
                            LeaderboardSortOption.RATING -> Icons.Default.Star
                            LeaderboardSortOption.WINS -> Icons.Default.CheckCircle
                            LeaderboardSortOption.COINS -> Icons.Default.ShoppingCart
                        },
                        contentDescription = null,
                        tint = if (selected) GoldenAccent else Color.Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = label,
                        color = if (selected) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leaderboard Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("CHESS CHALLENGER", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = when (sortOption) {
                    LeaderboardSortOption.RATING -> "ELO RATING"
                    LeaderboardSortOption.WINS -> "BATTLE WINS"
                    LeaderboardSortOption.COINS -> "COINS BALANCE"
                },
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(sortedPlayers) { index, player ->
                val rank = index + 1
                val cardBg = if (player.isUser) GoldenAccent.copy(alpha = 0.15f) else CardSurfaceColor.copy(alpha = 0.6f)
                val stroke = if (player.isUser) BorderStroke(1.dp, GoldenAccent) else null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(10.dp),
                    border = stroke
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank number/icon
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = when (rank) {
                                            1 -> Color(0xFFFFD700)
                                            2 -> Color(0xFFC0C0C0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> Color.Black.copy(alpha = 0.2f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rank.toString(),
                                    color = if (rank <= 3) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = player.name + (if (player.isUser) " (YOU)" else ""),
                                color = if (player.isUser) GoldenAccent else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (player.isUser) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (sortOption) {
                                LeaderboardSortOption.RATING -> {
                                    Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${player.rating} ELO",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LeaderboardSortOption.WINS -> {
                                    Icon(Icons.Default.CheckCircle, null, tint = GoldenAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${player.wins} Wins",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LeaderboardSortOption.COINS -> {
                                    Icon(Icons.Default.Star, null, tint = GoldenAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${player.coins} Gold",
                                        color = GoldenAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
