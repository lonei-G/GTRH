package com.loneig.gtrh.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.loneig.gtrh.Config;

import gregtech.api.enums.NaniteTier;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchNanite;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;

@Mixin(value = MTEBECAssembler.class, remap = false)
public abstract class MTEBECAssemblerMixin {

    @Shadow
    @Final
    private List<MTEHatchNanite> naniteHatches;

    @Shadow
    private int availableNanites;

    @Inject(method = "updateNaniteInfoIfDirty", at = @At("TAIL"))
    private void gtrh$uncapNanitesOnUpdate(CallbackInfo ci) {
        gtrh$recountNanites();
    }

    @Inject(method = "onPostTick", at = @At("TAIL"))
    private void gtrh$uncapNanitesOnTick(IGregTechTileEntity igte, long aTick, CallbackInfo ci) {
        gtrh$recountNanites();
    }

    private void gtrh$recountNanites() {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        long total = 0;

        for (MTEHatchNanite hatch : this.naniteHatches) {
            int count = hatch.getItemCount();
            if (count > 0 && NaniteTier.fromStack(hatch.getItemStack()) != null) {
                total += count;
                if (total > Integer.MAX_VALUE) {
                    total = Integer.MAX_VALUE;
                    break;
                }
            }
        }

        this.availableNanites = (int) total;
    }
}
