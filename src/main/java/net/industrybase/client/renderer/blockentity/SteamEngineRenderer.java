package net.industrybase.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.industrybase.network.client.RequestWaterAmountPayload;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		requestWaterAmount(blockEntity);
		int oldWaterAmount = blockEntity.getOldWaterAmount();
		int waterAmount = blockEntity.getWaterAmount();
		if (oldWaterAmount <= 0 && waterAmount <= 0) return; // 如果没水就没有渲染的必要了
		poseStack.pushPose();
		renderWater(this, Mth.lerp(partialTick, oldWaterAmount, waterAmount) / SteamEngineBlockEntity.MAX_WATER, blockEntity, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	private static void requestWaterAmount(SteamEngineBlockEntity blockEntity) {
		if (blockEntity.hasLevel() && !blockEntity.isSubscribed()) {
			PacketDistributor.sendToServer(new RequestWaterAmountPayload(blockEntity.getBlockPos()));
			blockEntity.setSubscribed();
		}
	}

	public static <T extends BlockEntity> void renderWater(BlockEntityRenderer<T> renderer, float waterAmount, T blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		BlockPos pos = blockEntity.getBlockPos();
		AABB box = renderer.getRenderBoundingBox(blockEntity);
		poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		Matrix4f matrix4f = poseStack.last().pose();

		float minX = (float) box.minX + 0.0635F; // 应为 0.0625，0.0635 是为兼容铷
		float minY = (float) box.minY + 0.5F;
		float minZ = (float) box.minZ + 0.0635F;
		float maxX = (float) box.maxX - 0.0635F;
		float maxY = (float) box.minY + 0.5F + 0.4365F * waterAmount; // 应为 0.4375，0.4365 是为兼容铷
		float maxZ = (float) box.maxZ - 0.0635F;

		FlowingFluid fluid = Fluids.WATER;
		TextureAtlasSprite[] sprites = FluidSpriteCache.getFluidSprites(blockEntity.getLevel(), pos, fluid.defaultFluidState());
		VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluid.defaultFluidState()));
		float u0 = sprites[0].getU(1.0F / 16.0F);
		float u1 = sprites[0].getU(15.0F / 16.0F);
		float v0 = sprites[0].getV(1.0F / 16.0F);
		float v1 = sprites[0].getV(15.0F / 16.0F);
		float u01 = sprites[1].getU(1.0F / 16.0F);
		float u11 = sprites[1].getU(8.0F / 16.0F);
		float v01 = sprites[1].getV((8.0F - 7.0F * waterAmount) / 16.0F);
		float v11 = sprites[1].getV(8.0F / 16.0F);
		int fluidColor = IClientFluidTypeExtensions.of(NeoForgeMod.WATER_TYPE.value()).getTintColor(); // 获取水的颜色
		float red = (float)(fluidColor >> 16 & 255) / 255.0F;
		float green = (float)(fluidColor >> 8 & 255) / 255.0F;
		float blue = (float)(fluidColor & 255) / 255.0F;
		float alpha = (float)(fluidColor >> 24 & 255) / 255.0F;

		// Up
		buffer.addVertex(matrix4f, minX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u0, v0)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u0, v1)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u1, v1)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u1, v0)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);

		// West
		buffer.addVertex(matrix4f, minX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, minY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, minY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);

		// North
		buffer.addVertex(matrix4f, maxX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, minY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, minY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);

		// South
		buffer.addVertex(matrix4f, minX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, minX, minY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, minY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);

		// East
		buffer.addVertex(matrix4f, maxX, maxY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, minY, maxZ)
				.setColor(red, green, blue, alpha)
				.setUv(u01, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, minY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v11)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
		buffer.addVertex(matrix4f, maxX, maxY, minZ)
				.setColor(red, green, blue, alpha)
				.setUv(u11, v01)
				.setLight(packedLight)
				.setNormal(0.0F, 1.0F, 0.0F);
	}
}
