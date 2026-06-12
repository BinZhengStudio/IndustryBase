package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IronTransmissionRodBlock extends TransmissionRodBlock {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID,
            "transmission_rod/iron");
	public static final MapCodec<IronTransmissionRodBlock> CODEC = simpleCodec(IronTransmissionRodBlock::new);

	public IronTransmissionRodBlock(Properties properties) {
		super(properties.strength(3.0F).noOcclusion(), 20);
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
