package com.tarotiq.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.R
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.AstroUtils
import com.tarotiq.app.viewmodel.HomeViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToDailyCard: () -> Unit,
    onNavigateToNewReading: () -> Unit,
    onNavigateToCoinShop: () -> Unit,
    onNavigateToQuickReading: (spreadType: String) -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    val dailyCard by homeViewModel.dailyCard.collectAsState()
    val dailyCardRevealed by homeViewModel.dailyCardRevealed.collectAsState()
    val coinBalance by homeViewModel.coinBalance.collectAsState()
    val moonPhase = homeViewModel.moonPhase

    // Breathing animation for glow effects
    val infiniteTransition = rememberInfiniteTransition(label = "home_breathe")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Portal Header ──
                PortalHeader(
                    coinBalance = coinBalance.balance,
                    onProfileClick = onNavigateToProfile,
                    onCoinClick = onNavigateToCoinShop
                )

                // ── Welcome / Hero Section ──
                WelcomeSection(
                    moonPhase = moonPhase,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ── Daily Card Draw Section ──
                DailyCardDrawSection(
                    dailyCard = dailyCard,
                    dailyCardRevealed = dailyCardRevealed,
                    glowAlpha = glowAlpha,
                    onRevealClick = onNavigateToDailyCard,
                    onReflectClick = onNavigateToDailyCard,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ── Quick Readings Section ──
                QuickReadingsSection(
                    onNavigateToNewReading = onNavigateToNewReading,
                    onNavigateToQuickReading = onNavigateToQuickReading,
                    onNavigateToLibrary = onNavigateToLibrary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── New Reading CTA Button ──
                ArtNouveauButton(
                    text = stringResource(R.string.home_new_reading),
                    onClick = onNavigateToNewReading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    variant = ButtonVariant.PRIMARY
                )

                // Free readings info
                if (coinBalance.hasFreeReadings) {
                    Text(
                        text = stringResource(
                            R.string.coins_free_readings,
                            coinBalance.freeReadingsRemaining
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessEmerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Lunar Phase Bar ──
                LunarPhaseSection(
                    moonPhase = moonPhase,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Rewarded Ad Card ──
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    RewardedAdCard()
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════
// Portal Header — Stitch "Portal" style
// ═══════════════════════════════════════════

@Composable
private fun PortalHeader(
    coinBalance: Int,
    onProfileClick: () -> Unit,
    onCoinClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VoidBlack,
                        VoidBlack.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: sparkle icon + "Tarotiq" branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AstralPurple,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Tarotiq",
                    style = TextStyle(
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = AstralPurple,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 16f
                    }
                )
            }

            // Right: coin balance chip + profile avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Coin balance chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CosmicMid.copy(alpha = 0.7f))
                        .clickable(onClick = onCoinClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SymbolIcon(
                        symbol = Symbol.COIN,
                        size = 18.dp,
                        color = CelestialGold
                    )
                    Text(
                        text = "$coinBalance",
                        style = MaterialTheme.typography.titleSmall,
                        color = CelestialGold
                    )
                }

                // Profile avatar in circular frame
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            width = 1.5.dp,
                            color = AstralPurple.copy(alpha = 0.20f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(CosmicMid.copy(alpha = 0.6f))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = MoonSilver,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Welcome Section — Portal-style hero
// ═══════════════════════════════════════════

@Composable
private fun WelcomeSection(
    moonPhase: AstroUtils.MoonPhase,
    modifier: Modifier = Modifier
) {
    val phaseName = getMoonPhaseName(moonPhase)

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        // Celestial status label
        Text(
            text = "$phaseName \u2022 ${stringResource(R.string.home_celestial_active)}".uppercase(),
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 3.sp
            ),
            color = CelestialGold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large headline with italic accent
        Column {
            Text(
                text = stringResource(R.string.home_headline_1),
                style = TextStyle(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 40.sp,
                    lineHeight = 46.sp
                ),
                color = StarWhite
            )
            Text(
                text = stringResource(R.string.home_headline_2),
                style = TextStyle(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 40.sp,
                    lineHeight = 46.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = AstralPurple
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Side glass card with descriptive body text
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = CosmicMid.copy(alpha = 0.6f)
        ) {
            Text(
                text = stringResource(R.string.home_welcome_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MoonSilver,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════
// Daily Card Draw Section — Portal style
// ═══════════════════════════════════════════

@Composable
@Suppress("UNUSED_PARAMETER")
private fun DailyCardDrawSection(
    dailyCard: com.tarotiq.app.domain.model.DailyCard?,
    dailyCardRevealed: Boolean,
    glowAlpha: Float,
    onRevealClick: () -> Unit,
    onReflectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "DAILY DRAWING" label
        Text(
            text = stringResource(R.string.home_daily_drawing),
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 3.sp
            ),
            color = CelestialGold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "Consult the Oracle" headline
        Text(
            text = stringResource(R.string.home_consult_oracle),
            style = TextStyle(
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.Light,
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            color = StarWhite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = stringResource(R.string.home_daily_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MoonSilver,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Pulsing animations for unrevealed card
        val cardTransition = rememberInfiniteTransition(label = "daily_card_pulse")
        val pulseScale by cardTransition.animateFloat(
            initialValue = 0.96f, targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "card_pulse_scale"
        )
        val glowPulse by cardTransition.animateFloat(
            initialValue = 0.15f, targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "card_glow_pulse"
        )
        val borderShimmer by cardTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "card_border_shimmer"
        )
        val levitate by cardTransition.animateFloat(
            initialValue = -6f, targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "card_levitate"
        )

        // Centered card slot — hero element with dramatic presence
        Box(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .fillMaxWidth(0.6f)
                .aspectRatio(2f / 3f),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulsing glow aura (behind the card, larger)
            if (!dailyCardRevealed) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.5f
                        scaleY = 1.5f
                    }
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AstralPurple.copy(alpha = glowPulse * 0.4f),
                                CelestialGold.copy(alpha = glowPulse * 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.minDimension * 0.5f
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 0.5f
                    )
                }
            }

            // The card itself
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (!dailyCardRevealed) {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            translationY = levitate * density
                        }
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (!dailyCardRevealed) {
                            // Animated shimmer border
                            val shimmerColors = listOf(
                                CelestialGold.copy(alpha = 0.3f),
                                AstralPurple.copy(alpha = 0.6f),
                                CelestialGold.copy(alpha = 0.8f),
                                AstralPurple.copy(alpha = 0.6f),
                                CelestialGold.copy(alpha = 0.3f)
                            )
                            Modifier.border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = shimmerColors,
                                    start = Offset(borderShimmer * 600f - 200f, 0f),
                                    end = Offset(borderShimmer * 600f + 200f, 400f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = CelestialGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    )
                    .clickable {
                        if (!dailyCardRevealed) onRevealClick() else onReflectClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Card back image
                Image(
                    painter = painterResource(R.drawable.card_back),
                    contentDescription = "Daily card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // "Awaiting invocation" overlay
                if (!dailyCardRevealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        VoidBlack.copy(alpha = 0.6f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_awaiting),
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 3.sp,
                                lineHeight = 18.sp
                            ),
                            color = CelestialGold.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action area below the card
        if (dailyCardRevealed && dailyCard != null) {
            Text(
                text = stringResource(R.string.daily_already_drawn),
                style = MaterialTheme.typography.bodyMedium,
                color = StarWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (!dailyCard.briefInsight.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dailyCard.briefInsight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MoonSilver,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ArtNouveauButton(
                text = stringResource(R.string.home_reflect),
                onClick = onReflectClick,
                variant = ButtonVariant.PRIMARY,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                textStyle = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            )
        } else {
            ArtNouveauButton(
                text = stringResource(R.string.home_daily_card_desc),
                onClick = onRevealClick,
                variant = ButtonVariant.PRIMARY,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                textStyle = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

// ═══════════════════════════════════════════
// Quick Readings — Portal-style spread grid
// ═══════════════════════════════════════════

@Composable
@Suppress("UNUSED_PARAMETER")
private fun QuickReadingsSection(
    onNavigateToNewReading: () -> Unit,
    onNavigateToQuickReading: (String) -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.home_quick_reading),
                style = MaterialTheme.typography.headlineMedium,
                color = StarWhite
            )
            Text(
                text = stringResource(R.string.home_explore),
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                ),
                color = CelestialGold,
                modifier = Modifier
                    .clickable(onClick = onNavigateToLibrary)
                    .padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Featured: Celtic Cross — full width with gold top border accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .goldGlow()
                .clip(RoundedCornerShape(16.dp))
                .background(CosmicMid.copy(alpha = 0.6f))
                .drawBehind {
                    // Gold top border accent
                    drawRect(
                        color = CelestialGold.copy(alpha = 0.50f),
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx())
                    )
                    // Ghost border
                    drawRoundRect(
                        color = AstralPurple.copy(alpha = 0.08f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                }
                .clickable { onNavigateToQuickReading("celtic_cross") }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_most_insightful),
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp,
                                letterSpacing = 2.sp
                            ),
                            color = CelestialGold.copy(alpha = 0.8f)
                        )
                        // Coin cost badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SymbolIcon(symbol = Symbol.COIN, size = 12.dp, color = CelestialGold)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "5", style = MaterialTheme.typography.labelSmall, color = CelestialGold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.spread_celtic_cross),
                        style = MaterialTheme.typography.headlineSmall,
                        color = StarWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.spread_celtic_cross_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MoonSilver
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.celtic),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row: Three Card + Relationship side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpreadCard(
                title = stringResource(R.string.spread_three_card),
                subtitle = stringResource(R.string.spread_three_card_desc),
                iconRes = R.drawable.cards_3,
                coinCost = 2,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToQuickReading("three_card") }
            )

            SpreadCard(
                title = stringResource(R.string.home_connection),
                subtitle = stringResource(R.string.home_connection_desc),
                iconRes = R.drawable.card_connection,
                coinCost = 5,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToQuickReading("relationship") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Single card — full width row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .goldGlow()
                .clip(RoundedCornerShape(16.dp))
                .background(CosmicMid.copy(alpha = 0.5f))
                .drawBehind {
                    drawRoundRect(
                        color = AstralPurple.copy(alpha = 0.06f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                }
                .clickable { onNavigateToQuickReading("single") }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.card_1),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.spread_single),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
                        color = StarWhite
                    )
                    Text(
                        text = stringResource(R.string.spread_single_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MoonSilver
                    )
                }
                // Coin cost
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SymbolIcon(symbol = Symbol.COIN, size = 14.dp, color = CelestialGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "1", style = MaterialTheme.typography.labelSmall, color = CelestialGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = CelestialGold.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SpreadCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    coinCost: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .goldGlow()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicMid.copy(alpha = 0.5f))
            .drawBehind {
                // Gold top border accent
                drawRect(
                    color = CelestialGold.copy(alpha = 0.30f),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx())
                )
                drawRoundRect(
                    color = AstralPurple.copy(alpha = 0.06f),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
            if (coinCost > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SymbolIcon(symbol = Symbol.COIN, size = 14.dp, color = CelestialGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coinCost",
                        style = MaterialTheme.typography.labelSmall,
                        color = CelestialGold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
            color = StarWhite
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MoonSilver
        )
    }
}

// ═══════════════════════════════════════════
// Lunar Phase Bar — Portal style
// ═══════════════════════════════════════════

@Composable
private fun LunarPhaseSection(
    moonPhase: AstroUtils.MoonPhase,
    modifier: Modifier = Modifier
) {
    val illumination = getMoonIllumination(moonPhase)
    val phaseName = getMoonPhaseName(moonPhase)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CosmicMid.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedSymbolIcon(
                    symbol = moonPhaseToSymbol(moonPhase),
                    size = 24.dp,
                    color = AstralPurple,
                    glowColor = AstralPurple
                )
                Text(
                    text = stringResource(R.string.home_lunar_phase),
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    ),
                    color = CelestialGold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phase details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = phaseName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = StarWhite
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${illumination}%",
                        style = TextStyle(
                            fontFamily = NewsreaderFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 28.sp
                        ),
                        color = CelestialGold
                    )
                    Text(
                        text = stringResource(R.string.home_visibility),
                        style = TextStyle(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        ),
                        color = MoonSilver
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(StarWhite.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(illumination / 100f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AstralPurple, CelestialGold)
                            )
                        )
                ) {
                    // Bright dot at the end
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Rewarded Ad Card — Portal style
// ═══════════════════════════════════════════

@Composable
private fun RewardedAdCard() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val settingsManager = remember { SettingsManager(context) }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    val scope = rememberCoroutineScope()

    val adsWatchedThisWeek by settingsManager.rewardedAdsThisWeekFlow.collectAsState(initial = 0)
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val maxAdsPerWeek = 3
    val remaining = maxAdsPerWeek - adsWatchedThisWeek

    LaunchedEffect(Unit) { rewardedAdManager.preload() }

    if (remaining > 0) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = CosmicMid.copy(alpha = 0.5f)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (activity != null && !isLoading) {
                                isLoading = true
                                rewardedAdManager.show(
                                    activity = activity,
                                    onRewarded = {
                                        scope.launch {
                                            settingsManager.incrementRewardedAdsCount()
                                            val userId =
                                                FirebaseAuth.getInstance().currentUser?.uid
                                            if (userId != null) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    val coinRef =
                                                        db.collection("users").document(userId)
                                                            .collection("coins")
                                                            .document("balance")
                                                    db.runTransaction { transaction ->
                                                        val doc = transaction.get(coinRef)
                                                        val current =
                                                            doc.getLong("balance")?.toInt() ?: 0
                                                        transaction.update(
                                                            coinRef,
                                                            "balance",
                                                            current + 1
                                                        )
                                                    }.await()
                                                } catch (_: Exception) {
                                                }
                                            }
                                            showSuccess = true
                                            isLoading = false
                                        }
                                    },
                                    onDismissed = { isLoading = false }
                                )
                            }
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedCoinIcon(size = 32.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.rewarded_ad_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = CelestialGold
                        )
                        Text(
                            text = stringResource(R.string.rewarded_ad_subtitle, remaining),
                            style = MaterialTheme.typography.bodySmall,
                            color = MoonSilver
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AstralPurple,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = CelestialGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        if (showSuccess) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSuccess = false
            }
            Text(
                text = stringResource(R.string.rewarded_ad_success),
                color = SuccessEmerald,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════
// Animated Symbol Icons
// ═══════════════════════════════════════════

/**
 * Animated symbol icon with glow aura + soft float + orbiting sparkle.
 * Used for spread cards, lunar phase, and decorative elements.
 */
@Composable
private fun AnimatedSymbolIcon(
    symbol: Symbol,
    size: Dp = 40.dp,
    color: Color = CelestialGold,
    glowColor: Color = color
) {
    val transition = rememberInfiniteTransition(label = "sym_${symbol.name}")

    // Soft float (no rotation)
    val float by transition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "sym_float"
    )
    // Orbiting sparkle angle
    val sparkleAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "sym_sparkle"
    )
    // Glow pulse
    val glow by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "sym_glow"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow aura
        Canvas(modifier = Modifier.size(size * 1.6f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = glow), Color.Transparent)
                ),
                radius = this.size.minDimension / 2f
            )
        }
        // Orbiting sparkle dot
        Canvas(modifier = Modifier.size(size * 1.4f)) {
            val angle = sparkleAngle * (PI / 180f).toFloat()
            val orbitR = this.size.minDimension * 0.42f
            drawCircle(
                color = CelestialGold,
                radius = 2.5f,
                center = Offset(
                    this.size.width / 2f + cos(angle) * orbitR,
                    this.size.height / 2f + sin(angle) * orbitR
                )
            )
        }
        // Symbol with float
        SymbolIcon(
            symbol = symbol,
            size = size,
            color = color,
            modifier = Modifier.graphicsLayer { translationY = float * density }
        )
    }
}

// ═══════════════════════════════════════════
// Animated Coin Icon (reward section)
// ═══════════════════════════════════════════

@Composable
private fun AnimatedCoinIcon(size: androidx.compose.ui.unit.Dp = 32.dp) {
    val transition = rememberInfiniteTransition(label = "coin_anim")

    // Slow Y-axis rotation
    val rotationY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coin_rotate"
    )

    // Shimmer glow pulse
    val shimmer by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_shimmer"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow behind coin
        Canvas(modifier = Modifier.size(size * 1.5f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CelestialGold.copy(alpha = 0.2f * shimmer),
                        Color.Transparent
                    )
                ),
                radius = this.size.minDimension / 2f
            )
        }
        // Coin with rotation
        SymbolIcon(
            symbol = Symbol.COIN,
            size = size,
            color = CelestialGold,
            modifier = Modifier.graphicsLayer {
                this.rotationY = rotationY
                scaleX = 0.85f + 0.15f * shimmer
                scaleY = 0.85f + 0.15f * shimmer
            }
        )
    }
}

// ═══════════════════════════════════════════
// Daily Card Icon with ritual animations
// ═══════════════════════════════════════════

@Composable
@Suppress("UNUSED_PARAMETER")
private fun DailyCardIcon(isRevealed: Boolean, glowAlpha: Float) {
    val transition = rememberInfiniteTransition(label = "daily_icon")

    // Levitation
    val float by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "float"
    )

    // Rotation wobble (very subtle)
    val wobble by transition.animateFloat(
        initialValue = -1.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "wobble"
    )

    // Aura pulse
    val auraPulse by transition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "aura"
    )

    // Icon scale
    val iconScale by transition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    // Icon alpha
    val iconAlpha by transition.animateFloat(
        initialValue = 0.75f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "icon_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Gold aura glow behind the card
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CelestialGold.copy(alpha = 0.25f * auraPulse),
                        CelestialGold.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.5f * auraPulse
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.minDimension * 0.5f * auraPulse
            )
        }

        // The card icon
        Image(
            painter = painterResource(R.drawable.card_back),
            contentDescription = "Daily card",
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    translationY = if (!isRevealed) float else 0f
                    rotationZ = if (!isRevealed) wobble else 0f
                    scaleX = if (!isRevealed) iconScale else 1f
                    scaleY = if (!isRevealed) iconScale else 1f
                    alpha = if (!isRevealed) iconAlpha else 1f
                },
            contentScale = ContentScale.Fit
        )
    }
}

// ═══════════════════════════════════════════
// Helper functions
// ═══════════════════════════════════════════

private fun moonPhaseToSymbol(phase: AstroUtils.MoonPhase): Symbol {
    return when (phase) {
        AstroUtils.MoonPhase.NEW_MOON -> Symbol.MOON_NEW
        AstroUtils.MoonPhase.WAXING_CRESCENT, AstroUtils.MoonPhase.FIRST_QUARTER -> Symbol.MOON_WAXING
        AstroUtils.MoonPhase.WAXING_GIBBOUS, AstroUtils.MoonPhase.FULL_MOON -> Symbol.MOON_FULL
        AstroUtils.MoonPhase.WANING_GIBBOUS, AstroUtils.MoonPhase.LAST_QUARTER -> Symbol.MOON_WANING
        AstroUtils.MoonPhase.WANING_CRESCENT -> Symbol.MOON_NEW
    }
}

private fun getMoonIllumination(phase: AstroUtils.MoonPhase): Int {
    return when (phase) {
        AstroUtils.MoonPhase.NEW_MOON -> 2
        AstroUtils.MoonPhase.WAXING_CRESCENT -> 25
        AstroUtils.MoonPhase.FIRST_QUARTER -> 50
        AstroUtils.MoonPhase.WAXING_GIBBOUS -> 84
        AstroUtils.MoonPhase.FULL_MOON -> 100
        AstroUtils.MoonPhase.WANING_GIBBOUS -> 84
        AstroUtils.MoonPhase.LAST_QUARTER -> 50
        AstroUtils.MoonPhase.WANING_CRESCENT -> 25
    }
}

@Composable
private fun getMoonPhaseName(phase: AstroUtils.MoonPhase): String {
    return when (phase) {
        AstroUtils.MoonPhase.NEW_MOON -> stringResource(R.string.moon_new)
        AstroUtils.MoonPhase.WAXING_CRESCENT -> stringResource(R.string.moon_waxing_crescent)
        AstroUtils.MoonPhase.FIRST_QUARTER -> stringResource(R.string.moon_first_quarter)
        AstroUtils.MoonPhase.WAXING_GIBBOUS -> stringResource(R.string.moon_waxing_gibbous)
        AstroUtils.MoonPhase.FULL_MOON -> stringResource(R.string.moon_full)
        AstroUtils.MoonPhase.WANING_GIBBOUS -> stringResource(R.string.moon_waning_gibbous)
        AstroUtils.MoonPhase.LAST_QUARTER -> stringResource(R.string.moon_last_quarter)
        AstroUtils.MoonPhase.WANING_CRESCENT -> stringResource(R.string.moon_waning_crescent)
    }
}
