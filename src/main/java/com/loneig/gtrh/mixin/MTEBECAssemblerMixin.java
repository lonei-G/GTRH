package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.loneig.gtrh.Config;

import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;

@Mixin(value = MTEBECAssembler.class, remap = false)
public abstract class MTEBECAssemblerMixin {

    @ModifyConstant(method = "updateNaniteInfoIfDirty()V", constant = @Constant(intValue = 30720))
    private int gtrh$updateNaniteInfoIfDirty(int original) {
        return Config.moduleEnabled("bec_no_nerf") ? Integer.MAX_VALUE : original;
    }

    @ModifyConstant(
        method = "createTooltip()Lgregtech/api/util/MultiblockTooltipBuilder;",
        constant = @Constant(intValue = 30720))
    private int gtrh$createTooltip(int original) {
        return Config.moduleEnabled("bec_no_nerf") ? Integer.MAX_VALUE : original;
    }
}
