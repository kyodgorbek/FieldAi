package org.yodgorbek.fieldai.shared.data.native

import kotlinx.coroutines.suspendCancellableCoroutine
import org.yodgorbek.fieldai.shared.domain.native.PushNotificationManager
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class IosPushNotificationManager : PushNotificationManager {
    override suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            if (granted) {
                // Return a dummy token for now, real APNs token must be fetched from AppDelegate
                cont.resume("device_token_pending_delegate_apns")
            } else {
                cont.resume(null)
            }
        }
    }

    override fun hasNotificationPermission(): Boolean {
        return true 
    }
}
