# CompareValues
## Description
指定された演算に基づいて、二つの値を比較します。  
比較はvalue1とvalue2の間で行われます。基本的にwikiと変わらないはずなので[詳細はwikiからどうぞ](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/skills/conditions/CompareValues)
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| value1 | val1, v1, 数値1 | 比較の第一の値 | 0.0 |
| value2 | val2, v2, 数値2 | 比較の第二の値 | 0.0 |
| operator | op, 比較 | 比較に使う符号の種類 | == |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |
## Operator Attributes
| Operator |
| :-- | 
| `==` |
| `!=` |
| `>` |
| `<` |
| `>=` |
| `<=` |

## Examples
```yml
 Conditions:
 - valCompare{val1=<skill.var.damage-type>;val2=FIRE;op===}
```
> <skill.var.damage-type>がFIREだった場合、trueを返す
```yml
 Conditions:
 - valCompare{val1=<caster.mhp>;val2=40;op=<=}
```
> casterのHP最大値が40以下だった場合、trueを返す

## Aliases
- valCompare
- valCompares
- compareValue