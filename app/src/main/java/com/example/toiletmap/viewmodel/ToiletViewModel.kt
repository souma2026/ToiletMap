package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.data.repository.ToiletRepository
import kotlinx.coroutines.flow.StateFlow

class ToiletViewModel : ViewModel() {

    /*
     * =====================================
     * Repository
     * =====================================
     */
    private val repository =
        ToiletRepository()


    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * Repositoryのデータを
     * UI側へ公開する。
     */
    val toilets:
            StateFlow<List<Toilet>> =
        repository.toilets


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        repository
            .addToilet(
                toilet
            )
    }


    /*
     * =====================================
     * 清掃依頼
     * =====================================
     */
    fun requestCleaning(
        toiletId: String
    ) {

        repository
            .requestCleaning(
                toiletId
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

        repository
            .markCleaned(
                toiletId
            )
    }
}