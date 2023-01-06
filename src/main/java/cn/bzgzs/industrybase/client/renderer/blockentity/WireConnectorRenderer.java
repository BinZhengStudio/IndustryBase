package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.electric.ElectricNetwork;
import cn.bzgzs.industrybase.world.level.block.entity.WireConnectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Optional;

public class WireConnectorRenderer implements BlockEntityRenderer<WireConnectorBlockEntity> {
	public WireConnectorRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(WireConnectorBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void render(WireConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		Optional.ofNullable(blockEntity.getLevel()).ifPresent(level -> {
			ElectricNetwork network = ElectricNetwork.Manager.get(level);
			network.wireConnects(blockEntity.getBlockPos()).forEach(pos -> this.renderWire(blockEntity, blockEntity.getBlockPos(), pos, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
		});
	}

	private void renderWire(WireConnectorBlockEntity blockEntity, BlockPos from, BlockPos to, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, int pPackedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.475D, 0.5D);
		Vec3 start = Vec3.atCenterOf(from);
		Vec3 end = Vec3.atCenterOf(to);
		float totalX = (float) (end.x - start.x);
		float totalY = (float) (end.y - start.y);
		float totalZ = (float) (end.z - start.z);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();
		float f4 = Mth.fastInvSqrt(totalX * totalX + totalZ * totalZ) * 0.025F;
		float f5 = totalZ * f4;
		float f6 = totalX * f4;
		int fromLightLevel = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, from);
		int toLightLevel = blockEntity.getLevel().getBrightness(LightLayer.BLOCK, to);
		int fromSkyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, from);
		int toSkyLight = blockEntity.getLevel().getBrightness(LightLayer.SKY, to);

		for (int i = 0; i <= 24; ++i) { // 一共 25 段
			addVertexPair(consumer, matrix4f, totalX, totalY, totalZ, fromLightLevel, toLightLevel, fromSkyLight,
					toSkyLight, 0.05F, 0.05F, f5, f6, i);
		}

		for (int j = 24; j >= 0; --j) { // 再来一次
			addVertexPair(consumer, matrix4f, totalX, totalY, totalZ, fromLightLevel, toLightLevel, fromSkyLight,
					toSkyLight, 0.05F, 0.0F, f5, f6, j);
		}

		poseStack.popPose();
	}

	private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix4f, float totalX, float totalY,
									  float totalZ, int fromLightLevel, int toLightLevel, int fromSkyLight,
									  int toSkyLight, float p_174317_, float p_174318_, float widthX, float widthZ,
									  int index) {
		float delta = (float) index / 24.0F;
		int lightLevel = (int) Mth.lerp(delta, (float) fromLightLevel, (float) toLightLevel);
		int brightness = (int) Mth.lerp(delta, (float) fromSkyLight, (float) toSkyLight);
		int packedLight = LightTexture.pack(lightLevel, brightness);
		float red = 0.1F;
		float green = 0.1F;
		float blue = 0.1F;
		float x = totalX * delta;
		float y = totalY > 0.0F ? totalY * delta * delta : totalY - totalY * (1.0F - delta) * (1.0F - delta);
		float z = totalZ * delta;
		consumer.vertex(matrix4f, x - widthX, y + p_174318_, z + widthZ).color(red, green, blue, 1.0F).uv2(packedLight).endVertex();
		consumer.vertex(matrix4f, x + widthX, y + p_174317_ - p_174318_, z - widthZ).color(red, green, blue, 1.0F).uv2(packedLight).endVertex();
	}
}
