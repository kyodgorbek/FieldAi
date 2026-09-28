package org.yodgorbek.fieldai.shared.domain.native

interface PushNotificationManager {
    /**
     * Get the push notification token (FCM/APNs) for this device.
     */
    suspend fun getToken(): String?
    
    fun hasNotificationPermission(): Boolean
}
