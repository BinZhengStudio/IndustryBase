package net.industrybase.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockEntityItemRenderer extends BlockEntityWithoutLevelRenderer {
	private final Map<BlockState, BlockEntity> temp = new HashMap<>();

	public BlockEntityItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		this.temp.clear();
	}

	public abstract void beforeRender(ItemStack stack, BlockItem item, BaseEntityBlock block, BlockState state,
									  ItemDisplayContext transformType, PoseStack poseStack,
									  MultiBufferSource bufferSource, int packedLight, int packedOverlay);

	public abstract void afterRender(ItemStack stack, BlockItem item, BaseEntityBlock block, BlockState state,
									  ItemDisplayContext transformType, PoseStack poseStack,
									  MultiBufferSource bufferSource, int packedLight, int packedOverlay);

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		BlockEntity blockEntity;
		if (stack.getItem() instanceof BlockItem item) {
			if (item.getBlock() instanceof BaseEntityBlock block) {
				BlockState state = block.defaultBlockState();
				blockEntity = this.temp.computeIfAbsent(state, (blockState) -> block.newBlockEntity(BlockPos.ZERO, blockState));
				poseStack.pushPose();
				this.beforeRender(stack, item, block, state, transformType, poseStack, bufferSource, packedLight, packedOverlay);
				Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
				this.afterRender(stack, item, block, state, transformType, poseStack, bufferSource, packedLight, packedOverlay);
				poseStack.popPose();
			}
		}
	}
}
