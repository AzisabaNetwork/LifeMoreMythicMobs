# lmTowerZone

Aliases: `TowerZone`

## Description
PvE レイド向けの塔踏み・頭割り・防衛サークルギミックを生成します。地面に `BlockDisplay` のリング、頭上に `TextDisplay` の人数カウンター（例: `1 / 3`）を自動描画し、達成状況に応じてリングの色を切り替え、終了時（または達成即座）に `onSuccess` / `onFail` を発火します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `radius` | `r` | サークル半径 | `3.0` |
| `required` | `req` | 成功に必要な最低プレイヤー数 | `1` |
| `maxplayers` | `max` | 最大許容人数（`-1` で上限なし） | `-1` |
| `duration` | `d` | 判定までの時間（tick） | `100` |
| `interval` | `i` | 判定・表示更新間隔（tick） | `2` |
| `instant` | `inst` | 人数条件を満たした瞬間に即成功終了するか | `false` |
| `ground` | `g` | 地面へスナップするか | `true` |
| `label` | `l` | 頭上テキストの先頭ラベル | `""` |
| `pendingblock` | `pb` | 未達成時のリングブロック | `red_concrete` |
| `readyblock` | `rb` | 達成時のリングブロック | `lime_concrete` |
| `onsuccess` | `os` | 成功時に実行するスキル（サークル内プレイヤーごと、不在時はキャスター） | - |
| `onfail` | `of` | 失敗時に実行するスキル | - |
| `onenter` | `oen` | プレイヤーが入場した瞬間のスキル | - |
| `onleave` | `ol` | プレイヤーが退場した瞬間のスキル | - |
| `ontick` | `ot` | 更新ごとに実行するスキル | - |

## Examples
```yml
Skills:
  - lmTowerZone{r=3.5;req=2;d=100;label="&e塔";onSuccess=TowerClear;onFail=TowerExplode} @selflocation
```
