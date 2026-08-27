package com.example.toiletmap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toiletmap.data.repository.ToiletRepository
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.CancellationException
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
         * 検索文字入力後の待ち時間。
         * 入力のたびにSupabaseへ通信しないため400ms待つ。
         */
        private const val
                SEARCH_DEBOUNCE_MS =
            400L


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
     * Supabase全体検索の結果
     * =====================================
     *
     * 地図に読み込まれている範囲とは別に保持する。
     * これにより、現在の画面外にあるトイレも検索できる。
     */
    private val _searchResults =
        MutableStateFlow<List<Toilet>>(
            emptyList()
        )


    val searchResults:
            StateFlow<List<Toilet>> =
        _searchResults
            .asStateFlow()


    private val _isSearching =
        MutableStateFlow(
            false
        )


    val isSearching:
            StateFlow<Boolean> =
        _isSearching
            .asStateFlow()


    private var latestSearchQuery =
        ""


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


    private var loadedSupplementalIds:
            Set<String> =
        emptySet()


    private var requestedSupplementalIds:
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
     * トイレ追加状態
     * =====================================
     *
     * INSERTが完了するまではtrue。
     * UI側はこの間、二重送信を行わない。
     */
    private val _isAdding =
        MutableStateFlow(
            false
        )


    val isAdding:
            StateFlow<Boolean> =
        _isAdding
            .asStateFlow()


    /*
     * =====================================
     * トイレ追加成功イベント
     * =====================================
     *
     * Supabase INSERTが成功した場合だけ値を入れる。
     * UI側で画面遷移・フォーム消去を行った後、
     * consumeAddSuccess() でnullへ戻す。
     */
    private val _addedToilet =
        MutableStateFlow<Toilet?>(
            null
        )


    val addedToilet:
            StateFlow<Toilet?> =
        _addedToilet
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
     * 範囲取得の世代番号
     * =====================================
     *
     * 新しい範囲取得を開始するたびに増やす。
     * 古いCoroutineが遅れて戻ってきても、
     * 現在の世代と一致しなければ結果を採用しない。
     */
    private var boundsRequestGeneration =
        0L


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


    private var searchLoadJob:
            Job? =
        null


    private var supplementalLoadJob:
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

            /*
             * すでに現在範囲のデータを持っている場合でも、
             * 別の古い範囲を取得中なら止める。
             *
             * 世代番号も進めることで、キャンセル済みの古いJobが
             * 遅れて完了しても結果を採用しない。
             */
            boundsRequestGeneration +=
                1L

            boundsLoadJob
                ?.cancel()

            boundsLoadJob =
                null

            return
        }


        requestBoundsLoad(

            visibleBounds =
                visibleBounds,

            showError =
                true,

            debounceMs =
                MAP_LOAD_DEBOUNCE_MS
        )
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


        requestBoundsLoad(

            visibleBounds =
                visibleBounds,

            showError =
                true,

            debounceMs =
                0L
        )
    }


    /*
     * =====================================
     * 範囲取得を単一Jobで開始
     * =====================================
     *
     * 地図移動・手動更新・1時間更新・登録後更新のすべてを
     * boundsLoadJob 1本に統一する。
     */
    private fun requestBoundsLoad(

        visibleBounds: Bounds,

        showError: Boolean,

        debounceMs: Long

    ) {

        boundsRequestGeneration +=
            1L


        val requestGeneration =
            boundsRequestGeneration


        boundsLoadJob
            ?.cancel()


        boundsLoadJob =
            viewModelScope.launch {

                try {

                    if (
                        debounceMs > 0L
                    ) {

                        delay(
                            debounceMs
                        )
                    }


                    loadBounds(

                        visibleBounds =
                            visibleBounds,

                        showError =
                            showError,

                        requestGeneration =
                            requestGeneration
                    )

                } finally {

                    /*
                     * 古いJobのfinallyが、新しく開始したJob参照を
                     * nullへ戻さないように世代番号を確認する。
                     */
                    if (
                        requestGeneration ==
                        boundsRequestGeneration
                    ) {

                        boundsLoadJob =
                            null
                    }
                }
            }
    }


    /*
     * =====================================
     * Supabase全体からトイレ名検索
     * =====================================
     *
     * MapScreen側ではローカル絞り込みを行わず、
     * Repository.searchToiletsByName() を使用する。
     *
     * 400msのデバウンスを入れ、連続入力時は
     * 直前の検索Jobをキャンセルする。
     */
    fun searchToilets(
        query: String
    ) {

        val normalizedQuery =
            query.trim()


        latestSearchQuery =
            normalizedQuery


        searchLoadJob
            ?.cancel()


        searchLoadJob =
            null


        if (normalizedQuery.isBlank()) {

            _searchResults.value =
                emptyList()

            _isSearching.value =
                false

            return
        }


        /*
         * 前の検索結果を表示したままにすると、
         * 新しい文字を入力した直後に古い候補が見えるため消す。
         */
        _searchResults.value =
            emptyList()

        _isSearching.value =
            true


        searchLoadJob =
            viewModelScope.launch {

                try {

                    delay(
                        SEARCH_DEBOUNCE_MS
                    )


                    val results =
                        repository
                            .searchToiletsByName(
                                normalizedQuery
                            )


                    /*
                     * 古い検索が遅れて返ってきても、
                     * 最新文字列と一致するときだけ採用する。
                     */
                    if (
                        latestSearchQuery ==
                        normalizedQuery
                    ) {

                        _searchResults.value =
                            results

                        _errorMessage.value =
                            null
                    }

                } catch (
                    e: CancellationException
                ) {

                    /*
                     * 新しい文字入力による正常なキャンセル。
                     * エラーとして扱わない。
                     */
                    throw e

                } catch (
                    e: Exception
                ) {

                    e.printStackTrace()


                    if (
                        latestSearchQuery ==
                        normalizedQuery
                    ) {

                        _searchResults.value =
                            emptyList()

                        _errorMessage.value =
                            "トイレの検索に失敗しました"
                    }

                } finally {

                    if (
                        latestSearchQuery ==
                        normalizedQuery
                    ) {

                        _isSearching.value =
                            false
                    }
                }
            }
    }


    /*
     * =====================================
     * 範囲取得
     * =====================================
     */
    private suspend fun loadBounds(

        visibleBounds: Bounds,

        showError: Boolean,

        requestGeneration: Long

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


            /*
             * 新しい範囲取得がすでに開始されている場合、
             * この結果は古いのでViewModel状態へ反映しない。
             */
            if (
                requestGeneration !=
                boundsRequestGeneration
            ) {

                return
            }


            lastLoadedBounds =
                fetchBounds


            _errorMessage.value =
                null

        } catch (
            e: CancellationException
        ) {

            /*
             * 新しい地図範囲の読み込みによる正常なキャンセル。
             * エラーとして表示しない。
             */
            throw e

        } catch (
            e: Exception
        ) {

            e.printStackTrace()


            if (
                showError &&
                requestGeneration ==
                boundsRequestGeneration
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
                    e: CancellationException
                ) {

                    /*
                     * 別のトイレを選択したことによる正常なキャンセル。
                     * エラーとして表示しない。
                     */
                    throw e

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
            normalizedIds.isEmpty()
        ) {

            supplementalLoadJob
                ?.cancel()


            supplementalLoadJob =
                null


            requestedSupplementalIds =
                emptySet()


            loadedSupplementalIds =
                emptySet()


            _supplementalToilets.value =
                emptyList()


            return
        }


        /*
         * The same request is already running.
         * Do not start a duplicate request.
         */
        if (
            normalizedIds ==
            requestedSupplementalIds &&

            supplementalLoadJob
                ?.isActive ==
            true
        ) {

            return
        }


        /*
         * A different request is running.
         * Cancel it before making the new request current.
         */
        if (
            supplementalLoadJob
                ?.isActive ==
            true
        ) {

            supplementalLoadJob
                ?.cancel()
        }


        supplementalLoadJob =
            null


        requestedSupplementalIds =
            normalizedIds


        /*
         * Remove records that are no longer requested while keeping
         * any overlapping records available during the refresh.
         */
        _supplementalToilets.value =
            _supplementalToilets
                .value
                .filter { toilet ->
                    toilet.id in normalizedIds
                }


        /*
         * These IDs were already loaded successfully.
         * If an older different request was running, it was cancelled above.
         */
        if (
            normalizedIds ==
            loadedSupplementalIds
        ) {

            return
        }


        supplementalLoadJob =

            viewModelScope.launch {

                try {

                    val loadedToilets =

                        repository
                            .loadToiletsByIds(
                                normalizedIds.toList()
                            )


                    /*
                     * Only the newest requested ID set may update the UI.
                     * This also protects against a cancelled old request
                     * returning after a newer request has started.
                     */
                    if (
                        requestedSupplementalIds ==
                        normalizedIds
                    ) {

                        _supplementalToilets.value =
                            loadedToilets


                        loadedSupplementalIds =
                            normalizedIds
                    }

                } catch (
                    e: CancellationException
                ) {

                    throw e

                } catch (
                    e: Exception
                ) {

                    /*
                     * Do not mark this ID set as successfully loaded.
                     * A later call with the same IDs can therefore retry.
                     */
                    e.printStackTrace()

                } finally {

                    if (
                        requestedSupplementalIds ==
                        normalizedIds
                    ) {

                        supplementalLoadJob =
                            null
                    }
                }
            }
    }


    /*
     * =====================================
     * Toilet add
     * =====================================
     */
    fun addToilet(

        toilet: Toilet

    ) {

        /*
         * 登録中の連打を防ぐ。
         */
        if (
            _isAdding.value
        ) {

            return
        }


        _isAdding.value =
            true


        _addedToilet.value =
            null


        _errorMessage.value =
            null


        viewModelScope.launch {

            try {

                /*
                 * =====================================
                 * Supabase INSERT
                 * =====================================
                 *
                 * ここが正常終了するまでは、
                 * UI側へ成功を通知しない。
                 */
                repository
                    .addToilet(
                        toilet
                    )


                /*
                 * =====================================
                 * INSERT成功
                 * =====================================
                 *
                 * この時点でDB登録は完了しているので、
                 * UIへ成功を通知する。
                 * 詳細再取得や地図再読込の完了は待たない。
                 */
                _selectedToilet.value =
                    toilet


                _errorMessage.value =
                    null


                _isAdding.value =
                    false


                _addedToilet.value =
                    toilet


                /*
                 * =====================================
                 * 登録後の詳細情報を再取得
                 * =====================================
                 *
                 * ここで通信に失敗しても、INSERT自体は
                 * 成功済みなので登録失敗には戻さない。
                 */
                try {

                    repository
                        .loadToiletById(
                            toilet.id
                        )
                        ?.let {
                                savedToilet ->

                            _selectedToilet.value =
                                savedToilet
                        }

                } catch (
                    e: CancellationException
                ) {

                    throw e

                } catch (
                    e: Exception
                ) {

                    e.printStackTrace()
                }


                /*
                 * 現在の範囲を静かに更新。
                 * ここが失敗しても登録成功は取り消さない。
                 */
                val visibleBounds =
                    lastVisibleBounds


                if (
                    visibleBounds != null
                ) {

                    requestBoundsLoad(

                        visibleBounds =
                            visibleBounds,

                        showError =
                            false,

                        debounceMs =
                            0L
                    )
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                e.printStackTrace()


                /*
                 * INSERT失敗時は成功イベントを出さない。
                 * そのため追加画面と入力内容は保持される。
                 */
                _addedToilet.value =
                    null


                _errorMessage.value =

                    e.message

                        ?: "トイレの登録に失敗しました"

            } finally {

                _isAdding.value =
                    false
            }
        }
    }


    /*
     * =====================================
     * 追加成功イベントを消費
     * =====================================
     */
    fun consumeAddSuccess() {

        _addedToilet.value =
            null
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
    private fun refreshToiletsSilently() {

        val visibleBounds =
            lastVisibleBounds

                ?: return


        /*
         * ユーザー操作による範囲取得が進行中なら、
         * その取得自体が最新データになるため
         * 1時間更新を重ねて開始しない。
         */
        if (
            boundsLoadJob
                ?.isActive ==
            true
        ) {

            return
        }


        requestBoundsLoad(

            visibleBounds =
                visibleBounds,

            showError =
                false,

            debounceMs =
                0L
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


        searchLoadJob
            ?.cancel()


        supplementalLoadJob
            ?.cancel()


        autoRefreshJob
            ?.cancel()


        boundsLoadJob =
            null


        detailLoadJob =
            null


        searchLoadJob =
            null


        supplementalLoadJob =
            null


        autoRefreshJob =
            null


        super.onCleared()
    }
}