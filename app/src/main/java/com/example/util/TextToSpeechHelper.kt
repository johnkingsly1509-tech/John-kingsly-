package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.StudyLanguage
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "TextToSpeechHelper"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(0.95f)
        } else {
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    fun speak(text: String, language: StudyLanguage) {
        if (!isInitialized || tts == null) return
        stop()

        val locale = when (language) {
            StudyLanguage.TAMIL -> Locale.forLanguageTag("ta-IN")
            StudyLanguage.ENGLISH, StudyLanguage.FORMAL_ENGLISH -> Locale.ENGLISH
            StudyLanguage.BILINGUAL -> {
                // If contains Tamil unicode characters, try Tamil locale, else English
                val hasTamil = text.any { it in '\u0B80'..'\u0BFF' }
                if (hasTamil) Locale.forLanguageTag("ta-IN") else Locale.ENGLISH
            }
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if Tamil language pack is not installed on device
                tts?.setLanguage(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            tts?.setLanguage(Locale.ENGLISH)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "study_master_tts")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
