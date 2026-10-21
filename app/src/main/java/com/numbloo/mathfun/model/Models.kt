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

    /** The mini-games that belong to this topic (the quiz is shared by all topics). */
    val games: List<GameType> get() = GameType.entries.filter { it.topic == this }

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
    QUIZ("quiz", "Snack Quiz", "🧠", "Quick questions for a big brain", null);
}

enum class Level(
    val title: String,
    val emoji: String,
    val blurb: String,
    val unlocked: Boolean,
) {
    BEGINNER("Beginner", "🐣", "Build super-confidence with + − × ÷", true),
    INTERMEDIATE("Intermediate", "🦊", "Bigger numbers and tricky puzzles", false),
    PRO("Pro", "🦉", "Mental-math master mode", false),
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
        PipColors.goldNeeded.forEachIndexed { i, need ->
            if (need > 0 && need in range) out.add(PipColors.names[i] + " colour")
        }
        return out
    }
}

/** How a child is doing in one topic. Difficulty "tier" is 1 (easy) to 3 (tricky). */
data class TopicStats(
    val tier: Int = 1,
    val lessonDone: Boolean = false,
    val gamesPlayed: Int = 0,
    val correct: Int = 0,
    val attempts: Int = 0,
    val bestStars: Map<String, Int> = emptyMap(),
) {
    val accuracyPercent: Int get() = if (attempts == 0) 0 else correct * 100 / attempts
    val tierName: String get() = when (tier) { 1 -> "Easy"; 2 -> "Medium"; else -> "Tricky" }
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
) {
    fun stats(topic: Topic): TopicStats = topics[topic.id] ?: TopicStats(tier = startingTier(age))
    fun withStats(topic: Topic, s: TopicStats): Profile = copy(topics = topics + (topic.id to s))

    val hat: Hat
        get() {
            val chosen = Hat.entries.firstOrNull { it.name == hatChoice }
            return if (chosen != null && totalStars >= chosen.starsNeeded) chosen else Hat.forStars(totalStars)
        }

    val closetOpen: Boolean get() = goldWins >= 1

    /** Best stars across a topic's games + quiz (max 9 per topic). */
    fun topicStars(topic: Topic): Int {
        val s = stats(topic)
        return (topic.games + GameType.QUIZ).sumOf { s.bestStars[it.id] ?: 0 }
    }

    companion object {
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
