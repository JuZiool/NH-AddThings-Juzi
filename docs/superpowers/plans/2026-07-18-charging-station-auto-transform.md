# 充电站自动降压与目标侧安培换算 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让现有单个充电站根据目标 GregTech 机器的实际输入电压自动降压，并将源端 EU 预算换算为目标侧可请求的安培数，同时禁止升压。

**Architecture:** 保留现有 `MTEChargingStation` 单机器注册和电路等级模型。将“是否允许传输电压”和“剩余 EU 能转换多少目标安培”收敛到 `ChargingStationLogic` 的纯函数中，由 `supplyMachines` 调用；源端仍按 `min(storedEU, stationVoltage × clamp(circuitCount, 0, 16))` 产生全局预算，玩家优先，机器仅消费剩余预算。

**Tech Stack:** Java 8, Minecraft 1.7.10, Forge 10.13.4.1614, GregTech 5.09.51.482, JUnit 4, Gradle Wrapper。

## Global Constraints

- 仍然只注册一个充电站机器，不新增 LV、MV、HV、EV、IV 五种机器。
- 电路等级继续决定充电站自身电压、服务半径、可充电物品等级和源端预算。
- 源端有效安培数为 `clamp(circuitCount, 0, 16)`；该上限只用于计算充电站总 EU 预算。
- 目标输入电压高于充电站当前电压时完全跳过，不调用 `injectEnergyUnits`，不得升压。
- 目标输入电压不高于充电站电压时，目标侧安培数按 `remainingBudget / targetVoltage` 向下取整，不再用 `circuitCount` 限制为 16A。
- 只按 GregTech 实际接受的安培数乘以传输电压扣除 EU；接受 0A 时不扣除。
- 保持玩家优先、目标缓存、区块不强制加载、目标过滤、NBT 和机器注册兼容性。
- 不修改与本功能无关的玩家队伍、电动物品、GUI 布局或配方逻辑。

---

## 文件结构与职责

- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java`
  - 将 `transferVoltage` 改为“安全降压”函数：无效电压或目标高于站电压时返回 `0`。
  - 增加纯函数 `targetAmperage(long stationVoltage, long targetVoltage, long remainingBudget)`，返回目标侧可请求的非负整安培数。
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java:586-623`
  - `supplyMachines` 使用安全降压结果和目标侧安培计算；删除 `Math.min(circuitCount, availableAmperes)` 的错误源端限制。
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`
  - 先添加失败测试，再覆盖等压、降压、禁止升压、预算不足、非整除预算和 EV→HV/MV/LV 换算。
- Modify: `README.md:39-46`
  - 明确源端最多 16A 不等于目标低压侧最多 16A；补充自动降压和目标侧安培换算说明。

---

### Task 1: 为安全降压和目标侧安培计算添加失败测试

**Files:**
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`

**Interfaces:**
- Consumes: 现有 `ChargingStationLogic.transferVoltage(long,long)`。
- Produces: 计划中实现的 `ChargingStationLogic.targetAmperage(long,long,long)` 测试契约。

- [ ] **Step 1: 更新升压测试的预期行为**

将现有测试中的：

```java
assertEquals(512L, ChargingStationLogic.transferVoltage(512L, 2048L));
```

改成：

```java
assertEquals(0L, ChargingStationLogic.transferVoltage(512L, 2048L));
```

并保留 LV/MV/HV 降压和零电压断言：

```java
assertEquals(32L, ChargingStationLogic.transferVoltage(512L, 32L));
assertEquals(128L, ChargingStationLogic.transferVoltage(512L, 128L));
assertEquals(512L, ChargingStationLogic.transferVoltage(512L, 512L));
assertEquals(0L, ChargingStationLogic.transferVoltage(512L, 2048L));
assertEquals(0L, ChargingStationLogic.transferVoltage(512L, 0L));
```

- [ ] **Step 2: 添加目标侧安培换算测试**

在 `ChargingStationLogicTest` 中加入：

```java
@Test
public void convertsRemainingEuIntoLowerVoltageAmperage() {
    assertEquals(16L, ChargingStationLogic.targetAmperage(2048L, 2048L, 32768L));
    assertEquals(64L, ChargingStationLogic.targetAmperage(2048L, 512L, 32768L));
    assertEquals(256L, ChargingStationLogic.targetAmperage(2048L, 128L, 32768L));
    assertEquals(1024L, ChargingStationLogic.targetAmperage(2048L, 32L, 32768L));
}

@Test
public void refusesUpscalingAndHandlesBudgetBoundaries() {
    assertEquals(0L, ChargingStationLogic.targetAmperage(512L, 2048L, 32768L));
    assertEquals(0L, ChargingStationLogic.targetAmperage(512L, 0L, 4096L));
    assertEquals(0L, ChargingStationLogic.targetAmperage(512L, 512L, 511L));
    assertEquals(1L, ChargingStationLogic.targetAmperage(512L, 512L, 512L));
    assertEquals(2L, ChargingStationLogic.targetAmperage(512L, 512L, 1535L));
    assertEquals(0L, ChargingStationLogic.targetAmperage(512L, 512L, 0L));
}
```

- [ ] **Step 3: 运行新增测试，确认测试因接口尚未实现而失败**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.machine.ChargingStationLogicTest
```

Expected: FAIL；`targetAmperage(long,long,long)` 尚不存在，且升压断言仍与当前实现冲突。不要修改生产代码来绕过失败。

- [ ] **Step 4: Commit the failing-test checkpoint**

```powershell
git add src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java
git commit -m "test: define charging station voltage conversion rules"
```

---

### Task 2: 实现纯逻辑的安全降压与目标侧安培换算

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java:55-60`
- Test: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`

**Interfaces:**
- Consumes: `stationVoltage`、`targetVoltage`、`remainingBudget`，均为 `long`。
- Produces: `transferVoltage(long stationVoltage, long targetVoltage)` 返回安全传输电压；`targetAmperage(long stationVoltage, long targetVoltage, long remainingBudget)` 返回目标侧可请求的整安培数。

- [ ] **Step 1: 修改 `transferVoltage` 为禁止升压的最小实现**

替换现有方法为：

```java
public static long transferVoltage(long stationVoltage, long targetVoltage) {
    if (stationVoltage <= 0L || targetVoltage <= 0L || targetVoltage > stationVoltage) {
        return 0L;
    }
    return targetVoltage;
}
```

该方法对等压目标返回目标电压，对低压目标返回目标电压，对高压目标返回 0。

- [ ] **Step 2: 添加目标侧安培纯函数**

在 `transferVoltage` 后加入：

```java
public static long targetAmperage(long stationVoltage,
                                  long targetVoltage,
                                  long remainingBudget) {
    long transferVoltage = transferVoltage(stationVoltage, targetVoltage);
    if (transferVoltage <= 0L || remainingBudget <= 0L) {
        return 0L;
    }
    return remainingBudget / transferVoltage;
}
```

该函数不使用源端电路数量，也不人为限制目标侧安培；整数除法自然向下取整，不能使用的零头 EU 留在本 Tick 预算中。

- [ ] **Step 3: 运行逻辑测试确认通过**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.machine.ChargingStationLogicTest
```

Expected: PASS，包含现有等级、预算、范围和过滤测试，以及新增自动降压测试。

- [ ] **Step 4: Commit pure-logic implementation**

```powershell
git add src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java
git commit -m "feat: add safe voltage down-conversion"
```

---

### Task 3: 接入机器供电流程并移除错误的 16A 目标限制

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java:586-623`

**Interfaces:**
- Consumes: `activeTier.getVoltage()`, 当前 `budget`、`used`，以及目标 `target.getInputVoltage()`。
- Produces: 每个目标调用 `injectEnergyUnits(side, transferVoltage, requestedAmperes)`，请求量不超过剩余 EU 换算值；升压目标不调用注入。

- [ ] **Step 1: 删除不再需要的局部 `circuitCount`**

在 `supplyMachines` 中删除：

```java
int circuitCount = getCircuitCount();
```

因为源端电路数量已经在 `onPostTick` 的 `tickBudget` 中使用，不能再次限制目标侧安培。

- [ ] **Step 2: 使用纯函数计算目标侧请求安培**

将现有代码：

```java
long targetVoltage = target.getInputVoltage();
long voltage = ChargingStationLogic.transferVoltage(
        activeTier.getVoltage(), targetVoltage);
long availableAmperes = voltage <= 0L ? 0L : (budget - used) / voltage;
long amperes = Math.min(circuitCount, availableAmperes);
if (amperes > 0L) {
    long accepted = injectFromAnySide(target, voltage, amperes);
    used += accepted * voltage;
}
```

替换为：

```java
long targetVoltage = target.getInputVoltage();
long voltage = ChargingStationLogic.transferVoltage(
        activeTier.getVoltage(), targetVoltage);
long amperes = ChargingStationLogic.targetAmperage(
        activeTier.getVoltage(), targetVoltage, budget - used);
if (amperes > 0L) {
    long accepted = injectFromAnySide(target, voltage, amperes);
    used += accepted * voltage;
}
```

`transferVoltage` 为 0 时 `targetAmperage` 返回 0，因此不会调用注入；目标输入电压高于站电压时自动跳过。现有目标过滤仍会在输入电压非正数时移除目标。

- [ ] **Step 3: 保持实际接受量扣费保护**

确认 `injectFromAnySide` 返回 GregTech 实际接受的安培数，并保持：

```java
used += accepted * voltage;
```

不要改成请求安培，也不要用 `circuitCount` 参与此处计算。若旧 API 返回异常负值，先不扩大本任务范围；本任务只保持现有 GregTech 返回值契约。

- [ ] **Step 4: 运行完整测试**

Run:

```powershell
.\gradlew.bat test
```

Expected: PASS；所有现有机器逻辑、GUI、UI 状态、配方和资源测试均通过。

- [ ] **Step 5: Commit machine integration**

```powershell
git add src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java
git commit -m "fix: convert charging station power for lower voltage targets"
```

---

### Task 4: 更新用户文档中的供电规则

**Files:**
- Modify: `README.md:39-46`

**Interfaces:**
- Consumes: 已实现的源端预算、自动降压和目标侧安培行为。
- Produces: 用户可读的充电站规则说明，与实际代码一致。

- [ ] **Step 1: 更新 README 规则段落**

在现有电路数量和供电规则后补充以下内容：

```markdown
- 电路数量的 `1–16 A` 是充电站源端电压下的预算；向低电压 GregTech 机器供电时会自动降压，并按相同 EU 功率换算为目标侧安培数。例如 `16 A EV` 最多可转换为 `64 A HV`、`256 A MV` 或 `1,024 A LV`，具体仍受本 Tick 剩余预算和机器实际接受量限制。
- 充电站不会升压；目标机器输入电压高于当前充电站电压等级时跳过，不会注入能源。
```

把原有“限制在目标安全输入电压”的表述改成“只允许不高于充电站电压的安全降压”，避免读者误以为高压目标会被尝试供电。

- [ ] **Step 2: 检查文档与实现一致性**

确认 README 同时表达：玩家优先、机器使用剩余全局预算、实际接受安培扣费、源端最多 16A 和目标低压侧可超过 16A 不矛盾。

- [ ] **Step 3: Commit documentation**

```powershell
git add README.md
git commit -m "docs: document charging station down-conversion"
```

---

### Task 5: 回归验证与最终检查

**Files:**
- Verify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogic.java`
- Verify: `src/main/java/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.java`
- Verify: `src/test/java/com/juzi/nhaddtingsjuzi/machine/ChargingStationLogicTest.java`
- Verify: `README.md`

- [ ] **Step 1: 运行完整测试套件**

Run:

```powershell
.\gradlew.bat clean test
```

Expected: BUILD SUCCESSFUL，全部测试通过。

- [ ] **Step 2: 运行完整构建**

Run:

```powershell
.\gradlew.bat clean build
```

Expected: BUILD SUCCESSFUL，并生成 `build/libs/NH-AddTings-Juzi-0.1.5a.jar`。

- [ ] **Step 3: 检查最终差异与状态**

Run:

```powershell
git diff HEAD~4..HEAD --check
git status --short
git log -4 --oneline
```

Expected: diff check 无输出；工作区只保留用户原有的未跟踪 `release-0.1.5a-check.jar`，不删除或覆盖该文件；最近提交包含逻辑测试、纯函数、机器接入和 README 更新。

- [ ] **Step 4: 进行代码级验收**

确认以下数据流成立：

```text
EV 电路 × 16
→ sourceBudget = 2048 × 16 = 32768 EU/t
→ 玩家先消费预算
→ 剩余预算 / 目标 inputVoltage
→ 目标侧请求安培
→ GregTech 返回实际接受安培
→ accepted × targetVoltage 扣除 EU
```

确认高压目标的 `targetVoltage > stationVoltage` 时 `transferVoltage == 0`、`targetAmperage == 0`，且 `injectFromAnySide` 不会被调用。

- [ ] **Step 5: Commit verification notes only if needed**

不要为了验证结果创建空提交。若测试或构建失败，保留失败证据，修复后重新运行对应命令；不要声称通过。

---

## Spec Coverage Self-Review

- 单一机器注册：Task 3 不触碰注册，Task 4 明确文档，覆盖完成。
- 源端电路和 16A 预算：现有逻辑不改，Task 1/2 保留回归测试，覆盖完成。
- 自动识别目标电压：Task 3 从 `target.getInputVoltage()` 读取，覆盖完成。
- 禁止升压：Task 1 修改预期、Task 2 实现返回 0、Task 3 不注入，覆盖完成。
- 降压后目标侧安培换算：Task 1/2 覆盖 EV→HV/MV/LV，Task 3 接入，覆盖完成。
- 实际接受量扣费：Task 3 保留 `accepted * voltage`，覆盖现有行为；没有虚构未存在的集成测试。
- 玩家优先和预算共享：Task 3 不改变 `onPostTick` 数据流，Task 5 验收，覆盖完成。
- 缓存、区块和 NBT 兼容：Task 3 只改供电计算，Task 5 代码级检查，覆盖完成。
- 文档同步：Task 4，覆盖完成。

## Plan Self-Review

- 未包含 `TBD`、`TODO`、`implement later` 或“写测试但不提供内容”等占位步骤。
- 方法签名在各任务间一致：`transferVoltage(long,long)` 与 `targetAmperage(long,long,long)`。
- 测试中的 `transferVoltage(512L, 2048L) == 0L` 与禁止升压规格一致。
- 未改变 `isEligibleMachineTarget` 的输入电压过滤；目标电压为 0 或负数继续在缓存维护阶段被移除。
- 计划只覆盖一个独立子系统：充电站机器供电电压转换；没有混入无关重构。
