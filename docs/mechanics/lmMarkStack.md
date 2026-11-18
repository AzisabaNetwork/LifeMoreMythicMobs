# lmMarkStack

Aliases: `MarkStack`

## Description
対象エンティティに期限付きの名前付きスタック型マーク（印・共鳴・蓄積ゲージ）を付与・操作・起爆します。現在値はプレースホルダー `<caster.lmmm.mark.ID>` / `<target.lmmm.mark.ID>` や条件 `?lmMarkStacks` でも参照できます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `id` | `mark`, `m` | マークの識別名 | `mark` |
| `mode` | `op` | 操作種別（`ADD`, `SET`, `TAKE`, `DETONATE`, `CLEAR`） | `ADD` |
| `amount` | `a` | 加減算・設定するスタック数 | `1` |
| `max` | `mx` | 最大スタック数（`0` で無制限） | `0` |
| `duration` | `d` | 有効期限（tick、`0` 以下で無期限） | `200` |
| `resetonmax` | `rom` | `max` 到達時にスタックを `0` にリセットするか | `true` |
| `requirenonzero` | `rnz` | `DETONATE` 時にスタックが 1 以上の時だけ `onDetonate` を実行するか | `true` |
| `var` | `v` | 結果のスタック数を格納するスキル変数名 | `lmmm_mark_stacks` |
| `onmax` | `om` | `max` 到達時に実行するスキル | - |
| `ondetonate` | `od` | `DETONATE` 時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmMarkStack{id=burn;mode=ADD;amount=1;max=5;d=120;resetOnMax=true;onMax=BurnBurst} @target
  - lmMarkStack{id=burn;mode=DETONATE;onDetonate=BurnDetonate;var=burn_count} @target
```
