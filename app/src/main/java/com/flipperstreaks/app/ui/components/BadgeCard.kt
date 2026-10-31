package com.flipperstreaks.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flipperstreaks.app.domain.model.Badge
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class BadgeVisuals(val color: Color, val icon: ImageVector)

private fun visualsFor(id: String): BadgeVisuals = when (id) {
    "homebody" -> BadgeVisuals(Color(0xFF7E57C2), Icons.Filled.Home)          // purple
    "time_traveler" -> BadgeVisuals(Color(0xFF3949AB), Icons.Filled.History)  // indigo
    "cross_country" -> BadgeVisuals(Color(0xFF00897B), Icons.Filled.Explore)  // teal
    "manufacturer_mixer" -> BadgeVisuals(Color(0xFFEF6C00), Icons.Filled.Factory) // orange
    "week_streak" -> BadgeVisuals(Color(0xFFE53935), Icons.Filled.Whatshot)   // red
    else -> BadgeVisuals(Color(0xFF0F6E56), Icons.Filled.CheckCircle)
}

private val EARNED_DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@Composable
fun BadgeCard(badge: Badge) {
    if (badge.isEarned) EarnedBadgeCard(badge) else UnearnedBadgeCard(badge)
}

@Composable
private fun EarnedBadgeCard(badge: Badge) {
    val v = visualsFor(badge.id)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = v.color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(v.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = v.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        badge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = v.color
                    )
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Earned",
                        tint = v.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    earnedMessage(badge),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                badge.earnedOn?.let { date ->
                    Text(
                        "Earned ${date.format(EARNED_DATE_FMT)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun UnearnedBadgeCard(badge: Badge) {
    val v = visualsFor(badge.id)
    val ratio = if (badge.goal > 0) (badge.progress.toFloat() / badge.goal).coerceIn(0f, 1f) else 0f
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(1.dp, v.color.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(v.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = v.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        badge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${badge.progress} of ${badge.goal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = v.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = v.color,
                    trackColor = v.color.copy(alpha = 0.15f)
                )
            }
        }
    }
}

private fun earnedMessage(badge: Badge): String = when (badge.id) {
    "homebody" -> "You've made a home at your favorite spot."
    "time_traveler" -> "You've journeyed across five decades of pinball."
    "cross_country" -> "You've played at five different locations."
    "manufacturer_mixer" -> "You've sampled machines from five makers."
    "week_streak" -> "Seven days in a row — a full week of flippin'."
    else -> badge.description
}

@Composable
fun StreakHeatmap(playDatesEpochDay: List<Long>) {
    Text(
        "Heatmap placeholder — ${playDatesEpochDay.size} active days",
        style = MaterialTheme.typography.labelSmall
    )
}
