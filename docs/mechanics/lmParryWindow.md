# lmParryWindow

Aliases: `ParryWindow`

## Description
対象エンティティに短時間のパリィ／受け流し受付状態を付与します。受付中に攻撃を受けると指定割合のダメージを軽減（既定 100%＝完全無効化）し、攻撃者を `trigger` として `onParry` スキルを発火します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `duration` | `d` | パリィ受付時間（tick） | `10` |
| `reduce` | `r` | ダメージ軽減率（`0.0〜1.0`。`1.0` で完全無効化） | `1.0` |
| `consume` | `c` | 1回のパリィ成功で受付状態を終了するか | `true` |
| `onparry` | `op` | パリィ成功時に実行するスキル（攻撃者が `trigger`） | - |
| `onexpire` | `oe` | 攻撃を受けずに時間切れになった時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmParryWindow{d=12;reduce=1.0;consume=true;onParry=CounterSlash;onExpire=ParryMiss} @self
```
