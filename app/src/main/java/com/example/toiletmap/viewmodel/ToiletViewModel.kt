package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.repository.ToiletRepository
import kotlinx.coroutines.flow.StateFlow

class ToiletViewModel : ViewModel() {

    /*
     * =====================================
     * Repository
     * =====================================
     *
     * データ管理はRepositoryへ任せる
     */
    private val repository =
        ToiletRepository()


    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * Repositoryが持っている一覧を
     * ViewModelから画面側へ公開する
     */
    val toilets: StateFlow<List<Toilet>> =
        repository.toilets


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        repository.addToilet(
            toilet
        )
    }
}