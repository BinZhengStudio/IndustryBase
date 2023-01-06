package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
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
		// 渲染一个透明小立方体，代替水
		poseStack.pushPose();
		// 使透明立方体居中并比透明容器略小
		poseStack.translate(0.06252D, 0.5001D, 0.06252D);
		poseStack.scale(0.8746F, 0.4365F * waterAmount / SteamEngineBlockEntity.MAX_WATER, 0.8746F);
		int fluidColor = IClientFluidTypeExtensions.of(ForgeMod.WATER_TYPE.get()).getTintColor(); // 获取水的颜色
		float red = (float)(fluidColor >> 16 & 255) / 255.0F;
		float green = (float)(fluidColor >> 8 & 255) / 255.0F;
		float blue = (float)(fluidColor & 255) / 255.0F;
		float alpha = (float)(fluidColor >> 24 & 255) / 255.0F;
		// 执行渲染
		this.main.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();

//		renderBoundingBox(poseStack, blockEntity);
	}

	public static void renderBoundingBox(PoseStack poseStack, SteamEngineBlockEntity blockEntity) { // 测试与学习顶点着色器用
		AABB box = blockEntity.getRenderBoundingBox();
		BlockPos pos = blockEntity.getBlockPos();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		poseStack.pushPose();

		poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());

		float minX = (float) box.minX;
		float minY = (float) box.minY;
		float minZ = (float) box.minZ;
		float maxX = (float) box.maxX;
		float maxY = (float) box.maxY;
		float maxZ = (float) box.maxZ;

		Matrix4f mat = poseStack.last().pose();
		float b = 1;
		buffer.vertex(mat, minX, minY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();

		buffer.vertex(mat, maxX, minY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();

		b = 0.5f;
		buffer.vertex(mat, minX, minY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, minY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();

		b = 1;
		buffer.vertex(mat, minX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();

		b = 0.8f;
		buffer.vertex(mat, minX, minY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, minZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, minY, minZ).color(0, 0, b, 0.5f).endVertex();

		buffer.vertex(mat, minX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, minY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, maxX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();
		buffer.vertex(mat, minX, maxY, maxZ).color(0, 0, b, 0.5f).endVertex();

		tessellator.end();
		RenderSystem.depthMask(true);
		poseStack.popPose();
	}
}
