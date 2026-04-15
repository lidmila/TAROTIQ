package com.tarotiq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.tarotiq.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ComebackRewardBanner(
    visible: Boolean,
    onClaim: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(500)) { -it } + fadeIn(tween(400)),
        exit = slideOutVertically(tween(300)) { -it } + fadeOut(tween(200))
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            backgroundColor = AstralPurpleDim.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.comeback_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CelestialGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.comeback_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MoonSilver,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.comeback_reward),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CelestialGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ArtNouveauButton(
                    text = stringResource(R.string.comeback_claim),
                    onClick = onClaim,
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StreakFreezeNotice(
    visible: Boolean,
    remainingFreezes: Int
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(400)) { -it } + fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            backgroundColor = SuccessEmerald.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "\u2744\uFE0F", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.streak_freeze_used),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SuccessEmerald
                    )
                    Text(
                        text = stringResource(R.string.streak_freeze_used_desc, remainingFreezes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MoonSilver
                    )
                }
            }
        }
    }
}

@Composable
fun StreakMilestonePopup(
    milestone: Int?,
    coinReward: Int,
    onDismiss: () -> Unit
) {
    val visible = milestone != null

    LaunchedEffect(milestone) {
        if (milestone != null) {
            delay(4000)
            onDismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "milestone_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "milestone_pulse"
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
            if (milestone != null) {
                Box(
                    modifier = Modifier
                        .padding(40.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CosmicMid, CosmicDeep)
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
                        Text(text = "\uD83C\uDF1F", fontSize = 40.sp)

                        Text(
                            text = stringResource(R.string.milestone_title),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                letterSpacing = 2.sp
                            ),
                            color = CelestialGold
                        )

                        Text(
                            text = stringResource(R.string.milestone_text, milestone),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = StarWhite,
                            textAlign = TextAlign.Center
                        )

                        if (coinReward > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassCard(modifier = Modifier.wrapContentSize()) {
                                Text(
                                    text = stringResource(R.string.milestone_reward, coinReward),
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
