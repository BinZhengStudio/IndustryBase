package cn.bzgzs.industrybase.client.gui.screens.inventory;

import cn.bzgzs.industrybase.IndustryBase;
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
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBase.MODID, "textures/gui/container/steam_engine.png");

	public SteamEngineScreen(SteamEngineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		MutableComponent speed = Component.translatable("label.steam_engine.speed");
		MutableComponent power = Component.translatable("label.steam_engine.power");
		MutableComponent waterAmount = Component.translatable("label.steam_engine.water_amount");
		this.font.draw(poseStack, speed, this.leftPos + 8, this.topPos + 24, 0x880000);
		this.font.draw(poseStack, power, this.leftPos + 8, this.topPos + 39, 0x826d00);
		this.font.draw(poseStack, waterAmount, this.leftPos + 8, this.topPos + 54, 0x006ee4);
		this.font.draw(poseStack, Float.toString(this.menu.getData().get(1) / 100.0F), this.leftPos + 9 + this.font.width(speed), this.topPos + 24, 0x880000);
		this.font.draw(poseStack, Integer.toString(this.menu.getData().get(0)), this.leftPos + 9 + this.font.width(power), this.topPos + 39, 0x826d00);
		this.font.draw(poseStack, Integer.toString(this.menu.getData().get(4)), this.leftPos + 9 + this.font.width(waterAmount), this.topPos + 54, 0x006ee4);
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
	}
}