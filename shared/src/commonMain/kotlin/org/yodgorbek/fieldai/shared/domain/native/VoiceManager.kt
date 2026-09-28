package org.yodgorbek.fieldai.shared.domain.native

import kotlinx.coroutines.flow.Flow

interface VoiceManager {
    /**
     * Start listening to voice input and return partial/final results as a flow of Strings.
     */
    fun startListening(): Flow<String>
    
    /**
     * Stop listening to voice input.
     */
    fun stopListening()
    
    fun hasVoicePermission(): Boolean
}
