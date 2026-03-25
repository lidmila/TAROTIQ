package com.tarotiq.app.ui.shop

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.data.billing.PurchaseState
import com.tarotiq.app.ui.components.*
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

    LaunchedEffect(purchaseState) {
        if (purchaseState is PurchaseState.Success) {
            kotlinx.coroutines.delay(2000)
            coinShopViewModel.resetPurchaseState()
        }
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
                        Text(
                            stringResource(R.string.coins_buy),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CelestialGold
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AstralPurple) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Balance
                ArtNouveauFrame(modifier = Modifier.fillMaxWidth(), frameStyle = FrameStyle.ARCH) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.coins_balance).uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFamily), color = MoonSilver)
                        Spacer(modifier = Modifier.height(8.dp))
                        SymbolIcon(symbol = Symbol.COIN, size = 40.dp, color = CelestialGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$coinBalance", style = MaterialTheme.typography.displayMedium, color = CelestialGold, fontWeight = FontWeight.Light)
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
                            SymbolIcon(symbol = Symbol.COIN, size = 36.dp, color = CelestialGold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        when (pack.coins) {
                                            5 -> stringResource(R.string.coins_pack_5)
                                            15 -> stringResource(R.string.coins_pack_15)
                                            50 -> stringResource(R.string.coins_pack_50)
                                            300 -> stringResource(R.string.coins_pack_300)
                                            else -> "${pack.coins}"
                                        },
                                        style = MaterialTheme.typography.headlineSmall, color = StarWhite
                                    )
                                    pack.badge?.let { badge ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = if (badge == "most_popular") AstralPurple else CelestialGold) {
                                            Text(
                                                (if (badge == "most_popular") stringResource(R.string.coins_most_popular) else stringResource(R.string.coins_best_value)).uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily), color = VoidBlack,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(pack.formattedPrice.ifEmpty { "---" }, style = MaterialTheme.typography.bodyMedium, color = MoonSilver)
                                if (pack.savingsPercent > 0) {
                                    Text(
                                        text = stringResource(R.string.coins_save, pack.savingsPercent),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            letterSpacing = 1.sp
                                        ),
                                        color = SuccessEmerald,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                when (val state = purchaseState) {
                    is PurchaseState.Processing -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = CelestialGold)
                    }
                    is PurchaseState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("+${state.coinsGranted} ${stringResource(R.string.coins_balance).lowercase()}!", color = SuccessEmerald, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    }
                    is PurchaseState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = ErrorCrimson, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    }
                    else -> {}
                }
            }
        }
        }
    }
}
