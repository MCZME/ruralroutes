---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.9
状态: 已确定
关联系统: 主题模板, 交换机制, 标签, 村庄库存规则
---

# Trade Profiles

Trade profiles define what a theme actually sells, what it buys, and a few special exchange rules.

They are kept separate from theme templates so that "identity" and "trade mix" stay decoupled.

## File Location

```text
data/<namespace>/ruralroutes/trade_profiles/<profile-name>.json
```

`<namespace>` can be customized; it does not have to be `ruralroutes`.

## File Structure

```json
{
  "name": "examplepack:plains_granary_basic",
  "sell_items": [
    "minecraft:oak_planks",
    {
      "key": "plains_granary/staples",
      "items": [
        "minecraft:bread",
        "#examplepack:candidate/biome/plains/crop"
      ],
      "pick": 2
    }
  ],
  "buy_items": [
    "minecraft:wheat",
    {
      "key": "plains_granary/procurement",
      "items": [
        "minecraft:raw_iron",
        "minecraft:charcoal"
      ],
      "pick": 1
    }
  ],
  "stock": {
    "targets": {
      "@plains_granary/staples": { "min": 18, "max": 32 },
      "#examplepack:candidate/biome/plains/crop": {
        "sell": { "min": 18, "max": 28 },
        "buy": { "min": 8, "max": 12 }
      }
    }
  },
  "trade_contracts": [
    {
      "type": "fixed",
      "inputs": [
        { "item": "minecraft:carrot", "count": 8 }
      ],
      "outputs": [
        { "item": "minecraft:golden_carrot", "count": 1 }
      ]
    }
  ]
}
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `name` | Yes | Trade profile ID. It is recommended to write it as `namespace:profile-name`. |
| `sell_items` | No | Candidate entries the village sells to the player. Can be left empty. |
| `buy_items` | No | Candidate entries the village buys from the player. Can be left empty. |
| `stock` | No | A stock patch for this trade profile. Only `targets` can be written here. See [Target Reference](target-reference.md). |
| `trade_contracts` | No | Additional exchange contracts. Usually only used for a small number of special exchanges. |

## Item Entries

Both `sell_items` and `buy_items` are `ItemReference` lists. Each entry can use one of the following three forms.

### Single-Item Shorthand

```json
"minecraft:oak_planks"
```

This represents one exact item or tag.

### Single-Item Object

```json
{
  "id": "minecraft:bread",
  "key": "plains_granary/bread",
  "components": {
    "minecraft:custom_name": "{\"text\":\"Village Bread\"}"
  }
}
```

This is suitable for exact items with components. `key` can be written at the same time to give the entry a stable source key.

### Candidate Group

```json
{
  "key": "plains_granary/staples",
  "items": [
    "minecraft:bread",
    {
      "id": "minecraft:written_book",
      "components": {
        "minecraft:written_book_content": {
          "title": "Plains Notes",
          "author": "Village Scribe",
          "pages": [
            "{\"text\":\"local trade note\"}"
          ]
        }
      }
    },
    "#examplepack:candidate/biome/plains/crop"
  ],
  "pick": 2
}
```

### Field Reference

| Field | Required | Description |
| :--- | :--- | :--- |
| `id` | Available for single items | One exact item or tag. |
| `items` | Required for candidate groups | Candidate list. Each entry can be an item, a tag, or a single-item object with components. |
| `pick` | No | How many to randomly pick from the candidate list. If omitted, everything is used. |
| `key` | No | Stable source key. It is recommended for candidate groups. |
| `components` | No | Only used for a single exact item. |

`pick` is applied after the candidates are expanded. If there are not enough candidates, it does not error; it simply uses everything.

## Stock Patch

`stock` is only used to add to this trade profile's stock. It does not define the default baseline. The baseline still comes from the theme template.

```json
{
  "targets": {
    "@plains_granary/staples": { "min": 18, "max": 32 },
    "#examplepack:candidate/biome/plains/crop": {
      "sell": { "min": 18, "max": 28 },
      "buy": { "min": 8, "max": 12 }
    }
  }
}
```

### Field Reference

| Field | Description |
| :--- | :--- |
| `targets` | Set stock by target reference. See [Target Reference](target-reference.md). The value can be a shared range or separate `sell` / `buy` ranges. |

Do not write `default` inside `stock`. If you want a default stock for the whole theme, write it in the theme template.

## Exchange Contracts

`trade_contracts` is optional. For ordinary buy/sell trades, `sell_items` and `buy_items` are enough.

### `fixed`

Fixed exchange rule.

```json
{
  "type": "fixed",
  "inputs": [
    { "item": "minecraft:carrot", "count": 8 }
  ],
  "outputs": [
    { "item": "minecraft:golden_carrot", "count": 1 }
  ]
}
```

Field reference:

| Field | Required | Description |
| :--- | :--- | :--- |
| `type` | Yes | Must be written as `fixed`. |
| `inputs` | Yes | The items and counts the player must pay. |
| `outputs` | Yes | The items and counts the player receives. |

### `currency_basket_dynamic`

A dynamic basket contract used to split a price into a set of acceptable items.

It is only for currency combinations and not for generic item assembly or other combination logic.

`accepted_currencies` can be currency tags or specific currency items.
Common forms are:

- `#ruralroutes:currency` for all currency items
- `#ruralroutes:currency_base` for the base currency
- `ruralroutes:copper_coin`, `ruralroutes:iron_coin`, `ruralroutes:gold_coin` for a few specific currency types

```json
{
  "type": "currency_basket_dynamic",
  "side": "sell_to_player",
  "items": [
    "minecraft:bread"
  ],
  "accepted_currencies": [
    "#ruralroutes:currency_base",
    "ruralroutes:iron_coin",
    "ruralroutes:gold_coin"
  ],
  "composition": "largest_first"
}
```

Field reference:

| Field | Required | Description |
| :--- | :--- | :--- |
| `type` | Yes | Must be written as `currency_basket_dynamic`. |
| `side` | Yes | `sell_to_player` or `buy_from_player`. |
| `items` | Yes | The list of items taking part in the currency basket. |
| `accepted_currencies` | Yes | The currencies that may be used in the basket. You can write currency tags or specific currency items. |
| `composition` | Yes | The composition strategy. |

### Composition Strategies

`composition` currently supports three values:

| Value | Description | When to use |
| :--- | :--- | :--- |
| `largest_first` | Split with the largest denomination first, then fill in smaller denominations. | The most common default choice, suitable for most trades. |
| `smallest_only` | Use only the smallest denomination from the available list. | Use when you want quotes to stay in a single low denomination as much as possible. |
| `single` | If only one currency is available, use only that one; if multiple currencies are provided, fall back to `largest_first`. | Use when you only want to force simplicity in single-currency scenarios. |

If you are unsure, choose `largest_first`.

If an item is not matched by any `currency_basket_dynamic`, it falls back to the default currency basket and keeps calculating with the base currency only. In other words, the price is expressed directly in base currency. For example, if an item costs `110`, the default result is `110` copper coins.

## How to Use It

### 1. Write candidates first, then exact items

In most cases, start with tags and candidate groups, then add exact items for special goods.

### 2. Keep selling and buying semantically symmetric

What the village sells and what it buys should both match the theme identity; do not write them independently.

### 3. Use contracts only for special behavior

Fixed contracts and dynamic basket contracts are good for a few key exchanges, but do not turn every trade into a contract.

### 4. Keep `key` stable whenever possible

If a candidate group may be referenced by stock, pricing, or other rules, the `key` should stay stable and not change casually.

## Notes

- `sell_items` and `buy_items` can be left empty, but usually at least one side should exist.
- `stock` should only contain `targets`; do not write `default` here.
- `pick` is random sampling, so omit it if you want a fixed result.
- If the same item appears more than once in a candidate group, keep the order stable.

## Related Docs

- [Theme Template](theme-template.md)
- [Tags](tags.md)
- [Exchange Mechanism](../../../系统/交换机制.md)
- [Village Stock Rules](../../../系统/村庄库存规则.md)

