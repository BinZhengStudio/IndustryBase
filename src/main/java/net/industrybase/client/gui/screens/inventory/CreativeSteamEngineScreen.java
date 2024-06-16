package net.industrybase.client.gui.screens.inventory;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.util.TransmitScreenHelper;
import net.industrybase.world.inventory.CreativeSteamEngineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CreativeSteamEngineScreen extends AbstractContainerScreen<CreativeSteamEngineMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/gui/container/steam_engine.png");

	public CreativeSteamEngineScreen(CreativeSteamEngineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos -= TransmitScreenHelper.PANEL_WIDTH / 2;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		// 先渲染默认背景，即使屏幕变暗
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		// 渲染水量的文字
		graphics.drawString(this.font, Component.translatable("label.steam_engine.water_amount", 2000), this.leftPos + 8, this.topPos + 39, 0x006ee4, false);
		// 渲染信息面板的内容
		TransmitScreenHelper.renderTransmitFont(graphics, this.font, this.menu.getData().get(0), 100, this.leftPos, this.topPos, this.imageWidth);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		// 渲染水量指示
		graphics.blit(TEXTURE, this.leftPos + 149, this.topPos + 9, 176, 0, 17, 65);
		// 渲染燃料燃烧进度
		graphics.blit(TEXTURE, this.leftPos + 70, this.topPos + 24, 176, 66, 14, 14);
		// 在右侧渲染一个用于显示传动网络信息的面板
		TransmitScreenHelper.renderTransmitPanel(graphics, this.leftPos, this.topPos, this.imageWidth);
	}
}
