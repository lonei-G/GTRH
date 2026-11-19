package com.loneig.gtrh.module.bec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NaniteBudgetTest {

    @Test
    public void noRoomOrNoNanitesBanksNothing() {
        assertEquals(0, NaniteBudget.resolve(1000, 0, 0, 1, 0, 1));
        assertEquals(0, NaniteBudget.resolve(0, 10, 0, 1, 0, 1));
        assertEquals(0, NaniteBudget.resolve(-5, 10, 0, 1, 0, 1));
    }

    @Test
    public void banksOnlyWhatTheStepStillNeeds() {
        assertEquals(20, NaniteBudget.resolve(1000, 10, 0, 2, 0, 1));
    }

    @Test
    public void banksEverythingWhenTheAssemblerIsTheBottleneck() {
        assertEquals(7, NaniteBudget.resolve(7, 10, 0, 2, 0, 1));
    }

    @Test
    public void saturatedNeededDoesNotStallProgress() {
        int budget = NaniteBudget
            .resolve(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, 1);
        assertEquals(Integer.MAX_VALUE, budget);
    }

    @Test
    public void budgetNeverOverflowsTheBankedCounter() {
        assertEquals(5, NaniteBudget.resolve(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE - 5, 1, 0, 1));
    }

    @Test
    public void negativeBankedCounterCountsAsZero() {
        assertEquals(4, NaniteBudget.resolve(1000, 4, -9, 1, 0, 1));
    }

    @Test
    public void manualSlowdownRaisesTheDivisor() {
        assertEquals(10, NaniteBudget.resolve(1000, 1, 0, 1, 0, 10));
    }

    @Test
    public void parallelsAreAtLeastOne() {
        assertEquals(1, NaniteBudget.resolve(3, 1, 0, 0, 0, 1));
    }

    @Test
    public void clogSlowdownsRaiseTheDivisor() {
        assertEquals(4, NaniteBudget.resolve(1000, 1, 0, 1, 3, 1));
    }

    @Test
    public void progressThatDidNotWrapIsKept() {
        assertEquals(150, NaniteBudget.saturateProgress(100, 150));
        assertEquals(0, NaniteBudget.saturateProgress(0, 0));
    }

    @Test
    public void wrappedProgressIsRecoveredAndSaturated() {
        int current = 2_000_000_000;
        int fullTicks = 500_000_000;
        assertEquals(Integer.MAX_VALUE, NaniteBudget.saturateProgress(current, current + fullTicks));
        assertEquals(Integer.MAX_VALUE, NaniteBudget.saturateProgress(Integer.MAX_VALUE, Integer.MAX_VALUE * 2));
    }

    @Test
    public void negativeProgressCountsAsZero() {
        assertEquals(5, NaniteBudget.saturateProgress(-9, 5));
    }

}
