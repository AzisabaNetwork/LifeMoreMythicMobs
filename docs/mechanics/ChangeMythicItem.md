# ChangeMythicItem

プレイヤーのメインハンドにある MythicMobs アイテムを、別の MythicMobs アイテムへ置き換えます。

```yaml
Skills:
  - changeMythicItem{from=ItemA;to=ItemB} @self
```

`from` (`f`) と `to` (`t`) には MythicMobs アイテムの内部名を指定します。対象がプレイヤーであり、メインハンドのアイテムが `from` と一致した場合だけ置換します。元のスタック個数は維持されます。

エイリアス: `mmchange`
