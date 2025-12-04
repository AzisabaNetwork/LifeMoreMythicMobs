# VarReplaceRegex
## Description
指定した元の変数に正規表現に基づく置換を行い、新しい変数へ格納する
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| regex | r, 正規表現 | マッチさせたい正規表現のパターン | null |
| replacement | rep, 置換 | マッチした部分を置き換える文字列 | null |
| from |  | 置換したい元の変数名 | null |
| to |  | 置換後の文字列を格納する新しい変数名 | null |

## Examples
```yml
 Skills:
 - setVar{var=caster.fromVar;val="Lv.100 Super Sword";type=STRING}
 - VarReplaceRegex{from=caster.fromVar;to=caster.toVar;r="Lv.";rep="Tier "}
```
> `<caster.var.fromVar>`の`"Lv.100"`を`"Tier 100"`に置換し、最終的な値を`"Tier 100 Super Sword"`にして`<caster.var.toVar>`へ格納する