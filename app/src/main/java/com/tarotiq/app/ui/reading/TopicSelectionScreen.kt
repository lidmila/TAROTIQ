package com.tarotiq.app.ui.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*

private data class TopicItem(
    val key: String,
    val nameRes: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val emoji: String
)

private val topics = listOf(
    TopicItem("love", R.string.topic_love, Icons.Default.Favorite, Color(0xFFE57373), "\u2764\uFE0F"),
    TopicItem("career", R.string.topic_career, Icons.Default.Work, Color(0xFF64B5F6), "\uD83D\uDCBC"),
    TopicItem("general", R.string.topic_general, Icons.Default.AutoAwesome, MysticLight, "\u2728"),
    TopicItem("yes_no", R.string.topic_yes_no, Icons.Default.HelpOutline, GoldSecondary, "\u2753"),
    TopicItem("spiritual", R.string.topic_spiritual, Icons.Default.SelfImprovement, TealTertiary, "\uD83E\uDDD8")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSelectionScreen(
    onTopicSelected: (topic: String) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.topic_select),
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

@Composable
private fun TopicCard(
    topic: TopicItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = topic.emoji,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Icon(
                topic.icon,
                contentDescription = null,
                tint = topic.accentColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(topic.nameRes),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
