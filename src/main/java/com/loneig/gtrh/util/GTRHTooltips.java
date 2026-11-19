package com.loneig.gtrh.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.gtnewhorizon.gtnhlib.util.AnimatedTooltipHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

public final class GTRHTooltips {

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
    private static final GTRHTooltips INSTANCE = new GTRHTooltips();

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

    public static void addModifiedByLine(ItemStack stack) {
        if (!isClient(stack)) return;
        AnimatedTooltipHandler.addItemTooltip(stack, modifiedByLine());
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

    public static void addModifiedTooltip(String key, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            addModifiedTooltip(stack, line(key));
        }
    }

    public static void replaceToken(ItemStack stack, Supplier<String> replacement, String... markers) {
        rewrite(stack, markers, false, replacement);
    }

    public static void replaceLineContaining(ItemStack stack, String marker, String translationKey) {
        rewrite(stack, new String[] { marker }, true, line(translationKey));
    }

    public static void replaceDescriptionLine(String[] description, int index, String translationKey) {
        description[index] = strikeLine(description[index], StatCollector.translateToLocal(translationKey));
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        for (Rewrite rule : REWRITES) {
            if (!sameItem(rule.stack, event.itemStack)) continue;

            List<String> tooltip = event.toolTip;
            String replacement = rule.replacement.get();
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

    private static void rewrite(ItemStack stack, String[] markers, boolean wholeLine, Supplier<String> replacement) {
        if (!isClient(stack) || markers == null || markers.length == 0) return;
        if (REWRITES.isEmpty()) MinecraftForge.EVENT_BUS.register(INSTANCE);
        addModifiedByLine(stack);
        REWRITES.add(new Rewrite(stack, markers, wholeLine, replacement));
    }

    private static boolean sameItem(ItemStack rule, ItemStack stack) {
        return stack != null && stack.getItem() == rule.getItem() && stack.getItemDamage() == rule.getItemDamage();
    }

    private static String strikeToken(String line, String marker, String replacement) {
        int[] span = findPlainSpan(line, marker);
        if (span == null) return null;

        StringBuilder rewritten = new StringBuilder(line.length() + replacement.length() + 24);
        rewritten.append(line, 0, span[0])
            .append(strikeLine(line.substring(span[0], span[1]), replacement))
            .append(restoreStyle(line, span[1]))
            .append(line, span[1], line.length());
        return rewritten.toString();
    }

    private static String strikeLine(String line, String marker, String replacement) {
        return stripFormatting(line).contains(marker) ? strikeLine(line, replacement) : null;
    }

    private static String strikeLine(String line, String replacement) {
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

    private static int[] findPlainSpan(String line, String marker) {
        if (marker.isEmpty()) return null;
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
            if (isNumberChar(charAt(plain, start - 1)) || isNumberChar(charAt(plain, end))) {
                from = start;
                continue;
            }

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

    private static String restoreStyle(String line, int end) {
        Style style = styleAt(line, end);
        return style.sawCode ? style.codes : GRAY;
    }

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
