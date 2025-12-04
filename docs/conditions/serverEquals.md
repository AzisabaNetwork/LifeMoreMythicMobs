# serverEquals
## Description
現在いるサーバーの名前が、指定されたサーバーの名前と一致するかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| server | s, サーバー | サーバーの名前を指定 | null |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

## Examples
```yml
 Conditions:
 - serverEquals{s=lifeevent}
```
> lifeeventサーバーにいた場合、trueを返す
```yml
 Conditions:
 - serverEquals{サーバー=LIFE;逆転=true}
```
> LIFEサーバーにいた場合、falseを返す