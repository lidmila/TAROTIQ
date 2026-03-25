package com.tarotiq.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.DrawnCard
import com.tarotiq.app.domain.model.TarotReading
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.CardNameTranslator
import com.tarotiq.app.viewmodel.ReadingHistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Card name / image resolution (mirrors CardDrawingScreen) ──

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


private val romanNumerals = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")

// ── Parse JSON into DrawnCard list ──

private fun parseDrawnCards(json: String): List<DrawnCard> {
    return try {
        val type = object : TypeToken<List<DrawnCard>>() {}.type
        Gson().fromJson(json, type)
    } catch (_: Exception) {
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDetailScreen(
    readingId: String,
    onBack: () -> Unit,
    historyViewModel: ReadingHistoryViewModel = viewModel()
) {
    var reading by remember { mutableStateOf<TarotReading?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    LaunchedEffect(readingId) {
        reading = historyViewModel.getReadingById(readingId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            ShimmerText(
                                text = stringResource(R.string.history_grimoire_title),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = NewsreaderFamily,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = AstralPurple
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CosmicDeep.copy(alpha = 0.85f)
                        )
                    )
                }
            ) { padding ->
                reading?.let { r ->
                    val drawnCards = remember(r.drawnCardsJson) {
                        parseDrawnCards(r.drawnCardsJson)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Card Display Section with nebula backdrop ──
                        CardDisplaySection(
                            drawnCards = drawnCards,
                            context = context
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Question / Meta info ──
                        if (r.question != null || r.topic.isNotEmpty()) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = dateFormat.format(Date(r.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MoonSilver.copy(alpha = 0.6f)
                                )
                                r.question?.let { q ->
                                    Text(
                                        text = q,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NewsreaderFamily,
                                            fontWeight = FontWeight.Light
                                        ),
                                        color = StarWhite.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                val spreadLabel = when (r.spreadType) {
                                    "single" -> stringResource(R.string.spread_single)
                                    "three_card" -> stringResource(R.string.spread_three_card)
                                    "celtic_cross" -> stringResource(R.string.spread_celtic_cross)
                                    "relationship" -> stringResource(R.string.spread_relationship)
                                    else -> r.spreadType.replace("_", " ").replaceFirstChar { it.uppercase() }
                                }
                                Text(
                                    text = "$spreadLabel \u00B7 ${stringResource(R.string.coins_count, r.coinsCost)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MoonSilver.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // ── Per-card interpretation sections ──
                        if (drawnCards.isNotEmpty() && drawnCards.any { it.cardInterpretation != null }) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                drawnCards.forEachIndexed { index, card ->
                                    InterpretationSection(
                                        card = card,
                                        index = index,
                                        isCenterCard = drawnCards.size > 1 && index == drawnCards.size / 2
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // ── Full AI Interpretation (synthesis / overall) ──
                        if (r.aiInterpretation.isNotBlank()) {
                            OverallInterpretationSection(
                                interpretation = r.aiInterpretation,
                                hasSeparateCards = drawnCards.any { it.cardInterpretation != null }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Action buttons ──
                        ActionButtonsRow(
                            onDelete = { showDeleteDialog = true }
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // ── Delete confirmation dialog ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceContainerCA,
            titleContentColor = StarWhite,
            textContentColor = MoonSilver,
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    reading?.let { r ->
                        scope.launch { historyViewModel.deleteReading(r); onBack() }
                    }
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete), color = ErrorCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MoonSilver)
                }
            }
        )
    }
}

// ════════════════════════════════════════════════
// Card Display Section — horizontal row with nebula gradient
// ════════════════════════════════════════════════

@Composable
private fun CardDisplaySection(
    drawnCards: List<DrawnCard>,
    context: android.content.Context
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Nebula gradient backdrop
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NebulaGradientStart.copy(alpha = 0.25f),
                                CosmicDeep.copy(alpha = 0.0f)
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            radius = size.width * 0.7f
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx())
                    )
                }
        )

        // Card row
        if (drawnCards.isEmpty()) {
            // Fallback for readings without parsed cards
            Spacer(modifier = Modifier.height(48.dp))
        } else if (drawnCards.size == 1) {
            // Single card — centered, larger
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                TarotCardImage(
                    cardId = drawnCards[0].cardId,
                    isReversed = drawnCards[0].isReversed,
                    context = context,
                    isCenter = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = drawnCards[0].positionMeaning.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = CelestialGold
                )
            }
        } else {
            val spreadCards = drawnCards.filter { it.position != "extra" }
            val extraCards = drawnCards.filter { it.position == "extra" }

            // Split spread cards into rows of max 5
            val maxPerRow = 5
            val rows = spreadCards.chunked(maxPerRow)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowCards ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowCards.forEach { card ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                TarotCardImage(
                                    cardId = card.cardId,
                                    isReversed = card.isReversed,
                                    context = context,
                                    isCenter = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = card.positionMeaning.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                        fontSize = 9.sp
                                    ),
                                    color = CelestialGold.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Extra cards row
                if (extraCards.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        extraCards.forEach { card ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                TarotCardImage(
                                    cardId = card.cardId,
                                    isReversed = card.isReversed,
                                    context = context,
                                    isCenter = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = card.positionMeaning.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                        fontSize = 9.sp
                                    ),
                                    color = AstralPurple.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
// Individual tarot card image with gold glow
// ════════════════════════════════════════════════

@Composable
private fun TarotCardImage(
    cardId: Int,
    isReversed: Boolean,
    context: android.content.Context,
    isCenter: Boolean
) {
    val resId = remember(cardId) { getCardDrawableResId(context, cardId) }
    val cardShape = RoundedCornerShape(12.dp)

    // Sizes: center card ~192x288dp, side cards ~160x256dp
    val width = if (isCenter) 144.dp else 112.dp
    val height = if (isCenter) 216.dp else 168.dp

    // Gold glow intensity differs for center vs side
    val glowAlpha = if (isCenter) 0.20f else 0.08f
    val borderAlpha = if (isCenter) 0.35f else 0.12f

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = if (isCenter) 24.dp else 12.dp,
                shape = cardShape,
                ambientColor = CelestialGold.copy(alpha = glowAlpha),
                spotColor = CelestialGold.copy(alpha = glowAlpha)
            )
            .clip(cardShape)
            .background(CosmicMid)
            .border(
                width = 1.dp,
                color = CelestialGold.copy(alpha = borderAlpha),
                shape = cardShape
            )
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = CardNameTranslator.getDisplayName(context, cardId),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = if (isReversed) 180f else 0f
                }
        )
    }
}

// ════════════════════════════════════════════════
// Per-card interpretation section
// ════════════════════════════════════════════════

@Composable
private fun InterpretationSection(
    card: DrawnCard,
    index: Int,
    isCenterCard: Boolean
) {
    val interpretation = card.cardInterpretation ?: return
    val context = LocalContext.current
    val cardName = CardNameTranslator.getDisplayName(context, card.cardId)
    val positionLabel = card.positionMeaning
    val romanNumeral = romanNumerals.getOrElse(index) { "${index + 1}" }

    // Left accent border opacity: center card is brighter
    val accentAlpha = if (isCenterCard) 1.0f else 0.3f

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AstralPurple.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .drawBehind {
                // Left gold accent bar (2dp wide)
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            CelestialGold.copy(alpha = 0.20f * accentAlpha),
                            CelestialGold.copy(alpha = 0.05f)
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(
                        width = 2.dp.toPx(),
                        height = size.height
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Roman numeral (decorative, faint)
            Text(
                text = romanNumeral,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Light
                ),
                color = CelestialGold.copy(alpha = 0.10f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Position label row with divider line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = positionLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 10.sp
                        ),
                        color = CelestialGold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(OutlineColor.copy(alpha = 0.15f))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card name as headline
                Text(
                    text = "$cardName",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 22.sp
                    ),
                    color = AstralPurple
                )

                // Reversed indicator
                if (card.isReversed) {
                    Text(
                        text = "(Reversed)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily
                        ),
                        color = CelestialGold.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interpretation body text — parse **bold** markdown
                val cardAnnotated = remember(interpretation) {
                    val raw = interpretation
                    androidx.compose.ui.text.buildAnnotatedString {
                        var i = 0
                        while (i < raw.length) {
                            val bs = raw.indexOf("**", i)
                            if (bs == -1) { append(raw.substring(i)); break }
                            append(raw.substring(i, bs))
                            val be = raw.indexOf("**", bs + 2)
                            if (be == -1) { append(raw.substring(bs)); break }
                            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = CelestialGold))
                            append(raw.substring(bs + 2, be))
                            pop()
                            i = be + 2
                        }
                    }
                }
                Text(
                    text = cardAnnotated,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Light,
                        lineHeight = 26.sp
                    ),
                    color = StarWhite.copy(alpha = 0.80f)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
// Overall AI interpretation / synthesis
// ════════════════════════════════════════════════

@Composable
private fun OverallInterpretationSection(
    interpretation: String,
    hasSeparateCards: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (hasSeparateCards) {
            // Show a "Cosmic Synthesis" label if we already showed per-card sections
            OrnamentalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = CelestialGold.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AstralPurple.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                .drawBehind {
                    // Left accent — gold for synthesis
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                CelestialGold.copy(alpha = 0.20f),
                                CelestialGold.copy(alpha = 0.05f)
                            )
                        ),
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(
                            width = 2.dp.toPx(),
                            height = size.height
                        )
                    )
                }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (hasSeparateCards) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.reading_cosmic_synthesis),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 10.sp
                            ),
                            color = AstralPurple.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(OutlineColor.copy(alpha = 0.15f))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val annotated = remember(interpretation) {
                    val raw = interpretation
                    androidx.compose.ui.text.buildAnnotatedString {
                        var i = 0
                        while (i < raw.length) {
                            val bs = raw.indexOf("**", i)
                            if (bs == -1) { append(raw.substring(i)); break }
                            append(raw.substring(i, bs))
                            val be = raw.indexOf("**", bs + 2)
                            if (be == -1) { append(raw.substring(bs)); break }
                            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = CelestialGold))
                            append(raw.substring(bs + 2, be))
                            pop()
                            i = be + 2
                        }
                    }
                }
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Light,
                        lineHeight = 26.sp
                    ),
                    color = StarWhite.copy(alpha = 0.80f)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
// Action buttons — Delete and Share
// ════════════════════════════════════════════════

@Composable
private fun ActionButtonsRow(
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        // Delete button (secondary / outline style)
        ArtNouveauButton(
            text = stringResource(R.string.delete),
            onClick = onDelete,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.weight(1f)
        )

        // Share button (primary style with breathing animation)
        ArtNouveauButton(
            text = stringResource(R.string.share),
            onClick = { /* Share functionality placeholder */ },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.weight(1f).breatheAnimation()
        )
    }
}
