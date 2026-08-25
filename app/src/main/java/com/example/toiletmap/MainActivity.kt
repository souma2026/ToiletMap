package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.ToiletViewModel

class MainActivity : ComponentActivity() {

    private lateinit var mapController: MapLibreMapController
    private lateinit var toiletViewModel: ToiletViewModel

    /*
     * 地図のピン、検索結果、未清掃一覧のいずれかで
     * 選択されたトイレのIDを保持する。
     */
    private var selectedToiletId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toiletViewModel =
            ViewModelProvider(this)[ToiletViewModel::class.java]

        mapController =
            MapLibreMapController(
                activity = this,
                savedInstanceState = savedInstanceState
            )

        /* 地図上のピンを押したとき */
        mapController.setOnToiletMarkerClickListener { toilet ->
            selectedToiletId = toilet.id
        }

        setContent {
            ToiletMapTheme {
                val toilets by toiletViewModel.toilets.collectAsState()

                val selectedToilet =
                    toilets.firstOrNull { toilet ->
                        toilet.id == selectedToiletId
                    }

                val uncleanedToilets =
                    toilets
                        .filter { toilet ->
                            toilet.cleaningStatus == CleaningStatus.REQUESTED
                        }
                        .map { toilet ->
                            UncleanedToilet(
                                id = toilet.id,
                                name = toilet.name,
                                latitude = toilet.latitude,
                                longitude = toilet.longitude,
                                lastCleanedAtMillis = toilet.lastCleanedAtMillis
                            )
                        }

                /* Supabaseの一覧が変わったら地図のピンを更新する。 */
                LaunchedEffect(toilets) {
                    mapController.showToilets(toilets)
                }

                ToiletMapApp(
                    mapView = mapController.mapView,
                    toilets = toilets,
                    selectedToilet = selectedToilet,
                    uncleanedToilets = uncleanedToilets,

                    /*
                     * 未清掃一覧の「地図で見る」を押したとき。
                     * UncleanedToiletのIDから元のToiletを取得し、
                     * 選択状態にして地図を移動する。
                     */
                    onShowUncleanedToiletOnMap = { uncleanedToilet ->
                        val toilet =
                            toilets.firstOrNull { candidate ->
                                candidate.id == uncleanedToilet.id
                            }

                        if (toilet != null) {
                            selectedToiletId = toilet.id
                            mapController.focusOnToilet(toilet)
                        }
                    },

                    /* 検索結果を押したとき */
                    onSearchToiletSelected = { toilet ->
                        selectedToiletId = toilet.id
                        mapController.focusOnToilet(toilet)
                    },

                    /* トイレ詳細を閉じたとき */
                    onDismissSelectedToilet = {
                        selectedToiletId = null
                    },

                    /* 清掃依頼 */
                    onRequestCleaning = { toilet ->
                        toiletViewModel.requestCleaning(toilet.id)
                    },

                    /* 清掃完了 */
                    onMarkCleaned = { toilet ->
                        toiletViewModel.markCleaned(toilet.id)
                    },

                    /* トイレ追加 */
                    onAddToilet = { toilet ->
                        toiletViewModel.addToilet(toilet)
                        selectedToiletId = toilet.id
                        mapController.focusOnToilet(toilet)
                    },

                    /* 現在地機能は一旦無効 */
                    onCurrentLocationRequested = {
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapController.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapController.onLowMemory()
    }
}