package com.flipperstreaks.app.domain.streaks

import com.flipperstreaks.app.domain.model.Badge
import com.flipperstreaks.app.domain.model.PlayLog
import com.flipperstreaks.app.domain.model.StreakSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Pure logic, no Android/Room/network dependencies — trivially unit testable.
// Given the full play log, compute the streaks/badges dashboard shown in ui/streaks.
object StreakEngine {

    private const val HOMEBODY_GOAL = 5
    private const val TIME_TRAVELER_GOAL = 5
    private const val CROSS_COUNTRY_GOAL = 5
    private const val MIXER_GOAL = 5
    private const val WEEK_STREAK_GOAL = 7

    fun compute(logs: List<PlayLog>, zoneId: ZoneId = ZoneId.systemDefault()): StreakSummary {
        if (logs.isEmpty()) {
            return StreakSummary(
                currentDayStreak = 0,
                longestDayStreak = 0,
                totalPlays = 0,
                uniqueMachines = 0,
                uniqueLocations = 0,
                playedToday = false,
                badges = allBadges(
                    earnDates = emptyMap(),
                    distinctDecades = 0,
                    maxPlaysAtOneLocation = 0,
                    uniqueLocations = 0,
                    distinctManufacturers = 0,
                    currentStreak = 0
                )
            )
        }

        val playDates = logs.map {
            Instant.ofEpochMilli(it.timestampEpochMillis).atZone(zoneId).toLocalDate()
        }.toSortedSet()

        val currentStreak = currentDayStreak(playDates, zoneId)
        val longestStreak = longestDayStreak(playDates)

        val uniqueMachines = logs.map { it.machineId }.distinct().count()
        val uniqueLocations = logs.map { it.locationId }.distinct().count()
        val distinctManufacturers = logs.mapNotNull { it.manufacturer }.distinct().count()
        val distinctDecades = logs.mapNotNull { it.year }.map { (it / 10) * 10 }.distinct().count()
        val maxPlaysAtOneLocation = logs.groupingBy { it.locationId }.eachCount().values.maxOrNull() ?: 0

        val earnDates = computeEarnDates(logs, zoneId)
        val playedToday = LocalDate.now(zoneId) in playDates

        return StreakSummary(
            currentDayStreak = currentStreak,
            longestDayStreak = longestStreak,
            totalPlays = logs.size,
            uniqueMachines = uniqueMachines,
            uniqueLocations = uniqueLocations,
            playedToday = playedToday,
            badges = allBadges(
                earnDates = earnDates,
                distinctDecades = distinctDecades,
                maxPlaysAtOneLocation = maxPlaysAtOneLocation,
                uniqueLocations = uniqueLocations,
                distinctManufacturers = distinctManufacturers,
                currentStreak = currentStreak
            )
        )
    }

    // Walk logs in chronological order, recording the first date each badge threshold is crossed.
    // A badge, once earned, stays earned — even if the underlying tally later drops (e.g. week streak).
    private fun computeEarnDates(logs: List<PlayLog>, zoneId: ZoneId): Map<String, LocalDate> {
        val earned = mutableMapOf<String, LocalDate>()
        val sorted = logs.sortedBy { it.timestampEpochMillis }

        val locationCounts = mutableMapOf<Int, Int>()
        val decades = mutableSetOf<Int>()
        val locations = mutableSetOf<Int>()
        val manufacturers = mutableSetOf<String>()
        val playDates = sortedSetOf<LocalDate>()

        for (log in sorted) {
            val date = Instant.ofEpochMilli(log.timestampEpochMillis).atZone(zoneId).toLocalDate()
            playDates += date

            val locCount = (locationCounts[log.locationId] ?: 0) + 1
            locationCounts[log.locationId] = locCount
            if ("homebody" !in earned && locCount >= HOMEBODY_GOAL) earned["homebody"] = date

            log.year?.let { decades += (it / 10) * 10 }
            if ("time_traveler" !in earned && decades.size >= TIME_TRAVELER_GOAL) earned["time_traveler"] = date

            locations += log.locationId
            if ("cross_country" !in earned && locations.size >= CROSS_COUNTRY_GOAL) earned["cross_country"] = date

            log.manufacturer?.let { manufacturers += it }
            if ("manufacturer_mixer" !in earned && manufacturers.size >= MIXER_GOAL) earned["manufacturer_mixer"] = date

            if ("week_streak" !in earned && longestDayStreak(playDates) >= WEEK_STREAK_GOAL) {
                earned["week_streak"] = date
            }
        }
        return earned
    }

    private fun currentDayStreak(playDates: Set<LocalDate>, zoneId: ZoneId): Int {
        var cursor = LocalDate.now(zoneId)
        // Today not yet played still counts as an active streak through yesterday.
        if (cursor !in playDates) cursor = cursor.minusDays(1)
        var streak = 0
        while (cursor in playDates) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private fun longestDayStreak(playDates: Set<LocalDate>): Int {
        var longest = 0
        var current = 0
        var previous: LocalDate? = null
        for (date in playDates) {
            current = if (previous != null && previous.plusDays(1) == date) current + 1 else 1
            longest = maxOf(longest, current)
            previous = date
        }
        return longest
    }

    private fun allBadges(
        earnDates: Map<String, LocalDate>,
        distinctDecades: Int,
        maxPlaysAtOneLocation: Int,
        uniqueLocations: Int,
        distinctManufacturers: Int,
        currentStreak: Int
    ): List<Badge> = listOf(
        badge("time_traveler", "Time traveler", "Played machines from $TIME_TRAVELER_GOAL different decades",
            earnDates, progress = distinctDecades, goal = TIME_TRAVELER_GOAL),
        badge("homebody", "Homebody", "$HOMEBODY_GOAL plays at your single most-visited location",
            earnDates, progress = maxPlaysAtOneLocation, goal = HOMEBODY_GOAL),
        badge("cross_country", "Cross-country", "Logged plays at $CROSS_COUNTRY_GOAL or more distinct locations",
            earnDates, progress = uniqueLocations, goal = CROSS_COUNTRY_GOAL),
        badge("manufacturer_mixer", "Manufacturer mixer", "Played machines from $MIXER_GOAL different manufacturers",
            earnDates, progress = distinctManufacturers, goal = MIXER_GOAL),
        badge("week_streak", "Week streak", "Played on $WEEK_STREAK_GOAL consecutive days",
            earnDates, progress = currentStreak, goal = WEEK_STREAK_GOAL)
    )

    private fun badge(
        id: String,
        title: String,
        description: String,
        earnDates: Map<String, LocalDate>,
        progress: Int,
        goal: Int
    ): Badge {
        val earnedOn = earnDates[id]
        val displayProgress = if (earnedOn != null) goal else progress.coerceAtMost(goal)
        return Badge(
            id = id,
            title = title,
            description = description,
            isEarned = earnedOn != null,
            progress = displayProgress,
            goal = goal,
            earnedOn = earnedOn
        )
    }
}
