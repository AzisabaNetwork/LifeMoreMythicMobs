# LifeMoreMythicMobs Documentation

このプラグインが追加するMechanics, Conditions, Placeholdersの詳細について記載しています。

## Commands

| Command | Description |
|:--------|:------------|
| [/lmmm reloadFile](commands/reloadFile.md) | MythicMobs のファイル・フォルダを全体リロードせずに読み直す(短縮: `/lmm r`) |

## Mob Options

| Option | Description |
|:-------|:------------|
| [LegacyDamage](mobs/LegacyDamage.md) | ダメージ周りの挙動を MythicMobs 4.12 と同じにする |

## Mechanics

| Mechanic | Description |
|:---------|:------------|
| [ChangeMythicItem](mechanics/ChangeMythicItem.md) | MythicItem の変換 |
| [fakeWorldBorder](mechanics/fakeWorldBorder.md) | プレイヤーの視界にのみ偽のワールドボーダーを出現させる |
| [fakeSoundDistortion](mechanics/fakeSoundDistortion.md) | 指定した音を歪ませて再生する |
| [lmChainSkill](mechanics/lmChainSkill.md) | ターゲット間を連鎖するスキル・パーティクル線 |
| [lmDash](mechanics/lmDash.md) | 精密なダッシュ・引き寄せ・打ち上げ移動 |
| [lmFloatingText](mechanics/lmFloatingText.md) | 空間・頭上の動的 TextDisplay |
| [lmLootRoll](mechanics/lmLootRoll.md) | Luck 補正対応の重み付きルート抽選 |
| [lmMarkStack](mechanics/lmMarkStack.md) | 期限付きスタック型マークの付与・起爆 |
| [lmParryWindow](mechanics/lmParryWindow.md) | パリィ／受け流し受付ウィンドウ |
| [lmResourceNode](mechanics/lmResourceNode.md) | インタラクティブ採取・採掘資源ノード |
| [lmSkillCheck](mechanics/lmSkillCheck.md) | ActionBar タイミングバー（QTE）ミニゲーム |
| [lmTether](mechanics/lmTether.md) | 追従 3D ビーム／鎖・横切り判定ギミック |
| [lmThreat](mechanics/lmThreat.md) | MythicMob のヘイトテーブル操作・挑発 |
| [lmTowerZone](mechanics/lmTowerZone.md) | 塔踏み・頭割り・防衛サークルギミック |
| [lmVeinMine](mechanics/lmVeinMine.md) | 鉱脈・隣接ブロックの一括採掘／置換 |
| [MMLuckEval](mechanics/MMLuckEval.md) | MMのLuckステータスを使用した計算 |
| [nullRecovery](mechanics/nullRecovery.md) | Null値のリカバリ |
| [particleverticalring](mechanics/particleverticalring.md) | 垂直リング状のパーティクル |
| [setFirstPersonView](mechanics/setFirstPersonView.md) | 一人称視点の設定 |
| [SetVarDisplayName](mechanics/SetVarDisplayName.md) | 変数を用いたDisplayNameの設定 |
| [SetVarLoreLine](mechanics/SetVarLoreLine.md) | 変数を用いたLore行の設定 |
| [skyRotation](mechanics/skyRotation.md) | 空の回転設定 |
| [slotJam](mechanics/slotJam.md) | スロットの詰め替え |
| [takeinv](mechanics/takeinv.md) | インベントリからアイテムを削除 |
| [tDamage](mechanics/tDamage.md) | 複数属性対応の属性ダメージ |
| [typeOffensiveBuff](mechanics/typeOffensiveBuff.md) | 属性攻撃バフの付与 |
| [VarExtractNumber](mechanics/VarExtractNumber.md) | 変数から数値を抽出 |
| [VarReplaceRegex](mechanics/VarReplaceRegex.md) | 正規表現による変数置換 |
| [VarSubstring](mechanics/VarSubstring.md) | 変数の部分文字列取得 |
| [PvEResourceSuite](mechanics/PvEResourceSuite.md) | PvE・資源コンテンツ拡張スイート概要 |

## Conditions

| Condition | Description |
|:----------|:------------|
| [BowTension](conditions/BowTension.md) | 弓のテンションをチェック |
| [CompareValues](conditions/CompareValues.md) | 値の比較 |
| [DayOfWeek](conditions/DayOfWeek.md) | 曜日チェック |
| [HasEmptyInventorySlot](conditions/HasEmptyInventorySlot.md) | 空のインベントリスロット有無チェック |
| [isPet](conditions/isPet.md) | myPetのモブかどうかチェック |
| [ItemInSlot](conditions/ItemInSlot.md) | 特定スロットのアイテムチェック |
| [lmBiome](conditions/lmBiome.md) | バイオーム一致チェック |
| [lmBlockCount](conditions/lmBlockCount.md) | 周囲ブロック数の判定 |
| [lmCanSeeSky](conditions/lmCanSeeSky.md) | 空が見えるか（屋外／洞窟）チェック |
| [lmIsTopThreat](conditions/lmIsTopThreat.md) | トップヘイト保持者かチェック |
| [lmLightLevel](conditions/lmLightLevel.md) | 光レベルの判定 |
| [lmMarkStacks](conditions/lmMarkStacks.md) | マークスタック数の判定 |
| [lmUsingSpear](conditions/lmUsingSpear.md) | 槍を構えているか・構え時間チェック |
| [mmidContains](conditions/mmidContains.md) | MythicMob IDの部分一致チェック |
| [mmidStartsWith](conditions/mmidStartsWith.md) | MythicMob IDの前方一致チェック |
| [PlayersInRadius](conditions/PlayersInRadius.md) | 半径内のプレイヤー数チェック |
| [realtime](conditions/realtime.md) | リアルタイムチェック |
| [serverEquals](conditions/serverEquals.md) | サーバー名の一致チェック |
| [varNotEquals](conditions/varNotEquals.md) | 変数の不一致チェック |

## Placeholders

| Placeholder | Description |
|:------------|:------------|
| `<caster.lmmm.mark.ID>` / `<target.lmmm.mark.ID>` | `lmMarkStack` の現在スタック数 |
| `<caster.lmmm.biome>` / `<target.lmmm.biome>` | 現在位置のバイオームキー名 |
| `<caster.lmmm.light>` / `<target.lmmm.light>` | 現在位置の光レベル（`0〜15`） |
| `<caster.lmmm.spear_ticks>` / `<target.lmmm.spear_ticks>` | 槍を構えている経過 tick 数 |

各MechanicやConditionの詳細については、各ドキュメントを参照してください。

また、[GitHub Wiki](https://github.com/AzisabaNetwork/LifeMoreMythicMobs/wiki)も合わせてご確認ください。
