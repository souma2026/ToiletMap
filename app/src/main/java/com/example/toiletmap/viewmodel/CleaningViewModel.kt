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


class CleaningViewModel : ViewModel() {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MS =
            60 * 60 * 1000L
    }


    private val repository =
        CleaningRepository()


    private val _requests =
        MutableStateFlow<List<CleaningRequest>>(
            emptyList()
        )

    val requests: StateFlow<List<CleaningRequest>> =
        _requests.asStateFlow()


    private val _currentUserId =
        MutableStateFlow<String?>(
            null
        )

    val currentUserId: StateFlow<String?> =
        _currentUserId.asStateFlow()


    private val _isLoading =
        MutableStateFlow(
            false
        )

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _actionRequestId =
        MutableStateFlow<String?>(
            null
        )

    val actionRequestId: StateFlow<String?> =
        _actionRequestId.asStateFlow()


    private val _errorMessage =
        MutableStateFlow<String?>(
            null
        )

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    private val _successMessage =
        MutableStateFlow<String?>(
            null
        )

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    private var autoRefreshJob: Job? =
        null


    init {

        loadRequests()
        startAutoRefresh()
    }


    fun loadRequests() {

        /*
         * init直後とMap画面表示時の再読込が重なっても、
         * 同じ通信を二重に起動しない。
         */
        if (_isLoading.value) {
            return
        }

        _isLoading.value =
            true

        viewModelScope.launch {

            try {

                refresh()

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    fun requestCleaning(
        toiletId: String,
        rewardPoints: Int
    ) {

        if (_actionRequestId.value != null) {
            return
        }

        viewModelScope.launch {

            _actionRequestId.value =
                toiletId

            try {

                repository.requestCleaning(
                    toiletId = toiletId,
                    rewardPoints = rewardPoints
                )

                val refreshed =
                    refresh()

                if (refreshed) {

                    _errorMessage.value =
                        null

                    _successMessage.value =
                        "清掃依頼を出しました"

                } else {

                    _successMessage.value =
                        "操作は完了しました。表示を更新できなかったため、更新ボタンを押してください"
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()

                _errorMessage.value =
                    e.message
                        ?: "清掃依頼に失敗しました"

            } finally {

                _actionRequestId.value =
                    null
            }
        }
    }


    fun acceptCleaning(
        requestId: String
    ) {

        if (_actionRequestId.value != null) {
            return
        }

        viewModelScope.launch {

            _actionRequestId.value =
                requestId

            try {

                repository.acceptCleaning(
                    requestId
                )

                val refreshed =
                    refresh()

                if (refreshed) {

                    _errorMessage.value =
                        null

                    _successMessage.value =
                        "清掃を引き受けました"

                } else {

                    _successMessage.value =
                        "操作は完了しました。表示を更新できなかったため、更新ボタンを押してください"
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()

                _errorMessage.value =
                    e.message
                        ?: "清掃の引き受けに失敗しました"

            } finally {

                _actionRequestId.value =
                    null
            }
        }
    }


    fun completeCleaning(
        requestId: String
    ) {

        if (_actionRequestId.value != null) {
            return
        }

        viewModelScope.launch {

            _actionRequestId.value =
                requestId

            try {

                repository.completeCleaning(
                    requestId
                )

                val refreshed =
                    refresh()

                if (refreshed) {

                    _errorMessage.value =
                        null

                    _successMessage.value =
                        "清掃を完了しました。報酬ポイントを受け取りました"

                } else {

                    _successMessage.value =
                        "清掃完了は登録されました。表示を更新できなかったため、更新ボタンを押してください"
                }

            } catch (e: Exception) {

                e.printStackTrace()

                _errorMessage.value =
                    e.message
                        ?: "清掃完了の登録に失敗しました"

            } finally {

                _actionRequestId.value =
                    null
            }
        }
    }


    fun cancelCleaning(
        requestId: String
    ) {

        if (_actionRequestId.value != null) {
            return
        }

        viewModelScope.launch {

            _actionRequestId.value =
                requestId

            try {

                repository.cancelCleaning(
                    requestId
                )

                val refreshed =
                    refresh()

                if (refreshed) {

                    _errorMessage.value =
                        null

                    _successMessage.value =
                        "清掃担当をキャンセルしました"

                } else {

                    _successMessage.value =
                        "操作は完了しました。表示を更新できなかったため、更新ボタンを押してください"
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()

                _errorMessage.value =
                    e.message
                        ?: "清掃担当のキャンセルに失敗しました"

            } finally {

                _actionRequestId.value =
                    null
            }
        }
    }


    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }


    private suspend fun refresh(): Boolean {

        try {

            val userId =
                repository.getCurrentUserId()

            _currentUserId.value =
                userId

            /*
             * cleaning_requests は認証済みユーザーだけが参照できる。
             * 未ログイン時は通信せず空一覧にすることで、
             * 起動直後に不要なRLSエラーを表示しない。
             */
            _requests.value =
                if (userId == null) {

                    emptyList()

                } else {

                    repository.loadActiveRequests()
                }

            _errorMessage.value =
                null

            return true

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            _errorMessage.value =
                "清掃依頼の取得に失敗しました"

            return false
        }
    }


    private fun startAutoRefresh() {

        if (autoRefreshJob?.isActive == true) {
            return
        }

        autoRefreshJob =
            viewModelScope.launch {

                while (isActive) {

                    delay(
                        AUTO_REFRESH_INTERVAL_MS
                    )

                    refresh()
                }
            }
    }


    override fun onCleared() {

        autoRefreshJob
            ?.cancel()

        autoRefreshJob =
            null

        super.onCleared()
    }
}
