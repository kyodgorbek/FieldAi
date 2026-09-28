package org.yodgorbek.fieldai.shared.data.native

import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import org.yodgorbek.fieldai.shared.domain.native.PushNotificationManager

class AndroidPushNotificationManager(
    private val context: Context
) : PushNotificationManager {
    override suspend fun getToken(): String? {
        // Without adding com.google.firebase:firebase-messaging to build.gradle,
        // we cannot call FirebaseMessaging.getInstance().token.
        // For production, the module needs the Firebase SDK injected.
        return "firebase_token_pending_dependency"
    }

    override fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}
