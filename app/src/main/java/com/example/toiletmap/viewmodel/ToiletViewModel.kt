package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ToiletRepository
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


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
     * Repositoryが持つ
     * StateFlowをそのまま公開する
     */
    val toilets:
            StateFlow<List<Toilet>> =
        repository.toilets


    /*
     * =====================================
     * エラーメッセージ
     * =====================================
     */
    private val _errorMessage =
        MutableStateFlow<String?>(
            null
        )


    val errorMessage:
            StateFlow<String?> =
        _errorMessage.asStateFlow()


    /*
     * =====================================
     * ViewModel作成時
     *
     * Supabaseから
     * トイレ一覧を読み込む
     * =====================================
     */
    init {

        loadToilets()
    }


    /*
     * =====================================
     * トイレ一覧取得
     * =====================================
     */
    fun loadToilets() {

        viewModelScope.launch {

            try {

                repository
                    .loadToilets()


                /*
                 * 成功したので
                 * エラーを消す
                 */
                _errorMessage.value =
                    null

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _errorMessage.value =
                    "トイレ情報の取得に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(
        toilet: Toilet
    ) {

        viewModelScope.launch {

            try {

                repository
                    .addToilet(
                        toilet
                    )


                _errorMessage.value =
                    null

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _errorMessage.value =
                    e.message
                        ?: "トイレの登録に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * 清掃依頼
     * =====================================
     */
    fun requestCleaning(
        toiletId: String
    ) {

        viewModelScope.launch {

            try {

                repository
                    .requestCleaning(
                        toiletId
                    )


                _errorMessage.value =
                    null

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _errorMessage.value =
                    e.message
                        ?: "清掃依頼に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * 清掃完了
     * =====================================
     */
    fun markCleaned(
        toiletId: String
    ) {

        viewModelScope.launch {

            try {

                repository
                    .markCleaned(
                        toiletId
                    )


                _errorMessage.value =
                    null

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _errorMessage.value =
                    e.message
                        ?: "清掃状態の更新に失敗しました"
            }
        }
    }
}