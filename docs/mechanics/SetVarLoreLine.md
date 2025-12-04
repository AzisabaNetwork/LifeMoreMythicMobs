# SetVarLoreLine
## Description
プレイヤーのホットバーのスロット/オフハンドに持っているアイテムのLoreを変数に格納する
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| variable | var, v, 変数 | 取得したLoreを格納する変数の名前 | null |
| line | l, 行 | 取得したいLoreの行番号(0が一番上) | 0 |
| stripcolor | sc, 色削除 | Loreに含まれるカラーコードを除去するか | false |
| slot | s, スロット | ホットバーのスロットか、部位を指定する。 | "DEFAULT" |
| offhand | o, オフハンド | slotが指定されていない場合に、オフハンドに所持しているアイテムを対象にするか<br>※ 恐らく`slot=OFFHAND`の方が良いので、非推奨 | false |

## Examples
```yml
 Skills:
 - SetVarLoreLine{var=caster.cl7_ohItemLore;sc=true;line=1;slot=OFFHAND}
```
> オフハンドに所持しているアイテムのLoreの、上から2番目を`<caster.var.cl7_ohItemLore>`に格納する
```yml
 Skills:
 - SetVarLoreLine{var=caster.cl7_ohItemLore;sc=true;line=1;slot=CHEST}
```
> 胴体に所持しているアイテムのLoreの、上から2番目を`<caster.var.cl7_ohItemLore>`に格納する

## Aliases
- SetLoreLineVar