package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*

private data class TopicItem(
    val key: String,
    val nameRes: Int,
    val iconRes: Int,
    val accentColor: Color
)

private val topics = listOf(
    TopicItem("love", R.string.topic_love, R.drawable.love, Color(0xFFE8A0A0)),
    TopicItem("career", R.string.topic_career, R.drawable.career, CelestialGold),
    TopicItem("general", R.string.topic_general, R.drawable.obecne, AstralPurple),
    TopicItem("yes_no", R.string.topic_yes_no, R.drawable.ano_ne, CelestialGoldLight),
    TopicItem("spiritual", R.string.topic_spiritual, R.drawable.duchovni, AstralPurpleLight)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSelectionScreen(
    onTopicSelected: (topic: String) -> Unit,
    onBack: () -> Unit
) {
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
                        text = stringResource(R.string.topic_select),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Topics grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Row 1: Love + Career
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TopicCard(
                        topic = topics[0],
                        onClick = { onTopicSelected(topics[0].key) },
                        modifier = Modifier.weight(1f)
                    )
                    TopicCard(
                        topic = topics[1],
                        onClick = { onTopicSelected(topics[1].key) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: General + Yes/No
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TopicCard(
                        topic = topics[2],
                        onClick = { onTopicSelected(topics[2].key) },
                        modifier = Modifier.weight(1f)
                    )
                    TopicCard(
                        topic = topics[3],
                        onClick = { onTopicSelected(topics[3].key) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Spiritual (centered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TopicCard(
                        topic = topics[4],
                        onClick = { onTopicSelected(topics[4].key) },
                        modifier = Modifier.width(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        }
    }
}

@Composable
private fun TopicCard(
    topic: TopicItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArtNouveauFrame(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ),
        frameStyle = FrameStyle.ORNATE,
        backgroundColor = CosmicMid.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val topicTransition = rememberInfiniteTransition(label = "topic_${topic.key}")
            val topicFloat by topicTransition.animateFloat(
                initialValue = -3f, targetValue = 3f,
                animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "topic_float_${topic.key}"
            )
            val topicGlow by topicTransition.animateFloat(
                initialValue = 0.1f, targetValue = 0.35f,
                animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "topic_glow_${topic.key}"
            )

            Box(contentAlignment = Alignment.Center) {
                // Glow aura
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(topic.accentColor.copy(alpha = topicGlow), Color.Transparent)
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                // Topic icon image with float animation
                Image(
                    painter = painterResource(id = topic.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer { translationY = topicFloat * density },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(topic.nameRes).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                ),
                color = StarWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}
