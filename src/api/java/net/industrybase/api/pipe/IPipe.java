package net.industrybase.api.pipe;

import net.minecraft.core.Direction;

public interface IPipe {
    boolean connected(Direction direction);
}
