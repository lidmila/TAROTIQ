package com.tarotiq.app.ui.challenge

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.ActiveChallenge
import com.tarotiq.app.domain.model.ChallengeDefinition
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeListScreen(
    onBack: () -> Unit,
    challengeViewModel: ChallengeViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeChallenge by challengeViewModel.activeChallenge.collectAsState()
    val completionMessage by challengeViewModel.completionMessage.collectAsState()
    val challenges = remember { ChallengeDefinition.all(context) }

    var showAbandonDialog by remember { mutableStateOf(false) }
    var confirmStartId by remember { mutableStateOf<String?>(null) }

    if (completionMessage != null) {
        val def = remember(completionMessage) { challenges.firstOrNull { it.id == completionMessage } }
        AlertDialog(
            onDismissRequest = { challengeViewModel.clearCompletionMessage() },
            containerColor = CosmicDeep,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    stringResource(R.string.challenge_completed_all),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = NewsreaderFamily),
                    color = CelestialGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.EmojiEvents, null, tint = CelestialGold, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    if (def != null) {
                        Text(
                            stringResource(R.string.challenge_reward, def.coinReward),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = ManropeFamily),
                            color = SuccessEmerald,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { challengeViewModel.clearCompletionMessage() }) {
                    Text("OK", color = CelestialGold)
                }
            }
        )
    }

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            containerColor = CosmicDeep,
            shape = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.challenge_abandon), color = StarWhite, fontFamily = NewsreaderFamily) },
            text = { Text(stringResource(R.string.challenge_abandon_confirm), color = MoonSilver, fontFamily = ManropeFamily) },
            confirmButton = {
                TextButton(onClick = { challengeViewModel.abandonChallenge(); showAbandonDialog = false }) {
                    Text(stringResource(R.string.challenge_abandon), color = ErrorCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) {
                    Text("Zpět", color = MoonSilver)
                }
            }
        )
    }

    if (confirmStartId != null) {
        AlertDialog(
            onDismissRequest = { confirmStartId = null },
            containerColor = CosmicDeep,
            shape = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.challenge_start), color = CelestialGold, fontFamily = NewsreaderFamily) },
            text = {
                val def = challenges.firstOrNull { it.id == confirmStartId }
                if (def != null) {
                    Text(
                        context.getString(def.titleRes),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
                        color = StarWhite
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmStartId?.let { challengeViewModel.startChallenge(it) }
                    confirmStartId = null
                }) { Text(stringResource(R.string.challenge_start), color = CelestialGold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmStartId = null }) {
                    Text("Zpět", color = MoonSilver)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.challenge_list_title),
                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily),
                                color = CelestialGold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AstralPurple)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.challenge_list_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
                        color = MoonSilver,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Active challenge card
                    activeChallenge?.let { active ->
                        val activeDef = challenges.firstOrNull { it.id == active.challengeId }
                        if (activeDef != null) {
                            ActiveChallengeCard(
                                challenge = active,
                                def = activeDef,
                                context = context,
                                isTodayDone = challengeViewModel.isTodayCompleted(active),
                                onCompleteToday = { challengeViewModel.completeToday(activeDef) },
                                onAbandon = { showAbandonDialog = true }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = AstralPurple.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Ostatní výzvy",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFamily, letterSpacing = 2.sp),
                                color = MoonSilver.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Challenge list
                    challenges.forEach { def ->
                        val isActive = activeChallenge?.challengeId == def.id
                        if (!isActive) {
                            ChallengeCard(
                                def = def,
                                context = context,
                                hasActiveChallenge = activeChallenge != null,
                                onStart = { confirmStartId = def.id }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveChallengeCard(
    challenge: ActiveChallenge,
    def: ChallengeDefinition,
    context: android.content.Context,
    isTodayDone: Boolean,
    onCompleteToday: () -> Unit,
    onAbandon: () -> Unit
) {
    ArtNouveauFrame(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CelestialGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        frameStyle = FrameStyle.ORNATE,
        backgroundColor = CosmicMid.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = CelestialGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    context.getString(def.titleRes),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = NewsreaderFamily, fontWeight = FontWeight.Bold),
                    color = CelestialGold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.challenge_active_day, challenge.currentDay, def.durationDays),
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFamily),
                color = AstralPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { challenge.currentDay.toFloat() / def.durationDays },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = CelestialGold,
                trackColor = AstralPurple.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            val todayDef = def.days.getOrNull(challenge.currentDay - 1)
            if (todayDef != null) {
                Text(
                    stringResource(R.string.challenge_today_intention),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, letterSpacing = 1.5.sp),
                    color = MoonSilver.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    todayDef.intention,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = StarWhite
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isTodayDone) {
                Text(
                    stringResource(R.string.challenge_completed_today),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
                    color = SuccessEmerald,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ArtNouveauButton(
                    text = stringResource(R.string.challenge_draw_card),
                    onClick = onCompleteToday,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAbandon, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.challenge_abandon), color = MoonSilver.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    def: ChallengeDefinition,
    context: android.content.Context,
    hasActiveChallenge: Boolean,
    onStart: () -> Unit
) {
    if (!hasActiveChallenge) {
        GlassCard(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            ChallengeCardContent(def, context, hasActiveChallenge, onStart)
        }
    } else {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            ChallengeCardContent(def, context, hasActiveChallenge, onStart)
        }
    }
}

@Composable
private fun ChallengeCardContent(
    def: ChallengeDefinition,
    context: android.content.Context,
    hasActiveChallenge: Boolean,
    onStart: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    context.getString(def.titleRes),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = NewsreaderFamily, fontWeight = FontWeight.Bold),
                    color = StarWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${def.durationDays} dní · +${def.coinReward} mincí",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily),
                    color = CelestialGold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            context.getString(def.descriptionRes),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = ManropeFamily),
            color = MoonSilver
        )
        if (!hasActiveChallenge) {
            Spacer(modifier = Modifier.height(12.dp))
            ArtNouveauButton(
                text = stringResource(R.string.challenge_start),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY
            )
        }
    }
}
