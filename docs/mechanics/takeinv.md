# takeinv
## Description
プレイヤーのインベントリから、指定されたmmidのアイテムを特定の個数削除します
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| item | i, material, m | 削除したいアイテムのmmid | "null" |
| amount | a | 削除したいアイテムの個数 | 1 |

## Examples
```yml
 Skills:
 - takeinv{m=vote_ticket;a=10}
```
> `vote_ticket`を10個インベントリから削除します