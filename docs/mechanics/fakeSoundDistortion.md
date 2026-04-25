# fakeSoundDistortion
## Description
特定のサウンドを指定した秒数繰り返し、ピッチを動的に変化させる
## Attributes
| Attribute  | Aliases | Description | Default                |
|:-----------|:--------|:------------|:-----------------------|
| sound      | s       | 再生するサウンド    | `block.note_block.bit` |
| duration   | d       | サウンドを再生する秒数 | 20                     |
| startPitch | sp      | 再生開始時のピッチ   | 2.0                    |
| endPitch   | ep      | 再生終了時のピッチ   | 0.5                    |
| volume     | v       | サウンドの怨霊     | 1.0                    |

## Examples
> `block.note_block.chime`をなんかいい感じに流す
```yml
いっぱいサウンド流すマン:
  Skills:
    - fakeSoundDistortion{s=block.note_block.chime;sp=1.75;ep=0.8;d=10} @self
```

## Aliases
- fakesound