# nullRecovery
## Description
対象にAuraを付与し、効果時間中のHP回復を阻害する。
## Attributes
| Attribute | Aliases | Description               | Default |
|:----------|:--------|:--------------------------|:--------|
| auraName  | n       | オーラの名前を指定                 | default |
| duration  | d       | オーラの持続時間(tick)            | 100     |
| amount    | a       | 阻害する量 / 割合の場合は%、固定値の場合は整数 | 100%    |
| onStart   | oS      | オーラが付与されたら発火              |         |
| onHeal    | oH      | ヒールされたら発火                 |         |
| onEnd     | oE      | オーラの効果時間が終わったら発火          |         |

## Examples
> @targetのプレイヤーを絶対に回復させない害悪攻撃
```yml
回復させたくない:
 Skills:
 - nullRecovery{n=お前に絶対回復させないマン;d=9999999;a=100%;oS=回復させない;oH=回復したね} @target

回復させない:
 Skills:
 - message{m="&c&l回復できないよ～～んｗｗ"}

回復したね:
 Skills:
 - message{m="&c&l回復出来ないってｗｗｗ"}
```