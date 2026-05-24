---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v2.6
状态: 已确定
关联系统: 市场波动, 贸易周期, 标签, 主题模板
---

# Market Event Generation Rules

This document defines market event generation rules, not the market events that have already been generated in a cycle.

Each rule is sampled when the trade cycle refreshes, and then expanded into one or more actual `MarketEvent`s according to `scopes`.

It mainly does three things:

- Decide which items or item groups are affected
- Decide which ranges the generated event applies to
- Decide how the rumor board should describe the event

## File Location

```text
data/<namespace>/ruralroutes/market_event_rules/<rule>.json
```

`<namespace>` can be customized; it does not have to be `ruralroutes`.

## What a Generation Rule Looks Like

```json
{
  "id": "ruralroutes:common/food_shortage",
  "name_key": "market.ruralroutes.common.food_shortage",
  "target_ref": "#ruralroutes:pool/food",
  "scopes": [
    {
      "type": "global",
      "selector": "all"
    }
  ],
  "delta": 0.15,
  "stock": {
    "sell": -0.35,
    "buy": 0.10
  },
  "weight": 100,
  "rumor_family": "shortage",
  "rumor_target_key": "rumor.target.food"
}
```

## Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `id` | Yes | Rule ID. It is recommended to write it as `namespace:category/name`. |
| `name_key` | Yes | The localization key for the rule name, mainly used for display and debugging. |
| `target_ref` | Yes | The affected target. See [Target Reference](target-reference.md). |
| `scopes` | Yes | Scope rules. |
| `delta` | Yes | Price change magnitude. Positive values mean price increases, negative values mean price decreases. |
| `stock` | No | Stock modifier. Only changes the stock for this cycle, not the long-term rule. |
| `weight` | No | Sampling weight, default `100`. |
| `rumor_family` | Yes | The rumor semantic type. |
| `rumor_target_key` | No | The target name shown in the rumor. |

## How to Write the Target

`target_ref` directly decides what this rule affects.

The three common forms are:

- `minecraft:bread` for an exact item
- `#ruralroutes:pool/food` for a tag
- `@plains_workshop/basic_tools` for a source key

Prefer tags, especially when the event applies to a class of goods.
If it is only a special item, writing the exact item directly is also fine.

## How `scopes` Works

`scopes` describes where the rule will expand and take effect.

### A Single Scope

```json
[
  {
    "type": "global",
    "selector": "all"
  }
]
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `type` | Yes | One of `global`, `biome`, or `theme`. |
| `selector` | Yes | `all` or `list`. |
| `targets` | Required when `selector=list` | The target list. Write biome or theme IDs here, not the target reference syntax used in [Target Reference](target-reference.md). |

### Three Scope Types

| Value | Description |
| :--- | :--- |
| `global` | Applies globally and generates one global event directly. |
| `biome` | Affects only the specified biomes; `all` expands to all available biomes, and `list` expands only to the listed biomes. |
| `theme` | Affects only the specified themes; `all` expands to all available themes, and `list` expands only to the listed themes. |

### `selector=list` Example

```json
[
  {
    "type": "biome",
    "selector": "list",
    "targets": [
      "minecraft:plains"
    ]
  }
]
```

This means the rule only takes effect in the listed targets and will not automatically expand to all biomes or themes.

## How to Modify Stock

`stock` is optional and is used to modify the stock baseline for the current cycle.

```json
{
  "sell": -0.35,
  "buy": 0.10
}
```

### Field Reference

| Field | Description |
| :--- | :--- |
| `sell` | Modifies the amount of stock available for sale. |
| `buy` | Modifies the buy capacity. |

Both `sell` and `buy` are percentage modifiers.
Positive values increase the amount; negative values decrease it.

## `rumor_family`

`rumor_family` decides which semantic family the rumor belongs to.

- `shortage`: a shortage, meaning the supply is tight
- `surplus`: a surplus, meaning there is a lot of the goods
- `demand`: demand, meaning someone wants it
- `release`: release, meaning supply has loosened or goods have become available

## `rumor_target_key`

Rumors are not shown directly from `target_ref`. They are first generated through a rumor layer.

After the system generates a `MarketEvent`, it builds rumor entries from the event's `target_ref` and `rumor_family`. When the rumor is shown, it first pulls the target name and then applies the matching semantic template.

`rumor_target_key` is the "player-facing target name" used in that step.

If you do not write it, the system falls back automatically:

- Tags become tag-style rumor keys, so `#ruralroutes:pool/food` will prefer the tag name
- Items become item translation keys, so `minecraft:bread` will use the item name

If you want the rumor to feel more natural and more like ordinary speech, write this field manually. For example, a source key like `@plains_workshop/basic_tools` is not something the system can directly translate into a common player term, so writing something like `rumor.target.tool` lets the rumor display more natural words such as "tools", "food", or "lumber".

Concrete example:

```json
{
  "rumor.shortage.biome.1": "People say %s is running short in %s.",
  "rumor.target.tool": "tools"
}
```

If a rule's `rumor_family` is `shortage`, its scope is the desert biome, and its `rumor_target_key` is `rumor.target.tool`, then the rumor generation step will fill the biome name and target name into the template. The final text players see will be something like:

```text
People say the desert is running short on tools.
```

## How to Use It

### 1. Choose the target first

First decide whether the rule is for a whole class of goods or for one special item.

### 2. Then choose the scope

- Use `global` for a world-wide rule
- Use `biome` for a biome-specific rule
- Use `theme` for a theme-specific rule

### 3. Then set the strength

`delta` controls the price fluctuation magnitude, and `stock` controls the stock linkage.

In general, do not make the numbers too aggressive. Market fluctuations work better as temporary windows.

## Examples

### Global Food Shortage

```json
{
  "id": "ruralroutes:common/food_shortage",
  "name_key": "market.ruralroutes.common.food_shortage",
  "target_ref": "#ruralroutes:pool/food",
  "scopes": [
    {
      "type": "global",
      "selector": "all"
    }
  ],
  "delta": 0.15,
  "stock": {
    "sell": -0.35,
    "buy": 0.10
  },
  "weight": 100,
  "rumor_family": "shortage",
  "rumor_target_key": "rumor.target.food"
}
```

### Theme-Specific Tool Demand

```json
{
  "id": "ruralroutes:biome/plains/tool_demand",
  "name_key": "market.ruralroutes.biome.plains.tool_demand",
  "target_ref": "@plains_workshop/basic_tools",
  "scopes": [
    {
      "type": "biome",
      "selector": "list",
      "targets": [
        "minecraft:plains"
      ]
    }
  ],
  "delta": 0.22,
  "stock": {
    "sell": -0.12,
    "buy": 0.35
  },
  "weight": 40,
  "rumor_family": "demand",
  "rumor_target_key": "rumor.target.tool"
}
```

## Notes

- One rule can expand into one or more actual events depending on scope
- `GLOBAL` usually generates one event, while `BIOME` / `THEME` may expand into multiple events based on the target list
- When the same item matches multiple events, the effects stack
- Events with the same `rule_id + target_ref + scope_type + scope_target` are deduplicated
- `stock` only changes stock, not the price itself
- `GLOBAL` does not need `scope_target`
- `weight` only affects sampling probability, not strength

## Related Docs

- [Market Fluctuations](../../../系统/市场波动.md)
- [Trade Cycle](../../../系统/贸易周期.md)
- [Tags](tags.md)
- [Theme Template](theme-template.md)

