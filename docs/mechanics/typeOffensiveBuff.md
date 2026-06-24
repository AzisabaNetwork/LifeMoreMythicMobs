# typeOffensiveBuff
## Description
対象にオフェンシブバフオーラを付与する。オーラがアクティブな間、`getCombinedMods(uuid)`で合算された倍率マップを取得でき、ダメージ計算などに利用できる。同名オーラのスタックを複数重ねることが可能。
## Attributes
| Attribute    | Aliases              | Description                                                                 | Default           |
|:-------------|:---------------------|:----------------------------------------------------------------------------|:------------------|
| auraName     | aura, name, n        | オーラの識別名                                                                     | typeoffensivebuff |
| mods         | mod                  | 補正値の定義。`キー 値`をカンマ区切りで指定。`%`付きでパーセント指定、なしで直接加算。スペースは`<&sp>`、カンマは`<&cm>`でエスケープ | (なし)              |
| duration     | d                    | オーラの持続時間(tick)                                                               | 100               |
| maxStacks    | ms                   | 最大スタック数。上限に達すると新規付与はスキップ                                                    | 1                 |
| stackTimer   | st                   | `true`=スタック個別にタイマー管理、`false`=最初のスタックのタイマーが切れると全スタック同時終了                     | false             |
| refreshDuration | rd                | `true`=新規付与時に既存スタックの残り時間をリセット                                               | false             |
| interval     | i                    | onTickスキルを実行するtick間隔                                                         | 4                 |
| onStart      | oS, onstart, onStartSkill | オーラが付与されたとき発火                                                          |                   |
| onTick       | oT, ontick, onTickSkill   | interval毎に発火                                                              |                   |
| onEnd        | oE, onend, onEndSkill     | オーラの効果時間が終わったとき発火                                                      |                   |

### modsの書き方
```
mods="fire 0.5, ice 30%"
```
- `fire 0.5` → fireの倍率に+0.5（getCombinedModsで`fire=1.5`が返る）
- `ice 30%` → iceの倍率に+0.30（getCombinedModsで`ice=1.30`が返る）
- 複数スタックがある場合は全スタックの値が合算される

### スキル変数
onStart/onTick/onEndスキル実行時に以下の変数が設定される。

| 変数名          | 内容              |
|:-------------|:----------------|
| aura-name    | オーラ名            |
| aura-stacks  | 現在のアクティブスタック数   |
| aura-duration | 残り時間(tick)      |

## Examples
> 攻撃力を50%増加するバフを最大5スタックまで付与する
```yml
AttackBuff:
 Skills:
 - typeOffensiveBuff{n=powerBuff;mods="attack 50%";d=200;ms=5;rd=true;i=20;oS=PowerBuffStart;oE=PowerBuffEnd} @self

PowerBuffStart:
 Skills:
 - message{m="&a攻撃バフを獲得した！(スタック: <var.aura-stacks>)"} @self

PowerBuffEnd:
 Skills:
 - message{m="&c攻撃バフが切れた"} @self
```
> 複数の属性に補正をかける例
```yml
ElementalBuff:
 Skills:
 - typeOffensiveBuff{n=elementBuff;mods="fire 0.3, ice 20%, thunder 1.0";d=100;ms=3;st=true} @target
```

## ダメージ計算式

`typedDamage` メカニックでの最終ダメージ計算式：

```
最終ダメージ = base × multiplier

base        = amount × power
multiplier  = max(0, targetAuraMod - upgradeRes) × max(0, casterAuraMod + upgradeDmg)
```

| 変数 | 取得元 | 説明 |
|:---|:---|:---|
| `targetAuraMod` | `TypeBuffMechanic.getCombinedMods(target)[element]` | 被ダメ側のAura補正（デフォルト1.0） |
| `upgradeRes` | `upg_total_<element>_res` 変数 × 0.01 | 被ダメ側のUpgrade耐性補正（減算） |
| `casterAuraMod` | `TypeOffensiveBuffMechanic.getCombinedMods(caster)[element]` | 与ダメ側のAura補正（デフォルト1.0） |
| `upgradeDmg` | `upg_total_<element>_dmg` 変数 × 0.01 | 与ダメ側のUpgrade攻撃補正（加算） |

### 計算例

- `typeOffensiveBuff` で fire +50% → `casterAuraMod = 1.5`
- `typeBuff` で fire -50% → `targetAuraMod = 0.5`
- 結果: `0.5 × 1.5 = 0.75` → **ダメージ-25%**（相殺しても0%にはならない）

> `typeBuff`（被ダメ補正）と`typeOffensiveBuff`（与ダメ補正）は**掛け算**で合成される。

## Aliases
- typeOffensiveBuff
- typeoffensivebuff
