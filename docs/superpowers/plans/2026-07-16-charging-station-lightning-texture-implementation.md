# Charging Station Lightning Texture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Render the Charging Station as an EV machine block with a custom 16x16 lightning overlay on its front face.

**Architecture:** Use GregTech's EV machine top, side, and bottom icons as the base layers. Register one custom block icon and append it only when the rendered side equals the machine front.

**Tech Stack:** Minecraft Forge 1.7.10, GregTech 5 texture API, 16x16 RGBA PNG.

## Global Constraints

- Preserve all Charging Station behavior and recipes.
- Do not add animation or active-state variants.
- Do not alter non-front faces.

### Task 1: Pixel Asset

- [ ] Generate the approved 16x16 transparent lightning pixel art.
- [ ] Verify exact dimensions, alpha, and palette.

### Task 2: Front Overlay

- [ ] Write a failing EV shell tier test.
- [ ] Set the registered shell tier to EV.
- [ ] Register the custom lightning icon.
- [ ] Return base EV casing plus overlay only for the front face.
- [ ] Run focused tests and a clean build.
