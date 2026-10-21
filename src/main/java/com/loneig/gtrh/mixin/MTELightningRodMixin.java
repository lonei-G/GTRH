package com.loneig.gtrh.mixin;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.loneig.gtrh.Config;

import gregtech.common.tileentities.generators.MTELightningRod;

@Mixin(value = MTELightningRod.class, remap = false)
public class MTELightningRodMixin {

    @Redirect(
        method = "onPostTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(III)Z"),
        remap = true)
    private boolean gtrh$keepRodIntact(World world, int x, int y, int z) {
        if (Config.moduleEnabled("unbreakable_lightning_rod")) {
            return true;
        }
        return world.setBlockToAir(x, y, z);
    }
}
