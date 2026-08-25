# コンフリクト解消版について

このプロジェクトは `main` と `tohoda` の機能を手動統合した版です。

## 統合した内容

- `main` の清掃フロー
  - 清掃依頼
  - 別ユーザーが清掃を引き受ける
  - 清掃中
  - 担当者が清掃完了
- `tohoda` の可変報酬ポイント
  - 1～10000pt を依頼者が設定
  - 依頼時に依頼者から差し引き
  - 完了時に担当者へ付与
- Google Maps 風の3段階トイレ詳細パネル
  - PEEK / HALF / EXPANDED
  - 上部ハンドルをドラッグまたはタップして切替
  - 閉じるボタンは右上固定
- 現在地表示（方向付きの青い現在地表示）
- 口コミ投稿
- ログイン中ユーザーによるトイレ削除
- 削除時、未完了の清掃依頼の報酬ポイントを依頼者へ返金

## Supabaseで最初に行うこと

`supabase/cleaning_system_final.sql` を Supabase Dashboard の SQL Editor で **ファイル全体を1回だけ** 実行してください。

このSQLは、旧清掃RPCと新しい `cleaning_requests` を整理して、次のRPCを作成します。

- `request_cleaning(text, integer)`
- `accept_cleaning(uuid)`
- `complete_cleaning(uuid)`
- `cancel_cleaning(uuid)`
- `delete_own_toilet(text)`

また、以前 `uuid = text` エラーの原因になっていた不要な
`delete_reviews_after_toilet_delete` トリガーも削除します。

## Gitでコンフリクト中のプロジェクトへ反映する場合

このZIPをそのまま別フォルダで動作確認するのが最も安全です。
既存のコンフリクト中リポジトリへ置き換える場合は、作業内容を退避してから反映してください。
