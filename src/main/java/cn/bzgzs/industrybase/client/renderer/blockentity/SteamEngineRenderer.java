package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
	public static final ModelLayerLocation MAIN = new ModelLayerLocation(new ResourceLocation(IndustryBase.MODID, "steam_engine"), "main");
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBase.MODID, "textures/entity/steam_engine.png");
	private final ModelPart main;

	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(MAIN);
		this.main = root.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, 6.0F, -8.0F, 4.0F, 4.0F, 16.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition up = main.addOrReplaceChild("up", CubeListBuilder.create().texOffs(0, 48).addBox(-7.0F, 1.0F, -7.0F, 14.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition down = main.addOrReplaceChild("down", CubeListBuilder.create().texOffs(0, 24).addBox(-8.0F, 8.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		main.render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, packedOverlay);
		poseStack.popPose();
	}
}
