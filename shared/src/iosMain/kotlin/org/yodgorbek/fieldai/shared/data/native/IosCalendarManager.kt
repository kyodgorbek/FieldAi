package org.yodgorbek.fieldai.shared.data.native

import org.yodgorbek.fieldai.shared.domain.native.CalendarEvent
import org.yodgorbek.fieldai.shared.domain.native.CalendarManager
import platform.EventKit.EKEventStore
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.dateWithTimeIntervalSince1970

class IosCalendarManager : CalendarManager {
    private val eventStore = EKEventStore()

    override suspend fun getEvents(startTimeMillis: Long, endTimeMillis: Long): List<CalendarEvent> {
        if (!hasCalendarPermission()) return emptyList()
        
        val startDate = NSDate.dateWithTimeIntervalSince1970(startTimeMillis / 1000.0)
        val endDate = NSDate.dateWithTimeIntervalSince1970(endTimeMillis / 1000.0)
        
        val predicate = eventStore.predicateForEventsWithStartDate(startDate, endDate, null)
        val events = eventStore.eventsMatchingPredicate(predicate) as? List<EKEvent> ?: emptyList()
        
        return events.map {
            CalendarEvent(
                id = it.eventIdentifier ?: "",
                title = it.title ?: "",
                description = it.notes,
                startTimeMillis = ((it.startDate?.timeIntervalSince1970 ?: 0.0) * 1000).toLong(),
                endTimeMillis = ((it.endDate?.timeIntervalSince1970 ?: 0.0) * 1000).toLong()
            )
        }
    }

    override fun hasCalendarPermission(): Boolean {
        return EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent) == EKAuthorizationStatusAuthorized
    }
}
