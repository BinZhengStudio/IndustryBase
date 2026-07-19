package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.util.Util;
import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StoneTransmissionRodBlock extends TransmissionRodBlock {
	public static final Identifier TEXTURE = Util.withNamespace(
            "transmission_rod/stone");
    public static final MapCodec<StoneTransmissionRodBlock> CODEC = simpleCodec(StoneTransmissionRodBlock::new);

	public StoneTransmissionRodBlock(Properties properties) {
		super(properties.noOcclusion(), 10);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }
}
