package net.industrybase.api.client.renderer.blockentity;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class TransmissionRodRenderer implements BlockEntityRenderer<TransmissionRodBlockEntity> {
	public static final ModelLayerLocation MAIN = new ModelLayerLocation(new ResourceLocation(IndustryBaseApi.MODID, "transmission_rod"), "main");
	private static final ResourceLocation LAYER_1 = new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/layer_1.png");
	private static final ResourceLocation LAYER_2 = new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/layer_2.png");
	private final ModelPart main;

	public TransmissionRodRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(MAIN);
		this.main = root.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void render(TransmissionRodBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		rodRotate(blockEntity, partialTick, poseStack); // 设置传动杆旋转
		if (blockEntity.getBlockState().getBlock() instanceof LayeredTransmissionRodBlock block) {
			main.render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(LAYER_1)), packedLight, packedOverlay);
			// 发光部分渲染
			main.render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(LAYER_2)), LightTexture.pack(15, 15), packedOverlay, block.getRed(), block.getGreen(), block.getBlue(), 1.0F);
		}
		poseStack.popPose();
	}

	public static void rodRotate(TransmissionRodBlockEntity blockEntity, float partialTick, PoseStack poseStack) {
		// 按照轴旋转模型
		switch (blockEntity.getBlockState().getValue(TransmissionRodBlock.AXIS)) {
			case X -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			case Z -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		}
		Level level = blockEntity.getLevel();
		if (level != null) {
			TransmitClientNetwork.RotateContext context = blockEntity.getRotate();
			// 根据速度设定传动杆的旋转角度
			poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, context.getOldDegree(), context.getDegree())));
		}
	}
}
