package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToiletViewModel : ViewModel() {

    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * トイレデータを管理するのは
     * このViewModelだけにする
     */

    private val _toilets =
        MutableStateFlow<List<Toilet>>(

            /*
             * 今までMapLibreMapControllerに
             * 入っていたサンプルトイレ
             */
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
     * 外部から読み取るための一覧
     */
    val toilets: StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * トイレを追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        _toilets.value += toilet
    }
}