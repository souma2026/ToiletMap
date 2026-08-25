# ToiletMap 清掃機能 第4段階（清掃完了）

## 今回追加した機能

- 清掃担当者本人だけが「清掃完了」を実行できます。
- 対象依頼が `IN_PROGRESS` かをSupabase RPC側で確認します。
- 完了すると `cleaning_requests.status = COMPLETED`、`completed_at = now()` を保存します。
- `toilets.cleaning_status` を `NORMAL` に戻します。
- `toilets.last_cleaned_at_millis` を現在時刻へ更新します。
- 清掃画面の担当一覧から完了した依頼が消えます。
- 第4段階では報酬ポイントの実付与はまだ行いません。

## Supabaseで実行するSQL

すでに第1〜3段階のSQLを実行済みなら、次のファイルだけをSQL Editorで1回実行してください。

`supabase/20260825_stage4_complete_cleaning.sql`

新規環境へ最初から構築する場合は、次を使用できます。

`supabase/cleaning_requests_stage1_4.sql`

## 動作確認

1. ユーザーAが清掃依頼を出す。
2. ユーザーBがその依頼を引き受ける。
3. ユーザーBで下部メニューの「清掃」を開く。
4. 「清掃完了」を押す。
5. 「清掃完了を記録しました」と表示される。
6. 担当中一覧から対象が消える。
7. 地図へ戻ると対象ピンが通常状態（赤）へ戻る。
8. 詳細の「前回の清掃完了」が更新される。

## 注意

予定報酬の5ptは表示されますが、第5段階までは実際のポイント残高には加算されません。
