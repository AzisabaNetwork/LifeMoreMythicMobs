# MeleeElement

直殴り(`ENTITY_ATTACK`、なぎ払いの `ENTITY_SWEEP_ATTACK` を含む)に属性を付ける。
MythicMobs のアイテムか Mob の定義に書く。書かなければ今までどおりの直殴り。

```yaml
FireSword:          # アイテム：メインハンドに持って殴ると FIRE の直殴りになる
  Id: DIAMOND_SWORD
  MeleeElement: FIRE

FlameGolem:         # Mob：この Mob の直殴りが FIRE になる
  Type: IRON_GOLEM
  MeleeElement: FIRE
```

## 複数の属性
`MeleeElement: FIRE,ICE` のように、`,` か縦棒で区切って複数書ける(tDamage の `e=` と同じ)。
殴った相手ごとに、いちばんダメージが大きくなる属性を1つ選ぶ。比べるのは、下のバフ・強化・宝玉の倍率と、
受けた Mob の `DamageModifiers` を掛けた結果。同じ値なら先に書いた方。何も無ければ先頭の属性。

## 属性が付いた直殴りの扱い
tDamage の属性ダメージと同じ扱いになる。

- 撃った側の `typeOffensiveBuff`・強化(`upg_total_<属性>_dmg`)・HoroloCore の宝玉の属性攻撃と、
  受けた側の `typeBuff`・強化(`upg_total_<属性>_res`)の倍率が掛かる。
- 受けたのが MythicMobs の Mob なら、`DamageModifiers` は `ENTITY_ATTACK` の代わりに**属性の値**を使う。
  (例:`FIRE 0.5` の Mob を FIRE の剣で殴ると半分。`ENTITY_ATTACK` の値は掛からない)
- 受けたのがプレイヤーなら、HoroloCore の属性防御(装飾品・宝玉・`elemDefBuff`)が効く。計算は mobTDamage と同じ。
- スキルのダメージ(`damage`・`tDamage`)には影響しない(スキル側の属性を使う)。

## 注意
- プレイヤーの場合、見るのは**メインハンド**のアイテムの MMID だけ。
- `DamageModifiers` の `ENTITY_ATTACK` が 0 の Mob(LegacyDamage の ModifierOrder が false のもの)は、
  MythicMobs が最後に 0 を掛けるので、属性を付けても 0 のまま。
