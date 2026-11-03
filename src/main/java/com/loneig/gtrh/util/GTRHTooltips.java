package com.loneig.gtrh.util;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.util.AnimatedTooltipHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public final class GTRHTooltips {

    private GTRHTooltips() {}

    public static void addMachineTooltip(ItemStack stack) {
        if (!isClient(stack)) return;
        AnimatedTooltipHandler.addItemTooltip(stack, authorLine());
    }

    @SafeVarargs
    public static void addModifiedTooltip(ItemStack stack, Supplier<String>... lines) {
        if (!isClient(stack) || lines == null || lines.length == 0) return;
        AnimatedTooltipHandler.addItemTooltip(stack, modificationLines(lines));
    }

    public static Supplier<String> line(String key) {
        return AnimatedTooltipHandler.translatedText(key);
    }

    private static boolean isClient(ItemStack stack) {
        return stack != null && FMLCommonHandler.instance()
            .getSide() == Side.CLIENT;
    }

    private static Supplier<String> modificationLines(Supplier<String>[] lines) {
        return () -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) builder.append('\n');
                builder.append(AnimatedTooltipHandler.GREEN)
                    .append(lines[i].get());
            }
            builder.append('\n')
                .append(modifiedByLine().get());
            return builder.toString();
        };
    }

    private static final String MOD_NAME = "GregTech: Relics of the Horizon";
    private static final long PHASE_MILLIS = 55L;
    private static final String WHITE_TEXT = AnimatedTooltipHandler.WHITE + AnimatedTooltipHandler.BOLD;
    private static final String SILVER_TEXT = AnimatedTooltipHandler.GRAY + AnimatedTooltipHandler.BOLD;
    private static final String BROWN_TEXT = AnimatedTooltipHandler.GOLD + AnimatedTooltipHandler.BOLD;

    private static Supplier<String> authorLine() {
        return creditLine("gtrh.tooltip.added_by");
    }

    private static Supplier<String> modifiedByLine() {
        return creditLine("gtrh.tooltip.modified_by");
    }

    private static Supplier<String> creditLine(String key) {
        return AnimatedTooltipHandler.chain(
            AnimatedTooltipHandler.translatedText(key),
            AnimatedTooltipHandler.text(" "),
            GTRHTooltips::sweepModName);
    }

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
