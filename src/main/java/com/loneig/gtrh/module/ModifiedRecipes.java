package com.loneig.gtrh.module;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import gregtech.api.util.GTRecipe;

public final class ModifiedRecipes {

    private static final Set<GTRecipe> MARKED = Collections.newSetFromMap(new IdentityHashMap<>());

    private ModifiedRecipes() {}

    public static void mark(GTRecipe recipe) {
        if (recipe != null) MARKED.add(recipe);
    }

    public static boolean isModified(GTRecipe recipe) {
        return recipe != null && MARKED.contains(recipe);
    }
}
