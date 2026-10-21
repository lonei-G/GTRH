package com.loneig.gtrh.module.disassembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;

import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;

public final class ReversedRecipeRegistry {

    public interface GTCraftingRecipe {

        Object[] getInputs();

        ItemStack getOutput();

        Optional<GTRecipe> toReversed();

        default GTRecipe toReversedSafe() {
            try {
                return toReversed().orElse(null);
            } catch (RuntimeException e) {
                GTRH.LOG.debug("Skipping crafting recipe that cannot be reversed", e);
                return null;
            }
        }
    }

    private static final List<GTCraftingRecipe> REGISTRY = new ArrayList<>(2048);

    private ReversedRecipeRegistry() {}

    public static void registerShaped(ItemStack output, Object[] recipe) {
        if (output == null || recipe == null) return;
        if (!Config.moduleEnabled("disassembler")) return;
        REGISTRY.add(new Shaped(recipe.clone(), output));
    }

    public static void registerShapeless(ItemStack output, Object[] recipe) {
        if (output == null || recipe == null) return;
        if (!Config.moduleEnabled("disassembler")) return;
        REGISTRY.add(new Shapeless(recipe.clone(), output));
    }

    public static void registerAllReversedRecipes() {
        GTRH.LOG.info("Reversing " + REGISTRY.size() + " GT crafting recipes");
        for (GTCraftingRecipe recipe : REGISTRY) {
            DisassemblerHelper.handleGTCraftingRecipe(recipe);
        }
        REGISTRY.clear();
    }

    private static final class Shaped implements GTCraftingRecipe {

        private final Object[] inputs;
        private final ItemStack output;

        private Shaped(Object[] inputs, ItemStack output) {
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public Object[] getInputs() {
            return inputs;
        }

        @Override
        public ItemStack getOutput() {
            return output;
        }

        @Override
        public Optional<GTRecipe> toReversed() {
            return GTUtility.reverseShapedRecipe(output, inputs);
        }
    }

    private static final class Shapeless implements GTCraftingRecipe {

        private final Object[] inputs;
        private final ItemStack output;

        private Shapeless(Object[] inputs, ItemStack output) {
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public Object[] getInputs() {
            return inputs;
        }

        @Override
        public ItemStack getOutput() {
            return output;
        }

        @Override
        public Optional<GTRecipe> toReversed() {
            return GTUtility.reverseShapelessRecipe(output, inputs);
        }
    }
}
