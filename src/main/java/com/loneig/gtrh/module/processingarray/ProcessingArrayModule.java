package com.loneig.gtrh.module.processingarray;

import net.minecraft.item.ItemStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;
import com.loneig.gtrh.util.GTRHTooltips;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.SoundResource;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTModHandler;

public final class ProcessingArrayModule {

    private ProcessingArrayModule() {}

    public static void init() {
        if (!Config.moduleEnabled("processing_array")) return;

        ProcessingArrayTextures.init();

        int id = Config.processingArrayMteId;
        if (GregTechAPI.METATILEENTITIES[id] != null) {
            GTRH.LOG.error("Processing array: MTE id {} is already in use, skipped", id);
            return;
        }

        ItemStack stack = new MTEProcessingArray(id, "multimachine.processingarray", "Processing Array")
            .getStackForm(1);
        GTRHTooltips.addMachineTooltip(stack);
        GTModHandler.addMachineCraftingRecipe(
            stack,
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.BUFFERED
                | GTModHandler.RecipeBits.NOT_REMOVABLE
                | GTModHandler.RecipeBits.REVERSIBLE,
            new Object[] { "CTC", "FMF", "CBC", 'M', ItemList.Hull_EV, 'B',
                OrePrefixes.pipeLarge.get(Materials.StainlessSteel), 'C', OrePrefixes.circuit.get(Materials.IV), 'F',
                ItemList.Robot_Arm_EV, 'T', ItemList.Energy_LapotronicOrb },
            4);
        GTRH.LOG.info("Processing array registered with MTE id {}", id);
    }

    public static void register() {
        if (!Config.moduleEnabled("processing_array")) return;

        registerRecipeMap(
            "basicmachine.alloysmelter",
            RecipeMaps.alloySmelterRecipes,
            SoundResource.IC2_MACHINES_INDUCTION_LOOP);
        registerRecipeMap(
            "basicmachine.arcfurnace",
            RecipeMaps.arcFurnaceRecipes,
            SoundResource.IC2_MACHINES_INDUCTION_LOOP);
        registerRecipeMap("basicmachine.assembler", RecipeMaps.assemblerRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.autoclave", RecipeMaps.autoclaveRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.bender", RecipeMaps.benderRecipes, SoundResource.IC2_MACHINES_COMPRESSOR_OP);
        registerRecipeMap("basicmachine.boxinator", RecipeMaps.packagerRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.brewery", RecipeMaps.brewingRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.canner", RecipeMaps.cannerRecipes, SoundResource.IC2_MACHINES_EXTRACTOR_OP);
        registerRecipeMap("basicmachine.centrifuge", RecipeMaps.centrifugeNonCellRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.chemicalbath", RecipeMaps.chemicalBathRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.chemicalreactor",
            RecipeMaps.multiblockChemicalReactorRecipes,
            SoundResource.IC2_MACHINES_EXTRACTOR_OP);
        registerRecipeMap("basicmachine.circuitassembler", RecipeMaps.circuitAssemblerRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.compressor",
            RecipeMaps.compressorRecipes,
            SoundResource.IC2_MACHINES_COMPRESSOR_OP);
        registerRecipeMap("basicmachine.cutter", RecipeMaps.cutterRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.distillery",
            RecipeMaps.distilleryRecipes,
            SoundResource.GT_MACHINES_DISTILLERY_LOOP);
        registerRecipeMap(
            "basicmachine.electrolyzer",
            RecipeMaps.electrolyzerNonCellRecipes,
            SoundResource.IC2_MACHINES_MAGNETIZER_LOOP);
        registerRecipeMap(
            "basicmachine.extractor",
            RecipeMaps.extractorRecipes,
            SoundResource.IC2_MACHINES_EXTRACTOR_OP);
        registerRecipeMap(
            "basicmachine.extruder",
            RecipeMaps.extruderRecipes,
            SoundResource.IC2_MACHINES_INDUCTION_LOOP);
        registerRecipeMap("basicmachine.fermenter", RecipeMaps.fermentingRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.fluidextractor",
            RecipeMaps.fluidExtractionRecipes,
            SoundResource.IC2_MACHINES_EXTRACTOR_OP);
        registerRecipeMap("basicmachine.fluidheater", RecipeMaps.fluidHeaterRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.fluidsolidifier", RecipeMaps.fluidSolidifierRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.hammer", RecipeMaps.hammerRecipes, SoundResource.RANDOM_ANVIL_USE);
        registerRecipeMap(
            "basicmachine.press",
            RecipeMaps.formingPressRecipes,
            SoundResource.IC2_MACHINES_COMPRESSOR_OP);
        registerRecipeMap(
            "basicmachine.laserengraver",
            RecipeMaps.laserEngraverRecipes,
            SoundResource.IC2_MACHINES_MAGNETIZER_LOOP);
        registerRecipeMap("basicmachine.lathe", RecipeMaps.latheRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.macerator",
            RecipeMaps.maceratorRecipes,
            SoundResource.IC2_MACHINES_MACERATOR_OP);
        registerRecipeMap(
            "basicmachine.electromagneticseparator",
            RecipeMaps.electroMagneticSeparatorRecipes,
            SoundResource.IC2_MACHINES_MAGNETIZER_LOOP);
        registerRecipeMap(
            "basicmachine.amplifab",
            RecipeMaps.amplifierRecipes,
            SoundResource.IC2_MACHINES_EXTRACTOR_OP);
        registerRecipeMap(
            "basicmachine.microwave",
            RecipeMaps.microwaveRecipes,
            SoundResource.IC2_MACHINES_ELECTROFURNACE_LOOP);
        registerRecipeMap("basicmachine.mixer", RecipeMaps.mixerNonCellRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.orewasher", RecipeMaps.oreWasherRecipes, SoundResource.NONE);
        registerRecipeMap(
            "basicmachine.polarizer",
            RecipeMaps.polarizerRecipes,
            SoundResource.IC2_MACHINES_MAGNETIZER_LOOP);
        registerRecipeMap("basicmachine.printer", RecipeMaps.printerRecipes, SoundResource.IC2_MACHINES_COMPRESSOR_OP);
        registerRecipeMap("basicmachine.recycler", RecipeMaps.recyclerRecipes, SoundResource.IC2_MACHINES_RECYCLER_OP);
        registerRecipeMap(
            "basicmachine.scanner",
            RecipeMaps.scannerFakeRecipes,
            SoundResource.IC2_MACHINES_MAGNETIZER_LOOP);
        registerRecipeMap("basicmachine.sifter", RecipeMaps.sifterRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.thermalcentrifuge", RecipeMaps.thermalCentrifugeRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.unboxinator", RecipeMaps.unpackagerRecipes, SoundResource.NONE);
        registerRecipeMap("basicmachine.wiremill", RecipeMaps.wiremillRecipes, SoundResource.IC2_MACHINES_RECYCLER_OP);
        registerRecipeMap("simplewasher.01", RecipeMaps.simpleWasherRecipes, SoundResource.NONE);
    }

    private static void registerRecipeMap(String machine, gregtech.api.recipe.RecipeMap<?> recipeMap,
        SoundResource sound) {
        ProcessingArrayManager.addRecipeMapToPA(machine, recipeMap);
        ProcessingArrayManager.addSoundResourceToPA(machine, sound);
    }
}
