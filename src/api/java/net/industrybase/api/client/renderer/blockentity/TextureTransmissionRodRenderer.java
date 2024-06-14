package net.industrybase.api.client.renderer.blockentity;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 带有自定义材质的传动杆。
 * 主要用于木质、石质传动杆的渲染。
 */
public abstract class TextureTransmissionRodRenderer implements BlockEntityRenderer<TransmissionRodBlockEntity> {
	private final ModelPart main;

	public TextureTransmissionRodRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(TransmissionRodRenderer.MAIN);
		this.main = root.getChild("main");
	}

	protected abstract ResourceLocation getTexture();

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void render(TransmissionRodBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		TransmissionRodRenderer.subscribeSpeed(blockEntity);
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		TransmissionRodRenderer.rodRotate(blockEntity, partialTick, poseStack);
		main.render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTexture())), packedLight, packedOverlay);
		poseStack.popPose();
	}
}
