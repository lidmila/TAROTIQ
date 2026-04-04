package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraCardPickerScreen(
    existingCardIds: Set<Int>,
    coinBalance: Int,
    onCoinSpent: suspend () -> Boolean,
    onCardConfirmed: (Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedCard by remember { mutableStateOf<Int?>(null) }
    var isSpending by remember { mutableStateOf(false) }
    var spendFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Local coin display — drops by 1 immediately on spend
    var displayCoins by remember { mutableIntStateOf(coinBalance) }
    val animatedCoins by animateIntAsState(
        targetValue = displayCoins,
        animationSpec = tween(600),
        label = "coin_countdown"
    )

    // Prevent back navigation while coin transaction is in progress
    BackHandler(enabled = isSpending) { /* block back press */ }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.extra_card_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CelestialGold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = !isSpending) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = StarWhite)
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            SymbolIcon(symbol = Symbol.COIN, size = 18.dp, color = CelestialGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$animatedCoins",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSpending) CelestialGold else StarWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 75.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(78) { cardId ->
                        val isDisabled = cardId in existingCardIds
                        val isSelected = selectedCard == cardId

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1f,
                            animationSpec = tween(200), label = "scale_$cardId"
                        )

                        Box(
                            modifier = Modifier
                                .aspectRatio(2f / 3f)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = if (isDisabled) 0.3f else 1f
                                }
                                .shadow(
                                    elevation = if (isSelected) 8.dp else 2.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    ambientColor = if (isSelected) CelestialGold.copy(alpha = 0.3f) else Color.Transparent
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) CelestialGold else CelestialGold.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isDisabled && !isSpending) {
                                    selectedCard = if (selectedCard == cardId) null else cardId
                                    spendFailed = false
                                }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.card_back),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (spendFailed) {
                    Text(
                        text = stringResource(R.string.coins_not_enough),
                        color = ErrorCrimson,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Loading indicator while spending
                if (isSpending) {
                    CircularProgressIndicator(
                        color = CelestialGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AnimatedVisibility(visible = selectedCard != null && !isSpending) {
                    ArtNouveauButton(
                        text = stringResource(R.string.extra_card_confirm),
                        onClick = {
                            isSpending = true
                            spendFailed = false
                            // Immediately animate coin display down
                            displayCoins = (displayCoins - 1).coerceAtLeast(0)
                            scope.launch {
                                // Wait for server to confirm coin deduction
                                val success = onCoinSpent()
                                if (success) {
                                    // Coins confirmed spent — navigate back
                                    selectedCard?.let { onCardConfirmed(it) }
                                } else {
                                    // Failed — revert animation
                                    isSpending = false
                                    spendFailed = true
                                    displayCoins = coinBalance
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        variant = ButtonVariant.PRIMARY,
                        enabled = coinBalance > 0
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
