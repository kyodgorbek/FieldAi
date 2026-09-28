package org.yodgorbek.fieldai.shared.domain.native

interface DocumentScanner {
    /**
     * Launch the native document scanner (e.g., CameraX or VisionKit)
     * and return the captured image data as a ByteArray or a local URI string.
     */
    suspend fun scanDocument(): String?
}
