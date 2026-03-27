package com.tarotiq.app.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.CardLibraryViewModel

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardLibraryScreen(
    onNavigateToCardDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CardLibraryViewModel = viewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filters = listOf(
        "all" to stringResource(R.string.library_all),
        "major" to stringResource(R.string.library_major_arcana),
        "cups" to stringResource(R.string.library_cups),
        "pentacles" to stringResource(R.string.library_pentacles),
        "swords" to stringResource(R.string.library_swords),
        "wands" to stringResource(R.string.library_wands)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = CelestialGold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicDeep.copy(alpha = 0.85f)
                )
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        stringResource(R.string.library_search),
                        color = MoonSilver
                    )
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = MoonSilver)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = null, tint = MoonSilver)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AstralPurple,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                    unfocusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                    cursorColor = CelestialGold,
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips — horizontally scrollable carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { viewModel.setFilter(key) },
                        label = {
                            Text(
                                text = label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    letterSpacing = 1.5.sp
                                ),
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AstralPurple,
                            selectedLabelColor = StarWhite,
                            containerColor = CosmicMid.copy(alpha = 0.5f),
                            labelColor = MoonSilver
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GlassBorder,
                            selectedBorderColor = AstralPurple,
                            enabled = true,
                            selected = selectedFilter == key
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cards grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cards, key = { it.id }) { card ->
                    CardGridItem(
                        card = card,
                        onClick = { onNavigateToCardDetail(card.id) }
                    )
                }

                // Bottom spacing for navigation bar
                item { Spacer(modifier = Modifier.height(80.dp)) }
                item { Spacer(modifier = Modifier.height(80.dp)) }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        }
    }
}

@Composable
private fun CardGridItem(
    card: TarotCard,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    ArtNouveauFrame(
        onClick = onClick,
        frameStyle = FrameStyle.SIMPLE,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card image
            val imageResId = remember(card.imageRes) {
                context.resources.getIdentifier(
                    card.imageRes, "drawable", context.packageName
                )
            }
            val actualResId = if (imageResId != 0) imageResId else R.drawable.card_back
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("android.resource://${context.packageName}/$actualResId")
                    .size(300, 450)
                    .crossfade(true)
                    .build(),
                contentDescription = getCardDisplayName(card),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.6f)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Card name — NewsreaderFamily (via headlineSmall style base)
            Text(
                text = getCardDisplayName(card),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Normal
                ),
                color = StarWhite,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Suit/Arcana indicator
            Text(
                text = (if (card.arcana == "major") {
                    stringResource(R.string.library_major_arcana)
                } else {
                    getSuitDisplayName(card.suit)
                }).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    letterSpacing = 1.sp
                ),
                color = getSuitColor(card),
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
private fun getSuitDisplayName(suit: String?): String {
    return when (suit) {
        "cups" -> stringResource(R.string.library_cups)
        "pentacles" -> stringResource(R.string.library_pentacles)
        "swords" -> stringResource(R.string.library_swords)
        "wands" -> stringResource(R.string.library_wands)
        else -> ""
    }
}

private fun getSuitColor(card: TarotCard): androidx.compose.ui.graphics.Color {
    if (card.arcana == "major") return CelestialGold
    return when (card.suit) {
        "cups" -> InfoSky
        "pentacles" -> SuccessEmerald
        "swords" -> MoonSilver
        "wands" -> CelestialGoldLight
        else -> MoonSilver
    }
}
