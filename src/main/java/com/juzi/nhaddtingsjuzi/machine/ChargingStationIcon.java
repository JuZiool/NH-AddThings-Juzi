package com.juzi.nhaddtingsjuzi.machine;

import gregtech.api.interfaces.IIconContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Minimal IIconContainer for a single custom block-atlas texture path.
 * Replaces removed Textures.BlockIcons.CustomIcon on GT 5.09.52.
 */
public final class ChargingStationIcon implements IIconContainer {
    private final String path;
    private IIcon icon;

    public ChargingStationIcon(String path) {
        this.path = path;
    }

    @SideOnly(Side.CLIENT)
    public void register(TextureMap map) {
        icon = map.registerIcon(path);
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
    public ResourceLocation getTextureFile() {
        return TextureMap.locationBlocksTexture;
    }
}
