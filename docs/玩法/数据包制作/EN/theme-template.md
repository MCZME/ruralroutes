---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.4
状态: 已确定
关联系统: 村庄资源身份, 村庄库存规则, 交换机制, 标签, 交易内容
---

# Theme Template

A theme template defines the skeleton of a village identity. It explains:

- Which biome this village belongs to.
- Roughly how the base stock is distributed.
- Which trade profiles this village should use.

A theme template does not write the full buy/sell table directly. The actual buy/sell combination is provided by trade profiles.

## File Location

```text
data/<namespace>/ruralroutes/themes/<theme-name>.json
```

`<namespace>` can be customized; it does not have to be `ruralroutes`.

## File Structure

```json
{
  "name": "examplepack:plains_granary",
  "biome": "minecraft:plains",
  "stock": {
    "default": { "min": 10, "max": 18 },
    "targets": {
      "#examplepack:candidate/biome/plains/crop": { "min": 20, "max": 30 },
      "@plains_granary/staples": {
        "sell": { "min": 18, "max": 28 },
        "buy": { "min": 8, "max": 12 }
      },
      "minecraft:golden_carrot": { "min": 1, "max": 3 }
    }
  },
  "price_modifiers": [
    {
      "target": "#examplepack:pool/crop",
      "sell": 0.82,
      "buy": 0.76
    },
    {
      "target": "@plains_granary/staples",
      "sell": 0.92,
      "buy": 1.08
    }
  ],
  "trade_profiles": [
    "examplepack:plains_granary_basic"
  ]
}
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `name` | Yes | Theme ID. It is recommended to write it as `namespace:theme-name`. |
| `biome` | Yes | The biome ID. |
| `stock` | Yes | Theme-level stock settings. Must include `default`. |
| `price_modifiers` | No | Theme-level price modifier list. |
| `trade_profiles` | No | The trade content list that should be merged into this theme. |

## Stock

```json
{
  "default": { "min": 10, "max": 18 },
  "targets": {
    "#examplepack:candidate/biome/plains/crop": { "min": 20, "max": 30 },
    "@plains_granary/staples": {
      "sell": { "min": 18, "max": 28 },
      "buy": { "min": 8, "max": 12 }
    },
    "minecraft:golden_carrot": { "min": 1, "max": 3 }
  }
}
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `default` | Yes | Default stock range. |
| `targets` | No | Batch stock settings by target key. See [Target Reference](target-reference.md). |

The values inside `targets` can be written directly as `{ "min": 10, "max": 18 }`, or as an object that controls sell and buy ranges separately.
`specific` has been removed. Single items are also written directly into `targets`.

## Price Modifiers

```json
[
  {
    "target": "#examplepack:pool/crop",
    "sell": 0.82,
    "buy": 0.76
  }
]
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `target` | Yes | Target reference. See [Target Reference](target-reference.md). |
| `sell` | Yes | The modifier applied when the village sells to the player. |
| `buy` | Yes | The modifier applied when the village buys from the player. |

## Trade Profiles

This is a string array used to reference trade profile files.

```json
"trade_profiles": ["examplepack:plains_granary_basic"]
```

### Purpose

- Separate theme identity from trade content.
- Let multiple themes reuse the same trade content.
- Let modpacks change trade combinations without changing theme identity.
- Merge in written order; keeping a fixed order is recommended.

## How to Use It

### 1. Define the identity first

The theme should answer these questions first:

- Which biome does this village belong to?
- What resource direction does it favor?
- What stock scale should it have?
- Which items should be cheaper or more expensive here?

### 2. Attach trade content next

The theme itself does not write the full buy/sell table. It only references trade profiles.

### 3. Keep the theme stable

Once a theme ID starts being referenced by villages, text, progress, and market rules, it is best not to rename it often.

## Recommended Use in This Mod

- The theme layer should only offset the system, not become a second shop system.
- Set the baseline with `stock.default` first, then use `targets` for local adjustments.
- `stock` should describe the theme skeleton only; do not stuff every item into it.
- Prefer tags for `price_modifiers`; use exact items only for signature goods.
- `trade_profiles` is for composition, not classification.

## Related Docs

- [Tags](tags.md)
- [Trade Profiles](trade-profiles.md)
- [Village Resource Identity](../../../系统/村庄资源身份.md)
- [Village Stock Rules](../../../系统/村庄库存规则.md)
- [Exchange Mechanism](../../../系统/交换机制.md)

