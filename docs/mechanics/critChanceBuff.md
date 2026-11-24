# critChanceBuff
## Description
対象に、決まった時間だけクリティカル率を足す。対象が近接攻撃でプレイヤー以外を殴ると、[critChance](critChance.md) と合わせて 1 回抽選する。同じ id でかけ直すと上書き。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| chance | c | 足すクリティカル率 | 0 |
| duration | d | 時間（tick） | 200 |
| multiplier | m | クリティカルのときの倍率 | 1.5 |
| id | name | 重ねがけの区別 | default |

## Examples
```yml
 Skills:
 - critChanceBuff{chance=0.5;d=1200;id=FFGGR_Under_虹の聖水} @self
```
> 1 分間クリティカル率 +50%
