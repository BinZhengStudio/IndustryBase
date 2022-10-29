package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.Preference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class TransmitScreenHelper {
	public static final int PANEL_WIDTH = 80;
	public static final int PANEL_HEIGHT = 90;
	private static final ResourceLocation PANEL_TEXTURE = new ResourceLocation(Preference.MODID, "textures/gui/container/transmit_panel.png");
	public static final MutableComponent SPEED = Component.translatable("label.transmit_panel.speed");
	public static final MutableComponent POWER = Component.translatable("label.transmit_panel.power");

	public static void renderTransmitPanel(Screen screen, PoseStack poseStack, int leftPos, int topPos, int imageWidth) {
		RenderSystem.setShaderTexture(0, PANEL_TEXTURE);
		screen.blit(poseStack, leftPos + imageWidth, topPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
	}

	public static void renderTransmitFont(Font font, PoseStack poseStack, int speed, int power, int leftPos, int topPos, int imageWidth) {
		leftPos += imageWidth + 6;
		topPos += 6;
		font.draw(poseStack, SPEED, leftPos, topPos, 0x880000);
		font.draw(poseStack, POWER, leftPos, topPos + 12, 0x826d00);
		font.draw(poseStack, Float.toString(speed / 100.0F), leftPos + font.width(SPEED), topPos, 0x880000);
		font.draw(poseStack, Integer.toString(power), leftPos + font.width(POWER), topPos + 12, 0x826d00);
	}
}
