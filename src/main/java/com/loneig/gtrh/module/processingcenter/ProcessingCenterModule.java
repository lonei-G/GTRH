package com.loneig.gtrh.module.processingcenter;

import net.minecraft.item.ItemStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;
import com.loneig.gtrh.util.GTRHTooltips;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gtPlusPlus.core.block.ModBlocks;

public final class ProcessingCenterModule {

    private ProcessingCenterModule() {}

    public static void postInit() {
        if (!Config.moduleEnabled("processing_center")) return;

        int id = Config.processingCenterMteId;
        if (GregTechAPI.METATILEENTITIES[id] != null) {
            GTRH.LOG.error("Processing center: MTE id {} is already in use, skipped", id);
            return;
        }

        ItemStack stack = new MTEProcessingCenter(id, "multimachine.processingcenter", "Processing Center")
            .getStackForm(1);
        GTRHTooltips.addMachineTooltip(stack);

        if (ModBlocks.blockCasings3Misc == null) {
            GTRH.LOG.error("Processing center: Multi-Use Casing is not available, skipped");
            return;
        }

        GTModHandler.addMachineCraftingRecipe(
            stack,
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.BUFFERED
                | GTModHandler.RecipeBits.NOT_REMOVABLE
                | GTModHandler.RecipeBits.REVERSIBLE,
            new Object[] { "ABA", "CDC", "ABA", 'A', OrePrefixes.circuit.get(Materials.IV), 'B', ItemList.Robot_Arm_EV,
                'C', new ItemStack(ModBlocks.blockCasings3Misc, 1, 2), 'D', ItemList.Hull_EV },
            4);
        GTRH.LOG.info("Processing center registered with MTE id {}", id);
    }
}
