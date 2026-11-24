# addItemCount
## Description
slot のアイテムに保存した数（アイテムのデータに文字で保存）に amount を足す。lore を書くと、その数を出す説明文の行を 1 行だけ保つ。
数を使う側は条件 [itemCount](../conditions/itemCount.md)。文字の中の `$added`（足した数）と `$count`（合計）は数に置き換わる。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| slot | s | HAND / OFFHAND / HEAD / CHEST / LEGS / FEET | HAND |
| key | k | 保存のキー（`名前空間:名前`） | lifemoremythicmobs:count |
| amount | a | 足す数 | 1 |
| perStack | ps | このスロットのスタック数を amount に掛ける | |
| lore | l | 数を出す説明文の行 | |
| message | m | 足す前にプレイヤーへ送る文 | |
| totalMessage | tm | 足した後にプレイヤーへ送る文 | |

## Examples
```yml
 Skills:
 - 'addItemCount{slot=OFFHAND;key=ffcore:crack_soul;lore="&f攻撃可能回数: &b&l$count";amount=20;perStack=HAND;message="+$added回の攻撃をスタックしました。";totalMessage="合計 $count回分"} @self'
```
> 手に持ったクラック 1 個につき、オフハンドの霊魂魂に 20 回分を貯める（`: ` を含む行は YAML のため引用符で囲む）
