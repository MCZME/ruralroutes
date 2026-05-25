---
创建日期: 2026-05-24
最后更新: 2026-05-25
版本: v1.9
状态: 已确定
关联系统: 标签, 价值表, 村庄资源身份, 市场波动, 贸易周期
---

# Datapack Creation

This folder is the English version of the Rural Routes datapack authoring docs. The Chinese source files live in the sibling `中文` folder.

## Document List

- [Tags](tags.md)
- [Target Reference](target-reference.md)
- [Value Table](value-table.md)
- [Theme Template](theme-template.md)
- [Theme Structure](theme-structure.md)
- [Trade Profiles](trade-profiles.md)
- [Market Event Rules](market-event-rules.md)

## Recommended Workflow

1. Start with [Theme Template](theme-template.md) to lock in the theme identity, stock skeleton, and trade content references.
2. Then build the [Theme Structure](theme-structure.md), handling the edge jigsaw blocks first.
3. When building the structure, right-click the trading post inside the theme structure with the mod's config tool and select the theme from step 1 in the UI to confirm the binding.
4. Fill in the matching [Trade Profiles](trade-profiles.md) and supporting [Value Table](value-table.md); when market changes are needed, add [Tags](tags.md) and [Market Event Rules](market-event-rules.md) as needed.
5. Write the theme structure into the `elements` list of the corresponding biome's `trade_stations.json`.
6. Launch the game and check structure generation, theme binding, the rumor board, and the display case.

## Common Questions

### Why does my custom theme structure not appear in the matching biome village?

The structure template must include an edge jigsaw block, and it has to face outward. See the images in [Theme Structure](theme-structure.md).

### Why does right-clicking the trading post in my generated village structure show a data mismatch?

That happens because the trading post was already right-clicked before the structure template was saved.

Fix: remove that trading post from the structure template, configure it again with the tool, and then save the structure template again.

Temporary workaround: break the trading post, place a new one, and configure it again. This only fixes that one post and does not help for future generated copies, so the first approach is preferred.

### Why does nothing happen when I right-click a trading post?

The trading post must be near a village environment before it can open. If it is too far from a village, right-clicking it may not trigger anything.

### What is the difference between "structure" and "structure template"?

"Structure" means the building generated in-game.

"Structure template" means the building body that gets exported as a `.nbt` file.

For more detail, see Minecraft's structure block documentation.
