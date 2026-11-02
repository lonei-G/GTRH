package com.loneig.gtrh.module.bec;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;

import goodgenerator.items.GGMaterial;
import goodgenerator.util.ItemRefer;
import gregtech.api.enums.CondensateType;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.BlastFurnaceGasStat;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.MaterialMisc;
import gtPlusPlus.core.material.MaterialsElements;
import tectech.recipe.TecTechRecipeMaps;
import tectech.thing.CustomItemList;

public final class BecNoNerfModule {

    private static final int OLD_EOH_DURATION = 3600 * 20;
    private static final int HOT_INGOT_NO_GAS_TICKS = 60 * 20;
    private static final double NO_GAS_TIME_MULTIPLIER = 1.25;

    private static final int INGOTS = 144;

    private static final ItemList[] METAMATERIALS = { ItemList.MetaMaterial_Shielding1,
        ItemList.MetaMaterial_Shielding2, ItemList.MetaMaterial_Shielding3, ItemList.MetaMaterial_Waveguide1,
        ItemList.MetaMaterial_Waveguide2, ItemList.MetaMaterial_Waveguide3, ItemList.MetaMaterial_EnergyConduit1,
        ItemList.MetaMaterial_EnergyConduit2, ItemList.MetaMaterial_EnergyConduit3,
        ItemList.MetaMaterial_ElectrograviticValve1, ItemList.MetaMaterial_ElectrograviticValve2,
        ItemList.MetaMaterial_ElectrograviticValve3, ItemList.MetaMaterial_WaveFocus1, ItemList.MetaMaterial_WaveFocus2,
        ItemList.MetaMaterial_WaveFocus3, ItemList.MetaMaterial_WaveFocus4, ItemList.MetaMaterial_ResonanceChamber1,
        ItemList.MetaMaterial_ResonanceChamber2, ItemList.MetaMaterial_ResonanceChamber3,
        ItemList.MetaMaterial_ResonanceChamber4, ItemList.MetaMaterial_SensorArray1, ItemList.MetaMaterial_SensorArray2,
        ItemList.MetaMaterial_SensorArray3, ItemList.MetaMaterial_SensorArray4, ItemList.MetaMaterial_FieldManipulator1,
        ItemList.MetaMaterial_FieldManipulator2, ItemList.MetaMaterial_FieldManipulator3,
        ItemList.MetaMaterial_FieldManipulator4 };

    private BecNoNerfModule() {}

    public static void loadComplete() {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        int condensates = revertCondensateDurations();
        int eoh = revertEohRecipes();
        int coils = revertCoilRecipes();
        int metamaterials = revertMetaMaterialRecipes();
        int ebf = revertBlastFurnaceRecipes();

        GTRH.LOG.info(
            "BEC no-nerf applied: condensates={}, eoh={}, coils={}, metamaterials={}, ebf={}",
            condensates,
            eoh,
            coils,
            metamaterials,
            ebf);
    }

    private static int revertCondensateDurations() {
        int changed = 0;

        for (GTRecipe recipe : TecTechRecipeMaps.condensateGeneratorRecipes.getAllRecipes()) {
            FluidStack[] inputs = recipe.mFluidInputs;
            if (inputs == null || inputs.length == 0) continue;

            Fluid fluid = inputs[0].getFluid();
            CondensateType type = CondensateType.getCondensateTypeBySource(fluid);
            if (type == null) type = CondensateType.getCondensateType(fluid);
            if (type == null) continue;

            int oldDuration = oldCondensateDuration(type);
            if (oldDuration > 0 && recipe.mDuration != oldDuration) {
                recipe.mDuration = oldDuration;
                changed++;
            }
        }

        return changed;
    }

    private static int oldCondensateDuration(CondensateType type) {
        switch (type) {
            case Neutronium:
            case CosmicNeutronium:
            case Bedrockium:
            case ChromaticGlass:
            case CelestialTungsten:
            case Infinity:
                return 20;
            case Hypogen:
            case TranscendentMetal:
                return 40;
            case SpaceTime:
            case Time:
            case Space:
                return 60;
            case MHDCSM:
            case MagMatter:
            case Universium:
            case Eternity:
                return 80;
            case DimensionallyShiftedSuperfluid:
            case PhononMedium:
            case QuarkGluonPlasma:
                return 120;
            case BoundlessCosmicSolder:
                return 160;
            default:
                return -1;
        }
    }

    private static int revertEohRecipes() {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(CustomItemList.EOH_Reinforced_Temporal_Casing.get(1));
        outputs.add(CustomItemList.EOH_Reinforced_Spatial_Casing.get(1));
        outputs.add(CustomItemList.EOH_Infinite_Energy_Casing.get(1));

        for (int tier = 0; tier < 9; tier++) {
            outputs.add(
                CustomItemList.valueOf("TimeAccelerationFieldGeneratorTier" + tier)
                    .get(1));
            outputs.add(
                CustomItemList.valueOf("SpacetimeCompressionFieldGeneratorTier" + tier)
                    .get(1));
            outputs.add(
                CustomItemList.valueOf("StabilisationFieldGeneratorTier" + tier)
                    .get(1));
        }

        ItemStack temporalCasing = CustomItemList.EOH_Reinforced_Temporal_Casing.get(1);
        ItemStack spatialCasing = CustomItemList.EOH_Reinforced_Spatial_Casing.get(1);
        ItemStack[] oldPlates = { GTOreDictUnificator.get(OrePrefixes.plateSuperdense, Materials.Bedrockium, 1),
            GTOreDictUnificator.get(OrePrefixes.plateSuperdense, Materials.CosmicNeutronium, 1),
            GGMaterial.shirabon.get(OrePrefixes.plateSuperdense, 1),
            GTOreDictUnificator.get(OrePrefixes.plateSuperdense, Materials.Infinity, 1) };

        int changed = 0;

        for (GTRecipe recipe : TecTechRecipeMaps.condensateAssemblingRecipes.getAllRecipes()) {
            if (!matchesAny(recipe, outputs)) continue;

            recipe.mDuration = OLD_EOH_DURATION;
            changed++;

            if (matches(recipe, temporalCasing) || matches(recipe, spatialCasing)) {
                revertPlates(recipe, oldPlates);
            }
        }

        return changed;
    }

    private static void revertPlates(GTRecipe recipe, ItemStack[] oldPlates) {
        if (recipe.mInputs == null) return;

        for (ItemStack input : recipe.mInputs) {
            if (input == null || input.stackSize <= 1) continue;

            for (ItemStack plate : oldPlates) {
                if (GTUtility.areStacksEqual(input, plate)) {
                    input.stackSize = 1;
                    break;
                }
            }
        }
    }

    private static int revertCoilRecipes() {
        ItemStack guidance = ItemList.CondensateGuidanceCoil.get(1);
        ItemStack transformative = ItemList.CondensateTransformativeCoil.get(1);
        ItemStack uxvCasing = ItemRefer.Compassline_Casing_UXV.get(1);

        int changed = 0;

        for (GTRecipe recipe : TecTechRecipeMaps.condensateAssemblingRecipes.getAllRecipes()) {
            if (matches(recipe, guidance) || matches(recipe, transformative)) {
                setCondensate(recipe, CondensateType.ChromaticGlass, 64 * INGOTS);
                setCondensate(recipe, CondensateType.Infinity, 8 * INGOTS);
                setCondensate(recipe, CondensateType.CelestialTungsten, 8 * INGOTS);
                changed++;
            } else if (matches(recipe, uxvCasing)) {
                setCondensate(recipe, CondensateType.BoundlessCosmicSolder, 32 * INGOTS);
                changed++;
            }
        }

        changed += revertResearchStationCoilFluids(guidance, transformative);

        return changed;
    }

    private static int revertResearchStationCoilFluids(ItemStack guidance, ItemStack transformative) {
        Fluid chromaticGlass = MaterialsElements.STANDALONE.CHRONOMATIC_GLASS.getFluidStack(1)
            .getFluid();
        Fluid infinity = Materials.Infinity.getMolten(1)
            .getFluid();
        Fluid celestialTungsten = MaterialsElements.STANDALONE.CELESTIAL_TUNGSTEN.getFluidStack(1)
            .getFluid();
        Fluid mutatedLivingSolder = MaterialMisc.MUTATED_LIVING_SOLDER.getFluidStack(1)
            .getFluid();

        int changed = 0;

        for (GTRecipe.RecipeAssemblyLine recipe : GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes) {
            if (!isCoil(recipe.mOutput, guidance, transformative)) continue;

            if (setFluidAmounts(
                recipe.mFluidInputs,
                chromaticGlass,
                96 * INGOTS,
                infinity,
                12 * INGOTS,
                celestialTungsten,
                12 * INGOTS,
                mutatedLivingSolder,
                4 * INGOTS)) {
                changed++;
            }
        }

        for (TecTechRecipeMaps.TTResearchStationALRecipe recipe : TecTechRecipeMaps.researchableALRecipeList) {
            if (!isCoil(recipe.mOutput, guidance, transformative)) continue;

            if (setFluidAmounts(
                recipe.mFluidInputs,
                chromaticGlass,
                96 * INGOTS,
                infinity,
                12 * INGOTS,
                celestialTungsten,
                12 * INGOTS,
                mutatedLivingSolder,
                4 * INGOTS)) {
                changed++;
            }
        }

        for (GTRecipe recipe : RecipeMaps.assemblylineVisualRecipes.getAllRecipes()) {
            ItemStack output = firstOutput(recipe);
            if (!isCoil(output, guidance, transformative)) continue;

            if (setFluidAmounts(
                recipe.mFluidInputs,
                chromaticGlass,
                96 * INGOTS,
                infinity,
                12 * INGOTS,
                celestialTungsten,
                12 * INGOTS,
                mutatedLivingSolder,
                4 * INGOTS)) {
                changed++;
            }
        }

        return changed;
    }

    private static boolean isCoil(ItemStack output, ItemStack guidance, ItemStack transformative) {
        return GTUtility.areStacksEqual(output, guidance) || GTUtility.areStacksEqual(output, transformative);
    }

    private static boolean setFluidAmounts(FluidStack[] fluids, Fluid fluidA, int amountA, Fluid fluidB, int amountB,
        Fluid fluidC, int amountC, Fluid fluidD, int amountD) {
        if (fluids == null) return false;

        boolean changed = false;

        for (FluidStack stack : fluids) {
            if (stack == null) continue;

            if (stack.getFluid() == fluidA) {
                changed |= stack.amount != amountA;
                stack.amount = amountA;
            } else if (stack.getFluid() == fluidB) {
                changed |= stack.amount != amountB;
                stack.amount = amountB;
            } else if (stack.getFluid() == fluidC) {
                changed |= stack.amount != amountC;
                stack.amount = amountC;
            } else if (stack.getFluid() == fluidD) {
                changed |= stack.amount != amountD;
                stack.amount = amountD;
            }
        }

        return changed;
    }

    private static int revertMetaMaterialRecipes() {
        int changed = 0;

        for (GTRecipe recipe : TecTechRecipeMaps.condensateAssemblingRecipes.getAllRecipes()) {
            if (!isMetaMaterial(recipe)) continue;

            FluidStack[] condensates = condensates(recipe);
            if (condensates == null) continue;

            for (FluidStack stack : condensates) {
                if (stack == null) continue;

                CondensateType type = CondensateType.getCondensateType(stack.getFluid());
                if (type == null || type.getUnit() != 1000) continue;

                int units = stack.amount / 1000;
                int oldAmount = units * INGOTS;
                if (type == CondensateType.DimensionallyShiftedSuperfluid) oldAmount *= 2;
                stack.amount = oldAmount;
            }

            changed++;
        }

        return changed;
    }

    private static int revertBlastFurnaceRecipes() {
        ItemStack shijima = GTOreDictUnificator.get(OrePrefixes.ingotHot, Materials.Shijima, 1);
        ItemStack churitsu = GTOreDictUnificator.get(OrePrefixes.ingotHot, Materials.Churitsu, 1);

        int changed = 0;

        double gasBaseTicks = HOT_INGOT_NO_GAS_TICKS / NO_GAS_TIME_MULTIPLIER;

        for (GTRecipe recipe : RecipeMaps.blastFurnaceRecipes.getAllRecipes()) {
            if (!matches(recipe, shijima) && !matches(recipe, churitsu)) continue;

            BlastFurnaceGasStat gas = gasInput(recipe);
            recipe.mDuration = gas == null ? HOT_INGOT_NO_GAS_TICKS
                : Math.max(1, (int) (gas.recipeTimeMultiplier * gasBaseTicks));
            recipe.mEUt = (int) TierEU.RECIPE_UHV;
            recipe.getMetadataStorage()
                .store(GTRecipeConstants.COIL_HEAT, Integer.valueOf(7400));
            changed++;
        }

        return changed;
    }

    private static BlastFurnaceGasStat gasInput(GTRecipe recipe) {
        if (recipe.mFluidInputs == null) return null;

        for (FluidStack stack : recipe.mFluidInputs) {
            if (stack == null) continue;

            for (BlastFurnaceGasStat gas : BlastFurnaceGasStat.BlastFurnaceGasStats) {
                if (stack.getFluid() == gas.gas.getFluid()) return gas;
            }
        }

        return null;
    }

    private static boolean isMetaMaterial(GTRecipe recipe) {
        for (ItemList item : METAMATERIALS) {
            if (matches(recipe, item.get(1))) return true;
        }
        return false;
    }

    private static boolean matchesAny(GTRecipe recipe, List<ItemStack> targets) {
        for (ItemStack target : targets) {
            if (matches(recipe, target)) return true;
        }
        return false;
    }

    private static boolean matches(GTRecipe recipe, ItemStack target) {
        return GTUtility.areStacksEqual(firstOutput(recipe), target);
    }

    private static ItemStack firstOutput(GTRecipe recipe) {
        if (recipe == null || recipe.mOutputs == null || recipe.mOutputs.length == 0) return null;
        return recipe.mOutputs[0];
    }

    private static FluidStack[] condensates(GTRecipe recipe) {
        return recipe.getMetadata(GTRecipeConstants.CONDENSATE_INPUT);
    }

    private static void setCondensate(GTRecipe recipe, CondensateType type, int amount) {
        FluidStack[] condensates = condensates(recipe);
        if (condensates == null) return;

        for (FluidStack stack : condensates) {
            if (stack == null) continue;
            if (CondensateType.getCondensateType(stack.getFluid()) == type) stack.amount = amount;
        }
    }
}
