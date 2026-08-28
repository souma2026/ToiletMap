package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.CleaningRepository
import com.example.toiletmap.model.CleaningRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

sealed interface CleaningUiEvent {
    data class CleaningCompleted(
        val earnedPoints: Int,
        val remainingRewardPoints: Int,
        val refreshSucceeded: Boolean
    ) : CleaningUiEvent
}

class CleaningViewModel : ViewModel() {

    companion object {
        private val AUTO_REFRESH_INTERVAL = 1.hours
    }

    private val repository = CleaningRepository()

    private val _requests =
        MutableStateFlow<List<CleaningRequest>>(emptyList())
    val requests: StateFlow<List<CleaningRequest>> =
        _requests.asStateFlow()

    private val _currentUserId =
        MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> =
        _currentUserId.asStateFlow()

    private val _requestPoints =
        MutableStateFlow(0)
    val requestPoints: StateFlow<Int> =
        _requestPoints.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _actionRequestId =
        MutableStateFlow<String?>(null)
    val actionRequestId: StateFlow<String?> =
        _actionRequestId.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private val _successMessage =
        MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()

    private val _events =
        MutableSharedFlow<CleaningUiEvent>(
            extraBufferCapacity = 1
        )
    val events: SharedFlow<CleaningUiEvent> =
        _events.asSharedFlow()

    private var autoRefreshJob: Job? = null

    init {
        loadRequests()
        startAutoRefresh()
    }

    fun loadRequests() {
        if (_isLoading.value) {
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                refresh(showError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestCleaning(
        toiletId: String,
        requestPoints: Int
    ) {
        val rewardPoints =
            when (requestPoints) {
                4 -> 5
                8 -> 10
                12 -> 15
                else -> 0
            }

        runCleaningAction(
            actionId = toiletId,
            failureMessage = "清掃依頼に失敗しました",
            successMessage = {
                "${requestPoints}ptを使って清掃依頼を出しました（報酬${rewardPoints}pt）"
            },
            refreshFailureMessage =
                "清掃依頼は完了しました。表示を更新できなかったため、画面を更新してください"
        ) {
            repository.requestCleaning(
                toiletId = toiletId,
                requestPoints = requestPoints
            )
        }
    }

    fun acceptCleaning(
        requestId: String
    ) {
        runCleaningAction(
            actionId = requestId,
            failureMessage = "清掃の引き受けに失敗しました",
            successMessage = {
                "清掃を引き受けました"
            },
            refreshFailureMessage =
                "清掃の引き受けは完了しました。表示を更新できなかったため、画面を更新してください"
        ) {
            repository.acceptCleaning(requestId)
        }
    }

    fun completeCleaning(
        requestId: String
    ) {
        if (_actionRequestId.value != null) {
            _successMessage.value = null
            _errorMessage.value = "別の清掃操作を処理中です"
            return
        }

        _actionRequestId.value = requestId
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val result =
                    repository.completeCleaning(requestId)

                val refreshed =
                    refresh(showError = false)

                _errorMessage.value = null

                _events.emit(
                    CleaningUiEvent.CleaningCompleted(
                        earnedPoints = result.earnedPoints,
                        remainingRewardPoints = result.remainingRewardPoints,
                        refreshSucceeded = refreshed
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()

                _successMessage.value = null
                _errorMessage.value =
                    e.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "清掃完了の記録に失敗しました"
            } finally {
                if (_actionRequestId.value == requestId) {
                    _actionRequestId.value = null
                }
            }
        }
    }

    fun cancelCleaning(
        requestId: String
    ) {
        runCleaningAction(
            actionId = requestId,
            failureMessage = "清掃担当のキャンセルに失敗しました",
            successMessage = {
                "清掃担当をキャンセルしました"
            },
            refreshFailureMessage =
                "キャンセルは完了しました。表示を更新できなかったため、画面を更新してください"
        ) {
            repository.cancelCleaning(requestId)
        }
    }

    fun cancelCleaningRequest(
        requestId: String
    ) {
        runCleaningAction(
            actionId = requestId,
            failureMessage = "清掃依頼の取り消しに失敗しました",
            successMessage = {
                "清掃依頼を取り消しました"
            },
            refreshFailureMessage =
                "清掃依頼は取り消されました。表示を更新できなかったため、画面を更新してください"
        ) {
            repository.cancelCleaningRequest(requestId)
        }
    }

    private fun runCleaningAction(
        actionId: String,
        failureMessage: String,
        successMessage: () -> String,
        refreshFailureMessage: String,
        action: suspend () -> Unit
    ) {
        if (_actionRequestId.value != null) {
            _successMessage.value = null
            _errorMessage.value = "別の清掃操作を処理中です"
            return
        }

        _actionRequestId.value = actionId
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                action()

                val refreshed =
                    refresh(showError = false)

                _errorMessage.value = null
                _successMessage.value =
                    if (refreshed) {
                        successMessage()
                    } else {
                        refreshFailureMessage
                    }
            } catch (e: Exception) {
                e.printStackTrace()

                _successMessage.value = null
                _errorMessage.value =
                    e.message
                        ?.takeIf { it.isNotBlank() }
                        ?: failureMessage
            } finally {
                if (_actionRequestId.value == actionId) {
                    _actionRequestId.value = null
                }
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private suspend fun refresh(
        showError: Boolean = true
    ): Boolean {
        return try {
            val userId =
                repository.getCurrentUserId()

            _currentUserId.value = userId

            if (userId == null) {
                _requestPoints.value = 0
                _requests.value = emptyList()
            } else {
                _requestPoints.value =
                    repository.loadCurrentRequestPoints(userId)

                _requests.value =
                    repository.loadActiveRequests()
            }

            if (showError) {
                _errorMessage.value = null
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()

            if (showError) {
                _errorMessage.value =
                    e.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "清掃依頼の取得に失敗しました"
            }

            false
        }
    }

    private fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) {
            return
        }

        autoRefreshJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(AUTO_REFRESH_INTERVAL)
                    refresh(showError = false)
                }
            }
    }

    override fun onCleared() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        super.onCleared()
    }
}