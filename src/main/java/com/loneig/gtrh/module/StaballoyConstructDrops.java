package com.loneig.gtrh.module;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.kuba6000.mobsinfo.api.MobDrop;
import com.kuba6000.mobsinfo.api.event.MobNEIRegistrationEvent;
import com.kuba6000.mobsinfo.api.event.PreMobRegistrationEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gtPlusPlus.core.material.MaterialsAlloy;
import kubatech.loaders.MobHandlerLoader;

public final class StaballoyConstructDrops {

    private static final String MOB_NAME = "miscutils.constructStaballoy";

    @SubscribeEvent
    public void restoreDrops(PreMobRegistrationEvent event) {
        if (!MOB_NAME.equals(event.currentMob)) return;

        ItemStack flower = new ItemStack(Blocks.yellow_flower);
        ItemStack ingots = MaterialsAlloy.STABALLOY.getIngot(4);
        event.drops.removeIf(drop -> drop.stack.isItemEqual(flower) || drop.stack.isItemEqual(ingots));

        event.drops.add(MobDrop.create(MaterialsAlloy.STABALLOY.getBlock(1)));
        event.drops.add(
            MobDrop.create(ingots)
                .withLooting());
        event.drops.add(
            MobDrop.create(MaterialsAlloy.STABALLOY.getPlate(2))
                .withChance(6666)
                .withLooting());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void addRecipeLabel(MobNEIRegistrationEvent event) {
        if (!MOB_NAME.equals(event.mobName) || !MobHandlerLoader.recipeMap.containsKey(MOB_NAME)) return;

        event.additionalInformation.add(
            EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.BOLD
                + StatCollector.translateToLocal("gtrh.nei.modified"));
    }
}
