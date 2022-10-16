package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.world.level.block.SteamEngineBlock;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.data.ModelData;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
	public static final ModelLayerLocation MAIN = new ModelLayerLocation(new ResourceLocation(IndustryBase.MODID, "steam_engine"), "main");
	private static final ResourceLocation TEXTURE_XZ = new ResourceLocation(IndustryBase.MODID, "textures/entity/steam_engine/steam_engine_xz.png");
	private static final ResourceLocation TEXTURE_LIT_XZ = new ResourceLocation(IndustryBase.MODID, "textures/entity/steam_engine/steam_engine_lit_xz.png");
	private static final ResourceLocation TEXTURE_Y = new ResourceLocation(IndustryBase.MODID, "textures/entity/steam_engine/steam_engine_y.png");
	private static final ResourceLocation TEXTURE_LIT_Y = new ResourceLocation(IndustryBase.MODID, "textures/entity/steam_engine/steam_engine_lit_y.png");
	private final ModelPart main;
	private final ModelPart rod;

	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(MAIN);
		this.main = root.getChild("main");
		this.rod = root.getChild("rod");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rod = partdefinition.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(48, 8).addBox(-3.0F, -11.0F, -8.0F, 6.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition up = main.addOrReplaceChild("up", CubeListBuilder.create().texOffs(0, 48).addBox(-7.0F, -8.0F, -7.0F, 14.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition down = main.addOrReplaceChild("down", CubeListBuilder.create().texOffs(0, 24).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		boolean lit = blockEntity.getBlockState().getValue(SteamEngineBlock.LIT);
		switch (blockEntity.getBlockState().getValue(SteamEngineBlock.AXIS)) {
			case X -> {
				RenderType type = RenderType.entityCutout(lit ? TEXTURE_LIT_XZ : TEXTURE_XZ);
				poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
				main.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
				rod.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
			}
			case Y -> {
				RenderType type = RenderType.entityCutout(lit ? TEXTURE_LIT_Y : TEXTURE_Y);
				main.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
				poseStack.translate(0.0F, 0.5F, -0.5F);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				rod.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
			}
			case Z -> {
				RenderType type = RenderType.entityCutout(lit ? TEXTURE_LIT_XZ : TEXTURE_XZ);
				main.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
				rod.render(poseStack, bufferSource.getBuffer(type), packedLight, packedOverlay);
			}
		}
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.translate(0.0625D, 0.0625D, 0.0625D); // TODO 需要自己造轮子
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.WATER.defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, RenderType.translucent());
		poseStack.popPose();
	}
}
