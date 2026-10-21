package com.loneig.gtrh.module.disassembler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import gregtech.api.enums.SoundResource;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.metatileentity.implementations.MTEBasicMachineWithRecipe;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;

public class MTEDisassembler extends MTEBasicMachineWithRecipe {

    public static final RecipeMap<RecipeMapBackend> RECIPES = RecipeMapBuilder.of("gtrh.recipe.disassembler")
        .maxIO(1, 9, 0, 0)
        .minInputs(1, 0)
        .slotOverlays(
            (index, isFluid, isOutput, isSpecial) -> !isFluid && !isOutput ? GTUITextures.OVERLAY_SLOT_CIRCUIT : null)
        .progressBar(GTUITextures.PROGRESSBAR_ASSEMBLE)
        .build();

    private static final String[] ROMAN = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };

    public MTEDisassembler(int id, int tier) {
        super(
            id,
            "gtrh.machine.disassembler.tier." + (tier < 10 ? "0" : "") + tier,
            englishName(tier),
            tier,
            new String[] { StatCollector.translateToLocal("gtrh.machine.disassembler.description") },
            RECIPES,
            1,
            9,
            false,
            false,
            SoundResource.GTCEU_LOOP_ASSEMBLER,
            SpecialEffects.NONE,
            "ASSEMBLER");
    }

    public static ItemStack create(int id, int tier) {
        return new MTEDisassembler(id, tier).getStackForm(1);
    }

    private static String englishName(int tier) {
        if (tier == 1) return "Basic Disassembler";
        if (tier == 2) return "Advanced Disassembler";
        if (tier == 12) return "Highly Advanced Disassembler";
        return "Advanced Disassembler " + ROMAN[tier - 3];
    }
}
