package cn.bzgzs.industrybase.client.gui.screens.inventory;

import cn.bzgzs.industrybase.api.Preference;
import cn.bzgzs.industrybase.api.util.TransmitScreenHelper;
import cn.bzgzs.industrybase.world.inventory.SteamEngineMenu;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SteamEngineScreen extends AbstractContainerScreen<SteamEngineMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Preference.MODID, "textures/gui/container/steam_engine.png");

	public SteamEngineScreen(SteamEngineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos -= TransmitScreenHelper.PANEL_WIDTH / 2;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		MutableComponent speed = Component.translatable("label.steam_engine.speed");
		MutableComponent power = Component.translatable("label.steam_engine.power");
		MutableComponent waterAmount = Component.translatable("label.steam_engine.water_amount");
		this.font.draw(poseStack, waterAmount, this.leftPos + 8, this.topPos + 39, 0x006ee4);
		this.font.draw(poseStack, Integer.toString(this.menu.getData().get(4)), this.leftPos + 9 + this.font.width(waterAmount), this.topPos + 39, 0x006ee4);
		TransmitScreenHelper.renderTransmitFont(this.font, poseStack, this.menu.getData().get(1), this.menu.getData().get(0), this.leftPos, this.topPos, this.imageWidth);
		this.renderTooltip(poseStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		int waterAmount = this.menu.getData().get(4);
		if (waterAmount > 0) {
			int textureHeight = (int) (64.0F * waterAmount / SteamEngineBlockEntity.MAX_WATER);
			this.blit(poseStack, this.leftPos + 149, this.topPos + 73 - textureHeight, 176, 64 - textureHeight, 17, textureHeight + 1);
		}
		int burnTime = this.menu.getData().get(2);
		if (burnTime > 0) {
			int totalBurnTime = this.menu.getData().get(3) > 0 ? this.menu.getData().get(3) : 200;
			int textureHeight = (int) (13.0F * burnTime / totalBurnTime);
			this.blit(poseStack, this.leftPos + 70, this.topPos + 37 - textureHeight, 176, 79 - textureHeight, 14, textureHeight + 1);
		}
		TransmitScreenHelper.renderTransmitPanel(this, poseStack, this.leftPos, this.topPos, this.imageWidth);
	}
}
