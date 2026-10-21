package com.loneig.gtrh.module.disassembler;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import com.loneig.gtrh.GTRH;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.items.MetaGeneratedTool;
import gregtech.api.objects.GTItemStack;
import gregtech.api.objects.ItemData;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;

public final class DisassemblerHelper {

    private static List<GTItemStack> inputBlacklist;
    private static List<Map.Entry<GTItemStack, ItemStack>> outputHardOverride;
    private static List<Map.Entry<String, ItemStack>> oreDictReplace;
    private static List<Map.Entry<GTItemStack, ItemStack>> alwaysReplace;

    private DisassemblerHelper() {}

    public static void loadAssemblerRecipesToDisassembler() {
        Map<GTItemStack, List<GTRecipe>> grouped = new LinkedHashMap<>();

        for (GTRecipe recipe : RecipeMaps.assemblerRecipes.getAllRecipes()) {
            if (!shouldDisassemble(recipe.mOutputs)) continue;
            GTItemStack key = new GTItemStack(recipe.mOutputs[0]);
            List<GTRecipe> recipes = grouped.get(key);
            if (recipes == null) {
                recipes = new ArrayList<>();
                grouped.put(key, recipes);
            }
            recipes.add(recipe);
        }

        GTRH.LOG.info("Building disassembler recipes from " + grouped.size() + " assembler recipe groups");

        for (List<GTRecipe> recipes : grouped.values()) {
            try {
                addAssemblerGroup(recipes);
            } catch (RuntimeException e) {
                GTRH.LOG.warn(
                    "Failed to convert assembler recipe {} -> {}",
                    Arrays.toString(recipes.get(0).mInputs),
                    Arrays.toString(recipes.get(0).mOutputs),
                    e);
            }
        }
    }

    public static void handleGTCraftingRecipe(ReversedRecipeRegistry.GTCraftingRecipe recipe) {
        GTRecipe reversed = recipe.toReversedSafe();
        if (reversed == null) return;
        if (!shouldDisassemble(reversed.mInputs)) return;

        try {
            List<ItemStack> outputs = handleRecipeTransformation(reversed.mOutputs, null);
            if (containsPowderFraction(outputs)) return;
            outputs = filterValid(outputs);
            if (outputs.isEmpty()) return;

            GTRecipeBuilder builder = GTValues.RA.stdBuilder()
                .itemInputs(reversed.mInputs)
                .itemOutputs(outputs.toArray(new ItemStack[0]))
                .duration(300)
                .eut(30)
                .setNEIDesc(StatCollector.translateToLocal("gtrh.nei.generated.crafting"));

            builder.addTo(MTEDisassembler.RECIPES);
        } catch (RuntimeException e) {
            GTRH.LOG.warn(
                "Failed to convert reversed crafting recipe {} -> {}",
                Arrays.toString(reversed.mInputs),
                Arrays.toString(reversed.mOutputs),
                e);
        }
    }

    private static void addAssemblerGroup(List<GTRecipe> recipes) {
        List<GTRecipe> reversed = new ArrayList<>(recipes.size());
        for (GTRecipe recipe : recipes) {
            reversed.add(reverseRecipe(recipe));
        }

        GTRecipe first = reversed.get(0);
        if (first.mInputs.length != 1 || first.mInputs[0] == null) return;

        ItemStack input = first.mInputs[0];
        ItemStack hardOverride = findHardOverride(input);

        if (hardOverride != null) {
            GTRecipeBuilder builder = GTValues.RA.stdBuilder()
                .itemInputs(input.copy())
                .itemOutputs(hardOverride.copy())
                .duration(600)
                .eut(30)
                .setNEIDesc(StatCollector.translateToLocal("gtrh.nei.generated.assembler.hard"));
            builder.addTo(MTEDisassembler.RECIPES);
            return;
        }

        List<ItemStack[]> outputsInOtherRecipes = new ArrayList<>();
        for (int i = 1; i < reversed.size(); i++) {
            outputsInOtherRecipes.add(reversed.get(i).mOutputs);
        }

        List<ItemStack> outputs = handleRecipeTransformation(first.mOutputs, outputsInOtherRecipes);
        if (containsPowderFraction(outputs)) return;
        outputs = filterValid(outputs);
        if (outputs.isEmpty()) return;

        GTRecipeBuilder builder = GTValues.RA.stdBuilder()
            .itemInputs(input.copy())
            .itemOutputs(outputs.toArray(new ItemStack[0]))
            .duration(first.mDuration)
            .eut(first.mEUt)
            .setNEIDesc(StatCollector.translateToLocal("gtrh.nei.generated.assembler"));
        builder.addTo(MTEDisassembler.RECIPES);
    }

    private static GTRecipe reverseRecipe(GTRecipe recipe) {
        GTRecipe reversed = recipe.copy();
        reversed.mInputs = recipe.mOutputs;
        reversed.mOutputs = recipe.mInputs;
        return reversed;
    }

    private static List<ItemStack> filterValid(List<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (GTUtility.isStackValid(stack) && stack.stackSize > 0) {
                result.add(stack);
            }
        }
        return result;
    }

    private static boolean shouldDisassemble(ItemStack[] inputsOrOutputs) {
        return inputsOrOutputs != null && inputsOrOutputs.length == 1 && shouldDisassembleItemStack(inputsOrOutputs[0]);
    }

    private static boolean shouldDisassembleItemStack(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() instanceof MetaGeneratedTool) return false;
        if (isCircuit(stack)) return false;
        if (isOre(stack)) return false;
        if (isPowderFraction(stack)) return false;
        if (hasUnpackerRecipe(stack)) return false;

        for (GTItemStack blacklisted : inputBlacklist()) {
            ItemStack blacklistedStack = blacklisted.toStack();
            if (blacklistedStack != null && GTUtility.areStacksEqual(blacklistedStack, stack, true)) return false;
        }

        return true;
    }

    private static boolean isCircuit(ItemStack stack) {
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        return data != null && data.mPrefix == OrePrefixes.circuit;
    }

    private static boolean isOre(ItemStack stack) {
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        if (data == null) return false;
        return data.mPrefix == OrePrefixes.ore || data.mPrefix == OrePrefixes.crushed
            || data.mPrefix == OrePrefixes.crushedCentrifuged
            || data.mPrefix == OrePrefixes.crushedPurified;
    }

    private static boolean isPowderFraction(ItemStack stack) {
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        return data != null && (data.mPrefix == OrePrefixes.dustSmall || data.mPrefix == OrePrefixes.dustTiny);
    }

    private static boolean containsPowderFraction(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (isPowderFraction(stack)) return true;
        }
        return false;
    }

    private static boolean hasUnpackerRecipe(ItemStack stack) {
        return RecipeMaps.unpackagerRecipes.findRecipeQuery()
            .items(stack)
            .find() != null;
    }

    private static List<ItemStack> handleRecipeTransformation(ItemStack[] outputs,
        List<ItemStack[]> outputsInOtherRecipes) {
        ItemStack[] transformed = new ItemStack[outputs.length];

        for (int index = 0; index < outputs.length; index++) {
            ItemStack stack = outputs[index];
            if (stack == null) continue;

            ItemData data = GTOreDictUnificator.getItemData(stack);
            if (data == null || data.mMaterial == null || data.mMaterial.mMaterial == null || data.mPrefix == null) {
                transformed[index] = stack;
                continue;
            }

            Materials material = data.mMaterial.mMaterial;
            boolean replaced = false;

            if (outputsInOtherRecipes != null) {
                for (ItemStack[] otherOutputs : outputsInOtherRecipes) {
                    if (index >= otherOutputs.length) continue;

                    ItemData against = GTOreDictUnificator.getItemData(otherOutputs[index]);
                    if (against == null || against.mMaterial == null
                        || against.mMaterial.mMaterial == null
                        || against.mPrefix != data.mPrefix) {
                        continue;
                    }

                    Materials cheaper = replaceCheaperOrNull(material, against.mMaterial.mMaterial);
                    if (cheaper != null) {
                        transformed[index] = GTOreDictUnificator.get(data.mPrefix, cheaper, stack.stackSize);
                        replaced = true;
                        break;
                    }

                    Materials nonAny = replaceAnyOrNull(material);
                    if (nonAny != null) {
                        transformed[index] = GTOreDictUnificator.get(data.mPrefix, nonAny, stack.stackSize);
                        replaced = true;
                        break;
                    }
                }
            }

            if (replaced) continue;

            Materials unprocessed = getUnprocessedMaterials(material);
            if (unprocessed != null) {
                transformed[index] = GTOreDictUnificator.get(data.mPrefix, unprocessed, stack.stackSize);
            }

            if (data.mPrefix == OrePrefixes.circuit) {
                ItemStack circuit = getCheapestCircuitOrNull(material);
                if (circuit != null) {
                    circuit = circuit.copy();
                    circuit.stackSize = stack.stackSize;
                    transformed[index] = circuit;
                }
            }
        }

        List<ItemStack> result = new ArrayList<>(outputs.length);

        for (int index = 0; index < outputs.length; index++) {
            ItemStack stack = transformed[index] != null ? transformed[index] : outputs[index];
            if (stack == null) continue;

            stack = stack.copy();

            if (outputs[index] != null && GTUtility.areStacksEqual(stack, outputs[index])) {
                stack.stackSize = Math.min(stack.stackSize, outputs[index].stackSize);
            }

            for (Map.Entry<GTItemStack, ItemStack> entry : alwaysReplace()) {
                ItemStack matcher = entry.getKey()
                    .toStack();
                if (matcher != null && GTUtility.areStacksEqual(stack, matcher, true)) {
                    stack = entry.getValue()
                        .copy();
                    break;
                }
            }

            stack = handleUnification(stack);
            stack = handleWildcard(stack);
            stack = handleContainerItem(stack);
            if (stack != null) {
                result.add(stack);
            }
        }

        return result;
    }

    private static Materials replaceCheaperOrNull(Materials first, Materials second) {
        if (first == second) return null;
        if (first == Materials.Aluminium && second == Materials.Iron) return second;
        if (first == Materials.Steel && second == Materials.Iron) return second;
        if (first == Materials.WroughtIron && second == Materials.Iron) return second;
        if (first == Materials.Aluminium && second == Materials.WroughtIron) return Materials.Iron;
        if (first == Materials.Aluminium && second == Materials.Steel) return second;
        if (first == Materials.Polytetrafluoroethylene && second == Materials.Polyethylene) return second;
        if (first == Materials.Polybenzimidazole && second == Materials.Polyethylene) return second;
        if (first == Materials.Polystyrene && second == Materials.Polyethylene) return second;
        if (first == Materials.Polydimethylsiloxane && second == Materials.Polyethylene) return second;
        if ((first == Materials.NetherQuartz || first == Materials.CertusQuartz) && second == Materials.Quartzite) {
            return second;
        }
        if (first == Materials.Polyethylene && second == Materials.Wood) return second;
        if (first == Materials.Diamond && second == Materials.Glass) return second;
        return null;
    }

    private static Materials replaceAnyOrNull(Materials material) {
        if (material.mOreReRegistrations.contains(Materials.AnyIron)) return Materials.Iron;
        if (material.mOreReRegistrations.contains(Materials.AnyCopper)) return Materials.Copper;
        if (material.mOreReRegistrations.contains(Materials.AnyRubber)) return Materials.Rubber;
        if (material.mOreReRegistrations.contains(Materials.AnyBronze)) return Materials.Bronze;
        if (material.mOreReRegistrations.contains(Materials.AnySyntheticRubber)) return Materials.Rubber;
        return null;
    }

    private static Materials getUnprocessedMaterials(Materials material) {
        if (material == Materials.SteelMagnetic) return Materials.Steel;
        if (material == Materials.IronMagnetic) return Materials.Iron;
        if (material == Materials.NeodymiumMagnetic) return Materials.Neodymium;
        if (material == Materials.SamariumMagnetic) return Materials.Samarium;
        if (material == Materials.AnnealedCopper) return Materials.Copper;
        return null;
    }

    private static ItemStack getCheapestCircuitOrNull(Materials material) {
        if (material == Materials.ULV) return ItemList.NandChip.get(1);
        if (material == Materials.LV) return ItemList.Circuit_Microprocessor.get(1);
        if (material == Materials.MV) return ItemList.Circuit_Good.get(1);
        if (material == Materials.HV) return ItemList.Circuit_Advanced.get(1);
        if (material == Materials.EV) return ItemList.Circuit_Data.get(1);
        if (material == Materials.IV) return ItemList.Circuit_Elite.get(1);
        if (material == Materials.LuV) return ItemList.Circuit_Master.get(1);
        if (material == Materials.ZPM) return ItemList.Circuit_Quantummainframe.get(1);
        if (material == Materials.UV) return ItemList.Circuit_Crystalmainframe.get(1);
        if (material == Materials.UHV) return ItemList.Circuit_Wetwaremainframe.get(1);
        if (material == Materials.UEV) return ItemList.Circuit_Biomainframe.get(1);
        if (material == Materials.UIV) return ItemList.Circuit_OpticalMainframe.get(1);
        if (material == Materials.UMV) return ItemList.Circuit_ExoticMainframe.get(1);
        return null;
    }

    private static ItemStack handleUnification(ItemStack stack) {
        if (stack != null) {
            for (int oreId : OreDictionary.getOreIDs(stack)) {
                String oreName = OreDictionary.getOreName(oreId);
                for (Map.Entry<String, ItemStack> entry : oreDictReplace()) {
                    if (oreName.equals(entry.getKey())) {
                        ItemStack replacement = entry.getValue()
                            .copy();
                        replacement.stackSize = stack.stackSize;
                        return replacement;
                    }
                }
            }
        }
        return GTOreDictUnificator.get(stack);
    }

    private static ItemStack handleWildcard(ItemStack stack) {
        if (stack != null && stack.getItemDamage() == OreDictionary.WILDCARD_VALUE
            && !stack.getItem()
                .isDamageable()) {
            stack.setItemDamage(0);
        }
        return stack;
    }

    private static ItemStack handleContainerItem(ItemStack stack) {
        if (stack != null && stack.getItem()
            .hasContainerItem(stack)) {
            return null;
        }
        return stack;
    }

    private static ItemStack findHardOverride(ItemStack input) {
        for (Map.Entry<GTItemStack, ItemStack> entry : outputHardOverride()) {
            if (entry.getKey()
                .isStackEqual(input)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static List<GTItemStack> inputBlacklist() {
        if (inputBlacklist != null) return inputBlacklist;

        List<GTItemStack> list = new ArrayList<>();

        addBlacklist(list, ItemList.Casing_Coil_Superconductor.get(1));
        addBlacklist(list, Materials.Graphene.getDust(1));
        addBlacklist(list, ItemList.Circuit_Parts_Vacuum_Tube.get(1));
        addBlacklist(list, ItemList.Schematic.get(1));

        if (Mods.Railcraft.isModLoaded()) {
            addBlacklist(list, GTModHandler.getModItem(Mods.Railcraft.ID, "track", 1L, 0));
            addBlacklist(list, GTModHandler.getModItem(Mods.Railcraft.ID, "track", 1L, 736));
            addBlacklist(list, GTModHandler.getModItem(Mods.Railcraft.ID, "track", 1L, 816));
        }

        addBlacklist(list, GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "mixedMetalIngot", 1L, 0));
        addBlacklist(list, GTModHandler.getModItem(Mods.Railcraft.ID, "machine.alpha", 1, 14));

        // transformers
        addBlacklist(list, ItemList.Transformer_MV_LV.get(1L));
        addBlacklist(list, GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "blockElectric", 1L, 3));
        addBlacklist(list, ItemList.Transformer_HV_MV.get(1L));
        addBlacklist(list, GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "blockElectric", 1L, 4));
        addBlacklist(list, ItemList.Transformer_EV_HV.get(1L));
        addBlacklist(list, GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "blockElectric", 1L, 5));
        addBlacklist(list, ItemList.Transformer_IV_EV.get(1L));
        addBlacklist(list, GTModHandler.getModItem(Mods.IndustrialCraft2.ID, "blockElectric", 1L, 6));

        addBlacklist(list, GTModHandler.getModItem(Mods.AppliedEnergistics2.ID, "item.ItemMultiPart", 1L, 36));
        addBlacklist(list, GTModHandler.getModItem(Mods.AppliedEnergistics2.ID, "item.ItemMultiPart", 1L, 536));

        // Radiation Proof Plate
        addBlacklist(list, GTModHandler.getModItem(Mods.GoodGenerator.ID, "radiationProtectionPlate", 1L, 0));

        inputBlacklist = list;
        return list;
    }

    private static void addBlacklist(List<GTItemStack> list, ItemStack stack) {
        if (stack != null) {
            list.add(new GTItemStack(stack));
        }
    }

    private static List<Map.Entry<GTItemStack, ItemStack>> outputHardOverride() {
        if (outputHardOverride != null) return outputHardOverride;

        List<Map.Entry<GTItemStack, ItemStack>> list = new ArrayList<>();
        list.add(
            new SimpleImmutableEntry<>(new GTItemStack(new ItemStack(Blocks.torch, 6)), new ItemStack(Items.stick)));
        outputHardOverride = list;
        return list;
    }

    private static List<Map.Entry<String, ItemStack>> oreDictReplace() {
        if (oreDictReplace != null) return oreDictReplace;

        List<Map.Entry<String, ItemStack>> list = new ArrayList<>();
        list.add(new SimpleImmutableEntry<>("plankWood", new ItemStack(Blocks.planks)));
        list.add(new SimpleImmutableEntry<>("stoneCobble", new ItemStack(Blocks.cobblestone)));
        list.add(new SimpleImmutableEntry<>("gemDiamond", new ItemStack(Items.diamond)));
        list.add(new SimpleImmutableEntry<>("logWood", new ItemStack(Blocks.log)));
        list.add(new SimpleImmutableEntry<>("stickWood", new ItemStack(Items.stick)));
        list.add(new SimpleImmutableEntry<>("treeSapling", new ItemStack(Blocks.sapling)));
        oreDictReplace = list;
        return list;
    }

    private static List<Map.Entry<GTItemStack, ItemStack>> alwaysReplace() {
        if (alwaysReplace != null) return alwaysReplace;

        List<Map.Entry<GTItemStack, ItemStack>> list = new ArrayList<>();
        list.add(
            new SimpleImmutableEntry<>(
                new GTItemStack(new ItemStack(Blocks.trapped_chest, 1, OreDictionary.WILDCARD_VALUE)),
                new ItemStack(Blocks.chest, 1, OreDictionary.WILDCARD_VALUE)));
        alwaysReplace = list;
        return list;
    }
}
