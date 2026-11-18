# lmResourceNode

Aliases: `ResourceNode`

## Description
採取・採掘・宝箱・ギミックスイッチ用のインタラクティブ資源ノード（`Interaction` + `BlockDisplay` + 耐久ゲージ `TextDisplay`）を設置します。プレイヤーのクリックで耐久値が減少し、枯渇時の報酬スキル実行や自動リポップに対応します。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `label` | `l` | 頭上に表示するノード名 | `""` |
| `block` | `b` | 通常時の見た目ブロック | `diamond_ore` |
| `depletedblock` | `db` | 枯渇時の見た目ブロック（`none` で非表示） | `cobblestone` |
| `scale` | `s` | 表示ブロックのスケール | `1.0` |
| `hitboxwidth` | `hw` | クリック判定の幅 | `scale + 0.15` |
| `hitboxheight` | `hh` | クリック判定の高さ | `scale + 0.15` |
| `hp` | `health` | 破壊までに必要なクリック回数 | `5` |
| `respawn` | `rs` | 枯渇から復活までの時間（tick、`0` 以下で消滅） | `0` |
| `duration` | `d` | 自動消滅までの時間（tick、`0` で無期限） | `0` |
| `cooldown` | `cd` | 同一プレイヤーの連続クリック制限（tick） | `5` |
| `yoffset` | `y` | 設置高さオフセット | `0.0` |
| `click` | `c` | 受付クリック種別（`BOTH`, `RIGHT`, `LEFT`） | `BOTH` |
| `reqmmid` | `tool`, `req` | 必要なメインハンドの MythicItem ID（カンマ区切り OR 可） | `""` |
| `glow` | `gl` | 発光色（`#RRGGBB`） | `""` |
| `onhit` | `oh` | クリック成功時に実行するスキル（対象プレイヤーが `trigger`） | - |
| `onbreak` | `ob` | 耐久値が 0 になった時に実行するスキル | - |
| `onrespawn` | `or` | 復活時に実行するスキル | - |
| `onwrongtool` | `owt` | 必要ツールを持たずにクリックした時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmResourceNode{label="&b蒼晶鉱脈";b=diamond_ore;db=bedrock;hp=5;respawn=200;reqMMID=MITHRIL_PICKAXE;onHit=MineHit;onBreak=MineDrop;onWrongTool=NeedPickaxe;glow=#00FFFF} @targetlocation
```
