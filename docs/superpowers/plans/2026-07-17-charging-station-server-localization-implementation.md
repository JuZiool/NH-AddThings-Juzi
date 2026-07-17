# Charging Station Server Localization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Charging Station status panel render from server-synchronized raw values using each client's local language.

**Architecture:** Add a small locale-independent `ChargingStationUiState` value object. `MTEChargingStation` synchronizes its raw fields with ModularUI typed `FakeSyncWidget` instances and uses an unsynced dynamic text supplier to localize on the client. Existing server-side `getInfoData()` remains localized and unchanged in behavior.

**Tech Stack:** Java 8, Minecraft 1.7.10, Forge 10.13.4.1614, GregTech 5, ModularUI 1.2.20, JUnit 4.13.2, Gradle wrapper.

## Global Constraints

- Keep the existing `nh_addtings_juzi.charging_station.*` keys.
- Do not change charging behavior, recipes, persistence, registration, tooltips, or server language.
- Synchronize raw state values only; never synchronize the already-localized status string.
- Preserve compact GUI line ordering, positions, alignment, and inventory binding behavior.
- Use test-first changes and run focused tests before implementation changes.

---

### Task 1: Locale-Independent UI State

**Files:**
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationUiState.java`
- Create: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationUiStateTest.java`

**Interfaces:**
- Provides raw getters for enabled, tier, amperage, output EU/t, stored EU, capacity, owner, player count, target count, and radius.
- Provides `localizedLines(Localizer, Function<Long, String>)` using the existing charging-station translation keys.

- [ ] **Step 1: Write the failing tests.** Assert all raw getters preserve distinct values. Use a recording `Localizer` to assert rendering requests the existing translation keys and contains numeric arguments without embedding English literals.
- [ ] **Step 2: Run `./gradlew.bat test --tests com.juzi.nhaddtingsjuzi.machine.ChargingStationUiStateTest`; expect compilation failure because the class does not exist.**
- [ ] **Step 3: Implement the final-field state object, `empty()`, getters, `Localizer`, and `localizedLines(...)` in the existing GUI line order.**
- [ ] **Step 4: Re-run the focused test and verify it passes.**
- [ ] **Step 5: Commit with `git commit -m "test: define charging station localized UI state"`.**

### Task 2: Synchronize Raw State and Localize on Client

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java`
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationGuiTest.java`

**Interfaces:**
- Consumes `ChargingStationUiState` from Task 1.
- Produces an unsynced dynamic status text plus typed syncers for all ten raw fields.

- [ ] **Step 1: Add a failing GUI regression test** that reflects `DynamicTextWidget.syncsToClient` and expects `false` for `createStatusTextWidget(...)`; also assert the class references `FakeSyncWidget`.
- [ ] **Step 2: Run `./gradlew.bat test --tests com.juzi.nhaddtingsjuzi.machine.ChargingStationGuiTest`; expect failure because current code calls `setSynced(true)` and has no raw-state syncers.**
- [ ] **Step 3: Add a client `ChargingStationUiState.empty()` snapshot; build authoritative state from the existing fields; add `BooleanSyncer`, `StringSyncer`, `IntegerSyncer`, and `LongSyncer` widgets for enabled, tier, amperage, output, stored EU, capacity, owner, players, targets, and radius; render the display snapshot through `StatCollector` locally; keep `getInfoData()` server-localized.**
- [ ] **Step 4: Run focused GUI and state tests and verify they pass.**
- [ ] **Step 5: Commit with `git commit -m "fix: localize charging station status on clients"`.**

### Task 3: Full Verification

**Files:**
- No source changes expected.

- [ ] **Step 1: Run `./gradlew.bat test` and verify exit code `0` with zero failures.**
- [ ] **Step 2: Run `./gradlew.bat clean build` and verify `build/libs/NH-AddTings-Juzi-0.1.4a.jar` exists.**
- [ ] **Step 3: Run `jar tf build/libs/NH-AddTings-Juzi-0.1.4a.jar | Select-String 'assets/nh_addtings_juzi/lang/(zh_CN|en_US)\.lang'` and verify both language entries exist.**
- [ ] **Step 4: Run `git diff --check` and inspect `git status --short`; pre-existing unrelated untracked files must remain untouched.**
