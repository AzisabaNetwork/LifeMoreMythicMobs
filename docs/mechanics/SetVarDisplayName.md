# SetVarDisplayName
## Description
プレイヤーがメインハンド/オフハンドに持っているアイテムのDisplayを変数に格納する
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| variable | var, v, 変数 | 取得したDisplayを格納する変数の名前 | null |
| stripcolor | sc, 色削除 | Displayに含まれるカラーコードを除去するか | false |
| offhand | o, オフハンド | チェック対象をオフハンドにするか。falseの場合メインハンドのアイテムが対象となる | false |

## Examples
```yml
 Skills:
 - SetVarDisplayName{var=caster.cl7_mhItemDisplay;sc=true}
```
> メインハンドに持っているアイテムのDisplayを、カラーコードを消して`<caster.var.cl7_mhItemDisplay>`へ格納する