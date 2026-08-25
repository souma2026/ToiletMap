# 清掃依頼・清掃担当機能（第1〜第3段階）

この実装は、`origin/main` のコミット `9260f18776d91c5b2b6c2c9bee3231983100583d` を基準にしています。

## 今回実装した範囲

1. 清掃状態を4段階へ拡張
   - `NORMAL`：通常
   - `REQUESTED`：清掃依頼中
   - `IN_PROGRESS`：清掃中
   - `COMPLETED`：履歴保存用
2. 通常状態のトイレから清掃依頼を作成
3. 清掃依頼中のトイレを、依頼者以外のログインユーザーが引き受ける
4. 引き受け後はトイレを `IN_PROGRESS` に変更し、地図ピンを青色にする
5. 下部メニューの「更新」を「清掃」へ変更
6. 清掃画面に「現在担当している清掃」を表示
7. 清掃画面から対象トイレを地図で確認
8. 担当者が清掃をキャンセルすると `REQUESTED` へ戻す
9. Supabase RPCの行ロックで、同じ依頼を複数人が同時に引き受けないようにする

## 画面上の流れ

```text
通常（赤ピン）
  ↓ 清掃を依頼する
清掃依頼中（黄ピン）
  ↓ 別ユーザーが清掃を引き受ける
清掃中（青ピン）
  ↓ 清掃画面で担当状況を確認
```

依頼者本人には「自分の清掃依頼です」と表示し、自分の依頼を自分で引き受けることはできません。

## Supabaseで最初に行う作業

Supabase Dashboard の **SQL Editor** を開き、次のファイルを全文貼り付けて **Run** してください。

```text
supabase/cleaning_requests_stage1_3.sql
```

このSQLは以下を作成します。

- `cleaning_requests` テーブル
- `request_cleaning(p_toilet_id)` RPC
- `accept_cleaning(p_cleaning_request_id)` RPC
- `cancel_cleaning(p_cleaning_request_id)` RPC
- 有効な清掃依頼を1トイレ1件に制限するインデックス
- 認証済みユーザー向けの参照RLS

また、旧RPCを削除します。

```text
request_cleaning_with_points(text, integer)
mark_toilet_cleaned_with_points(text)
```

旧SQL `supabase/cleaning_points_setup.sql` は新仕様と競合するため、このプロジェクトから削除しています。

## 今回はまだ有効にしていない機能

仕様書の段階順に進めるため、以下は次回以降です。

- 「清掃完了」の実処理
- 清掃完了時の報酬ポイント付与
- 毎日の清掃依頼ポイント
- ポイント増減履歴
- 商品交換

今回の `cleaning_requests` には、将来のポイント処理に備えて以下の列を用意しています。

```text
request_points_used
reward_points
```

現段階では `request_points_used = 0`、予定報酬は `5pt` です。清掃画面の「清掃完了」は、次段階で有効にすることが分かるよう無効表示にしています。

## 主な変更ファイル

```text
app/src/main/java/com/example/toiletmap/MainActivity.kt
app/src/main/java/com/example/toiletmap/model/Toilet.kt
app/src/main/java/com/example/toiletmap/model/CleaningRequest.kt
app/src/main/java/com/example/toiletmap/data/repository/CleaningRepository.kt
app/src/main/java/com/example/toiletmap/data/repository/ToiletRepository.kt
app/src/main/java/com/example/toiletmap/viewmodel/CleaningViewModel.kt
app/src/main/java/com/example/toiletmap/viewmodel/ToiletViewModel.kt
app/src/main/java/com/example/toiletmap/viewmodel/ReviewViewModel.kt
app/src/main/java/com/example/toiletmap/screen/cleaning/CleaningScreen.kt
app/src/main/java/com/example/toiletmap/screen/cleaning/CleaningDateFormatter.kt
app/src/main/java/com/example/toiletmap/screen/map/MapScreen.kt
app/src/main/java/com/example/toiletmap/screen/map/MapLibreMapController.kt
app/src/main/java/com/example/toiletmap/ui/ToiletMapApp.kt
app/src/main/java/com/example/toiletmap/ui/components/BottomNavigationBar.kt
supabase/cleaning_requests_stage1_3.sql
```

口コミ機能については、最新版の `MainActivity.kt` が参照している
`ReviewViewModel.kt` も同梱し、既存の口コミ投稿導線を維持しています。

## 実装時の確認内容

- ベースコミット：`9260f18776d91c5b2b6c2c9bee3231983100583d`
- Gitコンフリクト記号が残っていないこと
- Kotlinファイルの括弧・文字列・コメントの対応
- `CleaningStatus` / `CleaningRequest` / 日時変換の単体確認
- `CleaningViewModel` の状態遷移をスタブ環境でコンパイル確認
- SQLをトランザクション化し、旧状態・旧RPC・再実行を考慮

この実行環境ではGradle 9.5.0本体を取得できなかったため、Android依存関係を含む
`compileDebugKotlin` はローカルのAndroid Studioで最後に実行してください。

## 動作確認

1. Supabaseで新SQLを実行
2. ユーザーAでログイン
3. 通常状態のトイレから「清掃を依頼する」
4. ピンが黄色になり、詳細が「清掃依頼中」になることを確認
5. ユーザーBでログイン
6. 同じトイレで「清掃を引き受ける」
7. ピンが青色になり、「清掃中」になることを確認
8. 下部メニューの「清掃」を開く
9. 担当中のトイレ、依頼日時、引受日時、予定報酬が表示されることを確認
10. 「地図で確認する」で対象トイレへ移動できることを確認
11. 「清掃担当をキャンセル」で黄色の清掃依頼中へ戻ることを確認

同じ清掃依頼をユーザーBとユーザーCが同時に引き受けた場合、最初の1人だけが成功し、後のユーザーには「すでにほかのユーザーが引き受けています」と表示されます。
