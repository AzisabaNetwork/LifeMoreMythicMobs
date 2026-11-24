# momentumDash
## Description
対象を、いま動いている方向へ押し出す。ticks の間に動いた距離 × multiplier の速さにし、横方向はどちらの軸も max を超えないよう 0.95 ずつ縮める。縦の速さは最初のまま。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| ticks | t | 動いた距離を測る時間 | 2 |
| multiplier | m | 倍率 | 3 |
| max | | 横方向の上限 | 2 |

## Examples
```yml
 Skills:
 - momentumDash{ticks=2;multiplier=3;max=2} @self
```
