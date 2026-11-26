package com.loneig.gtrh;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static final String[] MODULES = { "disassembler", "unbreakable_lightning_rod", "infinity_storage_me_hatches",
        "steam_output_bus_auto_output", "indium_comb_processing", "qft_waterline", "processing_array",
        "processing_center", "bec_no_nerf", "eec_staballoy_no_nerf" };

    private static final Map<String, Boolean> MODULE_STATE = new LinkedHashMap<>();

    public static boolean debug;
    public static int t1MteId = 451;
    public static int t6MteId = 11160;
    public static int processingArrayMteId = 1199;
    public static int processingCenterMteId = 860;

    public static boolean moduleEnabled(String id) {
        Boolean value = MODULE_STATE.get(id);
        return value == null || value;
    }

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        configuration.load();

        for (String id : MODULES) {
            MODULE_STATE.put(id, configuration.getBoolean(id, "modules", true, moduleDescription(id)));
        }

        debug = configuration.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Enable GTRH debug logging.");
        t1MteId = configuration.getInt(
            "t1MteId",
            "disassembler",
            451,
            1,
            31995,
            "First MTE id of the T1-T5 disassemblers. Five consecutive ids are used.");
        t6MteId = configuration.getInt(
            "t6MteId",
            "disassembler",
            11160,
            1,
            31993,
            "First MTE id of the T6-T12 disassemblers. Seven consecutive ids are used.");

        processingArrayMteId = configuration
            .getInt("processingArrayMteId", "processing_array", 1199, 1, 31995, "MTE id of the processing array.");
        processingCenterMteId = configuration.getInt(
            "processingCenterMteId",
            "processing_center",
            860,
            1,
            31995,
            "MTE id of the nine in one processing center.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static String moduleDescription(String id) {
        switch (id) {
            case "disassembler":
                return "Enable the T1-T12 disassemblers and the recipes reversed from assembler and crafting table recipes.";
            case "unbreakable_lightning_rod":
                return "Prevent lightning rods from being destroyed when struck.";
            case "infinity_storage_me_hatches":
                return "Give ME output buses and ME output hatches infinite capacity.";
            case "steam_output_bus_auto_output":
                return "Restore the automatic output of steam output buses to adjacent inventories.";
            case "indium_comb_processing":
                return "Add large chemical reactor recipes for indium combs.";
            case "qft_waterline":
                return "Add the QFT waterline recipes and their catalysts.";
            case "processing_array":
                return "Add the processing array multiblock.";
            case "processing_center":
                return "Add the processing center multiblock.";
            case "bec_no_nerf":
                return "Revert BEC nerfs and remove the nanite cap.";
            case "eec_staballoy_no_nerf":
                return "Restore the pre-nerf Staballoy Construct drops in the Extreme Entity Crusher.";
            default:
                return "Enable the " + id + " module.";
        }
    }
}
