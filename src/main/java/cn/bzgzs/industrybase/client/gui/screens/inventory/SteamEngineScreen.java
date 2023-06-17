package cn.bzgzs.industrybase.client.gui.screens.inventory;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.util.TransmitScreenHelper;
import cn.bzgzs.industrybase.world.inventory.SteamEngineMenu;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SteamEngineScreen extends AbstractContainerScreen<SteamEngineMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/gui/container/steam_engine.png");

	public SteamEngineScreen(SteamEngineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		// 渲染水量的文字
		graphics.drawString(this.font, Component.translatable("label.steam_engine.water_amount", this.menu.getData().get(4)), this.leftPos + 8, this.topPos + 39, 0x006ee4, false);
		// 渲染信息面板的内容
		TransmitScreenHelper.renderTransmitFont(graphics, this.font, this.menu.getData().get(1), this.menu.getData().get(0), this.leftPos, this.topPos, this.imageWidth);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		int waterAmount = this.menu.getData().get(4);
		if (waterAmount > 0) { // 渲染水量指示
			int textureHeight = (int) (64.0F * waterAmount / SteamEngineBlockEntity.MAX_WATER);
			graphics.blit(TEXTURE, this.leftPos + 149, this.topPos + 73 - textureHeight, 176, 64 - textureHeight, 17, textureHeight + 1);
		}
		int burnTime = this.menu.getData().get(2);
		if (burnTime > 0) { // 渲染燃料燃烧进度
			int totalBurnTime = this.menu.getData().get(3) > 0 ? this.menu.getData().get(3) : 200;
			int textureHeight = (int) (13.0F * burnTime / totalBurnTime);
			graphics.blit(TEXTURE, this.leftPos + 70, this.topPos + 37 - textureHeight, 176, 79 - textureHeight, 14, textureHeight + 1);
		}
		// 在右侧渲染一个用于显示传动网络信息的面板
		TransmitScreenHelper.renderTransmitPanel(graphics, this.leftPos, this.topPos, this.imageWidth);
	}
}
