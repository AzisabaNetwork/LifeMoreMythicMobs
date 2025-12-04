# VarSubstring
## Description
指定した元の変数から範囲指定で文字列を抽出し、新しい変数へ格納する
## Attributes
| Attribute | Description | Default |
| :-- | :-- | :-- |
| from | 抽出したい文字列が含まれる、元の変数 | null |
| to | 抽出した文字列を格納する、新しい変数 | null |
| start | 抽出を開始する位置(インデックス)。0から始まる | 0 |
| end | 抽出を終了する位置(インデックス) | MAX\_VALUE |

## Examples
```yml
testSkill:
 Skills:
 - setVar{var=caster.testVar;val=こううんち;type=STRING}
 - VarSubstring{from=caster.testVar;to=caster.extractedVar;start=2}
```
> `<caster.var.testVar>`の`こううんち`から`うんち`を抽出し、`<caster.var.extractedVar>`へ格納する