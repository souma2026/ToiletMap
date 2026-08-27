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

    private val repository =
        ReviewRepository()


    /*
     * 現在口コミを表示しているトイレ
     */
    private var currentToiletId:
            String? =
        null


    /*
     * =====================================
     * 口コミ一覧
     * =====================================
     */
    private val _reviews =
        MutableStateFlow<List<ToiletReview>>(
            emptyList()
        )


    val reviews:
            StateFlow<List<ToiletReview>> =
        _reviews.asStateFlow()


    /*
     * =====================================
     * レビュー用ログインユーザーID
     * =====================================
     */
    private val _currentUserId =
        MutableStateFlow<String?>(
            null
        )


    val currentUserId:
            StateFlow<String?> =
        _currentUserId.asStateFlow()


    /*
     * =====================================
     * 読み込み中
     * =====================================
     */
    private val _isLoading =
        MutableStateFlow(
            false
        )


    val isLoading:
            StateFlow<Boolean> =
        _isLoading.asStateFlow()


    /*
     * =====================================
     * 投稿・削除処理中
     * =====================================
     */
    private val _isPosting =
        MutableStateFlow(
            false
        )


    val isPosting:
            StateFlow<Boolean> =
        _isPosting.asStateFlow()


    /*
     * =====================================
     * エラー
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
     * 成功メッセージ
     * =====================================
     */
    private val _successMessage =
        MutableStateFlow<String?>(
            null
        )


    val successMessage:
            StateFlow<String?> =
        _successMessage.asStateFlow()


    /*
     * =====================================
     * 選択したトイレ変更
     * =====================================
     */
    fun prepareForToilet(
        toiletId: String?
    ) {

        if (
            currentToiletId ==
            toiletId
        ) {

            return
        }


        currentToiletId =
            toiletId


        _reviews.value =
            emptyList()


        _isLoading.value =
            false


        _isPosting.value =
            false


        clearMessages()
    }


    /*
     * =====================================
     * 口コミ取得
     * =====================================
     */
    fun loadReviews(
        toiletId: String
    ) {

        if (
            toiletId.isBlank()
        ) {

            _errorMessage.value =
                "口コミを表示するトイレを選択してください"

            return
        }


        if (
            currentToiletId !=
            toiletId
        ) {

            currentToiletId =
                toiletId

            _reviews.value =
                emptyList()
        }


        viewModelScope.launch {

            _isLoading.value =
                true

            _errorMessage.value =
                null


            try {

                val loggedInUserId =
                    repository
                        .getCurrentUserId()


                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                /*
                 * 別のトイレへ移動済みなら
                 * 古い結果を表示しない。
                 */
                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _currentUserId.value =
                        loggedInUserId

                    _reviews.value =
                        loadedReviews
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _errorMessage.value =
                        e.message
                            ?: "口コミの取得に失敗しました"
                }

            } finally {

                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _isLoading.value =
                        false
                }
            }
        }
    }


    /*
     * =====================================
     * 口コミ投稿
     * =====================================
     */
    fun addReview(
        toiletId: String,
        rating: Int,
        comment: String
    ) {

        if (
            _isPosting.value
        ) {

            return
        }


        if (
            currentToiletId !=
            toiletId
        ) {

            currentToiletId =
                toiletId

            _reviews.value =
                emptyList()
        }


        viewModelScope.launch {

            _isPosting.value =
                true

            _errorMessage.value =
                null

            _successMessage.value =
                null


            try {

                repository
                    .addReview(
                        toiletId =
                            toiletId,

                        rating =
                            rating,

                        comment =
                            comment
                    )


                val loggedInUserId =
                    repository
                        .getCurrentUserId()


                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _currentUserId.value =
                        loggedInUserId

                    _reviews.value =
                        loadedReviews

                    _successMessage.value =
                        "口コミを投稿しました"
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _errorMessage.value =
                        e.message
                            ?: "口コミの投稿に失敗しました"
                }

            } finally {

                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _isPosting.value =
                        false
                }
            }
        }
    }


    /*
     * =====================================
     * 自分の口コミを削除
     * =====================================
     */
    fun deleteReview(
        toiletId: String,
        reviewId: String
    ) {

        if (
            _isPosting.value
        ) {

            return
        }


        if (
            toiletId.isBlank() ||
            reviewId.isBlank()
        ) {

            _errorMessage.value =
                "削除する口コミを選択してください"

            return
        }


        if (
            currentToiletId !=
            toiletId
        ) {

            currentToiletId =
                toiletId

            _reviews.value =
                emptyList()
        }


        viewModelScope.launch {

            _isPosting.value =
                true

            _errorMessage.value =
                null

            _successMessage.value =
                null


            try {

                repository
                    .deleteReview(
                        reviewId
                    )


                /*
                 * 削除後に一覧再取得。
                 * 自分の口コミがなくなるので、
                 * 投稿欄が再表示される。
                 */
                val loggedInUserId =
                    repository
                        .getCurrentUserId()


                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _currentUserId.value =
                        loggedInUserId

                    _reviews.value =
                        loadedReviews

                    _successMessage.value =
                        "口コミを削除しました"
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _errorMessage.value =
                        e.message
                            ?: "口コミの削除に失敗しました"
                }

            } finally {

                if (
                    currentToiletId ==
                    toiletId
                ) {

                    _isPosting.value =
                        false
                }
            }
        }
    }


    /*
     * =====================================
     * メッセージ削除
     * =====================================
     */
    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }
}