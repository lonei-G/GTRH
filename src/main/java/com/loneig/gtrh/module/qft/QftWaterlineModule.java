package com.loneig.gtrh.module.qft;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.loneig.gtrh.Config;

import bartworks.system.material.WerkstoffLoader;
import goodgenerator.items.GGMaterial;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.MaterialsElements;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

public final class QftWaterlineModule {

    private QftWaterlineModule() {}

    public static void register() {
        if (!Config.moduleEnabled("qft_waterline")) return;

        ItemStack limpidWaterCatalyst = GregtechItemList.PlatinumGroupCatalyst.get(1);
        ItemStack flawlessWaterCatalyst = GregtechItemList.InfiniteMutationCatalyst.get(1);

        OreDictionary.registerOre("catalystLimpidWater", limpidWaterCatalyst);
        OreDictionary.registerOre("catalystFlawlessWater", flawlessWaterCatalyst);

        GTValues.RA.stdBuilder()
            .itemInputs(
                GTUtility.getIntegratedCircuit(10),
                GregtechItemList.EmptyCatalystCarrier.get(1),
                WerkstoffLoader.Hedenbergit.get(OrePrefixes.lens, 1),
                GTOreDictUnificator.get(OrePrefixes.nanite, Materials.Silver, 1, false),
                ItemList.Circuit_Silicon_Wafer6.get(64))
            .itemOutputs(GTUtility.copyAmount(1, limpidWaterCatalyst))
            .fluidInputs(MaterialsElements.STANDALONE.HYPOGEN.getFluidStack(9216))
            .duration(60 * GTRecipeBuilder.SECONDS)
            .eut(TierEU.RECIPE_UEV)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                GTUtility.getIntegratedCircuit(10),
                GregtechItemList.EmptyCatalystCarrier.get(1),
                WerkstoffLoader.Hedenbergit.get(OrePrefixes.lens, 64),
                GTOreDictUnificator.get(OrePrefixes.nanite, Materials.Silver, 64, false),
                ItemList.Circuit_Silicon_Wafer6.get(64))
            .itemOutputs(GTUtility.copyAmount(1, flawlessWaterCatalyst))
            .fluidInputs(GGMaterial.shirabon.getMolten(92160))
            .duration(60 * GTRecipeBuilder.SECONDS)
            .eut(TierEU.RECIPE_UMV)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .fluidInputs(Materials.Water.getFluid(40_000L))
            .fluidOutputs(
                Materials.Grade1PurifiedWater.getFluid(20_000L),
                Materials.Grade2PurifiedWater.getFluid(10_000L),
                Materials.Grade3PurifiedWater.getFluid(5_000L),
                Materials.Grade4PurifiedWater.getFluid(1_000L))
            .duration(20 * GTRecipeBuilder.SECONDS)
            .eut(TierEU.RECIPE_UHV)
            .metadata(GTRecipeConstants.QFT_FOCUS_TIER, 2)
            .metadata(GTRecipeConstants.QFT_CATALYST, GTUtility.copyAmount(1, limpidWaterCatalyst))
            .addTo(RecipeMaps.quantumForceTransformerRecipes);

        GTValues.RA.stdBuilder()
            .fluidInputs(Materials.Water.getFluid(40_000L))
            .fluidOutputs(
                Materials.Grade5PurifiedWater.getFluid(20_000L),
                Materials.Grade6PurifiedWater.getFluid(10_000L),
                Materials.Grade7PurifiedWater.getFluid(5_000L),
                Materials.Grade8PurifiedWater.getFluid(1_000L))
            .duration(20 * GTRecipeBuilder.SECONDS)
            .eut(TierEU.RECIPE_UIV)
            .metadata(GTRecipeConstants.QFT_FOCUS_TIER, 4)
            .metadata(GTRecipeConstants.QFT_CATALYST, GTUtility.copyAmount(1, flawlessWaterCatalyst))
            .addTo(RecipeMaps.quantumForceTransformerRecipes);
    }
}
