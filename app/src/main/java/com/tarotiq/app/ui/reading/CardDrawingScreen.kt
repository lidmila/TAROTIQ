package com.tarotiq.app.ui.reading

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.ReadingSpread
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch
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

    // Arc selection state — stored in ViewModel to survive rotation
    val selectedArcCards = uiState.selectedArcCards
    val requiredCards = uiState.spread.cardCount

    // Selection is complete when enough cards chosen — user must confirm with CTA
    val selectionComplete = selectedArcCards.size >= requiredCards && uiState.drawnCards.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

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
                                ReadingSpread.YEAR_AHEAD -> stringResource(R.string.spread_year_ahead)
                                ReadingSpread.SHADOW_SELF -> stringResource(R.string.spread_shadow_self)
                                ReadingSpread.CROSSROADS -> stringResource(R.string.spread_crossroads)
                                ReadingSpread.CHAKRA -> stringResource(R.string.spread_chakra)
                                ReadingSpread.TWIN_FLAME -> stringResource(R.string.spread_twin_flame)
                                ReadingSpread.MOON_CYCLE -> stringResource(R.string.spread_moon_cycle)
                                ReadingSpread.CAREER_COMPASS -> stringResource(R.string.spread_career_compass)
                                ReadingSpread.INNER_CHILD -> stringResource(R.string.spread_inner_child)
                                ReadingSpread.TREE_OF_LIFE -> stringResource(R.string.spread_tree_of_life)
                                ReadingSpread.WEEK_AHEAD -> stringResource(R.string.spread_week_ahead)
                                ReadingSpread.KARMIC -> stringResource(R.string.spread_karmic)
                                ReadingSpread.SELF_LOVE -> stringResource(R.string.spread_self_love)
                            },
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
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = if (allRevealed) 100.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val cardsDrawn = uiState.drawnCards.isNotEmpty()

                    // Phase-aware content: arc selection -> card reveal
                    AnimatedContent(
                        targetState = cardsDrawn,
                        transitionSpec = {
                            (fadeIn(tween(600)) + scaleIn(
                                initialScale = 0.95f,
                                animationSpec = tween(600)
                            )).togetherWith(
                                fadeOut(tween(400)) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(400)
                                )
                            )
                        },
                        label = "arc_to_reveal"
                    ) { drawn ->
                        if (!drawn) {
                            // --- ARC SELECTION PHASE ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = pluralStringResource(R.plurals.spread_pick_cards, requiredCards, requiredCards).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 2.sp
                                    ),
                                    color = CelestialGold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.drawing_focus_energy),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = ManropeFamily
                                    ),
                                    color = MoonSilver.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.drawing_scroll_hint),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MoonSilver.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                SelectionProgressDots(
                                    total = requiredCards,
                                    selected = selectedArcCards.size,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )

                                CardArcDisplay(
                                    totalCards = 78,
                                    selectedCards = selectedArcCards,
                                    maxSelectable = requiredCards,
                                    onCardTap = { index ->
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        val updated = if (index in selectedArcCards) {
                                            selectedArcCards - index
                                        } else if (selectedArcCards.size < requiredCards) {
                                            selectedArcCards + index
                                        } else {
                                            selectedArcCards
                                        }
                                        readingViewModel.updateSelectedArcCards(updated)
                                    }
                                )

                                // Confirm selection CTA
                                AnimatedVisibility(
                                    visible = selectionComplete,
                                    enter = fadeIn(tween(400)) + scaleIn(
                                        initialScale = 0.8f,
                                        animationSpec = tween(400, easing = EaseOutBack)
                                    )
                                ) {
                                    ArtNouveauButton(
                                        text = stringResource(R.string.drawing_confirm_selection),
                                        onClick = { readingViewModel.drawCards() },
                                        variant = ButtonVariant.PRIMARY,
                                        modifier = Modifier.padding(top = 20.dp)
                                    )
                                }
                            }
                        } else {
                            // --- CARD REVEAL PHASE ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Instruction text with crossfade
                                AnimatedContent(
                                    targetState = allRevealed,
                                    transitionSpec = {
                                        (fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 4 })
                                            .togetherWith(fadeOut(tween(300)))
                                    },
                                    label = "instruction_crossfade"
                                ) { revealed ->
                                    Text(
                                        text = if (revealed) stringResource(R.string.drawing_all_revealed).uppercase()
                                               else stringResource(R.string.drawing_tap_to_reveal).uppercase(),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 2.sp
                                        ),
                                        color = CelestialGold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )
                                }

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
                                    else -> {
                                        GenericGridLayout(
                                            uiState = uiState,
                                            onReveal = { index ->
                                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                readingViewModel.revealCard(index)
                                            },
                                            context = context
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Get Reading button — appears after all spread cards revealed
                AnimatedVisibility(
                    visible = allRevealed,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    enter = fadeIn(tween(800)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(600, easing = EaseOutBack)
                    )
                ) {
                    ArtNouveauButton(
                        text = stringResource(R.string.drawing_get_reading),
                        onClick = onGetReading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

// -----------------------------------------------
// Flippable card with Mystic Portal reveal animation
// -----------------------------------------------

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
    cardHeight: Int = 150,
    levitationIndex: Int = 0
) {
    // Card flip animation state
    var hasStartedReveal by remember { mutableStateOf(false) }

    // Pre-reveal animations: only create for unrevealed cards to avoid 78+ infinite transitions
    val preRevealPulse: Float
    val levitationOffset: Float
    if (!hasStartedReveal) {
        val infiniteTransition = rememberInfiniteTransition(label = "levitate_$levitationIndex")
        preRevealPulse = infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_$levitationIndex"
        ).value
        levitationOffset = infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2500 + levitationIndex * 400,
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_$levitationIndex"
        ).value
    } else {
        preRevealPulse = 1f
        levitationOffset = 0f
    }
    val flipRotation = remember { Animatable(0f) }
    LaunchedEffect(isRevealed) {
        if (isRevealed && !hasStartedReveal) {
            hasStartedReveal = true
            flipRotation.animateTo(180f, tween(800, easing = EaseInOutCubic))
        }
    }

    val cardResId = remember(cardId) {
        getCardDrawableResId(context, cardId)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = positionLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                fontSize = 9.sp
            ),
            color = MoonSilver,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Card container
        Box(
            modifier = Modifier
                .width(cardWidth.dp)
                .height(cardHeight.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = if (!hasStartedReveal) levitationOffset else 0f
                        rotationY = flipRotation.value
                        cameraDistance = 14f * density
                        scaleX = if (!hasStartedReveal) preRevealPulse else 1f
                        scaleY = if (!hasStartedReveal) preRevealPulse else 1f
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = AstralPurple.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        CelestialGold.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = !isRevealed) { onReveal() }
            ) {
                if (flipRotation.value <= 90f) {
                    Image(
                        painter = painterResource(R.drawable.card_back),
                        contentDescription = stringResource(R.string.drawing_tap_to_reveal),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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

        }

        // Upright/Reversed label (only after reveal completes)
        if (isRevealed && flipRotation.value >= 170f) {
            Text(
                text = (if (isReversed) stringResource(R.string.reading_reversed)
                       else stringResource(R.string.reading_upright)).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    fontSize = 9.sp
                ),
                color = if (isReversed) CardReversed else CardUpright,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// -----------------------------------------------
// Layout variants per spread type
// -----------------------------------------------

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
                cardHeight = 150,
                levitationIndex = index
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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

@Composable
private fun GenericGridLayout(
    uiState: ReadingUiState,
    onReveal: (Int) -> Unit,
    context: android.content.Context
) {
    val columns = if (uiState.drawnCards.size <= 8) 3 else 4
    val cardW = if (columns == 3) 80 else 70
    val cardH = if (columns == 3) 120 else 105
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.drawnCards.chunked(columns).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { colIdx, c ->
                    val i = rowIdx * columns + colIdx
                    FlippableCard(
                        cardId = c.cardId, isReversed = c.isReversed,
                        positionLabel = c.positionMeaning,
                        isRevealed = i in uiState.revealedCardIndices,
                        onReveal = { onReveal(i) }, context = context,
                        cardWidth = cardW, cardHeight = cardH
                    )
                }
            }
        }
    }
}

// -----------------------------------------------
// Card drawable resource resolution
// -----------------------------------------------

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

// -----------------------------------------------
// Card grid display -- grid of face-down cards
// -----------------------------------------------

@Composable
@Suppress("UNUSED_PARAMETER")
private fun CardArcDisplay(
    totalCards: Int,
    selectedCards: Set<Int>,
    maxSelectable: Int,
    onCardTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth().height(480.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(totalCards) { i ->
            val isSelected = i in selectedCards
            val cardScale by animateFloatAsState(
                targetValue = if (isSelected) 1.08f else 1f,
                animationSpec = tween(200), label = "grid_scale_$i"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(2f / 3f)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .shadow(
                        elevation = if (isSelected) 12.dp else 2.dp,
                        shape = RoundedCornerShape(6.dp),
                        ambientColor = if (isSelected) CelestialGold.copy(alpha = 0.3f) else Color.Transparent
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) CelestialGold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onCardTap(i) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.card_back),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Selected overlay: dark scrim + gold check star
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VoidBlack.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        SymbolIcon(
                            symbol = Symbol.STAR,
                            size = 28.dp,
                            color = CelestialGold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------
// Selection progress dots
// -----------------------------------------------

@Composable
private fun SelectionProgressDots(
    total: Int,
    selected: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive = index < selected
            Box(
                modifier = Modifier
                    .size(if (isActive) 10.dp else 8.dp)
                    .then(
                        if (isActive) Modifier.shadow(
                            8.dp,
                            CircleShape,
                            ambientColor = CelestialGold.copy(alpha = 0.4f)
                        )
                        else Modifier
                    )
                    .background(
                        color = if (isActive) CelestialGold else SurfaceContainerHighest,
                        shape = CircleShape
                    )
            )
        }
    }
}
