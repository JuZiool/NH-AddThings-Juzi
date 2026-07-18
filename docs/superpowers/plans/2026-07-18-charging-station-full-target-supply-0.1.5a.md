# Charging Station Full Target Supply 0.1.5a Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Change the charging station to inspect every cached machine target each tick while preserving the station-wide EU budget, player priority, voltage safety cap, and save compatibility, then build version `0.1.5a` and copy it to the GTNH client.

**Architecture:** Keep target discovery and cached coordinate validation unchanged. Replace the service-cursor/16-target loop with a single forward pass over the current target list; invalid entries are removed in place, valid entries receive one supply check, and remaining EU is consumed in list order. Retain the `serviceCursor` field and its NBT key only as inert legacy save data.

**Tech Stack:** Java 8, Minecraft 1.7.10 ForgeGradle, GregTech APIs, JUnit 4, Gradle 4.4.1.

## Global Constraints

- Work directly on the current `master`; do not create a local feature branch.
- Do not run game or server tests; run the requested Gradle build only.
- Preserve machine IDs, block type, circuit tiers, target filters, coordinate cache, incremental discovery, and no chunk loading/threading.
- Version must be exactly `0.1.5a` in build metadata, mod metadata, documentation, and version assertions.
- Preserve `ChargingStationServiceCursor` NBT read/write compatibility.

---

### Task 1: Replace machine service scheduling

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java`
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`

**Interfaces:**
- Preserve `ChargingStationLogic.tickBudget`, `transferVoltage`, target filtering, and discovery APIs.
- Remove `ChargingStationLogic.serviceLimit`; no production call may impose a 16-target cap.
- `MTEChargingStation.supplyMachines(IGregTechTileEntity base, long budget)` must inspect every target present at the start of its pass, return actual EU used, and keep legacy `serviceCursor` NBT storage inert.

- [ ] Remove `SERVICE_LIMIT` and `serviceLimit()` from `ChargingStationLogic`; remove the obsolete round-robin assertions from `ChargingStationLogicTest` while retaining tests for discovery and budget behavior.
- [ ] Rewrite `supplyMachines` as an index-based pass over `targets`: loop while `index < targets.size()`, remove invalid targets without incrementing the index, validate range/filter, calculate `transferVoltage`, cap requested amperes by remaining budget and circuit count, inject only when requested amperes are positive, add `accepted * voltage`, then increment the index for valid targets.
- [ ] Leave `serviceCursor` declaration plus `ChargingStationServiceCursor` save/load calls intact, but remove all reads/writes that select the next machine and stop using it in `removeTarget` scheduling.
- [ ] Run `git diff --check` and inspect the diff for preserved player charging, target filtering, voltage capping, and actual-accepted-EU accounting.

### Task 2: Set release version and documentation

**Files:**
- Modify: `build.gradle:31`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/NHAddTingsJuzi.java:33`
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/client/NEIConfigTest.java`
- Modify: `README.md`
- Modify: `todo/charging-station-full-target-supply.md`

- [ ] Change every active project version from `0.1.4d` to `0.1.5a`, including the Gradle archive name, mod constant, README, and NEI assertion.
- [ ] Update the TODO baseline to commit `6eccdad release: prepare 0.1.4d`, artifact name to `NH-AddTings-Juzi-0.1.5a.jar`, and client path to the `0.1.5a` file.
- [ ] Mark the implementation/documentation checklist items complete where the code and README now satisfy them, while leaving actual build and client-copy checks for delivery.
- [ ] Update README wireless-station text to explicitly state that every cached target receives a check each tick, targets consume the remaining global budget in traversal order, and actual accepted amperes determine EU deduction; remove any 16-target/round-robin wording.

### Task 3: Build and deliver the client artifact

**Files:**
- Generated: `build/libs/NH-AddTings-Juzi-0.1.5a.jar`
- Copy destination: `D:/Ai工作区/GTNH-mod-plus/GTNH客户端/.minecraft/versions/GT New Horizons 2.8.4/mods/NH-AddTings-Juzi-0.1.5a.jar`

- [ ] Run `./gradlew.bat clean build` from the repository root; accept only a successful process exit and a generated `0.1.5a` JAR.
- [ ] Copy the generated JAR to the client `mods` directory, replacing only the same-version target artifact.
- [ ] Verify source and destination SHA-256 values match and report the exact artifact path and hash; do not claim game/server behavior was tested.
