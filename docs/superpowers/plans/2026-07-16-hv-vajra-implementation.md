# HV Vajra Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Register a playable HV Basic Vajra that uses GTNH electric-item APIs, acts as pickaxe/shovel/axe/wrench/wire cutter, and has the approved shaped recipe and GTO texture.

**Architecture:** A pure Java tier/decision layer makes electric boundaries testable without Minecraft. `ItemTieredVajra` owns the 1.7.10 item and IC2 behavior under this mod's namespace, while a focused dynamic recipe transfers charge from the central HV lithium battery. Existing registries remain lifecycle coordinators.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, ForgeGradle 1.2, GregTech 5U 5.09.51.482, IC2 electric item API, JUnit 4.13.2.

## Global Constraints

- Register only `nh_addtings_juzi:hv_vajra` in this phase.
- Display names are `基础金刚杵` and `Basic Vajra`.
- Use the GTOCore `hv_vajra.png` texture copied into this mod.
- Electric tier is HV (`3`), maximum charge is `10,000,000 EU`, transfer limit is `512 EU/t`, and operation cost is `3,333 EU`.
- Powered mining speed is `30.0F`; `getHarvestLevel` always reports `Integer.MAX_VALUE`.
- The item acts as exactly pickaxe, shovel, axe, wrench, and wire cutter.
- Sneak-right-click toggles Silk Touch level 1 on the individual stack.
- The shaped recipe is `PEP / CFC / RUR`: double Black Steel plate, HV emitter, carbon-fiber plate, LV field generator, dense Steel plate, and HV lithium battery.
- The recipe does not consume another Vajra and transfers the central battery's charge into the output, capped at maximum charge.
- Preserve all Flight Charm behavior and registry identity.
- Do not modify or commit `AGENTS.md` or `参考项目/`.

---

### Task 1: Electric Tier And Decision Model

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/item/VajraTier.java`
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/item/VajraLogic.java`
- Create: `src/test/java/com/juzi/nhaddtingsjuzi/item/VajraLogicTest.java`

**Interfaces:**
- Produces: `VajraTier.HV`, getters for tier, maximum charge, transfer limit, operation cost, mining speed, and harvest level.
- Produces: `VajraLogic.hasOperationEnergy(double, int)`, `miningSpeed(double, VajraTier)`, `canHarvest(double, VajraTier)`, and `transferredCharge(double, VajraTier)`.

- [ ] **Step 1: Write failing tier and boundary tests**

Create `VajraLogicTest` covering exact approved values, charge `3332/3333`, speed `0/30`, maximum harvest level, and recipe charge capping:

```java
@Test
public void definesApprovedHvTier() {
    assertEquals(3, VajraTier.HV.getElectricTier());
    assertEquals(10000000, VajraTier.HV.getMaxCharge());
    assertEquals(512.0D, VajraTier.HV.getTransferLimit(), 0.0D);
    assertEquals(3333, VajraTier.HV.getOperationCost());
    assertEquals(30.0F, VajraTier.HV.getMiningSpeed(), 0.0F);
    assertEquals(Integer.MAX_VALUE, VajraTier.HV.getHarvestLevel());
}

@Test
public void requiresOneFullOperationOfCharge() {
    assertFalse(VajraLogic.hasOperationEnergy(3332.0D, 3333));
    assertTrue(VajraLogic.hasOperationEnergy(3333.0D, 3333));
    assertEquals(0.0F, VajraLogic.miningSpeed(3332.0D, VajraTier.HV), 0.0F);
    assertEquals(30.0F, VajraLogic.miningSpeed(3333.0D, VajraTier.HV), 0.0F);
    assertFalse(VajraLogic.canHarvest(3332.0D, VajraTier.HV));
    assertTrue(VajraLogic.canHarvest(3333.0D, VajraTier.HV));
}

@Test
public void capsTransferredBatteryCharge() {
    assertEquals(0.0D, VajraLogic.transferredCharge(-1.0D, VajraTier.HV), 0.0D);
    assertEquals(7500000.0D, VajraLogic.transferredCharge(7500000.0D, VajraTier.HV), 0.0D);
    assertEquals(10000000.0D, VajraLogic.transferredCharge(12000000.0D, VajraTier.HV), 0.0D);
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.VajraLogicTest
```

Expected: `compileTestJava` fails because `VajraTier` and `VajraLogic` do not exist.

- [ ] **Step 3: Implement the minimal pure Java model**

Implement immutable `VajraTier` with `HV = new VajraTier(3, 10000000, 512.0D, 3333, 30.0F, Integer.MAX_VALUE)`. Implement `VajraLogic` with exact threshold and clamping behavior from the tests; reject no inputs and clamp transferred charge to `[0, maxCharge]`.

- [ ] **Step 4: Run focused and existing tests**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.VajraLogicTest
.\gradlew.bat test
```

Expected: three Vajra tests and four existing Flight Charm tests pass.

- [ ] **Step 5: Commit the tested model**

```powershell
git add -- src/main/java/com/juzi/nhaddtingsjuzi/item/VajraTier.java src/main/java/com/juzi/nhaddtingsjuzi/item/VajraLogic.java src/test/java/com/juzi/nhaddtingsjuzi/item/VajraLogicTest.java
git commit -m "test: define HV Vajra electric rules"
```

---

### Task 2: HV Electric Vajra Item

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/item/ItemTieredVajra.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModItems.java`

**Interfaces:**
- Consumes: `VajraTier.HV` and all `VajraLogic` decisions.
- Produces: `ModItems.HV_VAJRA_ID`, `ModItems.hvVajra`, and `ItemTieredVajra#createStackWithCharge(double)`.

- [ ] **Step 1: Implement the item under this mod's namespace**

Create `ItemTieredVajra extends Item implements IElectricItem`. It must:

- use `ElectricItem.manager` for charge checks and `use` calls;
- return the tier's max charge, electric tier, and transfer limit;
- return itself as charged and empty item and return false from `canProvideEnergy`;
- return `Integer.MAX_VALUE` from `getHarvestLevel`;
- return `30.0F` only when charge is at least `3333 EU`;
- recognize pickaxe, shovel, and axe blocks via harvest-tool names and block materials;
- consume `3333 EU` in `onBlockDestroyed` only on the server and only for non-creative players;
- preserve unbreakable blocks by returning no powered mining effect for hardness below zero;
- implement a non-stackable electric durability bar through the IC2 manager;
- toggle only Silk Touch level 1 on sneak-right-click using stack NBT and localized feedback;
- return a charged copy from `createStackWithCharge(double)`.
- expose crafting-container behavior that returns one copied Vajra with `3,333 EU` discharged; if the input cannot pay the cost, do not create a charged duplicate.

Use MCP/deobfuscated 1.7.10 method names so ForgeGradle performs SRG mapping.

- [ ] **Step 2: Register the item and five tool identities**

In `ModItems.register()`, construct `new ItemTieredVajra(HV_VAJRA_ID, VajraTier.HV)`, assign the existing creative tab, and register it with `GameRegistry.registerItem`.

Register one wildcard/representative stack as:

```java
GregTechAPI.registerWrench(stack);
GregTechAPI.registerWireCutter(stack);
OreDictionary.registerOre(ToolDictNames.craftingToolPickaxe.name(), stack);
OreDictionary.registerOre(ToolDictNames.craftingToolShovel.name(), stack);
OreDictionary.registerOre(ToolDictNames.craftingToolAxe.name(), stack);
OreDictionary.registerOre(ToolDictNames.craftingToolWrench.name(), stack);
OreDictionary.registerOre(ToolDictNames.craftingToolWireCutter.name(), stack);
```

Do not register any other `craftingTool*` identity.

- [ ] **Step 3: Compile the production integration**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: successful compilation against GregTech and IC2 APIs. If an API signature differs, inspect the compiled dependency and adjust only the adapter code without changing approved behavior.

- [ ] **Step 4: Run all tests**

Run `\.\gradlew.bat test` and expect seven tests with zero failures.

- [ ] **Step 5: Commit the item integration**

```powershell
git add -- src/main/java/com/juzi/nhaddtingsjuzi/item/ItemTieredVajra.java src/main/java/com/juzi/nhaddtingsjuzi/registry/ModItems.java
git commit -m "feat: register HV Basic Vajra"
```

---

### Task 3: Dynamic Shaped Recipe

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/recipe/RecipeVajra.java`
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/recipe/ShapedVajraRecipe.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModRecipes.java`

**Interfaces:**
- Consumes: `ModItems.hvVajra` and `ItemTieredVajra#createStackWithCharge(double)`.
- Produces: one `IRecipe` matching `PEP / CFC / RUR` and transferring central battery charge.

- [ ] **Step 1: Implement exact ingredient resolution**

`RecipeVajra.register()` resolves:

```java
GTOreDictUnificator.get(OrePrefixes.plateDouble, Materials.BlackSteel, 1)
ItemList.Emitter_HV.get(1)
GTModHandler.getIC2Item("carbonPlate", 1)
ItemList.Field_Generator_LV.get(1)
GTOreDictUnificator.get(OrePrefixes.plateDense, Materials.Steel, 1)
ItemList.Battery_RE_HV_Lithium.get(1)
```

Validate every result with `GTUtility.isStackInvalid`; throw `IllegalStateException` naming the missing ingredient instead of substituting another material.

- [ ] **Step 2: Implement shaped matching and charge transfer**

`ShapedVajraRecipe extends ShapedOreRecipe` with mirroring disabled. Its `getCraftingResult` first uses the superclass match, then locates slot 7 (zero-based center of bottom row), reads charge through `ElectricItem.manager.getCharge`, clamps it with `VajraLogic.transferredCharge`, and returns `hvVajra.createStackWithCharge(...)`.

The recipe must require the exact 3x3 arrangement and reject mirrored/shifted layouts. `getRecipeSize()` returns `9`; `getRecipeOutput()` returns an empty-charge display stack.

- [ ] **Step 3: Register through the existing recipe lifecycle**

Add `GameRegistry.addRecipe(new ShapedVajraRecipe(...))` in `RecipeVajra.register()` and call it from `ModRecipes.register()` after the existing arcane recipe.

- [ ] **Step 4: Compile and run tests**

Run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

Expected: recipe compiles, all seven pure tests remain green, and no existing recipe code changes behavior.

- [ ] **Step 5: Commit the recipe**

```powershell
git add -- src/main/java/com/juzi/nhaddtingsjuzi/recipe/RecipeVajra.java src/main/java/com/juzi/nhaddtingsjuzi/recipe/ShapedVajraRecipe.java src/main/java/com/juzi/nhaddtingsjuzi/registry/ModRecipes.java
git commit -m "feat: add HV Vajra crafting recipe"
```

---

### Task 4: Texture And Localization

**Files:**
- Copy: `参考项目/GTOCore/src/main/resources/assets/gtocore/textures/item/tools/hv_vajra.png` to `src/main/resources/assets/nh_addtings_juzi/textures/items/hv_vajra.png`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/client/ClientEventHandler.java`
- Modify: `src/main/resources/assets/nh_addtings_juzi/lang/zh_CN.lang`
- Modify: `src/main/resources/assets/nh_addtings_juzi/lang/en_US.lang`

**Interfaces:**
- Consumes: `ModItems.HV_VAJRA_ID` and `ModItems.hvVajra`.
- Produces: stitched item icon plus localized name, electric description, five-tool description, and Silk Touch mode messages.

- [ ] **Step 1: Copy the approved GTO bitmap asset**

Copy the PNG byte-for-byte to the mod namespace. Do not edit the reference project asset.

- [ ] **Step 2: Register the icon in the existing item atlas event**

Extend `ClientEventHandler.onTextureStitch` so texture type `1` registers `nh_addtings_juzi:hv_vajra` and assigns it to the HV Vajra item. Keep Flight Charm stitching unchanged.

- [ ] **Step 3: Add English and Chinese localization**

Add keys for:

- `item.hv_vajra.name`;
- `item.hv_vajra.tooltip` describing pickaxe/shovel/axe/wrench/wire cutter;
- `item.hv_vajra.silk.enabled`;
- `item.hv_vajra.silk.disabled`.

Use `基础金刚杵` / `Basic Vajra` and concise localized mode messages.

- [ ] **Step 4: Compile resources and commit**

Run `\.\gradlew.bat processResources compileJava`, then commit only the four planned files.

---

### Task 5: Full Verification And Playtest Artifact

**Files:**
- Verify: `build/libs/NH-AddTings-Juzi-0.1.1b.jar`

**Interfaces:**
- Consumes: all completed HV Vajra code and resources.
- Produces: a reobfuscated JAR ready for the user's GTNH client playtest.

- [ ] **Step 1: Run a clean full build**

Run:

```powershell
.\gradlew.bat clean build
```

Expected: `BUILD SUCCESSFUL`, all seven tests pass, and ForgeGradle `reobf` succeeds.

- [ ] **Step 2: Inspect the generated JAR**

Run `jar tf build\libs\NH-AddTings-Juzi-0.1.1b.jar` and verify entries for:

```text
com/juzi/nhaddtingsjuzi/item/ItemTieredVajra.class
com/juzi/nhaddtingsjuzi/item/VajraTier.class
com/juzi/nhaddtingsjuzi/item/VajraLogic.class
com/juzi/nhaddtingsjuzi/recipe/RecipeVajra.class
com/juzi/nhaddtingsjuzi/recipe/ShapedVajraRecipe.class
assets/nh_addtings_juzi/textures/items/hv_vajra.png
assets/nh_addtings_juzi/lang/zh_CN.lang
assets/nh_addtings_juzi/lang/en_US.lang
```

- [ ] **Step 3: Audit source requirements and worktree scope**

Confirm exact five Ore Dictionary registrations, wrench/wire-cutter GT registrations, recipe ingredients, electric values, speed, and maximum harvest level with `rg`. Run `git diff --check` and `git status --short`; unrelated untracked files must remain untouched.

- [ ] **Step 4: Report the playtest checklist**

Give the user the JAR path and ask them to test creative-tab appearance, HV charging, five block/tool interactions, Silk Touch toggle, charge drain/empty behavior, and the shaped recipe in their actual GTNH client.
