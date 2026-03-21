package com.tarotiq.app.ui.shop

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.data.billing.PurchaseState
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.CoinShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinShopScreen(
    onBack: () -> Unit,
    coinShopViewModel: CoinShopViewModel = viewModel()
) {
    val coinPacks by coinShopViewModel.coinPacks.collectAsState()
    val purchaseState by coinShopViewModel.purchaseState.collectAsState()
    val coinBalance by coinShopViewModel.coinBalance.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle purchase result
    LaunchedEffect(purchaseState) {
        if (purchaseState is PurchaseState.Success) {
            kotlinx.coroutines.delay(2000)
            coinShopViewModel.resetPurchaseState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.coins_buy)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Balance
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.coins_balance), style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text("$coinBalance", style = MaterialTheme.typography.displaySmall, color = GoldSecondary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Coin packs
                coinPacks.forEach { pack ->
                    GlassCard(
                        onClick = { activity?.let { coinShopViewModel.purchaseCoinPack(it, pack.productId) } },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, null, tint = GoldSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${pack.coins} coins", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                    pack.badge?.let { badge ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = if (badge == "most_popular") MysticPrimary else GoldSecondary) {
                                            Text(
                                                if (badge == "most_popular") stringResource(R.string.coins_most_popular) else stringResource(R.string.coins_best_value),
                                                style = MaterialTheme.typography.labelSmall, color = OnPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(pack.formattedPrice.ifEmpty { "---" }, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                }

                // Purchase state feedback
                when (val state = purchaseState) {
                    is PurchaseState.Processing -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = MysticPrimary)
                    }
                    is PurchaseState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("+${state.coinsGranted} coins!", color = SuccessColor, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    }
                    is PurchaseState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = ErrorColor, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    }
                    else -> {}
                }
            }
        }
    }
}
