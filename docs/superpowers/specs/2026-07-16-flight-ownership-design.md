# Flight Ownership and Consumption Design

## Goal

Make the Flight Charm revoke only flight permission that it granted, and charge hunger only while the player is actually flying.

## Scope

This change is limited to `ItemFlightCharm`. It does not add compatibility adapters for individual flight-providing mods or change the existing recipe, item registration, or hunger cost.

## Flight Ownership

- The charm grants flight only when the player does not already have `allowFlying`.
- When it grants flight, it records an ownership flag in the charm's item NBT.
- If the player already has `allowFlying`, the charm does not claim ownership.
- On unequip or insufficient hunger, the charm revokes flight only when its ownership flag is set.
- Creative-mode players retain flight unconditionally. The charm neither claims nor revokes creative flight.
- After revoking its own permission, the charm clears its ownership flag.
- `onPlayerLoad` runs the same grant/check logic as an equipped charm so a player who rejoins with the charm equipped regains the intended state.

Minecraft Forge 1.7.10 has no common registry of flight-permission providers. If another mod grants flight after this charm has already claimed ownership, the charm cannot discover that provider generically. Supporting that edge case would require explicit integration with each provider and is outside this change.

## Hunger Consumption

- The existing cost remains `8.0F` exhaustion per 600 flying ticks.
- The timer advances only when all of the following are true:
  - the charm owns the player's flight permission;
  - the player is currently flying (`isFlying`);
  - the player is not in creative mode;
  - the player has at least the minimum food level.
- When the player stops flying, the timer pauses and retains its value.
- Reaching 600 accumulated flying ticks applies the exhaustion cost and resets the timer to zero.
- Unequipping the charm retains the timer on that item, matching the current item-NBT behavior.
- Creative flight and flight supplied before the charm was equipped are not charged by the charm.

## User-Facing Text

The Shift tooltip changes from a general 30-second wearing cost to: `Every 30 seconds of accumulated flight consumes 1 hunger shank` in meaning. The existing Chinese tooltip remains the primary in-code text and will explicitly say `每累计飞行 30 秒消耗 1 格饱食度`.

## Verification

Focused tests will cover the ownership state decisions independently from Minecraft runtime objects:

- claim flight when the player initially cannot fly;
- do not claim pre-existing or creative flight;
- revoke only owned flight;
- count only actual owned, non-creative flight;
- pause and retain timer progress while grounded;
- charge and reset after 600 eligible ticks.

The final verification is a full `gradlew.bat build`, followed by inspection of the generated reobfuscated JAR.
