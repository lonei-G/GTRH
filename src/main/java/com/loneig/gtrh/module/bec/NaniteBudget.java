package com.loneig.gtrh.module.bec;

/**
 * Nanite budget used by {@code MTEBECIONode#incrementProgressTime} to decide how many nanites may be banked in one
 * tick. Kept free of Minecraft/GT types so the integer boundary behaviour can be unit tested.
 */
public final class NaniteBudget {

    private NaniteBudget() {}

    /**
     * @param available      nanites the assembler can provide this tick
     * @param room           remaining progress ticks of the current step
     * @param subtickCounter nanites already banked on the node
     * @param parallels      parallel recipes currently in progress
     * @param slowdowns      clog slowdown counter for the required condensates
     * @param manualSlowdown user configured speed divisor
     * @return nanites to bank this tick, always {@code 0 <= result <= Integer.MAX_VALUE - subtickCounter}
     */
    public static int resolve(int available, int room, int subtickCounter, int parallels, int slowdowns,
        int manualSlowdown) {
        if (available <= 0 || room <= 0) return 0;

        int banked = Math.max(subtickCounter, 0);
        long slowdown = Math.max((long) slowdowns + 1L, manualSlowdown);
        long divisor = Math.max((long) Math.max(parallels, 1) * slowdown, 1L);

        // room * divisor can exceed Long.MAX_VALUE, saturate instead of wrapping negative.
        long needed = room > Long.MAX_VALUE / divisor ? Long.MAX_VALUE : (long) room * divisor - banked;
        if (needed < 1) needed = 1;

        // The call site does subtickCounter += availableNanites, so the budget must stay within int range.
        long budget = Math.min(Math.min((long) available, needed), (long) Integer.MAX_VALUE - banked);
        return (int) Math.max(budget, 0L);
    }
}
