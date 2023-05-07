package cn.bzgzs.industrybase.client.renderer;

import cn.bzgzs.industrybase.api.client.renderer.BlockEntityItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityAsItemRenderer extends BlockEntityItemRenderer {
	public static final BlockEntityAsItemRenderer INSTANCE = new BlockEntityAsItemRenderer();

	@Override
	public void beforeRender(ItemStack stack, BlockItem item, BaseEntityBlock block, BlockState state, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
	}

	@Override
	public void afterRender(ItemStack stack, BlockItem item, BaseEntityBlock block, BlockState state, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
	}
}
