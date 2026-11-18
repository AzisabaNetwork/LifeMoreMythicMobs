# lmMarkStacks

Aliases: `MarkStacks`

## Description
対象エンティティに付与されている `lmMarkStack` のスタック数が条件式を満たしているかチェックします。

## Attributes
| Attribute | Aliases | Description | Default |
| :--- | :--- | :--- | :--- |
| `id` | `mark`, `m` | マークの識別名 | `mark` |
| `value` | `v`, `stacks`, `s` | 比較式（例: `>=3`, `5`, `2to4`） | `>=1` |

## Examples
```yml
Conditions:
  - lmMarkStacks{id=burn;v=>=3} true
```
