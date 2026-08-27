package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ReviewRepository
import com.example.toiletmap.model.ToiletReview
import kotlinx.coroutines.CancellationException
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
     * 現在ログイン中ユーザーID
     * =====================================
     *
     * 清掃機能のログイン状態には依存させず、
     * ReviewRepositoryから直接取得する。
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
     * 口コミ操作中
     * =====================================
     *
     * 投稿中・削除中の両方でtrueになる。
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
         * 別のトイレの読み込みなら
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

                /*
                 * ログインユーザー取得
                 */
                val loggedInUserId =
                    repository
                        .getCurrentUserId()


                /*
                 * 口コミ取得
                 *
                 * ReviewRepository側で
                 * created_at DESC + limit
                 * を実行する。
                 */
                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                /*
                 * 通信中に別のトイレへ移動した場合、
                 * 古い通信結果を画面へ反映しない。
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
                e: CancellationException
            ) {

                /*
                 * Coroutineキャンセルを
                 * 通常エラーとして扱わない。
                 */
                throw e


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
                            ?.takeIf {
                                    message ->

                                message.isNotBlank()
                            }
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
         * 二重タップによる
         * 二重操作を防止。
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
                 * 投稿成功後、
                 * 最新の口コミ一覧と
                 * ログインユーザーを再取得。
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
                        "口コミを投稿しました"
                }


            } catch (
                e: CancellationException
            ) {

                /*
                 * Coroutineキャンセルは
                 * エラー表示しない。
                 */
                throw e


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
                            ?.takeIf {
                                    message ->

                                message.isNotBlank()
                            }
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

        /*
         * 投稿・削除処理中なら
         * 二重操作しない。
         */
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

                /*
                 * Supabaseから口コミ削除
                 */
                repository
                    .deleteReview(
                        reviewId
                    )


                /*
                 * 削除後、
                 * 最新の口コミ一覧を再取得。
                 *
                 * 自分の口コミがなくなるので
                 * 再度投稿できる状態になる。
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
                e: CancellationException
            ) {

                /*
                 * Coroutineキャンセルは
                 * 通常エラーとして扱わない。
                 */
                throw e


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
                            ?.takeIf {
                                    message ->

                                message.isNotBlank()
                            }
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