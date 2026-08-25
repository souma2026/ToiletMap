# ToiletMap 隠しゲーム「TOILET DODGE」実装版

## 起動方法

1. アプリを起動して Map 画面を開く
2. 左上の緑色の `WC` ロゴを2秒以内に5回連続タップする
3. `SECRET MODE UNLOCKED` が表示される
4. `TOILET DODGE` タイトル画面で `START` を押す

ボトムナビゲーションにはゲーム項目を追加していません。

## 実装済み

- WCロゴ5回連続タップで起動
- 2秒以上タップ間隔が空いた場合のカウントリセット
- 別画面へ移動した場合のカウントリセット
- SECRET MODE UNLOCKED 演出
- タイトル画面
- 3 / 2 / 1 / START! カウントダウン
- プレイヤー左右ドラッグ操作
- ランダム障害物生成
- 障害物落下
- 当たり判定
- 初期ライフ3
- 接触後約1秒の無敵時間
- 生存時間 x 10 のスコア
- 15秒ごとの LEVEL 1～5
- LEVEL 5以降エンドレス
- レベルアップ演出
- GAME OVER画面
- C / B / A / Sランク
- RETRY
- ToiletMapへ戻る
- 端末内ハイスコア保存

## 難易度

- LEVEL 1: 0～15秒 / 最大2個 / ゆっくり
- LEVEL 2: 15～30秒 / 最大3個 / 約1.2倍
- LEVEL 3: 30～45秒 / 最大4個 / 約1.5倍 / 高速障害物
- LEVEL 4: 45～60秒 / 最大5個 / 約1.8倍 / 大型障害物
- LEVEL 5: 60秒以降 / 最大7個 / 約2.2倍 / 全種類 / エンドレス

## 障害物

- 🧻 トイレットペーパー
- 🪠 ラバーカップ
- 🧹 モップ相当
- 🧴 洗剤
- 🪣 バケツ
- 💩 汚れマーク
- 🚽 トイレ

## ハイスコア保存

仕様書では DataStore / SharedPreferences が候補です。
今回は既存Gradle依存関係を増やさず本体への影響を最小化するため、SharedPreferencesを使用しています。

保存データ:

- bestScore
- bestSurvivalTime
- playCount

ゲームスコアは清掃ポイント・報酬ポイントとは一切連携しません。

## 新規ファイル

```text
app/src/main/java/com/example/toiletmap/
├── data/local/
│   └── GameScoreRepository.kt
├── screen/game/
│   ├── GameModels.kt
│   └── SecretGameScreen.kt
└── viewmodel/
    └── GameViewModel.kt
```

## 変更ファイル

```text
MainActivity.kt
ui/ToiletMapApp.kt
screen/map/MapScreen.kt
```

## ビルド

Windows PowerShell / コマンドプロンプトでプロジェクト直下から:

```bat
gradlew.bat :app:compileDebugKotlin
```

`BUILD SUCCESSFUL` になればOKです。
