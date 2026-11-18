# lmLootRoll

Aliases: `LootRoll`

## Description
重み付きランダム抽選を行い、プレイヤーに MythicItem の付与（インベントリ満杯時は足元にドロップ）またはスキルを実行します。プレイヤーの `GENERIC_LUCK` 属性値に応じてレア枠（平均より重みが小さい枠）の確率をブーストできます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `entries` | `e`, `table` | カンマ区切りの抽選テーブル（書式は下記参照） | `""` |
| `rolls` | `r` | 抽選回数（`1〜64`） | `1` |
| `luckfactor` | `lf` | 平均より重みが小さい枠に掛かる `1 + Luck * luckFactor` 倍率 | `0.0` |

## Entry Format
- `MythicItemID:weight` または `MythicItemID:weight:amount`
- `item:MythicItemID:weight` または `item:MythicItemID:weight:amount`
- `skill:SkillName:weight`
- `nothing:weight`

## Examples
```yml
Skills:
  - lmLootRoll{entries="StoneShard:60:2,GoldCrystal:30,skill:JackpotSkill:5,nothing:5";rolls=2;luckFactor=0.05} @trigger
```
