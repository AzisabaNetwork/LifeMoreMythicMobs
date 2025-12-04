# PlayersInRadius
## Description
周囲にプレイヤーが何人いるかをチェックします。casterがplayerだった場合、自分も対象になります。
## Attributes
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| radius | r, distance, d | プレイヤーをチェックする範囲。[符号が使えます](#operator) | 32 |
| ignoreSpectator | is | スペクテイターのプレイヤーをカウントするか | true |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

### Operator
| [Operator](#operator) |
| :-- | 
| `==` |
| `!=` |
| `>` |
| `<` |
| `>=` |
| `<=` |
## Examples
```yml
 Conditions:
 - PlayersInRadius{r=12;a=>=10}
```
> 半径12ブロック以内に、プレイヤーが10人以上存在する場合trueを返す
```yml
 Conditions:
 - PlayersInRadius{r=8;a=<5}
```
> 半径8ブロック以内に、プレイヤーが5人未満しかいなかった場合にtrueを返す