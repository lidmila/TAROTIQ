package com.tarotiq.app.ui.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.ReadingSpread
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.HomeViewModel

private data class SpreadOption(
    val spread: ReadingSpread,
    val nameRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val emoji: String
)

private val spreadOptions = listOf(
    SpreadOption(
        spread = ReadingSpread.SINGLE,
        nameRes = R.string.spread_single,
        descRes = R.string.spread_single_desc,
        icon = Icons.Default.Style,
        emoji = "\uD83C\uDCCF"
    ),
    SpreadOption(
        spread = ReadingSpread.THREE_CARD,
        nameRes = R.string.spread_three_card,
        descRes = R.string.spread_three_card_desc,
        icon = Icons.Default.ViewColumn,
        emoji = "\uD83C\uDCCF\uD83C\uDCCF\uD83C\uDCCF"
    ),
    SpreadOption(
        spread = ReadingSpread.RELATIONSHIP,
        nameRes = R.string.spread_relationship,
        descRes = R.string.spread_relationship_desc,
        icon = Icons.Default.Favorite,
        emoji = "\u2764\uFE0F"
    ),
    SpreadOption(
        spread = ReadingSpread.CELTIC_CROSS,
        nameRes = R.string.spread_celtic_cross,
        descRes = R.string.spread_celtic_cross_desc,
        icon = Icons.Default.GridView,
        emoji = "\u271D\uFE0F"
    )
)

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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.spread_select),
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
                actions = {
                    // Coin balance in top bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "\uD83E\uDE99",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${coinBalance.balance}",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldSecondary
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

                // Free readings info
                if (coinBalance.hasFreeReadings) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SuccessColor.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = SuccessColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.coins_free_readings,
                                    coinBalance.freeReadingsRemaining
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessColor
                            )
                        }
                    }
                }

                // Spread options
                spreadOptions.forEach { option ->
                    SpreadOptionCard(
                        option = option,
                        canAfford = coinBalance.balance >= option.spread.coinCost || coinBalance.hasFreeReadings,
                        onClick = { onSpreadSelected(option.spread.key) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SpreadOptionCard(
    option: SpreadOption,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (canAfford) GlassBorder else ErrorColor.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Icon(
                    option.icon,
                    contentDescription = null,
                    tint = MysticLight,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${option.spread.cardCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle: Name + description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(option.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(option.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Coin cost
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (canAfford) GoldSecondary.copy(alpha = 0.15f) else ErrorColor.copy(alpha = 0.1f),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83E\uDE99",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${option.spread.coinCost}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (canAfford) GoldSecondary else ErrorColor
                    )
                }
            }
        }
    }
}
