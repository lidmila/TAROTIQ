package com.tarotiq.app.ui.components

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tarotiq.app.R
import com.tarotiq.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WelcomeOfferDialog(
    onDismiss: () -> Unit,
    onBuyCoins: (Activity) -> Unit,
    onBuyLifetime: (Activity) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 48-hour countdown from first shown (use 48*60 minutes for demo, show MM:SS)
    var secondsLeft by remember { mutableIntStateOf(48 * 3600) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    val hours = secondsLeft / 3600
    val minutes = (secondsLeft % 3600) / 60
    val seconds = secondsLeft % 60
    val timerText = "%02d:%02d:%02d".format(hours, minutes, seconds)

    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glowA"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(
                        Color(0xFF1A0A3A), Color(0xFF0D0520), Color(0xFF1A0A3A)
                    ))
                )
                .border(1.dp, CelestialGold.copy(alpha = glowAlpha), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = MoonSilver.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Stars
                Row {
                    repeat(5) {
                        Icon(Icons.Default.Star, null, tint = CelestialGold, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    stringResource(R.string.welcome_offer_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal
                    ),
                    color = CelestialGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.welcome_offer_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
                    color = MoonSilver,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AstralPurple.copy(alpha = 0.2f))
                        .border(1.dp, AstralPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${stringResource(R.string.welcome_offer_timer)}: ",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily),
                            color = MoonSilver
                        )
                        Text(
                            timerText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold
                            ),
                            color = CelestialGold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Tier 1 — Coins
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicMid.copy(alpha = 0.4f))
                        .border(1.dp, AstralPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SymbolIcon(symbol = Symbol.COIN, size = 32.dp, color = CelestialGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.welcome_offer_tier1_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = NewsreaderFamily),
                                    color = StarWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = SuccessEmerald) {
                                    Text(
                                        stringResource(R.string.welcome_offer_tier1_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily),
                                        color = VoidBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                stringResource(R.string.welcome_offer_tier1_coins),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = ManropeFamily),
                                color = CelestialGold
                            )
                        }
                        TextButton(onClick = { activity?.let { onBuyCoins(it) } }) {
                            Text("Koupit", color = CelestialGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Tier 2 — Lifetime (recommended)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(
                                Color(0xFF2D1B69), Color(0xFF1A0A3A)
                            ))
                        )
                        .border(2.dp, CelestialGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = CelestialGold) {
                                Text(
                                    stringResource(R.string.welcome_offer_recommended),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily),
                                    color = VoidBlack,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = AstralPurple) {
                                Text(
                                    stringResource(R.string.welcome_offer_tier2_badge),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily),
                                    color = StarWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = CelestialGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.welcome_offer_tier2_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Bold
                                    ),
                                    color = CelestialGold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stringResource(R.string.welcome_offer_tier2_original),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = ManropeFamily,
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = MoonSilver.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(R.string.welcome_offer_tier2_price),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = ManropeFamily, fontWeight = FontWeight.Bold
                                        ),
                                        color = SuccessEmerald
                                    )
                                }
                                Text(
                                    stringResource(R.string.welcome_offer_tier2_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = ManropeFamily, fontSize = 11.sp),
                                    color = MoonSilver.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        ArtNouveauButton(
                            text = stringResource(R.string.lifetime_buy),
                            onClick = { activity?.let { onBuyLifetime(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.PRIMARY
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(R.string.welcome_offer_skip),
                        color = MoonSilver.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
