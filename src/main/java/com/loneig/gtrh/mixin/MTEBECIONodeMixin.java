package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.Config;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import tectech.mechanics.boseEinsteinCondensate.CondensateList;
import tectech.thing.metaTileEntity.hatch.bec.MTEHatchLoS;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;
import tectech.thing.metaTileEntity.multi.bec.MTEBECIONode;

@Mixin(value = MTEBECIONode.class, remap = false)
public abstract class MTEBECIONodeMixin {

    @Shadow
    private int slowdowns;

    @Shadow
    private int subtickCounter;

    @Shadow
    private CondensateList requiredCondensate;

    @Shadow
    private MTEHatchLoS losHatch;

    @Inject(method = "incrementProgressTime", at = @At("HEAD"))
    private void gtrh$repairOverflowedProgress(CallbackInfo ci) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        MTEBECIONode self = (MTEBECIONode) (Object) this;
        if (self.mProgresstime < 0) self.mProgresstime = 0;
        if (this.subtickCounter < 0) this.subtickCounter = 0;
    }

    @Redirect(
        method = "incrementProgressTime",
        at = @At(
            value = "INVOKE",
            target = "Ltectech/thing/metaTileEntity/multi/bec/MTEBECAssembler;getAvailableNanites()I"))
    private int gtrh$nanitesThatFitProgress(MTEBECAssembler assembler) {
        int raw = assembler.getAvailableNanites();
        if (!Config.moduleEnabled("bec_no_nerf") || raw <= 0) return Math.max(raw, 0);

        MTEBECIONode self = (MTEBECIONode) (Object) this;
        int progress = Math.max(self.mProgresstime, 0);
        int room = self.mMaxProgresstime - progress;
        if (room <= 0) return 0;

        int sub = Math.max(this.subtickCounter, 0);
        int parallels = Math.max(self.getParallelRecipesInProgress(), 1);
        int slowdownCount = this.requiredCondensate == null ? this.slowdowns
            : assembler.getSlowdowns(this.requiredCondensate.keySet());
        int slowdown = Math.max(slowdownCount + 1, self.getManualSlowdown());
        long divisor = (long) parallels * slowdown;
        if (divisor < 1) divisor = 1;

        long needed = (long) room * divisor - sub;
        if (needed < 1) needed = 1;
        if (raw > needed) return needed > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) needed;

        long sumRoom = (long) Integer.MAX_VALUE - sub;
        return raw > sumRoom ? (int) sumRoom : raw;
    }

    @Inject(method = "getProcessingSpeed", at = @At("RETURN"), cancellable = true)
    private void gtrh$removeOverTierPenalty(CallbackInfoReturnable<Float> cir) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        MTEBECIONode self = (MTEBECIONode) (Object) this;

        MTEBECAssembler assembler = gtrh$getAssembler();
        int parallels = self.getParallelRecipesInProgress();

        if (assembler == null || parallels == 0) {
            cir.setReturnValue(0.0F);
            return;
        }

        int divisor = Math.max(this.slowdowns + 1, self.getManualSlowdown());
        cir.setReturnValue(assembler.getAvailableNanites() / (float) parallels / (float) divisor);
    }

    @Redirect(
        method = "incrementProgressTime",
        at = @At(value = "INVOKE", target = "Lgregtech/api/util/GTUtility;mulSafe(II)I", ordinal = 0))
    private int gtrh$dropAboveTierDivisor(int parallels, int aboveTier) {
        if (Config.moduleEnabled("bec_no_nerf")) return parallels;
        return GTUtility.mulSafe(parallels, aboveTier);
    }

    private MTEBECAssembler gtrh$getAssembler() {
        IGregTechTileEntity igte = ((MTEBECIONode) (Object) this).getBaseMetaTileEntity();

        if (igte == null || igte.isDead()) return null;
        if (!GTUtility.isServer()) return null;
        if (this.losHatch == null) return null;

        MTEHatchLoS connection = this.losHatch.getConnectedHatch();
        if (connection == null) return null;

        MetaTileEntity owner = connection.getOwner();
        return owner instanceof MTEBECAssembler ? (MTEBECAssembler) owner : null;
    }
}
