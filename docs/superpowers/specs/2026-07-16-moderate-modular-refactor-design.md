# Moderate Modular Refactor Design

## Goal

Restructure the current single-item mod into a small modular foundation that can grow into a broader GTNH addon without changing Flight Charm gameplay.

## Scope

- Delete the unused, untracked `proxy_card.png` resource.
- Keep the Forge 1.7.10 entry point focused on lifecycle coordination.
- Centralize item ownership and registration in `ModItems`.
- Centralize recipe registration in `ModRecipes`.
- Move client-only texture stitching into `ClientEventHandler`.
- Preserve the existing `ItemFlightCharm` and `FlightCharmLogic` separation.
- Do not create empty machine, AE2, networking, configuration, or persistence modules.

## Architecture

### Entry Point

`NHAddTingsJuzi` retains mod metadata constants, the creative tab, and Forge lifecycle handlers. During pre-initialization it delegates item registration to `ModItems` and registers `ClientEventHandler` only on the client. During initialization it delegates all recipes to `ModRecipes`.

### Item Registry

`ModItems` owns the `flightCharm` singleton and its stable registry name. It constructs the item, assigns the creative tab, and registers it through `GameRegistry`. Other modules access the item through `ModItems.flightCharm` rather than a field on the entry point.

### Recipe Registry

`ModRecipes.register()` is the single lifecycle-facing recipe entry. It delegates the existing Thaumcraft recipe to `RecipeArcane.register()`. Future GT, crafting, or integration recipe modules can be added behind this entry without changing the mod class.

### Client Events

`ClientEventHandler` subscribes to `TextureStitchEvent.Pre` and assigns the Flight Charm icon. The class is client-only and is never constructed or registered on a dedicated server.

## Preserved Behavior

- Registry identity remains `nh_addtings_juzi:flight_charm`.
- Creative tab label and icon remain unchanged.
- Flight Charm slot, flight permission, food threshold, actual-flight timer, exhaustion cost, tooltip, and item NBT remain unchanged.
- The Thaumcraft Arcane Worktable recipe and all ingredients/aspects remain unchanged.
- Mod ID, display name, and version remain `nh_addtings_juzi`, `NH-AddTings-Juzi`, and `0.1.0b`.

## Verification

- Existing `FlightCharmLogicTest` remains green.
- Add focused registry-source tests only where behavior can be tested without bootstrapping Minecraft; otherwise compilation and JAR inspection verify wiring.
- Run `gradlew.bat clean build` and confirm Forge reobfuscation succeeds.
- Inspect the output JAR for the new registry/client classes and verify `proxy_card.png` is absent.
