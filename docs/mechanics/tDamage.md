# tDamage / mobTDamage の複数属性
## 書き方
`e`(`element`・`type`・`t`)に属性を `,` か `|` で区切って複数書ける。1つだけ書いた場合は今までと同じ。

```yml
- tDamage{a=100;e=FIRE,ICE} @target
- mobTDamage{a=50;e=FIRE|ICE|THUNDER} @target
```

## どの属性になるか
当たった相手ごとに、**いちばんダメージが大きくなる属性**を1つ選んで、その属性のダメージとして与える。比べるのは次の掛け算。

- 撃った側の `typeOffensiveBuff`・強化(`upg_total_<属性>_dmg`)・時計塔の属性攻撃
- 受けた側の `typeBuff`・強化(`upg_total_<属性>_res`)
- 受けた Mob の `DamageModifiers`

同じ値なら先に書いた属性。バフも DamageModifiers も無ければ、先頭の属性になる。

`<skill.var.damage-type>` には選ばれた属性が入る。

# クリティカル
MythicMobs 標準のクリティカル(Stats の `CRITICAL_STRIKE_*`)は通常攻撃にしか掛からず、スキルのダメージには掛からない。
tDamage(と tDamage として動く `damage`)では、`crit=true` を書いた行だけクリティカルを判定する。**書かなければクリティカルは出ない。**

| 指定 | 別名 | 内容 | 省略時 |
|:--|:--|:--|:--|
| `crit` | `critical` | クリティカルを判定するか | `false` |
| `critChance` | `crc` | 発生率(1 で 100%) | 撃った側の `CRITICAL_STRIKE_CHANCE` |
| `critDamage` | `crd` | 上乗せ倍率(0.5 なら 1.5 倍) | 撃った側の `CRITICAL_STRIKE_DAMAGE` |

- 発生率からは、受けた側の `CRITICAL_STRIKE_RESILIENCE` を引く。
- Stats から読む値は、`stats.yml` で `Enabled: true` の項目だけ(それ以外は 0)。
- 掛かる順番: 属性・バフ・強化の倍率 → クリティカル → 受けた Mob の `DamageModifiers`。
- `<skill.var.critical>` に `true` / `false` が入る(`crit=true` の行のみ)。

```yml
- tDamage{a=100;e=FIRE;crit=true} @target                       # Stats の値で判定
- tDamage{a=100;e=FIRE;crit=true;critChance=0.25;critDamage=0.5} @target  # 25% で 1.5 倍
```
