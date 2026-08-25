package com.example.toiletmap.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.toiletmap.data.local.GameScoreRepository
import com.example.toiletmap.screen.game.FallingObject
import com.example.toiletmap.screen.game.GamePlayer
import com.example.toiletmap.screen.game.GameState
import com.example.toiletmap.screen.game.LevelConfig
import com.example.toiletmap.screen.game.ObstacleType
import com.example.toiletmap.screen.game.levelConfigFor
import com.example.toiletmap.screen.game.levelTitle
import kotlin.random.Random


class GameViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scoreRepository =
        GameScoreRepository(
            application.applicationContext
        )

    private val random =
        Random.Default

    private var nextObjectId =
        1L

    private var spawnAccumulatorMillis =
        0f

    private var invincibleRemainingSeconds =
        0f


    var gameState by
        mutableStateOf(
            GameState.TITLE
        )
        private set

    var score by
        mutableIntStateOf(0)
        private set

    var life by
        mutableIntStateOf(
            GamePlayer.INITIAL_LIFE
        )
        private set

    var survivalTime by
        mutableFloatStateOf(0f)
        private set

    var playerX by
        mutableFloatStateOf(0.5f)
        private set

    var fallingObjects by
        mutableStateOf<List<FallingObject>>(
            emptyList()
        )
        private set

    var currentLevel by
        mutableIntStateOf(1)
        private set

    var countdownText by
        mutableStateOf("3")
        private set

    var levelAnnouncement by
        mutableStateOf<String?>(null)
        private set

    var isInvincible by
        mutableStateOf(false)
        private set

    var bestScore by
        mutableIntStateOf(0)
        private set

    var bestSurvivalTime by
        mutableFloatStateOf(0f)
        private set

    var playCount by
        mutableIntStateOf(0)
        private set


    init {
        loadBestRecord()
    }


    fun returnToTitle() {
        gameState =
            GameState.TITLE

        resetRound()
    }


    fun startGame() {
        resetRound()

        countdownText =
            "3"

        gameState =
            GameState.COUNTDOWN
    }


    fun retry() {
        startGame()
    }


    fun updateCountdownText(
        text: String
    ) {
        countdownText =
            text
    }


    fun beginPlaying() {
        if (
            gameState !=
            GameState.COUNTDOWN
        ) {
            return
        }

        gameState =
            GameState.PLAYING

        levelAnnouncement =
            "LEVEL 1\n${levelTitle(1)}"
    }


    fun clearLevelAnnouncement() {
        levelAnnouncement =
            null
    }


    fun movePlayerBy(
        normalizedDeltaX: Float
    ) {
        if (
            gameState != GameState.PLAYING &&
            gameState != GameState.COUNTDOWN
        ) {
            return
        }

        val halfWidth =
            GamePlayer.PLAYER_WIDTH /
                    2f

        playerX =
            (playerX + normalizedDeltaX)
                .coerceIn(
                    minimumValue = halfWidth,
                    maximumValue = 1f - halfWidth
                )
    }


    fun updateFrame(
        deltaSeconds: Float
    ) {
        if (
            gameState !=
            GameState.PLAYING
        ) {
            return
        }

        val dt =
            deltaSeconds
                .coerceIn(
                    minimumValue = 0f,
                    maximumValue = 0.05f
                )

        if (dt <= 0f) {
            return
        }

        survivalTime +=
            dt

        score =
            (survivalTime * 10f)
                .toInt()

        updateLevel()

        val config =
            levelConfigFor(
                currentLevel
            )

        updateInvincibility(
            dt
        )

        spawnAccumulatorMillis +=
            dt * 1_000f

        if (
            spawnAccumulatorMillis >= config.spawnIntervalMillis &&
            fallingObjects.size < config.maxObjects
        ) {
            spawnAccumulatorMillis =
                0f

            fallingObjects =
                fallingObjects +
                        createFallingObject(
                            config
                        )
        }

        fallingObjects =
            fallingObjects
                .map { obstacle ->

                    obstacle.copy(
                        y =
                            obstacle.y +
                                    obstacle.speed *
                                    config.speedMultiplier *
                                    dt
                    )
                }
                .filter { obstacle ->
                    obstacle.y < 1.08f
                }

        checkCollision()
    }


    private fun updateLevel() {
        val calculatedLevel =
            (
                    survivalTime /
                            15f
                    )
                .toInt()
                .plus(1)
                .coerceAtMost(5)

        if (
            calculatedLevel !=
            currentLevel
        ) {
            currentLevel =
                calculatedLevel

            levelAnnouncement =
                if (
                    calculatedLevel == 5
                ) {
                    "FINAL LEVEL\nTOILET HELL"
                } else {
                    "LEVEL $calculatedLevel\n${levelTitle(calculatedLevel)}"
                }
        }
    }


    private fun updateInvincibility(
        dt: Float
    ) {
        if (
            invincibleRemainingSeconds <= 0f
        ) {
            isInvincible =
                false

            return
        }

        invincibleRemainingSeconds -=
            dt

        if (
            invincibleRemainingSeconds <= 0f
        ) {
            invincibleRemainingSeconds =
                0f

            isInvincible =
                false
        }
    }


    private fun createFallingObject(
        config: LevelConfig
    ): FallingObject {
        val type =
            selectObstacleType(
                config
            )

        val maxX =
            (1f - type.widthFraction)
                .coerceAtLeast(0f)

        val x =
            random.nextFloat() *
                    maxX

        return FallingObject(
            id =
                nextObjectId++,

            x =
                x,

            y =
                -type.heightFraction,

            width =
                type.widthFraction,

            height =
                type.heightFraction,

            speed =
                type.baseSpeed,

            type =
                type
        )
    }


    private fun selectObstacleType(
        config: LevelConfig
    ): ObstacleType {
        val fastTypes =
            config.allowedTypes
                .filter {
                    it.isFast
                }

        val largeTypes =
            config.allowedTypes
                .filter {
                    it.isLarge
                }

        val roll =
            random.nextFloat()

        return when {
            largeTypes.isNotEmpty() &&
                    roll < config.largeObstacleRate ->
                largeTypes.random(
                    random
                )

            fastTypes.isNotEmpty() &&
                    roll < config.largeObstacleRate + config.fastObstacleRate ->
                fastTypes.random(
                    random
                )

            else ->
                config.allowedTypes
                    .random(
                        random
                    )
        }
    }


    private fun checkCollision() {
        if (
            isInvincible ||
            fallingObjects.isEmpty()
        ) {
            return
        }

        val playerLeft =
            playerX -
                    GamePlayer.PLAYER_WIDTH /
                    2f

        val playerRight =
            playerX +
                    GamePlayer.PLAYER_WIDTH /
                    2f

        val playerTop =
            GamePlayer.PLAYER_Y

        val playerBottom =
            GamePlayer.PLAYER_Y +
                    GamePlayer.PLAYER_HEIGHT

        val collided =
            fallingObjects
                .firstOrNull { obstacle ->

                    val obstacleRight =
                        obstacle.x +
                                obstacle.width

                    val obstacleBottom =
                        obstacle.y +
                                obstacle.height

                    playerLeft < obstacleRight &&
                            playerRight > obstacle.x &&
                            playerTop < obstacleBottom &&
                            playerBottom > obstacle.y
                }
                ?: return

        fallingObjects =
            fallingObjects
                .filterNot {
                    it.id == collided.id
                }

        life =
            (life - 1)
                .coerceAtLeast(0)

        if (
            life <= 0
        ) {
            finishGame()

        } else {
            isInvincible =
                true

            invincibleRemainingSeconds =
                1f
        }
    }


    private fun finishGame() {
        gameState =
            GameState.GAME_OVER

        fallingObjects =
            emptyList()

        val record =
            scoreRepository.saveResult(
                score =
                    score,

                survivalTime =
                    survivalTime
            )

        bestScore =
            record.bestScore

        bestSurvivalTime =
            record.bestSurvivalTime

        playCount =
            record.playCount
    }


    private fun resetRound() {
        score =
            0

        life =
            GamePlayer.INITIAL_LIFE

        survivalTime =
            0f

        playerX =
            0.5f

        fallingObjects =
            emptyList()

        currentLevel =
            1

        countdownText =
            "3"

        levelAnnouncement =
            null

        isInvincible =
            false

        invincibleRemainingSeconds =
            0f

        spawnAccumulatorMillis =
            0f
    }


    private fun loadBestRecord() {
        val record =
            scoreRepository.loadRecord()

        bestScore =
            record.bestScore

        bestSurvivalTime =
            record.bestSurvivalTime

        playCount =
            record.playCount
    }
}
