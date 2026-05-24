---
创建日期: 2026-05-24
最后更新: 2026-05-24
版本: v1.4
状态: 已确定
关联系统: 村庄资源身份, 村庄结构注入实现
---

# Theme Structure

A theme structure is the actual building template that appears in villages. It has a different job from the theme template:

- The theme template decides what the village is.
- The theme structure decides what the village looks like.

In this mod, a theme structure usually means a set of biome-organized NBT structure files plus one matching template pool JSON.

## Core Blocks

A valid theme structure is recommended to include three core block types:

- Trading post: required, because it carries the theme configuration and acts as the interaction entry.
- Rumor board: optional.
- Display case: optional.

## Theme Setup

The theme template is not generated randomly. The theme structure is.

After placing the trading post, right-click it with the mod's configuration tool, choose the matching theme in the UI, and confirm it. **After you confirm, do not right-click the trading post again**, or the template save will write block entity data together with the structure and cause later node data to become wrong.

The rumor board and display case can be placed next to the trading post. As long as they are within 16 blocks, the first time the player right-clicks them they will be detected and configured automatically.

## File Location

```text
data/ruralroutes/structure/village/<biome>/<theme-name>.nbt
data/ruralroutes/worldgen/template_pool/village/<biome>/trade_stations.json
```

`<namespace>` must be `ruralroutes`.

### 1. Create the Structure File First

Export the building as NBT and place it in the matching biome folder.

```text
data/ruralroutes/structure/village/plains/plains_granary.nbt
data/ruralroutes/structure/village/plains/plains_pasture.nbt
```

### 2. Then Write the Template Pool

List those structures in the biome's `trade_stations.json`:

```json
{
  "name": "ruralroutes:village/plains/trade_stations",
  "fallback": "minecraft:village/plains/terminators",
  "elements": [
    {
      "weight": 1,
      "element": {
        "element_type": "minecraft:single_pool_element",
        "location": "ruralroutes:village/plains/plains_granary",
        "processors": { "processors": [] },
        "projection": "rigid"
      }
    },
    {
      "weight": 1,
      "element": {
        "element_type": "minecraft:single_pool_element",
        "location": "ruralroutes:village/plains/plains_pasture",
        "processors": { "processors": [] },
        "projection": "rigid"
      }
    }
  ]
}
```

### `trade_stations.json` Structure

| Field | Description |
| :--- | :--- |
| `elements` | The list of theme structures that may be selected. Each object corresponds to one `.nbt` structure. |

Each object inside `elements` contains:

| Field | Description |
| :--- | :--- |
| `weight` | The selection weight of the structure. Higher values mean a higher chance of being chosen. |
| `element` | The structure element definition. |

## Jigsaw Blocks

Because this uses the vanilla jigsaw structure generation logic, the theme structure needs a jigsaw block placed on the edge and set correctly in order to generate properly.
If you are not sure how to set it up, the images below may help.

![Theme structure jigsaw block setup](E:/MinecraftMod/ruralroutes/docs/图片/主题结构拼图方块设置.png)

![Theme structure placement](E:/MinecraftMod/ruralroutes/docs/图片/主题结构拼图结构放置.png)

## Related Docs

- [Theme Template](theme-template.md)
- [Village Resource Identity](../../../系统/村庄资源身份.md)

