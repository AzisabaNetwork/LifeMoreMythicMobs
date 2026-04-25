# setFirstPersonView
## Description
プレイヤーの視点を指定した座標へ飛ばす
## Attributes
| Attribute         | Aliases | Description                   | Default |
|:------------------|:--------|:------------------------------|:--------|
| duration          | d       | 持続時間                          | 100     |
| yaw               | y       | カメラの水平角度                      | 0       |
| pitch             | p       | カメラの垂直角度                      | 0       |
| useTargetRotation | utr     | trueの場合、対象エンティティの向いている方向を維持する | false   |
| yOffset           | yo      | 座標に加える高さ                      | 1.6     |

## Examples
> 座標の位置に視点を飛ばす
```yml
BossView:
  Skills:
    - sfpv{d=100;utr=false;yo=0;y=-90;p=28} @location{c=100.5,48.5,852.5}
```

## Aliases
- sfpv