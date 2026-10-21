package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.Config;

import gregtech.common.tileentities.machines.outputme.base.MTEHatchOutputMEBase;

@Mixin(value = MTEHatchOutputMEBase.class, remap = false)
public class MTEHatchOutputMEBaseMixin {

    @Inject(method = "getCacheCapacity", at = @At("RETURN"), cancellable = true)
    private void gtrh$infiniteCacheCapacity(CallbackInfoReturnable<Long> cir) {
        if (Config.moduleEnabled("infinity_storage_me_hatches")) {
            cir.setReturnValue(Long.MAX_VALUE);
        }
    }
}
