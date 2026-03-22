package com.tarotiq.app.ui.daily

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.DailyCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCardScreen(
    onBack: () -> Unit,
    onCardDetail: (Int) -> Unit,
    dailyCardViewModel: DailyCardViewModel = viewModel()
) {
    val dailyCard by dailyCardViewModel.dailyCard.collectAsState()
    val hasDrawnToday by dailyCardViewModel.hasDrawnToday.collectAsState()
    val isLoading by dailyCardViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var showCard by remember { mutableStateOf(false) }

    LaunchedEffect(hasDrawnToday) { if (hasDrawnToday) showCard = true }

    val rotation by animateFloatAsState(
        targetValue = if (showCard) 180f else 0f,
        animationSpec = tween(800, easing = EaseInOutCubic), label = "flip"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.daily_title)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (hasDrawnToday) stringResource(R.string.daily_already_drawn) else stringResource(R.string.daily_tap_reveal),
                    style = MaterialTheme.typography.titleMedium, color = GoldSecondary, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.width(180.dp).height(270.dp)
                        .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !hasDrawnToday && !isLoading) { dailyCardViewModel.drawDailyCard() }
                ) {
                    if (rotation <= 90f) {
                        Image(painterResource(R.drawable.card_back), "Card back", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        dailyCard?.let { card ->
                            val resName = if (card.cardId < 22) "major_arcana_x22_%02d".format(card.cardId + 1)
                                else { val mi = card.cardId - 22; val suit = when (mi / 14) { 0->"cups"; 1->"pentacles"; 2->"swords"; else->"wands" }
                                    "%02d_%s_x14_minor_arcana".format(mi % 14 + 1, suit) }
                            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                            Image(painterResource(if (resId != 0) resId else R.drawable.card_back), "Daily card",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f; if (card.isReversed) rotationZ = 180f })
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(color = MysticPrimary, modifier = Modifier.padding(top = 16.dp))
                }

                dailyCard?.briefInsight?.let { insight ->
                    Spacer(modifier = Modifier.height(24.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(insight, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                    }
                }

                dailyCard?.let { card ->
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { onCardDetail(card.cardId) }) {
                        Text(stringResource(R.string.daily_view_details), color = MysticLight)
                    }
                }
            }
        }
    }
}
