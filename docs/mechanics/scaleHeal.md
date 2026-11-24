# scaleHeal
## Description
`~onHealed` で使う。この回復量を m 倍する。reasons で回復の理由（Bukkit の RegainReason）を絞れる。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| multiplier | m, amount, a | 倍率 | 1 |
| reasons | reason, r | 対象の理由（カンマ区切り。空なら全部） | |

## Examples
```yml
 Skills:
 - scaleHeal{m=1.5;reasons=REGEN,MAGIC_REGEN} @self ~onHealed ?wearingSlot{s=LEGS;m=FFGGR_Under_Boss_1_LootArmor1}
 - scaleHeal{m=2;reasons=EATING,SATIATED} @self ~onHealed ?wearingSlot{s=LEGS;m=FFGGR_Under_Boss_1_LootArmor1}
```
> 再生は 1.5 倍、満腹度による回復は 2 倍
