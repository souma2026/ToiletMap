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

    /*
     * =====================================
     * Repository
     * =====================================
     */
    private val repository =
        ReviewRepository()


    /*
     * =====================================
     * 現在口コミを表示しているトイレID
     * =====================================
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
     * 投稿中
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
     * 選択中トイレが変わったときの準備
     * =====================================
     *
     * 前のトイレの口コミやメッセージを
     * 次のトイレに持ち越さないようにする。
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
     * 口コミ一覧取得
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


        /*
         * 別のトイレの読み込みなら、
         * 表示対象を切り替える。
         */
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

                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                /*
                 * 通信中に別のトイレへ移動した場合、
                 * 古い結果を表示しない。
                 */
                if (
                    currentToiletId ==
                    toiletId
                ) {

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

        /*
         * 二重タップによる二重投稿を防ぐ。
         */
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


                /*
                 * 投稿成功後、最新の口コミ一覧を再取得する。
                 */
                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
                    currentToiletId ==
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
     * メッセージを消す
     * =====================================
     */
    fun clearMessages() {

        _errorMessage.value =
            null

        _successMessage.value =
            null
    }
}
