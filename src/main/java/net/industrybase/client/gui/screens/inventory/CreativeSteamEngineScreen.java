package net.industrybase.client.gui.screens.inventory;

import net.industrybase.util.Util;
import net.industrybase.world.inventory.CreativeSteamEngineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CreativeSteamEngineScreen extends AbstractContainerScreen<CreativeSteamEngineMenu> {
    private static final Identifier TEXTURE = Util.withNamespace(
            "textures/gui/container/steam_engine.png");

    public CreativeSteamEngineScreen(CreativeSteamEngineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);
        // 渲染水量指示
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 149, this.topPos + 9, 176, 0, 17, 65, 256,
                256);
        // 渲染燃料燃烧进度
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 70, this.topPos + 24, 176, 66, 14, 14, 256,
                256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        super.extractLabels(graphics, xm, ym);
        graphics.text(this.font, Component.translatable("label.steam_engine.water_amount", 2000), this.leftPos + 8,
                this.topPos + 39, 0x006ee4, false);
    }
}
