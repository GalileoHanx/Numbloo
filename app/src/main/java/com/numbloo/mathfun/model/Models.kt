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
) {
    fun stats(topic: Topic): TopicStats = topics[topic.id] ?: TopicStats(tier = startingTier(age))
    fun withStats(topic: Topic, s: TopicStats): Profile = copy(topics = topics + (topic.id to s))
    val hat: Hat get() = Hat.forStars(totalStars)

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

data class Sticker(val id: String, val emoji: String, val name: String)

object Stickers {
    val all: List<Sticker> = listOf(
        Sticker("unicorn", "🦄", "Unicorn"), Sticker("octopus", "🐙", "Octopus"),
        Sticker("rocket", "🚀", "Rocket"), Sticker("donut", "🍩", "Donut"),
        Sticker("dino", "🦖", "Dino"), Sticker("rainbow", "🌈", "Rainbow"),
        Sticker("frog", "🐸", "Frog"), Sticker("pizza", "🍕", "Pizza"),
        Sticker("whale", "🐳", "Whale"), Sticker("fox", "🦊", "Fox"),
        Sticker("balloon", "🎈", "Balloon"), Sticker("penguin", "🐧", "Penguin"),
        Sticker("icecream", "🍦", "Ice Cream"), Sticker("lion", "🦁", "Lion"),
        Sticker("star", "🌟", "Star"), Sticker("bee", "🐝", "Bee"),
        Sticker("butterfly", "🦋", "Butterfly"), Sticker("guitar", "🎸", "Guitar"),
        Sticker("turtle", "🐢", "Turtle"), Sticker("melon", "🍉", "Watermelon"),
        Sticker("robot", "🤖", "Robot"), Sticker("owl", "🦉", "Owl"),
        Sticker("dragon", "🐲", "Dragon"), Sticker("cupcake", "🧁", "Cupcake"),
    )

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
