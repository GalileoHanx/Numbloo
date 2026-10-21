package com.numbloo.mathfun

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.audio.SoundFx
import com.numbloo.mathfun.audio.Speaker
import com.numbloo.mathfun.data.ProfileStore
import com.numbloo.mathfun.model.GameResult
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Stickers
import com.numbloo.mathfun.model.Topic
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/** Everything the screens share: players, settings, voice and sounds. */
class AppState(context: Context) {

    private val store = ProfileStore(context)
    private val speaker = Speaker(context)
    private val sfx = SoundFx()

    val profiles = mutableStateListOf<Profile>().apply { addAll(store.loadProfiles()) }
    var currentId by mutableStateOf<String?>(null)
        private set
    var voiceOn by mutableStateOf(store.voiceOn)
        private set
    var soundOn by mutableStateOf(store.soundOn)
        private set

    val current: Profile? get() = profiles.firstOrNull { it.id == currentId }

    fun select(id: String?) {
        currentId = id
    }

    fun say(text: String) {
        if (voiceOn) speaker.speak(text)
    }

    fun hush() = speaker.stop()

    fun play(s: Sfx) {
        if (soundOn) sfx.play(s)
    }

    fun setVoice(on: Boolean) {
        voiceOn = on
        store.voiceOn = on
        if (!on) speaker.stop()
    }

    fun setSound(on: Boolean) {
        soundOn = on
        store.soundOn = on
    }

    fun addProfile(name: String, age: Int, colorIndex: Int): Profile {
        val p = Profile(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Player" },
            age = age,
            colorIndex = colorIndex,
        )
        profiles.add(p)
        save()
        return p
    }

    fun deleteProfile(id: String) {
        profiles.removeAll { it.id == id }
        if (currentId == id) currentId = null
        save()
    }

    fun resetProgress(id: String) {
        update(id) { it.copy(totalStars = 0, stickers = emptyList(), topics = emptyMap()) }
    }

    fun setAge(id: String, age: Int) {
        update(id) { it.copy(age = age.coerceIn(4, 10)) }
    }

    private fun update(id: String, change: (Profile) -> Profile) {
        val i = profiles.indexOfFirst { it.id == id }
        if (i >= 0) {
            profiles[i] = change(profiles[i])
            save()
        }
    }

    private fun save() = store.saveProfiles(profiles.toList())

    /** Saves a finished game and works out the rewards. */
    fun recordResult(
        profileId: String,
        topic: Topic,
        game: GameType,
        guided: Boolean,
        stars: Int,
        correctFirstTry: Int,
        total: Int,
    ): GameResult {
        val i = profiles.indexOfFirst { it.id == profileId }
        if (i < 0) return GameResult(topic, game, guided, stars, correctFirstTry, total, null, null, 0)
        val p = profiles[i]
        val s = p.stats(topic)

        val newTier = when {
            guided -> s.tier
            stars >= 3 -> min(3, s.tier + 1)
            stars <= 1 -> max(1, s.tier - 1)
            else -> s.tier
        }
        val key = if (guided) "lesson" else game.id
        val best = s.bestStars + (key to max(stars, s.bestStars[key] ?: 0))
        val newStats = s.copy(
            tier = newTier,
            lessonDone = s.lessonDone || guided,
            gamesPlayed = s.gamesPlayed + if (guided) 0 else 1,
            correct = s.correct + correctFirstTry,
            attempts = s.attempts + total,
            bestStars = best,
        )

        // A sticker for finishing a lesson, or for 2+ stars in a game.
        val owned = p.stickers.toSet()
        val sticker = if (guided || stars >= 2) {
            Stickers.all.filter { it.id !in owned }.randomOrNull()
        } else null

        val oldHat = Hat.forStars(p.totalStars)
        val newTotal = p.totalStars + stars
        val newHat = Hat.forStars(newTotal).takeIf { it != oldHat }

        profiles[i] = p.withStats(topic, newStats).copy(
            totalStars = newTotal,
            stickers = if (sticker != null) p.stickers + sticker.id else p.stickers,
            lastPlayed = System.currentTimeMillis(),
        )
        save()
        return GameResult(
            topic = topic,
            game = game,
            guided = guided,
            stars = stars,
            correctFirstTry = correctFirstTry,
            total = total,
            sticker = sticker,
            newHat = newHat,
            tierChange = newTier - s.tier,
        )
    }

    fun shutdown() {
        speaker.shutdown()
        sfx.release()
    }
}
