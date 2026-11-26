package com.loneig.gtrh.mixin;

import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.loneig.gtrh.Config;

import cpw.mods.fml.common.registry.GameRegistry;

@Mixin(ContainerWorkbench.class)
public abstract class ContainerWorkbenchMixin {

    @WrapOperation(
        method = "transferStackInSlot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/ContainerWorkbench;mergeItemStack(Lnet/minecraft/item/ItemStack;IIZ)Z",
            ordinal = 0))
    private boolean gtrh$shiftCraftUnstableIngot(ContainerWorkbench workbench, ItemStack stack, int start, int end,
        boolean reverse, Operation<Boolean> merge) {
        NBTTagCompound tag = stack.getTagCompound();
        if (!Config.moduleEnabled("unstable_ingot_shift_craft") || stack.getItemDamage() != 0
            || tag == null
            || !tag.getBoolean("crafting")
            || tag.hasKey("time")
            || stack.getItem() != GameRegistry.findItem("ExtraUtilities", "unstableingot")) {
            return merge.call(workbench, stack, start, end, reverse);
        }

        stack.setTagCompound(null);
        try {
            return merge.call(workbench, stack, start, end, reverse);
        } finally {
            stack.setTagCompound(tag);
        }
    }
}
