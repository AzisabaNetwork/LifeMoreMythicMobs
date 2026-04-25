# skyRotation
## Description
指定したプレイヤーだけ、空を爆速で回転させる
## Attributes
| Attribute | Aliases | Description  | Default |
|:----------|:--------|:-------------|:--------|
| duration  | d       | 効果の持続時間      | 100     |
| speed     | s       | 1tick辺りに進む時間 | 500     |

## Examples
> 200tick、爆速で空をぶん回す
```ymlTimeWarp:
  Skills:
  - skyRotation{d=200;s=800} @target
```