# isPet
## Description
指定されたEntityがmyPetのモブかどうかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| invert | i, 逆転 | trueとfalseの逆転を行うか | false |

## Examples
```yml
 TargetConditions:
 - isPet{}
```
> ターゲットのMobがmyPetのモブだったらtrueを返す
```yml
 TargetConditions:
 - isPet{i=true}
```
> ターゲットのMobがmyPetのモブじゃなかったらtrueを返す