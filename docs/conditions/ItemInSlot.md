# ItemInSlot
## Description
対象がプレイヤーである場合にのみ動作し、インベントリを走査してアイテムの存在と個数を数える
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| mmid | id | 検出したいアイテムのmmid | null |
| count | c | 条件を満たすのに必要なアイテムの個数 | 1 |
| slots | s | チェックするインベントリのスロット番号。ホットバーの場合`0~8`。インベントリの場合、左上から数えて`9~36`<br>また、`5-8`のように記述する事で、範囲指定可能。この場合、その範囲内のどこかで条件を満たせばtrueを返します | null |
| invert | i, 逆転 | trueとfalseの逆転を行うか | false |

## Examples
```yml
 Conditions:
 - itemInSlot{mmid=cl7_fa5_Luminance;c=1;slots=9}
```
> インベントリの一番左上に`cl7_fa5_Luminance`が1つ以上存在する場合、trueを返す
```yml
 Conditions:
 - itemInSlot{mmid=cl7_fa5_Luminance;c=1;slots=0-8}
```
> ホットバー全体の何処かに`cl7_fa5_Luminance`が1つ以上存在する場合、trueを返す