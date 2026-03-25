package com.tarotiq.app.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.CardMeaningTranslator
import com.tarotiq.app.utils.LocaleUtils
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
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back button
            TopAppBar(
                title = {
                    Text(
                        text = card?.let { getCardDisplayName(it) } ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CelestialGold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = AstralPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicDeep.copy(alpha = 0.85f)
                )
            )

            card?.let { tarotCard ->
                // ── Ritual card reveal animation ──
                var cardRevealed by remember { mutableStateOf(false) }
                LaunchedEffect(tarotCard) {
                    delay(400) // brief pause before reveal
                    cardRevealed = true
                }

                val flipRotation by animateFloatAsState(
                    targetValue = if (cardRevealed) 180f else 0f,
                    animationSpec = tween(1200, easing = EaseInOutCubic),
                    label = "library_card_flip"
                )

                // Glow peaks at 90deg (moment of reveal)
                val flipProgress = flipRotation / 180f
                val glowIntensity = 1f - kotlin.math.abs(flipProgress * 2f - 1f)

                // Levitation while unrevealed
                val cardTransition = rememberInfiniteTransition(label = "card_float")
                val cardFloat by cardTransition.animateFloat(
                    initialValue = -6f, targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
                    ),
                    label = "float"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Large card image with flip reveal
                    val imageResId = context.resources.getIdentifier(
                        tarotCard.imageRes, "drawable", context.packageName
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.6f)
                                .graphicsLayer {
                                    rotationY = flipRotation
                                    cameraDistance = 14f * density
                                    if (!cardRevealed) translationY = cardFloat
                                }
                                .shadow(
                                    elevation = (8f + glowIntensity * 20f).dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = CelestialGold.copy(alpha = glowIntensity * 0.4f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            if (flipRotation <= 90f) {
                                // Card back (mystical reveal)
                                Image(
                                    painter = painterResource(R.drawable.card_back),
                                    contentDescription = "Card back",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Card front (revealed)
                                if (imageResId != 0) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = tarotCard.nameKey,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { rotationY = 180f },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(CosmicMid)
                                            .graphicsLayer { rotationY = 180f },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SymbolIcon(
                                            symbol = Symbol.TAROT_CARD,
                                            size = 64.dp,
                                            color = CelestialGold
                                        )
                                    }
                                }
                            }
                        }

                        // Gold glow burst during flip
                        if (glowIntensity > 0.05f) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.6f)
                            ) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            CelestialGold.copy(alpha = glowIntensity * 0.3f),
                                            CelestialGold.copy(alpha = glowIntensity * 0.1f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        radius = size.minDimension * 0.7f
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.minDimension * 0.7f
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Card info section
                    ArtNouveauFrame(
                        frameStyle = FrameStyle.ORNATE,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Card name — NewsreaderFamily
                            Text(
                                text = getCardDisplayName(tarotCard),
                                style = MaterialTheme.typography.headlineMedium,
                                color = CelestialGold,
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
                                    color = if (tarotCard.arcana == "major") CelestialGold else AstralPurpleLight
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
                    ArtNouveauFrame(
                        frameStyle = FrameStyle.SIMPLE,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.library_upright_keywords).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily
                                ),
                                color = CardUpright
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parseKeywords(tarotCard.uprightKeywords).forEach { keyword ->
                                    KeywordChip(text = translateKeyword(keyword), color = CardUpright)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reversed keywords
                    ArtNouveauFrame(
                        frameStyle = FrameStyle.SIMPLE,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.library_reversed_keywords).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily
                                ),
                                color = CardReversed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parseKeywords(tarotCard.reversedKeywords).forEach { keyword ->
                                    KeywordChip(text = translateKeyword(keyword), color = CardReversed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category tabs
                    ArtNouveauFrame(
                        frameStyle = FrameStyle.ORNATE,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ScrollableTabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = CelestialGold,
                                edgePadding = 8.dp,
                                divider = {
                                    HorizontalDivider(color = GlassBorder)
                                },
                                indicator = { tabPositions ->
                                    if (selectedTab < tabPositions.size) {
                                        Box(
                                            Modifier
                                                .tabIndicatorOffset(tabPositions[selectedTab])
                                                .height(3.dp)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            CelestialGold.copy(alpha = 0.3f),
                                                            CelestialGold,
                                                            CelestialGoldLight,
                                                            CelestialGold,
                                                            CelestialGold.copy(alpha = 0.3f)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }
                            ) {
                                tabTitles.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = {
                                            Text(
                                                text = title.uppercase(),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = SpaceGroteskFamily,
                                                    letterSpacing = 1.5.sp
                                                ),
                                                color = if (selectedTab == index) CelestialGold else MoonSilver
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
                                    0 -> CardMeaningTranslator.translate(context, tarotCard.id, "love_up", tarotCard.loveMeaningUpright) to
                                         CardMeaningTranslator.translate(context, tarotCard.id, "love_rev", tarotCard.loveMeaningReversed)
                                    1 -> CardMeaningTranslator.translate(context, tarotCard.id, "career_up", tarotCard.careerMeaningUpright) to
                                         CardMeaningTranslator.translate(context, tarotCard.id, "career_rev", tarotCard.careerMeaningReversed)
                                    2 -> CardMeaningTranslator.translate(context, tarotCard.id, "finances_up", tarotCard.financesMeaningUpright) to
                                         CardMeaningTranslator.translate(context, tarotCard.id, "finances_rev", tarotCard.financesMeaningReversed)
                                    3 -> CardMeaningTranslator.translate(context, tarotCard.id, "feelings_up", tarotCard.feelingsMeaningUpright) to
                                         CardMeaningTranslator.translate(context, tarotCard.id, "feelings_rev", tarotCard.feelingsMeaningReversed)
                                    4 -> CardMeaningTranslator.translate(context, tarotCard.id, "actions_up", tarotCard.actionsMeaningUpright) to
                                         CardMeaningTranslator.translate(context, tarotCard.id, "actions_rev", tarotCard.actionsMeaningReversed)
                                    else -> "" to ""
                                }

                                // Upright meaning
                                Text(
                                    text = stringResource(R.string.reading_upright).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily
                                    ),
                                    color = CardUpright
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uprightMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarWhite,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Reversed meaning
                                Text(
                                    text = stringResource(R.string.reading_reversed).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily
                                    ),
                                    color = CardReversed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = reversedMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarWhite,
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
                    CircularProgressIndicator(color = AstralPurple)
                }
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
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = SpaceGroteskFamily,
                letterSpacing = 1.5.sp
            ),
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = SpaceGroteskFamily
            ),
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
        "cups" -> InfoSky
        "pentacles" -> SuccessEmerald
        "swords" -> MoonSilver
        "wands" -> CelestialGoldLight
        else -> MoonSilver
    }
}

@Composable
private fun getElementDisplay(element: String): String {
    return when (element) {
        "water" -> stringResource(R.string.element_water)
        "earth" -> stringResource(R.string.element_earth)
        "air" -> stringResource(R.string.element_air)
        "fire" -> stringResource(R.string.element_fire)
        else -> element
    }
}

private fun getElementColor(element: String): Color {
    return when (element) {
        "water" -> InfoSky
        "earth" -> SuccessEmerald
        "air" -> MoonSilver
        "fire" -> CelestialGoldLight
        else -> MoonSilver
    }
}

@Composable
private fun translateKeyword(keyword: String): String {
    val locale = LocaleUtils.getCurrentLanguage(LocalContext.current)
    if (locale == "en") return keyword

    val csMap = mapOf(
        "beginnings" to "za\u010D\u00E1tky", "innocence" to "nevinnost", "spontaneity" to "spont\u00E1nnost",
        "free spirit" to "voln\u00FD duch", "recklessness" to "lehkomyslnost", "risk-taking" to "riskov\u00E1n\u00ED",
        "naivety" to "naivita", "foolishness" to "po\u0161etilost",
        "willpower" to "v\u016Fle", "desire" to "touha", "creation" to "tvo\u0159en\u00ED", "manifestation" to "manifestace",
        "trickery" to "podvod", "illusions" to "iluze", "manipulation" to "manipulace",
        "intuition" to "intuice", "sacred knowledge" to "posv\u00E1tn\u00E9 v\u011Bd\u011Bn\u00ED", "divine feminine" to "bo\u017Esk\u00E9 \u017Eenstv\u00ED",
        "subconscious" to "podv\u011Bdom\u00ED", "secrets" to "tajemstv\u00ED", "withdrawal" to "sta\u017Een\u00ED se",
        "silence" to "ticho",
        "femininity" to "\u017Eenskost", "beauty" to "kr\u00E1sa", "nature" to "p\u0159\u00EDroda", "nurturing" to "p\u00E9\u010De",
        "abundance" to "hojnost", "creative block" to "tv\u016Fr\u010D\u00ED blok", "dependence" to "z\u00E1vislost",
        "authority" to "autorita", "structure" to "struktura", "leadership" to "veden\u00ED",
        "father figure" to "otcovsk\u00E1 postava", "domination" to "dominance", "rigidity" to "rigidita",
        "tradition" to "tradice", "conformity" to "konformita", "morality" to "mor\u00E1lka",
        "spiritual wisdom" to "duchovn\u00ED moudrost", "rebellion" to "rebelie", "subversiveness" to "podvratnost",
        "love" to "l\u00E1ska", "harmony" to "harmonie", "relationships" to "vztahy", "choices" to "volby",
        "values alignment" to "soulad hodnot", "self-love" to "sebel\u00E1ska", "disharmony" to "disharmonie",
        "imbalance" to "nerovnov\u00E1ha",
        "determination" to "odhodl\u00E1n\u00ED", "control" to "kontrola",
        "direction" to "sm\u011Br", "success" to "\u00FAsp\u011Bch", "aggression" to "agrese",
        "lack of direction" to "ztr\u00E1ta sm\u011Bru",
        "strength" to "s\u00EDla", "courage" to "odvaha", "patience" to "trp\u011Blivost",
        "compassion" to "soucit", "inner strength" to "vnit\u0159n\u00ED s\u00EDla",
        "self-doubt" to "pochybnosti o sob\u011B", "weakness" to "slabost", "insecurity" to "nejistota",
        "soul-searching" to "hled\u00E1n\u00ED sebe", "solitude" to "samota", "inner guidance" to "vnit\u0159n\u00ED veden\u00ED",
        "introspection" to "introspekce", "isolation" to "izolace", "loneliness" to "osam\u011Blost",
        "good luck" to "\u0161t\u011Bst\u00ED", "karma" to "karma", "destiny" to "osud", "turning point" to "zlomov\u00FD bod",
        "life cycles" to "\u017Eivotn\u00ED cykly", "bad luck" to "sm\u016Fla", "resistance to change" to "odpor ke zm\u011Bn\u011B",
        "justice" to "spravedlnost", "fairness" to "f\u00E9rovost", "truth" to "pravda", "law" to "z\u00E1kon",
        "cause and effect" to "p\u0159\u00ED\u010Dina a n\u00E1sledek", "unfairness" to "nespravedlnost", "dishonesty" to "nepoctivost",
        "sacrifice" to "ob\u011B\u0165", "release" to "uvoln\u011Bn\u00ED", "martyrdom" to "mu\u010Dednietv\u00ED",
        "new perspective" to "nov\u00FD pohled", "suspension" to "pozastaven\u00ED", "restriction" to "omezen\u00ED",
        "stalling" to "stagnace",
        "endings" to "konce", "change" to "zm\u011Bna", "transformation" to "transformace",
        "transition" to "p\u0159echod", "fear of change" to "strach ze zm\u011Bny",
        "balance" to "rovnov\u00E1ha", "moderation" to "um\u00ED\u0159n\u011Bnost", "purpose" to "\u00FA\u010Del",
        "meaning" to "smysl", "excess" to "p\u0159ebytek",
        "self-healing" to "sebel\u00E9\u010Den\u00ED",
        "shadow self" to "st\u00EDnov\u00E9 j\u00E1", "attachment" to "p\u0159ipoutanost", "addiction" to "z\u00E1vislost",
        "sexuality" to "sexualita", "detachment" to "odpout\u00E1n\u00ED",
        "breaking free" to "osvobozen\u00ED", "reclaiming power" to "znovuz\u00EDsk\u00E1n\u00ED moci",
        "sudden change" to "n\u00E1hl\u00E1 zm\u011Bna", "upheaval" to "p\u0159evrat", "chaos" to "chaos",
        "revelation" to "odhalen\u00ED", "awakening" to "probuzen\u00ED", "disaster averted" to "odvr\u00E1cen\u00E1 katastrofa",
        "personal transformation" to "osobn\u00ED transformace",
        "hope" to "nad\u011Bje", "faith" to "v\u00EDra", "renewal" to "obnova", "serenity" to "klid",
        "inspiration" to "inspirace", "lack of faith" to "nedostatek v\u00EDry", "despair" to "zouf\u00E1lstv\u00ED",
        "disconnection" to "odpojen\u00ED",
        "illusion" to "iluze", "fear" to "strach", "anxiety" to "\u00FAzkost",
        "confusion" to "zmatek", "deception" to "podvod",
        "releasing fear" to "uvoln\u011Bn\u00ED strachu",
        "positivity" to "pozitivita", "warmth" to "v\u0159elost", "joy" to "radost",
        "vitality" to "vitalita", "fun" to "z\u00E1bava", "sadness" to "smutek",
        "inner child" to "vnit\u0159n\u00ED d\u00EDt\u011B", "feeling down" to "skl\u00ED\u010Denost",
        "judgement" to "soud", "rebirth" to "znovuzrozen\u00ED", "inner calling" to "vnit\u0159n\u00ED vol\u00E1n\u00ED",
        "absolution" to "rozh\u0159e\u0161en\u00ED",
        "completion" to "dokon\u010Den\u00ED", "integration" to "integrace", "accomplishment" to "\u00FAsp\u011Bch",
        "travel" to "cestov\u00E1n\u00ED", "unity" to "jednota", "incompletion" to "ne\u00FAplnost",
        "lack of closure" to "nedokon\u010Denost",
        "new beginnings" to "nov\u00E9 za\u010D\u00E1tky", "opportunity" to "p\u0159\u00EDle\u017Eitost", "potential" to "potenci\u00E1l",
        "missed chance" to "promar\u0148\u011Bn\u00E1 \u0161ance",
        "partnership" to "partnerstv\u00ED", "duality" to "dualita", "union" to "spojen\u00ED",
        "decision" to "rozhodnut\u00ED", "indecision" to "nerozhodnost",
        "creativity" to "kreativita", "growth" to "r\u016Fst", "progress" to "pokrok",
        "teamwork" to "t\u00FDmov\u00E1 pr\u00E1ce", "collaboration" to "spolupr\u00E1ce", "delays" to "zpo\u017Ed\u011Bn\u00ED",
        "frustration" to "frustrace", "lack of progress" to "nedostatek pokroku",
        "celebration" to "oslavy", "community" to "komunita", "stability" to "stabilita",
        "foundations" to "z\u00E1klady", "home" to "domov", "lack of harmony" to "nedostatek harmonie",
        "conflict" to "konflikt", "loss" to "ztr\u00E1ta", "grief" to "smutek",
        "regret" to "l\u00EDtost", "disappointment" to "zklam\u00E1n\u00ED", "acceptance" to "p\u0159ijet\u00ED",
        "moving on" to "posunut\u00ED se", "recovery" to "zotaven\u00ED",
        "generosity" to "\u0161t\u011Bdrost", "charity" to "charita", "prosperity" to "prosperita",
        "sharing" to "sd\u00EDlen\u00ED", "greed" to "chamtivost", "selfishness" to "sobeckost",
        "contemplation" to "kontemplace", "reevaluation" to "p\u0159ehodnocen\u00ED",
        "assessment" to "hodnocen\u00ED", "reflection" to "reflexe", "dissatisfaction" to "nespokojenost",
        "stagnation" to "stagnace",
        "skill" to "dovednost", "craftsmanship" to "\u0159emeslo", "dedication" to "oddanost",
        "quality" to "kvalita", "mastery" to "mistrovstv\u00ED", "mediocrity" to "pr\u016Fm\u011Brnost",
        "lack of commitment" to "nedostatek z\u00E1vazku",
        "achievement" to "\u00FAsp\u011Bch", "luxury" to "luxus", "self-sufficiency" to "sob\u011Bsta\u010Dnost",
        "financial security" to "finan\u010Dn\u00ED jistota", "material success" to "materi\u00E1ln\u00ED \u00FAsp\u011Bch",
        "overindulgence" to "p\u0159ehnan\u00FD po\u017Eitek", "hustling" to "honba",
        "legacy" to "odkaz", "inheritance" to "d\u011Bdictv\u00ED", "wealth" to "bohatstv\u00ED",
        "family" to "rodina", "financial failure" to "finan\u010Dn\u00ED ne\u00FAsp\u011Bch",
        "page" to "p\u00E1\u017Ee", "knight" to "ryt\u00ED\u0159", "queen" to "kr\u00E1lovna", "king" to "kr\u00E1l",
        "curiosity" to "zv\u011Bdavost", "adventure" to "dobrodru\u017Estv\u00ED", "ambition" to "ambice",
        "action" to "akce", "passion" to "v\u00E1\u0161e\u0148", "energy" to "energie",
        "enthusiasm" to "nad\u0161en\u00ED", "expansion" to "expanze", "exploration" to "pr\u016Fzkum",
        "foresight" to "p\u0159edv\u00EDdavost", "enterprise" to "podnikavost",
        "confidence" to "sebev\u011Bdom\u00ED", "bravery" to "state\u010Dnost",
        "vision" to "vize", "business sense" to "obchodn\u00ED smysl",
        "emotional" to "emocion\u00E1ln\u00ED", "romantic" to "romantick\u00FD",
        "healing" to "l\u00E9\u010Den\u00ED",
        "intellectual" to "intelektu\u00E1ln\u00ED", "analytical" to "analytick\u00FD",
        "honest" to "up\u0159\u00EDmn\u00FD", "direct" to "p\u0159\u00EDm\u00FD",
        "practical" to "praktick\u00FD", "reliable" to "spolehliv\u00FD",
        "generous" to "velkorys\u00FD", "provider" to "\u017Eivitel",
        "power" to "moc", "influence" to "vliv",
        "emotional maturity" to "emo\u010Dn\u00ED zralost",
        "clarity" to "jasnost", "intellect" to "intelekt",
        "mental clarity" to "ment\u00E1ln\u00ED jasnost", "communication" to "komunikace",
        "heartbreak" to "zlomen\u00E9 srdce", "betrayal" to "zrada",
        "burden" to "b\u0159emeno", "overwhelm" to "zahlcen\u00ED",
        "defeat" to "por\u00E1\u017Eka", "surrender" to "kapitulace",
        "movement" to "pohyb",
        "planning" to "pl\u00E1nov\u00E1n\u00ED", "preparation" to "p\u0159\u00EDprava",
        "defense" to "obrana", "boundaries" to "hranice",
        "strategy" to "strategie",
        "truce" to "p\u0159\u00EDm\u011B\u0159\u00ED", "compromise" to "kompromis"
    )

    return if (locale == "cs") csMap[keyword.lowercase()] ?: keyword else keyword
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
