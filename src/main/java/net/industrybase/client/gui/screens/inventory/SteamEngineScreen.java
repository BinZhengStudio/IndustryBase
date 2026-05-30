package net.industrybase.client.gui.screens.inventory;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.inventory.SteamEngineMenu;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SteamEngineScreen extends AbstractContainerScreen<SteamEngineMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID,
            "textures/gui/container/steam_engine.png");

    public SteamEngineScreen(SteamEngineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);

        int waterAmount = this.menu.getData().get(4);
        if (waterAmount > 0) { // 渲染水量指示
            int textureHeight = (int) (64.0F * waterAmount / SteamEngineBlockEntity.MAX_WATER);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 149, this.topPos + 73 - textureHeight,
                    176, 64 - textureHeight, 17, textureHeight + 1, 256, 256);
        }

        int burnTime = this.menu.getData().get(2);
        if (burnTime > 0) { // 渲染燃料燃烧进度
            int totalBurnTime = this.menu.getData().get(3) > 0 ? this.menu.getData().get(3) : 200;
            int textureHeight = (int) (13.0F * burnTime / totalBurnTime);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 70, this.topPos + 37 - textureHeight,
                    176, 79 - textureHeight, 14, textureHeight + 1, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        super.extractLabels(graphics, xm, ym);
        graphics.text(this.font, Component.translatable("label.steam_engine.water_amount", this.menu.getData().get(4)),
                this.leftPos + 8, this.topPos + 39, 0x006ee4, false);
    }
}
