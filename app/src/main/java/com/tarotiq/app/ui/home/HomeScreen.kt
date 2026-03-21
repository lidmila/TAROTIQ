package com.tarotiq.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.AstroUtils
import com.tarotiq.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToDailyCard: () -> Unit,
    onNavigateToNewReading: () -> Unit,
    onNavigateToCoinShop: () -> Unit,
    onNavigateToQuickReading: (spreadType: String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val dailyCard by homeViewModel.dailyCard.collectAsState()
    val dailyCardRevealed by homeViewModel.dailyCardRevealed.collectAsState()
    val currentStreak by homeViewModel.currentStreak.collectAsState()
    val coinBalance by homeViewModel.coinBalance.collectAsState()
    val moonPhase = homeViewModel.moonPhase

    // Card breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "home_breathe")
    val cardGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top row: coin balance + profile icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coin balance widget
                GlassCard(
                    onClick = onNavigateToCoinShop,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\uD83E\uDE99",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${coinBalance.balance}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldSecondary
                        )
                    }
                }

                // Profile icon
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Card section
            GlassCard(
                onClick = {
                    if (!dailyCardRevealed) {
                        homeViewModel.drawDailyCard()
                    } else {
                        onNavigateToDailyCard()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = cardGlow }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_daily_card),
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (dailyCardRevealed && dailyCard != null) {
                        // Show revealed card indicator
                        Text(
                            text = "\uD83C\uDCCF",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.daily_already_drawn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Card back - tap to reveal
                        Text(
                            text = "\u2728",
                            fontSize = 64.sp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = cardGlow
                                scaleY = cardGlow
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_daily_card_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak + Moon phase row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak counter
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CandleFlicker,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$currentStreak",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldSecondary
                        )
                        Text(
                            text = stringResource(R.string.home_streak),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Moon phase widget
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = moonPhase.emoji,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getMoonPhaseName(moonPhase),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick reading buttons header
            Text(
                text = stringResource(R.string.home_quick_reading),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Quick reading grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickReadingCard(
                    title = stringResource(R.string.spread_single),
                    subtitle = "1",
                    icon = Icons.Default.Style,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToQuickReading("single") }
                )
                QuickReadingCard(
                    title = stringResource(R.string.spread_three_card),
                    subtitle = "3",
                    icon = Icons.Default.ViewColumn,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToQuickReading("three_card") }
                )
                QuickReadingCard(
                    title = stringResource(R.string.spread_celtic_cross),
                    subtitle = "10",
                    icon = Icons.Default.GridView,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToQuickReading("celtic_cross") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // New Reading button
            Button(
                onClick = onNavigateToNewReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_new_reading),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Free readings info
            if (coinBalance.hasFreeReadings) {
                Text(
                    text = stringResource(R.string.coins_free_readings, coinBalance.freeReadingsRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Bottom spacing for nav bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun QuickReadingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MysticLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.headlineSmall,
                color = GoldSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
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
