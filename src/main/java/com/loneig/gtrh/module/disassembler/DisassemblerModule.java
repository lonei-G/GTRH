package com.loneig.gtrh.module.disassembler;

import net.minecraft.item.ItemStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;
import com.loneig.gtrh.util.GTRHTooltips;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.implementations.MTEBasicMachineWithRecipe;
import gregtech.api.util.GTModHandler;

public final class DisassemblerModule {

    private static final int FIRST_TIER = 1;
    private static final int LAST_T1_TIER = 5;
    private static final int LAST_TIER = 12;

    private DisassemblerModule() {}

    public static void init() {
        if (!Config.moduleEnabled("disassembler")) return;

        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            int id = mteId(tier);

            if (GregTechAPI.METATILEENTITIES[id] != null) {
                GTRH.LOG.error("Disassembler tier {}: MTE id {} is already in use, skipped", tier, id);
                continue;
            }

            ItemStack stack = MTEDisassembler.create(id, tier);
            GTRHTooltips.addMachineTooltip(stack);
            GTModHandler.addMachineCraftingRecipe(stack, craftingRecipe(), tier);
        }
    }

    public static void loadComplete() {
        if (!Config.moduleEnabled("disassembler")) return;

        DisassemblerHelper.loadAssemblerRecipesToDisassembler();
        ReversedRecipeRegistry.registerAllReversedRecipes();
    }

    private static int mteId(int tier) {
        if (tier <= LAST_T1_TIER) {
            return Config.t1MteId + tier - 1;
        }
        return Config.t6MteId + tier - LAST_T1_TIER - 1;
    }

    private static Object[] craftingRecipe() {
        return new Object[] { "ACA", "WHW", "ACA", 'A', MTEBasicMachineWithRecipe.X.ROBOT_ARM, 'C',
            MTEBasicMachineWithRecipe.X.CIRCUIT, 'W', MTEBasicMachineWithRecipe.X.WIRE, 'H',
            MTEBasicMachineWithRecipe.X.HULL };
    }
}
