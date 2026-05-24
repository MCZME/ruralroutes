---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.0
状态: 已确定
关联系统: 标签, 主题模板, 交易内容, 市场波动, 村庄库存规则
---

# Target Reference

Target references tell a rule who it should affect.

The following fields all use the same target reference syntax:

- `target_ref`
- `target`
- every key inside `stock.targets`

Note: `targets` inside market event `scopes` is a scope target list used for biome or theme IDs. It is not the same thing as the target reference syntax described here.

## Three Common Forms

### Exact Item

```json
"minecraft:bread"
```

This affects only that item.

Good for a few special goods such as blaze rods, ender pearls, and packed ice.

### Tag

```json
"#examplepack:pool/food"
```

This affects every item inside the tag.

Good for a whole class of goods such as food, wood, ore, or dyed items.

### Source Key

```json
"@plains_granary/staples"
```

This affects the entries in trade content that carry the matching `key`.

Source keys come from the trade content's `key` field. Example:

```json
{
  "key": "plains_granary/staples",
  "items": [
    "minecraft:bread",
    "#examplepack:candidate/biome/plains/crop"
  ],
  "pick": 2
}
```

When referencing it, add `@` in front:

```json
"@plains_granary/staples"
```

## Used in Different Fields

### `target_ref`

`target_ref` is a single target reference.

```json
{
  "target_ref": "#examplepack:pool/food"
}
```

### `target`

`target` is also a single target reference.

```json
{
  "target": "@plains_granary/staples",
  "sell": 0.92,
  "buy": 1.08
}
```

### `stock.targets`

`stock.targets` is a set of target references. Here the target is not written as a field value; it is written as the key itself.

```json
{
  "targets": {
    "#examplepack:pool/food": { "min": 20, "max": 30 },
    "@plains_granary/staples": {
      "sell": { "min": 18, "max": 28 },
      "buy": { "min": 8, "max": 12 }
    },
    "minecraft:golden_carrot": { "min": 1, "max": 3 }
  }
}
```

## Exact Items With Components

If you must distinguish different components of the same item, use the object form.

```json
{
  "item": "minecraft:written_book",
  "components": {
    "minecraft:custom_name": "{\"text\":\"Village Ledger\"}"
  }
}
```

This form is only for a single exact item. Do not add `components` to tags or source keys.

In key-based places like `stock.targets`, object forms with components are not recommended. If you need component-based distinction, prefer giving the trade entry a stable `key` in trade content and then reference it with `@source-key`.

## How to Choose

- Use a tag when you are targeting a class of goods.
- Use an exact item when you are targeting one fixed item.
- Use a source key when you are targeting a candidate group or an entry with components.

## Notes

- Source key references must start with `@`.
- Tag references must start with `#`.
- `target_ref`, `target`, and `stock.targets` all mean the same target concept, only the field shape is different.
- Changing a source key affects every inventory, price, and market rule that references it.

## Related Docs

- [Tags](tags.md)
- [Theme Template](theme-template.md)
- [Trade Profiles](trade-profiles.md)
- [Market Event Rules](market-event-rules.md)
- [Market Fluctuations](../../../系统/市场波动.md)
- [Village Stock Rules](../../../系统/村庄库存规则.md)

