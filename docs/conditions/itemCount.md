# itemCount
## Description
slot のアイテムに保存した数（[addItemCount](../mechanics/addItemCount.md)）が min 以上なら true。consume を書くとその分を減らす。減らしてから window tick の間は、減らさずに true を返す（同じ発動で 2 回減らさない）。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| slot | s | スロット | HAND |
| key | k | 保存のキー | lifemoremythicmobs:count |
| min | amount, a | 必要な数 | 1 |
| consume | c | 減らす数 | 0 |
| window | w | 減らさずに true にする時間（tick） | 2 |
| lore | l | 減らしたときに書き直す説明文の行（addItemCount と同じもの） | |

## Examples
```yml
 Conditions:
 - 'itemCount{slot=HAND;key=ffcore:crack_soul;lore="&f攻撃可能回数: &b&l$count";min=1;consume=1;window=2} true'
```
