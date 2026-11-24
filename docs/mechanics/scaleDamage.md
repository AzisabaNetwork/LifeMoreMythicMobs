# scaleDamage
## Description
`~onAttack` / `~onDamaged` で使う。この一撃のダメージ全体（属性ダメージも含む）を m 倍する。
modifyDamage と違い、攻撃した相手のいないダメージ（落下・炎など）にも効く。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| multiplier | m, amount, a | 倍率（プレースホルダー可） | 1 |

## Examples
```yml
 EquipSlot: OFFHAND
 Skills:
 - scaleDamage{m=0.7} @self ~onDamaged ?wearingSlot{s=OFFHAND;m=FFGGR_Under_Boss_1_LootWeapon4}
```
> オフハンドに持っていると、何から受けるダメージも 30% 減る（オフハンドのアイテムのスキルは `EquipSlot: OFFHAND` が要る）
