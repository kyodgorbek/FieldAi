package org.yodgorbek.fieldai.shared.data.native

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class ScannerActivity : ComponentActivity() {
    companion object {
        var pendingContinuation: CancellableContinuation<String?>? = null
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(cacheDir, "scanned_doc_${System.currentTimeMillis()}.png")
                val fos = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                pendingContinuation?.takeIf { it.isActive }?.resume(file.absolutePath)
            } catch (e: Exception) {
                pendingContinuation?.takeIf { it.isActive }?.resume(null)
            }
        } else {
            pendingContinuation?.takeIf { it.isActive }?.resume(null)
        }
        pendingContinuation = null
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (pendingContinuation != null) {
            takePictureLauncher.launch(null)
        } else {
            finish()
        }
    }
}
