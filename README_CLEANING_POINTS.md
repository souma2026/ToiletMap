# 清掃依頼ポイント機能

## 追加された動作

1. 通常状態のトイレで「清掃を依頼する」を押します。
2. 支払う報酬ポイントを 1～10000 pt の範囲で入力します。
3. 依頼が成功すると、依頼者の `profiles.points` からそのポイントが差し引かれます。
4. トイレは `REQUESTED` になり、地図詳細・未清掃一覧に報酬ポイントが表示されます。
5. 清掃したユーザーが「清掃しました」を押すと、そのユーザーの `profiles.points` に報酬が加算されます。
6. トイレは `NORMAL` に戻り、報酬ポイントは 0 に戻ります。

## 最初に1回だけ必要なSupabase設定

Android Studioで実行する前に、Supabase Dashboard の **SQL Editor** を開いて、

`supabase/cleaning_points_setup.sql`

の内容をすべて貼り付けて **Run** してください。

このSQLを実行しないと、アプリから呼び出す `request_cleaning_with_points` / `mark_toilet_cleaned_with_points` が存在しないため清掃ポイント機能は動きません。

## ポイント処理の安全性

ポイントの減算・加算はAndroid端末側で直接2回に分けて更新せず、SupabaseのRPC内でトランザクションとして処理します。
そのため、通信途中で「ポイントだけ減った」「報酬だけ二重にもらえた」といった状態になりにくい実装です。

## 主な変更ファイル

- `app/src/main/java/com/example/toiletmap/model/Toilet.kt`
- `app/src/main/java/com/example/toiletmap/data/repository/ToiletRepository.kt`
- `app/src/main/java/com/example/toiletmap/viewmodel/ToiletViewModel.kt`
- `app/src/main/java/com/example/toiletmap/MainActivity.kt`
- `app/src/main/java/com/example/toiletmap/ui/ToiletMapApp.kt`
- `app/src/main/java/com/example/toiletmap/screen/map/MapScreen.kt`
- `app/src/main/java/com/example/toiletmap/screen/listofuncleaned/UncleanedToilet.kt`
- `app/src/main/java/com/example/toiletmap/screen/listofuncleaned/UncleanedToiletCard.kt`
- `supabase/cleaning_points_setup.sql`
