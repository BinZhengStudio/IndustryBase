package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.client.renderer.BlockEntityAsItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class BlockEntityItem extends BlockItem {
	public BlockEntityItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return BlockEntityAsItemRenderer.INSTANCE;
			}
		});
	}
}
