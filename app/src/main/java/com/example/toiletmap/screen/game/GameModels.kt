package com.example.toiletmap.screen.game

enum class GameState {
    TITLE,
    COUNTDOWN,
    PLAYING,
    GAME_OVER
}

enum class ObstacleType(
    val symbol: String,
    val baseSpeed: Float,
    val widthFraction: Float,
    val heightFraction: Float,
    val isFast: Boolean = false,
    val isLarge: Boolean = false
) {
    TOILET_PAPER(
        symbol = "🧻",
        baseSpeed = 0.22f,
        widthFraction = 0.10f,
        heightFraction = 0.075f
    ),
    PLUNGER(
        symbol = "🪠",
        baseSpeed = 0.23f,
        widthFraction = 0.10f,
        heightFraction = 0.08f
    ),
    MOP(
        symbol = "🧹",
        baseSpeed = 0.20f,
        widthFraction = 0.13f,
        heightFraction = 0.10f,
        isLarge = true
    ),
    CLEANER(
        symbol = "🧴",
        baseSpeed = 0.33f,
        widthFraction = 0.09f,
        heightFraction = 0.075f,
        isFast = true
    ),
    BUCKET(
        symbol = "🪣",
        baseSpeed = 0.21f,
        widthFraction = 0.12f,
        heightFraction = 0.09f,
        isLarge = true
    ),
    DIRT(
        symbol = "💩",
        baseSpeed = 0.36f,
        widthFraction = 0.085f,
        heightFraction = 0.07f,
        isFast = true
    ),
    TOILET(
        symbol = "🚽",
        baseSpeed = 0.18f,
        widthFraction = 0.17f,
        heightFraction = 0.12f,
        isLarge = true
    )
}

data class FallingObject(
    val id: Long,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val speed: Float,
    val type: ObstacleType
)

/*
 * 回復アイテム。
 * LIFEが減っているときだけ低頻度で出現する。
 */
data class RecoveryItem(
    val id: Long,
    val x: Float,
    val y: Float,
    val width: Float = WIDTH,
    val height: Float = HEIGHT,
    val speed: Float = BASE_SPEED
) {
    companion object {
        const val WIDTH = 0.09f
        const val HEIGHT = 0.07f
        const val BASE_SPEED = 0.17f
    }
}

data class GamePlayer(
    val x: Float = 0.5f,
    val y: Float = PLAYER_Y,
    val width: Float = PLAYER_WIDTH,
    val height: Float = PLAYER_HEIGHT,
    val life: Int = INITIAL_LIFE,
    val isInvincible: Boolean = false
) {
    companion object {
        const val INITIAL_LIFE = 3
        const val PLAYER_WIDTH = 0.13f
        const val PLAYER_HEIGHT = 0.085f
        const val PLAYER_Y = 0.84f
    }
}

data class LevelConfig(
    val level: Int,
    val speedMultiplier: Float,
    val maxObjects: Int,
    val spawnIntervalMillis: Long,
    val fastObstacleRate: Float,
    val largeObstacleRate: Float,
    val allowedTypes: List<ObstacleType>
)

/*
 * 全体的に以前より速め。
 * さらにLEVEL 5ではGameViewModel側で時間経過加速する。
 */
val TOILET_DODGE_LEVELS: List<LevelConfig> =
    listOf(
        LevelConfig(
            level = 1,
            speedMultiplier = 1.0f,
            maxObjects = 2,
            spawnIntervalMillis = 1_050L,
            fastObstacleRate = 0.0f,
            largeObstacleRate = 0.10f,
            allowedTypes = listOf(
                ObstacleType.TOILET_PAPER,
                ObstacleType.BUCKET
            )
        ),
        LevelConfig(
            level = 2,
            speedMultiplier = 1.2f,
            maxObjects = 3,
            spawnIntervalMillis = 820L,
            fastObstacleRate = 0.12f,
            largeObstacleRate = 0.15f,
            allowedTypes = listOf(
                ObstacleType.TOILET_PAPER,
                ObstacleType.BUCKET,
                ObstacleType.MOP,
                ObstacleType.CLEANER
            )
        ),
        LevelConfig(
            level = 3,
            speedMultiplier = 1.5f,
            maxObjects = 4,
            spawnIntervalMillis = 630L,
            fastObstacleRate = 0.35f,
            largeObstacleRate = 0.15f,
            allowedTypes = listOf(
                ObstacleType.TOILET_PAPER,
                ObstacleType.BUCKET,
                ObstacleType.MOP,
                ObstacleType.CLEANER,
                ObstacleType.DIRT
            )
        ),
        LevelConfig(
            level = 4,
            speedMultiplier = 1.8f,
            maxObjects = 5,
            spawnIntervalMillis = 480L,
            fastObstacleRate = 0.35f,
            largeObstacleRate = 0.35f,
            allowedTypes = listOf(
                ObstacleType.TOILET_PAPER,
                ObstacleType.BUCKET,
                ObstacleType.MOP,
                ObstacleType.CLEANER,
                ObstacleType.DIRT,
                ObstacleType.TOILET
            )
        ),
        LevelConfig(
            level = 5,
            speedMultiplier = 2.2f,
            maxObjects = 7,
            spawnIntervalMillis = 350L,
            fastObstacleRate = 0.48f,
            largeObstacleRate = 0.42f,
            allowedTypes = ObstacleType.entries
        )
    )

fun levelConfigFor(level: Int): LevelConfig =
    TOILET_DODGE_LEVELS[
        (level - 1).coerceIn(
            minimumValue = 0,
            maximumValue = TOILET_DODGE_LEVELS.lastIndex
        )
    ]

fun levelTitle(level: Int): String =
    when (level) {
        1 -> "CLEANING START!"
        2 -> "A LITTLE BUSY!"
        3 -> "CLEANING RUSH!"
        4 -> "TOILET PANIC!"
        else -> "TOILET HELL"
    }

fun scoreRank(score: Int): String =
    when {
        score >= 1_200 -> "S"
        score >= 700 -> "A"
        score >= 300 -> "B"
        else -> "C"
    }

fun rankMessage(rank: String): String =
    when (rank) {
        "S" -> "TOILET MASTER！"
        "A" -> "清掃エリアの達人！"
        "B" -> "なかなかの回避力！"
        else -> "まだまだ修行中！"
    }