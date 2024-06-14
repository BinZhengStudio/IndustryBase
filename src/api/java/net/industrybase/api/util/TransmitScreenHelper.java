package net.industrybase.api.util;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TransmitScreenHelper {
	public static final int PANEL_WIDTH = 80;
	public static final int PANEL_HEIGHT = 90;
	private static final ResourceLocation PANEL_TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/gui/container/transmit_panel.png");

	public static void renderTransmitPanel(GuiGraphics graphics, int leftPos, int topPos, int imageWidth) {
		graphics.blit(PANEL_TEXTURE, leftPos + imageWidth, topPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
	}

	public static void renderTransmitFont(GuiGraphics graphics, Font font, int speed, int power, int leftPos, int topPos, int imageWidth) {
		leftPos += imageWidth + 6;
		topPos += 6;
		graphics.drawString(font, Component.translatable("label.transmit_panel.speed", speed / 100.0F), leftPos, topPos, 0x880000, false);
		graphics.drawString(font, Component.translatable("label.transmit_panel.power", power), leftPos, topPos + 12, 0x826d00, false);
	}
}
