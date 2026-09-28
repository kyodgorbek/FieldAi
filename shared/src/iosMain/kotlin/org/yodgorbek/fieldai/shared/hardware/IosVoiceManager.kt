package org.yodgorbek.fieldai.shared.hardware

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.yodgorbek.fieldai.shared.domain.hardware.VoiceManager

class IosVoiceManager : VoiceManager {

    private val _isListening = MutableStateFlow(false)
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()

    override fun startListening(): Flow<String> {
        _isListening.value = true
        // Simulate iOS voice recognition
        return kotlinx.coroutines.flow.flow {
            kotlinx.coroutines.delay(1000)
            emit("Simulated iOS voice transcription")
        }
    }

    override fun stopListening() {
        _isListening.value = false
    }
}
