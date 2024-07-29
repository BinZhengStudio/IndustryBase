package net.industrybase.api.tags;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class BlockTagList {
	public static final TagKey<Block> TRANSMISSION_ROD = create("transmission_rod");
	public static final TagKey<Block> METAL_TRANSMISSION_ROD = create("metal_transmission_rod");
	public static final TagKey<Block> WOOD_TRANSMISSION_ROD = create("wood_transmission_rod");
	public static final TagKey<Block> PIPE = create("pipe");

	private BlockTagList() {
	}

	private static TagKey<Block> create(String name) {
		return BlockTags.create(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, name));
	}
}
