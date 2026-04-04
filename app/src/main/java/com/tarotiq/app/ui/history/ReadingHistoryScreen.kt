package com.tarotiq.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotReading
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(
    onReadingClick: (String) -> Unit,
    historyViewModel: ReadingHistoryViewModel = viewModel()
) {
    val readings by historyViewModel.readings.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf(
        "all" to stringResource(R.string.history_filter_all).uppercase(),
        "love" to stringResource(R.string.topic_love).uppercase(),
        "career" to stringResource(R.string.topic_career).uppercase(),
        "general" to stringResource(R.string.topic_general).uppercase(),
        "yes_no" to stringResource(R.string.topic_yes_no).uppercase(),
        "spiritual" to stringResource(R.string.topic_spiritual).uppercase()
    )

    val filteredReadings = readings
        .let { list -> if (selectedFilter == "all") list else list.filter { it.topic == selectedFilter } }
        .let { list ->
            if (searchQuery.isBlank()) list
            else list.filter { reading ->
                reading.topic.contains(searchQuery, ignoreCase = true) ||
                        (reading.question ?: "").contains(searchQuery, ignoreCase = true) ||
                        reading.aiInterpretation.contains(searchQuery, ignoreCase = true) ||
                        reading.spreadType.contains(searchQuery, ignoreCase = true)
            }
        }

    val fullDateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    // Group readings into timeline phases (by month-year)
    val groupedReadings = remember(filteredReadings) {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        filteredReadings.groupBy { reading ->
            monthYearFormat.format(Date(reading.timestamp))
        }
    }

    // Phase labels for timeline groups
    val phaseNames = listOf(
        stringResource(R.string.moon_waxing_gibbous),
        stringResource(R.string.moon_waning_crescent),
        stringResource(R.string.moon_waxing_crescent),
        stringResource(R.string.moon_waning_gibbous),
        stringResource(R.string.moon_last_quarter),
        stringResource(R.string.moon_first_quarter)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
            Scaffold(containerColor = Color.Transparent) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // ============================================
                    // HEADER: "Sacred Archive" + "The Grimoire"
                    // ============================================
                    item(key = "header") {
                        GrimoireHeader()
                    }

                    // ============================================
                    // SEARCH BAR
                    // ============================================
                    item(key = "search") {
                        GrimoireSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                    }

                    // ============================================
                    // FILTER CHIPS
                    // ============================================
                    item(key = "filters") {
                        Spacer(modifier = Modifier.height(16.dp))
                        GrimoireFilterChips(
                            filters = filters,
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // ============================================
                    // CONTENT: Empty state or timeline
                    // ============================================
                    if (filteredReadings.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    SymbolIcon(
                                        symbol = Symbol.MOON_NEW,
                                        color = DimSilver.copy(alpha = 0.4f),
                                        size = 48.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        stringResource(R.string.history_empty),
                                        color = DimSilver,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NewsreaderFamily,
                                            fontStyle = FontStyle.Italic
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Timeline entries grouped by phase
                        var phaseIndex = 0
                        groupedReadings.forEach { (monthYear, readingsInGroup) ->
                            // Phase group header
                            val phaseName = phaseNames[phaseIndex % phaseNames.size]
                            item(key = "phase_$monthYear") {
                                TimelinePhaseHeader(
                                    phaseName = phaseName,
                                    monthYear = monthYear
                                )
                            }
                            phaseIndex++

                            // Timeline entry cards
                            items(
                                readingsInGroup,
                                key = { reading -> reading.id }
                            ) { reading ->
                                TimelineEntryCard(
                                    reading = reading,
                                    fullDateFormat = fullDateFormat,
                                    onClick = { onReadingClick(reading.id) }
                                )
                            }
                        }

                        // "Deepen Search" load-more button
                        if (filteredReadings.size >= 10) {
                            item(key = "load_more") {
                                DeepenSearchButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================
// Grimoire Header
// =============================================
@Composable
private fun GrimoireHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "SACRED ARCHIVE" label with gold line accent
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, CelestialGold)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.history_sacred_archive),
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 3.sp
                ),
                color = CelestialGold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CelestialGold, Color.Transparent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // "The Grimoire" headline
        ShimmerText(
            text = stringResource(R.string.history_grimoire_title),
            style = TextStyle(
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 44.sp,
                lineHeight = 52.sp,
                letterSpacing = (-0.5).sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description text
        Text(
            text = stringResource(R.string.history_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.3.sp
            ),
            color = DimSilver
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// =============================================
// Grimoire Search Bar
// =============================================
@Composable
private fun GrimoireSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(
                color = CosmicMid.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = DimSilver.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = DimSilver.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.history_search_hint),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = ManropeFamily,
                            letterSpacing = 0.3.sp
                        ),
                        color = DimSilver.copy(alpha = 0.4f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = StarWhite,
                        fontFamily = ManropeFamily,
                        letterSpacing = 0.3.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(CelestialGold),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear search",
                    tint = DimSilver,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}

// =============================================
// Grimoire Filter Chips
// =============================================
@Composable
private fun GrimoireFilterChips(
    filters: List<Pair<String, String>>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) AstralPurple.copy(alpha = 0.10f)
                        else CosmicMid.copy(alpha = 0.35f)
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            1.dp,
                            AstralPurple.copy(alpha = 0.20f),
                            RoundedCornerShape(50)
                        )
                        else Modifier.border(
                            1.dp,
                            DimSilver.copy(alpha = 0.06f),
                            RoundedCornerShape(50)
                        )
                    )
                    .clickable { onFilterSelected(key) }
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = if (isSelected) AstralPurple else DimSilver
                )
            }
        }
    }
}

// =============================================
// Timeline Phase Header
// =============================================
@Composable
private fun TimelinePhaseHeader(
    phaseName: String,
    monthYear: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left decorative line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CelestialGold.copy(alpha = 0.25f)
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.width(16.dp))

        // Phase label with diamond
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = phaseName.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    letterSpacing = 2.5.sp
                ),
                color = CelestialGold.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = monthYear,
                style = TextStyle(
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = DimSilver.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right decorative line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CelestialGold.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// =============================================
// Timeline Entry Card (Grimoire style)
// =============================================
@Composable
private fun TimelineEntryCard(
    reading: TarotReading,
    fullDateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val cardCount = remember(reading.drawnCardsJson) {
        try {
            reading.drawnCardsJson.count { it == '{' }
        } catch (_: Exception) { 0 }
    }
    val spreadLabel = when (reading.spreadType) {
        "single" -> stringResource(R.string.spread_single)
        "three_card" -> stringResource(R.string.spread_three_card)
        "celtic_cross" -> stringResource(R.string.spread_celtic_cross)
        "relationship" -> stringResource(R.string.spread_relationship)
        else -> reading.spreadType.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
    val topicLabel = when (reading.topic) {
        "love" -> stringResource(R.string.topic_love)
        "career" -> stringResource(R.string.topic_career)
        "general" -> stringResource(R.string.topic_general)
        "yes_no" -> stringResource(R.string.topic_yes_no)
        "spiritual" -> stringResource(R.string.topic_spiritual)
        else -> reading.topic.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
    val arcanaType = if (cardCount <= 1) stringResource(R.string.library_major_arcana)
        else stringResource(R.string.library_minor_arcana)
    val date = remember(reading.timestamp) { Date(reading.timestamp) }

    // Timeline row: vertical line on left + card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(start = 24.dp, end = 24.dp)
            .padding(vertical = 6.dp)
    ) {
        // Timeline vertical line + dot
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical gradient line
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CelestialGold.copy(alpha = 0.30f),
                                CelestialGold.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Timeline dot
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(8.dp)
                    .background(
                        CelestialGold.copy(alpha = 0.5f),
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.dp,
                        CelestialGold.copy(alpha = 0.3f),
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Entry card content
        ArtNouveauFrame(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            frameStyle = FrameStyle.SIMPLE,
            backgroundColor = CosmicMid.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Card thumbnail (2:3 aspect ratio, 80dp width)
                CardThumbnail(
                    reading = reading,
                    cardCount = cardCount
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Text content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Arcana type badge + Date row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Arcana badge
                        Box(
                            modifier = Modifier
                                .background(
                                    AstralPurple.copy(alpha = 0.08f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = arcanaType.uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.5.sp
                                ),
                                color = AstralPurple.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Date
                        Text(
                            text = fullDateFormat.format(date),
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = DimSilver.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Card name / topic as headline with italic keyword
                    Text(
                        text = buildString {
                            append(topicLabel)
                            append(" \u2022 ")
                            append(spreadLabel)
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = NewsreaderFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        color = StarWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Excerpt with left gold border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Left gold border (2dp, gold at 20%)
                                drawLine(
                                    color = CelestialGold.copy(alpha = 0.20f),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            text = reading.aiInterpretation.take(100).let {
                                if (reading.aiInterpretation.length > 100) "$it..." else it
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = ManropeFamily,
                                fontWeight = FontWeight.Light,
                                lineHeight = 18.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = MoonSilver.copy(alpha = 0.8f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "REVISIT INTERPRETATION" link
                    Text(
                        text = stringResource(R.string.history_revisit),
                        style = TextStyle(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp
                        ),
                        color = AstralPurple
                    )
                }
            }
        }
    }
}

// =============================================
// Card Thumbnail with decorative double border
// =============================================
@Composable
private fun CardThumbnail(
    reading: TarotReading,
    cardCount: Int
) {
    val spreadSymbol = remember(reading.spreadType) {
        when (reading.spreadType) {
            "single" -> Symbol.TAROT_CARD
            "three_card" -> Symbol.STAR
            "celtic_cross" -> Symbol.CELTIC_CROSS
            "relationship" -> Symbol.HEART
            else -> Symbol.LOTUS
        }
    }

    // Outer decorative border
    Box(
        modifier = Modifier
            .width(70.dp)
            .aspectRatio(2f / 3f)
            .border(
                width = 1.dp,
                color = CelestialGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(3.dp)
            .border(
                width = 1.dp,
                color = CelestialGold.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicMid.copy(alpha = 0.8f),
                        CosmicDeep.copy(alpha = 0.9f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SymbolIcon(
                symbol = spreadSymbol,
                color = CelestialGold.copy(alpha = 0.35f),
                size = 28.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (cardCount > 0) {
                Text(
                    text = "$cardCount",
                    style = TextStyle(
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp
                    ),
                    color = CelestialGold.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// =============================================
// Deepen Search Button
// =============================================
@Composable
private fun DeepenSearchButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(
                    width = 1.dp,
                    color = CelestialGold.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
                .background(CelestialGold.copy(alpha = 0.05f))
                .clickable { /* load more action */ }
                .padding(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.history_deepen_search),
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                ),
                color = CelestialGold
            )
        }
    }
}
