package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.data.ModelData;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.translate(0.0625D, 0.0625D, 0.0625D); // TODO 需要自己造轮子
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.WATER.defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, RenderType.translucent());
		poseStack.popPose();
	}
}
