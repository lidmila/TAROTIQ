package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.ReadingSpread
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.HomeViewModel

private data class SpreadOption(
    val spread: ReadingSpread,
    val nameRes: Int,
    val descRes: Int,
    val imageRes: Int
)

private val spreadOptions = listOf(
    SpreadOption(
        spread = ReadingSpread.SINGLE,
        nameRes = R.string.spread_single,
        descRes = R.string.spread_single_desc,
        imageRes = R.drawable.card_1
    ),
    SpreadOption(
        spread = ReadingSpread.THREE_CARD,
        nameRes = R.string.spread_three_card,
        descRes = R.string.spread_three_card_desc,
        imageRes = R.drawable.cards_3
    ),
    SpreadOption(
        spread = ReadingSpread.RELATIONSHIP,
        nameRes = R.string.spread_relationship,
        descRes = R.string.spread_relationship_desc,
        imageRes = R.drawable.love
    ),
    SpreadOption(
        spread = ReadingSpread.CELTIC_CROSS,
        nameRes = R.string.spread_celtic_cross,
        descRes = R.string.spread_celtic_cross_desc,
        imageRes = R.drawable.celtic
    ),
    SpreadOption(
        spread = ReadingSpread.YEAR_AHEAD,
        nameRes = R.string.spread_year_ahead,
        descRes = R.string.spread_year_ahead_desc,
        imageRes = R.drawable.fortune
    ),
    SpreadOption(
        spread = ReadingSpread.SHADOW_SELF,
        nameRes = R.string.spread_shadow_self,
        descRes = R.string.spread_shadow_self_desc,
        imageRes = R.drawable.had
    ),
    SpreadOption(
        spread = ReadingSpread.CROSSROADS,
        nameRes = R.string.spread_crossroads,
        descRes = R.string.spread_crossroads_desc,
        imageRes = R.drawable.osud
    ),
    SpreadOption(
        spread = ReadingSpread.CHAKRA,
        nameRes = R.string.spread_chakra,
        descRes = R.string.spread_chakra_desc,
        imageRes = R.drawable.duchovni
    ),
    SpreadOption(
        spread = ReadingSpread.TWIN_FLAME,
        nameRes = R.string.spread_twin_flame,
        descRes = R.string.spread_twin_flame_desc,
        imageRes = R.drawable.love
    ),
    SpreadOption(
        spread = ReadingSpread.MOON_CYCLE,
        nameRes = R.string.spread_moon_cycle,
        descRes = R.string.spread_moon_cycle_desc,
        imageRes = R.drawable.lunar
    ),
    SpreadOption(
        spread = ReadingSpread.CAREER_COMPASS,
        nameRes = R.string.spread_career_compass,
        descRes = R.string.spread_career_compass_desc,
        imageRes = R.drawable.osud
    ),
    SpreadOption(
        spread = ReadingSpread.INNER_CHILD,
        nameRes = R.string.spread_inner_child,
        descRes = R.string.spread_inner_child_desc,
        imageRes = R.drawable.dite
    ),
    SpreadOption(
        spread = ReadingSpread.TREE_OF_LIFE,
        nameRes = R.string.spread_tree_of_life,
        descRes = R.string.spread_tree_of_life_desc,
        imageRes = R.drawable.tree
    ),
    SpreadOption(
        spread = ReadingSpread.WEEK_AHEAD,
        nameRes = R.string.spread_week_ahead,
        descRes = R.string.spread_week_ahead_desc,
        imageRes = R.drawable.fortune
    ),
    SpreadOption(
        spread = ReadingSpread.KARMIC,
        nameRes = R.string.spread_karmic,
        descRes = R.string.spread_karmic_desc,
        imageRes = R.drawable.nekonecno
    ),
    SpreadOption(
        spread = ReadingSpread.SELF_LOVE,
        nameRes = R.string.spread_self_love,
        descRes = R.string.spread_self_love_desc,
        imageRes = R.drawable.love
    )
)

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadSelectionScreen(
    topic: String,
    question: String?,
    onSpreadSelected: (spreadType: String) -> Unit,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val coinBalance by homeViewModel.coinBalance.collectAsState()

    // Track selected spread for visual highlight
    var selectedSpread by remember { mutableStateOf<ReadingSpread?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.spread_select),
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
                actions = {
                    // Coin balance in top bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        SymbolIcon(
                            symbol = Symbol.COIN,
                            size = 18.dp,
                            color = CelestialGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${coinBalance.balance}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = CelestialGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Section header: "SELECT SPREAD" with gold line accents
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OrnamentalDivider(
                        modifier = Modifier.weight(1f),
                        color = CelestialGold.copy(alpha = 0.3f),
                        height = 16.dp
                    )
                }

                // Free readings info
                if (coinBalance.hasFreeReadings) {
                    ArtNouveauFrame(
                        modifier = Modifier.fillMaxWidth(),
                        frameStyle = FrameStyle.SIMPLE,
                        backgroundColor = SuccessEmerald.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SymbolIcon(
                                symbol = Symbol.STAR,
                                size = 20.dp,
                                color = SuccessEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.coins_free_readings,
                                    coinBalance.freeReadingsRemaining
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    letterSpacing = 1.sp
                                ),
                                color = SuccessEmerald
                            )
                        }
                    }
                }

                // Filter spreads based on selected topic
                val loveOnly = setOf(
                    ReadingSpread.RELATIONSHIP, ReadingSpread.TWIN_FLAME
                )
                val careerOnly = setOf(ReadingSpread.CAREER_COMPASS)
                val spiritualOnly = setOf(ReadingSpread.TREE_OF_LIFE)
                val availableSpreads = when (topic) {
                    "all" -> ReadingSpread.entries.toList()
                    "love" -> ReadingSpread.entries.filter { it !in careerOnly && it !in spiritualOnly }
                    "career" -> ReadingSpread.entries.filter { it !in loveOnly && it !in spiritualOnly }
                    "general" -> ReadingSpread.entries.filter { it !in loveOnly && it !in careerOnly && it !in spiritualOnly }
                    "yes_no" -> listOf(ReadingSpread.SINGLE)
                    "spiritual" -> ReadingSpread.entries.filter { it !in loveOnly && it !in careerOnly }
                    else -> ReadingSpread.entries.toList()
                }
                val filteredOptions = spreadOptions.filter { it.spread in availableSpreads }

                // Spread options
                filteredOptions.forEach { option ->
                    val isSelected = selectedSpread == option.spread
                    SpreadOptionCard(
                        option = option,
                        canAfford = coinBalance.balance >= option.spread.coinCost || coinBalance.hasFreeReadings,
                        isSelected = isSelected,
                        onClick = {
                            if (!hasNavigated) {
                                hasNavigated = true
                                selectedSpread = option.spread
                                onSpreadSelected(option.spread.key)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        }
    }
}

@Composable
private fun SpreadOptionCard(
    option: SpreadOption,
    canAfford: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        AstralPurple.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    val bgColor = if (isSelected) {
        AstralPurple.copy(alpha = 0.10f)
    } else {
        CosmicMid.copy(alpha = 0.3f)
    }

    ArtNouveauFrame(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = AstralPurple.copy(alpha = 0.15f)
                ) else Modifier
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        frameStyle = FrameStyle.ORNATE,
        backgroundColor = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Symbol area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Image(
                    painter = painterResource(option.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${option.spread.cardCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGroteskFamily,
                        letterSpacing = 2.sp
                    ),
                    color = MoonSilver
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle: Name + description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(option.nameRes),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    ),
                    color = StarWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(option.descRes),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = ManropeFamily
                    ),
                    color = MoonSilver
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Coin cost
            ArtNouveauFrame(
                modifier = Modifier.wrapContentSize(),
                frameStyle = FrameStyle.SIMPLE,
                backgroundColor = if (canAfford) CelestialGold.copy(alpha = 0.12f) else ErrorCrimson.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SymbolIcon(
                        symbol = Symbol.COIN,
                        size = 14.dp,
                        color = if (canAfford) CelestialGold else ErrorCrimson
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${option.spread.coinCost}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (canAfford) CelestialGold else ErrorCrimson
                    )
                }
            }
        }
    }
}
