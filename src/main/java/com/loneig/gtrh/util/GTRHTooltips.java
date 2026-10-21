package com.loneig.gtrh.util;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.util.AnimatedTooltipHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public final class GTRHTooltips {

    private GTRHTooltips() {}

    /**
     * Appends the "added by" line through GTNHLib's item tooltip registry, the same way GT-Not-Leisure does.
     * Must stay client only, the handler renders during ItemTooltipEvent.
     */
    public static void addMachineTooltip(ItemStack stack) {
        if (stack == null || FMLCommonHandler.instance()
            .getSide() != Side.CLIENT) return;
        AnimatedTooltipHandler.addItemTooltip(stack, authorLine());
    }

    private static final String MOD_NAME = "GregTech: Relics of the Horizon";
    private static final long PHASE_MILLIS = 55L;
    private static final String WHITE_TEXT = AnimatedTooltipHandler.WHITE + AnimatedTooltipHandler.BOLD;
    private static final String SILVER_TEXT = AnimatedTooltipHandler.GRAY + AnimatedTooltipHandler.BOLD;
    private static final String BROWN_TEXT = AnimatedTooltipHandler.GOLD + AnimatedTooltipHandler.BOLD;

    private static Supplier<String> authorLine() {
        return AnimatedTooltipHandler.chain(
            AnimatedTooltipHandler.translatedText("gtrh.tooltip.added_by"),
            AnimatedTooltipHandler.text(" "),
            GTRHTooltips::sweepModName);
    }

    /**
     * A highlight sweeps across the name, each character turning silver, then brown, and back to white.
     */
    private static String sweepModName() {
        long tick = System.currentTimeMillis() / PHASE_MILLIS;
        int cycle = MOD_NAME.length() + 2;
        StringBuilder builder = new StringBuilder(MOD_NAME.length() * 3);
        for (int i = 0; i < MOD_NAME.length(); i++) {
            long phase = Math.floorMod(tick - i, (long) cycle);
            builder.append(phase == 0L ? SILVER_TEXT : phase == 1L ? BROWN_TEXT : WHITE_TEXT)
                .append(MOD_NAME.charAt(i));
        }
        builder.append(AnimatedTooltipHandler.RESET);
        return builder.toString();
    }
}
