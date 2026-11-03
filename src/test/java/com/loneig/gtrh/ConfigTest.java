package com.loneig.gtrh;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class ConfigTest {

    @Test
    public void becNoNerfSwitchIsRegisteredAndEnabledByDefault() {
        assertTrue(
            Arrays.asList(Config.MODULES)
                .contains("bec_no_nerf"));
        assertTrue(Config.moduleEnabled("bec_no_nerf"));
    }
}
