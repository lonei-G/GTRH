package com.loneig.gtrh.mixin;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.loneig.gtrh.module.ModifiedRecipes;

import gregtech.api.recipe.RecipeMapFrontend;
import gregtech.nei.RecipeDisplayInfo;

@Mixin(value = RecipeMapFrontend.class, remap = false)
public class RecipeMapFrontendMixin {

    @Inject(method = "drawDescription", at = @At("TAIL"))
    private void gtrh$showModifiedBy(RecipeDisplayInfo recipeInfo, CallbackInfo ci) {
        if (!ModifiedRecipes.isModified(recipeInfo.recipe)) return;
        recipeInfo.drawText(EnumChatFormatting.GOLD + StatCollector.translateToLocal("gtrh.nei.modified"));
    }
}
