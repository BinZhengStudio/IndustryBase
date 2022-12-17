package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import cn.bzgzs.industrybase.world.level.block.IronTransmissionRodBlock;
import cn.bzgzs.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Optional;

public abstract class TextureTransmissionRodRenderer implements BlockEntityRenderer<TransmissionRodBlockEntity> {
	private final ModelPart main;

	public TextureTransmissionRodRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(TransmissionRodRenderer.MAIN);
		this.main = root.getChild("main");
	}

	protected abstract ResourceLocation getTexture();

	@Override
	public void render(TransmissionRodBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		switch (blockEntity.getBlockState().getValue(IronTransmissionRodBlock.AXIS)) {
			case X -> poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			case Z -> poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
		}
		Optional.ofNullable(blockEntity.getLevel()).ifPresent(level -> {
			TransmitNetwork.RotateContext context = TransmitNetwork.Manager.get(blockEntity.getLevel()).getRotateContext(blockEntity.getBlockPos());
			poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.rotLerp(partialTick, (float) context.getOldDegree(), (float) context.getDegree())));
		});
		main.render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTexture())), packedLight, packedOverlay);
		poseStack.popPose();
	}
}