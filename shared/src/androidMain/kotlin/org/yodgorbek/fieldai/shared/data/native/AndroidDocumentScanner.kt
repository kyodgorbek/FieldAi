package org.yodgorbek.fieldai.shared.data.native

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yodgorbek.fieldai.shared.domain.native.DocumentScanner

class AndroidDocumentScanner(
    private val context: Context
) : DocumentScanner {
    override suspend fun scanDocument(): String? {
        return suspendCancellableCoroutine { cont ->
            ScannerActivity.pendingContinuation = cont
            val intent = Intent(context, ScannerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            
            cont.invokeOnCancellation {
                ScannerActivity.pendingContinuation = null
            }
        }
    }
}
