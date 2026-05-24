---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.2
状态: 已确定
关联系统: 物品池, 交换机制, 村庄资源身份, 市场波动
---

# Tags

Tags are Minecraft's way of grouping items in a datapack.

Rural Routes reads these tags and uses them to organize value tables, theme trade content, market event targets, and rumor text.

## File Location

```text
data/<namespace>/tags/item/<tag>.json
```

`<namespace>` can be your own namespace; it does not have to be `ruralroutes`.

NeoForge official docs: [Tags](https://docs.neoforged.net/docs/1.21.1/resources/server/tags)

## File Structure

```json
{
  "replace": false,
  "values": [
    "minecraft:stone",
    "minecraft:cobblestone",
    "#minecraft:stairs",
    {
      "id": "othermod:optional_item",
      "required": false
    }
  ]
}
```

### Fields

| Field | Description |
| :--- | :--- |
| `replace` | Whether to replace the existing tag contents. Usually `false`. |
| `values` | The tag entry list. |

## How `values` Works

`values` can contain:

| Form | Example | Description |
| :--- | :--- | :--- |
| Item ID | `minecraft:stone` | Adds a single item. |
| Tag ID | `#minecraft:planks` | References another tag. |
| Mod item ID | `othermod:example_item` | References an item from another mod. |
| Optional entry | `{ "id": "othermod:example_item", "required": false }` | Does not error if the target does not exist. |

## Recommended Use in This Mod

Tags mainly do three things in Rural Routes:

- Provide batch pricing targets for value tables.
- Provide candidate item sources for trade content.
- Provide impact targets for market events.

It is best to split tags into two usage groups.

### System Tags

```text
#<namespace>:pool/<category>
```

These tags mean "what kind of resource is this" and are commonly used by value tables, theme price modifiers, market events, and rumors.

Examples:

```text
#examplepack:pool/stone
#examplepack:pool/wood
#examplepack:pool/food
```

### Candidate Tags

```text
#<namespace>:candidate/biome/<biome>/<category>
#<namespace>:candidate/theme/<theme>/<category>
```

These tags mean "which items are more likely to appear in a certain kind of village" and are commonly used by trade content.

Examples:

```text
#examplepack:candidate/biome/plains/crop
#examplepack:candidate/theme/plains_workshop/tool_goods
```

## Examples

### Value Table

The value table can price a whole tag at once:

```json
{
  "replace": true,
  "values": {
    "#examplepack:pool/wood": { "value": 2 }
  }
}
```

### Trade Content

Trade content can use a tag as a candidate source:

```json
{
  "items": [
    "#examplepack:candidate/biome/plains/crop",
    "minecraft:bread"
  ],
  "pick": 2
}
```

### Market Events

Market events can use tags as impact targets. The syntax is the same as the other target references; see [Target Reference](target-reference.md):

```json
{
  "target_ref": "#examplepack:pool/food"
}
```

## Example

```json
{
  "replace": false,
  "values": [
    "minecraft:oak_log",
    "minecraft:spruce_log",
    "minecraft:acacia_log",
    "minecraft:charcoal"
  ]
}
```

This tag can be referenced by value tables, trade content, and market events.

## Notes

- A tag is not always an "item pool"; it is simply Minecraft's grouping mechanism.
- The namespace is configurable.
- When you want full coverage, double-check whether `replace` should be `true`.
- Do not create circular tag references or the datapack will fail to load.

## Related Docs

- [Value Table](value-table.md)
- [Theme Template](theme-template.md)
- [Trade Profiles](trade-profiles.md)
- [Market Event Rules](market-event-rules.md)

