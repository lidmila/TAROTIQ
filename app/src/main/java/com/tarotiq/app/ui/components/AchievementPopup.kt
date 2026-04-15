package com.tarotiq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.AchievementDef
import com.tarotiq.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AchievementPopup(
    achievementDef: AchievementDef?,
    onDismiss: () -> Unit
) {
    val visible = achievementDef != null

    // Auto-dismiss after 3 seconds
    LaunchedEffect(achievementDef) {
        if (achievementDef != null) {
            delay(3000)
            onDismiss()
        }
    }

    // Pulse animation for the gold border
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.5f, animationSpec = tween(400, easing = EaseOutBack)),
        exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimMedium),
            contentAlignment = Alignment.Center
        ) {
            if (achievementDef != null) {
                Box(
                    modifier = Modifier
                        .padding(40.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CosmicMid,
                                    CosmicDeep
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.verticalGradient(
                                listOf(CelestialGold, CelestialGoldDim)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Star icon
                        Text(
                            text = "\u2728",
                            fontSize = 40.sp
                        )

                        // "Achievement Unlocked!"
                        Text(
                            text = stringResource(R.string.achievement_unlocked),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                letterSpacing = 2.sp
                            ),
                            color = CelestialGold
                        )

                        // Achievement name
                        Text(
                            text = stringResource(achievementDef.titleResId),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = StarWhite,
                            textAlign = TextAlign.Center
                        )

                        // Achievement description
                        Text(
                            text = stringResource(achievementDef.descResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MoonSilver,
                            textAlign = TextAlign.Center
                        )

                        // Coin reward
                        if (achievementDef.coinReward > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassCard(
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = stringResource(R.string.achievement_coin_reward, achievementDef.coinReward),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = CelestialGold,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
