package com.loneig.gtrh.mixin;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.loneig.gtrh.module.disassembler.ReversedRecipeRegistry;

import bartworks.util.BWUtil;

@Mixin(value = BWUtil.class, remap = false)
public class BWUtilMixin {

    @Inject(
        method = "createGTCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Lnet/minecraftforge/oredict/ShapedOreRecipe;",
        at = @At("RETURN"))
    private static void gtrh$captureShaped(ItemStack result, long bitMask, Object[] recipe,
        CallbackInfoReturnable<ShapedOreRecipe> cir) {
        if (cir.getReturnValue() != null) {
            ReversedRecipeRegistry.registerShaped(result, recipe);
        }
    }
}
