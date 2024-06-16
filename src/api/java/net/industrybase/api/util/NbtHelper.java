package net.industrybase.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;

import java.util.Optional;

public class NbtHelper {
	public static Optional<BlockPos> readBlockPos(IntArrayTag tag) {
		int[] array = tag.getAsIntArray();
		return array.length == 3 ? Optional.of(new BlockPos(array[0], array[1], array[2])) : Optional.empty();
	}
}
