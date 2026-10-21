package com.loneig.gtrh.proxy;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.GTRH;
import com.loneig.gtrh.Tags;
import com.loneig.gtrh.module.comb.IndiumCombModule;
import com.loneig.gtrh.module.disassembler.DisassemblerModule;
import com.loneig.gtrh.module.processingarray.ProcessingArrayModule;
import com.loneig.gtrh.module.processingcenter.ProcessingCenterModule;
import com.loneig.gtrh.module.qft.QftWaterlineModule;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        GTRH.LOG.info("GTRH " + Tags.VERSION + " loaded");
    }

    public void init(FMLInitializationEvent event) {
        DisassemblerModule.init();
        ProcessingArrayModule.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        IndiumCombModule.register();
        QftWaterlineModule.register();
        ProcessingArrayModule.register();
        // GT++ blocks and TAE entries are only created during GregTech's init, which may run after ours.
        ProcessingCenterModule.postInit();
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        DisassemblerModule.loadComplete();
    }
}
