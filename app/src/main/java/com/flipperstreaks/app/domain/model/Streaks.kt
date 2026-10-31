package com.flipperstreaks.app.domain.model

import java.time.LocalDate

data class StreakSummary(
    val currentDayStreak: Int,
    val longestDayStreak: Int,
    val totalPlays: Int,
    val uniqueMachines: Int,
    val uniqueLocations: Int,
    val playedToday: Boolean,
    val badges: List<Badge>
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val isEarned: Boolean,
    val progress: Int = 0,
    val goal: Int = 1,
    val earnedOn: LocalDate? = null
)
