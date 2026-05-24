---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.2
状态: 已确定
关联系统: 交换机制, 物品池, 村庄库存规则
---

# Value Table

The value table is the base pricing table. It only decides "what is this item worth at the base level" and does not handle theme modifiers, stock, or market fluctuations.

## File Location

```text
data/<namespace>/data_maps/item/value.json
```

`<namespace>` can be your own namespace; it does not have to be `ruralroutes`.

## File Structure

```json
{
  "replace": true,
  "values": {
    "#examplepack:pool/stone": { "value": 1 },
    "#examplepack:pool/crop": { "value": 2 },
    "minecraft:diamond": { "value": 40 }
  }
}
```

### Root Fields

| Field | Description |
| :--- | :--- |
| `replace` | It is best to write this explicitly. For a complete value table, `true` is usually used. |
| `values` | The value entries. |

## Single Entry

Each entry is very simple:

```json
{ "value": 40 }
```

### Fields

| Field | Description |
| :--- | :--- |
| `value` | The base value, as an integer. |

## How `values` Works

Keys can be written as:

| Form | Example | Description |
| :--- | :--- | :--- |
| Item ID | `minecraft:iron_ingot` | Prices a single item. |
| Tag ID | `#examplepack:pool/stone` | Prices a whole class of items at once. |

## Default Value

Items that do not match fall back to the mod config's default value. This is not a datapack field; it is a fallback value.

## Example

```json
{
  "replace": true,
  "values": {
    "#examplepack:pool/wood": { "value": 2 },
    "#examplepack:pool/food": { "value": 4 },
    "minecraft:golden_carrot": { "value": 40 }
  }
}
```

## Notes

- The value table only provides base values.
- The namespace is configurable.
- When you build a full setup, remember to write `replace` explicitly.

## Related Docs

- [Tags](tags.md)
- [Theme Template](theme-template.md)
- [Trade Profiles](trade-profiles.md)
- [Exchange Mechanism](../../../系统/交换机制.md)

