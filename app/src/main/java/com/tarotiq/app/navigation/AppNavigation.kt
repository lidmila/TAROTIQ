package com.tarotiq.app.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tarotiq.app.R
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.domain.model.ReadingSpread
import com.tarotiq.app.ui.auth.LoginScreen
import com.tarotiq.app.ui.components.InterstitialAdManager
import com.tarotiq.app.ui.components.OfflineBanner
import com.tarotiq.app.ui.daily.DailyCardScreen
import com.tarotiq.app.ui.history.ReadingDetailScreen
import com.tarotiq.app.ui.history.ReadingHistoryScreen
import com.tarotiq.app.ui.home.HomeScreen
import com.tarotiq.app.ui.info.AboutScreen
import com.tarotiq.app.ui.info.ContactScreen
import com.tarotiq.app.ui.info.FAQScreen
import com.tarotiq.app.ui.library.CardDetailScreen
import com.tarotiq.app.ui.library.CardLibraryScreen
import com.tarotiq.app.ui.onboarding.OnboardingScreen
import com.tarotiq.app.ui.profile.ProfileScreen
import com.tarotiq.app.ui.reading.*
import com.tarotiq.app.ui.shop.CoinShopScreen
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.CelestialGold
import com.tarotiq.app.ui.theme.CosmicDeep
import com.tarotiq.app.ui.theme.SpaceGroteskFamily
import com.tarotiq.app.ui.theme.MoonSilver
import com.tarotiq.app.ui.theme.VoidBlack
import com.tarotiq.app.viewmodel.AuthViewModel
import com.tarotiq.app.viewmodel.ReadingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class NavItem(val route: String, val icon: ImageVector, val labelRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    initialRoute: String? = null
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val interstitialAdManager = remember { InterstitialAdManager(context) }
    var actionCount by remember { mutableIntStateOf(0) }
    val activity = context as? Activity

    LaunchedEffect(Unit) { interstitialAdManager.preload() }

    val trackAction: () -> Unit = remember {
        { actionCount++; if (actionCount % 5 == 0 && activity != null) interstitialAdManager.showIfReady(activity) }
    }

    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()
    val hasSeenOnboarding by settingsManager.hasSeenOnboardingFlow.collectAsState(initial = true)
    val appOpenCount by settingsManager.appOpenCountFlow.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    // Mark existing users as having seen onboarding
    LaunchedEffect(appOpenCount) {
        if (appOpenCount > 0 && !hasSeenOnboarding) settingsManager.setHasSeenOnboarding(true)
    }

    val isAuthenticated = authState.user != null
    val startDestination = when {
        !isAuthenticated -> Screen.Login.route
        !hasSeenOnboarding && appOpenCount == 0 -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    // Shared reading ViewModel
    val readingViewModel: ReadingViewModel = viewModel()

    // Increment open count once per session
    var countIncremented by remember { mutableStateOf(false) }
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated && !countIncremented) { countIncremented = true; settingsManager.incrementAppOpenCount() }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Stitch design: 4 bottom tabs — Portal, Oracle, Grimoire, Library
    val bottomNavRoutes = listOf(
        Screen.Home.route, Screen.TopicSelection.route, Screen.ReadingHistory.route,
        Screen.CardLibrary.route, Screen.Profile.route
    )
    val showBottomNav = isAuthenticated && currentRoute in bottomNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    // Stitch AI bottom nav — 4 tabs, glass style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 24.dp,
                                ambientColor = AstralPurple.copy(alpha = 0.15f),
                                spotColor = AstralPurple.copy(alpha = 0.08f)
                            )
                            .background(VoidBlack.copy(alpha = 0.90f))
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val navItems = listOf(
                                NavItem(Screen.Home.route, Icons.Outlined.AutoAwesome, R.string.nav_portal),
                                NavItem(Screen.TopicSelection.route, Icons.Outlined.AutoFixHigh, R.string.nav_oracle),
                                NavItem(Screen.ReadingHistory.route, Icons.AutoMirrored.Outlined.MenuBook, R.string.nav_grimoire),
                                NavItem(Screen.CardLibrary.route, Icons.Outlined.WbSunny, R.string.nav_library)
                            )
                            navItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (item.route == Screen.TopicSelection.route) {
                                                trackAction(); readingViewModel.resetReading()
                                            }
                                            navController.navigate(item.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        .graphicsLayer {
                                            scaleX = if (isSelected) 1.1f else 1f
                                            scaleY = if (isSelected) 1.1f else 1f
                                        }
                                ) {
                                    val label = stringResource(item.labelRes).uppercase()
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = label,
                                        tint = if (isSelected) AstralPurple else AstralPurple.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.5.sp,
                                        color = if (isSelected) AstralPurple else AstralPurple.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            topBar = { OfflineBanner() }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues),
                enterTransition = { fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(onLoginSuccess = {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    })
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onComplete = { zodiac ->
                        scope.launch { settingsManager.setHasSeenOnboarding(true); if (zodiac != null) settingsManager.setZodiacSign(zodiac) }
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                    })
                }

                composable(Screen.Home.route) {
                    if (!isAuthenticated) { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
                    else HomeScreen(
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToDailyCard = { navController.navigate(Screen.DailyCard.route) },
                        onNavigateToNewReading = { navController.navigate(Screen.TopicSelection.route) },
                        onNavigateToCoinShop = { navController.navigate(Screen.CoinShop.route) },
                        onNavigateToQuickReading = { spreadType -> readingViewModel.resetReading(); readingViewModel.setSpread(ReadingSpread.fromKey(spreadType)); readingViewModel.setTopic("general")
                            navController.navigate(Screen.CardDrawing.createRoute("general", spreadType)) },
                        onNavigateToLibrary = { navController.navigate(Screen.CardLibrary.route) }
                    )
                }

                // Reading flow
                composable(Screen.TopicSelection.route) {
                    // Reset state for new reading
                    LaunchedEffect(Unit) { readingViewModel.resetReading() }
                    TopicSelectionScreen(
                        onTopicSelected = { topic ->
                            readingViewModel.setTopic(topic)
                            navController.navigate(Screen.QuestionInput.createRoute(topic))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.QuestionInput.route, arguments = listOf(navArgument("topic") { type = NavType.StringType })) { entry ->
                    val topic = entry.arguments?.getString("topic") ?: ""
                    QuestionInputScreen(
                        topic = topic,
                        onContinue = { question ->
                            readingViewModel.setQuestion(question)
                            navController.navigate(Screen.SpreadSelection.createRoute(topic, question))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SpreadSelection.route, arguments = listOf(
                    navArgument("topic") { type = NavType.StringType },
                    navArgument("question") { type = NavType.StringType; nullable = true; defaultValue = null }
                )) { entry ->
                    val topic = entry.arguments?.getString("topic") ?: ""
                    val question = entry.arguments?.getString("question")
                    SpreadSelectionScreen(
                        topic = topic,
                        question = question,
                        onSpreadSelected = { spreadKey ->
                            readingViewModel.setSpread(ReadingSpread.fromKey(spreadKey))
                            navController.navigate(Screen.CardDrawing.createRoute(topic, spreadKey))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.CardDrawing.route, arguments = listOf(
                    navArgument("topic") { type = NavType.StringType },
                    navArgument("spreadType") { type = NavType.StringType },
                    navArgument("question") { type = NavType.StringType; nullable = true; defaultValue = null }
                )) {
                    CardDrawingScreen(
                        onGetReading = { navController.navigate("reading/interpretation/temp") },
                        onBack = { navController.popBackStack() },
                        readingViewModel = readingViewModel
                    )
                }

                composable("reading/interpretation/{readingId}", arguments = listOf(
                    navArgument("readingId") { type = NavType.StringType }
                )) {
                    ReadingInterpretationScreen(
                        onDone = { navController.navigate("reading/summary/temp") },
                        onBack = { navController.popBackStack() },
                        onBuyExtraCard = { navController.navigate("reading/extra_card_picker") },
                        readingViewModel = readingViewModel
                    )
                }

                composable("reading/extra_card_picker") {
                    val coins by readingViewModel.coinBalance.collectAsState()
                    ExtraCardPickerScreen(
                        existingCardIds = readingViewModel.uiState.value.drawnCards.map { it.cardId }.toSet(),
                        coinBalance = coins.balance,
                        onCoinSpent = { readingViewModel.spendCoinForExtraCard() },
                        onCardConfirmed = { cardId ->
                            readingViewModel.interpretExtraCard(cardId)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("reading/summary/{readingId}", arguments = listOf(
                    navArgument("readingId") { type = NavType.StringType }
                )) {
                    ReadingSummaryScreen(
                        onDone = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                        readingViewModel = readingViewModel
                    )
                }

                // Library
                composable(Screen.CardLibrary.route) {
                    CardLibraryScreen(
                        onNavigateToCardDetail = { cardId -> navController.navigate(Screen.CardDetail.createRoute(cardId)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    Screen.CardDetail.route,
                    arguments = listOf(navArgument("cardId") { type = NavType.IntType }),
                    enterTransition = {
                        slideInVertically(tween(500, easing = EaseOutCubic)) { it / 3 } +
                        fadeIn(tween(400)) +
                        scaleIn(initialScale = 0.85f, animationSpec = tween(500, easing = EaseOutCubic))
                    },
                    exitTransition = { fadeOut(tween(300)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = {
                        slideOutVertically(tween(400, easing = EaseInCubic)) { it / 4 } +
                        fadeOut(tween(300)) +
                        scaleOut(targetScale = 0.9f, animationSpec = tween(400))
                    }
                ) { entry ->
                    val cardId = entry.arguments?.getInt("cardId") ?: 0
                    CardDetailScreen(cardId = cardId, onNavigateBack = { navController.popBackStack() })
                }

                // History
                composable(Screen.ReadingHistory.route) {
                    ReadingHistoryScreen(onReadingClick = { id -> navController.navigate(Screen.ReadingDetail.createRoute(id)) })
                }
                composable(Screen.ReadingDetail.route, arguments = listOf(navArgument("readingId") { type = NavType.StringType })) { entry ->
                    val readingId = entry.arguments?.getString("readingId") ?: ""
                    ReadingDetailScreen(readingId = readingId, onBack = { navController.popBackStack() })
                }

                // Daily Card
                composable(Screen.DailyCard.route) {
                    DailyCardScreen(onBack = { navController.popBackStack() }, onCardDetail = { id -> navController.navigate(Screen.CardDetail.createRoute(id)) })
                }

                // Shop
                composable(Screen.CoinShop.route) {
                    CoinShopScreen(onBack = { navController.popBackStack() })
                }

                // Profile & Info
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToShop = { navController.navigate(Screen.CoinShop.route) },
                        onNavigateToFAQ = { navController.navigate(Screen.FAQ.route) },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToContact = { navController.navigate(Screen.Contact.route) }
                    )
                }
                composable(Screen.FAQ.route) { FAQScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Contact.route) { ContactScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}
