# DayOfWeek
## Description
スキルが実行された時、現実世界での曜日が指定された曜日を一致するかをチェックします
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| day | d, 曜日 | 曜日名を指定 (英語・カンマ区切りで複数指定可・OR判定) | monday |

## Examples
```yml
 Conditions:
 - DayOfWeek(d=saturday)
```
> 今日が土曜日だったらtrueを返す