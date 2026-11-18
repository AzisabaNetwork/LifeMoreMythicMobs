# lmBlockCount

## Description
エンティティまたはターゲット座標の周囲立方範囲（半径 `r`）内に、指定ブロックが条件数だけ存在するかチェックします。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `materials` | `m`, `block`, `b` | カンマ区切りのブロック名 | `""` |
| `radius` | `r` | 検索半径（`0〜10`） | `3` |
| `count` | `c`, `value`, `v` | ブロック数の比較式（例: `>=4`, `1to5`） | `>=1` |

## Examples
```yml
Conditions:
  - lmBlockCount{m=WATER,BUBBLE_COLUMN;r=3;c=>=4} true
```
