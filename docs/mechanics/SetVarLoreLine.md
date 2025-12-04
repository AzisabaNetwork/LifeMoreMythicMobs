# SetVarLoreLine
## Description
プレイヤーの装備スロット，またはメインハンド・オフハンドに持っているアイテムのLoreを指定した変数に格納する．
## Attributes
| Attribute  | Aliases          | Description                                                         | Default  |
|:-----------|:-----------------|:--------------------------------------------------------------------|:---------|
| variable   | `var`, `v`, `変数` | 取得したLoreを格納する変数の名前                                                  | null     |
| line       | `l`, `行`         | 取得したいLoreの行番号<br>0を指定した場合，1行目が返されます)                                | 0        |
| stripcolor | `sc`, `色削除`      | Loreに含まれるカラーコードを除去するかどうか                                            | false    |
| slot       | `s`, `スロット`      | Loreを取得したい装備部位またはスロットを指定する                                          | MAINHAND |
| offhand    | `o`, `オフハンド`     | `slot`が指定されていないときに，オフハンドのアイテムを対象にするかどうか<br/>※`slot="OFFHAND"`の使用を推奨 | false    |
## Examples
> オフハンドに所持しているアイテムのLoreの上から2番目(インデックス`1`)を取得し，カラーコードを削除して`<caster.var.cl7_ohItemLore>`に格納する．
```yml
 Skills:
   - SetVarLoreLine{var=caster.c17_ohItemLore;sc=true;line=1;slot=OFFHAND}
```
<br/>

> 胴体に所持しているアイテムのLoreの上から2番目を(インデックス`1`)を取得し，`<caster.var.cl7_ohItemLore>`に格納する．
```yml
 Skills:
   - SetVarLoreLine{var=caster.cl7_ohItemLore;sc=true;line=1;slot=CHEST}
```

## Aliases
- SetLoreLineVar