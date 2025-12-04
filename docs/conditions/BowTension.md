# BowTension
## Description
プレイヤーの弓の引き絞り具合をチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| force | f, value, v, val | チェックする弓のテンションの値または範囲(例: `1.0`で完全に引いたとき、`>0.5`で半分以上引いたとき) | >0 |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

## Examples
```yml
 Conditions:
 - BowTension{val=>0.8}
```
> 弓の引き絞り具合が8割を超えたらtrueを返す
```yml
 Conditions:
 - BowTension{val=<0.2}
```
> 引き絞ってからすぐの場合trueを返す