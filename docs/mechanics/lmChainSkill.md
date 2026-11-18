# lmChainSkill

Aliases: `ChainSkill`

## Description
最初のターゲットから半径内の別エンティティへと次々に連鎖（チェイン）し、パーティクル線と `onHit` スキルを実行します。チェインライトニングや連鎖ヒールに適しています。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `bounces` | `b`, `max` | 最大連鎖回数（上限 `32`） | `4` |
| `radius` | `r` | 次の対象を検索する半径 | `6.0` |
| `delay` | `d` | 次の対象へ跳ぶまでの遅延（tick、`0` で即時） | `2` |
| `decay` | `dc` | 1連鎖ごとの減衰倍率（`lmmm_chain_scale` に入る） | `1.0` |
| `allowrepeat` | `rep` | 一度当たった対象に再度連鎖するか | `false` |
| `target` | `tf` | 連鎖対象のフィルタ（`ALL`, `PLAYERS`, `MOBS`） | `ALL` |
| `particle` | `p` | 連鎖間に描画するパーティクル（`none` でなし） | `ELECTRIC_SPARK` |
| `particlestep` | `ps` | パーティクル線の間隔 | `0.35` |
| `indexvar` | `iv` | 連鎖ステップ番号（1〜）を格納するスキル変数名 | `lmmm_chain_index` |
| `scalevar` | `sv` | 減衰倍率（`decay^(step-1)`）を格納するスキル変数名 | `lmmm_chain_scale` |
| `onhit` | `oh` | 各対象へのヒット時に実行するスキル | - |

## Examples
```yml
Skills:
  - lmChainSkill{bounces=5;r=7;delay=3;decay=0.85;target=MOBS;p=ELECTRIC_SPARK;onHit=ChainZap} @target
```
