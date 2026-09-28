package org.yodgorbek.fieldai.shared.domain.native

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long
)

interface CalendarManager {
    /**
     * Get calendar events within a time range.
     */
    suspend fun getEvents(startTimeMillis: Long, endTimeMillis: Long): List<CalendarEvent>
    
    fun hasCalendarPermission(): Boolean
}
