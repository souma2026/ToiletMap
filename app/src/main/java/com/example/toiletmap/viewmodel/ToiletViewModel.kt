package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ToiletRepository
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
     * 自動更新の間隔
     * =====================================
     *
     * 1時間
     *
     * Supabaseへの定期アクセスを抑えるため、
     * 最新リポジトリの方針に合わせている。
     */
    companion object {

        private const val
                AUTO_REFRESH_INTERVAL_MS =
            60 * 60 * 1000L
    }


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
     * 自動更新Job
     * =====================================
     *
     * 同じ自動更新処理が
     * 二重起動しないように保持する
     */
    private var autoRefreshJob:
            Job? =
        null


    /*
     * =====================================
     * ViewModel作成時
     * =====================================
     */
    init {

        /*
         * 最初に1回
         * Supabaseから取得
         */
        loadToilets()


        /*
         * その後
         * 定期更新開始
         */
        startAutoRefresh()
    }


    /*
     * =====================================
     * 自動更新開始
     * =====================================
     *
     * 1時間ごとに
     * Supabaseから最新状態を取得する
     */
    private fun startAutoRefresh() {

        /*
         * すでに動いている場合は
         * 二重起動しない
         */
        if (
            autoRefreshJob?.isActive ==
            true
        ) {

            return
        }


        autoRefreshJob =

            viewModelScope.launch {

                /*
                 * ViewModelが生きている間
                 * 繰り返す
                 */
                while (
                    isActive
                ) {

                    /*
                     * 1時間待つ
                     */
                    delay(
                        AUTO_REFRESH_INTERVAL_MS
                    )


                    /*
                     * Supabaseから
                     * 最新データを取得
                     */
                    refreshToiletsSilently()
                }
            }
    }


    /*
     * =====================================
     * 自動更新用
     * =====================================
     *
     * 通信が一時的に失敗しても
     * アプリを止めない。
     *
     * 次の定期更新時に
     * また取得を試す。
     */
    private suspend fun refreshToiletsSilently() {

        try {

            repository
                .loadToilets()


            /*
             * 取得成功
             */
            _errorMessage.value =
                null

        } catch (
            e: Exception
        ) {

            /*
             * 自動更新なので
             * エラーでアプリを止めない
             */
            e.printStackTrace()
        }
    }


    /*
     * =====================================
     * トイレ一覧取得
     * =====================================
     *
     * 初回読み込みや
     * 手動更新用
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
     * 表示済みエラーを消す
     * =====================================
     */
    fun clearErrorMessage() {

        _errorMessage.value =
            null
    }


    /*
     * =====================================
     * ViewModel破棄時
     * =====================================
     */
    override fun onCleared() {

        /*
         * 念のため
         * 自動更新を終了
         */
        autoRefreshJob
            ?.cancel()


        autoRefreshJob =
            null


        super.onCleared()
    }
}