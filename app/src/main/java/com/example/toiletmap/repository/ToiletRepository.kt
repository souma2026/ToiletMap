package com.example.toiletmap.repository

import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToiletRepository {

    /*
     * =====================================
     * トイレデータ
     * =====================================
     *
     * トイレデータの唯一の管理場所。
     *
     * 将来Supabaseを導入するときは
     * このRepositoryの中を
     * Supabaseとの読み書きに変更する。
     */
    private val _toilets =
        MutableStateFlow(

            listOf(

                /*
                 * サンプルトイレ
                 */
                Toilet(

                    name =
                        "東京駅トイレ",

                    latitude =
                        35.681236,

                    longitude =
                        139.767125,

                    cleanliness =
                        4,

                    comment =
                        "東京駅の近くにあるトイレです",

                    /*
                     * 最初は通常状態
                     * → 赤いピン
                     */
                    cleaningStatus =
                        CleaningStatus.NORMAL,

                    /*
                     * サンプルとして
                     * 2時間前に清掃された状態
                     */
                    lastCleanedAtMillis =
                        System.currentTimeMillis() -
                                (
                                        2L *
                                                60L *
                                                60L *
                                                1000L
                                        )
                )
            )
        )

    /*
     * =====================================
     * 外部公開用
     * =====================================
     */
    val toilets:
            StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        _toilets.value +=
            toilet
    }


    /*
     * =====================================
     * 清掃依頼
     * =====================================
     */
    fun requestCleaning(
        toiletId: String
    ) {

        /*
         * 対象トイレを探す
         */
        val toilet =
            _toilets
                .value
                .firstOrNull {

                    it.id ==
                            toiletId
                }
                ?: return

        /*
         * 通常状態以外なら
         * 何もしない
         */
        if (
            toilet.cleaningStatus !=
            CleaningStatus.NORMAL
        ) {

            return
        }

        /*
         * NORMAL
         * ↓
         * REQUESTED
         */
        updateToilet(

            toilet.copy(

                cleaningStatus =
                    CleaningStatus.REQUESTED
            )
        )
    }


    /*
     * =====================================
     * 清掃完了
     * =====================================
     */
    fun markCleaned(
        toiletId: String
    ) {

        /*
         * 対象トイレを探す
         */
        val toilet =
            _toilets
                .value
                .firstOrNull {

                    it.id ==
                            toiletId
                }
                ?: return

        /*
         * 清掃待ち状態以外では
         * 「清掃しました」を行わない
         */
        if (
            toilet.cleaningStatus !=
            CleaningStatus.REQUESTED
        ) {

            return
        }

        /*
         * REQUESTED
         * ↓
         * NORMAL
         *
         * 前回清掃時間も更新
         */
        updateToilet(

            toilet.copy(

                cleaningStatus =
                    CleaningStatus.NORMAL,

                lastCleanedAtMillis =
                    System.currentTimeMillis()
            )
        )
    }


    /*
     * =====================================
     * トイレ情報更新
     * =====================================
     */
    private fun updateToilet(
        updatedToilet: Toilet
    ) {

        _toilets.value =
            _toilets.value.map {
                    toilet ->

                if (
                    toilet.id ==
                    updatedToilet.id
                ) {

                    updatedToilet

                } else {

                    toilet
                }
            }
    }
}