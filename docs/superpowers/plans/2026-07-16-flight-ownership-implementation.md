# Flight Ownership and Consumption Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Flight Charm revoke only flight that it granted and consume hunger after each 600 ticks of actual, owned flight.

**Architecture:** Add a package-private pure Java `FlightCharmLogic` decision helper so ownership and timer rules can be tested without constructing Minecraft entities. `ItemFlightCharm` remains responsible for reading item NBT and applying decisions to `EntityPlayer`; item NBT stores both the ownership marker and accumulated flying ticks.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, ForgeGradle 1.2, Gradle 4.4.1, Baubles Expanded, JUnit 4.13.2.

## Global Constraints

- Keep the existing hunger cost exactly `8.0F` exhaustion per 600 eligible flying ticks.
- Pause and retain timer progress whenever the player is not actually flying.
- Never claim, revoke, or charge creative-mode flight.
- Do not add compatibility adapters for individual third-party flight providers.
- Preserve Minecraft 1.7.10 and Java 8 compatibility.
- Do not modify the recipe, item registration, or unrelated untracked files.

## File Structure

- Create `src/main/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.java`: pure ownership and timer decisions with no Minecraft dependencies.
- Create `src/test/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogicTest.java`: regression coverage for ownership, eligibility, pause, and cost-boundary behavior.
- Modify `src/main/java/com/juzi/nhaddtingsjuzi/item/ItemFlightCharm.java`: persist ownership, apply grant/release decisions, count actual flight, and update tooltip text.
- Modify `build.gradle`: add JUnit to the test-only classpath.

---

### Task 1: Flight Ownership Decision Model

**Files:**
- Create: `src/test/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogicTest.java`
- Create: `src/main/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.java`
- Modify: `build.gradle:33-50`

**Interfaces:**
- Produces: `FlightCharmLogic.shouldClaimFlight(boolean ownsFlight, boolean allowFlying, boolean creative, boolean hasEnoughFood)`.
- Produces: `FlightCharmLogic.shouldRestoreOwnedFlight(boolean ownsFlight, boolean allowFlying, boolean creative, boolean hasEnoughFood)`.
- Produces: `FlightCharmLogic.shouldReleaseOwnedFlight(boolean ownsFlight, boolean creative)`.

- [ ] **Step 1: Add the test dependency and write failing ownership tests**

Add this dependency inside the existing `dependencies` block:

```groovy
testCompile 'junit:junit:4.13.2'
```

Create `FlightCharmLogicTest.java` with the ownership cases:

```java
package com.juzi.nhaddtingsjuzi.item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlightCharmLogicTest {

    @Test
    public void claimsOnlyUnavailableSurvivalFlightWithEnoughFood() {
        assertTrue(FlightCharmLogic.shouldClaimFlight(false, false, false, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, true, false, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, false, true, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, false, false, false));
        assertFalse(FlightCharmLogic.shouldClaimFlight(true, false, false, true));
    }

    @Test
    public void restoresPreviouslyOwnedFlightAfterPlayerLoad() {
        assertTrue(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(false, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, true, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, false, false));
    }

    @Test
    public void releasesOnlyOwnedNonCreativeFlight() {
        assertTrue(FlightCharmLogic.shouldReleaseOwnedFlight(true, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(false, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(true, true));
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
```

Expected: `compileTestJava` fails because `FlightCharmLogic` does not exist. This is the intended RED failure.

- [ ] **Step 3: Implement the minimal ownership decisions**

Create `FlightCharmLogic.java`:

```java
package com.juzi.nhaddtingsjuzi.item;

final class FlightCharmLogic {

    private FlightCharmLogic() {}

    static boolean shouldClaimFlight(boolean ownsFlight, boolean allowFlying,
                                     boolean creative, boolean hasEnoughFood) {
        return !ownsFlight && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldRestoreOwnedFlight(boolean ownsFlight, boolean allowFlying,
                                            boolean creative, boolean hasEnoughFood) {
        return ownsFlight && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldReleaseOwnedFlight(boolean ownsFlight, boolean creative) {
        return ownsFlight && !creative;
    }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
```

Expected: three tests pass with no failures.

- [ ] **Step 5: Commit the tested ownership model**

```powershell
git add -- build.gradle src/main/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.java src/test/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogicTest.java
git commit -m "test: define flight charm ownership rules"
```

---

### Task 2: Actual-Flight Timer Model

**Files:**
- Modify: `src/test/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogicTest.java`
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.java`

**Interfaces:**
- Consumes: ownership decisions from Task 1.
- Produces: `FlightCharmLogic.shouldCountFlight(boolean ownsFlight, boolean isFlying, boolean creative, boolean hasEnoughFood)`.
- Produces: `FlightCharmLogic.shouldChargeOnNextTick(int timer, int interval)`.
- Produces: `FlightCharmLogic.nextTimer(int timer, int interval)`.

- [ ] **Step 1: Write failing timer eligibility and boundary tests**

Append these methods to `FlightCharmLogicTest`:

```java
@Test
public void countsOnlyActualOwnedSurvivalFlightWithEnoughFood() {
    assertTrue(FlightCharmLogic.shouldCountFlight(true, true, false, true));
    assertFalse(FlightCharmLogic.shouldCountFlight(false, true, false, true));
    assertFalse(FlightCharmLogic.shouldCountFlight(true, false, false, true));
    assertFalse(FlightCharmLogic.shouldCountFlight(true, true, true, true));
    assertFalse(FlightCharmLogic.shouldCountFlight(true, true, false, false));
}

@Test
public void chargesAndResetsOnTheSixHundredthEligibleTick() {
    assertFalse(FlightCharmLogic.shouldChargeOnNextTick(598, 600));
    assertTrue(FlightCharmLogic.shouldChargeOnNextTick(599, 600));
    org.junit.Assert.assertEquals(599, FlightCharmLogic.nextTimer(598, 600));
    org.junit.Assert.assertEquals(0, FlightCharmLogic.nextTimer(599, 600));
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
```

Expected: `compileTestJava` fails because the three timer methods are missing.

- [ ] **Step 3: Implement the minimal timer decisions**

Add these methods to `FlightCharmLogic`:

```java
static boolean shouldCountFlight(boolean ownsFlight, boolean isFlying,
                                 boolean creative, boolean hasEnoughFood) {
    return ownsFlight && isFlying && !creative && hasEnoughFood;
}

static boolean shouldChargeOnNextTick(int timer, int interval) {
    return timer + 1 >= interval;
}

static int nextTimer(int timer, int interval) {
    return shouldChargeOnNextTick(timer, interval) ? 0 : timer + 1;
}
```

- [ ] **Step 4: Run all model tests and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
```

Expected: five tests pass with no failures. Grounded state is represented by `shouldCountFlight(...) == false`, so the caller leaves the stored timer unchanged rather than calling `nextTimer`.

- [ ] **Step 5: Commit the tested timer model**

```powershell
git add -- src/main/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.java src/test/java/com/juzi/nhaddtingsjuzi/item/FlightCharmLogicTest.java
git commit -m "test: define actual flight consumption timing"
```

---

### Task 3: Integrate Ownership and Actual-Flight Charging

**Files:**
- Modify: `src/main/java/com/juzi/nhaddtingsjuzi/item/ItemFlightCharm.java:21-155`

**Interfaces:**
- Consumes: all `FlightCharmLogic` methods from Tasks 1 and 2.
- Persists: item NBT boolean `ownsFlightPermission` and integer `flyTimer`.

- [ ] **Step 1: Add the ownership NBT key and replace direct permission mutation**

Add the key beside `TAG_FLY_TIMER`:

```java
private static final String TAG_OWNS_FLIGHT = "ownsFlightPermission";
```

Replace `onWornTick`, `onEquipped`, `onUnequipped`, and `onPlayerLoad` with calls to focused helpers:

```java
@Override
public void onWornTick(ItemStack stack, EntityLivingBase player) {
    if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;

    EntityPlayer ep = (EntityPlayer) player;
    updateFlightPermission(stack, ep);
    tickFlightConsumption(stack, ep);
}

@Override
public void onEquipped(ItemStack stack, EntityLivingBase player) {
    if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
    updateFlightPermission(stack, (EntityPlayer) player);
}

@Override
public void onUnequipped(ItemStack stack, EntityLivingBase player) {
    if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
    releaseOwnedFlight(stack, (EntityPlayer) player);
}

@Override
public void onPlayerLoad(ItemStack stack, EntityLivingBase player) {
    if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
    updateFlightPermission(stack, (EntityPlayer) player);
}
```

Add the NBT and permission helpers:

```java
private void updateFlightPermission(ItemStack stack, EntityPlayer ep) {
    NBTTagCompound tag = getOrCreateTag(stack);
    boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);
    boolean creative = ep.capabilities.isCreativeMode;
    boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

    if (creative) {
        tag.setBoolean(TAG_OWNS_FLIGHT, false);
        return;
    }

    if (!hasEnoughFood) {
        releaseOwnedFlight(stack, ep);
        return;
    }

    if (FlightCharmLogic.shouldClaimFlight(
            ownsFlight, ep.capabilities.allowFlying, false, true)) {
        tag.setBoolean(TAG_OWNS_FLIGHT, true);
        ep.capabilities.allowFlying = true;
        ep.sendPlayerAbilities();
    } else if (FlightCharmLogic.shouldRestoreOwnedFlight(
            ownsFlight, ep.capabilities.allowFlying, false, true)) {
        ep.capabilities.allowFlying = true;
        ep.sendPlayerAbilities();
    }
}

private void releaseOwnedFlight(ItemStack stack, EntityPlayer ep) {
    NBTTagCompound tag = getOrCreateTag(stack);
    boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);

    if (FlightCharmLogic.shouldReleaseOwnedFlight(
            ownsFlight, ep.capabilities.isCreativeMode)) {
        ep.capabilities.allowFlying = false;
        ep.capabilities.isFlying = false;
        ep.sendPlayerAbilities();
    }
    tag.setBoolean(TAG_OWNS_FLIGHT, false);
}

private NBTTagCompound getOrCreateTag(ItemStack stack) {
    if (!stack.hasTagCompound()) {
        stack.setTagCompound(new NBTTagCompound());
    }
    return stack.getTagCompound();
}
```

- [ ] **Step 2: Replace worn-time consumption with eligible-flight consumption**

Replace `tickFoodConsumption` with:

```java
private void tickFlightConsumption(ItemStack stack, EntityPlayer ep) {
    NBTTagCompound tag = getOrCreateTag(stack);
    boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);
    boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

    if (!FlightCharmLogic.shouldCountFlight(
            ownsFlight, ep.capabilities.isFlying,
            ep.capabilities.isCreativeMode, hasEnoughFood)) {
        return;
    }

    int timer = tag.getInteger(TAG_FLY_TIMER);
    if (FlightCharmLogic.shouldChargeOnNextTick(timer, FOOD_COST_INTERVAL)) {
        ep.getFoodStats().addExhaustion(EXHAUSTION_COST);
    }
    tag.setInteger(TAG_FLY_TIMER,
            FlightCharmLogic.nextTimer(timer, FOOD_COST_INTERVAL));
}
```

This early return is the pause behavior: the stored timer is untouched while the player is grounded or otherwise ineligible.

- [ ] **Step 3: Update the tooltip to describe accumulated flight time**

Replace:

```java
list.add(EnumChatFormatting.GRAY + "每 30 秒消耗 1 格饱食度");
```

with:

```java
list.add(EnumChatFormatting.GRAY + "每累计飞行 30 秒消耗 1 格饱食度");
```

- [ ] **Step 4: Run model tests and compile the integrated mod**

Run:

```powershell
.\gradlew.bat test --tests com.juzi.nhaddtingsjuzi.item.FlightCharmLogicTest
.\gradlew.bat compileJava
```

Expected: all five model tests pass and `compileJava` exits successfully.

- [ ] **Step 5: Commit the integration**

```powershell
git add -- src/main/java/com/juzi/nhaddtingsjuzi/item/ItemFlightCharm.java
git commit -m "fix: preserve external flight permissions"
```

---

### Task 4: Full Build and Artifact Verification

**Files:**
- Verify: `build/libs/NH-AddTings-Juzi-0.0.9b.jar`

**Interfaces:**
- Consumes: completed production code and tests from Tasks 1-3.
- Produces: a reobfuscated Minecraft 1.7.10 mod JAR containing `FlightCharmLogic.class` and the updated `ItemFlightCharm.class`.

- [ ] **Step 1: Run a clean full build**

Run:

```powershell
.\gradlew.bat clean build
```

Expected: `BUILD SUCCESSFUL`; JUnit reports no failed tests; ForgeGradle completes `reobf`.

- [ ] **Step 2: Inspect the generated JAR**

Run:

```powershell
jar tf build\libs\NH-AddTings-Juzi-0.0.9b.jar
```

Expected entries include:

```text
com/juzi/nhaddtingsjuzi/item/FlightCharmLogic.class
com/juzi/nhaddtingsjuzi/item/ItemFlightCharm.class
mcmod.info
assets/nh_addtings_juzi/textures/items/flight_charm.png
```

- [ ] **Step 3: Check the final diff and worktree boundaries**

Run:

```powershell
git status --short
git diff HEAD~3 --check
git diff HEAD~3 --stat
```

Expected: no whitespace errors. Existing unrelated untracked files remain untouched; only the planned source, test, build, and documentation files appear in the feature history.

- [ ] **Step 4: Report the verified behavior and residual platform limitation**

Report the passing test count, successful full build, generated JAR path, and the Forge 1.7.10 limitation that a provider granting flight after the charm claims it cannot be detected generically.
