package com.numbloo.mathfun.data

import android.content.Context
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.TopicStats
import org.json.JSONArray
import org.json.JSONObject

/** Saves everything on the phone only (SharedPreferences + JSON). Nothing leaves the device. */
class ProfileStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("numbloo", Context.MODE_PRIVATE)

    var voiceOn: Boolean
        get() = prefs.getBoolean(KEY_VOICE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_VOICE, value).apply()
        }

    var soundOn: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND, value).apply()
        }

    fun loadProfiles(): List<Profile> {
        val raw = prefs.getString(KEY_PROFILES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<Profile>()
            for (i in 0 until arr.length()) {
                val p = parseProfile(arr.getJSONObject(i))
                if (p != null) out.add(p)
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProfiles(list: List<Profile>) {
        val arr = JSONArray()
        list.forEach { arr.put(profileToJson(it)) }
        prefs.edit().putString(KEY_PROFILES, arr.toString()).apply()
    }

    private fun profileToJson(p: Profile): JSONObject {
        val o = JSONObject()
        o.put("id", p.id)
        o.put("name", p.name)
        o.put("age", p.age)
        o.put("color", p.colorIndex)
        o.put("stars", p.totalStars)
        val st = JSONArray()
        p.stickers.forEach { st.put(it) }
        o.put("stickers", st)
        val topics = JSONObject()
        for ((key, s) in p.topics) topics.put(key, statsToJson(s))
        o.put("topics", topics)
        o.put("created", p.createdAt)
        o.put("last", p.lastPlayed)
        return o
    }

    private fun statsToJson(s: TopicStats): JSONObject {
        val o = JSONObject()
        o.put("tier", s.tier)
        o.put("lesson", s.lessonDone)
        o.put("played", s.gamesPlayed)
        o.put("correct", s.correct)
        o.put("attempts", s.attempts)
        val best = JSONObject()
        for ((k, v) in s.bestStars) best.put(k, v)
        o.put("best", best)
        return o
    }

    private fun parseProfile(o: JSONObject): Profile? {
        val id = o.optString("id", "")
        if (id.isEmpty()) return null
        val stickers = ArrayList<String>()
        val st = o.optJSONArray("stickers")
        if (st != null) for (i in 0 until st.length()) stickers.add(st.optString(i))
        val topics = HashMap<String, TopicStats>()
        val t = o.optJSONObject("topics")
        if (t != null) {
            val keys = t.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val so = t.optJSONObject(k) ?: continue
                topics[k] = parseStats(so)
            }
        }
        return Profile(
            id = id,
            name = o.optString("name", "Player"),
            age = o.optInt("age", 6),
            colorIndex = o.optInt("color", 0),
            totalStars = o.optInt("stars", 0),
            stickers = stickers,
            topics = topics,
            createdAt = o.optLong("created", System.currentTimeMillis()),
            lastPlayed = o.optLong("last", System.currentTimeMillis()),
        )
    }

    private fun parseStats(o: JSONObject): TopicStats {
        val best = HashMap<String, Int>()
        val b = o.optJSONObject("best")
        if (b != null) {
            val keys = b.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                best[k] = b.optInt(k, 0)
            }
        }
        return TopicStats(
            tier = o.optInt("tier", 1).coerceIn(1, 3),
            lessonDone = o.optBoolean("lesson", false),
            gamesPlayed = o.optInt("played", 0),
            correct = o.optInt("correct", 0),
            attempts = o.optInt("attempts", 0),
            bestStars = best,
        )
    }

    private companion object {
        const val KEY_PROFILES = "profiles"
        const val KEY_VOICE = "voiceOn"
        const val KEY_SOUND = "soundOn"
    }
}
