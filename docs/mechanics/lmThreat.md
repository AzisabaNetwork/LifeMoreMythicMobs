# lmThreat

Aliases: `ThreatControl`

## Description
キャスターである MythicMob のヘイトテーブル（ThreatTable）を直接操作します。タンクの挑発やヘイト半減スキルに利用できます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `mode` | `m` | 操作種別（`TAUNT`, `ADD`, `SET`, `MULTIPLY`, `DROP`, `CLEAR`） | `ADD` |
| `amount` | `a` | 加算・設定・乗算する値（`TAUNT` 時はトップヘイトへの上乗せ量） | `100.0` |

## Modes
- `TAUNT`: 現在のトップヘイト + `amount` に設定し、ターゲットを即座に強制変更します。
- `ADD`: ヘイト値を `amount` だけ増減します。
- `SET`: ヘイト値を `amount` に設定します。
- `MULTIPLY`: 現在のヘイト値に `amount` を乗算します（例: `0.5` で半減）。
- `DROP`: ターゲットのヘイトをテーブルから削除します。
- `CLEAR`: キャスターのターゲットを解除します。

## Examples
```yml
Skills:
  - lmThreat{mode=TAUNT;a=500} @trigger
  - lmThreat{mode=MULTIPLY;a=0.5} @target
```
