package com.tarotiq.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.LocaleUtils
import com.tarotiq.app.viewmodel.AuthViewModel
import com.tarotiq.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToContact: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val zodiacSign by profileViewModel.zodiacSign.collectAsState()
    val currentStreak by profileViewModel.currentStreak.collectAsState()
    val longestStreak by profileViewModel.longestStreak.collectAsState()
    val coinBalance by profileViewModel.coinBalance.collectAsState()
    val totalReadings by profileViewModel.totalReadings.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var showZodiacDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val zodiacSigns = listOf("aries","taurus","gemini","cancer","leo","virgo","libra","scorpio","sagittarius","capricorn","aquarius","pisces")
    val zodiacResMap = mapOf(
        "aries" to R.string.zodiac_aries, "taurus" to R.string.zodiac_taurus,
        "gemini" to R.string.zodiac_gemini, "cancer" to R.string.zodiac_cancer,
        "leo" to R.string.zodiac_leo, "virgo" to R.string.zodiac_virgo,
        "libra" to R.string.zodiac_libra, "scorpio" to R.string.zodiac_scorpio,
        "sagittarius" to R.string.zodiac_sagittarius, "capricorn" to R.string.zodiac_capricorn,
        "aquarius" to R.string.zodiac_aquarius, "pisces" to R.string.zodiac_pisces
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.profile_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CelestialGold
                        )
                    },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AstralPurple) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                // User info
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.ARCH) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Outlined.AccountCircle, null, tint = CelestialGold, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(user?.email ?: "Anonymous", style = MaterialTheme.typography.headlineSmall, color = StarWhite)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Zodiac
                ArtNouveauFrame(onClick = { showZodiacDialog = true }, modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.SIMPLE) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        SymbolIcon(symbol = Symbol.STAR, size = 24.dp, color = CelestialGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_zodiac).uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily), color = MoonSilver)
                            Text(
                                zodiacSign?.let { zodiacResMap[it]?.let { res -> stringResource(res) } ?: it.replaceFirstChar { c -> c.uppercase() } }
                                    ?: stringResource(R.string.profile_zodiac_not_set),
                                style = MaterialTheme.typography.bodyLarge, color = StarWhite
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MoonSilver)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Coins
                ArtNouveauFrame(onClick = onNavigateToShop, modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.SIMPLE) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        SymbolIcon(symbol = Symbol.COIN, size = 24.dp, color = CelestialGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.coins_balance).uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily), color = MoonSilver)
                            Text(stringResource(R.string.coins_count, coinBalance.balance), style = MaterialTheme.typography.bodyLarge, color = CelestialGold)
                        }
                        Text(stringResource(R.string.coins_buy), color = AstralPurpleLight, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Stats
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.ORNATE) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.profile_statistics), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp), color = CelestialGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(stringResource(R.string.profile_total_readings), "$totalReadings")
                            StatItem(stringResource(R.string.home_streak), "$currentStreak")
                            StatItem(stringResource(R.string.profile_longest_streak), "$longestStreak")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Language
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.SIMPLE) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(stringResource(R.string.profile_language), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp), color = CelestialGold)
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LocaleUtils.getAvailableLanguages().forEach { (code, name) ->
                                FilterChip(
                                    selected = LocaleUtils.getCurrentLanguage(context) == code,
                                    onClick = { LocaleUtils.saveAndApplyLanguage(context, code) },
                                    label = { Text(name.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AstralPurple,
                                        selectedLabelColor = StarWhite
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Legal & Info
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.SIMPLE) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(stringResource(R.string.profile_legal), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp), color = CelestialGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            Triple(R.string.faq_title, Icons.Outlined.QuestionAnswer, { onNavigateToFAQ() }),
                            Triple(R.string.about_title, Icons.Outlined.Info, { onNavigateToAbout() }),
                            Triple(R.string.contact_title, Icons.Outlined.Email, { onNavigateToContact() }),
                            Triple(R.string.terms_title, Icons.Outlined.Description, {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.codewhiskers.app/obchodni-podminky")))
                            }),
                            Triple(R.string.privacy_title, Icons.Outlined.Shield, {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.codewhiskers.app/zasady-ochrany-osobnich-udaju")))
                            })
                        ).forEach { (labelRes, icon, action) ->
                            TextButton(onClick = { action() }, modifier = Modifier.fillMaxWidth()) {
                                Icon(icon, null, tint = MoonSilver, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(labelRes), color = StarWhite, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = MoonSilver)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Other apps
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.SIMPLE) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.profile_other_apps),
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                            color = CelestialGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        data class AppInfo(val icon: String, val name: String, val desc: String, val packageId: String)
                        val otherApps = listOf(
                            AppInfo("\uD83D\uDCB0", "Kvitta", stringResource(R.string.app_kvitta_desc), "com.newkvitta.app"),
                            AppInfo("\uD83D\uDC8C", "BetterMingle", stringResource(R.string.app_bettermingle_desc), "com.bettermingle.app"),
                            AppInfo("\uD83D\uDCDA", "BetterShelf", stringResource(R.string.app_bettershelf_desc), "com.codewhiskers.bettershelf"),
                            AppInfo("\uD83C\uDF19", "Dream Witness", stringResource(R.string.app_dreamwitness_desc), "com.dreamwitness.app")
                        )

                        otherApps.forEach { app ->
                            GlassCard(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageId}")))
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Emoji logo
                                    Text(
                                        text = app.icon,
                                        fontSize = 28.sp,
                                        modifier = Modifier.padding(end = 14.dp)
                                    )
                                    // Name + description
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = StarWhite
                                        )
                                        Text(
                                            text = app.desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MoonSilver,
                                            maxLines = 2
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MoonSilver)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OrnamentalDivider()

                Spacer(modifier = Modifier.height(18.dp))

                // Logout
                ArtNouveauButton(
                    text = stringResource(R.string.auth_logout),
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY
                )

                Spacer(modifier = Modifier.height(10.dp))
                ArtNouveauButton(
                    text = stringResource(R.string.auth_delete_account),
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.codewhiskers.app/smazani-uctu")))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.SECONDARY
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }

    if (showZodiacDialog) {
        AlertDialog(
            onDismissRequest = { showZodiacDialog = false },
            title = { Text(stringResource(R.string.profile_zodiac)) },
            text = {
                Column {
                    zodiacSigns.forEach { sign ->
                        val displayName = zodiacResMap[sign]?.let { stringResource(it) } ?: sign.replaceFirstChar { it.uppercase() }
                        TextButton(onClick = { profileViewModel.setZodiacSign(sign); showZodiacDialog = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(displayName, color = if (zodiacSign == sign) CelestialGold else StarWhite)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showZodiacDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.auth_logout)) },
            confirmButton = { TextButton(onClick = { authViewModel.logout(); showLogoutDialog = false }) { Text(stringResource(R.string.ok)) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.auth_delete_account)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { authViewModel.deleteAccount { _, _ -> showDeleteDialog = false } }) {
                    Text(stringResource(R.string.delete), color = ErrorCrimson)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = CelestialGold)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily), color = MoonSilver)
    }
}
