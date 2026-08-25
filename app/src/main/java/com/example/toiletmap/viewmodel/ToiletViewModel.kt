package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ToiletRepository
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


class ToiletViewModel : ViewModel() {

    /*
     * =====================================
     * Repository
     * =====================================
     */
    private val repository =
        ToiletRepository()


    /*
     * =====================================
     * 設定
     * =====================================
     */
    companion object {

        /*
         * 1時間ごとに更新。
         */
        private const val
                AUTO_REFRESH_INTERVAL_MS =
            60 * 60 * 1000L


        /*
         * 地図停止後700ms待つ。
         */
        private const val
                MAP_LOAD_DEBOUNCE_MS =
            700L


        /*
         * 表示画面より上下左右25%広く取得。
         */
        private const val
                PREFETCH_MARGIN_RATIO =
            0.25
    }


    /*
     * =====================================
     * 緯度経度範囲
     * =====================================
     */
    private data class Bounds(

        val south: Double,

        val north: Double,

        val west: Double,

        val east: Double

    ) {

        fun contains(
            other: Bounds
        ): Boolean {

            return other.south >= south &&

                    other.north <= north &&

                    other.west >= west &&

                    other.east <= east
        }


        fun expanded(
            ratio: Double
        ): Bounds {

            val latitudeSpan =
                north - south


            val longitudeSpan =
                east - west


            val latitudeMargin =
                latitudeSpan *
                        ratio


            val longitudeMargin =
                longitudeSpan *
                        ratio


            return Bounds(

                south =
                    max(
                        -90.0,
                        south -
                                latitudeMargin
                    ),

                north =
                    min(
                        90.0,
                        north +
                                latitudeMargin
                    ),

                west =
                    max(
                        -180.0,
                        west -
                                longitudeMargin
                    ),

                east =
                    min(
                        180.0,
                        east +
                                longitudeMargin
                    )
            )
        }
    }


    /*
     * =====================================
     * 地図表示用トイレ一覧
     * =====================================
     *
     * Repositoryから直接公開。
     *
     * 今回からここには
     * 軽量データだけが入る。
     */
    val toilets:
            StateFlow<List<Toilet>> =
        repository.toilets


    /*
     * =====================================
     * 選択中トイレの詳細
     * =====================================
     *
     * ピン・検索結果を押したときだけ
     * Supabaseから1件取得する。
     */
    private val _selectedToilet =
        MutableStateFlow<Toilet?>(
            null
        )


    val selectedToilet:
            StateFlow<Toilet?> =
        _selectedToilet
            .asStateFlow()


    /*
     * =====================================
     * 表示範囲外でも必要なトイレ
     * =====================================
     *
     * 清掃依頼一覧などで使用する。
     */
    private val _supplementalToilets =

        MutableStateFlow<List<Toilet>>(
            emptyList()
        )


    val supplementalToilets:
            StateFlow<List<Toilet>> =

        _supplementalToilets
            .asStateFlow()


    private var lastSupplementalIds:
            Set<String> =
        emptySet()


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

        _errorMessage
            .asStateFlow()


    /*
     * =====================================
     * 現在の表示範囲
     * =====================================
     */
    private var lastVisibleBounds:
            Bounds? =
        null


    /*
     * =====================================
     * 最後に取得した範囲
     * =====================================
     *
     * 実際の画面 + 25%
     */
    private var lastLoadedBounds:
            Bounds? =
        null


    /*
     * =====================================
     * Job
     * =====================================
     */
    private var boundsLoadJob:
            Job? =
        null


    private var detailLoadJob:
            Job? =
        null


    private var autoRefreshJob:
            Job? =
        null


    /*
     * =====================================
     * 初期化
     * =====================================
     */
    init {

        /*
         * 起動時には全件取得しない。
         */
        startAutoRefresh()
    }


    /*
     * =====================================
     * MapLibreから表示範囲を受け取る
     * =====================================
     */
    fun onVisibleBoundsChanged(

        south: Double,

        north: Double,

        west: Double,

        east: Double

    ) {

        val visibleBounds =

            createValidBounds(

                south =
                    south,

                north =
                    north,

                west =
                    west,

                east =
                    east
            )

                ?: return


        lastVisibleBounds =
            visibleBounds


        /*
         * すでに25%先読み済み範囲内なら
         * 再通信しない。
         */
        val loadedBounds =
            lastLoadedBounds


        if (
            loadedBounds != null &&

            loadedBounds.contains(
                visibleBounds
            )
        ) {

            return
        }


        /*
         * 前の予約をキャンセル。
         */
        boundsLoadJob
            ?.cancel()


        boundsLoadJob =

            viewModelScope.launch {

                /*
                 * 地図が完全に止まってから
                 * 少し待つ。
                 */
                delay(
                    MAP_LOAD_DEBOUNCE_MS
                )


                loadBounds(

                    visibleBounds =
                        visibleBounds,

                    showError =
                        true
                )
            }
    }


    /*
     * =====================================
     * 現在範囲を強制更新
     * =====================================
     */
    fun loadToilets() {

        val visibleBounds =
            lastVisibleBounds

                ?: return


        boundsLoadJob
            ?.cancel()


        boundsLoadJob =

            viewModelScope.launch {

                loadBounds(

                    visibleBounds =
                        visibleBounds,

                    showError =
                        true
                )
            }
    }


    /*
     * =====================================
     * 範囲取得
     * =====================================
     */
    private suspend fun loadBounds(

        visibleBounds: Bounds,

        showError: Boolean

    ) {

        val fetchBounds =

            visibleBounds.expanded(
                PREFETCH_MARGIN_RATIO
            )


        try {

            repository
                .loadToiletsInBounds(

                    south =
                        fetchBounds.south,

                    north =
                        fetchBounds.north,

                    west =
                        fetchBounds.west,

                    east =
                        fetchBounds.east
                )


            lastLoadedBounds =
                fetchBounds


            _errorMessage.value =
                null

        } catch (
            e: Exception
        ) {

            e.printStackTrace()


            if (
                showError
            ) {

                _errorMessage.value =
                    "トイレ情報の取得に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * トイレ詳細を1件取得
     * =====================================
     */
    fun loadToiletDetail(

        toiletId: String,

        force: Boolean = false

    ) {

        val id =
            toiletId.trim()


        if (
            id.isBlank()
        ) {

            clearSelectedToilet()

            return
        }


        /*
         * すでに同じトイレの詳細を持っているなら
         * 通常は再通信しない。
         */
        if (
            !force &&

            _selectedToilet
                .value
                ?.id == id
        ) {

            return
        }


        detailLoadJob
            ?.cancel()


        /*
         * 違うトイレを選んだ場合は
         * 古い詳細を消しておく。
         */
        if (
            _selectedToilet
                .value
                ?.id != id
        ) {

            _selectedToilet.value =
                null
        }


        detailLoadJob =

            viewModelScope.launch {

                try {

                    val toilet =

                        repository
                            .loadToiletById(
                                id
                            )


                    if (
                        toilet != null
                    ) {

                        _selectedToilet.value =
                            toilet


                        _errorMessage.value =
                            null

                    } else {

                        _selectedToilet.value =
                            null


                        _errorMessage.value =
                            "トイレ情報が見つかりませんでした"
                    }

                } catch (
                    e: Exception
                ) {

                    e.printStackTrace()


                    _errorMessage.value =
                        "トイレ詳細の取得に失敗しました"
                }
            }
    }


    /*
     * =====================================
     * 選択中詳細を消す
     * =====================================
     */
    fun clearSelectedToilet() {

        detailLoadJob
            ?.cancel()


        detailLoadJob =
            null


        _selectedToilet.value =
            null
    }


    /*
     * =====================================
     * 清掃依頼等で必要なIDだけ取得
     * =====================================
     */
    fun loadSupplementalToilets(

        toiletIds:
        List<String>

    ) {

        val normalizedIds =

            toiletIds
                .filter {
                    it.isNotBlank()
                }
                .toSet()


        if (
            normalizedIds ==
            lastSupplementalIds
        ) {

            return
        }


        lastSupplementalIds =
            normalizedIds


        if (
            normalizedIds.isEmpty()
        ) {

            _supplementalToilets.value =
                emptyList()


            return
        }


        viewModelScope.launch {

            try {

                _supplementalToilets.value =

                    repository
                        .loadToiletsByIds(
                            normalizedIds.toList()
                        )

            } catch (
                e: Exception
            ) {

                /*
                 * 補助取得失敗だけでは
                 * 地図を止めない。
                 */
                e.printStackTrace()
            }
        }
    }


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    fun addToilet(

        toilet: Toilet

    ) {

        viewModelScope.launch {

            try {

                repository
                    .addToilet(
                        toilet
                    )


                /*
                 * 登録した1件の詳細を取得。
                 */
                _selectedToilet.value =

                    repository
                        .loadToiletById(
                            toilet.id
                        )

                        ?: toilet


                _errorMessage.value =
                    null


                /*
                 * 現在の範囲だけ更新。
                 */
                val visibleBounds =
                    lastVisibleBounds


                if (
                    visibleBounds != null
                ) {

                    loadBounds(

                        visibleBounds =
                            visibleBounds,

                        showError =
                            true
                    )
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                _errorMessage.value =

                    e.message

                        ?: "トイレの登録に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * 1時間ごとの自動更新
     * =====================================
     */
    private fun startAutoRefresh() {

        if (
            autoRefreshJob
                ?.isActive ==
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
                        AUTO_REFRESH_INTERVAL_MS
                    )


                    refreshToiletsSilently()
                }
            }
    }


    /*
     * =====================================
     * 自動更新
     * =====================================
     *
     * 全件ではなく現在の範囲だけ。
     */
    private suspend fun refreshToiletsSilently() {

        val visibleBounds =
            lastVisibleBounds

                ?: return


        loadBounds(

            visibleBounds =
                visibleBounds,

            showError =
                false
        )
    }


    /*
     * =====================================
     * Bounds検証
     * =====================================
     */
    private fun createValidBounds(

        south: Double,

        north: Double,

        west: Double,

        east: Double

    ): Bounds? {

        if (
            !south.isFinite() ||

            !north.isFinite() ||

            !west.isFinite() ||

            !east.isFinite()
        ) {

            return null
        }


        if (
            south >= north ||

            west >= east
        ) {

            return null
        }


        return Bounds(

            south =
                max(
                    -90.0,
                    south
                ),

            north =
                min(
                    90.0,
                    north
                ),

            west =
                max(
                    -180.0,
                    west
                ),

            east =
                min(
                    180.0,
                    east
                )
        )
    }


    /*
     * =====================================
     * エラーを消す
     * =====================================
     */
    fun clearErrorMessage() {

        _errorMessage.value =
            null
    }


    /*
     * =====================================
     * ViewModel破棄
     * =====================================
     */
    override fun onCleared() {

        boundsLoadJob
            ?.cancel()


        detailLoadJob
            ?.cancel()


        autoRefreshJob
            ?.cancel()


        boundsLoadJob =
            null


        detailLoadJob =
            null


        autoRefreshJob =
            null


        super.onCleared()
    }
}