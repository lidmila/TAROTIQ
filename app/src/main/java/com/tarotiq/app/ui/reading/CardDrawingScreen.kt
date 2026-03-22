package com.tarotiq.app.ui.reading

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.ReadingSpread
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingUiState
import com.tarotiq.app.viewmodel.ReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDrawingScreen(
    onGetReading: () -> Unit,
    onBack: () -> Unit,
    readingViewModel: ReadingViewModel = viewModel()
) {
    val uiState by readingViewModel.uiState.collectAsState()
    val allRevealed by readingViewModel.allCardsRevealed.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current

    // Draw cards on first composition
    LaunchedEffect(Unit) {
        if (uiState.drawnCards.isEmpty()) {
            readingViewModel.drawCards()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (uiState.spread) {
                                ReadingSpread.SINGLE -> stringResource(R.string.spread_single)
                                ReadingSpread.THREE_CARD -> stringResource(R.string.spread_three_card)
                                ReadingSpread.RELATIONSHIP -> stringResource(R.string.spread_relationship)
                                ReadingSpread.CELTIC_CROSS -> stringResource(R.string.spread_celtic_cross)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MidnightBg2.copy(alpha = 0.8f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Instruction text
                Text(
                    text = if (allRevealed) stringResource(R.string.drawing_all_revealed)
                           else stringResource(R.string.drawing_tap_to_reveal),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Card spread layout
                when (uiState.spread) {
                    ReadingSpread.SINGLE -> {
                        SingleCardLayout(
                            uiState = uiState,
                            onReveal = { index ->
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                readingViewModel.revealCard(index)
                            },
                            context = context
                        )
                    }
                    ReadingSpread.THREE_CARD -> {
                        ThreeCardLayout(
                            uiState = uiState,
                            onReveal = { index ->
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                readingViewModel.revealCard(index)
                            },
                            context = context
                        )
                    }
                    ReadingSpread.RELATIONSHIP -> {
                        RelationshipLayout(
                            uiState = uiState,
                            onReveal = { index ->
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                readingViewModel.revealCard(index)
                            },
                            context = context
                        )
                    }
                    ReadingSpread.CELTIC_CROSS -> {
                        CelticCrossLayout(
                            uiState = uiState,
                            onReveal = { index ->
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                readingViewModel.revealCard(index)
                            },
                            context = context
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Get Reading button - appears after all cards revealed
                if (allRevealed) {
                    Button(
                        onClick = onGetReading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.drawing_get_reading),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Flippable card with 3D rotation animation
// ─────────────────────────────────────────────

@Composable
private fun FlippableCard(
    cardId: Int,
    isReversed: Boolean,
    positionLabel: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    cardWidth: Int = 100,
    cardHeight: Int = 150
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic),
        label = "card_flip"
    )

    val cardResId = remember(cardId) {
        getCardDrawableResId(context, cardId)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Position label above card
        Text(
            text = positionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .width(cardWidth.dp)
                .height(cardHeight.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = !isRevealed) { onReveal() }
        ) {
            if (rotation <= 90f) {
                // Card back (face-down)
                Image(
                    painter = painterResource(R.drawable.card_back),
                    contentDescription = stringResource(R.string.drawing_tap_to_reveal),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Card front (revealed) - mirror horizontally so it reads correctly after flip
                Image(
                    painter = painterResource(cardResId),
                    contentDescription = positionLabel,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                            if (isReversed) rotationZ = 180f
                        }
                )
            }
        }

        // Upright / Reversed indicator after reveal
        if (isRevealed) {
            Text(
                text = if (isReversed) stringResource(R.string.reading_reversed)
                       else stringResource(R.string.reading_upright),
                style = MaterialTheme.typography.labelSmall,
                color = if (isReversed) CardReversed else CardUpright,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Layout variants per spread type
// ─────────────────────────────────────────────

@Composable
private fun SingleCardLayout(
    uiState: ReadingUiState,
    onReveal: (Int) -> Unit,
    context: android.content.Context
) {
    if (uiState.drawnCards.isNotEmpty()) {
        val card = uiState.drawnCards[0]
        FlippableCard(
            cardId = card.cardId,
            isReversed = card.isReversed,
            positionLabel = card.positionMeaning,
            isRevealed = 0 in uiState.revealedCardIndices,
            onReveal = { onReveal(0) },
            context = context,
            cardWidth = 140,
            cardHeight = 210
        )
    }
}

@Composable
private fun ThreeCardLayout(
    uiState: ReadingUiState,
    onReveal: (Int) -> Unit,
    context: android.content.Context
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        uiState.drawnCards.forEachIndexed { index, card ->
            FlippableCard(
                cardId = card.cardId,
                isReversed = card.isReversed,
                positionLabel = card.positionMeaning,
                isRevealed = index in uiState.revealedCardIndices,
                onReveal = { onReveal(index) },
                context = context,
                cardWidth = 100,
                cardHeight = 150
            )
        }
    }
}

@Composable
private fun RelationshipLayout(
    uiState: ReadingUiState,
    onReveal: (Int) -> Unit,
    context: android.content.Context
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row: You + Partner
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (uiState.drawnCards.size > 0) {
                val c = uiState.drawnCards[0]
                FlippableCard(
                    cardId = c.cardId, isReversed = c.isReversed,
                    positionLabel = c.positionMeaning,
                    isRevealed = 0 in uiState.revealedCardIndices,
                    onReveal = { onReveal(0) }, context = context,
                    cardWidth = 90, cardHeight = 135
                )
            }
            if (uiState.drawnCards.size > 1) {
                val c = uiState.drawnCards[1]
                FlippableCard(
                    cardId = c.cardId, isReversed = c.isReversed,
                    positionLabel = c.positionMeaning,
                    isRevealed = 1 in uiState.revealedCardIndices,
                    onReveal = { onReveal(1) }, context = context,
                    cardWidth = 90, cardHeight = 135
                )
            }
        }
        // Middle: Relationship
        if (uiState.drawnCards.size > 2) {
            val c = uiState.drawnCards[2]
            FlippableCard(
                cardId = c.cardId, isReversed = c.isReversed,
                positionLabel = c.positionMeaning,
                isRevealed = 2 in uiState.revealedCardIndices,
                onReveal = { onReveal(2) }, context = context,
                cardWidth = 90, cardHeight = 135
            )
        }
        // Bottom: Challenges + Future
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (uiState.drawnCards.size > 3) {
                val c = uiState.drawnCards[3]
                FlippableCard(
                    cardId = c.cardId, isReversed = c.isReversed,
                    positionLabel = c.positionMeaning,
                    isRevealed = 3 in uiState.revealedCardIndices,
                    onReveal = { onReveal(3) }, context = context,
                    cardWidth = 90, cardHeight = 135
                )
            }
            if (uiState.drawnCards.size > 4) {
                val c = uiState.drawnCards[4]
                FlippableCard(
                    cardId = c.cardId, isReversed = c.isReversed,
                    positionLabel = c.positionMeaning,
                    isRevealed = 4 in uiState.revealedCardIndices,
                    onReveal = { onReveal(4) }, context = context,
                    cardWidth = 90, cardHeight = 135
                )
            }
        }
    }
}

@Composable
private fun CelticCrossLayout(
    uiState: ReadingUiState,
    onReveal: (Int) -> Unit,
    context: android.content.Context
) {
    // Simplified Celtic Cross: 2 rows of 3 + 1 row of 4
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cross section - top row (cards 0-2)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0..2) {
                if (i < uiState.drawnCards.size) {
                    val c = uiState.drawnCards[i]
                    FlippableCard(
                        cardId = c.cardId, isReversed = c.isReversed,
                        positionLabel = c.positionMeaning,
                        isRevealed = i in uiState.revealedCardIndices,
                        onReveal = { onReveal(i) }, context = context,
                        cardWidth = 70, cardHeight = 105
                    )
                }
            }
        }
        // Cross section - bottom row (cards 3-5)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 3..5) {
                if (i < uiState.drawnCards.size) {
                    val c = uiState.drawnCards[i]
                    FlippableCard(
                        cardId = c.cardId, isReversed = c.isReversed,
                        positionLabel = c.positionMeaning,
                        isRevealed = i in uiState.revealedCardIndices,
                        onReveal = { onReveal(i) }, context = context,
                        cardWidth = 70, cardHeight = 105
                    )
                }
            }
        }
        // Staff section (cards 6-9)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 6..9) {
                if (i < uiState.drawnCards.size) {
                    val c = uiState.drawnCards[i]
                    FlippableCard(
                        cardId = c.cardId, isReversed = c.isReversed,
                        positionLabel = c.positionMeaning,
                        isRevealed = i in uiState.revealedCardIndices,
                        onReveal = { onReveal(i) }, context = context,
                        cardWidth = 70, cardHeight = 105
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Card drawable resource resolution
// ─────────────────────────────────────────────

private val majorNames = listOf(
    "major_the_fool", "major_the_magician", "major_the_high_priestess",
    "major_the_empress", "major_the_emperor", "major_the_hierophant",
    "major_the_lovers", "major_the_chariot", "major_strength",
    "major_the_hermit", "major_wheel_of_fortune", "major_justice",
    "major_the_hanged_man", "major_death", "major_temperance",
    "major_the_devil", "major_the_tower", "major_the_star",
    "major_the_moon", "major_the_sun", "major_judgement", "major_the_world"
)
private val ranks = listOf("ace","two","three","four","five","six","seven","eight","nine","ten","page","knight","queen","king")
private val suits = listOf("cups","pentacles","swords","wands")

private fun getCardDrawableResId(context: android.content.Context, cardId: Int): Int {
    val resName = when {
        cardId < 22 -> majorNames[cardId]
        else -> {
            val minorIndex = cardId - 22
            val suitIndex = minorIndex / 14
            val cardNumber = minorIndex % 14
            "minor_${ranks[cardNumber]}_of_${suits[suitIndex]}"
        }
    }
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.card_back
}
