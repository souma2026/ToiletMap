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

        /**
         * 清掃依頼を自動更新する間隔。
         */
        private val AUTO_REFRESH_INTERVAL =
            1.hours
    }


    // =========================================================
    // データ操作
    // =========================================================

    private val repository =
        CleaningRepository()


    // =========================================================
    // 清掃依頼一覧
    // =========================================================

    private val _requests =
        MutableStateFlow<List<CleaningRequest>>(
            emptyList()
        )

    val requests: StateFlow<List<CleaningRequest>> =
        _requests.asStateFlow()


    // =========================================================
    // 現在ログインしているユーザー
    // =========================================================

    private val _currentUserId =
        MutableStateFlow<String?>(
            null
        )

    val currentUserId: StateFlow<String?> =
        _currentUserId.asStateFlow()


    // =========================================================
    // 一覧読み込み中
    // =========================================================

    private val _isLoading =
        MutableStateFlow(
            false
        )

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // =========================================================
    // 現在処理している清掃操作
    // =========================================================

    private val _actionRequestId =
        MutableStateFlow<String?>(
            null
        )

    val actionRequestId: StateFlow<String?> =
        _actionRequestId.asStateFlow()


    // =========================================================
    // エラーメッセージ
    // =========================================================

    private val _errorMessage =
        MutableStateFlow<String?>(
            null
        )

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    // =========================================================
    // 成功メッセージ
    // =========================================================

    private val _successMessage =
        MutableStateFlow<String?>(
            null
        )

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    // =========================================================
    // 自動更新処理
    // =========================================================

    private var autoRefreshJob: Job? =
        null


    // =========================================================
    // 初期処理
    // =========================================================

    init {

        loadRequests()

        startAutoRefresh()
    }


    // =========================================================
    // 清掃依頼一覧を取得
    // =========================================================

    fun loadRequests() {

        if (_isLoading.value) {
            return
        }


        _isLoading.value =
            true


        viewModelScope.launch {

            try {

                refresh(
                    showError = true
                )

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    // =========================================================
    // 清掃を依頼する
    // =========================================================

    fun requestCleaning(
        toiletId: String
    ) {

        runCleaningAction(
            actionId = toiletId,
            failureMessage =
                "清掃依頼に失敗しました",
            successMessage = {
                "清掃依頼を出しました"
            },
            refreshFailureMessage =
                "清掃依頼は完了しました。表示を更新できなかったため、画面を更新してください"
        ) {

            repository.requestCleaning(
                toiletId
            )
        }
    }


    // =========================================================
    // 清掃を引き受ける
    // =========================================================

    fun acceptCleaning(
        requestId: String
    ) {

        runCleaningAction(
            actionId = requestId,
            failureMessage =
                "清掃の引き受けに失敗しました",
            successMessage = {
                "清掃を引き受けました"
            },
            refreshFailureMessage =
                "清掃の引き受けは完了しました。表示を更新できなかったため、画面を更新してください"
        ) {

            repository.acceptCleaning(
                requestId
            )
        }
    }


    // =========================================================
    // 清掃完了
    // =========================================================

    fun completeCleaning(
        requestId: String
    ) {

        val earnedRewardPoints =
            _requests.value
                .firstOrNull { request ->

                    request.id ==
                            requestId

                }
                ?.rewardPoints
                ?: 0


        runCleaningAction(
            actionId = requestId,
            failureMessage =
                "清掃完了の記録に失敗しました",
            successMessage = {

                if (earnedRewardPoints > 0) {

                    "清掃完了！${earnedRewardPoints}ptを獲得しました"

                } else {

                    "清掃完了を記録しました"
                }
            },
            refreshFailureMessage =
                "清掃完了は記録されました。表示を更新できなかったため、画面を更新してください"
        ) {

            repository.completeCleaning(
                requestId
            )
        }
    }


    // =========================================================
    // 清掃担当をキャンセル
    // =========================================================

    fun cancelCleaning(
        requestId: String
    ) {

        runCleaningAction(
            actionId = requestId,
            failureMessage =
                "清掃担当のキャンセルに失敗しました",
            successMessage = {
                "清掃担当をキャンセルしました"
            },
            refreshFailureMessage =
                "キャンセルは完了しました。表示を更新できなかったため、画面を更新してください"
        ) {

            repository.cancelCleaning(
                requestId
            )
        }
    }


    // =========================================================
    // 清掃操作共通処理
    // =========================================================

    private fun runCleaningAction(
        actionId: String,
        failureMessage: String,
        successMessage: () -> String,
        refreshFailureMessage: String,
        action: suspend () -> Unit
    ) {

        /*
         * すでに別の清掃操作を実行中の場合、
         * 無反応にせず理由を表示する。
         */
        if (_actionRequestId.value != null) {

            _successMessage.value =
                null

            _errorMessage.value =
                "別の清掃操作を処理中です"

            return
        }


        /*
         * 非同期処理開始前に処理中状態にすることで、
         * ボタン連打による二重実行を防止する。
         */
        _actionRequestId.value =
            actionId


        _errorMessage.value =
            null

        _successMessage.value =
            null


        viewModelScope.launch {

            try {

                // サーバー側の処理を実行
                action()


                /*
                 * 操作自体が成功した後に
                 * 最新状態を再取得する。
                 */
                val refreshed =
                    refresh(
                        showError = false
                    )


                _errorMessage.value =
                    null


                if (refreshed) {

                    _successMessage.value =
                        successMessage()

                } else {

                    /*
                     * 操作自体は成功しているため、
                     * 再取得だけ失敗したことを伝える。
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
                            it.isNotBlank()
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


    // =========================================================
    // メッセージを消す
    // =========================================================

    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }


    // =========================================================
    // 最新状態を取得
    // =========================================================

    private suspend fun refresh(
        showError: Boolean = true
    ): Boolean {

        return try {

            // ログインユーザーを取得
            val userId =
                repository.getCurrentUserId()


            _currentUserId.value =
                userId


            /*
             * ログイン済みの場合のみ
             * 清掃依頼一覧を取得する。
             */
            _requests.value =
                if (userId == null) {

                    emptyList()

                } else {

                    repository.loadActiveRequests()
                }


            if (showError) {

                _errorMessage.value =
                    null
            }


            true

        } catch (
            e: Exception
        ) {

            e.printStackTrace()


            if (showError) {

                _errorMessage.value =
                    e.message
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "清掃依頼の取得に失敗しました"
            }


            false
        }
    }


    // =========================================================
    // 自動更新開始
    // =========================================================

    private fun startAutoRefresh() {

        if (
            autoRefreshJob?.isActive ==
            true
        ) {

            return
        }


        autoRefreshJob =
            viewModelScope.launch {

                while (isActive) {

                    delay(
                        AUTO_REFRESH_INTERVAL
                    )


                    /*
                     * 自動更新では、
                     * 更新失敗時に突然エラー表示を出さない。
                     */
                    refresh(
                        showError = false
                    )
                }
            }
    }


    // =========================================================
    // ViewModel破棄
    // =========================================================

    override fun onCleared() {

        autoRefreshJob
            ?.cancel()


        autoRefreshJob =
            null


        super.onCleared()
    }
}