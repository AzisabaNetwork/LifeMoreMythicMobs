# mmidStartsWith
## Description
対象のプレイヤーが特定のスロットに所持しているアイテムのmmidが、指定された文字列で始まっているかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| mmid | id | アイテムのMMIDの接頭辞 | "" |
| ignorecase | ic | 大文字・小文字を無視するか | false |
| offhand | oh | チェック対象をオフハンドにするか | false |
| hotbar | hb | チェック対象をホットバーの特定スロットにするか | false |
| hotbarnum | hbn | hotBarがtrueの場合にチェックする、ホットバーのスロット番号(`1~9`で指定) | 1 |
| invert | i, 逆転 | trueとfalseの逆転を行うか | false |

## Examples
```yml
 Conditions:
 - mmidStartsWith{mmid="cl6_";oh=true}
```
> オフハンドアイテムのmmidが'cl6_'で始まっていたらtrueを返す