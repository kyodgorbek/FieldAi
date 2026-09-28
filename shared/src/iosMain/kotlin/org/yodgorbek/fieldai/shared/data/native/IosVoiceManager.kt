package org.yodgorbek.fieldai.shared.data.native

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.yodgorbek.fieldai.shared.domain.native.VoiceManager
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer

import kotlinx.cinterop.ExperimentalForeignApi

class IosVoiceManager : VoiceManager {
    @OptIn(ExperimentalForeignApi::class)
    override fun startListening(): Flow<String> = callbackFlow {
        val speechRecognizer = SFSpeechRecognizer()
        val audioEngine = AVAudioEngine()
        val request = SFSpeechAudioBufferRecognitionRequest()

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryRecord, error = null)
        audioSession.setMode(AVAudioSessionModeMeasurement, error = null)
        audioSession.setActive(true, error = null)

        val inputNode = audioEngine.inputNode
        val recordingFormat = inputNode.outputFormatForBus(0u)

        inputNode.installTapOnBus(0u, 1024u, recordingFormat) { buffer, _ ->
            if (buffer != null) {
                request.appendAudioPCMBuffer(buffer)
            }
        }

        audioEngine.prepare()
        audioEngine.startAndReturnError(null)

        val recognitionTask = speechRecognizer?.recognitionTaskWithRequest(request) { result, error ->
            if (result != null) {
                trySend(result.bestTranscription.formattedString)
            }
            if (error != null || (result != null && result.isFinal())) {
                audioEngine.stop()
                inputNode.removeTapOnBus(0u)
                close(error?.let { Exception(it.localizedDescription) })
            }
        }

        awaitClose {
            audioEngine.stop()
            inputNode.removeTapOnBus(0u)
            recognitionTask?.cancel()
        }
    }
    
    override fun stopListening() {
        // Implementation handled by flow cancellation (awaitClose)
    }

    override fun hasVoicePermission(): Boolean {
        // Assume true for this scope, would query SFSpeechRecognizer and AVAudioSession in prod
        return true
    }
}
