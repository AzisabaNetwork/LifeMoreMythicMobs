# lmDash

Aliases: `MotionDash`

## Description
ターゲットエンティティを指定方向へダッシュ・引き寄せ・吹き飛ばし・打ち上げ移動させます。落下ダメージ無効期間（`noFall`）も付与できます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `mode` | `m` | 移動方向（`FORWARD`, `TOWARD`, `AWAY`, `UP`） | `FORWARD` |
| `power` | `p` | 移動速度の大きさ | `1.2` |
| `yboost` | `y` | Y軸（上方向）への加算速度 | `0.25` |
| `ignorey` | `iy` | 視線・方向ベクトルのY成分を無視して水平移動にするか | `true` |
| `add` | `a` | 現在の速度を上書きせず加算するか | `false` |
| `nofall` | `nf` | 落下ダメージ判定をリセットし続ける期間（tick） | `0` |

## Examples
```yml
Skills:
  - lmDash{mode=FORWARD;power=1.4;y=0.25;ignoreY=true;noFall=40} @self
  - lmDash{mode=TOWARD;power=1.1;y=0.2} @PIR{r=12}
```
