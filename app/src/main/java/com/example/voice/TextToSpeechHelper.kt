package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    var isVoiceEnabled: Boolean = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            } else {
                isInitialized = true
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(0.95f) // Slightly deeper futuristic tone
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isVoiceEnabled || !isInitialized) return

        val cleanText = text.replace(Regex("[*#_~`]"), "") // Strip markdown symbols
        
        // Auto-detect Devanagari characters (Hindi/Marathi) vs English
        val hasDevanagari = cleanText.any { it.code in 0x0900..0x097F }
        if (hasDevanagari) {
            val hiLocale = Locale("hi", "IN")
            val res = tts?.setLanguage(hiLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("mr", "IN"))
            }
        } else {
            tts?.setLanguage(Locale.US)
        }

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onComplete?.invoke()
            }
            override fun onError(utteranceId: String?) {
                onComplete?.invoke()
            }
        })

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
