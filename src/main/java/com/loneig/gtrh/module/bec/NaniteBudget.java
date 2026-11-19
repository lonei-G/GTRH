package com.loneig.gtrh.module.bec;

public final class NaniteBudget {

    private NaniteBudget() {}

    public static int resolve(int available, int room, int subtickCounter, int parallels, int slowdowns,
        int manualSlowdown) {
        if (available <= 0 || room <= 0) return 0;

        int banked = Math.max(subtickCounter, 0);
        long slowdown = Math.max((long) slowdowns + 1L, manualSlowdown);
        long divisor = Math.max((long) Math.max(parallels, 1) * slowdown, 1L);

        long needed = room > Long.MAX_VALUE / divisor ? Long.MAX_VALUE : (long) room * divisor - banked;
        if (needed < 1) needed = 1;

        long budget = Math.min(Math.min((long) available, needed), (long) Integer.MAX_VALUE - banked);
        return (int) Math.max(budget, 0L);
    }

    public static int saturateProgress(int current, int stored) {
        if (stored >= 0) return stored;

        int base = Math.max(current, 0);
        long sum = base + Integer.toUnsignedLong(stored - base);
        return (int) Math.min(Integer.MAX_VALUE, sum);
    }
}
