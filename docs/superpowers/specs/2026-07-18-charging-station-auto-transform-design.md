# 充电站自动降压与目标侧安培换算设计

日期：2026-07-18
状态：已获用户确认，待规格文件审阅

## 背景

充电站使用一个专用电路槽，通过电路种类决定自身工作电压等级，通过电路堆叠数量决定源端安培预算。当前供给 GregTech 机器时，代码在计算剩余 EU 可转换出的目标侧安培后，又使用原始 `circuitCount` 将目标安培限制为最多 16A。这会错误地限制降压后的输出能力：例如 16A EV（32,768 EU/t）供给 HV 机器时，理论上应为 64A HV，但当前实现只能请求 16A HV。

## 目标

- 保持一个充电站机器注册，不新增 LV、MV、HV、EV、IV 五种机器。
- 保持电路等级决定充电站自身电压、可服务半径、可充电物品等级和源端预算的现有规则。
- 根据目标 GregTech 机器的实际 `getInputVoltage()` 自动选择安全传输电压。
- 充电站只允许降压，不允许升压：目标输入电压高于充电站当前电压时完全跳过。
- 降压后允许目标侧安培数按剩余 EU 预算增加，不再错误继承源端最多 16A 限制。
- 继续按照 GregTech 实际接受的安培数扣除充电站储能，绝不凭理论请求量扣除未接受的 EU。
- 保持玩家优先、机器其次、目标缓存、区块不强制加载、目标过滤和存档兼容行为不变。

## 电路与源端预算

仍由一个充电站实体承载所有等级，电路种类决定当前等级：

| 电路 | 等级 | 充电站电压 | 服务半径 |
| --- | --- | ---: | ---: |
| `circuitBasic` | LV | 32 EU/t | 16 格 |
| `circuitGood` | MV | 128 EU/t | 32 格 |
| `circuitAdvanced` | HV | 512 EU/t | 64 格 |
| `circuitData` | EV | 2,048 EU/t | 128 格 |
| `circuitElite` | IV | 8,192 EU/t | 256 格 |

有效源端安培数仍为：

```text
sourceAmperage = clamp(circuitCount, 0, 16)
sourceBudget = stationVoltage × sourceAmperage
```

源端 16A 上限只用于计算充电站本 Tick 的总 EU 预算，不限制降压后目标侧的安培数。

所有目标共享同一预算：

1. 先给所有者及其在线队伍成员的合格电动物品充电。
2. 机器阶段使用玩家充电后的剩余预算。
3. 每个目标消耗的 EU 不能超过剩余预算。

## 自动识别与降压流程

机器供给阶段逐个处理缓存目标，并在实际注入前重新验证目标。

### 目标电压

读取目标 GregTech 机器的 `getInputVoltage()`，作为目标输入电压。只接受大于 0 的电压；非标准但有效的 GregTech 输入电压也按返回的实际值计算，不额外四舍五入到某个标准等级。

### 升压禁止

若：

```text
targetVoltage > stationVoltage
```

则移除或跳过该目标，不调用 `injectEnergyUnits`。这意味着：

- LV 充电站只供 LV 目标；
- MV 充电站可供 MV、LV 目标；
- HV 充电站可供 HV、MV、LV 目标；
- EV 充电站可供 EV、HV、MV、LV 目标；
- IV 充电站可供 IV、EV、HV、MV、LV 目标。

### 降压与目标侧安培

目标电压不高于充电站电压时：

```text
transferVoltage = targetVoltage
availableAmperage = remainingBudget / transferVoltage
```

请求的目标侧安培数由剩余预算决定，不再执行 `min(circuitCount, availableAmperage)`。例如：

```text
16A EV = 16 × 2,048 = 32,768 EU/t

EV 目标：32,768 / 2,048 = 16A EV
HV 目标：32,768 / 512   = 64A HV
MV 目标：32,768 / 128   = 256A MV
LV 目标：32,768 / 32    = 1,024A LV
```

实际注入仍交给 GregTech：

```java
acceptedAmperes = target.injectEnergyUnits(side, transferVoltage, requestedAmperes)
```

目标实际接受的安培可能少于请求值；实际扣除量为：

```text
usedEU = acceptedAmperes × transferVoltage
```

若目标接受 0A，则不扣除 EU。

## 目标处理与异常行为

以下既有规则保持不变：

- 只供给 GregTech 机器，不供给普通 IC2 机器。
- 排除充电站自身和其他充电站。
- 目标必须在当前维度、当前等级服务半径内、区块已加载，并具有有效输入面。
- 缓存只存坐标，不持有 TileEntity 强引用。
- 目标在供给时即时复核；失效、卸载、越界或不再接受能源的目标从缓存移除。
- 不强制加载区块。
- 玩家充电消耗预算后，机器只能使用剩余预算。
- 所有供给完成后，只从充电站储能扣除实际接受量。

当剩余预算小于目标电压时，`availableAmperage` 为 0，本 Tick 跳过该目标，不产生扣除。

## 代码边界

主要修改点：

- `MTEChargingStation.supplyMachines`：使用目标实际输入电压进行升压检查和目标侧安培计算，移除源端电路数量对目标侧安培的错误上限。
- `ChargingStationLogic`：增加可独立测试的目标电压/安培计算规则，或复用现有安全传输电压函数并补充不升压判定。
- `ChargingStationLogicTest`：覆盖自动降压、目标侧安培换算、升压目标跳过、预算不足和非整数预算边界。

不修改：

- 机器注册 ID、机器数量、方块类型和电路槽规则；
- 玩家充电优先级和队伍解析；
- 目标发现缓存结构；
- NBT 字段和旧存档格式。

## 测试与验收

纯逻辑测试至少覆盖：

- EV → EV：16A EV；
- EV → HV：64A HV；
- EV → MV：256A MV；
- EV → LV：1,024A LV；
- HV → EV：跳过；
- 目标输入电压为 0 或负数：跳过；
- 剩余预算不足一个目标电压：0A；
- 预算为非整除值：按整数安培向下取整，剩余不足一个安培的 EU 不使用；
- 实际接受安培低于请求值：只按实际接受量计算消耗；
- 玩家先消耗预算后：机器使用剩余预算，不能超支。

回归验收：

- 现有电路等级映射、源端 16A 钳制和缓冲容量测试继续通过；
- 目标过滤、范围、缓存和 UI 测试继续通过；
- `clean build` 与全部现有测试通过；
- 不新增充电站机器注册。
