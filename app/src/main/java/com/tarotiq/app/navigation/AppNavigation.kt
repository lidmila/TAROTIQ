package com.tarotiq.app.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.tarotiq.app.ui.theme.MidnightBg2
import com.tarotiq.app.viewmodel.AuthViewModel
import com.tarotiq.app.viewmodel.ReadingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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

    val bottomNavRoutes = listOf(Screen.Home.route, Screen.CardLibrary.route, Screen.TopicSelection.route, Screen.ReadingHistory.route, Screen.Profile.route)
    val showBottomNav = isAuthenticated && currentRoute in bottomNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar(containerColor = MidnightBg2.copy(alpha = 0.95f), tonalElevation = 0.dp) {
                        listOf(
                            Triple(Screen.Home.route, Icons.Default.Home, R.string.nav_home),
                            Triple(Screen.CardLibrary.route, Icons.Default.MenuBook, R.string.nav_library),
                            Triple(Screen.TopicSelection.route, Icons.Default.AutoAwesome, R.string.nav_new_reading),
                            Triple(Screen.ReadingHistory.route, Icons.Default.History, R.string.nav_history),
                            Triple(Screen.Profile.route, Icons.Default.Person, R.string.nav_profile)
                        ).forEach { (route, icon, labelRes) ->
                            NavigationBarItem(
                                icon = { Icon(icon, null) },
                                label = { Text(stringResource(labelRes)) },
                                selected = currentRoute == route,
                                onClick = {
                                    if (route == Screen.TopicSelection.route) { trackAction(); readingViewModel.resetReading() }
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
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
                        onNavigateToDaily = { navController.navigate(Screen.DailyCard.route) },
                        onNavigateToReading = { spread -> readingViewModel.resetReading(); readingViewModel.setSpread(spread); readingViewModel.setTopic("general")
                            navController.navigate(Screen.CardDrawing.createRoute("general", spread.key)) },
                        onNavigateToNewReading = { navController.navigate(Screen.TopicSelection.route) },
                        onNavigateToShop = { navController.navigate(Screen.CoinShop.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                    )
                }

                // Reading flow
                composable(Screen.TopicSelection.route) {
                    TopicSelectionScreen(onTopicSelected = { topic ->
                        readingViewModel.setTopic(topic)
                        navController.navigate(Screen.QuestionInput.createRoute(topic))
                    })
                }

                composable(Screen.QuestionInput.route, arguments = listOf(navArgument("topic") { type = NavType.StringType })) { entry ->
                    val topic = entry.arguments?.getString("topic") ?: ""
                    QuestionInputScreen(
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
                    SpreadSelectionScreen(
                        onSpreadSelected = { spread ->
                            readingViewModel.setSpread(spread)
                            navController.navigate(Screen.CardDrawing.createRoute(topic, spread.key))
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
                        readingViewModel = readingViewModel
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
                    CardLibraryScreen(onCardClick = { cardId -> navController.navigate(Screen.CardDetail.createRoute(cardId)) })
                }
                composable(Screen.CardDetail.route, arguments = listOf(navArgument("cardId") { type = NavType.IntType })) { entry ->
                    val cardId = entry.arguments?.getInt("cardId") ?: 0
                    CardDetailScreen(cardId = cardId, onBack = { navController.popBackStack() })
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
