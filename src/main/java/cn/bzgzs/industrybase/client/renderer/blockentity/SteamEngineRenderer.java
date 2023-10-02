package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import org.joml.Matrix4f;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
	public static final ModelLayerLocation MAIN = new ModelLayerLocation(new ResourceLocation(IndustryBaseApi.MODID, "steam_engine"), "main");
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/fake_fluid.png");
	private final ModelPart main;

	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(MAIN);
		this.main = root.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		int waterAmount = blockEntity.getWaterAmount();
		if (waterAmount <= 0) return; // 如果没水就没有渲染的必要了
		poseStack.pushPose();
		renderWater((float) waterAmount / SteamEngineBlockEntity.MAX_WATER, blockEntity, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	public static void renderWater(float waterAmount, BlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		BlockPos pos = blockEntity.getBlockPos();
		AABB box = blockEntity.getRenderBoundingBox();
		poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		Matrix4f matrix4f = poseStack.last().pose();

		float minX = (float) box.minX + 0.06252F;
		float minY = (float) box.minY + 0.5F;
		float minZ = (float) box.minZ + 0.06252F;
		float maxX = (float) box.maxX - 0.06252F;
		float maxY = (float) box.minY + 0.5F + 0.43748F * waterAmount;
		float maxZ = (float) box.maxZ - 0.06252F;

		FlowingFluid fluid = Fluids.WATER;
		TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(blockEntity.getLevel(), pos, fluid.defaultFluidState());
		VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluid.defaultFluidState()));
		float u0 = sprites[0].getU(1.0D);
		float u1 = sprites[0].getU(15.0D);
		float v0 = sprites[0].getV(1.0D);
		float v1 = sprites[0].getV(15.0D);
		float u01 = sprites[1].getU(1.0D);
		float u11 = sprites[1].getU(8.0D);
		float v01 = sprites[1].getV(8.0D - 7.0D * waterAmount);
		float v11 = sprites[1].getV(8.0D);
		int fluidColor = IClientFluidTypeExtensions.of(ForgeMod.WATER_TYPE.get()).getTintColor(); // 获取水的颜色
		float red = (float)(fluidColor >> 16 & 255) / 255.0F;
		float green = (float)(fluidColor >> 8 & 255) / 255.0F;
		float blue = (float)(fluidColor & 255) / 255.0F;
		float alpha = (float)(fluidColor >> 24 & 255) / 255.0F;

		// Up
		buffer.vertex(matrix4f, minX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u0, v0)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u0, v1)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u1, v1)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u1, v0)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();

		// West
		buffer.vertex(matrix4f, minX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u01, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, minY, minZ)
				.color(red, green, blue, alpha)
				.uv(u01, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, minY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u11, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u11, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();

		// North
		buffer.vertex(matrix4f, maxX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u01, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, minY, minZ)
				.color(red, green, blue, alpha)
				.uv(u01, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, minY, minZ)
				.color(red, green, blue, alpha)
				.uv(u11, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u11, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();

		// South
		buffer.vertex(matrix4f, minX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u01, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, minX, minY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u01, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, minY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u11, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u11, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();

		// East
		buffer.vertex(matrix4f, maxX, maxY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u01, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, minY, maxZ)
				.color(red, green, blue, alpha)
				.uv(u01, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, minY, minZ)
				.color(red, green, blue, alpha)
				.uv(u11, v11)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		buffer.vertex(matrix4f, maxX, maxY, minZ)
				.color(red, green, blue, alpha)
				.uv(u11, v01)
				.uv2(packedLight)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
	}
}
