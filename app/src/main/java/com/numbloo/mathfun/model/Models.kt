package com.numbloo.mathfun.model

import androidx.compose.ui.graphics.Color

/** The four beginner topics. */
enum class Topic(
    val id: String,
    val title: String,
    val symbol: String,
    val spokenOp: String,
    val emoji: String,
    val color: Color,
    val tagline: String,
) {
    ADD("add", "Addition", "+", "plus", "🍎", Color(0xFFFF8A3D), "Adding means putting things together!"),
    SUB("sub", "Subtraction", "−", "take away", "🐸", Color(0xFF2EA8F5), "Subtracting means taking some away!"),
    MUL("mul", "Multiplication", "×", "times", "🍪", Color(0xFFA25CF0), "Multiplying means equal groups!"),
    DIV("div", "Division", "÷", "divided by", "🍕", Color(0xFFF2557F), "Dividing means sharing fairly!");

    companion object {
        fun byId(id: String): Topic? = entries.firstOrNull { it.id == id }
    }
}

enum class GameType(
    val id: String,
    val title: String,
    val emoji: String,
    val blurb: String,
    val topic: Topic?,
) {
    FEED_PIP("feed_pip", "Feed Pip", "😋", "Count apples into Pip's tummy", Topic.ADD),
    BALLOON_POP("balloon_pop", "Balloon Pop", "🎈", "Pop the balloon with the answer", Topic.ADD),
    FROG_HOP("frog_hop", "Frog Hop", "🐸", "Hop back on the number line", Topic.SUB),
    SNEAKY_SQUIRREL("squirrel", "Sneaky Squirrel", "🐿️", "How many acorns are left?", Topic.SUB),
    COOKIE_PLATES("cookie_plates", "Cookie Plates", "🍪", "Fill equal groups of cookies", Topic.MUL),
    KANGAROO_JUMPS("kangaroo", "Kangaroo Jumps", "🦘", "Skip-count to find the missing number", Topic.MUL),
    PIZZA_SHARE("pizza_share", "Pizza Party", "🍕", "Share slices fairly with friends", Topic.DIV),
    PACK_BOXES("pack_boxes", "Egg Packers", "📦", "Pack eggs into boxes", Topic.DIV),

    // Round 2 of games
    DOMINO_DOTS("domino", "Domino Dots", "🎲", "Count the dots on both sides", Topic.ADD),
    SNAIL_RACE("snail", "Snail Race", "🐌", "Crawl forward on the number line", Topic.ADD),
    MYSTERY_BOX("mystery_box", "Mystery Box", "🎁", "How many are hiding in the box?", Topic.ADD),
    BUBBLE_POP("bubbles", "Bubble Pop", "💥", "Pop some bubbles, count the rest", Topic.SUB),
    TOWER_COMPARE("towers", "Tower Compare", "🧱", "How many more blocks?", Topic.SUB),
    MAGIC_HAT("magic_hat", "Magic Hat", "🎩", "How many bunnies hid in the hat?", Topic.SUB),
    FLOWER_GARDEN("garden", "Flower Garden", "🌷", "Make rows of flowers bloom", Topic.MUL),
    LADYBUG_SPOTS("ladybugs", "Ladybug Spots", "🐞", "Count spots on every ladybug", Topic.MUL),
    TIMES_MACHINE("times_machine", "Times Machine", "⚙️", "Crank the machine, guess the number", Topic.MUL),
    MONSTER_MUNCH("monster", "Monster Munch", "👾", "How many bites to eat them all?", Topic.DIV),
    CUPCAKE_TRAYS("cupcakes", "Cupcake Trays", "🧁", "Sort cupcakes into trays", Topic.DIV),
    DIVIDE_MACHINE("divide_machine", "Divide-o-Matic", "🤖", "Feed the sharing machine", Topic.DIV),

    // Round 3: work with any size of number, so they appear in every level
    SUM_SNAP("sum_snap", "Sum Snap", "🃏", "Find two cards that add up to the target", Topic.ADD),
    SILLY_ADD("silly_add", "Silly Sums", "🤪", "Is Pip right or silly? Catch his mistakes!", Topic.ADD),
    DIFF_SNAP("diff_snap", "Difference Detective", "🔍", "Find two cards that are the right gap apart", Topic.SUB),
    SILLY_SUB("silly_sub", "Silly Take-aways", "🤪", "Is Pip right or silly? Catch his mistakes!", Topic.SUB),
    TIMES_SNAP("times_snap", "Times Twins", "👯", "Find two cards that multiply to the target", Topic.MUL),
    SILLY_MUL("silly_mul", "Silly Times", "🤪", "Is Pip right or silly? Catch his mistakes!", Topic.MUL),
    SHARE_SNAP("share_snap", "Share Pairs", "🍰", "Find two cards that divide to the target", Topic.DIV),
    SILLY_DIV("silly_div", "Silly Shares", "🤪", "Is Pip right or silly? Catch his mistakes!", Topic.DIV),

    // Round 4: real games (drag, catch, whack, steer)
    FILL_BASKET("fill_basket", "Fruit Basket", "🧺", "Drag fruit in to make the number", Topic.ADD),
    STAR_CATCHER("star_catcher", "Star Catcher", "🌟", "Move Pip to catch the right star", Topic.ADD),
    MOLE_WHACK("mole_whack", "Mole Whack", "🔨", "Bonk the mole holding the answer", Topic.SUB),
    MAILBOX("mailbox", "Mail Sorter", "📮", "Drag letters into the right mailbox", Topic.SUB),
    TILE_PAINTER("tile_painter", "Tile Painter", "🎨", "Paint a rectangle with the right tiles", Topic.MUL),
    GO_FISHING("go_fishing", "Go Fishing", "🎣", "Hook the fish with the answer", Topic.MUL),
    FAIR_SHARE("fair_share", "Cookie Share", "🍪", "Drag cookies to share them fairly", Topic.DIV),
    LANE_RACER("lane_racer", "Lane Racer", "🏎️", "Steer into the lane with the answer", Topic.DIV),

    QUIZ("quiz", "Snack Quiz", "🧠", "Quick questions for a big brain", null);

    companion object {
        /** Games added in v0.4. Used so levels opened before they existed stay open. */
        val ADDED_V4: Set<GameType> = setOf(
            FILL_BASKET, STAR_CATCHER, MOLE_WHACK, MAILBOX, TILE_PAINTER, GO_FISHING, FAIR_SHARE, LANE_RACER,
        )
    }

    val isSnap: Boolean get() = this == SUM_SNAP || this == DIFF_SNAP || this == TIMES_SNAP || this == SHARE_SNAP
    val isSilly: Boolean get() = this == SILLY_ADD || this == SILLY_SUB || this == SILLY_MUL || this == SILLY_DIV
}

/** Which games each operation has at each level. Beginner games use objects to count;
 *  higher levels use games that work with bigger numbers. */
object Curriculum {
    fun games(topic: Topic, level: Level): List<GameType> = when (level) {
        Level.BEGINNER -> when (topic) {
            Topic.ADD -> listOf(
                GameType.FEED_PIP, GameType.BALLOON_POP, GameType.FILL_BASKET, GameType.STAR_CATCHER,
                GameType.DOMINO_DOTS, GameType.SNAIL_RACE,
                GameType.MYSTERY_BOX, GameType.SUM_SNAP, GameType.SILLY_ADD,
            )
            Topic.SUB -> listOf(
                GameType.FROG_HOP, GameType.SNEAKY_SQUIRREL, GameType.MOLE_WHACK, GameType.MAILBOX,
                GameType.BUBBLE_POP, GameType.TOWER_COMPARE,
                GameType.MAGIC_HAT, GameType.DIFF_SNAP, GameType.SILLY_SUB,
            )
            Topic.MUL -> listOf(
                GameType.COOKIE_PLATES, GameType.KANGAROO_JUMPS, GameType.TILE_PAINTER, GameType.GO_FISHING,
                GameType.FLOWER_GARDEN, GameType.LADYBUG_SPOTS,
                GameType.TIMES_MACHINE, GameType.TIMES_SNAP, GameType.SILLY_MUL,
            )
            Topic.DIV -> listOf(
                GameType.PIZZA_SHARE, GameType.PACK_BOXES, GameType.FAIR_SHARE, GameType.LANE_RACER,
                GameType.MONSTER_MUNCH, GameType.CUPCAKE_TRAYS,
                GameType.DIVIDE_MACHINE, GameType.SHARE_SNAP, GameType.SILLY_DIV,
            )
        }
        Level.INTERMEDIATE -> when (topic) {
            Topic.ADD -> listOf(GameType.BALLOON_POP, GameType.FILL_BASKET, GameType.STAR_CATCHER, GameType.SNAIL_RACE, GameType.MYSTERY_BOX, GameType.SUM_SNAP, GameType.SILLY_ADD)
            Topic.SUB -> listOf(GameType.BALLOON_POP, GameType.MOLE_WHACK, GameType.MAILBOX, GameType.FROG_HOP, GameType.MAGIC_HAT, GameType.DIFF_SNAP, GameType.SILLY_SUB)
            Topic.MUL -> listOf(
                GameType.BALLOON_POP, GameType.TILE_PAINTER, GameType.GO_FISHING, GameType.KANGAROO_JUMPS,
                GameType.TIMES_MACHINE, GameType.TIMES_SNAP, GameType.SILLY_MUL,
            )
            Topic.DIV -> listOf(
                GameType.BALLOON_POP, GameType.FAIR_SHARE, GameType.LANE_RACER, GameType.DIVIDE_MACHINE,
                GameType.MYSTERY_BOX, GameType.SHARE_SNAP, GameType.SILLY_DIV,
            )
        }
        Level.PRO -> when (topic) {
            Topic.ADD -> listOf(GameType.BALLOON_POP, GameType.FILL_BASKET, GameType.STAR_CATCHER, GameType.MYSTERY_BOX, GameType.SUM_SNAP, GameType.SILLY_ADD)
            Topic.SUB -> listOf(GameType.BALLOON_POP, GameType.MOLE_WHACK, GameType.MAILBOX, GameType.MAGIC_HAT, GameType.DIFF_SNAP, GameType.SILLY_SUB)
            Topic.MUL -> listOf(GameType.BALLOON_POP, GameType.GO_FISHING, GameType.KANGAROO_JUMPS, GameType.TIMES_MACHINE, GameType.TIMES_SNAP, GameType.SILLY_MUL)
            Topic.DIV -> listOf(GameType.BALLOON_POP, GameType.LANE_RACER, GameType.DIVIDE_MACHINE, GameType.MYSTERY_BOX, GameType.SHARE_SNAP, GameType.SILLY_DIV)
        }
    }

    /** Games + the quiz: everything that needs 3 stars to open the next level. */
    fun starGames(topic: Topic, level: Level): List<GameType> = games(topic, level) + GameType.QUIZ

    fun maxStars(topic: Topic, level: Level): Int = starGames(topic, level).size * 3

    /** Levels a player had opened with the v0.3 game list (before the v0.4 games existed). */
    fun legacyUnlocks(p: Profile): Set<String> {
        val out = HashSet<String>()
        for (t in Topic.entries) {
            var open = true
            for (lv in listOf(Level.BEGINNER, Level.INTERMEDIATE)) {
                val best = p.stats(t, lv).bestStars
                val full = starGames(t, lv).filter { it !in GameType.ADDED_V4 }.all { (best[it.id] ?: 0) >= 3 }
                open = open && full
                val next = lv.next
                if (open && next != null) out.add(Profile.statsKey(t, next))
            }
        }
        return out
    }
}

enum class Level(
    val key: String,
    val title: String,
    val emoji: String,
    val blurb: String,
) {
    BEGINNER("beg", "Beginner", "🐣", "Build super-confidence with + − × ÷"),
    INTERMEDIATE("int", "Intermediate", "🦊", "Numbers to 50 and all the times tables"),
    PRO("pro", "Pro", "🦉", "Big numbers, carrying and borrowing");

    val previous: Level? get() = when (this) {
        BEGINNER -> null
        INTERMEDIATE -> BEGINNER
        PRO -> INTERMEDIATE
    }
    val next: Level? get() = when (this) {
        BEGINNER -> INTERMEDIATE
        INTERMEDIATE -> PRO
        PRO -> null
    }
}

/** Pip earns hats as the child collects stars. */
enum class Hat(val label: String, val starsNeeded: Int) {
    NONE("Sprout", 0),
    PARTY("Party Hat", 5),
    CROWN("Crown", 15),
    WIZARD("Wizard Hat", 30),
    PROPELLER("Propeller Cap", 50);

    companion object {
        fun forStars(stars: Int): Hat = entries.lastOrNull { stars >= it.starsNeeded } ?: NONE
        fun next(stars: Int): Hat? = entries.firstOrNull { it.starsNeeded > stars }
    }
}

/** Glasses for Pip, unlocked by 3-star wins. */
enum class Glasses(val label: String, val goldNeeded: Int) {
    NONE("No glasses", 0),
    SHADES("Cool shades", 2),
    SPECS("Smart specs", 5),
    STARS("Star glasses", 8),
}

/** Neckwear for Pip, unlocked by 3-star wins. */
enum class Neck(val label: String, val goldNeeded: Int) {
    NONE("Nothing", 0),
    BOW_TIE("Bow tie", 1),
    SCARF("Cozy scarf", 4),
}

/** Little flag Pip holds. All flags unlock as soon as Pip's Closet opens. */
enum class Flag(val label: String, val emoji: String, val goldNeeded: Int) {
    NONE("No flag", "", 0),
    SAUDI("Saudi Arabia", "🇸🇦", 1),
    UAE("UAE", "🇦🇪", 1),
    KUWAIT("Kuwait", "🇰🇼", 1),
    QATAR("Qatar", "🇶🇦", 1),
    BAHRAIN("Bahrain", "🇧🇭", 1),
    OMAN("Oman", "🇴🇲", 1),
    PHILIPPINES("Philippines", "🇵🇭", 1),
    PORTUGAL("Portugal", "🇵🇹", 1),
    ENGLAND("England", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F", 1),
    SPAIN("Spain", "🇪🇸", 1),
    BRAZIL("Brazil", "🇧🇷", 1),
}

enum class Hair(val label: String, val goldNeeded: Int) {
    NONE("Sprout", 0),
    SPIKY("Spiky", 1),
    CURLY("Curly", 2),
    PIGTAILS("Pigtails", 3),
    MOHAWK("Mohawk", 5),
    SWOOP("Swoosh", 7),
}

/** Job outfits replace the hat while they are worn. */
enum class Job(val label: String, val goldNeeded: Int) {
    NONE("No job", 0),
    CHEF("Chef", 2),
    DOCTOR("Doctor", 3),
    FIREFIGHTER("Firefighter", 4),
    BUILDER("Builder", 6),
    PILOT("Pilot", 8),
    ASTRONAUT("Astronaut", 10),
}

enum class Eyes(val label: String, val goldNeeded: Int) {
    NORMAL("Normal", 0),
    SPARKLY("Sparkly", 1),
    SLEEPY("Sleepy", 3),
    WINK("Winky", 4),
    HEARTS("Hearts", 6),
    STARRY("Starry", 9),
}

enum class Mouth(val label: String, val goldNeeded: Int) {
    NORMAL("Normal", 0),
    TOOTHY("Toothy", 2),
    TONGUE("Silly tongue", 3),
    MUSTACHE("Mustache", 5),
    FANGS("Vampire", 7),
}

/** Pip's colours. The first six are free; the rest unlock with 3-star wins. */
object PipColors {
    val names = listOf("Lime", "Sky", "Tangerine", "Bubblegum", "Grape", "Banana", "Mint", "Ruby", "Gold", "Midnight")
    val goldNeeded = listOf(0, 0, 0, 0, 0, 0, 3, 6, 10, 12)
    const val FREE = 6
}

object Closet {
    /** Labels of items that unlock when 3-star wins go from [old] to [new]. */
    fun unlockedBetween(old: Int, new: Int): List<String> {
        if (new <= old) return emptyList()
        val out = ArrayList<String>()
        val range = (old + 1)..new
        Neck.entries.filter { it != Neck.NONE && it.goldNeeded in range }.forEach { out.add(it.label) }
        Glasses.entries.filter { it != Glasses.NONE && it.goldNeeded in range }.forEach { out.add(it.label) }
        if (old < 1 && new >= 1) out.add("Flags")
        Hair.entries.filter { it != Hair.NONE && it.goldNeeded in range }.forEach { out.add(it.label + " hair") }
        Job.entries.filter { it != Job.NONE && it.goldNeeded in range }.forEach { out.add(it.label + " outfit") }
        Eyes.entries.filter { it != Eyes.NORMAL && it.goldNeeded in range }.forEach { out.add(it.label + " eyes") }
        Mouth.entries.filter { it != Mouth.NORMAL && it.goldNeeded in range }.forEach { out.add(it.label + " mouth") }
        PipColors.goldNeeded.forEachIndexed { i, need ->
            if (need > 0 && need in range) out.add(PipColors.names[i] + " colour")
        }
        return out
    }
}

/** How a child is doing in one topic. Difficulty "tier" is 1 (easy) to 3 (tricky). */
data class TopicStats(
    val tier: Int = 1,
    /** Ids of finished lessons ("1", "2", "3"). */
    val lessons: Set<String> = emptySet(),
    val gamesPlayed: Int = 0,
    val correct: Int = 0,
    val attempts: Int = 0,
    val bestStars: Map<String, Int> = emptyMap(),
) {
    val accuracyPercent: Int get() = if (attempts == 0) 0 else correct * 100 / attempts
    val tierName: String get() = when (tier) { 1 -> "Easy"; 2 -> "Medium"; else -> "Tricky" }
    val lessonDone: Boolean get() = lessons.isNotEmpty()
}

data class Profile(
    val id: String,
    val name: String,
    val age: Int,
    val colorIndex: Int,
    val totalStars: Int = 0,
    val stickers: List<String> = emptyList(),
    val topics: Map<String, TopicStats> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayed: Long = System.currentTimeMillis(),
    /** Number of games finished with 3 stars. Unlocks Pip's Closet and its items. */
    val goldWins: Int = 0,
    /** Hat picked in Pip's Closet (null = wear the newest one). */
    val hatChoice: String? = null,
    val glasses: Glasses = Glasses.NONE,
    val neck: Neck = Neck.NONE,
    val flag: Flag = Flag.NONE,
    val hair: Hair = Hair.NONE,
    val job: Job = Job.NONE,
    val eyes: Eyes = Eyes.NORMAL,
    val mouth: Mouth = Mouth.NORMAL,
    /** Grown-ups can open every level (for testing or older kids). */
    val allUnlocked: Boolean = false,
    /** Levels already opened (e.g. "int_add"). Once open, a level stays open even if new games are added. */
    val unlockedKeys: Set<String> = emptySet(),
) {
    fun stats(topic: Topic, level: Level = Level.BEGINNER): TopicStats =
        topics[statsKey(topic, level)] ?: TopicStats(tier = if (level == Level.BEGINNER) startingTier(age) else 1)

    fun withStats(topic: Topic, level: Level, s: TopicStats): Profile =
        copy(topics = topics + (statsKey(topic, level) to s))

    /** Stars collected in one operation at one level. */
    fun topicStars(topic: Topic, level: Level = Level.BEGINNER): Int {
        val s = stats(topic, level)
        return Curriculum.starGames(topic, level).sumOf { s.bestStars[it.id] ?: 0 }
    }

    /** Every game + quiz in this operation/level has 3 stars. */
    fun fullStars(topic: Topic, level: Level): Boolean = topicStars(topic, level) >= Curriculum.maxStars(topic, level)

    fun topicUnlocked(topic: Topic, level: Level): Boolean {
        val prev = level.previous ?: return true
        if (allUnlocked || statsKey(topic, level) in unlockedKeys) return true
        return topicUnlocked(topic, prev) && fullStars(topic, prev)
    }

    fun levelUnlocked(level: Level): Boolean = Topic.entries.any { topicUnlocked(it, level) }

    val hat: Hat
        get() {
            val chosen = Hat.entries.firstOrNull { it.name == hatChoice }
            return if (chosen != null && totalStars >= chosen.starsNeeded) chosen else Hat.forStars(totalStars)
        }

    val closetOpen: Boolean get() = goldWins >= 1

    companion object {
        /** Beginner keeps the old key ("add") so saved progress still loads. */
        fun statsKey(topic: Topic, level: Level): String =
            if (level == Level.BEGINNER) topic.id else level.key + "_" + topic.id

        /** Everyone starts easy to build confidence; older kids start one step up. */
        fun startingTier(age: Int): Int = if (age >= 8) 2 else 1
    }
}

data class Sticker(val id: String, val emoji: String, val name: String, val level: Level)

object Stickers {
    private fun set(level: Level, vararg items: Pair<String, String>): List<Sticker> =
        items.map { (emoji, name) -> Sticker(level.name.lowercase() + "_" + name.lowercase().replace(" ", ""), emoji, name, level) }

    val beginner: List<Sticker> = set(
        Level.BEGINNER,
        "🦄" to "Unicorn", "🐙" to "Octopus", "🚀" to "Rocket", "🍩" to "Donut",
        "🦖" to "Dino", "🌈" to "Rainbow", "🐸" to "Frog", "🍕" to "Pizza",
        "🐳" to "Whale", "🦊" to "Fox", "🎈" to "Balloon", "🐧" to "Penguin",
        "🍦" to "Ice Cream", "🦁" to "Lion", "🌟" to "Star", "🐝" to "Bee",
        "🦋" to "Butterfly", "🎸" to "Guitar", "🐢" to "Turtle", "🍉" to "Watermelon",
        "🤖" to "Robot", "🦉" to "Owl", "🐲" to "Dragon", "🧁" to "Cupcake",
    )

    val intermediate: List<Sticker> = set(
        Level.INTERMEDIATE,
        "🦒" to "Giraffe", "🐬" to "Dolphin", "🦜" to "Parrot", "🦔" to "Hedgehog",
        "🐊" to "Crocodile", "🦩" to "Flamingo", "🦥" to "Sloth", "🐘" to "Elephant",
        "🚂" to "Train", "🚁" to "Helicopter", "⛵" to "Sailboat", "🎡" to "Ferris Wheel",
        "🏰" to "Castle", "🌋" to "Volcano", "🍔" to "Burger", "🌮" to "Taco",
        "🍓" to "Strawberry", "🥨" to "Pretzel", "🎨" to "Paint", "🎺" to "Trumpet",
        "🪁" to "Kite", "🛹" to "Skateboard", "🧩" to "Puzzle", "🔭" to "Telescope",
    )

    val pro: List<Sticker> = set(
        Level.PRO,
        "🦕" to "Brontosaurus", "🦈" to "Shark", "🐉" to "Sky Dragon", "🦅" to "Eagle",
        "🐺" to "Wolf", "🦚" to "Peacock", "🛸" to "UFO", "🌌" to "Galaxy",
        "🪐" to "Planet", "☄️" to "Comet", "🌠" to "Shooting Star", "🏆" to "Trophy",
        "👑" to "Royal Crown", "💎" to "Diamond", "🧙" to "Wizard", "🦸" to "Superhero",
        "🥷" to "Ninja", "🤿" to "Diver", "🏎️" to "Race Car", "🛰️" to "Satellite",
        "🎯" to "Bullseye", "🧠" to "Big Brain", "🔮" to "Crystal Ball", "🎢" to "Roller Coaster",
    )

    val all: List<Sticker> = beginner + intermediate + pro

    fun forLevel(level: Level): List<Sticker> = when (level) {
        Level.BEGINNER -> beginner
        Level.INTERMEDIATE -> intermediate
        Level.PRO -> pro
    }

    /** Stickers saved by version 0.1 used short ids like "unicorn". */
    fun migrateId(id: String): String {
        if (all.any { it.id == id }) return id
        val fixed = if (id == "melon") "watermelon" else id
        return beginner.firstOrNull { it.id == "beginner_$fixed" }?.id ?: id
    }

    fun byId(id: String): Sticker? = all.firstOrNull { it.id == id }
}

/** What happened in one finished game, shown on the results screen. */
data class GameResult(
    val topic: Topic,
    val level: Level,
    val game: GameType,
    val guided: Boolean,
    val stars: Int,
    val correctFirstTry: Int,
    val total: Int,
    val sticker: Sticker?,
    val newHat: Hat?,
    val tierChange: Int,
    val closetOpened: Boolean = false,
    val unlocked: List<String> = emptyList(),
    /** Set when this game opened the next level of this operation. */
    val levelUnlocked: Level? = null,
)

/** Funny things Pip says. */
object Quips {
    val correct = listOf(
        "Yum-tastic!", "Brainy banana!", "Woo-hoo! Pip did a happy wiggle!",
        "You're a math wizard!", "Boom! Nailed it!", "Super-duper!",
        "Pip is SO proud of you!", "High five!", "Correct-a-mundo!",
        "Your brain is sparkling!", "Wowzers!", "Math-tastic!",
    )
    val wrong = listOf(
        "Hic! Not quite. Try again!", "Oopsie noodle! One more try!",
        "Hmm, close! Try another one.", "Whoops! Pip's tummy says try again.",
        "Nearly! You've got this!", "Wibble wobble! Have another go!",
    )
    const val hint = "Psst! Look at the wiggly one!"
}
