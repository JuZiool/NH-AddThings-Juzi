# Flight Control and Consumption Design

## Goal

Make the Flight Charm directly control survival flight while equipped and charge hunger only while the player is actually flying.

## Scope

This change is limited to `ItemFlightCharm`. It does not track or preserve flight permission granted by other mods, and it does not change the existing recipe, item registration, or hunger cost.

## Flight Control

- An equipped survival player with enough food receives `allowFlying`.
- On unequip or insufficient hunger, the charm directly clears `allowFlying` and `isFlying` for survival players.
- The charm does not track which mod granted flight. Other flight sources are not preserved when the charm disables survival flight.
- Creative-mode players retain flight unconditionally. The charm neither disables creative flight nor charges hunger for it.
- `onPlayerLoad` runs the same grant/check logic as an equipped charm so a player who rejoins with the charm equipped regains flight.

No UUID or flight-ownership marker is stored. The only item NBT used by this behavior is the accumulated flying timer.

## Hunger Consumption

- The existing cost remains `8.0F` exhaustion per 600 flying ticks.
- The timer advances only when all of the following are true:
  - the player is currently flying (`isFlying`);
  - the player is not in creative mode;
  - the player has at least the minimum food level.
- When the player stops flying, the timer pauses and retains its value.
- Reaching 600 accumulated flying ticks applies the exhaustion cost and resets the timer to zero.
- Unequipping the charm retains the timer on that item, matching the current item-NBT behavior.
- Creative flight is not charged by the charm.

## User-Facing Text

The Shift tooltip changes from a general 30-second wearing cost to: `Every 30 seconds of accumulated flight consumes 1 hunger shank` in meaning. The existing Chinese tooltip remains the primary in-code text and will explicitly say `每累计飞行 30 秒消耗 1 格饱食度`.

## Verification

Focused tests will cover the flight-control decisions independently from Minecraft runtime objects:

- enable flight for eligible survival players;
- disable flight on unequip or insufficient food for survival players;
- preserve creative flight;
- count only actual, non-creative flight;
- pause and retain timer progress while grounded;
- charge and reset after 600 eligible ticks.

The final verification is a full `gradlew.bat build`, followed by inspection of the generated reobfuscated JAR.
