package net.industrybase.api.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.industrybase.api.electric.IWireConnectable;
import net.industrybase.api.network.client.SubscribeWireConnPacket;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

public class WireConnectableRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
	private AABB aabb = null;

	public WireConnectableRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(T blockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public AABB getRenderBoundingBox(T blockEntity) {
//		return AABB.INFINITE;
		if (this.aabb == null) {
			BlockPos pos = blockEntity.getBlockPos();
			this.aabb = new AABB(pos.offset(-256, -256, -256).getCenter(), pos.offset(256, 256, 256).getCenter());
		}
		return this.aabb;
	}

	@Override
	public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		if (blockEntity instanceof IWireConnectable connectable) {
			if (blockEntity.hasLevel() && !connectable.isSubscribed()) {
				PacketDistributor.sendToServer(new SubscribeWireConnPacket(blockEntity.getBlockPos()));
				connectable.setSubscribed();
			}
			connectable.getWires().forEach(pos -> this.renderWire(blockEntity, blockEntity.getBlockPos(), pos, poseStack, bufferSource));
		}
	}

	private void renderWire(T blockEntity, BlockPos from, BlockPos to, PoseStack poseStack, MultiBufferSource bufferSource) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		Vec3 start = Vec3.atCenterOf(from);
		Vec3 end = Vec3.atCenterOf(to);
		float totalX = (float) (end.x - start.x);
		float totalY = (float) (end.y - start.y);
		float totalZ = (float) (end.z - start.z);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();
		float width = 0.05F; // 导线粗细
		float square = totalX * totalX + totalZ * totalZ;
		float horizonDistance = Mth.sqrt(totalX * totalX + totalZ * totalZ);
		boolean vertical = square == 0;
		// 在添加顶点时，相同宽度会被添加两次，因此需要除以 2
		float size = Mth.invSqrt(square) * width / 2.0F;
		float widthY = width / 2.0F;
		float widthX = vertical ? width / 2 : totalZ * size; // 在导线竖直下垂时，size = 0，导线不渲染，因此需要分类讨论
		float widthZ = vertical ? width / 2 : totalX * size;

		Level level = blockEntity.getLevel();
		if (level == null) return;
		int fromLightLevel = level.getBrightness(LightLayer.BLOCK, from);
		int toLightLevel = level.getBrightness(LightLayer.BLOCK, to);
		int fromSkyLight = level.getBrightness(LightLayer.SKY, from);
		int toSkyLight = level.getBrightness(LightLayer.SKY, to);

		for (int i = 0; i <= 24; ++i) { // 一共 25 段
			addVertexPair(consumer, matrix4f, totalX, totalY, totalZ, horizonDistance, fromLightLevel, toLightLevel, fromSkyLight,
					toSkyLight, widthY, widthX, widthZ, i);
		}

		for (int j = 24; j >= 0; --j) { // 再来一次，以渲染两个相互交叉的面
			// 需要将 widthY 取相反数，否则生成的面与原有面重合
			addVertexPair(consumer, matrix4f, totalX, totalY, totalZ, horizonDistance, fromLightLevel, toLightLevel, fromSkyLight,
					toSkyLight, -widthY, vertical ? -widthX : widthX, widthZ, j);
		}

		poseStack.popPose();
	}

	private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix4f, float totalX, float totalY,
									  float totalZ, float horizonDistance, int fromLightLevel, int toLightLevel, int fromSkyLight,
									  int toSkyLight, float widthY, float widthX, float widthZ, int index) {
		float delta = (float) index / 24.0F;
		int lightLevel = (int) Mth.lerp(delta, (float) fromLightLevel, (float) toLightLevel);
		int brightness = (int) Mth.lerp(delta, (float) fromSkyLight, (float) toSkyLight);
		int packedLight = LightTexture.pack(lightLevel, brightness);
		float red = 0.1F;
		float green = 0.1F;
		float blue = 0.1F;
		float f = 0.08F; // 下垂量，经测试，0.08 下垂较为自然
		// 用的是二次函数
		float y = totalY > 0.0F ? totalY * delta * delta : totalY - totalY * (1.0F - delta) * (1.0F - delta);
		// 增加下垂量，并使水平连接的导线也有下垂效果
		y += delta > 0.5F ? horizonDistance * f * (delta - 0.5F) * (delta - 0.5F) : horizonDistance * f * (0.5F - delta) * (0.5F - delta);
		// 纠正导线位置
		y -= horizonDistance * f * 0.5F * 0.5F;
		float x = totalX * delta;
		float z = totalZ * delta;
		consumer.addVertex(matrix4f, x - widthX, y + widthY, z + widthZ).setColor(red, green, blue, 1.0F).setLight(packedLight);
		consumer.addVertex(matrix4f, x + widthX, y - widthY, z - widthZ).setColor(red, green, blue, 1.0F).setLight(packedLight);
	}
}
