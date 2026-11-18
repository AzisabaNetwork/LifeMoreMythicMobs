# lmFloatingText

Aliases: `FloatingText`

## Description
空間またはエンティティ頭上に `TextDisplay` を配置し、プレースホルダーを定期的に再評価しながら表示します。同一 `id` で再実行すると内容を上書き更新します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `id` | - | 表示の識別子（同じ `id` で上書き・削除可能） | `ft` |
| `text` | `t` | 表示文字列（`\n` または `<nl>` で改行） | `""` |
| `yoffset` | `y` | 高さオフセット | `2.3` |
| `duration` | `d` | 表示時間（tick） | `60` |
| `interval` | `i` | テキスト更新・追従間隔（tick） | `5` |
| `follow` | `f` | ターゲットエンティティに追従するか | `true` |
| `scale` | `s` | 文字スケール | `1.0` |
| `billboard` | `bb` | ビルボード設定（`CENTER`, `VERTICAL`, `HORIZONTAL`, `FIXED`） | `CENTER` |
| `shadow` | `sh` | 文字影を付けるか | `true` |
| `seethrough` | `st` | ブロック越しに透かして見せるか | `false` |
| `background` | `bg` | 背景色（`transparent` または `#AARRGGBB` / `#RRGGBB`） | - |
| `linewidth` | `lw` | 自動折り返し幅 | `200` |
| `remove` | `r` | `true` で指定 `id` の表示を即削除 | `false` |

## Examples
```yml
Skills:
  - lmFloatingText{id=cast;t="&e詠唱中... &f<caster.var.cast_sec>";y=2.5;d=40;i=2;scale=1.2;bg=transparent} @self
  - lmFloatingText{id=cast;remove=true} @self
```
