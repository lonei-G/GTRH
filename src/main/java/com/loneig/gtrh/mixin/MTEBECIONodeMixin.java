package com.loneig.gtrh.mixin;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.loneig.gtrh.Config;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import tectech.thing.metaTileEntity.hatch.bec.MTEHatchLoS;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;
import tectech.thing.metaTileEntity.multi.bec.MTEBECIONode;

@Mixin(value = MTEBECIONode.class, remap = false)
public abstract class MTEBECIONodeMixin extends MTEBECMultiblockBase<MTEBECIONodeMixin> {

    @Shadow
    private int slowdowns;

    @Shadow
    private int subtickCounter;

    @Shadow
    private MTEHatchLoS losHatch;

    private MTEBECIONodeMixin(int id, String name) {
        super(id, name);
    }

    @Inject(method = "incrementProgressTime", at = @At("HEAD"))
    private void gtrh$repairOverflowedProgress(CallbackInfo ci) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        if (this.mProgresstime < 0) this.mProgresstime = 0;
        if (this.subtickCounter < 0) this.subtickCounter = 0;
    }

    @WrapOperation(
        method = "incrementProgressTime",
        at = @At(
            value = "FIELD",
            target = "Ltectech/thing/metaTileEntity/multi/bec/MTEBECIONode;subtickCounter:I",
            opcode = Opcodes.PUTFIELD))
    private void gtrh$incrementProgressTime1(MTEBECIONode instance, int originValue, Operation<Void> original,
        @Local(ordinal = 0) int availableNanites) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;
        original.call(instance, (int) Math.min(Integer.MAX_VALUE, (long) subtickCounter + availableNanites));
    }

    @ModifyVariable(method = "incrementProgressTime", at = @At(value = "STORE", ordinal = 0), name = "nextProgress")
    private int gtrh$incrementProgressTime2(int originValue, @Local(ordinal = 0) int fullTicks) {
        return Config.moduleEnabled("bec_no_nerf")
            ? (int) Math.min(Integer.MAX_VALUE, (long) this.mProgresstime + fullTicks)
            : originValue;
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
