package com.loneig.gtrh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import com.loneig.gtrh.Config;

import gregtech.api.metatileentity.implementations.MTETooltipMultiBlockBase;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;
import tectech.thing.metaTileEntity.multi.bec.MTEBECIONode;

@Mixin(value = MTETooltipMultiBlockBase.class, remap = false)
public class MTETooltipMultiBlockBaseMixin {

    @Inject(method = "getPrimaryDescription", at = @At("RETURN"), cancellable = true)
    private void gtrh$dropStaleBecLines(CallbackInfoReturnable<String[]> cir) {
        if (!Config.moduleEnabled("bec_no_nerf")) return;

        String marker = null;
        if ((Object) this instanceof MTEBECAssembler) {
            marker = NumberFormatUtil.formatNumber(MTEBECAssembler.MAX_NANITES);
        } else if ((Object) this instanceof MTEBECIONode) {
            marker = "2^";
        }
        if (marker == null) return;

        String[] lines = cir.getReturnValue();
        String[] filtered = withoutLinesContaining(lines, marker);
        if (filtered != lines) cir.setReturnValue(filtered);
    }

    private static String[] withoutLinesContaining(String[] lines, String marker) {
        if (lines == null || marker.isEmpty()) return lines;

        int kept = 0;
        for (String line : lines) {
            if (line == null || !line.contains(marker)) kept++;
        }
        if (kept == lines.length) return lines;

        String[] filtered = new String[kept];
        int index = 0;
        for (String line : lines) {
            if (line == null || !line.contains(marker)) filtered[index++] = line;
        }
        return filtered;
    }
}
