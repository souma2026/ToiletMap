package com.example.toiletmap.repository

import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToiletRepository {

    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * データそのものを管理する場所。
     *
     * 将来的にはここを
     * Supabaseとの通信処理に変更する。
     */
    private val _toilets =
        MutableStateFlow<List<Toilet>>(
            listOf(
                Toilet(
                    name = "東京駅トイレ",
                    latitude = 35.681236,
                    longitude = 139.767125,
                    cleanliness = 4,
                    comment = "東京駅の近くにあるトイレです"
                )
            )
        )

    /*
     * 外部から読み取るためのトイレ一覧
     */
    val toilets: StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        _toilets.value += toilet
    }
}