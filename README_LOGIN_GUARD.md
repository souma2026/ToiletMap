# 清掃機能 未ログイン時の条件処理

## 動作
- NORMAL + 未ログイン: 「ログインして清掃を依頼」→ アカウント画面へ
- REQUESTED + 未ログイン: 「ログインして清掃を引き受ける」→ アカウント画面へ
- 未ログイン時は cleaning_requests を取得しないため、「清掃依頼を読み込み中」を表示し続けない
- ログイン済みなら従来どおり清掃依頼・引受処理を実行
- Repository のログイン必須チェックも残す（二重チェック）

## 変更ファイル
- app/src/main/java/com/example/toiletmap/screen/map/MapScreen.kt
- app/src/main/java/com/example/toiletmap/ui/ToiletMapApp.kt
