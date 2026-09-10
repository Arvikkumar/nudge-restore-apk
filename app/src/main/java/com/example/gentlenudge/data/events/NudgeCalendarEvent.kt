package com.example.gentlenudge.data.events

import androidx.compose.ui.graphics.Color

/**
 * Categories for calendar events and observances.
 */
enum class EventCategory(
    val displayName: String,
    val bgLight: Long,
    val textLight: Long,
    val badgeBgLight: Long,
    val badgeTextLight: Long
) {
    HINDU_FESTIVAL(
        displayName = "Hindu Festival",
        bgLight = 0xFFFFF8EC, // Soft warm saffron / cream
        textLight = 0xFF4E2600, // Dark warm brown for high contrast
        badgeBgLight = 0xFFFFE4BF,
        badgeTextLight = 0xFF4E2600
    ),
    NATIONAL_DAY(
        displayName = "National Day",
        bgLight = 0xFFEFF4FC, // Soft light blue
        textLight = 0xFF0E2554, // Dark navy text
        badgeBgLight = 0xFFD7E4FB,
        badgeTextLight = 0xFF0E2554
    ),
    CULTURAL_EVENT(
        displayName = "Cultural Event",
        bgLight = 0xFFF1F8F3, // Soft light green
        textLight = 0xFF0E3F1F, // Dark forest green text
        badgeBgLight = 0xFFD4EED8,
        badgeTextLight = 0xFF0E3F1F
    ),
    INTERNATIONAL_DAY(
        displayName = "International Observance",
        bgLight = 0xFFF0F9FB, // Soft light cyan / blue
        textLight = 0xFF053F4D, // Dark teal / blue text
        badgeBgLight = 0xFFD2EFF5,
        badgeTextLight = 0xFF053F4D
    ),
    RELIGIOUS_FESTIVAL(
        displayName = "Religious Observance",
        bgLight = 0xFFF6F0FC, // Soft light purple / lavender
        textLight = 0xFF3B1266, // Dark purple text
        badgeBgLight = 0xFFE8DBFA,
        badgeTextLight = 0xFF3B1266
    ),
    AWARENESS_DAY(
        displayName = "Awareness Day",
        bgLight = 0xFFFFF2F4, // Soft rose / coral
        textLight = 0xFF5C1021, // Dark rose text
        badgeBgLight = 0xFFFFDBE1,
        badgeTextLight = 0xFF5C1021
    ),
    PUBLIC_HOLIDAY(
        displayName = "Public Holiday",
        bgLight = 0xFFF4F6F8, // Soft slate / neutral gray
        textLight = 0xFF1F2937, // Dark slate text
        badgeBgLight = 0xFFE2E8F0,
        badgeTextLight = 0xFF1F2937
    );

    val lightBackgroundColor: Color get() = Color(bgLight)
    val lightTextColor: Color get() = Color(textLight)
    val badgeBackgroundColor: Color get() = Color(badgeBgLight)
    val badgeTextColor: Color get() = Color(badgeTextLight)
}

enum class DateBasis(val displayName: String) {
    FIXED_GREGORIAN("Fixed Gregorian Date"),
    SOLAR_CALENDAR("Solar Calendar (Sankranti / Sauramana)"),
    HINDU_LUNAR("Hindu Lunisolar (Tithi / Chandramana)"),
    ISLAMIC_LUNAR("Islamic Lunar Calendar"),
    REGIONAL_CALENDAR("Regional Calendar"),
    ASTRONOMICAL_CALCULATED("Astronomical Calculation")
}

/**
 * Importance / prominence level for an event.
 */
enum class EventImportance(val displayName: String) {
    MAJOR("Major Festival / Holiday"),
    STANDARD("Important Observance"),
    REGIONAL("Regional / Cultural Observance")
}

/**
 * Immutable structured model for calendar events and observances.
 */
data class NudgeCalendarEvent(
    val id: String,
    val name: String,
    val month: Int, // 1-12 (1 = January, 12 = December)
    val day: Int, // 1-31
    val year: Int? = null, // null for annual recurring events, specific year for lunisolar/movable events
    val category: EventCategory,
    val description: String,
    val traditionOrRegion: String? = null,
    val importance: EventImportance = EventImportance.STANDARD,
    val dateBasis: DateBasis = if (year == null) DateBasis.FIXED_GREGORIAN else DateBasis.HINDU_LUNAR,
    val region: String? = traditionOrRegion,
    val religionOrTradition: String? = traditionOrRegion,
    val regionalVariationPossible: Boolean = false,
    val calculationBasisOrSource: String? = null
) {
    /**
     * Checks if this event matches the given year, month (1-12), and day (1-31).
     */
    fun matchesDate(targetYear: Int, targetMonth: Int, targetDay: Int): Boolean {
        if (this.month != targetMonth || this.day != targetDay) {
            return false
        }
        return this.year == null || this.year == targetYear
    }
}
