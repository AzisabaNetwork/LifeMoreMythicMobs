# fakeWorldBorder
## Description
プレイヤーの視界にのみ偽のワールドボーダーを出現させる
## Attributes
| Attribute | Aliases | Description  | Default |
|:----------|:--------|:-------------|:--------|
| size      | s       | ボーダーの直径      | 20.0    |
| duration  | d       | 表示持続時間       | 100     |
| warning   | w       | 警告が発生するまでの時間 | 5       |
| x         |         | 中心のX座標       |         |
| y         |         | 中心のY座標       |         |
| z         |         | 中心のZ座標       |         |

## Examples
> 直径30のワールドボーダーを、ボスMOBの足元を中心に展開する
```yml
BossArena:
  Skills:
    - fakeWorldBorder{s=30;d=200;w=2} @self
```

## Aliases
- fakeborder