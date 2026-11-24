# critChance
## Description
`~onAttack`（近接攻撃）で使う。その一撃にクリティカル率を足す。
同じ一撃で動いた critChance と、攻撃した人の [critChanceBuff](critChanceBuff.md) を全部足してから **1 回だけ** 抽選する（5% の装備を 4 つ着ると 20% を 1 回）。当たるとダメージ × multiplier（いちばん大きい値）。
プレイヤーへの攻撃では動かない（`pvp=true` で動く）。スキルのダメージ（damage など）では動かない（MythicMobs が `~onAttack` を動かさないため）。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| chance | c | 足すクリティカル率（0.05 = 5%） | 0 |
| multiplier | m | クリティカルのときの倍率 | 1.5 |
| pvp | | プレイヤーへの攻撃でも動かす | false |

## Examples
```yml
 Skills:
 - critChance{c=0.05} @self ~onAttack ?wearingSlot{m=FFGGR_Under_ラッキーH;s=HEAD}
```
> 頭に着ているとクリティカル率 +5%
