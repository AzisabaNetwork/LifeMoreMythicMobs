# lmSkillCheck

Aliases: `SkillCheck`

## Description
対象プレイヤーの ActionBar に往復するタイミングバー（QTE）を表示し、入力タイミングに応じて `onGreat` / `onGood` / `onFail` スキルを実行します。資源採掘のクリティカル判定や釣り・鍛冶・パリィ演出に利用できます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `label` | `l` | バーの左側に表示するラベル | `&eタイミング` |
| `speed` | `sp` | 1 tick あたりのカーソル移動量（`0.01〜0.25`） | `0.045` |
| `duration` | `d` | 制限時間（tick） | `80` |
| `center` | `c` | 成功ゾーンの中心位置（`0.1〜0.9`、負値で毎回ランダム） | `-1.0` |
| `great` | `gr` | GREAT 判定の半径 | `0.08` |
| `good` | `go` | GOOD 判定の半径 | `0.20` |
| `input` | `in` | 入力方法（`SNEAK` = Shift, `CLICK` = 左クリック） | `SNEAK` |
| `ongreat` | `ogr` | GREAT 判定時に実行するスキル | - |
| `ongood` | `ogo` | GOOD 判定時に実行するスキル | - |
| `onfail` | `of` | FAIL または時間切れ時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmSkillCheck{label="&e採掘";speed=0.045;d=80;input=SNEAK;onGreat=MineCrit;onGood=MineNormal;onFail=MineMiss} @trigger
```
