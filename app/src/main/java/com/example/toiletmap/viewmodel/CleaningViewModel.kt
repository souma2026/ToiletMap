package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.CleaningRepository
import com.example.toiletmap.model.CleaningRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class CleaningViewModel : ViewModel() {

    companion object {

        /*
         * 清掃依頼の自動更新間隔
         */
        private val AUTO_REFRESH_INTERVAL =
            1.hours
    }


    /*
     * =====================================
     * Repository
     * =====================================
     */

    private val repository =
        CleaningRepository()


    /*
     * =====================================
     * 清掃依頼一覧
     * =====================================
     */

    private val _requests =
        MutableStateFlow<List<CleaningRequest>>(
            emptyList()
        )

    val requests: StateFlow<List<CleaningRequest>> =
        _requests.asStateFlow()


    /*
     * =====================================
     * 現在のログインユーザー
     * =====================================
     */

    private val _currentUserId =
        MutableStateFlow<String?>(
            null
        )

    val currentUserId: StateFlow<String?> =
        _currentUserId.asStateFlow()


    /*
     * =====================================
     * 清掃依頼ポイント
     * =====================================
     */

    private val _requestPoints =
        MutableStateFlow(
            0
        )

    val requestPoints: StateFlow<Int> =
        _requestPoints.asStateFlow()


    /*
     * =====================================
     * 読み込み状態
     * =====================================
     */

    private val _isLoading =
        MutableStateFlow(
            false
        )

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    /*
     * =====================================
     * 現在処理中の清掃依頼ID
     * =====================================
     */

    private val _actionRequestId =
        MutableStateFlow<String?>(
            null
        )

    val actionRequestId: StateFlow<String?> =
        _actionRequestId.asStateFlow()


    /*
     * =====================================
     * エラーメッセージ
     * =====================================
     */

    private val _errorMessage =
        MutableStateFlow<String?>(
            null
        )

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    /*
     * =====================================
     * 成功メッセージ
     * =====================================
     */

    private val _successMessage =
        MutableStateFlow<String?>(
            null
        )

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    /*
     * =====================================
     * 自動更新Job
     * =====================================
     */

    private var autoRefreshJob:
            Job? =
        null


    /*
     * =====================================
     * 初期処理
     * =====================================
     */

    init {

        loadRequests()

        startAutoRefresh()
    }


    /*
     * =====================================
     * 清掃依頼一覧取得
     * =====================================
     */

    fun loadRequests() {

        if (
            _isLoading.value
        ) {

            return
        }


        _isLoading.value =
            true


        viewModelScope.launch {

            try {

                refresh(
                    showError =
                        true
                )

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    /*
     * =====================================
     * 清掃依頼
     * =====================================
     */

    fun requestCleaning(
        toiletId: String,
        requestPoints: Int
    ) {

        /*
         * 現在の仕様
         *
         * 依頼ポイント
         * ↓
         * 清掃報酬は +2pt
         *
         * 1pt → 3pt
         * 3pt → 5pt
         * 5pt → 7pt
         */
        val rewardPoints =
            requestPoints + 2


        runCleaningAction(

            actionId =
                toiletId,

            failureMessage =
                "清掃依頼に失敗しました",

            successMessage = {

                "${requestPoints}ptを使って清掃依頼を出しました" +
                        "（報酬${rewardPoints}pt）"
            },

            refreshFailureMessage =
                "清掃依頼は完了しました。" +
                        "表示を更新できなかったため、" +
                        "画面を更新してください"

        ) {

            repository
                .requestCleaning(

                    toiletId =
                        toiletId,

                    requestPoints =
                        requestPoints
                )
        }
    }


    /*
     * =====================================
     * 清掃を引き受ける
     * =====================================
     */

    fun acceptCleaning(
        requestId: String
    ) {

        runCleaningAction(

            actionId =
                requestId,

            failureMessage =
                "清掃の引き受けに失敗しました",

            successMessage = {

                "清掃を引き受けました"
            },

            refreshFailureMessage =
                "清掃の引き受けは完了しました。" +
                        "表示を更新できなかったため、" +
                        "画面を更新してください"

        ) {

            repository
                .acceptCleaning(
                    requestId
                )
        }
    }


    /*
     * =====================================
     * 清掃完了
     * =====================================
     */

    fun completeCleaning(
        requestId: String
    ) {

        /*
         * 清掃完了後は、
         * COMPLETEDになって一覧から消えるため、
         *
         * RPCを実行する前に
         * 今回獲得する報酬ポイントを保存する。
         */
        val earnedRewardPoints =

            _requests.value
                .firstOrNull {
                        request ->

                    request.id ==
                            requestId
                }
                ?.rewardPoints
                ?: 0


        runCleaningAction(

            actionId =
                requestId,

            failureMessage =
                "清掃完了の記録に失敗しました",

            /*
             * =====================================
             * 清掃完了成功時
             * =====================================
             *
             * MainActivityの既存Toastで
             * このメッセージが表示される。
             */
            successMessage = {

                if (
                    earnedRewardPoints > 0
                ) {

                    "清掃お疲れさまでした！\n" +
                            "＋${earnedRewardPoints}pt獲得しました"

                } else {

                    "清掃お疲れさまでした！\n" +
                            "清掃完了を記録しました"
                }
            },

            refreshFailureMessage =

                if (
                    earnedRewardPoints > 0
                ) {

                    "清掃お疲れさまでした！\n" +
                            "＋${earnedRewardPoints}pt獲得しました\n" +
                            "表示の更新だけに失敗しました"

                } else {

                    "清掃完了は記録されました。" +
                            "表示の更新だけに失敗しました"
                }

        ) {

            /*
             * =====================================
             * Supabase RPC
             * =====================================
             *
             * complete_cleaning 内で
             *
             * ・清掃完了
             * ・ポイント加算
             * ・ポイント履歴追加
             *
             * をまとめて行う。
             */
            repository
                .completeCleaning(
                    requestId
                )
        }
    }


    /*
     * =====================================
     * 清掃担当キャンセル
     * =====================================
     */

    fun cancelCleaning(
        requestId: String
    ) {

        runCleaningAction(

            actionId =
                requestId,

            failureMessage =
                "清掃担当のキャンセルに失敗しました",

            successMessage = {

                "清掃担当をキャンセルしました"
            },

            refreshFailureMessage =
                "キャンセルは完了しました。" +
                        "表示を更新できなかったため、" +
                        "画面を更新してください"

        ) {

            repository
                .cancelCleaning(
                    requestId
                )
        }
    }


    /*
     * =====================================
     * 自分が出した清掃依頼を取り消す
     * =====================================
     */

    fun cancelCleaningRequest(
        requestId: String
    ) {

        runCleaningAction(

            actionId =
                requestId,

            failureMessage =
                "清掃依頼の取り消しに失敗しました",

            successMessage = {

                "清掃依頼を取り消しました"
            },

            refreshFailureMessage =
                "清掃依頼は取り消されました。" +
                        "表示を更新できなかったため、" +
                        "画面を更新してください"

        ) {

            repository
                .cancelCleaningRequest(
                    requestId
                )
        }
    }


    /*
     * =====================================
     * 清掃操作共通処理
     * =====================================
     */

    private fun runCleaningAction(

        actionId: String,

        failureMessage: String,

        successMessage:
            () -> String,

        refreshFailureMessage:
        String,

        action:
        suspend () -> Unit

    ) {

        /*
         * すでに別処理中
         */
        if (
            _actionRequestId.value != null
        ) {

            _successMessage.value =
                null

            _errorMessage.value =
                "別の清掃操作を処理中です"

            return
        }


        /*
         * 処理開始
         */
        _actionRequestId.value =
            actionId

        _errorMessage.value =
            null

        _successMessage.value =
            null


        viewModelScope.launch {

            try {

                /*
                 * Supabase処理
                 */
                action()


                /*
                 * 最新状態を再取得
                 */
                val refreshed =

                    refresh(
                        showError =
                            false
                    )


                _errorMessage.value =
                    null


                if (
                    refreshed
                ) {

                    /*
                     * 操作成功
                     */
                    _successMessage.value =
                        successMessage()

                } else {

                    /*
                     * DB操作は成功したが
                     * 再取得だけ失敗
                     */
                    _successMessage.value =
                        refreshFailureMessage
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _successMessage.value =
                    null


                _errorMessage.value =

                    e.message
                        ?.takeIf {
                                message ->

                            message.isNotBlank()
                        }
                        ?: failureMessage

            } finally {

                if (
                    _actionRequestId.value ==
                    actionId
                ) {

                    _actionRequestId.value =
                        null
                }
            }
        }
    }


    /*
     * =====================================
     * メッセージを消す
     * =====================================
     */

    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }


    /*
     * =====================================
     * 最新状態取得
     * =====================================
     */

    private suspend fun refresh(
        showError: Boolean =
            true
    ): Boolean {

        return try {

            /*
             * ログインユーザー取得
             */
            val userId =

                repository
                    .getCurrentUserId()


            _currentUserId.value =
                userId


            if (
                userId == null
            ) {

                _requestPoints.value =
                    0

                _requests.value =
                    emptyList()

            } else {

                /*
                 * デイリー回復後の
                 * 清掃依頼ポイント
                 */
                _requestPoints.value =

                    repository
                        .loadCurrentRequestPoints(
                            userId
                        )


                /*
                 * 清掃依頼一覧
                 */
                _requests.value =

                    repository
                        .loadActiveRequests()
            }


            if (
                showError
            ) {

                _errorMessage.value =
                    null
            }


            true

        } catch (
            e: Exception
        ) {

            e.printStackTrace()


            if (
                showError
            ) {

                _errorMessage.value =

                    e.message
                        ?.takeIf {
                                message ->

                            message.isNotBlank()
                        }
                        ?: "清掃依頼の取得に失敗しました"
            }


            false
        }
    }


    /*
     * =====================================
     * 自動更新開始
     * =====================================
     */

    private fun startAutoRefresh() {

        if (
            autoRefreshJob?.isActive ==
            true
        ) {

            return
        }


        autoRefreshJob =

            viewModelScope.launch {

                while (
                    isActive
                ) {

                    delay(
                        AUTO_REFRESH_INTERVAL
                    )


                    refresh(
                        showError =
                            false
                    )
                }
            }
    }


    /*
     * =====================================
     * ViewModel終了
     * =====================================
     */

    override fun onCleared() {

        autoRefreshJob
            ?.cancel()


        autoRefreshJob =
            null


        super.onCleared()
    }
}