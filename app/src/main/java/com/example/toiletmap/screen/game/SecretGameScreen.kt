package com.example.toiletmap.screen.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toiletmap.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import java.util.Locale

private val GameBackground = Color(0xFF071D24)
private val GamePanel = Color(0xFF102D35)
private val GameGreen = Color(0xFF16B8A6)
private val GameMint = Color(0xFFB9F4EC)
private val GameGold = Color(0xFFFFC857)
private val GameDanger = Color(0xFFFF6B6B)

@Composable
fun SecretGameScreen(
    viewModel: GameViewModel,
    onExit: () -> Unit
) {
    var showUnlockedMessage by remember {
        mutableStateOf(true)
    }

    /*
     * =========================================
     * ポーズ状態
     * =========================================
     * true の間はゲームループを止める。
     * GameViewModel 側を変更しないため、
     * 既存の難易度・Hellモード・当たり判定はそのまま。
     */
    var isPaused by remember {
        mutableStateOf(false)
    }

    /*
     * 隠しゲームを開いた直後
     */
    LaunchedEffect(Unit) {
        viewModel.returnToTitle()
        delay(1_200L)
        showUnlockedMessage = false
    }

    /*
     * =========================================
     * カウントダウン
     * =========================================
     */
    LaunchedEffect(viewModel.gameState) {
        if (viewModel.gameState == GameState.COUNTDOWN) {
            val labels = listOf("3", "2", "1", "START!")

            labels.forEach { label ->
                if (viewModel.gameState != GameState.COUNTDOWN) {
                    return@LaunchedEffect
                }

                viewModel.updateCountdownText(label)

                delay(
                    if (label == "START!") {
                        600L
                    } else {
                        750L
                    }
                )
            }

            if (viewModel.gameState == GameState.COUNTDOWN) {
                viewModel.beginPlaying()
            }
        }
    }

    /*
     * =========================================
     * ゲームループ
     * =========================================
     * isPaused が true になるとこの LaunchedEffect が
     * キャンセルされるため、時間・スコア・障害物・
     * 当たり判定・Hellモードの速度上昇も停止する。
     *
     * 再開時は新しい previousFrame から開始するので、
     * ポーズしていた時間が survivalTime に加算されない。
     */
    LaunchedEffect(
        viewModel.gameState,
        isPaused
    ) {
        if (
            viewModel.gameState != GameState.PLAYING ||
            isPaused
        ) {
            return@LaunchedEffect
        }

        var previousFrame = withFrameNanos { it }

        while (
            viewModel.gameState == GameState.PLAYING &&
            !isPaused
        ) {
            val currentFrame = withFrameNanos { it }

            val deltaSeconds =
                (currentFrame - previousFrame).toFloat() /
                        1_000_000_000f

            previousFrame = currentFrame

            viewModel.updateFrame(deltaSeconds)
        }
    }

    /*
     * LEVEL UP 表示の時間もポーズ中は停止扱いにする。
     */
    val currentAnnouncement = viewModel.levelAnnouncement

    LaunchedEffect(
        currentAnnouncement,
        isPaused
    ) {
        if (
            currentAnnouncement != null &&
            !isPaused
        ) {
            delay(1_000L)

            if (
                !isPaused &&
                viewModel.levelAnnouncement == currentAnnouncement
            ) {
                viewModel.clearLevelAnnouncement()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        if (showUnlockedMessage) {
            SecretUnlockedScreen()
        } else {
            when (viewModel.gameState) {
                GameState.TITLE -> {
                    GameTitleScreen(
                        bestScore = viewModel.bestScore,
                        onStart = {
                            isPaused = false
                            viewModel.startGame()
                        },
                        onExit = {
                            isPaused = false
                            viewModel.returnToTitle()
                            onExit()
                        }
                    )
                }

                GameState.COUNTDOWN,
                GameState.PLAYING -> {
                    GamePlayScreen(
                        viewModel = viewModel,
                        isPaused = isPaused,
                        onPause = {
                            if (viewModel.gameState == GameState.PLAYING) {
                                isPaused = true
                            }
                        },
                        onResume = {
                            isPaused = false
                        },
                        onExitToMap = {
                            isPaused = false
                            viewModel.returnToTitle()
                            onExit()
                        }
                    )
                }

                GameState.GAME_OVER -> {
                    GameOverScreen(
                        score = viewModel.score,
                        survivalTime = viewModel.survivalTime,
                        bestScore = viewModel.bestScore,
                        onRetry = {
                            isPaused = false
                            viewModel.retry()
                        },
                        onExit = {
                            isPaused = false
                            viewModel.returnToTitle()
                            onExit()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SecretUnlockedScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GamePanel
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 28.dp,
                    vertical = 24.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SECRET MODE",
                    color = GameGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "UNLOCKED",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
private fun GameTitleScreen(
    bestScore: Int,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 28.dp,
                vertical = 22.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SECRET MODE",
            color = GameGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "TOILET\nDODGE",
            color = Color.White,
            fontSize = 38.sp,
            lineHeight = 39.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GamePanel,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = "遊び方",
                    color = GameGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "← →  緑の『WC』を左右にドラッグ",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "⚠  落ちてくる障害物を避けよう",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "❤  ライフは3。当たると1減る",
                    color = GameMint,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "♥  赤いハートを取るとLIFE +1",
                    color = GameDanger,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "⏱  15秒ごとにLEVEL UP",
                    color = GameGold,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "II  ゲーム中は右上からポーズ可能",
                    color = GameMint,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            color = GamePanel,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BEST SCORE",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = bestScore.toString(),
                    color = GameGold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GameGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "START",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "ToiletMapに戻る",
                color = GameMint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GamePlayScreen(
    viewModel: GameViewModel,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onExitToMap: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        val density = LocalDensity.current

        val areaWidthPx = with(density) {
            maxWidth.toPx()
        }

        val playerWidth =
            maxWidth * GamePlayer.PLAYER_WIDTH

        val playerHeight =
            maxHeight * GamePlayer.PLAYER_HEIGHT

        val playerLeft =
            maxWidth *
                    (
                            viewModel.playerX -
                                    GamePlayer.PLAYER_WIDTH / 2f
                            )

        val playerTop =
            maxHeight * GamePlayer.PLAYER_Y

        /*
         * 障害物
         */
        viewModel.fallingObjects.forEach { obstacle ->
            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * obstacle.x,
                        y = maxHeight * obstacle.y
                    )
                    .width(maxWidth * obstacle.width)
                    .height(maxHeight * obstacle.height),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = obstacle.type.symbol,
                    fontSize =
                        if (obstacle.type.isLarge) {
                            39.sp
                        } else {
                            31.sp
                        },
                    textAlign = TextAlign.Center
                )
            }
        }

        /*
         * =========================================
         * 回復アイテム
         * =========================================
         * LIFEが減っているときに低頻度で落下する。
         * 取得するとGameViewModel側でLIFE +1。
         */
        viewModel.recoveryItems.forEach { item ->
            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * item.x,
                        y = maxHeight * item.y
                    )
                    .width(maxWidth * item.width)
                    .height(maxHeight * item.height)
                    .zIndex(4f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♥",
                    color = GameDanger,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        /*
         * プレイヤー
         */
        Box(
            modifier = Modifier
                .offset(
                    x = playerLeft,
                    y = playerTop
                )
                .width(playerWidth)
                .height(playerHeight)
                .alpha(
                    if (viewModel.isInvincible) {
                        0.45f
                    } else {
                        1f
                    }
                )
                .clip(RoundedCornerShape(18.dp))
                .background(GameGreen)
                .pointerInput(
                    areaWidthPx,
                    viewModel.gameState,
                    isPaused
                ) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        if (
                            !isPaused &&
                            areaWidthPx > 0f
                        ) {
                            viewModel.movePlayerBy(
                                dragAmount.x / areaWidthPx
                            )
                        }
                    }
                }
                .zIndex(5f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WC",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        /*
         * HUD
         */
        GameHud(
            score = viewModel.score,
            life = viewModel.life,
            survivalTime = viewModel.survivalTime,
            level = viewModel.currentLevel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )

        /*
         * =========================================
         * PAUSEボタン
         * =========================================
         */
        if (
            viewModel.gameState == GameState.PLAYING &&
            !isPaused
        ) {
            Button(
                onClick = onPause,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 105.dp,
                        end = 14.dp
                    )
                    .height(42.dp)
                    .zIndex(20f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamePanel,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "II  PAUSE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        /*
         * カウントダウン
         */
        if (viewModel.gameState == GameState.COUNTDOWN) {
            Text(
                text = viewModel.countdownText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(15f),
                color =
                    if (viewModel.countdownText == "START!") {
                        GameGreen
                    } else {
                        Color.White
                    },
                fontSize =
                    if (viewModel.countdownText == "START!") {
                        44.sp
                    } else {
                        72.sp
                    },
                fontWeight = FontWeight.Black
            )
        }

        /*
         * レベルアップ表示
         */
        val announcement = viewModel.levelAnnouncement

        if (
            announcement != null &&
            !isPaused
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(14f),
                color = GamePanel.copy(alpha = 0.94f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(
                    text = announcement,
                    modifier = Modifier.padding(
                        horizontal = 28.dp,
                        vertical = 20.dp
                    ),
                    color =
                        if (viewModel.currentLevel == 5) {
                            GameDanger
                        } else {
                            GameGold
                        },
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (
            viewModel.gameState == GameState.PLAYING &&
            !isPaused &&
            viewModel.survivalTime < 4f
        ) {
            Text(
                text = "←  プレイヤーを左右にドラッグ  →",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        /*
         * =========================================
         * ポーズ画面
         * =========================================
         */
        if (isPaused) {
            PauseOverlay(
                score = viewModel.score,
                survivalTime = viewModel.survivalTime,
                onResume = onResume,
                onExitToMap = onExitToMap,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    score: Int,
    survivalTime: Float,
    onResume: () -> Unit,
    onExitToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.72f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            color = GamePanel,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ゲームを一時停止しています",
                    color = GameMint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PauseInfo(
                        label = "SCORE",
                        value = score.toString()
                    )

                    PauseInfo(
                        label = "TIME",
                        value = String.format(
                            Locale.US,
                            "%.1f",
                            survivalTime
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "ゲームに戻る",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onExitToMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Mapに戻る",
                        color = GameMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseInfo(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.60f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = GameGold,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun GameHud(
    score: Int,
    life: Int,
    survivalTime: Float,
    level: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        color = GamePanel.copy(alpha = 0.92f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SCORE $score",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = String.format(
                        Locale.US,
                        "TIME %.1f",
                        survivalTime
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIFE " +
                            "❤".repeat(
                                life.coerceAtLeast(0)
                            ),
                    color = GameDanger,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "LEVEL $level",
                    color =
                        if (level == 5) {
                            GameDanger
                        } else {
                            GameGold
                        },
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun GameOverScreen(
    score: Int,
    survivalTime: Float,
    bestScore: Int,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    val rank = scoreRank(score)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 28.dp,
                vertical = 34.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GAME OVER",
            color = GameDanger,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        ResultPanel(
            label = "SCORE",
            value = score.toString()
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResultPanel(
            label = "SURVIVAL TIME",
            value = String.format(
                Locale.US,
                "%.1f sec",
                survivalTime
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResultPanel(
            label = "BEST SCORE",
            value = bestScore.toString()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            color = GamePanel,
            shape = CircleShape
        ) {
            Text(
                text = rank,
                modifier = Modifier.padding(
                    horizontal = 26.dp,
                    vertical = 16.dp
                ),
                color = GameGold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            text = rankMessage(rank),
            modifier = Modifier.padding(top = 10.dp),
            color = GameMint,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GameGreen
            )
        ) {
            Text(
                text = "RETRY",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "ToiletMapに戻る",
                color = GameMint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ResultPanel(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GamePanel,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 13.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}