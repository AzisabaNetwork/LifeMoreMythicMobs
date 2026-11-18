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
