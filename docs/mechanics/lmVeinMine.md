# lmVeinMine

Aliases: `VeinMine`

## Description
ターゲット地点から隣接する同種または指定ブロックを幅優先探索（BFS）で探索し、一括破壊または指定ブロックへの置換を行います。メインハンドのツール（幸運・シルクタッチ）やワールド保護イベントを尊重できます。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `materials` | `m` | 対象ブロック名（カンマ区切り。省略時は開始位置のブロック種） | `""` |
| `limit` | `l` | 一括処理する最大ブロック数（上限 `256`） | `16` |
| `radius` | `r` | 開始地点からの最大探索半径（上限 `24`） | `8` |
| `diagonal` | `diag` | 斜め隣接（26近傍）を含めるか（`false` で6近傍） | `true` |
| `drop` | `d` | アイテムをドロップさせるか | `true` |
| `usetool` | `ut` | キャスターのメインハンドツールを適用するか | `true` |
| `respectprotection` | `rp` | `BlockBreakEvent` を発行しキャンセル時にスキップするか | `true` |
| `replace` | `rep` | 破壊せず指定ブロックに置換する（例: `STONE`） | `""` |
| `var` | `v` | 処理したブロック数を格納するスキル変数名 | `lmmm_vein_count` |
| `onblock` | `ob` | 各ブロック位置を `@Origin` として実行するスキル | - |

## Examples
```yml
Skills:
  - lmVeinMine{m=COAL_ORE,DEEPSLATE_COAL_ORE;limit=24;drop=true;useTool=true;var=vein_count;onBlock=MineSpark} @targetlocation
```
