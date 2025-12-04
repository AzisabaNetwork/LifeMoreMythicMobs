# LifeMoreMythicMobs
LifeMoreMythicMobsで追加されたスキル群の説明。  
極稀にAliasesに日本語が含まれている場合がありますが、**普通に見づらいので使わないでください。絶対に。**

---

## ショートカット
- [Mechanics](#mechanics)
    - [Meta Mechanics](#meta-mechanics)
- [Conditions](#conditions)
- [Placeholders](#placeholders)

---

## Mechanics
具体的なMechanicの記述法は[wiki](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/Skills/Mechanics)を見よう！

| Mechanic | Description |
|:---------------------------------------------------------------|:--|
| [takeinv](docs/mechanics/takeinv.md) | インベントリからアイテムを削除します |
| [particleVerticalRing](docs/mechanics/particleverticalring.md) | particleRingの向きが違うバージョン |
| bossbar | ボスバーを表示します。非推奨 |
| removebossbar | bossbarで作ったやつを削除します |
| modifybossbar | bossbarで作ったやつをいじれます |

---

## Meta Mechanics
wikiのMetaMechanicsに書いてあるものと、恐らく同じような効果を持つMechanicたち

> これらのスキルメカニクスは特別な高度な機能を持ち、そのほとんどは他のスキルを呼び出すために使用されます。  
> ターゲットを指定した場合、これらによって呼び出される他のすべてのスキルは（該当する場合）そのターゲットを「継承」します。(DeepL)

| Mechanic | Description |
|:---------------------------------------------------------|:------------------------------------------|
| [MMLuckEval](docs/mechanics/MMLuckEval.md) | <font color=#e6798c>わ...わかんないっピ...</font> |
| [SetVarDisplayName](docs/mechanics/SetVarDisplayName.md) | アイテムのDisplayを変数に入れます |
| [SetVarLoreLine](docs/mechanics/SetVarLoreLine.md) | アイテムのLoreを変数に入れます |
| [VarSubstring](docs/mechanics/VarSubstring.md) | 変数から文字列を抽出します |
| [VarExtractNumber](docs/mechanics/VarExtractNumber.md) | 変数から数値のみを抽出します |
| [VarReplaceRegex](docs/mechanics/VarReplaceRegex.md) | 変数の文字列を設定された正規表現に基づいて置換します |

---

## Conditions
具体的なConditionの記述法は[wiki](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/Skills/conditions)を見よう！

| Condition | Type | Description |
|:------------------------------------------------------------------|:-------|:-------------------------------|
| [ItemInSlot](docs/conditions/ItemInSlot.md) | Entity | 指定されたスロットに指定されたmmidのアイテムが存在するか |
| [PlayersInRadius](docs/conditions/PlayersInRadius.md) | Entity | 指定された範囲内にプレイヤーが何人いるか |
| [isPet](docs/conditions/isPet.md) | Entity | myPetのモブか |
| [HasEmptyInventorySlot](docs/conditions/HasEmptyInventorySlot.md) | Entity | 対象のエンティティのインベントリに空きスロットがあるか |
| [BowTension](docs/conditions/BowTension.md) | Meta | 弓の引き絞り具合を確認 |
| [mmidContains](docs/conditions/mmidContains.md) | Meta | mmidに指定された文字列が含まれているか |
| [mmidStartsWith](docs/conditions/mmidStartsWith.md) | Meta | mmidの最初に指定された文字列が含まれているか |
| [serverEquals](docs/conditions/serverEquals.md) | Meta | スキルが実行されたサーバー名を確認 |
| [varNotEquals](docs/conditions/varNotEquals.md) | Meta | variableEqualsの逆 |
| [DayOfWeek](docs/conditions/DayOfWeek.md) | Meta | 曜日を確認 |
| [realtime](docs/conditions/realtime.md) | Meta | 時刻を確認 |
| [CompareValues](docs/conditions/CompareValues.md) | Meta | 指定された二つの値を比較 |

---

## Placeholders
具体的なPlaceholderの記述法は[wiki](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/Skills/Placeholders)を見よう！

| Placeholder | Function |
|:----------------------|:------------------------------------|
| `<caster.armor>` | 対象の防御力値を返します |
| `<caster.attack>` | 対象の攻撃力値を返します |
| `<caster.mainhand.tag>` | 対象のメインハンドアイテムのCustomModelData値を返します |
| `<caster.offhand.tag>` | 対象のオフハンドアイテムのCustomModelData値を返します |
| `<caster.mmid>` | 対象のメインハンドアイテムのmmidを返します |
| `<server-name>` | 実行されているサーバーの名前を返します |
