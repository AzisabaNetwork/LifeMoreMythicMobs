# LegacyDamage

Mob の設定に書くと、MythicMobs 4.12 と同じダメージの扱いに戻します。書かない Mob は 5.12 の挙動のままです。

```yaml
ExampleMob:
  Type: ZOMBIE
  LegacyDamage: true        # 下の5つをすべて 4.12 の挙動にする
```

一部だけ 5.12 の挙動にしたい場合は、項目ごとに `false` を書きます(書かなかった項目は `true` 扱い)。

```yaml
ExampleMob:
  LegacyDamage:
    EnvironmentTrigger: false
```

| 項目 | 4.12 の挙動(true) | 5.12 の挙動(false) |
|:--|:--|:--|
| `SelfDamage` | Mob が自分に与えたスキルダメージ(`damagepercent @self` など)では、自分の `~onDamaged` は発動しない | 発動する(`CancelEvent` を持つと自分へのダメージを打ち消す) |
| `EnvironmentTrigger` | `~onDamaged` はエンティティからのダメージでのみ発動する | 落下・炎・窒息などの環境ダメージでも発動する |
| `CancelledThreat` | 自分の `~onDamaged` で無効化した攻撃でも、攻撃者にヘイトが溜まる | 溜まらない |
| `ZeroModifier` | `DamageModifiers` の 0(例: `SUFFOCATION 0`)が環境ダメージにも効く | 効かない(`Mobs.CancelDamageIfZero` が false のとき) |
| `MobArmor` | Mob の `Armor` の値を毎回のダメージから引く(最低1)。Mob 本来の防御力(ゾンビなら2)はそのまま | `Armor` の値が Mob の防御力になり、防御計算(CombatMath)で割合として軽減する |
