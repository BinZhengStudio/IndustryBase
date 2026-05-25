package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StoneTransmissionRodBlock extends TransmissionRodBlock {
	public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID,
            "textures/entity/transmission_rod/stone.png");
    public static final MapCodec<StoneTransmissionRodBlock> CODEC = simpleCodec((properties) -> new StoneTransmissionRodBlock());

	public StoneTransmissionRodBlock() {
		super(Properties.ofFullCopy(Blocks.STONE).noOcclusion(), 10);
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
