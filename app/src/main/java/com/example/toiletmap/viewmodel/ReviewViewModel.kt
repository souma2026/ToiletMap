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
     */
    fun prepareForToilet(
        toiletId: String?
    ) {

        if (
            toiletId
        ) {

            return
        }


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


            toiletId



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


                if (
                ) {

                    _reviews.value =
                        loadedReviews
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                if (
                ) {

                    _errorMessage.value =
                        e.message
                            ?: "口コミの取得に失敗しました"
                }

            } finally {

                if (
                ) {

                    _isLoading.value =
                        false
                }
            }
        }
    }


    /*
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


            toiletId


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


                val loadedReviews =
                    repository
                        .loadReviews(
                            toiletId
                        )


                if (
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
                    toiletId
                ) {

                    _errorMessage.value =
                        e.message
                            ?: "口コミの投稿に失敗しました"
                }

            } finally {

                if (
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
