# NH-AddTings-Juzi

一个面向 **Minecraft 1.7.10 / GregTech: New Horizons** 的扩展模组，为整合包补充飞行饰品、HV 电动工具与无线供电设备。

当前版本：`0.1.5a`

## 功能

### 飞行符咒

- 装备在 Baubles Expanded 的符咒栏后获得生存模式飞行能力。
- 飞行期间每 30 秒增加 `8.0` 疲劳度，约等于消耗 1 个鸡腿。
- 饥饿值低于 4 点时停止提供飞行；恢复进食后可重新启用。
- 创造模式玩家不会受到飞行权限回收影响。
- 通过神秘时代奥术工作台合成，需要六种基础要素各 `50` 点。

### HV 基础金刚杵

- 一件 HV 级 IC2 电动工具，可作为镐、铲和斧使用，并能拆除 GregTech 机器方块。
- 支持仙人掌、玻璃、海绵、布料、蛛网、冰等常见可破坏材质。
- 最大储能 `10,000,000 EU`，充电等级 HV，传输限制 `512 EU/t`。
- 挖掘速度 `30`，每破坏一个方块消耗 `3,333 EU`。
- 电量不足一次操作所需能量时无法继续采掘。
- 通过普通有序合成获得，成品会继承配方中 HV 锂电池的电量。

### 无线充电站

- EV 机器外壳等级的 GregTech 单方块机器，通过 GUI 开关控制运行。
- 在专用槽位放入不同等级的 GregTech 电路，可设定输出等级和球形服务半径：

| 电路 | 输出等级 | 电压 | 半径 |
| --- | --- | ---: | ---: |
| 基础电路 | LV | 32 EU/t | 16 格 |
| 良好电路 | MV | 128 EU/t | 32 格 |
| 高级电路 | HV | 512 EU/t | 64 格 |
| 数据电路 | EV | 2,048 EU/t | 128 格 |
| 精英电路 | IV | 8,192 EU/t | 256 格 |

- 电路槽只能放置同种电路，最多堆叠 `16` 个；每个电路提供 `1 A`，因此源端总预算为 `1–16 A`。
- 每 Tick 总供电预算为“当前电压 × 电路数量”，所有玩家电器和机器共享该全局预算。
- 优先为机器所有者及其 ServerUtilities 队伍中在线玩家的电动物品充电，再将剩余预算供给范围内可接收能源的 GregTech 机器。
- 玩家物品按剩余电量比例从低到高充电，且不会给高于当前电路等级的物品充电。
- 每台已缓存且有效的目标机器每 Tick 都会获得一次供电检查机会；机器按当前目标遍历顺序消耗玩家充电后的剩余预算，不做预先平均分配。
- 机器实际接受的安培数由 GregTech 返回值决定，充电站按实际接受安培数和传输电压扣除 EU；目标接受 `0 A` 时不消耗预算。
- 电路数量的 `1–16 A` 是充电站源端电压下的预算；向低电压 GregTech 机器供电时会自动降压，并按相同 EU 功率换算为目标侧安培数。例如 `16 A EV` 最多可转换为 `64 A HV`、`256 A MV` 或 `1,024 A LV`，具体仍受本 Tick 剩余预算和机器实际接受量限制。
- 充电站不会升压；目标机器输入电压高于当前充电站电压等级时跳过，不会注入能源。
- 对机器输出时只允许不高于充电站电压的安全降压，不会向其他充电站、发电机或纯输出设备供电。
- 可向多方块机器的能源仓供电。
- 充电站物品 tooltip 会显示其无线充电用途和等级规则。
- 在充电站界面使用 Shift+左键转移玩家背包物品时，充电站电路槽不会参与转移，避免物品消失。
- 在组装机中使用 `1× EV 机器外壳`、`2× 精英电路`、`16× HV 发射器`、`8× HV 传感器` 和 `1× LV 力场发生器` 合成；耗时 30 秒，功率 `480 EU/t`。

## 运行依赖

本模组面向 GTNH `2.8.4` 环境开发。运行时所需的核心模组包括：

- Minecraft `1.7.10`
- Forge `10.13.4.1614`
- GregTech 5 Unofficial
- IndustrialCraft 2 Experimental
- Baubles Expanded
- Thaumcraft 4
- Thaumic Exploration
- ModularUI 2
- ServerUtilities（无线充电站队伍识别）

建议直接在对应版本的 GTNH 客户端或服务端中使用，不建议将其视为通用 Forge 独立模组安装。

## 安装

1. 从 [Releases](https://github.com/JuZiool/NH-AddThings-Juzi/releases) 下载与当前版本对应的 JAR。
2. 将 JAR 放入 GTNH 实例的 `mods/` 目录。
3. 客户端和服务端均需安装相同版本。
4. 完整重启游戏或服务端。

## 从源码构建

### 环境

- JDK 8
- 项目自带 Gradle Wrapper
- 能访问 GTNH Maven 仓库
- `libs/` 中放置项目依赖的开发版 JAR

### 命令

Windows：

```powershell
.\gradlew.bat clean build
```

Linux / macOS：

```bash
./gradlew clean build
```

构建产物位于：

```text
build/libs/NH-AddTings-Juzi-0.1.5a.jar
```

运行单元测试：

```powershell
.\gradlew.bat test
```

## 项目结构

```text
src/main/java/com/juzi/nhaddtingsjuzi/
├─ item/       飞行符咒与 HV 基础金刚杵
├─ machine/    无线充电站及其供电逻辑
├─ recipe/     奥术、工作台与组装机配方
├─ registry/   物品、机器和配方注册
├─ compat/     玩家电动物品与队伍兼容层
└─ client/     客户端事件与 NEI 集成
```

## 许可证与第三方内容

本仓库包含 Minecraft Forge 开发环境附带的许可证与致谢文件。Minecraft、Forge、GregTech、GTNH 及其他依赖模组的名称和资源归各自权利人所有。

项目作者：`juzi`

