package com.example.toiletmap.data.local

import android.content.Context


data class GameBestRecord(
    val bestScore: Int,
    val bestSurvivalTime: Float,
    val playCount: Int
)


class GameScoreRepository(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )


    fun loadRecord(): GameBestRecord =
        GameBestRecord(
            bestScore =
                preferences.getInt(
                    KEY_BEST_SCORE,
                    0
                ),

            bestSurvivalTime =
                preferences.getFloat(
                    KEY_BEST_SURVIVAL_TIME,
                    0f
                ),

            playCount =
                preferences.getInt(
                    KEY_PLAY_COUNT,
                    0
                )
        )


    fun saveResult(
        score: Int,
        survivalTime: Float
    ): GameBestRecord {

        val current =
            loadRecord()

        val updated =
            GameBestRecord(
                bestScore =
                    maxOf(
                        current.bestScore,
                        score
                    ),

                bestSurvivalTime =
                    maxOf(
                        current.bestSurvivalTime,
                        survivalTime
                    ),

                playCount =
                    current.playCount + 1
            )

        preferences
            .edit()
            .putInt(
                KEY_BEST_SCORE,
                updated.bestScore
            )
            .putFloat(
                KEY_BEST_SURVIVAL_TIME,
                updated.bestSurvivalTime
            )
            .putInt(
                KEY_PLAY_COUNT,
                updated.playCount
            )
            .apply()

        return updated
    }


    private companion object {
        const val PREFS_NAME =
            "toilet_dodge_score"

        const val KEY_BEST_SCORE =
            "best_score"

        const val KEY_BEST_SURVIVAL_TIME =
            "best_survival_time"

        const val KEY_PLAY_COUNT =
            "play_count"
    }
}
