package com.numbloo.mathfun.model

/** How a closet item is earned. */
sealed class Unlock {
    /** Ready as soon as Pip's Closet opens. */
    data object Free : Unlock()

    /** Collected stars: the best stars in every game added up (each game counts once, max 3). */
    data class Stars(val n: Int) : Unlock()

    /** Every star in one operation at one level, e.g. all of Beginner Addition. */
    data class OpLevel(val topic: Topic, val level: Level) : Unlock()

    /** Every star in all four operations of one level. */
    data class FullLevel(val level: Level) : Unlock()

    /** Every star in one operation at all three levels. */
    data class OpMaster(val topic: Topic) : Unlock()

    /** Every single star in the game. */
    data object Everything : Unlock()
}

enum class Shelf(val title: String) {
    COLOURS("🎨 Colours"),
    FLAGS("🚩 Flags"),
    HAIR("💇 Hair"),
    JOBS("👷 Jobs"),
    EYES("👀 Eyes"),
    MOUTH("👄 Mouth"),
    HATS("🎩 Hats"),
    GLASSES("🕶️ Glasses"),
    NECK("🎀 Neck"),
    PETS("🐾 Pets"),
}

/** One thing in Pip's Closet. [value] is the enum entry (or colour index) it puts on Pip. */
data class Reward(
    val id: String,
    val shelf: Shelf,
    val label: String,
    val unlock: Unlock,
    val value: Any,
)

/**
 * The reward ladder. Something new unlocks every few collected stars all the way
 * to the very last star, and every big achievement (an operation, a whole level,
 * one operation at every level, everything) wins a trophy prize.
 */
object Rewards {

    private fun stars(n: Int) = Unlock.Stars(n)

    /* ---------------------------------------------------- what unlocks what */

    private val colourRule: Map<Int, Unlock> = mapOf(
        6 to stars(6), 10 to stars(24), 11 to stars(36), 7 to stars(48), 12 to stars(65),
        14 to stars(90), 13 to stars(110), 15 to stars(160), 16 to stars(220), 9 to stars(250),
        8 to stars(280),
        17 to Unlock.FullLevel(Level.PRO),
        18 to Unlock.OpMaster(Topic.ADD),
        19 to Unlock.OpMaster(Topic.SUB),
        20 to Unlock.OpMaster(Topic.MUL),
        21 to Unlock.OpMaster(Topic.DIV),
        PipColors.RAINBOW to Unlock.Everything,
    )

    private val flagRule: Map<Flag, Unlock> = mapOf(
        Flag.EGYPT to stars(15), Flag.JORDAN to stars(21), Flag.MOROCCO to stars(27), Flag.IRAQ to stars(33),
        Flag.LEBANON to stars(40), Flag.PALESTINE to stars(44), Flag.TURKEY to stars(52), Flag.INDIA to stars(60),
        Flag.PAKISTAN to stars(70), Flag.INDONESIA to stars(80), Flag.JAPAN to stars(95), Flag.USA to stars(105),
        Flag.FRANCE to stars(120), Flag.ITALY to stars(145), Flag.GERMANY to stars(175), Flag.ARGENTINA to stars(205),
    )

    private val hairRule: Map<Hair, Unlock> = mapOf(
        Hair.SPIKY to stars(3), Hair.CURLY to stars(9), Hair.PIGTAILS to stars(18), Hair.MOHAWK to stars(40),
        Hair.SWOOP to stars(56), Hair.LONG to stars(85), Hair.BUN to stars(115), Hair.AFRO to stars(190),
        Hair.RAINBOW to stars(295),
    )

    private val jobRule: Map<Job, Unlock> = mapOf(
        Job.CHEF to stars(12), Job.DOCTOR to stars(21), Job.FIREFIGHTER to stars(30), Job.BUILDER to stars(48),
        Job.PILOT to stars(65), Job.ASTRONAUT to stars(80), Job.POLICE to stars(100), Job.ARTIST to stars(120),
        Job.FARMER to stars(175), Job.KNIGHT to stars(235), Job.DETECTIVE to stars(265), Job.DIVER to stars(295),
    )

    private val eyesRule: Map<Eyes, Unlock> = mapOf(
        Eyes.SPARKLY to stars(6), Eyes.SLEEPY to stars(18), Eyes.WINK to stars(30), Eyes.HEARTS to stars(52),
        Eyes.STARRY to stars(75), Eyes.LASHES to stars(105), Eyes.GOOGLY to stars(130), Eyes.ROBOT to stars(205),
        Eyes.SPIRAL to stars(265),
    )

    private val mouthRule: Map<Mouth, Unlock> = mapOf(
        Mouth.TOOTHY to stars(12), Mouth.TONGUE to stars(24), Mouth.MUSTACHE to stars(44), Mouth.FANGS to stars(60),
        Mouth.CAT to stars(95), Mouth.BUNNY to stars(130), Mouth.GRIN to stars(220), Mouth.BEARD to stars(280),
    )

    private val hatRule: Map<Hat, Unlock> = mapOf(
        Hat.PARTY to stars(15), Hat.CROWN to stars(33), Hat.WIZARD to stars(56), Hat.COWBOY to stars(75),
        Hat.PROPELLER to stars(90), Hat.PIRATE to stars(110), Hat.BEANIE to stars(160), Hat.VIKING to stars(235),
        Hat.GRAD_CAP to Unlock.FullLevel(Level.BEGINNER),
        Hat.TOP_HAT to Unlock.FullLevel(Level.INTERMEDIATE),
        // Ghutra, shemagh and hijab are part of who you are, so they are free.
    )

    private val glassesRule: Map<Glasses, Unlock> = mapOf(
        Glasses.SHADES to stars(9), Glasses.SPECS to stars(36), Glasses.STARS to stars(70),
        Glasses.HEART_SHADES to stars(100), Glasses.GOGGLES to stars(145), Glasses.MONOCLE to stars(250),
    )

    private val neckRule: Map<Neck, Unlock> = mapOf(
        Neck.BOW_TIE to stars(3), Neck.SCARF to stars(27), Neck.TIE to stars(85), Neck.BANDANA to stars(115),
        Neck.LEI to stars(190),
        Neck.BRONZE_MEDAL to Unlock.FullLevel(Level.BEGINNER),
        Neck.SILVER_MEDAL to Unlock.FullLevel(Level.INTERMEDIATE),
        Neck.GOLD_MEDAL to Unlock.FullLevel(Level.PRO),
        Neck.CAPE to Unlock.Everything,
    )

    private val petRule: Map<Pet, Unlock> = mapOf(
        Pet.PUPPY to Unlock.OpLevel(Topic.ADD, Level.BEGINNER),
        Pet.KITTEN to Unlock.OpLevel(Topic.SUB, Level.BEGINNER),
        Pet.BUNNY to Unlock.OpLevel(Topic.MUL, Level.BEGINNER),
        Pet.CHICK to Unlock.OpLevel(Topic.DIV, Level.BEGINNER),
        Pet.TURTLE to Unlock.OpLevel(Topic.ADD, Level.INTERMEDIATE),
        Pet.FOX to Unlock.OpLevel(Topic.SUB, Level.INTERMEDIATE),
        Pet.PANDA to Unlock.OpLevel(Topic.MUL, Level.INTERMEDIATE),
        Pet.PENGUIN to Unlock.OpLevel(Topic.DIV, Level.INTERMEDIATE),
        Pet.DINO to Unlock.OpLevel(Topic.ADD, Level.PRO),
        Pet.OWL to Unlock.OpLevel(Topic.SUB, Level.PRO),
        Pet.UNICORN to Unlock.OpLevel(Topic.MUL, Level.PRO),
        Pet.DRAGON to Unlock.OpLevel(Topic.DIV, Level.PRO),
    )

    /* ---------------------------------------------------- the catalogue */

    val all: List<Reward> by lazy {
        val out = ArrayList<Reward>()
        PipColors.names.forEachIndexed { i, name ->
            out.add(Reward("color_$i", Shelf.COLOURS, name, colourRule[i] ?: Unlock.Free, i))
        }
        Flag.entries.forEach { out.add(Reward(flagId(it), Shelf.FLAGS, it.label, flagRule[it] ?: Unlock.Free, it)) }
        Hair.entries.forEach { out.add(Reward(hairId(it), Shelf.HAIR, it.label, hairRule[it] ?: Unlock.Free, it)) }
        Job.entries.forEach { out.add(Reward(jobId(it), Shelf.JOBS, it.label, jobRule[it] ?: Unlock.Free, it)) }
        Eyes.entries.forEach { out.add(Reward(eyesId(it), Shelf.EYES, it.label, eyesRule[it] ?: Unlock.Free, it)) }
        Mouth.entries.forEach { out.add(Reward(mouthId(it), Shelf.MOUTH, it.label, mouthRule[it] ?: Unlock.Free, it)) }
        Hat.entries.forEach { out.add(Reward(hatId(it), Shelf.HATS, it.label, hatRule[it] ?: Unlock.Free, it)) }
        Glasses.entries.forEach { out.add(Reward(glassesId(it), Shelf.GLASSES, it.label, glassesRule[it] ?: Unlock.Free, it)) }
        Neck.entries.forEach { out.add(Reward(neckId(it), Shelf.NECK, it.label, neckRule[it] ?: Unlock.Free, it)) }
        Pet.entries.forEach { out.add(Reward(petId(it), Shelf.PETS, it.label, petRule[it] ?: Unlock.Free, it)) }
        out
    }

    private val byId: Map<String, Reward> by lazy { all.associateBy { it.id } }

    fun byId(id: String): Reward? = byId[id]

    fun flagId(f: Flag) = "flag_" + f.name
    fun hairId(h: Hair) = "hair_" + h.name
    fun jobId(j: Job) = "job_" + j.name
    fun eyesId(e: Eyes) = "eyes_" + e.name
    fun mouthId(m: Mouth) = "mouth_" + m.name
    fun hatId(h: Hat) = "hat_" + h.name
    fun glassesId(g: Glasses) = "glasses_" + g.name
    fun neckId(n: Neck) = "neck_" + n.name
    fun petId(p: Pet) = "pet_" + p.name

    fun colour(i: Int): Reward = byId.getValue("color_$i")
    fun hat(h: Hat): Reward = byId.getValue(hatId(h))

    /** Hats Pip puts on by himself until the child picks one. */
    val autoHats: List<Hat> = listOf(Hat.PARTY, Hat.CROWN, Hat.WIZARD, Hat.PROPELLER)

    fun onShelf(shelf: Shelf): List<Reward> = all.filter { it.shelf == shelf }

    /** Items that need collected stars, smallest first. */
    val starLadder: List<Pair<Reward, Int>> by lazy {
        all.mapNotNull { r -> (r.unlock as? Unlock.Stars)?.let { r to it.n } }.sortedBy { it.second }
    }

    /** The next items a player can win with stars (all items at the next star number). */
    fun nextByStars(collected: Int): Pair<Int, List<Reward>>? {
        val next = starLadder.firstOrNull { it.second > collected }?.second ?: return null
        return next to starLadder.filter { it.second == next }.map { it.first }
    }

    /* ---------------------------------------------------- trophies */

    /** Every big achievement, in the order kids usually reach them. */
    val trophies: List<Unlock> by lazy {
        val out = ArrayList<Unlock>()
        for (lv in Level.entries) {
            Topic.entries.forEach { out.add(Unlock.OpLevel(it, lv)) }
            out.add(Unlock.FullLevel(lv))
        }
        Topic.entries.forEach { out.add(Unlock.OpMaster(it)) }
        out.add(Unlock.Everything)
        out
    }

    fun trophyName(u: Unlock): String = when (u) {
        is Unlock.OpLevel -> "${u.level.title} ${u.topic.title} Champion"
        is Unlock.FullLevel -> "${u.level.title} Superstar"
        is Unlock.OpMaster -> "${u.topic.title} Master"
        Unlock.Everything -> "Numbloo Legend"
        is Unlock.Stars -> "${u.n} stars"
        Unlock.Free -> "Free"
    }

    fun trophyGoal(u: Unlock): String = when (u) {
        is Unlock.OpLevel -> "Every ⭐ in ${u.level.title} ${u.topic.title}"
        is Unlock.FullLevel -> "Every ⭐ in all of ${u.level.title}"
        is Unlock.OpMaster -> "Every ⭐ in ${u.topic.title} at all 3 levels"
        Unlock.Everything -> "Every ⭐ in the whole game"
        is Unlock.Stars -> "Collect ${u.n} ⭐"
        Unlock.Free -> ""
    }

    fun prizesFor(u: Unlock): List<Reward> = all.filter { it.unlock == u }

    /** (stars so far, stars needed) for a trophy. */
    fun trophyProgress(p: Profile, u: Unlock): Pair<Int, Int> = when (u) {
        is Unlock.OpLevel -> p.topicStars(u.topic, u.level) to Curriculum.maxStars(u.topic, u.level)
        is Unlock.FullLevel -> p.levelStars(u.level) to Topic.entries.sumOf { Curriculum.maxStars(it, u.level) }
        is Unlock.OpMaster -> Level.entries.sumOf { p.topicStars(u.topic, it) } to Level.entries.sumOf { Curriculum.maxStars(u.topic, it) }
        Unlock.Everything -> p.collectedStars() to totalStars()
        is Unlock.Stars -> p.collectedStars() to u.n
        Unlock.Free -> 0 to 0
    }

    /** Every star in the game. */
    fun totalStars(): Int = Level.entries.sumOf { lv -> Topic.entries.sumOf { Curriculum.maxStars(it, lv) } }

    /** Short "how to get it" text for a locked item. */
    fun lockText(p: Profile, r: Reward): String = when (val u = r.unlock) {
        is Unlock.Stars -> "${u.n - p.collectedStars()} more ⭐"
        Unlock.Free -> ""
        else -> trophyGoal(u)
    }

    /* ---------------------------------------------------- saves from v0.4 */

    /**
     * Items a player had already unlocked under the old rules (3-star wins and total
     * stars), so nothing they earned before disappears after updating.
     */
    fun legacyOwned(goldWins: Int, totalStars: Int): Set<String> {
        val out = HashSet<String>()
        val g = goldWins
        if (g >= 1) {
            listOf(
                Flag.SAUDI, Flag.UAE, Flag.KUWAIT, Flag.QATAR, Flag.BAHRAIN, Flag.OMAN,
                Flag.PHILIPPINES, Flag.PORTUGAL, Flag.ENGLAND, Flag.SPAIN, Flag.BRAZIL,
            ).forEach { out.add(flagId(it)) }
        }
        mapOf(Glasses.SHADES to 2, Glasses.SPECS to 5, Glasses.STARS to 8)
            .forEach { (k, v) -> if (g >= v) out.add(glassesId(k)) }
        mapOf(Neck.BOW_TIE to 1, Neck.SCARF to 4).forEach { (k, v) -> if (g >= v) out.add(neckId(k)) }
        mapOf(Hair.SPIKY to 1, Hair.CURLY to 2, Hair.PIGTAILS to 3, Hair.MOHAWK to 5, Hair.SWOOP to 7)
            .forEach { (k, v) -> if (g >= v) out.add(hairId(k)) }
        mapOf(Job.CHEF to 2, Job.DOCTOR to 3, Job.FIREFIGHTER to 4, Job.BUILDER to 6, Job.PILOT to 8, Job.ASTRONAUT to 10)
            .forEach { (k, v) -> if (g >= v) out.add(jobId(k)) }
        mapOf(Eyes.SPARKLY to 1, Eyes.SLEEPY to 3, Eyes.WINK to 4, Eyes.HEARTS to 6, Eyes.STARRY to 9)
            .forEach { (k, v) -> if (g >= v) out.add(eyesId(k)) }
        mapOf(Mouth.TOOTHY to 2, Mouth.TONGUE to 3, Mouth.MUSTACHE to 5, Mouth.FANGS to 7)
            .forEach { (k, v) -> if (g >= v) out.add(mouthId(k)) }
        mapOf(6 to 3, 7 to 6, 8 to 10, 9 to 12).forEach { (k, v) -> if (g >= v) out.add("color_$k") }
        mapOf(Hat.PARTY to 5, Hat.CROWN to 15, Hat.WIZARD to 30, Hat.PROPELLER to 50)
            .forEach { (k, v) -> if (totalStars >= v) out.add(hatId(k)) }
        return out
    }
}
