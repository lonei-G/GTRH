package com.loneig.gtrh.module.processingarray;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.loneig.gtrh.GTRH;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.IIconContainer;

public final class ProcessingArrayTextures {

    public static final IIconContainer OVERLAY_FRONT_PROCESSING_ARRAY = new CustomIcon(
        "iconsets/OVERLAY_FRONT_PROCESSING_ARRAY");
    public static final IIconContainer OVERLAY_FRONT_PROCESSING_ARRAY_GLOW = new CustomIcon(
        "iconsets/OVERLAY_FRONT_PROCESSING_ARRAY_GLOW");
    public static final IIconContainer OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE = new CustomIcon(
        "iconsets/OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE");
    public static final IIconContainer OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW = new CustomIcon(
        "iconsets/OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW");

    private ProcessingArrayTextures() {}

    public static void init() {}

    private static final class CustomIcon implements IIconContainer, Runnable {

        private final String iconName;
        private IIcon icon;

        private CustomIcon(String iconName) {
            this.iconName = iconName;
            GregTechAPI.sGTBlockIconload.add(this);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon() {
            return icon;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getOverlayIcon() {
            return null;
        }

        @Override
        public void run() {
            icon = GregTechAPI.sBlockIcons.registerIcon(GTRH.MODID + ":" + iconName);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ResourceLocation getTextureFile() {
            return TextureMap.locationBlocksTexture;
        }
    }
}
