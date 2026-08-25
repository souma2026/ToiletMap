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


    private val _reviews =
        MutableStateFlow<List<ToiletReview>>(
            emptyList()
        )

    val reviews:
            StateFlow<List<ToiletReview>> =
        _reviews.asStateFlow()


    private val _isLoading =
        MutableStateFlow(
            false
        )

    val isLoading:
            StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _isPosting =
        MutableStateFlow(
            false
        )

    val isPosting:
            StateFlow<Boolean> =
        _isPosting.asStateFlow()


    private val _errorMessage =
        MutableStateFlow<String?>(
            null
        )

    val errorMessage:
            StateFlow<String?> =
        _errorMessage.asStateFlow()


    private val _successMessage =
        MutableStateFlow<String?>(
            null
        )

    val successMessage:
            StateFlow<String?> =
        _successMessage.asStateFlow()


    /*
     * 現在、口コミを表示しているトイレID。
     * 別のトイレへ切り替えたあとに古い通信結果が届いても、
     * 新しいトイレの画面を上書きしないために使用する。
     */
    private var activeToiletId:
            String? =
        null


    private var loadRequestId:
            Long =
        0L


    /*
     * 選択中のトイレが変わったときに呼ぶ。
     */
    fun prepareForToilet(
        toiletId: String?
    ) {

        if (
            activeToiletId ==
            toiletId
        ) {

            return
        }


        activeToiletId =
            toiletId

        loadRequestId +=
            1L

        _reviews.value =
            emptyList()

        _isLoading.value =
            false

        _isPosting.value =
            false

        clearMessages()
    }


    /*
     * 選択中トイレの口コミを取得する。
     */
    fun loadReviews(
        toiletId: String
    ) {

        if (
            toiletId.isBlank()
        ) {

            _reviews.value =
                emptyList()

            _errorMessage.value =
                "口コミを表示するトイレを選択してください"

            return
        }


        activeToiletId =
            toiletId

        val requestId =
            ++loadRequestId


        _isLoading.value =
            true

        _errorMessage.value =
            null

        _successMessage.value =
            null


        viewModelScope.launch {

            try {

                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
                    activeToiletId ==
                    toiletId &&
                    loadRequestId ==
                    requestId
                ) {

                    _reviews.value =
                        loadedReviews
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                if (
                    activeToiletId ==
                    toiletId &&
                    loadRequestId ==
                    requestId
                ) {

                    _errorMessage.value =
                        e.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "口コミの取得に失敗しました"
                }

            } finally {

                if (
                    activeToiletId ==
                    toiletId &&
                    loadRequestId ==
                    requestId
                ) {

                    _isLoading.value =
                        false
                }
            }
        }
    }


    /*
     * 口コミを投稿し、成功したら同じトイレの一覧を再取得する。
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


        activeToiletId =
            toiletId

        loadRequestId +=
            1L

        _isPosting.value =
            true

        /*
         * 投稿開始時点で、それ以前の一覧読込表示を終了する。
         * 古い読込処理はloadRequestIdにより結果を反映しない。
         */
        _isLoading.value =
            false

        _errorMessage.value =
            null

        _successMessage.value =
            null


        viewModelScope.launch {

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


                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
                    activeToiletId ==
                    toiletId
                ) {

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
                    activeToiletId ==
                    toiletId
                ) {

                    _errorMessage.value =
                        e.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "口コミの投稿に失敗しました"
                }

            } finally {

                if (
                    activeToiletId ==
                    toiletId
                ) {

                    _isPosting.value =
                        false
                }
            }
        }
    }


    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }
}
