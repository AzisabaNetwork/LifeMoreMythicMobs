# lmUsingSpear

Aliases: `isUsingSpear`, `usingSpear`, `spearReady`

## Description
対象エンティティが現在「槍（Spear）」を構えている（チャージ／使用中）かどうかをチェックします。Minecraft 1.21.11 の全槍マテリアル（`WOODEN_SPEAR` 〜 `NETHERITE_SPEAR`, `COPPER_SPEAR`）に加え、`KINETIC_WEAPON` コンポーネントを持つカスタムアイテムにも対応します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `ticks` | `t`, `time` | 構え経過 tick 数の比較式（例: `>=10`, `5to20`。空なら構えている間 `true`） | `""` |
| `hand` | `h` | 構えている手のフィルタ（`ANY`, `MAIN`/`HAND`, `OFF`/`OFF_HAND`） | `ANY` |
| `includetrident` | `trident` | トライデントも対象に含めるか | `false` |
| `includekinetic` | `kinetic` | `KINETIC_WEAPON` コンポーネント持ちカスタムアイテムも対象に含めるか | `true` |

## Examples
```yml
Conditions:
  - lmUsingSpear true
  - lmUsingSpear{ticks=>=10;hand=MAIN} true
```
