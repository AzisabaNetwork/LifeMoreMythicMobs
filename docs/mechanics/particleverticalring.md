# particleVerticalRing
## Description
指定された位置に、垂直な円状にパーティクルを描画する
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| particle | pa | 描画するパーティクルの種類 | REDSTONE |
| color | c | パーティクルの色 | #ffffff |
| radius | r | リングの半径 | 3.0 |
| points | po | リングを構成するパーティクルの数 | 10 |
| amount | a | 各点で描画するパーティクルの数 | 1 |
| size" | si | パーティクルのサイズ。一部パーティクルでは使えません | 1 |
| speed | s | パーティクルの速度。一部パーティクルでは使えません | 0 |
| rotatex | rotx | X軸周りの回転角 | 1 |
| rotatey | roty | Y軸周りの回転角 | 1 |
| rotatez | rotz | Z軸周りの回転角 | 1 |
| startxoffset | sxo | 発動地点からのX軸での開始オフセット | 0.0 |
| startyoffset | syo | 発動地点からのY軸での開始オフセット | 0.0 |
| startzoffset | szo | 発動地点からのZ軸での開始オフセット | 0.0 |
| startsideoffset | sso | リングの開始位置を回転角で変更する | 0 |
| ignoreentityrotation | ier, i | エンティティのyawを無視してワールド座標で描画するか | true |
| uniform | uni, u | パーティクルを均等な間隔で表示するか | true |

## Examples
```yml
 Skills:
 - pvr{c=#000000;a=1;r=3;points=40;rotz=10;rotx=90;roty=0;syo=0.5}
```
> なんかいい感じにパーティクルを描画する

## Aliases
- pvr
- pvring