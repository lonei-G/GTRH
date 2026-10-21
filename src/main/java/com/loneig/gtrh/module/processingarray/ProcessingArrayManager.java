package com.loneig.gtrh.module.processingarray;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.SoundResource;
import gregtech.api.recipe.RecipeMap;

/**
 * Maps single block machines to the recipe map and activity sound the processing array should use for them.
 */
public final class ProcessingArrayManager {

    private static final HashMap<String, RecipeMap<?>> RECIPE_MAPS = new HashMap<>();
    private static final HashMap<String, SoundResource> MACHINE_SOUNDS = new HashMap<>();

    private ProcessingArrayManager() {}

    public static void addRecipeMapToPA(String machine, RecipeMap<?> recipeMap) {
        RECIPE_MAPS.put(machine, recipeMap);
    }

    public static RecipeMap<?> giveRecipeMap(String machine) {
        return RECIPE_MAPS.get(machine);
    }

    public static void addSoundResourceToPA(String machine, SoundResource sound) {
        MACHINE_SOUNDS.put(machine, sound);
    }

    public static SoundResource getSoundResource(String machine) {
        return MACHINE_SOUNDS.get(machine);
    }

    /**
     * Strips the {@code gt.blockmachines.} prefix and the {@code .tier.xx} suffix from a machine item.
     */
    public static String getMachineName(ItemStack stack) {
        String name = stack == null ? null : stack.getUnlocalizedName();
        if (name == null || name.length() <= 25) {
            return "";
        }
        return name.substring(17, name.length() - 8);
    }
}
