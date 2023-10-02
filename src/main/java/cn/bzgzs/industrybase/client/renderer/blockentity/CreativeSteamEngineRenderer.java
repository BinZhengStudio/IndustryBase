package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.world.level.block.entity.CreativeSteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class CreativeSteamEngineRenderer implements BlockEntityRenderer<CreativeSteamEngineBlockEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/fake_fluid.png");
	private final ModelPart main;

	public CreativeSteamEngineRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(SteamEngineRenderer.MAIN);
		this.main = root.getChild("main");
	}

	@Override
	public void render(CreativeSteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		SteamEngineRenderer.renderWater(1.0F, blockEntity, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	private void vertex(VertexConsumer consumer, Matrix4f matrix4f, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
		consumer.vertex(matrix4f, pX, pY, pZ).color(pRed, pGreen, pBlue, alpha).uv(pU, pV).uv2(pPackedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

}
