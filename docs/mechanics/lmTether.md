# lmTether

Aliases: `TetherBeam`

## Description
キャスター（または `@Origin`）とターゲットをリアルタイムに追従して結ぶ 3D ビーム／鎖（`BlockDisplay` およびパーティクル）を生成します。距離による切断・接近判定、および第三者が線分を横切った際の判定に対応します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `block` | `b` | ビームに使うブロック名（`none` で非表示） | `sea_lantern` |
| `width` | `w` | ビームの太さ | `0.16` |
| `duration` | `d` | 持続時間（tick） | `100` |
| `interval` | `i` | 位置・判定の更新間隔（tick） | `2` |
| `yoffset` | `yo` | 始点・終点の高さオフセット | `1.0` |
| `fromorigin` | `fo` | `true` の場合、始点を `@Origin` に固定する | `false` |
| `maxdist` | `maxd` | 最大距離。超えると `onBreak` を発火して終了（`-1` で無効） | `-1` |
| `mindist` | `mind` | 最小距離。下回ると `onNear` を発火（`-1` で無効） | `-1` |
| `breakonnear` | `bon` | `minDist` 到達時にビームを終了するか | `true` |
| `crossradius` | `cr` | 横切り判定の太さ（半径） | `0.8` |
| `crossplayersonly` | `cpo` | 横切り判定をプレイヤーのみに限定するか | `true` |
| `crosscooldown` | `cc` | 同一エンティティの横切り再判定クールダウン（tick） | `10` |
| `glow` | `gl` | 発光色（`#RRGGBB` 形式。空で発光なし） | `""` |
| `particle` | `p` | 線上に並べるパーティクル名（`none` でなし） | `none` |
| `particlestep` | `ps` | パーティクルの配置間隔 | `0.4` |
| `ontick` | `ot` | 更新ごとに実行するスキル | - |
| `onbreak` | `ob` | `maxDist` 超過で切れた時に実行するスキル | - |
| `onnear` | `on` | `minDist` 未満で実行するスキル | - |
| `oncross` | `oc` | 第三者が横切った時に実行するスキル（横切った対象が `trigger`） | - |
| `onend` | `oe` | `duration` 完走時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmTether{b=red_stained_glass;w=0.18;d=120;maxDist=14;onBreak=ChainBreak;onCross=LaserHit;glow=#FF3333} @target
```
