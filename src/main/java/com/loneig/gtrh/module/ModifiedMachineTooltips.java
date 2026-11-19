package com.loneig.gtrh.module;

import java.util.function.Supplier;

import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import com.loneig.gtrh.Config;
import com.loneig.gtrh.util.GTRHTooltips;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.ItemList;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;

public final class ModifiedMachineTooltips {

    private ModifiedMachineTooltips() {}

    public static void register() {
        if (FMLCommonHandler.instance()
            .getSide() != Side.CLIENT) return;

        if (Config.moduleEnabled("unbreakable_lightning_rod")) {
            GTRHTooltips.addModifiedTooltip(
                "gtrh.tooltip.change.lightning_rod",
                ItemList.Machine_HV_LightningRod.get(1),
                ItemList.Machine_EV_LightningRod.get(1),
                ItemList.Machine_IV_LightningRod.get(1));
        }

        if (Config.moduleEnabled("infinity_storage_me_hatches")) {
            Supplier<String> capacity = () -> NumberFormatUtil.formatNumber(Long.MAX_VALUE);
            GTRHTooltips.replaceToken(ItemList.Hatch_Output_Bus_ME.get(1), capacity, "1600");
            GTRHTooltips.replaceToken(ItemList.Hatch_Output_ME.get(1), capacity, "128,000", "128kЛ", "128kл");
        }

        if (Config.moduleEnabled("steam_output_bus_auto_output")) {
            GTRHTooltips.addModifiedByLine(GregtechItemList.Hatch_Output_Bus_Steam.get(1));
        }

        if (Config.moduleEnabled("bec_no_nerf")) {
            GTRHTooltips.replaceToken(
                CustomItemList.Machine_Multi_BECAssembler.get(1),
                () -> NumberFormatUtil.formatNumber(Integer.MAX_VALUE),
                NumberFormatUtil.formatNumber(MTEBECAssembler.MAX_NANITES));
            GTRHTooltips.replaceLineContaining(
                CustomItemList.Machine_Multi_BECIONode.get(1),
                "2^",
                "gtrh.tooltip.change.bec_ionode");
        }
    }
}
