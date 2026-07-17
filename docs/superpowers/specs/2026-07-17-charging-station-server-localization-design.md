# Charging Station Server Localization Design

## Problem

The Charging Station status panel is translated on the server before ModularUI
sends it to the client. A dedicated server uses its own default language, so a
Chinese client receives the already-rendered English string. Single-player
works because the integrated server and client share the same language state.

## Goal

Make the Charging Station status panel use each connected client's language in
both single-player and dedicated-server games. Preserve all status values and
their existing refresh behavior.

## Scope

- Change only the Charging Station status widget and the data it synchronizes.
- Keep `getInfoData()` localized for GregTech's server-side information hooks.
- Keep the existing machine name, tooltip keys, power-switch tooltip, charging
  behavior, recipes, and persistence unchanged.
- Do not change the server's global language or add a server configuration.

## Design

`MTEChargingStation` will expose a locale-independent UI snapshot containing:

- enabled state
- tier name
- amperage and output EU/t
- stored EU and maximum stored EU
- owner name
- eligible online-player count
- cached target count
- service radius

The status widget will no longer use `DynamicTextWidget.setSynced(true)` for a
pre-rendered string. Instead:

1. The server sends each snapshot field through ModularUI `FakeSyncWidget`
   typed syncers.
2. The client stores the received fields in a client-side snapshot.
3. The client-side dynamic text supplier calls `StatCollector` with the
   existing `nh_addtings_juzi.charging_station.*` keys.
4. The client renders the localized result on every normal widget update.

This keeps numeric and ownership data authoritative on the server while
deferring all human-language rendering to the client. It also avoids encoding
locale-dependent text into network packets.

## Data Flow and Failure Behavior

- Initial GUI synchronization sends all fields, including zero/empty values.
- Later updates send only fields whose values changed, using ModularUI's
  existing change detection.
- Before the first synchronization packet arrives, the client displays a
  harmless empty/default snapshot rather than server-translated text.
- Missing translation keys continue to follow Minecraft's existing
  `StatCollector` fallback behavior.
- Existing server-side `getInfoData()` output remains unchanged for callers
  that expect localized strings on the server.

## Testing

- Add a focused UI-state test proving that the synchronized snapshot contains
  raw values and that rendering is performed through supplied localization
  keys/arguments rather than an English literal.
- Update the GUI widget test to prove the status widget is not configured for
  server-rendered text synchronization.
- Run the complete Gradle test suite and inspect the built jar for both
  language resources.
