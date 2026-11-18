# lmBiome

## Description
エンティティまたはターゲット座標が指定したバイオーム（カンマ区切り OR 指定可）にあるかチェックします。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `biome` | `biomes`, `b` | バイオームキー名（例: `PLAINS,FOREST,TAIGA` または `minecraft:plains`） | `""` |

## Examples
```yml
Conditions:
  - lmBiome{b=PLAINS,FOREST,FLOWER_FOREST} true
```
