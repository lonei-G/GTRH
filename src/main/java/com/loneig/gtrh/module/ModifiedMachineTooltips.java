package com.loneig.gtrh.module;

import net.minecraft.item.ItemStack;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.util.GTRHTooltips;

import gregtech.api.enums.ItemList;
import tectech.thing.CustomItemList;

public final class ModifiedMachineTooltips {

    private ModifiedMachineTooltips() {}

    public static void register() {
        if (Config.moduleEnabled("unbreakable_lightning_rod")) {
            add(
                "gtrh.tooltip.change.lightning_rod",
                ItemList.Machine_HV_LightningRod.get(1),
                ItemList.Machine_EV_LightningRod.get(1),
                ItemList.Machine_IV_LightningRod.get(1));
        }

        if (Config.moduleEnabled("infinity_storage_me_hatches")) {
            add("gtrh.tooltip.change.me_hatch", ItemList.Hatch_Output_Bus_ME.get(1), ItemList.Hatch_Output_ME.get(1));
        }

        if (Config.moduleEnabled("bec_no_nerf")) {
            add("gtrh.tooltip.change.bec_assembler.nanites", CustomItemList.Machine_Multi_BECAssembler.get(1));
            add("gtrh.tooltip.change.bec_ionode", CustomItemList.Machine_Multi_BECIONode.get(1));
        }
    }

    private static void add(String key, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            GTRHTooltips.addModifiedTooltip(stack, GTRHTooltips.line(key));
        }
    }
}
