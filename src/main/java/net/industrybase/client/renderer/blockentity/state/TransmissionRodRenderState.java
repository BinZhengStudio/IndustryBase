package net.industrybase.client.renderer.blockentity.state;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class TransmissionRodRenderState extends BlockEntityRenderState {
    public Direction.Axis axis = Direction.Axis.X;
    public float rotate = 0.0F;
    @Nullable
    public Identifier texture = null;
    public int rgbColor = 0xFFFFFF;
}
