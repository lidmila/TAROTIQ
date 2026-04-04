package com.tarotiq.app.ui.daily

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.CardNameTranslator
import com.tarotiq.app.viewmodel.DailyCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCardScreen(
    onBack: () -> Unit,
    onCardDetail: (Int) -> Unit,
    dailyCardViewModel: DailyCardViewModel = viewModel()
) {
    val dailyCard by dailyCardViewModel.dailyCard.collectAsState()
    val hasDrawnToday by dailyCardViewModel.hasDrawnToday.collectAsState()
    val isLoading by dailyCardViewModel.isLoading.collectAsState()
    val isInitDone by dailyCardViewModel.isInitDone.collectAsState()
    val restoredFromDb by dailyCardViewModel.restoredFromDb.collectAsState()
    val context = LocalContext.current

    // Simple flip animation — no bounce, no portal, just clean rotation
    var hasStartedReveal by remember { mutableStateOf(false) }
    val flipRotation = remember { Animatable(0f) }

    // Card restored from DB (previous session) → show face immediately, no animation
    LaunchedEffect(restoredFromDb) {
        if (restoredFromDb) {
            hasStartedReveal = true
            flipRotation.snapTo(180f)
        }
    }

    // Auto-draw when screen opens and no card exists yet
    LaunchedEffect(isInitDone, hasDrawnToday) {
        if (isInitDone && !hasDrawnToday) {
            dailyCardViewModel.drawDailyCard()
        }
    }

    // Fresh draw by user → animate the flip
    LaunchedEffect(hasDrawnToday) {
        if (hasDrawnToday && !hasStartedReveal) {
            hasStartedReveal = true
            flipRotation.animateTo(180f, tween(1000, easing = EaseInOutCubic))
        }
    }

    // Pre-reveal levitation + prompt pulse
    val dailyTransition = rememberInfiniteTransition(label = "daily_ritual")
    val levitation by dailyTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "levitate"
    )
    val promptAlpha by dailyTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "prompt_pulse"
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
                            stringResource(R.string.daily_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CelestialGold
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AstralPurple) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Show prompt text: "tap to reveal" only when init is done and card not drawn yet
                // Show "already drawn" only when card was restored from DB (not during fresh reveal animation)
                val promptText = when {
                    !isInitDone -> ""
                    restoredFromDb -> stringResource(R.string.daily_already_drawn)
                    hasDrawnToday -> "" // Fresh draw in progress — don't show "already drawn" during animation
                    else -> stringResource(R.string.daily_tap_reveal)
                }
                Text(
                    text = promptText.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = CelestialGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (!isInitDone || (!restoredFromDb && !hasDrawnToday)) promptAlpha else 1f
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Card — clean flip only, no portal/bounce
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(2f / 3f)
                        .graphicsLayer {
                            translationY = if (!hasStartedReveal) levitation else 0f
                            rotationY = flipRotation.value
                            cameraDistance = 14f * density
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CelestialGold.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                        .clickable(enabled = isInitDone && !hasDrawnToday && !isLoading) {
                            dailyCardViewModel.drawDailyCard()
                        }
                    ) {
                        if (flipRotation.value <= 90f) {
                            Image(painterResource(R.drawable.card_back), "Card back", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            dailyCard?.let { card ->
                                val majorNames = listOf("major_the_fool","major_the_magician","major_the_high_priestess","major_the_empress","major_the_emperor","major_the_hierophant","major_the_lovers","major_the_chariot","major_strength","major_the_hermit","major_wheel_of_fortune","major_justice","major_the_hanged_man","major_death","major_temperance","major_the_devil","major_the_tower","major_the_star","major_the_moon","major_the_sun","major_judgement","major_the_world")
                                val ranks = listOf("ace","two","three","four","five","six","seven","eight","nine","ten","page","knight","queen","king")
                                val suits = listOf("cups","pentacles","swords","wands")
                                val resName = if (card.cardId < 22) majorNames[card.cardId]
                                    else { val mi = card.cardId - 22; "minor_${ranks[mi % 14]}_of_${suits[mi / 14]}" }
                                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                                Image(painterResource(if (resId != 0) resId else R.drawable.card_back), "Daily card",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f; if (card.isReversed) rotationZ = 180f })
                            }
                        }
                    }

                if (isLoading) {
                    CircularProgressIndicator(color = CelestialGold, modifier = Modifier.padding(top = 16.dp))
                }

                // Show insight and detail button only after flip animation completes
                if (flipRotation.value >= 170f && dailyCard != null) {
                    dailyCard?.briefInsight?.takeIf { it.isNotBlank() }?.let { insight ->
                        Spacer(modifier = Modifier.height(24.dp))
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = CardNameTranslator.getDisplayName(context, dailyCard!!.cardId),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = CelestialGold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarWhite.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ArtNouveauButton(
                        text = stringResource(R.string.daily_view_details),
                        onClick = { onCardDetail(dailyCard!!.cardId) },
                        variant = ButtonVariant.SECONDARY
                    )
                }
            }
        }
        }
    }
}
