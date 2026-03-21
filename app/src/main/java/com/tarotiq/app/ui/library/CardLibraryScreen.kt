package com.tarotiq.app.ui.library

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.CardLibraryViewModel

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
    val context = LocalContext.current

    val filters = listOf(
        "all" to stringResource(R.string.library_all),
        "major" to stringResource(R.string.library_major_arcana),
        "cups" to stringResource(R.string.library_cups),
        "pentacles" to stringResource(R.string.library_pentacles),
        "swords" to stringResource(R.string.library_swords),
        "wands" to stringResource(R.string.library_wands)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldSecondary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        stringResource(R.string.library_search),
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MysticPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = SurfaceDark.copy(alpha = 0.6f),
                    unfocusedContainerColor = SurfaceDark.copy(alpha = 0.4f),
                    cursorColor = GoldSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { viewModel.setFilter(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MysticPrimary,
                            selectedLabelColor = OnPrimary,
                            containerColor = SurfaceDark.copy(alpha = 0.6f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GlassBorder,
                            selectedBorderColor = MysticPrimary,
                            enabled = true,
                            selected = selectedFilter == key
                        ),
                        modifier = Modifier.weight(1f)
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

@Composable
private fun CardGridItem(
    card: TarotCard,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card image
            val imageResId = context.resources.getIdentifier(
                card.imageRes, "drawable", context.packageName
            )
            if (imageResId != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = card.nameKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83C\uDCCF",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Card name
            Text(
                text = getCardDisplayName(card),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Suit/Arcana indicator
            Text(
                text = if (card.arcana == "major") {
                    stringResource(R.string.library_major_arcana)
                } else {
                    getSuitDisplayName(card.suit)
                },
                style = MaterialTheme.typography.labelSmall,
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
    if (card.arcana == "major") return GoldSecondary
    return when (card.suit) {
        "cups" -> CrystalBlue
        "pentacles" -> SuccessColor
        "swords" -> TextSecondary
        "wands" -> CandleFlicker
        else -> TextSecondary
    }
}
