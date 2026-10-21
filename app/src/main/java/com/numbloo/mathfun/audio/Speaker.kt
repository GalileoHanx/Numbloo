package com.numbloo.mathfun.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Reads text aloud with the phone's own text-to-speech engine.
 * English voices are stored on the phone, so this works without internet.
 * If no voice is available the app simply stays quiet.
 */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    init {
        tts = try {
            TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            null
        }
    }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        ready = try {
            var res = engine.setLanguage(Locale.US)
            if (res < TextToSpeech.LANG_AVAILABLE) res = engine.setLanguage(Locale.ENGLISH)
            res >= TextToSpeech.LANG_AVAILABLE
        } catch (e: Exception) {
            false
        }
        if (ready) {
            engine.setSpeechRate(0.9f)
            engine.setPitch(1.25f) // a little squeaky, like Pip
            val p = pending
            pending = null
            if (p != null) speak(p)
        }
    }

    fun speak(text: String) {
        val engine = tts
        if (engine == null || !ready) {
            pending = text
            return
        }
        try {
            engine.speak(clean(text), TextToSpeech.QUEUE_FLUSH, null, "numbloo-" + System.nanoTime())
        } catch (e: Exception) {
            // ignore, voice is a bonus
        }
    }

    fun stop() {
        pending = null
        try {
            tts?.stop()
        } catch (e: Exception) {
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
        }
        tts = null
        ready = false
    }

    private fun clean(text: String): String =
        text.replace("×", " times ")
            .replace("÷", " divided by ")
            .replace("−", " minus ")
            .replace("+", " plus ")
            .replace("=", " equals ")
            .replace(EMOJI, "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private companion object {
        // Emoji and other pictographs should not be read out loud.
        val EMOJI = Regex("[\\p{So}\\p{Sk}\\p{Cs}\\uFE0F\\u200D]")
    }
}
