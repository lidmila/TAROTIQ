package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingViewModel
import androidx.compose.runtime.mutableIntStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingInterpretationScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    onBuyExtraCard: () -> Unit = {},
    readingViewModel: ReadingViewModel = viewModel()
) {
    val uiState by readingViewModel.uiState.collectAsState()

    // Request interpretation on first composition
    LaunchedEffect(Unit) {
        if (uiState.interpretation.isEmpty() && !uiState.isInterpreting) {
            readingViewModel.requestInterpretation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (uiState.interpretation.isNotEmpty()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.reading_interpretation_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = NewsreaderFamily,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = StarWhite
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = StarWhite
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CosmicDeep.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        ) { padding ->
            if (uiState.isInterpreting) {
                LoadingInterpretation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else if (uiState.error != null) {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { readingViewModel.requestInterpretation() },
                    onBack = onBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main interpretation
                    if (uiState.interpretation.isNotEmpty()) {
                        item(key = "interpretation") {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                InterpretationBubble(text = uiState.interpretation)
                            }
                        }

                        // Extra card purchase offer + action buttons
                        item(key = "actions") {
                            Spacer(modifier = Modifier.height(16.dp))
                            OrnamentalDivider()
                            Spacer(modifier = Modifier.height(20.dp))

                            // Extra card offer
                            Text(
                                text = stringResource(R.string.reading_extra_card_offer),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = ManropeFamily
                                ),
                                color = MoonSilver,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ArtNouveauButton(
                                text = stringResource(R.string.reading_buy_extra_card),
                                onClick = onBuyExtraCard,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                variant = ButtonVariant.SECONDARY
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Summary button
                            ArtNouveauButton(
                                text = stringResource(R.string.reading_summary),
                                onClick = onDone,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                variant = ButtonVariant.PRIMARY
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------
// Loading animation: candle video background + pulsing icon
// -----------------------------------------------

@Composable
private fun LoadingInterpretation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "loading_breathe")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier) {
        // Video background
        val exoPlayer = remember {
            androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                val uri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.loading_candle}")
                setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                volume = 0f
                prepare()
                play()
            }
        }
        DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

        AndroidView(
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack.copy(alpha = 0.55f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = glowAlpha
                }
            ) {
                Image(
                    painter = painterResource(R.drawable.fortune),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.reading_interpreting),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Normal
                ),
                color = CelestialGold.copy(alpha = glowAlpha),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AstralPurple,
                trackColor = CosmicMid
            )
        }
    }
}

// -----------------------------------------------
// Error state
// -----------------------------------------------

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInsufficientCoins = error == "INSUFFICIENT_COINS"
    val isNoInternet = error == "NO_INTERNET"
    val isTimeout = error == "TIMEOUT"
    val isAuthError = error == "NOT_AUTHENTICATED"

    val title = when {
        isInsufficientCoins -> stringResource(R.string.error_insufficient_coins_title)
        isNoInternet -> stringResource(R.string.error_no_internet_title)
        isTimeout -> stringResource(R.string.error_timeout_title)
        isAuthError -> stringResource(R.string.error_auth_title)
        else -> stringResource(R.string.error_generic)
    }

    val description = when {
        isInsufficientCoins -> stringResource(R.string.error_insufficient_coins_desc)
        isNoInternet -> stringResource(R.string.error_no_internet_desc)
        isTimeout -> stringResource(R.string.error_timeout_desc)
        isAuthError -> stringResource(R.string.error_auth_desc)
        else -> error
    }

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isInsufficientCoins) {
            SymbolIcon(symbol = Symbol.COIN, size = 48.dp, color = CelestialGold)
        } else {
            Image(
                painter = painterResource(R.drawable.fortune),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily),
            color = if (isInsufficientCoins) CelestialGold else ErrorCrimson,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
            color = MoonSilver,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        if (isInsufficientCoins) {
            ArtNouveauButton(
                text = stringResource(R.string.back),
                onClick = onBack,
                variant = ButtonVariant.PRIMARY
            )
        } else {
            ArtNouveauButton(
                text = stringResource(R.string.error_retry),
                onClick = onRetry,
                variant = ButtonVariant.PRIMARY
            )
        }
    }
}

// -----------------------------------------------
// Interpretation bubble with typewriter effect
// -----------------------------------------------

@Composable
private fun InterpretationBubble(text: String) {
    var visibleCount by remember { mutableIntStateOf(0) }
    val totalChars = text.length

    LaunchedEffect(text) {
        visibleCount = 0
        for (i in 1..totalChars) {
            visibleCount = i
            kotlinx.coroutines.delay(18L)
        }
    }

    val displayText = text.take(visibleCount)
    val isTyping = visibleCount < totalChars

    ArtNouveauFrame(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AstralPurple.copy(alpha = 0.10f),
                shape = RoundedCornerShape(24.dp)
            ),
        frameStyle = FrameStyle.ORNATE,
        backgroundColor = CosmicMid.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AstralPurple.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fortune),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.reading_oracle_insight),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = NewsreaderFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        ),
                        color = StarWhite
                    )
                    Text(
                        text = stringResource(R.string.reading_generated_by_ai),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            fontSize = 9.sp
                        ),
                        color = DimSilver
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val annotated = remember(displayText) {
                val raw = displayText + if (isTyping) "\u2588" else ""
                androidx.compose.ui.text.buildAnnotatedString {
                    var i = 0
                    while (i < raw.length) {
                        val boldStart = raw.indexOf("**", i)
                        if (boldStart == -1) {
                            append(raw.substring(i))
                            break
                        }
                        append(raw.substring(i, boldStart))
                        val boldEnd = raw.indexOf("**", boldStart + 2)
                        if (boldEnd == -1) {
                            append(raw.substring(boldStart))
                            break
                        }
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = CelestialGold))
                        append(raw.substring(boldStart + 2, boldEnd))
                        pop()
                        i = boldEnd + 2
                    }
                }
            }
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    letterSpacing = 0.3.sp
                ),
                color = StarWhite.copy(alpha = 0.95f)
            )
        }
    }
}
