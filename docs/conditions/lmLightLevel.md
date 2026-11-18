# lmLightLevel

## Description
エンティティまたはターゲット座標の光レベル（`0〜15`）が条件式を満たしているかチェックします。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `level` | `l`, `value`, `v` | 比較式（例: `<=7`, `>=10`, `4to8`） | `<=7` |
| `type` | `t` | 光の種類（`TOTAL`, `BLOCK`, `SKY`） | `TOTAL` |

## Examples
```yml
Conditions:
  - lmLightLevel{l=<=7;type=TOTAL} true
```
