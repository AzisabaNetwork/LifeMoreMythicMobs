# VarExtractNumber
## Description
指定した元の変数から範囲指定で数値のみを抽出し、新しい変数へ格納する
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| from |  | 抽出したい数値が含まれる、元の変数 | null |
| to |  | 抽出した数値を格納する、新しい変数 | null |
| start |  | 抽出を開始する位置(インデックス)。0から始まる | 0 |
| end |  | 抽出を終了する位置(インデックス) | MAX\_VALUE |
| lazy | l, 怠惰 | trueの場合、最初に見つかった数値の塊のみを抽出します。<br>falseの場合、全体の数値を抽出します。 | false |

## Examples
```yml
 Skills:
 - SetVarLoreLine{var=caster.cl7_ohItemLore;sc=true;line=1;slot=MAINHAND}
 - VarSubstring{from=caster.testVar;to=caster.extractedVar;l=true}
```
> `<caster.var.cl7_ohItemLore>`にMAINHANDアイテムのLore2行目を格納し、その変数の最初に見つかった数値を`<caster.var.testVar>`へ格納する