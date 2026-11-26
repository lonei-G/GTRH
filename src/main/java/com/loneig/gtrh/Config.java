package com.loneig.gtrh;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static final String[] MODULES = { "disassembler", "unbreakable_lightning_rod", "infinity_storage_me_hatches",
        "steam_output_bus_auto_output", "indium_comb_processing", "qft_waterline", "processing_array",
        "processing_center", "bec_no_nerf", "eec_staballoy_no_nerf", "unstable_ingot_shift_craft" };

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

        debug = configuration.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Enable debug logging.");
        t1MteId = configuration.getInt("t1MteId", "disassembler", 451, 1, 31995, "First T1-T5 disassembler MTE id.");
        t6MteId = configuration.getInt("t6MteId", "disassembler", 11160, 1, 31993, "First T6-T12 disassembler MTE id.");

        processingArrayMteId = configuration
            .getInt("processingArrayMteId", "processing_array", 1199, 1, 31995, "Processing Array MTE id.");
        processingCenterMteId = configuration
            .getInt("processingCenterMteId", "processing_center", 860, 1, 31995, "Processing Center MTE id.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static String moduleDescription(String id) {
        switch (id) {
            case "disassembler":
                return "Enable disassemblers.";
            case "unbreakable_lightning_rod":
                return "Protect lightning rods from strikes.";
            case "infinity_storage_me_hatches":
                return "Enable infinite ME output storage.";
            case "steam_output_bus_auto_output":
                return "Enable steam bus auto-output.";
            case "indium_comb_processing":
                return "Enable indium comb recipes.";
            case "qft_waterline":
                return "Enable QFT waterline recipes.";
            case "processing_array":
                return "Enable Processing Array.";
            case "processing_center":
                return "Enable Processing Center.";
            case "bec_no_nerf":
                return "Restore BEC nerfs and remove the nanite cap.";
            case "eec_staballoy_no_nerf":
                return "Restore Staballoy Construct drops.";
            case "unstable_ingot_shift_craft":
                return "Shift-right-click to craft unstable ingots without an explosion.";
            default:
                return "Enable the " + id + " module.";
        }
    }
}
