# Charging Station Design

## Goal

Add one GregTech 5 single-block machine named **充电站** (Charging Station, registry identity charging_station) that uses a real tiered GT circuit to determine its LV-IV operating tier. While its chunk is loaded, it wirelessly charges the placing player's current ServerUtilities team across dimensions and supplies loaded GregTech machines within a tier-scaled local radius.

## Tier Control

The machine has one non-consuming circuit slot. Recognized ore-dictionary circuits are:

| Circuit | Tier | Voltage | Machine radius |
| --- | --- | ---: | ---: |
| circuitBasic | LV | 32 EU/t | 16 blocks |
| circuitGood | MV | 128 EU/t | 32 blocks |
| circuitAdvanced | HV | 512 EU/t | 64 blocks |
| circuitData | EV | 2048 EU/t | 128 blocks |
| circuitElite | IV | 8192 EU/t | 256 blocks |

No circuit or an unsupported circuit leaves the machine inactive. LuV and higher circuits are intentionally unsupported because later progression uses the large wireless-grid machines.

## Energy Rules

- The active circuit tier defines safe input voltage.
- The charging station accepts and budgets up to 16A at that voltage.
- Its internal buffer stores 20 seconds of full-load energy: voltage times 16A times 400 ticks.
- GregTech overvoltage behavior is preserved.
- Each tick has one shared energy budget of voltage times 16 EU.
- Player equipment is charged first; local machines receive only the remainder.
- Energy is never created: every accepted EU is removed from the station buffer.

## Player Charging

- Placement permanently stores the placing player's UUID.
- Each one-second scan dynamically resolves that player's current ServerUtilities team.
- If the owner has a team, all currently online members are eligible; otherwise only the owner is eligible.
- ServerUtilities absence or unavailable team data safely falls back to the owner only.
- Eligible players may be at any distance and in any dimension.
- Scan the hotbar, main inventory, armor inventory, and Baubles inventory.
- Charge IC2-compatible electric items whose electric tier is no greater than the active circuit tier.
- Sort candidates by remaining charge percentage ascending before spending the shared budget.
- Offline players are never loaded or modified.

## GregTech Machine Supply

- Only GregTech machines are eligible; generic IC2 machines are excluded.
- Targets must be in the station's current dimension, inside the active tier radius, in loaded chunks, and able to accept EU.
- A higher-tier station may safely power lower-tier machines. Transfer voltage is capped to the target's maximum safe input voltage.
- All eligible machines in range are public recipients; ownership and team are not checked.
- Targets are served with a persistent round-robin cursor so later targets cannot starve.
- At most 16 cached targets are serviced per tick.

## Discovery And Cache

- Never scan every coordinate in the radius.
- Incrementally inspect the current world's loaded TileEntity list, checking at most 128 entries per tick.
- Cache only dimension and coordinates, never strong TileEntity references.
- Cached targets are revalidated when serviced; removed, unloaded, out-of-range, or non-receiving targets are discarded.
- Circuit changes rebuild the radius-dependent cache.
- Newly placed machines are discovered on later incremental passes.
- No chunks are force-loaded. Unloading the charging station's own chunk stops all work immediately.

## User Interface

- The GUI exposes the circuit slot, current tier, stored EU, owner, eligible online player count, cached machine count, and current radius.
- The machine has an enabled/disabled control.
- Status text must not spam player chat.

## Compatibility

- Minecraft 1.7.10, Forge 10.13.4.1614, GregTech 5.09.51.482, IC2 experimental 2.2.828.
- ServerUtilities 2.2.2 integration is optional at runtime and isolated behind a compatibility adapter.
- BaublesExpanded/Baubles integration is optional at runtime and isolated behind a compatibility adapter.
- Existing Flight Charm and Basic Vajra behavior remains unchanged.

## Verification

- Unit tests cover circuit-to-tier mapping, radii, 16A budgets, 20-second buffers, player-first allocation, tier restrictions, voltage down-conversion, incremental scan limits, and round-robin cursor behavior.
- Integration-oriented tests cover owner-only fallback and team-member selection through adapter boundaries.
- A clean ./gradlew test build must succeed and produce the reobfuscated JAR.
