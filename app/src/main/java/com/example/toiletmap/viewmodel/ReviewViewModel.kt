package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ReviewRepository
import com.example.toiletmap.model.ToiletReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val repository = ReviewRepository()

    // =========================================
    // 口コミ一覧
    // =========================================

    private val _reviews =
        MutableStateFlow<List<ToiletReview>>(emptyList())

    val reviews: StateFlow<List<ToiletReview>> =
        _reviews.asStateFlow()


    // =========================================
    // 読み込み中
    // =========================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // =========================================
    // 投稿中
    // =========================================

    private val _isPosting =
        MutableStateFlow(false)

    val isPosting: StateFlow<Boolean> =
        _isPosting.asStateFlow()


    // =========================================
    // エラーメッセージ
    // =========================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    // =========================================
    // 成功メッセージ
    // =========================================

    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    // =========================================
    // 現在表示しているトイレ
    // =========================================

    private var activeToiletId: String? = null

    private var loadRequestId: Long = 0L


    // =========================================
    // トイレ切り替え
    // =========================================

    fun prepareForToilet(
        toiletId: String?
    ) {

        if (activeToiletId == toiletId) {
            return
        }

        activeToiletId = toiletId

        loadRequestId++

        _reviews.value = emptyList()

        _isLoading.value = false

        _isPosting.value = false

        clearMessages()
    }


    // =========================================
    // 口コミ取得
    // =========================================

    fun loadReviews(
        toiletId: String
    ) {

        if (toiletId.isBlank()) {

            _reviews.value = emptyList()

            _errorMessage.value =
                "口コミを表示するトイレを選択してください"

            return
        }

        activeToiletId = toiletId

        val requestId = ++loadRequestId

        _isLoading.value = true

        _errorMessage.value = null

        _successMessage.value = null


        viewModelScope.launch {

            try {

                val loadedReviews =
                    repository.loadReviews(
                        toiletId
                    )


                // 別のトイレへ移動していない場合だけ反映
                if (
                    activeToiletId == toiletId &&
                    loadRequestId == requestId
                ) {

                    _reviews.value =
                        loadedReviews
                }

            } catch (e: Exception) {

                e.printStackTrace()


                if (
                    activeToiletId == toiletId &&
                    loadRequestId == requestId
                ) {

                    _errorMessage.value =
                        e.message
                            ?.takeIf { message ->
                                message.isNotBlank()
                            }
                            ?: "口コミの取得に失敗しました"
                }

            } finally {

                if (
                    activeToiletId == toiletId &&
                    loadRequestId == requestId
                ) {

                    _isLoading.value =
                        false
                }
            }
        }
    }


    // =========================================
    // 口コミ投稿
    // =========================================

    fun addReview(
        toiletId: String,
        rating: Int,
        comment: String
    ) {

        if (_isPosting.value) {
            return
        }


        if (toiletId.isBlank()) {

            _errorMessage.value =
                "口コミを投稿するトイレを選択してください"

            return
        }


        if (rating !in 1..5) {

            _errorMessage.value =
                "評価は1〜5で選択してください"

            return
        }


        activeToiletId = toiletId

        // 古い取得処理を無効にする
        loadRequestId++

        _isPosting.value = true

        _isLoading.value = false

        _errorMessage.value = null

        _successMessage.value = null


        viewModelScope.launch {

            try {

                // 口コミ投稿
                repository.addReview(
                    toiletId = toiletId,
                    rating = rating,
                    comment = comment
                )


                // 投稿後に口コミ一覧を再取得
                val loadedReviews =
                    repository.loadReviews(
                        toiletId
                    )


                if (activeToiletId == toiletId) {

                    _reviews.value =
                        loadedReviews

                    _successMessage.value =
                        "口コミを投稿しました"
                }

            } catch (e: Exception) {

                e.printStackTrace()


                if (activeToiletId == toiletId) {

                    _errorMessage.value =
                        e.message
                            ?.takeIf { message ->
                                message.isNotBlank()
                            }
                            ?: "口コミの投稿に失敗しました"
                }

            } finally {

                if (activeToiletId == toiletId) {

                    _isPosting.value =
                        false
                }
            }
        }
    }


    // =========================================
    // メッセージ削除
    // =========================================

    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }
}