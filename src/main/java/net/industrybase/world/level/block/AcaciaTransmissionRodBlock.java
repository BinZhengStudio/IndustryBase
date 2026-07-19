package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.util.Util;
import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AcaciaTransmissionRodBlock extends WoodTransmissionRod {
    public static final Identifier TEXTURE = Util.withNamespace(
            "transmission_rod/acacia");
	public static final MapCodec<AcaciaTransmissionRodBlock> CODEC = simpleCodec(AcaciaTransmissionRodBlock::new);

	public AcaciaTransmissionRodBlock(Properties properties) {
		super(properties);
	}

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
