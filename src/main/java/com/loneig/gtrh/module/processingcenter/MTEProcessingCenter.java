package com.loneig.gtrh.module.processingcenter;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.Muffler;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.enums.TAE;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.metadata.CompressionTierKey;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSolidifier;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;

public class MTEProcessingCenter extends GTPPMultiBlockBase<MTEProcessingCenter> implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final int MACHINEMODE_METAL = 0;
    private static final int MACHINEMODE_FLUID = 1;
    private static final int MACHINEMODE_MISC = 2;

    private static final int MODE_COMPRESSOR = 0;
    private static final int MODE_LATHE = 1;
    private static final int MODE_MAGNETIC = 2;
    private static final int MODE_FERMENTER = 3;
    private static final int MODE_FLUIDEXTRACT = 4;
    private static final int MODE_EXTRACTOR = 5;
    private static final int MODE_LASER = 6;
    private static final int MODE_AUTOCLAVE = 7;
    private static final int MODE_FLUIDSOLIDIFY = 8;
    private static final int[][] MODE_MAP = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };

    private static final int[] CASING_TEXTURE_ID = { TAE.getIndexFromPage(2, 2) };

    private static IStructureDefinition<MTEProcessingCenter> STRUCTURE_DEFINITION = null;

    private int mCasing = 0;

    public MTEProcessingCenter(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEProcessingCenter(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEProcessingCenter(this.mName);
    }

    @Override
    public String getMachineType() {
        return StatCollector.translateToLocal("gtrh.machine.processing_center.type");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        String[] names = new String[9];
        for (int i = 0; i < 9; i++) {
            names[i] = StatCollector.translateToLocal("gtrh.machine.processing_center.machine." + i);
        }

        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType(getMachineType())
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.0"))
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.1"))
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.2"))
            .addInfo(
                StatCollector.translateToLocalFormatted(
                    "gtrh.machine.processing_center.info.metal",
                    names[0],
                    names[1],
                    names[2]))
            .addInfo(
                StatCollector.translateToLocalFormatted(
                    "gtrh.machine.processing_center.info.fluid",
                    names[3],
                    names[4],
                    names[5]))
            .addInfo(
                StatCollector.translateToLocalFormatted(
                    "gtrh.machine.processing_center.info.misc",
                    names[6],
                    names[7],
                    names[8]))
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.circuit"))
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.mode_switch"))
            .addInfo(StatCollector.translateToLocal("gtrh.machine.processing_center.info.solidifier"))
            .addPollutionAmount(getPollutionPerSecond(null))
            .beginStructureBlock(3, 3, 3, true)
            .addController(StatCollector.translateToLocal("gtrh.tooltip.front_center"))
            .addCasingInfoMin(StatCollector.translateToLocal("gtrh.tooltip.casing.multi_use"), 6, false)
            .addEnergyHatch(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addMaintenanceHatch(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addMufflerHatch(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addInputBus(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addOutputBus(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addInputHatch(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .addOutputHatch(StatCollector.translateToLocal("gtrh.tooltip.any_casing"), 1)
            .toolTipFinisher();
        return tt;
    }

    @Override
    public IStructureDefinition<MTEProcessingCenter> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<MTEProcessingCenter>builder()
                .addShape(
                    STRUCTURE_PIECE_MAIN,
                    transpose(
                        new String[][] { { "CCC", "CCC", "CCC" }, { "C~C", "C-C", "CCC" }, { "CCC", "CCC", "CCC" }, }))
                .addElement(
                    'C',
                    buildHatchAdder(MTEProcessingCenter.class)
                        .atLeast(InputBus, OutputBus, Maintenance, Energy, Muffler, InputHatch, OutputHatch)
                        .casingIndex(getCasingTextureId())
                        .hint(1)
                        .buildAndChain(onElementPass(x -> ++x.mCasing, ofBlock(ModBlocks.blockCasings3Misc, 2))))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 1, 1, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 1, 0, elementBudget, env, false, true);
    }

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
        mCasing = 0;
        if (!checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0, errors)) return;
        checkCasingMin(errors, mCasing, 6);
        checkHatch(errors);
    }

    @Override
    protected IIconContainer getActiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active;
    }

    @Override
    protected IIconContainer getActiveGlowOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active_Glow;
    }

    @Override
    protected IIconContainer getInactiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced;
    }

    @Override
    protected IIconContainer getInactiveGlowOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced_Glow;
    }

    @Override
    protected int getCasingTextureId() {
        return CASING_TEXTURE_ID[0];
    }

    @Override
    public int getMaxParallelRecipes() {
        return 2 * GTUtility.getTier(this.getMaxInputVoltage());
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        switch (machineMode) {
            case MACHINEMODE_METAL:
            case MACHINEMODE_FLUID:
                return 400;
            default:
                return 600;
        }
    }

    @Nonnull
    @Override
    public Collection<RecipeMap<?>> getAvailableRecipeMaps() {
        return Arrays.asList(
            RecipeMaps.compressorRecipes,
            RecipeMaps.latheRecipes,
            RecipeMaps.polarizerRecipes,
            RecipeMaps.fermentingRecipes,
            RecipeMaps.fluidExtractionRecipes,
            RecipeMaps.extractorRecipes,
            RecipeMaps.laserEngraverRecipes,
            RecipeMaps.autoclaveRecipes,
            RecipeMaps.fluidSolidifierRecipes);
    }

    @Override
    public int getRecipeCatalystPriority() {
        return -10;
    }

    private static RecipeMap<?> getRecipeMap(int aMode) {
        switch (aMode) {
            case MODE_COMPRESSOR:
                return RecipeMaps.compressorRecipes;
            case MODE_LATHE:
                return RecipeMaps.latheRecipes;
            case MODE_MAGNETIC:
                return RecipeMaps.polarizerRecipes;
            case MODE_FERMENTER:
                return RecipeMaps.fermentingRecipes;
            case MODE_FLUIDEXTRACT:
                return RecipeMaps.fluidExtractionRecipes;
            case MODE_EXTRACTOR:
                return RecipeMaps.extractorRecipes;
            case MODE_LASER:
                return RecipeMaps.laserEngraverRecipes;
            case MODE_AUTOCLAVE:
                return RecipeMaps.autoclaveRecipes;
            case MODE_FLUIDSOLIDIFY:
                return RecipeMaps.fluidSolidifierRecipes;
            default:
                return null;
        }
    }

    private ItemStack getCircuit(ItemStack[] t) {
        if (t == null) return null;
        ItemStack circuitItem = GTUtility.getIntegratedCircuit(0);
        for (ItemStack j : t) {
            if (j == null || j.getItem() != circuitItem.getItem()) continue;
            if (j.getItemDamage() >= 20 && j.getItemDamage() <= 22) {
                return j;
            }
        }
        return null;
    }

    private int getCircuitID(ItemStack circuit) {
        int damage = circuit.getItemDamage();
        int index = (damage == 20 ? 0 : (damage == 21 ? 1 : (damage == 22 ? 2 : -1)));
        if (index < 0) return -1;
        return MODE_MAP[machineMode][index];
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            private ItemStack lastCircuit = null;
            private int lastMode = -1;

            @Nonnull
            @Override
            protected Stream<GTRecipe> findRecipeMatches(@Nullable RecipeMap<?> map) {
                ItemStack circuit = getCircuit(inputItems);
                if (circuit == null) {
                    return Stream.empty();
                }
                if (!GTUtility.areStacksEqual(circuit, lastCircuit)) {
                    lastRecipe = null;
                    lastCircuit = circuit;
                }
                if (machineMode != lastMode) {
                    lastRecipe = null;
                    lastMode = machineMode;
                }
                RecipeMap<?> foundMap = getRecipeMap(getCircuitID(circuit));
                if (foundMap == null) {
                    return Stream.empty();
                }
                return super.findRecipeMatches(foundMap);
            }

            @Nonnull
            @Override
            protected CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                if (recipe.getMetadataOrDefault(CompressionTierKey.INSTANCE, 0) > 0)
                    return CheckRecipeResultRegistry.NO_RECIPE;
                return super.validateRecipe(recipe);
            }
        }.setSpeedBonus(1F / 3.5F)
            .setEuModifier(0.8F)
            .setMaxParallelSupplier(this::getTrueParallel);
    }

    @Nonnull
    @Override
    protected CheckRecipeResult checkRecipeForCustomHatches(CheckRecipeResult lastResult) {
        if (machineMode != MACHINEMODE_MISC) {
            return lastResult;
        }

        CheckRecipeResult result = lastResult;
        for (MTEHatchInput solidifierHatch : mInputHatches) {
            if (!(solidifierHatch instanceof MTEHatchSolidifier)) continue;

            List<ItemStack> items = ((MTEHatchSolidifier) solidifierHatch).getNonConsumableItems();
            FluidStack fluid = solidifierHatch.getFluid();
            if (items == null || fluid == null) continue;

            ItemStack circuitItem = GTUtility.getIntegratedCircuit(0);
            items.removeIf(
                stack -> stack != null && stack.getItem() == circuitItem.getItem()
                    && stack.getItemDamage() >= 20
                    && stack.getItemDamage() <= 22);
            items.add(GTUtility.getIntegratedCircuit(22));

            processingLogic.setInputItems(items);
            processingLogic.setInputFluids(fluid);

            CheckRecipeResult foundResult = processingLogic.process();
            if (foundResult.wasSuccessful()) {
                return foundResult;
            }
            if (foundResult != CheckRecipeResultRegistry.NO_RECIPE) {
                result = foundResult;
            }
        }
        return result;
    }

    @Override
    public String getMachineModeKey() {
        return "gtrh.machine.processing_center.mode." + machineMode;
    }

    @Override
    public boolean supportsMachineModeSwitch() {
        return true;
    }

    @Override
    protected @Nonnull MTEMultiBlockBaseGui<?> getGui() {
        return new MTEMultiBlockBaseGui<>(this).withMachineModeIcons(
            GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_LPF_METAL,
            GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_LPF_FLUID,
            GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_DEFAULT);
    }

    @Override
    public int nextMachineMode() {
        mLastRecipe = null;
        if (machineMode == MACHINEMODE_METAL) return MACHINEMODE_FLUID;
        if (machineMode == MACHINEMODE_FLUID) return MACHINEMODE_MISC;
        return MACHINEMODE_METAL;
    }

    @Override
    public boolean isInputSeparationEnabled() {
        return true;
    }

    @Override
    public void getExtraWailaNBT(EntityPlayerMP playerMP, TileEntity tileEntity, NBTTagCompound tag, World world, int x,
        int y, int z) {
        super.getExtraWailaNBT(playerMP, tileEntity, tag, world, x, y, z);
        tag.setString("mode", getMachineModeName());
    }

    @Override
    public String[] getInfoData() {
        String[] data = super.getInfoData();
        ArrayList<String> info = new ArrayList<>(Arrays.asList(data));
        info.add(StatCollector.translateToLocal(getMachineModeKey()));
        return info.toArray(new String[0]);
    }
}
