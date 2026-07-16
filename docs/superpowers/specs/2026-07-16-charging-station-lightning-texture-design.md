# Charging Station Lightning Texture Design

## Approved Design

- Keep the Charging Station body visually identical to the GregTech EV machine block, using its top, side, and bottom textures.
- Add a transparent 16x16 pixel-art lightning overlay only on the machine front.
- Use bright yellow as the main fill, white for the upper highlight, and orange for the lower shadow.
- Keep all non-front faces unchanged.
- Use the same overlay while enabled and disabled for now.

## Asset

- Resource: assets/nh_addtings_juzi/textures/blocks/machine/overlay_charging_station.png
- Dimensions: exactly 16x16 pixels.
- Background: fully transparent.
- Filtering: Minecraft nearest-neighbor pixel rendering; no antialiasing.

## Verification

- Unit test confirms the registered machine tier is EV.
- Asset inspection confirms 16x16 RGBA dimensions and transparency.
- A clean Gradle test/build succeeds and the reobfuscated JAR contains the texture.
