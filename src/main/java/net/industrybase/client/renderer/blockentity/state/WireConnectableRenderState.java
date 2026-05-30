package net.industrybase.client.renderer.blockentity.state;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class WireConnectableRenderState extends BlockEntityRenderState {
    public Set<BlockPos> wires = new HashSet<>();
}
