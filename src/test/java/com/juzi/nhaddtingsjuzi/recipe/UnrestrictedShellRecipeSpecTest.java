package com.juzi.nhaddtingsjuzi.recipe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnrestrictedShellRecipeSpecTest {

    @Test
    public void matchesTheHvAssemblerRecipe() {
        assertEquals(1, UnrestrictedShellRecipeSpec.GLASS_PANES);
        assertEquals(1, UnrestrictedShellRecipeSpec.CERTUS_QUARTZ_PLATES);
        assertEquals(1, UnrestrictedShellRecipeSpec.TITANIUM_PLATES);
        assertEquals(2, UnrestrictedShellRecipeSpec.STAINLESS_STEEL_PLATES);
        assertEquals(1, UnrestrictedShellRecipeSpec.DURATION_TICKS);
        assertEquals(16, UnrestrictedShellRecipeSpec.EUT);
    }
}
