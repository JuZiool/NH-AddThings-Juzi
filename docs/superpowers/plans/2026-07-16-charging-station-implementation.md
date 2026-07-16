# Charging Station Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (- [ ]) syntax for tracking.

**Goal:** Build the approved LV-IV circuit-controlled Charging Station as one GregTech 5 single-block machine.

**Architecture:** Keep deterministic tier, budget, radius, cache, and round-robin calculations in pure Java classes covered by JUnit. Isolate optional ServerUtilities and Baubles access behind reflection-safe adapters. Implement the world-facing behavior in an MTEBasicHull-derived MetaTileEntity that uses the GregTech base energy buffer and injectEnergyUnits for local GT targets.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, GregTech 5.09.51.482, IC2 2.2.828, ModularUI, JUnit 4.

## Global Constraints

- One machine named 充电站 / Charging Station with registry identity charging_station.
- Real circuitBasic through circuitElite select LV through IV; higher circuits are unsupported.
- Shared 16A budget; players first, then loaded in-range GT machines.
- Player charging follows the owner's current ServerUtilities team across dimensions.
- Machine range is 16, 32, 64, 128, or 256 blocks by tier.
- At most 128 loaded TileEntities are discovered and 16 cached targets are serviced per tick.
- No chunk loading and no generic IC2 machine supply.
- Existing Flight Charm and Basic Vajra behavior must remain unchanged.

---

### Task 1: Deterministic Charging Rules

**Files:**
- Create: src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationTier.java
- Create: src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java
- Create: src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java

**Interfaces:**
- Produces circuit tier mapping, voltage, radius, 16A budget, buffer capacity, target voltage cap, scan limits, and round-robin cursor calculations.

- [ ] Write failing JUnit tests for all LV-IV values and unsupported circuits.
- [ ] Run the focused test and confirm missing-type failures.
- [ ] Implement the enums and pure functions.
- [ ] Run the focused tests and confirm they pass.

### Task 2: Optional Player Integrations

**Files:**
- Create: src/main/java/com/juzi/nhaddtingsjuzi/compat/TeamResolver.java
- Create: src/main/java/com/juzi/nhaddtingsjuzi/compat/ServerUtilitiesTeamResolver.java
- Create: src/main/java/com/juzi/nhaddtingsjuzi/compat/PlayerElectricInventory.java
- Test: src/test/java/com/juzi/nhaddtingsjuzi/compat/TeamResolverTest.java

**Interfaces:**
- Consumes owner UUID and MinecraftServer.
- Produces online EntityPlayerMP recipients, falling back to owner only.
- Produces inventory, armor, and Baubles ItemStack candidates without hard runtime dependencies.

- [ ] Write adapter-boundary tests for owner fallback and deduplication.
- [ ] Implement reflection-isolated ServerUtilities lookup.
- [ ] Implement inventory and optional Baubles enumeration.
- [ ] Run adapter tests.

### Task 3: GregTech MetaTileEntity

**Files:**
- Create: src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java
- Modify: src/main/java/com/juzi/nhaddtingsjuzi/registry/ModMachines.java
- Modify: src/main/java/com/juzi/nhaddtingsjuzi/NHAddTingsJuzi.java

**Interfaces:**
- Consumes ChargingStationLogic, TeamResolver, IC2 ElectricItem manager, and GT energy interfaces.
- Produces one registered GT MetaTileEntity stack and tick behavior.

- [ ] Add a registration test for the fixed MetaTileEntity ID and identity.
- [ ] Implement one circuit slot and dynamic maxEUInput, maxAmperesIn, and maxEUStore.
- [ ] Implement player-first charging against the real GT energy buffer.
- [ ] Implement incremental loaded-TileEntity discovery with coordinate cache.
- [ ] Implement round-robin local GT injection with voltage capping.
- [ ] Implement NBT persistence for enabled state and cursors.
- [ ] Run focused tests and compileJava.

### Task 4: UI, Localization, And Recipe

**Files:**
- Modify: src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java
- Modify: src/main/java/com/juzi/nhaddtingsjuzi/registry/ModRecipes.java
- Create: src/main/java/com/juzi/nhaddtingsjuzi/recipe/RecipeChargingStation.java
- Modify: src/main/resources/assets/nh_addtings_juzi/lang/zh_CN.lang
- Modify: src/main/resources/assets/nh_addtings_juzi/lang/en_US.lang

**Interfaces:**
- Produces a usable circuit-slot GUI, status text, localized item name, and shaped crafting recipe.

- [ ] Add one circuit slot and synchronized status fields.
- [ ] Add localized machine name and descriptions.
- [ ] Register a progression-appropriate shaped recipe.
- [ ] Compile and inspect generated resources.

### Task 5: Verification And Packaging

**Files:**
- Test all changed sources and final JAR.

- [ ] Run focused charging-station tests.
- [ ] Run ./gradlew clean test build.
- [ ] Run git diff --check.
- [ ] Audit every approved requirement against current source.
- [ ] Confirm the reobfuscated JAR exists in build/libs.
