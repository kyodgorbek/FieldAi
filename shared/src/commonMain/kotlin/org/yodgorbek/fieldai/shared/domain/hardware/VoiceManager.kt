package org.yodgorbek.fieldai.shared.domain.hardware

import kotlinx.coroutines.flow.Flow

interface VoiceManager {
    fun startListening(): Flow<String>
    fun stopListening()
    val isListening: Flow<Boolean>
}
