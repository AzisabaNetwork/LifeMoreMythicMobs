# mmidContains
## Description
対象のプレイヤーが特定のスロットに所持しているアイテムのmmidに、指定された文字列が含まれているかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| mmid | id | アイテムのMMIDに含まれているべき文字列 | "" |
| ignorecase | ic | 大文字・小文字を無視するか | false |
| offhand | oh | チェック対象をオフハンドにするか | false |
| hotbar | hb | チェック対象をホットバーの特定スロットにするか | false |
| hotbarnum | hbn | hotBarがtrueの場合にチェックする、ホットバーのスロット番号(`1~9`で指定) | 1 |
| invert | i, 逆転 | trueとfalseの逆転を行うか | false |

## Examples
```yml
 Conditions:
 - mmidContains{mmid="7_fa";oh=true}
```
> オフハンドアイテムのmmidに`7_fa`が含まれていたらtrueを返す