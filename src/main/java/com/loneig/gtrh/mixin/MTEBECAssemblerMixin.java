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
    private boolean nanitesDirty;

    @Shadow
    private NaniteTier currentNaniteTier;

    @Shadow
    private int availableNanites;

    @Inject(method = "updateNaniteInfoIfDirty", at = @At("HEAD"), cancellable = true)
    private void gtrh$uncapNanitesOnUpdate(CallbackInfo ci) {
        if (!Config.moduleEnabled("bec_no_nerf") || !this.nanitesDirty) return;

        gtrh$recountNanites();
        ci.cancel();
    }

    // Hatch contents can change without marking the assembler cache dirty.
    @Inject(method = "onPostTick", at = @At("TAIL"))
    private void gtrh$uncapNanitesOnTick(IGregTechTileEntity igte, long aTick, CallbackInfo ci) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        gtrh$recountNanites();
    }

    private void gtrh$recountNanites() {
        long total = 0;
        NaniteTier lowestTier = null;

        for (MTEHatchNanite hatch : this.naniteHatches) {
            NaniteTier tier = NaniteTier.fromStack(hatch.getItemStack());
            if (tier == null) continue;

            int count = hatch.getItemCount();
            if (count == 0) continue;

            if (lowestTier == null || tier.ordinal() < lowestTier.ordinal()) {
                lowestTier = tier;
            }

            total += count;
        }

        this.nanitesDirty = false;
        this.currentNaniteTier = lowestTier;
        this.availableNanites = (int) Math.min(total, Integer.MAX_VALUE);
    }
}
