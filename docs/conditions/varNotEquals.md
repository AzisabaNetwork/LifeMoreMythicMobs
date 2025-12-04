# varNotEquals
## Description
variableEqualsの逆を行います。ちゃんと説明すると、値が一致しなかった場合にtrueを返します。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| variable | var, v, 変数 | チェックしたい変数の名前 | null |
| value | val, 値 | 変数の値と比較したい値 | null |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

## Examples
```yml
 Conditions:
 - varNotEquals{var=<caster.cl7_test>;val=1}
```
> <caster.var.cl7_test>が`1`で無い場合、trueを返す