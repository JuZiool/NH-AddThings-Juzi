# Charging Amperage and Vajra Material Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make each charging-station circuit provide 1 A up to 16 A, cap the circuit slot at 16, and let the HV Basic Vajra efficiently mine common breakable materials including cactus.

**Architecture:** Keep amperage arithmetic in `ChargingStationLogic` so it is unit-testable without Minecraft runtime. `MTEChargingStation` derives tier and circuit count from its existing single slot, applies the count to input capacity, buffer size, tick budget, machine injection, and GUI status. Extend the Vajra's existing explicit material whitelist rather than broadening it to unsafe non-block materials.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, GregTech 5 Unofficial, ModularUI 2, JUnit 4, Gradle 4.4.1 wrapper.

## Global Constraints

- Keep the existing charging-station MetaTileEntity ID, inventory layout, and NBT format compatible.
- One valid circuit supplies `1 A`; valid circuit count is clamped to `0..16`.
- The single circuit slot accepts only valid circuit stacks and has a hard stack limit of `16`.
- Circuit tier continues to control voltage, item charging tier, and radius.
- Player charging remains ahead of machine charging within one shared tick budget.
- Do not classify air, liquids, fire, or portals as Vajra-mineable materials.
- Update README behavior text; do not change version or publish without a separate request.

---

### Task 1: Circuit-Scaled Energy Arithmetic

**Files:**
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java`

**Interfaces:**
- Produces: `effectiveAmperage(int circuitCount)`, `tickBudget(ChargingStationTier tier, int circuitCount)`, `bufferCapacity(ChargingStationTier tier, int circuitCount)`.

- [ ] Add failing tests for counts `0`, `1`, `8`, `16`, and values above `16`.
- [ ] Run `gradlew.bat test --tests com.juzi.nhaddtingsjuzi.machine.ChargingStationLogicTest` and confirm failure.
- [ ] Implement count clamping and count-aware budget/capacity.
- [ ] Run the focused test and confirm success.

### Task 2: Machine Slot and Runtime Integration

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java`
- Modify: `src/main/resources/assets/nh_addtings_juzi/lang/zh_CN.lang`
- Modify: `src/main/resources/assets/nh_addtings_juzi/lang/en_US.lang`

**Interfaces:**
- Consumes: Task 1 count-aware logic.
- Produces: runtime amperage derived from `mInventory[0].stackSize`, a slot limit of `16`, and dynamic GUI status.

- [ ] Apply effective amperage to tick budget, maximum input amperage, buffer size, and machine injection cap.
- [ ] Configure the circuit slot to reject invalid circuits and cap its stack size at `16` for GUI and automation paths supported by the MTE API.
- [ ] Show tier, amperage, radius, and total tick budget in status text.
- [ ] Run charging-station tests and compile Java.

### Task 3: Vajra Common Material Coverage

**Files:**
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/item/VajraLogicTest.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/item/VajraLogic.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/item/ItemTieredVajra.java`

**Interfaces:**
- Produces: a testable common-material eligibility input to the existing `isMineableBlock` decision.

- [ ] Add failing tests proving common material eligibility is accepted while unsupported inputs remain rejected.
- [ ] Run the focused Vajra test and confirm failure.
- [ ] Add cactus, glass, sponge, cloth, carpet, coral, ice, packed ice, circuits, redstone-light, TNT, cake, web, piston, and dragon-egg materials to the explicit whitelist.
- [ ] Run the focused test and confirm success.

### Task 4: Documentation and Full Verification

**Files:**
- Modify: `README.md`

**Interfaces:**
- Documents the final player-facing behavior.

- [ ] Replace fixed `16 A` wording with one-circuit-per-ampere behavior and the 16-item slot limit.
- [ ] Mention broader Vajra common-material support.
- [ ] Run `gradlew.bat clean build`.
- [ ] Run `git diff --check` and review the complete diff.
- [ ] Commit with the configured `JuZiool` author identity; push or release only if requested.
