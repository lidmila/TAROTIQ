package com.tarotiq.app.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.CardLibraryViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardDetailScreen(
    cardId: Int,
    onNavigateBack: () -> Unit,
    viewModel: CardLibraryViewModel = viewModel()
) {
    var card by remember { mutableStateOf<TarotCard?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val tabTitles = listOf(
        stringResource(R.string.library_love),
        stringResource(R.string.library_career),
        stringResource(R.string.library_finances),
        stringResource(R.string.library_feelings),
        stringResource(R.string.library_actions)
    )

    LaunchedEffect(cardId) {
        card = viewModel.getCardById(cardId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back button
            TopAppBar(
                title = {
                    Text(
                        text = card?.let { getCardDisplayName(it) } ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            card?.let { tarotCard ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Large card image
                    val imageResId = context.resources.getIdentifier(
                        tarotCard.imageRes, "drawable", context.packageName
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = tarotCard.nameKey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.6f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.6f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "\uD83C\uDCCF", fontSize = 64.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Card info section
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Card name
                            Text(
                                text = getCardDisplayName(tarotCard),
                                style = MaterialTheme.typography.headlineSmall,
                                color = GoldSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Info row: Arcana, Suit, Element
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoChip(
                                    label = if (tarotCard.arcana == "major") {
                                        stringResource(R.string.library_major_arcana)
                                    } else {
                                        stringResource(R.string.library_minor_arcana)
                                    },
                                    color = if (tarotCard.arcana == "major") GoldSecondary else MysticLight
                                )

                                tarotCard.suit?.let { suit ->
                                    InfoChip(
                                        label = getSuitName(suit),
                                        color = getSuitColor(suit)
                                    )
                                }

                                tarotCard.element?.let { element ->
                                    InfoChip(
                                        label = getElementDisplay(element),
                                        color = getElementColor(element)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upright keywords
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.library_upright_keywords),
                                style = MaterialTheme.typography.titleSmall,
                                color = CardUpright
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parseKeywords(tarotCard.uprightKeywords).forEach { keyword ->
                                    KeywordChip(text = keyword, color = CardUpright)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reversed keywords
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.library_reversed_keywords),
                                style = MaterialTheme.typography.titleSmall,
                                color = CardReversed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parseKeywords(tarotCard.reversedKeywords).forEach { keyword ->
                                    KeywordChip(text = keyword, color = CardReversed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category tabs
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ScrollableTabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = GoldSecondary,
                                edgePadding = 8.dp,
                                divider = {
                                    HorizontalDivider(color = GlassBorder)
                                },
                                indicator = {
                                    TabRowDefaults.SecondaryIndicator(
                                        color = GoldSecondary
                                    )
                                }
                            ) {
                                tabTitles.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (selectedTab == index) GoldSecondary else TextSecondary
                                            )
                                        }
                                    )
                                }
                            }

                            // Tab content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                val (uprightMeaning, reversedMeaning) = when (selectedTab) {
                                    0 -> tarotCard.loveMeaningUpright to tarotCard.loveMeaningReversed
                                    1 -> tarotCard.careerMeaningUpright to tarotCard.careerMeaningReversed
                                    2 -> tarotCard.financesMeaningUpright to tarotCard.financesMeaningReversed
                                    3 -> tarotCard.feelingsMeaningUpright to tarotCard.feelingsMeaningReversed
                                    4 -> tarotCard.actionsMeaningUpright to tarotCard.actionsMeaningReversed
                                    else -> "" to ""
                                }

                                // Upright meaning
                                Text(
                                    text = stringResource(R.string.reading_upright),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CardUpright
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uprightMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Reversed meaning
                                Text(
                                    text = stringResource(R.string.reading_reversed),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CardReversed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = reversedMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } ?: run {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MysticPrimary)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun KeywordChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun getCardDisplayName(card: TarotCard): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(
        "card_${card.nameKey}", "string", context.packageName
    )
    return if (resId != 0) stringResource(resId) else card.nameKey.replace("_", " ")
        .replaceFirstChar { it.uppercase() }
}

@Composable
private fun getSuitName(suit: String): String {
    return when (suit) {
        "cups" -> stringResource(R.string.library_cups)
        "pentacles" -> stringResource(R.string.library_pentacles)
        "swords" -> stringResource(R.string.library_swords)
        "wands" -> stringResource(R.string.library_wands)
        else -> suit
    }
}

private fun getSuitColor(suit: String): Color {
    return when (suit) {
        "cups" -> CrystalBlue
        "pentacles" -> SuccessColor
        "swords" -> TextSecondary
        "wands" -> CandleFlicker
        else -> TextSecondary
    }
}

private fun getElementDisplay(element: String): String {
    return when (element) {
        "water" -> "\uD83C\uDF0A Water"
        "earth" -> "\uD83C\uDF3F Earth"
        "air" -> "\uD83D\uDCA8 Air"
        "fire" -> "\uD83D\uDD25 Fire"
        else -> element
    }
}

private fun getElementColor(element: String): Color {
    return when (element) {
        "water" -> CrystalBlue
        "earth" -> SuccessColor
        "air" -> TextSecondary
        "fire" -> CandleFlicker
        else -> TextSecondary
    }
}

private fun parseKeywords(json: String): List<String> {
    return try {
        Gson().fromJson(json, Array<String>::class.java).toList()
    } catch (_: Exception) {
        json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }
}
