package org.yodgorbek.fieldai.shared.data.native

import android.content.Context
import android.provider.CalendarContract
import org.yodgorbek.fieldai.shared.domain.native.CalendarEvent
import org.yodgorbek.fieldai.shared.domain.native.CalendarManager

class AndroidCalendarManager(
    private val context: Context
) : CalendarManager {
    override suspend fun getEvents(startTimeMillis: Long, endTimeMillis: Long): List<CalendarEvent> {
        if (!hasCalendarPermission()) return emptyList()
        
        val events = mutableListOf<CalendarEvent>()
        val contentResolver = context.contentResolver
        
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
        val selectionArgs = arrayOf(startTimeMillis.toString(), endTimeMillis.toString())
        
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
            val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
            val descIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
            
            if (idIndex != -1 && titleIndex != -1 && startIndex != -1 && endIndex != -1) {
                while (it.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            id = it.getString(idIndex),
                            title = it.getString(titleIndex) ?: "",
                            description = if (descIndex != -1) it.getString(descIndex) else null,
                            startTimeMillis = it.getLong(startIndex),
                            endTimeMillis = it.getLong(endIndex)
                        )
                    )
                }
            }
        }
        
        return events
    }

    override fun hasCalendarPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
