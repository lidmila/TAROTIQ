package com.tarotiq.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.launch

private data class ZodiacItem(
    val key: String,
    val nameRes: Int,
    val symbol: String
)

private val zodiacSigns = listOf(
    ZodiacItem("aries", R.string.zodiac_aries, "\u2648"),
    ZodiacItem("taurus", R.string.zodiac_taurus, "\u2649"),
    ZodiacItem("gemini", R.string.zodiac_gemini, "\u264A"),
    ZodiacItem("cancer", R.string.zodiac_cancer, "\u264B"),
    ZodiacItem("leo", R.string.zodiac_leo, "\u264C"),
    ZodiacItem("virgo", R.string.zodiac_virgo, "\u264D"),
    ZodiacItem("libra", R.string.zodiac_libra, "\u264E"),
    ZodiacItem("scorpio", R.string.zodiac_scorpio, "\u264F"),
    ZodiacItem("sagittarius", R.string.zodiac_sagittarius, "\u2650"),
    ZodiacItem("capricorn", R.string.zodiac_capricorn, "\u2651"),
    ZodiacItem("aquarius", R.string.zodiac_aquarius, "\u2652"),
    ZodiacItem("pisces", R.string.zodiac_pisces, "\u2653")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (zodiacSign: String?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var selectedZodiac by remember { mutableStateOf<String?>(null) }

    // Breathing animation for icons
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_breathe")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < 3) {
                    TextButton(onClick = { onComplete(selectedZodiac) }) {
                        Text(
                            stringResource(R.string.onboarding_skip),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> WelcomePage(iconScale)
                    1 -> ZodiacPage(
                        selectedZodiac = selectedZodiac,
                        onZodiacSelected = { selectedZodiac = it }
                    )
                    2 -> DailyCardPage(iconScale)
                    3 -> ReadyPage(iconScale)
                }
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) GoldSecondary else TextSecondary.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            // Bottom button
            Button(
                onClick = {
                    if (pagerState.currentPage < 3) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete(selectedZodiac)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
            ) {
                Text(
                    text = if (pagerState.currentPage < 3)
                        stringResource(R.string.onboarding_next)
                    else
                        stringResource(R.string.onboarding_start),
                    style = MaterialTheme.typography.titleMedium
                )
                if (pagerState.currentPage < 3) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(iconScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\u2728",
            fontSize = 80.sp,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineMedium,
            color = GoldSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ZodiacPage(
    selectedZodiac: String?,
    onZodiacSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_zodiac),
            style = MaterialTheme.typography.headlineSmall,
            color = GoldSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_zodiac_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(zodiacSigns) { zodiac ->
                val isSelected = selectedZodiac == zodiac.key
                GlassCard(
                    onClick = { onZodiacSelected(zodiac.key) },
                    modifier = Modifier.aspectRatio(1f),
                    borderColor = if (isSelected) GoldSecondary else GlassBorder,
                    borderWidth = if (isSelected) 2.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = zodiac.symbol,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(zodiac.nameRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) GoldSecondary else TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCardPage(iconScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83C\uDCCF",
            fontSize = 80.sp,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_daily),
            style = MaterialTheme.typography.headlineMedium,
            color = GoldSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_daily_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReadyPage(iconScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDD2E",
            fontSize = 80.sp,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_ready),
            style = MaterialTheme.typography.headlineMedium,
            color = GoldSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_ready_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
