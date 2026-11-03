package com.loneig.gtrh.module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.gtnewhorizon.gtnhlib.util.AnimatedTooltipHandler;
import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import com.loneig.gtrh.Config;
import com.loneig.gtrh.util.GTRHTooltips;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.ItemList;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.multi.bec.MTEBECAssembler;

public final class ModifiedMachineTooltips {

    private static final char FORMAT = '\u00a7';
    private static final String STRIKE = AnimatedTooltipHandler.STRIKETHROUGH;
    private static final String RESET = AnimatedTooltipHandler.RESET;
    private static final String BOLD = AnimatedTooltipHandler.BOLD;
    private static final String GRAY = AnimatedTooltipHandler.GRAY;
    private static final String GREEN = AnimatedTooltipHandler.GREEN;
    private static final String FRAME = AnimatedTooltipHandler.DARK_AQUA;
    private static final String OPEN_FRAME = FRAME + "[";
    private static final String CLOSE_FRAME = FRAME + "]";
    private static final String ARROW = GRAY + " → ";

    private static final List<Rewrite> REWRITES = new ArrayList<>();
    private static final ModifiedMachineTooltips INSTANCE = new ModifiedMachineTooltips();

    private ModifiedMachineTooltips() {}

    public static void register() {
        if (Config.moduleEnabled("unbreakable_lightning_rod")) {
            add(
                "gtrh.tooltip.change.lightning_rod",
                ItemList.Machine_HV_LightningRod.get(1),
                ItemList.Machine_EV_LightningRod.get(1),
                ItemList.Machine_IV_LightningRod.get(1));
        }

        if (FMLCommonHandler.instance()
            .getSide() != Side.CLIENT) return;

        if (Config.moduleEnabled("infinity_storage_me_hatches")) {
            Supplier<String> capacity = AnimatedTooltipHandler.text(NumberFormatUtil.formatNumber(Long.MAX_VALUE));
            ItemStack bus = ItemList.Hatch_Output_Bus_ME.get(1);
            ItemStack hatch = ItemList.Hatch_Output_ME.get(1);
            rewrite(bus, new String[] { "1600" }, false, capacity);
            rewrite(hatch, new String[] { "128,000", "128kЛ", "128kл" }, false, capacity);
            GTRHTooltips.addModifiedByLine(bus);
            GTRHTooltips.addModifiedByLine(hatch);
        }

        if (Config.moduleEnabled("bec_no_nerf")) {
            ItemStack assembler = CustomItemList.Machine_Multi_BECAssembler.get(1);
            rewrite(
                assembler,
                new String[] { NumberFormatUtil.formatNumber(MTEBECAssembler.MAX_NANITES) },
                false,
                () -> NumberFormatUtil.formatNumber(Integer.MAX_VALUE));
            GTRHTooltips.addModifiedByLine(assembler);

            ItemStack ioNode = CustomItemList.Machine_Multi_BECIONode.get(1);
            rewrite(ioNode, new String[] { "2^" }, true, GTRHTooltips.line("gtrh.tooltip.change.bec_ionode"));
            GTRHTooltips.addModifiedByLine(ioNode);
        }

        if (!REWRITES.isEmpty()) MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (REWRITES.isEmpty()) return;

        for (Rewrite rule : REWRITES) {
            if (!sameItem(rule.stack, event.itemStack)) continue;

            List<String> tooltip = event.toolTip;
            String replacement = rule.replacement.get();
            // Index 0 is the item's display name, which the player is free to rename, and only the first line that
            // carries the old value belongs to the description this rule targets.
            for (int i = 1; i < tooltip.size(); i++) {
                String line = tooltip.get(i);
                String rewritten = null;
                for (String marker : rule.markers) {
                    rewritten = rule.wholeLine ? strikeLine(line, marker, replacement)
                        : strikeToken(line, marker, replacement);
                    if (rewritten != null) break;
                }
                if (rewritten == null) continue;

                tooltip.set(i, rewritten);
                break;
            }
        }
    }

    private static void add(String key, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            GTRHTooltips.addModifiedTooltip(stack, GTRHTooltips.line(key));
        }
    }

    private static void rewrite(ItemStack stack, String[] markers, boolean wholeLine, Supplier<String> replacement) {
        if (stack == null || markers == null || markers.length == 0) return;
        REWRITES.add(new Rewrite(stack, markers, wholeLine, replacement));
    }

    private static boolean sameItem(ItemStack rule, ItemStack stack) {
        return stack != null && stack.getItem() == rule.getItem() && stack.getItemDamage() == rule.getItemDamage();
    }

    /**
     * Strikes through only the matched text and appends the replacement after it, framed so the rewrite cannot be
     * confused with the surrounding text.
     */
    private static String strikeToken(String line, String marker, String replacement) {
        int[] span = findPlainSpan(line, marker);
        if (span == null) return null;

        StringBuilder rewritten = new StringBuilder(line.length() + replacement.length() + 24);
        rewritten.append(line, 0, span[0])
            .append(OPEN_FRAME)
            .append(GRAY)
            .append(STRIKE)
            .append(line, span[0], span[1])
            .append(RESET)
            .append(ARROW)
            .append(GREEN)
            .append(BOLD)
            .append(replacement)
            .append(RESET)
            .append(CLOSE_FRAME)
            .append(restoreStyle(line, span[1]))
            .append(line, span[1], line.length());
        return rewritten.toString();
    }

    /**
     * Strikes through the whole line and appends the framed replacement after it.
     */
    private static String strikeLine(String line, String marker, String replacement) {
        if (findPlainSpan(line, marker) == null) return null;

        return OPEN_FRAME + GRAY
            + STRIKE
            + stripFormatting(line)
            + RESET
            + ARROW
            + GREEN
            + BOLD
            + replacement
            + RESET
            + CLOSE_FRAME;
    }

    /**
     * Finds {@code marker} in {@code line} ignoring formatting codes.
     *
     * @return the raw {@code [start, end)} indexes of {@code marker}, or {@code null} when it is absent
     */
    private static int[] findPlainSpan(String line, String marker) {
        StringBuilder plain = new StringBuilder(line.length());
        int[] rawIndex = new int[line.length() + 1];

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == FORMAT && i + 1 < line.length()) {
                i++;
                continue;
            }
            rawIndex[plain.length()] = i;
            plain.append(c);
        }
        rawIndex[plain.length()] = line.length();

        for (int from = 0; from < plain.length(); from++) {
            int start = plain.indexOf(marker, from);
            if (start < 0) return null;

            int end = start + marker.length();
            // Reject markers that are part of a longer number, so "1600" does not match "16000".
            if (isNumberChar(charAt(plain, start - 1)) || isNumberChar(charAt(plain, end))) {
                from = start;
                continue;
            }

            // End right after the last matched character, so formatting codes that follow it stay outside the rewrite.
            return new int[] { rawIndex[start], rawIndex[end - 1] + 1 };
        }
        return null;
    }

    private static char charAt(CharSequence text, int index) {
        return index < 0 || index >= text.length() ? '\0' : text.charAt(index);
    }

    private static boolean isNumberChar(char c) {
        return Character.isDigit(c) || c == ',' || c == '.';
    }

    /**
     * Picks the formatting to re-apply after the rewritten region. Vanilla {@code §r} restores the color the line was
     * drawn with, but tooltip lines are usually colored gray by the GUI rather than by codes inside the text, so a line
     * without any formatting code has to fall back to gray instead of leaving the rest of it white.
     */
    private static String restoreStyle(String line, int end) {
        Style style = styleAt(line, end);
        return style.sawCode ? style.codes : GRAY;
    }

    /**
     * Rebuilds the formatting active at {@code end} in {@code line}, mirroring the vanilla font renderer: a color code
     * or {@code §r} clears bold, italic, underline, strikethrough and obfuscation.
     */
    private static Style styleAt(String line, int end) {
        String color = "";
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strike = false;
        boolean obfuscated = false;
        boolean sawCode = false;

        for (int i = 0; i < end; i++) {
            if (line.charAt(i) != FORMAT || i + 1 >= end) continue;

            sawCode = true;
            char code = Character.toLowerCase(line.charAt(i + 1));
            if (code == 'r') {
                color = "";
                bold = italic = underline = strike = obfuscated = false;
            } else if (isColorCode(code)) {
                color = FORMAT + "" + line.charAt(i + 1);
                bold = italic = underline = strike = obfuscated = false;
            } else if (code == 'l') {
                bold = true;
            } else if (code == 'o') {
                italic = true;
            } else if (code == 'n') {
                underline = true;
            } else if (code == 'm') {
                strike = true;
            } else if (code == 'k') {
                obfuscated = true;
            }
            i++;
        }

        StringBuilder codes = new StringBuilder(color);
        if (bold) codes.append(FORMAT)
            .append('l');
        if (italic) codes.append(FORMAT)
            .append('o');
        if (underline) codes.append(FORMAT)
            .append('n');
        if (strike) codes.append(FORMAT)
            .append('m');
        if (obfuscated) codes.append(FORMAT)
            .append('k');
        return new Style(codes.toString(), sawCode);
    }

    private static boolean isColorCode(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }

    private static String stripFormatting(String line) {
        StringBuilder stripped = new StringBuilder(line.length());
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == FORMAT && i + 1 < line.length()) {
                i++;
                continue;
            }
            stripped.append(c);
        }
        return stripped.toString();
    }

    private static final class Style {

        private final String codes;
        private final boolean sawCode;

        private Style(String codes, boolean sawCode) {
            this.codes = codes;
            this.sawCode = sawCode;
        }
    }

    private static final class Rewrite {

        private final ItemStack stack;
        private final String[] markers;
        private final boolean wholeLine;
        private final Supplier<String> replacement;

        private Rewrite(ItemStack stack, String[] markers, boolean wholeLine, Supplier<String> replacement) {
            this.stack = stack;
            this.markers = markers;
            this.wholeLine = wholeLine;
            this.replacement = replacement;
        }
    }
}
