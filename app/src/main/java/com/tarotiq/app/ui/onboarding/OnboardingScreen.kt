package com.tarotiq.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.LocaleUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalContext
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
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    var selectedZodiac by remember { mutableStateOf<String?>(null) }

    // Breathing animation for symbols
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_breathe")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
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
                if (pagerState.currentPage < 4) {
                    TextButton(onClick = { onComplete(selectedZodiac) }) {
                        Text(
                            stringResource(R.string.onboarding_skip).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily
                            ),
                            color = MoonSilver
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
                    0 -> LanguagePage(iconScale)
                    1 -> WelcomePage(iconScale)
                    2 -> ZodiacPage(
                        selectedZodiac = selectedZodiac,
                        onZodiacSelected = { selectedZodiac = it }
                    )
                    3 -> DailyCardPage(iconScale)
                    4 -> ReadyPage(iconScale)
                }
            }

            // Diamond page indicators
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(if (isSelected) 10.dp else 7.dp)
                            .rotate(45f)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isSelected) CelestialGold else MoonSilver.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Bottom button
            ArtNouveauButton(
                text = if (pagerState.currentPage < 4)
                    stringResource(R.string.onboarding_next)
                else
                    stringResource(R.string.onboarding_start),
                onClick = {
                    if (pagerState.currentPage < 4) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete(selectedZodiac)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
                variant = if (pagerState.currentPage < 4) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY
            )
        }
        }
    }
}

@Composable
private fun LanguagePage(iconScale: Float) {
    val context = LocalContext.current
    val languages = remember { LocaleUtils.getAvailableLanguages() }
    var selectedCode by remember { mutableStateOf(LocaleUtils.getCurrentLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.fortune),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_language),
            style = MaterialTheme.typography.headlineMedium,
            color = CelestialGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(languages) { (code, name) ->
                val isSelected = selectedCode == code
                ArtNouveauFrame(
                    onClick = {
                        selectedCode = code
                        LocaleUtils.saveAndApplyLanguage(context, code)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    frameStyle = if (isSelected) FrameStyle.ORNATE else FrameStyle.SIMPLE,
                    backgroundColor = if (isSelected) CosmicSurface else GlassBackground
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isSelected) CelestialGold else MoonSilver,
                            textAlign = TextAlign.Center
                        )
                    }
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
        SymbolIcon(
            symbol = Symbol.STAR,
            size = 100.dp,
            color = CelestialGold,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = CelestialGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MoonSilver,
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
            style = MaterialTheme.typography.headlineMedium,
            color = CelestialGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_zodiac_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MoonSilver,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(zodiacSigns) { zodiac ->
                val isSelected = selectedZodiac == zodiac.key
                ArtNouveauFrame(
                    onClick = { onZodiacSelected(zodiac.key) },
                    modifier = Modifier.aspectRatio(1f),
                    frameStyle = if (isSelected) FrameStyle.ORNATE else FrameStyle.SIMPLE,
                    backgroundColor = if (isSelected) CosmicSurface else GlassBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Zodiac Unicode symbols (text, not emoji)
                        Text(
                            text = zodiac.symbol,
                            fontFamily = NewsreaderFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelected) CelestialGold else MoonSilver
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(zodiac.nameRes).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFamily,
                                letterSpacing = 1.sp
                            ),
                            color = if (isSelected) CelestialGold else MoonSilver,
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
        SymbolIcon(
            symbol = Symbol.TAROT_CARD,
            size = 100.dp,
            color = CelestialGold,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = stringResource(R.string.onboarding_daily),
            style = MaterialTheme.typography.headlineLarge,
            color = CelestialGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.onboarding_daily_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MoonSilver,
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
        Image(
            painter = painterResource(R.drawable.fortune),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = stringResource(R.string.onboarding_ready),
            style = MaterialTheme.typography.headlineLarge,
            color = CelestialGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.onboarding_ready_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MoonSilver,
            textAlign = TextAlign.Center
        )
    }
}
