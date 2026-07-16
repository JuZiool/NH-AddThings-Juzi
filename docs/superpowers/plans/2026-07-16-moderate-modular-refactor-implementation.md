# Moderate Modular Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split item registration, recipe registration, and client texture events out of the Forge entry point while preserving all Flight Charm behavior.

**Architecture:** `NHAddTingsJuzi` remains the lifecycle coordinator and creative-tab owner. New focused classes own item registration (`ModItems`), recipe dispatch (`ModRecipes`), and client-only texture stitching (`ClientEventHandler`); the existing item, pure flight logic, and Thaumcraft recipe implementation remain behaviorally unchanged.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, ForgeGradle 1.2, Gradle 4.4.1, Baubles Expanded, Thaumcraft 4, JUnit 4.

## Global Constraints

- Preserve registry identity `nh_addtings_juzi:flight_charm`.
- Preserve mod ID, name, and version: `nh_addtings_juzi`, `NH-AddTings-Juzi`, `0.1.0b`.
- Preserve creative tab behavior, Flight Charm gameplay, tooltip, item NBT, hunger thresholds, timer, and exhaustion cost.
- Preserve the existing Thaumcraft recipe pattern, ingredients, research key, and six aspects at 50 each.
- Register client-only texture code only when `FMLPreInitializationEvent#getSide()` is `CLIENT`.
- Do not create empty machine, AE2, network, config, or persistence modules.
- Delete the unused `proxy_card.png`; do not modify `AGENTS.md` or `参考项目/`.

## File Structure

- Create `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModItems.java`: owns item singleton and `GameRegistry` registration.
- Create `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModRecipes.java`: lifecycle-facing recipe dispatcher.
- Create `src/main/java/com/juzi/nhaddtingsjuzi/client/ClientEventHandler.java`: client-only item texture stitch subscriber.
- Modify `src/main/java/com/juzi/nhaddtingsjuzi/NHAddTingsJuzi.java`: delegate registration and retain lifecycle coordination.
- Modify `src/main/java/com/juzi/nhaddtingsjuzi/recipe/RecipeArcane.java`: use `ModItems.flightCharm` as the registered result.
- Delete `src/main/resources/assets/nh_addtings_juzi/textures/items/proxy_card.png`.

---

### Task 1: Item and Recipe Registration Modules

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModItems.java`
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/registry/ModRecipes.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/recipe/RecipeArcane.java`

**Interfaces:**
- Produces: `ModItems.FLIGHT_CHARM_ID`, `ModItems.flightCharm`, and `ModItems.register()`.
- Produces: `ModRecipes.register()`.

- [x] **Step 1: Run the existing behavior tests as the green refactor baseline**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
```

Expected: 4 tests pass before structural changes.

- [x] **Step 2: Create the item registry**

Create `ModItems.java`:

```java
package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static final String FLIGHT_CHARM_ID = "flight_charm";
    public static ItemFlightCharm flightCharm;

    private ModItems() {}

    public static void register() {
        flightCharm = new ItemFlightCharm();
        flightCharm.setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        GameRegistry.registerItem(flightCharm, FLIGHT_CHARM_ID);
    }
}
```

- [x] **Step 3: Create the recipe dispatcher and update the recipe result**

Create `ModRecipes.java`:

```java
package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.recipe.RecipeArcane;

public final class ModRecipes {

    private ModRecipes() {}

    public static void register() {
        RecipeArcane.register();
    }
}
```

In `RecipeArcane`, import `ModItems` and replace the string lookup result with:

```java
ItemStack result = new ItemStack(ModItems.flightCharm, 1);
```

- [x] **Step 4: Compile and run tests**

Run:

```powershell
.\gradlew.bat test
```

Expected: compilation succeeds and all 4 tests pass.

---

### Task 2: Client Texture Module and Lifecycle Delegation

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/client/ClientEventHandler.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/NHAddTingsJuzi.java`

**Interfaces:**
- Consumes: `ModItems.flightCharm`, `ModItems.register()`, and `ModRecipes.register()`.
- Produces: `ClientEventHandler.register()` and its `TextureStitchEvent.Pre` subscriber.

- [x] **Step 1: Create the client-only event handler**

Create `ClientEventHandler.java`:

```java
package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;
import com.juzi.nhaddtingsjuzi.registry.ModItems;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) {
            ItemFlightCharm.icon = event.map.registerIcon(
                    NHAddTingsJuzi.MODID + ":" + ModItems.FLIGHT_CHARM_ID);
        }
    }
}
```

- [x] **Step 2: Reduce the entry point to coordination**

Update `NHAddTingsJuzi` so:

- `flightCharm` is removed.
- `tabNHAddTings` becomes `TAB_NH_ADD_TINGS`.
- `getTabIconItem()` returns `ModItems.flightCharm` or a feather fallback.
- `preInit` calls `ModItems.register()` and calls `ClientEventHandler.register()` only when `event.getSide() == Side.CLIENT`.
- `init` calls `ModRecipes.register()`.
- The texture stitch subscriber and its client imports are removed.

- [x] **Step 3: Compile the lifecycle wiring and run tests**

Run:

```powershell
.\gradlew.bat test
```

Expected: all 4 tests pass and common/client classes compile.

---

### Task 3: Resource Cleanup and Release Artifact Verification

**Files:**
- Delete: `src/main/resources/assets/nh_addtings_juzi/textures/items/proxy_card.png`
- Verify: `build/libs/NH-AddTings-Juzi-0.1.0b.jar`

**Interfaces:**
- Consumes: completed modular wiring from Tasks 1-2.
- Produces: a reobfuscated JAR containing the new modules and no proxy-card resource.

- [x] **Step 1: Delete the unused texture**

Delete only:

```text
src/main/resources/assets/nh_addtings_juzi/textures/items/proxy_card.png
```

- [x] **Step 2: Run a clean full build**

Run:

```powershell
.\gradlew.bat clean build
```

Expected: `BUILD SUCCESSFUL`, 4 tests pass, and ForgeGradle completes `reobf`.

- [x] **Step 3: Inspect the JAR**

Run:

```powershell
jar tf build\libs\NH-AddTings-Juzi-0.1.0b.jar
```

Expected entries include:

```text
com/juzi/nhaddtingsjuzi/registry/ModItems.class
com/juzi/nhaddtingsjuzi/registry/ModRecipes.class
com/juzi/nhaddtingsjuzi/client/ClientEventHandler.class
com/juzi/nhaddtingsjuzi/item/ItemFlightCharm.class
```

Expected absent entry:

```text
assets/nh_addtings_juzi/textures/items/proxy_card.png
```

- [x] **Step 4: Check scope and commit**

Run:

```powershell
git diff --check
git status --short
```

Commit only the planned Java changes and implementation plan. The deleted texture was untracked, so its successful deletion is proven by filesystem and JAR absence rather than a Git deletion record.
