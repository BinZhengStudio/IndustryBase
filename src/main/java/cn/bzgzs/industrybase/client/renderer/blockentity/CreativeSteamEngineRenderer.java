package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.world.level.block.entity.CreativeSteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;

public class CreativeSteamEngineRenderer implements BlockEntityRenderer<CreativeSteamEngineBlockEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/fake_fluid.png");
	private final ModelPart main;

	public CreativeSteamEngineRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(SteamEngineRenderer.MAIN);
		this.main = root.getChild("main");
	}

	@Override
	public void render(CreativeSteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		// 渲染一个透明小立方体，代替水
		poseStack.pushPose();
		// 使透明立方体居中并比透明容器略小
		poseStack.translate(0.06252D, 0.5001D, 0.06252D);
		poseStack.scale(0.8746F, 0.4365F, 0.8746F);
		int fluidColor = IClientFluidTypeExtensions.of(ForgeMod.WATER_TYPE.get()).getTintColor(); // 获取水的颜色
		float red = (float)(fluidColor >> 16 & 255) / 255.0F;
		float green = (float)(fluidColor >> 8 & 255) / 255.0F;
		float blue = (float)(fluidColor & 255) / 255.0F;
		float alpha = (float)(fluidColor >> 24 & 255) / 255.0F;
		// 执行渲染
		this.main.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}
}
