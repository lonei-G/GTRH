package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.Config;

import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;

@Mixin(value = MTEHatchSteamBusOutput.class, remap = false)
public class MTEHatchSteamBusOutputMixin {

    @Inject(method = "pushOutputInventory", at = @At("HEAD"), cancellable = true)
    private void gtrh$restoreAutoOutput(CallbackInfoReturnable<Boolean> cir) {
        if (Config.moduleEnabled("steam_output_bus_auto_output")) {
            cir.setReturnValue(true);
        }
    }
}
