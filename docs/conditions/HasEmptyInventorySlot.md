# HasEmptyInventorySlot
## Description
対象のプレイヤーのインベントリに空きスロットがあるかどうかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

## Examples
```yml
 Conditions:
 - HasEmptyInventorySlot{}
```
> インベントリに空きがあった場合、trueを返す