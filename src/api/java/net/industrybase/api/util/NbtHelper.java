package net.industrybase.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.IntArrayTag;

import java.util.Optional;

public class NbtHelper {
	public static Optional<BlockPos> readBlockPos(IntArrayTag tag) {
		int[] array = tag.getAsIntArray();
		return array.length == 3 ? Optional.of(new BlockPos(array[0], array[1], array[2])) : Optional.empty();
	}

    public static IntArrayTag writeBlockPos(BlockPos pos) {
        return new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }
}
