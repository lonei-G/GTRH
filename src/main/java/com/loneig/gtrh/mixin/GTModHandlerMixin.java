package com.loneig.gtrh.mixin;

import java.util.function.Predicate;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.module.disassembler.ReversedRecipeRegistry;

import gregtech.api.util.GTModHandler;

@Mixin(value = GTModHandler.class, remap = false)
public class GTModHandlerMixin {

    @Inject(method = "addCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Z", at = @At("RETURN"))
    private static void gtrh$captureShaped(ItemStack result, long bitMask, Object[] recipe,
        CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            ReversedRecipeRegistry.registerShaped(result, recipe);
        }
    }

    @Inject(
        method = "addCraftingRecipe(Lnet/minecraft/item/ItemStack;[Lnet/minecraft/enchantment/Enchantment;[I[Ljava/lang/Object;)Z",
        at = @At("RETURN"))
    private static void gtrh$captureShapedWithEnchantments(ItemStack result, Enchantment[] enchantments,
        int[] enchantmentLevels, Object[] recipe, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            ReversedRecipeRegistry.registerShaped(result, recipe);
        }
    }

    @Inject(
        method = "addShapelessCraftingRecipe(Lnet/minecraft/item/ItemStack;JLjava/util/function/Predicate;[Ljava/lang/Object;)Z",
        at = @At("RETURN"))
    private static void gtrh$captureShapeless(ItemStack result, long bitMask, Predicate<InventoryCrafting> validator,
        Object[] recipe, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            ReversedRecipeRegistry.registerShapeless(result, recipe);
        }
    }
}
