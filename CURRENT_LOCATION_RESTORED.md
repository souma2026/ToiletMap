# 現在地表示機能 復旧内容

このプロジェクトは、受け取った最新の ToiletMap をベースに現在地機能を復旧したものです。

## 復旧した機能

- Android の位置情報権限要求
- MapLibre LocationComponent による青い現在地表示
- 現在地のパルス表示
- 地図右側の現在地ボタンから現在地へ移動
- 位置情報の状態表示
  - 緑: 現在地取得済み
  - グレー: 確認中
  - 赤 + !: 権限なし / スマホ本体の位置情報OFF
  - オレンジ + !: 位置情報ONだが測位できない
- 異常時に現在地ボタンを押すと原因を日本語で表示

## 変更したファイル

- app/src/main/java/com/example/toiletmap/MainActivity.kt
- app/src/main/java/com/example/toiletmap/screen/map/MapLibreMapController.kt
- app/src/main/java/com/example/toiletmap/screen/map/MapScreen.kt
- app/src/main/java/com/example/toiletmap/ui/ToiletMapApp.kt

口コミ機能、ReviewViewModel、清掃ポイント機能、Supabase RPC は元プロジェクトの内容を維持しています。
