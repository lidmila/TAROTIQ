package com.tarotiq.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.LocaleUtils
import com.tarotiq.app.viewmodel.AuthViewModel
import com.tarotiq.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.profile_title)) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                // User info
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.AccountCircle, null, tint = MysticPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(user?.email ?: "Anonymous", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Zodiac
                GlassCard(onClick = { showZodiacDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, null, tint = GoldSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_zodiac), style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                            Text(zodiacSign?.replaceFirstChar { it.uppercase() } ?: "Not set", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Coins
                GlassCard(onClick = onNavigateToShop, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, null, tint = GoldSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.coins_balance), style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                            Text("${coinBalance.balance} coins", style = MaterialTheme.typography.bodyLarge, color = GoldSecondary)
                        }
                        Text(stringResource(R.string.coins_buy), color = MysticLight, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Stats
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.profile_statistics), style = MaterialTheme.typography.titleSmall, color = GoldSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(stringResource(R.string.profile_total_readings), "$totalReadings")
                            StatItem(stringResource(R.string.home_streak), "$currentStreak")
                            StatItem(stringResource(R.string.profile_longest_streak), "$longestStreak")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Language
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.profile_language), style = MaterialTheme.typography.titleSmall, color = GoldSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LocaleUtils.getAvailableLanguages().forEach { (code, name) ->
                                FilterChip(
                                    selected = LocaleUtils.getCurrentLanguage(context) == code,
                                    onClick = { LocaleUtils.saveAndApplyLanguage(context, code) },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Legal & Info
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.profile_legal), style = MaterialTheme.typography.titleSmall, color = GoldSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            Triple(R.string.faq_title, Icons.Default.QuestionAnswer, { onNavigateToFAQ() }),
                            Triple(R.string.about_title, Icons.Default.Info, { onNavigateToAbout() }),
                            Triple(R.string.contact_title, Icons.Default.Email, { onNavigateToContact() }),
                            Triple(R.string.terms_title, Icons.Default.Description, {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tarotiq.app/terms")))
                            }),
                            Triple(R.string.privacy_title, Icons.Default.Shield, {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tarotiq.app/privacy")))
                            })
                        ).forEach { (labelRes, icon, action) ->
                            TextButton(onClick = { action() }, modifier = Modifier.fillMaxWidth()) {
                                Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(labelRes), color = TextPrimary, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Logout
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.auth_logout)) }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                ) { Text(stringResource(R.string.auth_delete_account)) }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Zodiac dialog
    if (showZodiacDialog) {
        AlertDialog(
            onDismissRequest = { showZodiacDialog = false },
            title = { Text(stringResource(R.string.profile_zodiac)) },
            text = {
                Column {
                    zodiacSigns.forEach { sign ->
                        TextButton(onClick = { profileViewModel.setZodiacSign(sign); showZodiacDialog = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(sign.replaceFirstChar { it.uppercase() }, color = if (zodiacSign == sign) GoldSecondary else TextPrimary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showZodiacDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.auth_logout)) },
            confirmButton = { TextButton(onClick = { authViewModel.logout(); showLogoutDialog = false }) { Text(stringResource(R.string.ok)) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.auth_delete_account)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { authViewModel.deleteAccount { _, _ -> showDeleteDialog = false } }) {
                    Text(stringResource(R.string.delete), color = ErrorColor)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MysticLight)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
