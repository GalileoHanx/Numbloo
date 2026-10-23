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
import com.numbloo.mathfun.model.Level
import com.numbloo.mathfun.model.PipColors
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Rewards
import com.numbloo.mathfun.model.Unlock
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

    /** Pip speaks. [queue] lets the current sentence finish first. */
    fun say(text: String, queue: Boolean = false) {
        if (voiceOn) speaker.speak(text, queue)
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

    /** Clears stars, stickers, records and outfits. Keeps name, age and the unlock-all switch. */
    fun resetProgress(id: String) {
        update(id) {
            Profile(
                id = it.id,
                name = it.name,
                age = it.age,
                colorIndex = it.colorIndex % PipColors.FREE,
                createdAt = it.createdAt,
                allUnlocked = it.allUnlocked,
            )
        }
    }

    fun setAllUnlocked(id: String, on: Boolean) {
        update(id) { it.copy(allUnlocked = on) }
    }

    fun setAge(id: String, age: Int) {
        update(id) { it.copy(age = age.coerceIn(4, 10)) }
    }

    fun updateProfile(id: String, change: (Profile) -> Profile) = update(id, change)

    private fun update(id: String, change: (Profile) -> Profile) {
        val i = profiles.indexOfFirst { it.id == id }
        if (i >= 0) {
            profiles[i] = change(profiles[i])
            save()
        }
    }

    private fun save() = store.saveProfiles(profiles.toList())

    /** Saves a finished game and works out the rewards. [lessonId] is set for lesson practice. */
    fun recordResult(
        profileId: String,
        topic: Topic,
        level: Level,
        game: GameType,
        lessonId: String?,
        stars: Int,
        correctFirstTry: Int,
        total: Int,
    ): GameResult {
        val guided = lessonId != null
        val i = profiles.indexOfFirst { it.id == profileId }
        if (i < 0) return GameResult(topic, level, game, guided, stars, correctFirstTry, total, null, null, 0)
        val p = profiles[i]
        val s = p.stats(topic, level)

        val newTier = when {
            guided -> s.tier
            stars >= 3 -> min(3, s.tier + 1)
            stars <= 1 -> max(1, s.tier - 1)
            else -> s.tier
        }
        val best = if (guided) s.bestStars else s.bestStars + (game.id to max(stars, s.bestStars[game.id] ?: 0))
        val newStats = s.copy(
            tier = newTier,
            lessons = if (lessonId != null) s.lessons + lessonId else s.lessons,
            gamesPlayed = s.gamesPlayed + if (guided) 0 else 1,
            correct = s.correct + correctFirstTry,
            attempts = s.attempts + total,
            bestStars = best,
        )

        // A sticker (from this level's set) for finishing a lesson, or for 2+ stars in a game.
        val owned = p.stickers.toSet()
        val sticker = if (guided || stars >= 2) {
            Stickers.forLevel(level).filter { it.id !in owned }.randomOrNull()
        } else null

        val newTotal = p.totalStars + stars

        // 3 stars in a real game (not lesson practice) counts as a gold win (opens Pip's Closet).
        val newGold = p.goldWins + if (!guided && stars >= 3) 1 else 0

        val updated = p.withStats(topic, level, newStats).copy(
            goldWins = newGold,
            totalStars = newTotal,
            stickers = if (sticker != null) p.stickers + sticker.id else p.stickers,
            lastPlayed = System.currentTimeMillis(),
        )
        // Did this game open the next level of this operation?
        val next = level.next
        val levelUnlocked = if (next != null && !p.topicUnlocked(topic, next) && updated.topicUnlocked(topic, next)) next else null

        val opened = if (levelUnlocked != null) {
            updated.copy(unlockedKeys = updated.unlockedKeys + Profile.statsKey(topic, levelUnlocked))
        } else {
            updated
        }

        // New closet items and trophies won by this game.
        val newItems = Rewards.all.filter { it.unlock != Unlock.Free && !p.owns(it) && opened.owns(it) }
        val trophies = Rewards.trophies.filter { !p.meets(it) && opened.meets(it) }.map { Rewards.trophyName(it) }
        profiles[i] = opened.copy(ownedItems = opened.ownedItems + newItems.map { it.id })
        save()
        return GameResult(
            topic = topic,
            level = level,
            game = game,
            guided = guided,
            stars = stars,
            correctFirstTry = correctFirstTry,
            total = total,
            sticker = sticker,
            newHat = null,
            tierChange = newTier - s.tier,
            closetOpened = !p.closetOpen && opened.closetOpen,
            unlocked = newItems.map { "${it.label} (${it.shelf.title.substringAfter(' ')})" },
            levelUnlocked = levelUnlocked,
            trophies = trophies,
        )
    }

    fun shutdown() {
        speaker.shutdown()
        sfx.release()
    }
}
