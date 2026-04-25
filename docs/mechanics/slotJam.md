# slotJam
## Description
指定したプレイヤーのメインハンドスロットやホットバースロットを固定する
## Attributes
| Attribute   | Aliases | Description                   | Default |
|:------------|:--------|:------------------------------|:--------|
| duration    | d       | 固定する時間(tick)                  | 100     |
| lockCurrent | lc      | trueの場合、プレイヤーが持っているスロットに固定される | true    |
| slot        | s       | 0~8                           | 0       |

## Examples
> @targetのホットバーを、対象が現在持っているスロットで固定する
```yml
WeaponLock:
 Skills:
 - slotJam{d=100;lc=true} @target
```