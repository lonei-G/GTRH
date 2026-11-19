package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.Config;
import com.loneig.gtrh.util.GTRHTooltips;

import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;

@Mixin(value = MTEHatchSteamBusOutput.class, remap = false)
public class MTEHatchSteamBusOutputMixin {

    @Inject(method = "getDescription", at = @At("RETURN"))
    private void gtrh$describeAutoOutput(CallbackInfoReturnable<String[]> cir) {
        String[] description = cir.getReturnValue();
        // GT++ puts the export behavior on the second description line, before capacity and the GT++ credit.
        if (Config.moduleEnabled("steam_output_bus_auto_output") && description.length > 2) {
            GTRHTooltips.replaceDescriptionLine(description, 1, "gtrh.tooltip.change.steam_output_bus");
        }
    }

    @Inject(method = "pushOutputInventory", at = @At("HEAD"), cancellable = true)
    private void gtrh$restoreAutoOutput(CallbackInfoReturnable<Boolean> cir) {
        if (Config.moduleEnabled("steam_output_bus_auto_output")) {
            cir.setReturnValue(true);
        }
    }
}
