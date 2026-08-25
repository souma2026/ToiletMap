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
         * 1時間ごとの自動更新。
         * ただし全件ではなく、最後に表示していた範囲だけ更新する。
         */
        private const val AUTO_REFRESH_INTERVAL_MS =
            60 * 60 * 1000L

        /*
         * 地図停止後、通信開始まで少し待つ。
         * 短時間にカメラ移動が続いた場合の無駄な通信を防ぐ。
         */
        private const val MAP_LOAD_DEBOUNCE_MS =
            700L

        /*
         * 実際の画面より上下左右25%広い範囲を先読みする。
         */
        private const val PREFETCH_MARGIN_RATIO =
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
                latitudeSpan * ratio

            val longitudeMargin =
                longitudeSpan * ratio

            return Bounds(
                south =
                    max(
                        -90.0,
                        south - latitudeMargin
                    ),
                north =
                    min(
                        90.0,
                        north + latitudeMargin
                    ),
                west =
                    max(
                        -180.0,
                        west - longitudeMargin
                    ),
                east =
                    min(
                        180.0,
                        east + longitudeMargin
                    )
            )
        }
    }


    /*
     * =====================================
     * トイレ一覧
     * =====================================
     */
    val toilets:
            StateFlow<List<Toilet>> =
        repository.toilets


    /*
     * =====================================
     * 表示範囲外でも必要なトイレ
     * =====================================
     *
     * 清掃依頼一覧などで必要なトイレだけを
     * ID指定で追加取得する。
     *
     * 地図描画には使わないので、
     * 画面外の大量マーカーが増えることはない。
     */
    private val _supplementalToilets =
        MutableStateFlow<List<Toilet>>(
            emptyList()
        )

    val supplementalToilets:
            StateFlow<List<Toilet>> =
        _supplementalToilets.asStateFlow()


    private var lastSupplementalIds:
            Set<String> =
        emptySet()


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
     * 現在表示している範囲
     * =====================================
     *
     * ここは「実際のスマホ画面」の範囲。
     */
    private var lastVisibleBounds:
            Bounds? =
        null


    /*
     * =====================================
     * 最後にSupabaseから取得した範囲
     * =====================================
     *
     * 実画面 + 25%余白。
     * 現在の画面がこの中に収まっている限り、
     * カメラ移動だけでは再通信しない。
     */
    private var lastLoadedBounds:
            Bounds? =
        null


    /*
     * =====================================
     * 地図範囲取得Job
     * =====================================
     */
    private var boundsLoadJob:
            Job? =
        null


    /*
     * =====================================
     * 自動更新Job
     * =====================================
     */
    private var autoRefreshJob:
            Job? =
        null


    /*
     * =====================================
     * ViewModel作成時
     * =====================================
     *
     * ここではSupabaseへアクセスしない。
     * MapLibreから最初の表示範囲が届いてから取得する。
     *
     * これによりアプリ起動直後の全件取得を廃止する。
     */
    init {
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
                south = south,
                north = north,
                west = west,
                east = east
            ) ?: return


        lastVisibleBounds =
            visibleBounds


        /*
         * すでに先読み済み範囲の中なら通信しない。
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
         * 以前予約していた通信をキャンセルし、
         * 最後のカメラ位置だけを対象にする。
         */
        boundsLoadJob
            ?.cancel()


        boundsLoadJob =
            viewModelScope.launch {

                delay(
                    MAP_LOAD_DEBOUNCE_MS
                )

                loadBounds(
                    visibleBounds = visibleBounds,
                    showError = true
                )
            }
    }


    /*
     * =====================================
     * 現在の範囲を強制再読込
     * =====================================
     *
     * MainActivityの既存コードから呼ばれる loadToilets() は、
     * 今後「全件取得」ではなく
     * 「現在表示中の範囲を再取得」という意味にする。
     *
     * 清掃状態が別端末で変わった場合にも使える。
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
                    visibleBounds = visibleBounds,
                    showError = true
                )
            }
    }


    /*
     * =====================================
     * 実際の範囲取得
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


            if (showError) {

                _errorMessage.value =
                    "トイレ情報の取得に失敗しました"
            }
        }
    }


    /*
     * =====================================
     * 自動更新開始
     * =====================================
     */
    private fun startAutoRefresh() {

        if (
            autoRefreshJob?.isActive ==
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
     * 1時間ごとの自動更新
     * =====================================
     *
     * 全件ではなく、現在表示中の地図範囲だけ再取得する。
     */
    private suspend fun refreshToiletsSilently() {

        val visibleBounds =
            lastVisibleBounds
                ?: return


        loadBounds(
            visibleBounds = visibleBounds,
            showError = false
        )
    }


    /*
     * =====================================
     * 清掃依頼などで必要なトイレだけ追加取得
     * =====================================
     */
    fun loadSupplementalToilets(
        toiletIds: List<String>
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
                 * 地図表示そのものは続けられるので、
                 * 補助取得の失敗では既存データを残す。
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


                _errorMessage.value =
                    null


                /*
                 * 登録完了後は、現在表示している範囲だけ再取得。
                 * 全件取得はしない。
                 */
                val visibleBounds =
                    lastVisibleBounds

                if (
                    visibleBounds != null
                ) {

                    loadBounds(
                        visibleBounds = visibleBounds,
                        showError = true
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
     * 表示済みエラーを消す
     * =====================================
     */
    fun clearErrorMessage() {

        _errorMessage.value =
            null
    }


    /*
     * =====================================
     * ViewModel破棄時
     * =====================================
     */
    override fun onCleared() {

        boundsLoadJob
            ?.cancel()

        boundsLoadJob =
            null


        autoRefreshJob
            ?.cancel()

        autoRefreshJob =
            null


        super.onCleared()
    }
}