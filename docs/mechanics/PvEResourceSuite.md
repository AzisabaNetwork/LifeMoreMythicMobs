# PvE & 資源コンテンツ拡張スイート

PvE（ボス戦・ダンジョンギミック・戦闘アクション）および資源コンテンツ（採取・採掘・ミニゲーム・報酬抽選）を拡張するための Mechanics / Conditions / Placeholders 一覧です。

---

## Mechanics

### `lmTether` (別名: `TetherBeam`)
キャスター（または `fromOrigin=true` で `@Origin`）とターゲットを結ぶ追従 3D ビーム／鎖（`BlockDisplay` + パーティクル）。
- `block` / `b`: ブロック名 (`none` でブロック非表示、既定 `sea_lantern`)
- `width` / `w`: 太さ (既定 `0.16`)
- `duration` / `d`: 持続 tick (既定 `100`)
- `interval` / `i`: 更新間隔 tick (既定 `2`)
- `maxDist` / `maxd`: 最大距離。超えると `onBreak` を発火して切断
- `minDist` / `mind`: 最小距離。下回ると `onNear` を発火（`breakOnNear=true` で終了）
- `onCross` / `oc`: 第三者が線分（`crossRadius` 既定 `0.8`）を横切ると、そのエンティティを trigger として発火
- `onTick` / `onEnd` / `glow`: 各タイミングのスキル・発光色 (`#RRGGBB`)

```yaml
- lmTether{b=red_stained_glass;w=0.18;d=100;maxDist=14;onBreak=ChainBreak;onCross=LaserHit;glow=#FF3333} @target
```

---

### `lmFloatingText` (別名: `FloatingText`)
空間やエンティティ頭上に `TextDisplay` を生成し、プレースホルダーを動的更新する。
- `id`: 識別子（同じ `id` で上書き更新、`remove=true` で削除）
- `text` / `t`: 表示文字（`\n` または `<nl>` で改行）
- `yoffset` / `y`: 高さオフセット (既定 `2.3`)
- `duration` / `d`: 持続 tick (既定 `60`)
- `interval` / `i`: 更新間隔 tick (既定 `5`)
- `follow` / `f`: ターゲット追従 (既定 `true`)
- `scale` / `s` / `billboard` / `bg` (`transparent` または `#AARRGGBB`)

```yaml
- lmFloatingText{id=cast;t="&e詠唱中... &f<caster.var.cast_sec>";y=2.5;d=40;i=2;scale=1.2} @self
```

---

### `lmTowerZone` (別名: `TowerZone`)
塔踏み・頭割り・防衛サークルギミック。地面の `BlockDisplay` リングと頭上人数表示を自動描画する。
- `radius` / `r`: 半径 (既定 `3.0`)
- `required` / `req`: 必要最低人数 (既定 `1`)
- `maxPlayers` / `max`: 最大許容人数 (既定 `-1` で上限なし)
- `duration` / `d`: 判定までの tick (既定 `100`)
- `instant` / `inst`: 条件達成で即成功終了するか (既定 `false`)
- `label` / `l`: 頭上ラベル
- `onSuccess` / `onFail` / `onEnter` / `onLeave` / `onTick`

```yaml
- lmTowerZone{r=3.5;req=2;d=100;label="&e塔";onSuccess=TowerClear;onFail=TowerExplode} @selflocation
```

---

### `lmResourceNode` (別名: `ResourceNode`)
採取・採掘・宝箱・スイッチ用のインタラクティブ資源ノード（`Interaction` + `BlockDisplay` + 耐久ゲージ `TextDisplay`）。
- `label` / `l`: 表示名
- `block` / `b`: 通常時のブロック (既定 `diamond_ore`)
- `depletedBlock` / `db`: 枯渇時のブロック (`none` で非表示、既定 `cobblestone`)
- `hp`: 耐久回数 (既定 `5`)
- `respawn` / `rs`: 再生までの tick (`0` なら枯渇後消滅)
- `duration` / `d`: 自動消滅までの tick (`0` なら無期限)
- `click` / `c`: `BOTH` / `RIGHT` / `LEFT`
- `reqMMID` / `tool`: 必要な MythicItem ID（カンマ区切りOR可）
- `onHit` / `onBreak` / `onRespawn` / `onWrongTool`

```yaml
- lmResourceNode{label="&b蒼晶鉱脈";b=diamond_ore;db=bedrock;hp=5;respawn=200;onHit=MineHit;onBreak=MineDrop;glow=#00FFFF} @targetlocation
```

---

### `lmSkillCheck` (別名: `SkillCheck`)
ActionBar 上に動くタイミングバー（QTE）を表示し、プレイヤー入力で `GREAT` / `GOOD` / `FAIL` を判定する。
- `label` / `l`: ラベル
- `speed` / `sp`: 1tick の移動量 (既定 `0.045`)
- `duration` / `d`: 制限時間 tick (既定 `80`)
- `input` / `in`: `SNEAK` (Shift) または `CLICK` (左クリック)
- `great` / `gr` (既定 `0.08`), `good` / `go` (既定 `0.20`), `center` / `c` (`-1` でランダム)
- `onGreat` / `onGood` / `onFail`

```yaml
- lmSkillCheck{label="&e採掘";speed=0.045;d=80;input=SNEAK;onGreat=MineCrit;onGood=MineNormal;onFail=MineMiss} @trigger
```

---

### `lmVeinMine` (別名: `VeinMine`)
ターゲット地点から隣接する同種または指定ブロックを BFS で一括採掘／置換する。
- `materials` / `m`: 対象ブロック（カンマ区切り。省略時は開始位置のブロック種）
- `limit` / `l`: 最大数 (既定 `16`, 上限 `256`)
- `radius` / `r`: 最大探索半径 (既定 `8`)
- `drop` / `d`: ドロップするか (既定 `true`)
- `useTool` / `ut`: メインハンドのツール（幸運・シルクタッチ）を適用 (既定 `true`)
- `replace` / `rep`: 破壊せず指定ブロックに置換
- `var` / `v`: 破壊数を格納するスキル変数名 (既定 `lmmm_vein_count`)
- `onBlock` / `ob`: 1ブロックごとに `@Origin` をその座標にして実行するスキル

```yaml
- lmVeinMine{m=COAL_ORE,DEEPSLATE_COAL_ORE;limit=24;drop=true;useTool=true;var=vein_count;onBlock=MineSpark} @targetlocation
```

---

### `lmParryWindow` (別名: `ParryWindow`)
短時間のパリィ／受け流し受付状態を付与する。
- `duration` / `d`: 受付 tick (既定 `10`)
- `reduce` / `r`: ダメージ軽減率 `0.0〜1.0` (既定 `1.0` = 完全無効)
- `consume` / `c`: 1回成功で終了するか (既定 `true`)
- `onParry` / `op`: 成功時スキル（攻撃者が `trigger`）
- `onExpire` / `oe`: 不発終了時スキル

---

### `lmMarkStack` (別名: `MarkStack`)
対象エンティティに期限付きスタック型マーク（印）を付与・起爆する。
- `id` / `mark`: マーク名
- `mode` / `op`: `ADD` / `SET` / `TAKE` / `DETONATE` / `CLEAR`
- `amount` / `a`: 増減値 (既定 `1`)
- `max` / `mx`: 上限 (`0` で無制限)
- `duration` / `d`: 持続 tick (既定 `200`, `0` 以下で無期限)
- `resetOnMax` / `rom`: 上限到達時にリセット (既定 `true`)
- `var` / `v`: 結果スタック数を格納する変数名 (既定 `lmmm_mark_stacks`)
- `onMax` / `onDetonate`

---

### `lmChainSkill` (別名: `ChainSkill`)
ターゲット間を連鎖してスキルとパーティクル線を実行する。
- `bounces` / `b`: 最大連鎖数 (既定 `4`)
- `radius` / `r`: 連鎖検索半径 (既定 `6.0`)
- `delay` / `d`: 連鎖間隔 tick (既定 `2`)
- `decay` / `dc`: 減衰率 (既定 `1.0`)
- `target` / `tf`: `ALL` / `PLAYERS` / `MOBS`
- `particle` / `p`: 線パーティクル (`none` でなし)
- `indexVar` (`lmmm_chain_index`), `scaleVar` (`lmmm_chain_scale`), `onHit`

---

### `lmThreat` (別名: `ThreatControl`)
MythicMob のヘイトテーブルを操作する。
- `mode` / `m`: `TAUNT` / `ADD` / `SET` / `MULTIPLY` / `DROP` / `CLEAR`
- `amount` / `a`: 値 (既定 `100.0`)

---

### `lmDash` (別名: `MotionDash`)
精密なダッシュ・引き寄せ・打ち上げ移動。
- `mode` / `m`: `FORWARD` / `TOWARD` / `AWAY` / `UP`
- `power` / `p`: 速度倍率 (既定 `1.2`)
- `yBoost` / `y`: Y軸加算 (既定 `0.25`)
- `ignoreY` / `iy`: 水平方向のみにするか (既定 `true`)
- `add` / `a`: 既存速度に加算するか (既定 `false`)
- `noFall` / `nf`: 落下ダメージ無効 tick (既定 `0`)

---

### `lmLootRoll` (別名: `LootRoll`)
重み付きランダム抽選（`GENERIC_LUCK` に応じたレア枠ブースト対応）。
- `entries` / `e`: `"ItemA:60:2,skill:RareSkill:10,nothing:30"` 形式
- `rolls` / `r`: 抽選回数 (既定 `1`)
- `luckFactor` / `lf`: 平均より重みが小さい枠への `Luck * luckFactor` ブースト倍率 (既定 `0.0`)

---

## Conditions & Placeholders

- `?lmMarkStacks{id=burn;v=>=3}`: エンティティのマークスタック数判定
- `?lmIsTopThreat`: キャスターのトップヘイト対象かどうか
- `?lmBiome{b=PLAINS,FOREST}`: バイオーム判定
- `?lmLightLevel{l=<=7;type=TOTAL}`: 光レベル判定 (`TOTAL` / `BLOCK` / `SKY`)
- `?lmBlockCount{m=WATER,BUBBLE_COLUMN;r=3;c=>=4}`: 周囲立方範囲内のブロック数判定
- `?lmCanSeeSky`: 空が見える位置かどうか
- `?lmUsingSpear{ticks=>=10;hand=MAIN;trident=false;kinetic=true}` (別名: `isUsingSpear`, `usingSpear`, `spearReady`): 槍（1.21.11の各種Spearおよび `KINETIC_WEAPON` コンポーネント持ちアイテム）を構えているかどうか
- `<caster.lmmm.mark.ID>` / `<target.lmmm.mark.ID>`: マークスタック数
- `<caster.lmmm.biome>` / `<target.lmmm.biome>`: バイオームキー名
- `<caster.lmmm.light>` / `<target.lmmm.light>`: 光レベル
- `<caster.lmmm.spear_ticks>` / `<target.lmmm.spear_ticks>`: 槍を構えている経過 tick 数
