# realtime
## Description
スキルが実行された時、現実世界での日時が指定された時間の範囲内であるかどうかをチェックします
## Attributes
### 上限
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| maxYear | maxyear, maxy, may, 年次 | **年**がこの値**以下**であること。 | MAX_VALUE |
| maxMonth | maxmonth, maxm, mamo, 月次 | **月**（1〜12）がこの値**以下**であること。 | MAX_VALUE |
| maxDay | maxday, maxd, mad, 日次 | **日**（1〜31）がこの値**以下**であること。 | MAX_VALUE |
| maxHour | maxhour, maxh, mah, 時次 | **時**（0〜23）がこの値**以下**であること。 | MAX_VALUE |
| maxMinute | maxminute, maxmi, mami, 分次 | **分**（0〜59）がこの値**以下**であること。 | MAX\_VALUE |
| maxSecond | maxsecond, maxs, mas, 秒次 | **秒**（0〜59）がこの値**以下**であること。 | MAX\_VALUE |

### 下限
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| minYear | minyear, miny, miy, 年前 | **年**がこの値**以上**であること。 | MIN\_VALUE |
| minMonth | minmonth, minm, mimo, 月前 | **月**（1〜12）がこの値**以上**であること。 | MIN\_VALUE |
| minDay | minday, mind, mid, 日前 | **日**（1〜31）がこの値**以上**であること。 | MIN\_VALUE |
| minHour | minhour, minh, mih, 時前 | **時**（0〜23）がこの値**以上**であること。 | MIN\_VALUE |
| minMinute | minminute, minmi, mimi, 分前 | **分**（0〜59）がこの値**以上**であること。 | MIN\_VALUE |
| minSecond | minsecond, mins, mis, 秒前 | **秒**（0〜59）がこの値**以上**であること。 | MIN\_VALUE |

### その他
| Attribute | Aliases | Description | Default |
| :-- | :-- | :-- | :-- |
| invert | i, 逆転 | trueとfalseの反転を行うか | false |

## Examples
```yml
 Conditions:
 - realtime{minSecond=0;maxSecond=5;minMinute=00;maxMinute=00}
```
> 毎時、5分0秒になったらtrueを返す
```yml
 Conditions:
 - realtime{minSecond=0;maxSecond=00;minMinute=00;maxMinute=00;minHour=15;maxHour=15}
```
> 毎日、15時0分0秒になったらtrueを返す