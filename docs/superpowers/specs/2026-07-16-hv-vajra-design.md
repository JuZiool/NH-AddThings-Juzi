# HV Vajra Design

## Goal

Add an HV Basic Vajra to NH-AddTings-Juzi as the first playable tier of a future HV/EV/IV Vajra family. The item is inspired by GTLCore's multifunction Vajra while fitting the Minecraft 1.7.10 GTNH tool and electric-item APIs.

## Scope

This phase registers only the HV Vajra. EV and IV Vajras are not registered and receive no recipes or resources yet. The implementation may centralize tier parameters so later tiers can reuse the same item class without changing the HV registry identity.

The HV Vajra acts as exactly these five tools:

- pickaxe;
- shovel;
- axe;
- wrench;
- wire cutter.

It does not act as a hammer, saw, file, screwdriver, crowbar, or other GregTech crafting tool.

## Registration And Presentation

- Registry ID: `nh_addtings_juzi:hv_vajra`.
- Chinese name: `基础金刚杵`.
- English name: `Basic Vajra`.
- Creative tab: the existing NH-AddTings-Juzi tab.
- Texture: copy GTOCore's `hv_vajra.png` into this mod's item texture namespace.
- The item is non-stackable and uses electric charge instead of ordinary durability.

The implementation must keep ownership in this mod's namespace. It must not instantiate GregTech's `ToolVajra` directly because its superclass registers the item in the `gregtech` namespace during construction.

## Electric Properties

- Electric tier: HV.
- Maximum charge: `10,000,000 EU`.
- Transfer limit: `512 EU/t`.
- Block-break cost: `3,333 EU`.
- The item cannot provide energy to other items.
- It remains the same item when empty or charged.
- A newly crafted Vajra inherits the charge stored in the recipe's HV lithium battery, capped at the Vajra's maximum charge.

An operation that requires energy must first verify that at least one operation's cost is available. Insufficient charge disables effective tool operation instead of consuming ordinary durability.

## Mining Behavior

- Harvest level always reports `Integer.MAX_VALUE`, matching GTNH's original Vajra.
- Mining speed is `30.0F` while sufficiently charged.
- The tool recognizes blocks appropriate for pickaxes, shovels, and axes.
- Breaking one block consumes `3,333 EU` on the server.
- Blocks with hardness below zero remain unbreakable.
- Player permissions, ownership checks, and normal Forge block-break cancellation remain authoritative.
- With insufficient charge, `canHarvestBlock` returns false and powered mining speed is zero even though the static harvest-level query remains `Integer.MAX_VALUE`.

The HV tier intentionally keeps the original Vajra's maximum harvest level. Future tiers may increase speed, capacity, or efficiency, but not harvest level.

## Wrench And Wire-Cutter Behavior

The HV Vajra is registered in GregTech's wrench and wire-cutter tool collections so GT machines, covers, cables, and compatible integrations identify it through established GTNH APIs.

It is also registered under these Ore Dictionary crafting-tool names:

- `craftingToolPickaxe`;
- `craftingToolShovel`;
- `craftingToolAxe`;
- `craftingToolWrench`;
- `craftingToolWireCutter`.

Crafting-tool use must return the same Vajra with `3,333 EU` consumed rather than consuming the item. A Vajra without enough charge cannot be used as a crafting tool. No unrequested tool identity is registered.

## Silk Touch Mode

Sneak-right-click toggles Silk Touch mode, following GTNH's original Vajra interaction model.

- The mode is stored on the individual ItemStack in NBT.
- Enabling the mode applies Silk Touch level 1.
- Disabling it removes only the Vajra's Silk Touch mode enchantment.
- The server owns the state change and sends localized player feedback.
- Normal right-click remains available to wrench and wire-cutter interactions where applicable.

## Recipe

Register one shaped 3x3 crafting recipe:

```text
P E P
C F C
R U R
```

- `P`: double Black Steel plate;
- `E`: HV emitter;
- `C`: carbon-fiber plate;
- `F`: LV field generator;
- `R`: dense Steel plate;
- `U`: HV lithium battery.

The recipe independently manufactures the HV Vajra. Future EV and IV recipes will also be independent and will not consume the previous Vajra tier. Recipe ingredients are intentionally isolated in a dedicated recipe class so later balancing changes do not affect item behavior or registry identity.

If an exact ingredient is unavailable under the expected GTNH registry/API entry, registration must fail clearly during development rather than silently substituting an unrelated material.

## Architecture

### `VajraTier`

A small package-private value holder for electric tier, maximum charge, transfer limit, block cost, mining speed, and maximum harvest level. Only the HV constant is instantiated in this phase.

### `VajraLogic`

A package-private pure Java decision helper covering energy eligibility, reported mining speed, harvest level, and charge consumption boundaries. It contains no Minecraft classes and is tested with JUnit.

### `ItemTieredVajra`

The item implementation owns IC2 electric behavior, block mining behavior, five-tool compatibility, Silk Touch mode, tooltip text, and texture lookup. It uses `VajraTier` for parameters and `VajraLogic` for testable decisions.

### Registration

`ModItems` constructs and registers the HV Vajra, adds it to the existing creative tab, and registers its GregTech and Ore Dictionary tool identities. Existing Flight Charm registration remains unchanged.

### Recipe

`RecipeVajra` owns the shaped recipe and any charge-transfer handling. `ModRecipes` invokes it alongside the existing Thaumcraft recipe.

### Client Resources

The existing client texture stitch handler registers the HV Vajra icon in addition to the Flight Charm icon. English and Chinese language files provide the item name and tooltip strings.

## Error Handling And Compatibility

- All gameplay mutations occur on the logical server.
- The item checks available charge before powered operations.
- The implementation relies on the GTNH-provided IC2 and GregTech APIs already present in the project's compile classpath.
- It avoids copying 1.20.1 Registrate, tag, or Mixin code from GTLCore/GTOCore.
- Existing Flight Charm behavior, recipe, texture, and registry ID remain unchanged.
- Registry names are constants and must not be renamed after release.

## Verification

Automated verification covers:

- the HV tier values;
- enough-energy and insufficient-energy boundaries;
- powered mining speed and maximum harvest level;
- exact block-break charge consumption;
- the five allowed crafting-tool identities and absence of extra identities where this can be tested without Minecraft bootstrap;
- existing Flight Charm tests.

Build verification consists of:

1. focused red-green JUnit runs for pure Vajra logic;
2. full `gradlew.bat clean build` with Forge reobfuscation;
3. JAR inspection for the item, tier, logic, recipe, language entries, and texture;
4. source checks for the five tool registrations and the shaped recipe ingredients.

Manual in-game acceptance checks are:

- appears in the mod creative tab with the intended GTO texture;
- charges in an HV-capable charger and displays charge correctly;
- mines pickaxe, shovel, and axe blocks at the intended speed;
- reports maximum harvest capability but cannot break normally unbreakable blocks;
- works as a GregTech wrench and wire cutter;
- participates in recipes as exactly the five intended crafting tools;
- toggles Silk Touch while sneaking;
- consumes EU and becomes ineffective when charge is insufficient;
- crafts from the specified 3x3 recipe.
