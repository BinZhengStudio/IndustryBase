package net.industrybase.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.industrybase.world.level.block.entity.CreativeSteamEngineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class CreativeSteamEngineRenderer implements BlockEntityRenderer<CreativeSteamEngineBlockEntity> {
	public CreativeSteamEngineRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(CreativeSteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		SteamEngineRenderer.renderWater(this, 1.0F, blockEntity, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	private void vertex(VertexConsumer consumer, Matrix4f matrix4f, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
		consumer.addVertex(matrix4f, pX, pY, pZ).setColor(pRed, pGreen, pBlue, alpha).setUv(pU, pV).setLight(pPackedLight).setNormal(0.0F, 1.0F, 0.0F);
	}

}
