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
import com.example.toiletmap.screen.game.RecoveryItem
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

    /*
     * 回復アイテム用タイマー。
     * LIFEが減っている間だけ進む。
     */
    private var recoverySpawnAccumulatorSeconds =
        0f

    private var recoverySpawnWaitSeconds =
        nextRecoveryDelaySeconds()

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

    var recoveryItems by
    mutableStateOf<List<RecoveryItem>>(
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

        /*
         * LEVEL 5 = TOILET HELL。
         * 60秒以降、10秒ごとに約6%ずつ加速。
         * 暴走しすぎないよう最大1.75倍まで。
         */
        val hellSpeedMultiplier =
            currentHellSpeedMultiplier()

        spawnAccumulatorMillis +=
            dt * 1_000f

        val effectiveSpawnIntervalMillis =
            if (currentLevel == 5) {
                (
                        config.spawnIntervalMillis /
                                hellSpeedMultiplier.coerceAtMost(1.35f)
                        ).toLong()
                    .coerceAtLeast(250L)
            } else {
                config.spawnIntervalMillis
            }

        if (
            spawnAccumulatorMillis >= effectiveSpawnIntervalMillis &&
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
                                    hellSpeedMultiplier *
                                    dt
                    )
                }
                .filter { obstacle ->
                    obstacle.y < 1.08f
                }

        updateRecoveryItems(
            dt
        )

        checkRecoveryCollision()
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

    private fun currentHellSpeedMultiplier(): Float {
        if (currentLevel < 5) {
            return 1f
        }

        val hellElapsedSeconds =
            (survivalTime - 60f)
                .coerceAtLeast(0f)

        return (
                1f +
                        (hellElapsedSeconds / 10f) *
                        0.06f
                ).coerceAtMost(1.75f)
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

    /*
     * =========================================
     * 回復アイテム
     * =========================================
     */
    private fun updateRecoveryItems(
        dt: Float
    ) {
        if (life < GamePlayer.INITIAL_LIFE) {
            recoverySpawnAccumulatorSeconds +=
                dt

            if (
                recoveryItems.isEmpty() &&
                recoverySpawnAccumulatorSeconds >= recoverySpawnWaitSeconds
            ) {
                recoveryItems =
                    listOf(
                        createRecoveryItem()
                    )

                recoverySpawnAccumulatorSeconds =
                    0f

                recoverySpawnWaitSeconds =
                    nextRecoveryDelaySeconds()
            }
        } else {
            recoverySpawnAccumulatorSeconds =
                0f
        }

        val recoverySpeedMultiplier =
            1f +
                    (currentLevel - 1) *
                    0.08f

        recoveryItems =
            recoveryItems
                .map { item ->
                    item.copy(
                        y =
                            item.y +
                                    item.speed *
                                    recoverySpeedMultiplier *
                                    dt
                    )
                }
                .filter { item ->
                    item.y < 1.08f
                }
    }

    private fun createRecoveryItem(): RecoveryItem {
        val maxX =
            (1f - RecoveryItem.WIDTH)
                .coerceAtLeast(0f)

        return RecoveryItem(
            id =
                nextObjectId++,
            x =
                random.nextFloat() * maxX,
            y =
                -RecoveryItem.HEIGHT
        )
    }

    private fun nextRecoveryDelaySeconds(): Float =
        random
            .nextInt(
                from = 9,
                until = 15
            )
            .toFloat()

    private fun checkRecoveryCollision() {
        if (recoveryItems.isEmpty()) {
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

        val collected =
            recoveryItems
                .firstOrNull { item ->
                    val horizontalInset =
                        item.width *
                                0.08f

                    val verticalInset =
                        item.height *
                                0.08f

                    val itemLeft =
                        item.x + horizontalInset

                    val itemRight =
                        item.x +
                                item.width -
                                horizontalInset

                    val itemTop =
                        item.y + verticalInset

                    val itemBottom =
                        item.y +
                                item.height -
                                verticalInset

                    playerLeft < itemRight &&
                            playerRight > itemLeft &&
                            playerTop < itemBottom &&
                            playerBottom > itemTop
                }
                ?: return

        recoveryItems =
            recoveryItems
                .filterNot {
                    it.id == collected.id
                }

        life =
            (life + 1)
                .coerceAtMost(
                    GamePlayer.INITIAL_LIFE
                )

        recoverySpawnAccumulatorSeconds =
            0f

        recoverySpawnWaitSeconds =
            nextRecoveryDelaySeconds()
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
                    /*
                     * 絵文字の透明な余白を当たり判定から除外。
                     */
                    val horizontalInset =
                        obstacle.width *
                                0.10f

                    val verticalInset =
                        obstacle.height *
                                0.22f

                    val obstacleLeft =
                        obstacle.x +
                                horizontalInset

                    val obstacleRight =
                        obstacle.x +
                                obstacle.width -
                                horizontalInset

                    val obstacleTop =
                        obstacle.y +
                                verticalInset

                    val obstacleBottom =
                        obstacle.y +
                                obstacle.height -
                                verticalInset

                    playerLeft < obstacleRight &&
                            playerRight > obstacleLeft &&
                            playerTop < obstacleBottom &&
                            playerBottom > obstacleTop
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

        recoveryItems =
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

        recoveryItems =
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

        recoverySpawnAccumulatorSeconds =
            0f

        recoverySpawnWaitSeconds =
            nextRecoveryDelaySeconds()
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