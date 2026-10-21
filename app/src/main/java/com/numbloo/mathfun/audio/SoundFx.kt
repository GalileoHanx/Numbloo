package com.numbloo.mathfun.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class Sfx { TAP, CORRECT, WRONG, POP, HOP, CHOMP, CELEBRATE, WHOOSH }

/**
 * Cartoon sound effects made with code (no sound files, no internet).
 * Each sound is synthesised once into a small buffer and played with AudioTrack.
 */
class SoundFx {

    private val rate = 22050
    private val pool: ExecutorService = Executors.newFixedThreadPool(3)
    private val cache = ConcurrentHashMap<Sfx, ShortArray>()

    private enum class Wave { SINE, SQUARE, TRIANGLE }

    init {
        pool.execute {
            for (s in Sfx.entries) cache[s] = render(s)
        }
    }

    fun play(s: Sfx) {
        try {
            pool.execute { playNow(s) }
        } catch (e: Exception) {
            // pool shut down
        }
    }

    fun release() {
        pool.shutdownNow()
    }

    private fun playNow(s: Sfx) {
        val data = cache[s] ?: render(s).also { cache[s] = it }
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(data, 0, data.size)
            track.play()
            Thread.sleep(data.size * 1000L / rate + 60L)
        } catch (e: Exception) {
            // sound is optional
        } finally {
            try {
                track?.release()
            } catch (e: Exception) {
            }
        }
    }

    private fun render(s: Sfx): ShortArray {
        val parts: List<FloatArray> = when (s) {
            Sfx.TAP -> listOf(tone(880f, 1100f, 45, 0.30f, Wave.SINE))
            Sfx.CORRECT -> listOf(
                tone(784f, 784f, 80, 0.32f, Wave.TRIANGLE),
                tone(988f, 988f, 80, 0.32f, Wave.TRIANGLE),
                tone(1319f, 1319f, 200, 0.32f, Wave.TRIANGLE),
            )
            Sfx.WRONG -> listOf(tone(330f, 160f, 380, 0.22f, Wave.SQUARE, vibrato = 14f))
            Sfx.POP -> listOf(noise(35, 0.45f), tone(1500f, 500f, 70, 0.28f, Wave.SINE))
            Sfx.HOP -> listOf(tone(280f, 780f, 120, 0.32f, Wave.TRIANGLE))
            Sfx.CHOMP -> listOf(
                tone(190f, 120f, 70, 0.25f, Wave.SQUARE),
                silence(45),
                tone(210f, 130f, 70, 0.25f, Wave.SQUARE),
            )
            Sfx.CELEBRATE -> listOf(
                tone(523f, 523f, 90, 0.3f, Wave.TRIANGLE),
                tone(659f, 659f, 90, 0.3f, Wave.TRIANGLE),
                tone(784f, 784f, 90, 0.3f, Wave.TRIANGLE),
                tone(1047f, 1047f, 140, 0.3f, Wave.TRIANGLE),
                tone(784f, 784f, 70, 0.25f, Wave.TRIANGLE),
                tone(1047f, 1319f, 320, 0.3f, Wave.TRIANGLE, vibrato = 6f),
            )
            Sfx.WHOOSH -> listOf(noise(220, 0.25f))
        }
        val total = parts.sumOf { it.size }
        val out = ShortArray(total)
        var i = 0
        for (p in parts) for (v in p) {
            out[i++] = (v.coerceIn(-1f, 1f) * 32767f).toInt().toShort()
        }
        return out
    }

    private fun tone(
        f0: Float,
        f1: Float,
        ms: Int,
        vol: Float,
        wave: Wave,
        vibrato: Float = 0f,
    ): FloatArray {
        val n = rate * ms / 1000
        val out = FloatArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toFloat() / n
            var f = f0 + (f1 - f0) * t
            if (vibrato > 0f) {
                f *= (1.0 + 0.08 * sin(2.0 * PI * vibrato * i / rate)).toFloat()
            }
            phase += f.toDouble() / rate
            val p = phase - kotlin.math.floor(phase)
            val v: Double = when (wave) {
                Wave.SINE -> sin(2.0 * PI * p)
                Wave.SQUARE -> if (p < 0.5) 0.6 else -0.6
                Wave.TRIANGLE -> 4.0 * abs(p - 0.5) - 1.0
            }
            val attack = min(1f, i / (rate * 0.005f))
            val release = if (t > 0.7f) (1f - t) / 0.3f else 1f
            out[i] = (v * vol * attack * release).toFloat()
        }
        return out
    }

    private fun noise(ms: Int, vol: Float): FloatArray {
        val n = rate * ms / 1000
        return FloatArray(n) { i ->
            val t = i.toFloat() / n
            (Random.nextFloat() * 2f - 1f) * vol * (1f - t)
        }
    }

    private fun silence(ms: Int): FloatArray = FloatArray(rate * ms / 1000)
}
