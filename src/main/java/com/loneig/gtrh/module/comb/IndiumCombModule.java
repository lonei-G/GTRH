package com.loneig.gtrh.module.comb;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;

import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.common.items.CombType;
import gregtech.loaders.misc.GTBees;

public final class IndiumCombModule {

    private IndiumCombModule() {}

    public static void register() {
        if (!Config.moduleEnabled("indium_comb_processing")) return;
        if (!Mods.Forestry.isModLoaded()) {
            GTRH.LOG.info("Forestry not found, skipping indium comb processing.");
            return;
        }
        GTBees.combs.addProcessGT(CombType.INDIUM, new Materials[] { Materials.Indium }, CombType.INDIUM.voltage);
    }
}
