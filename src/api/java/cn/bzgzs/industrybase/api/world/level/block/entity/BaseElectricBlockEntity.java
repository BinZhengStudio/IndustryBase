package cn.bzgzs.industrybase.api.world.level.block.entity;

import cn.bzgzs.industrybase.api.util.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseElectricBlockEntity extends BlockEntity {
	@Nullable
	private ElectricNetwork network;
	private int tmpPower;
	private int tmpResistance;
//	private final LazyOptional<IElectricPower> electricPower = LazyOptional.of(() -> new IElectricPower() {
//		@Override
//		public double getOutputPower() {
//			return BaseElectricBlockEntity.this.getOutput();
//		}
//
//		@Override
//		public double getInputPower() {
//			return BaseElectricBlockEntity.this.getInput();
//		}
//	});

	public BaseElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	public double getOutput() {
		return this.network.getMachineOutput(this.worldPosition);
	}

	protected void setOutput(int output) {
		this.network.setMachineOutput(this.worldPosition, output);
	}

	public double getInput() {
		return this.network.getMachineInput(this.worldPosition);
	}

	protected void setInput(int input) {
		this.network.setMachineInput(this.worldPosition, input);
	}

//	public LazyOptional<IElectricPower> getElectricPower() {
//		return this.electricPower;
//	}

	public ElectricNetwork getNetwork() {
		return this.network;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.tmpPower = tag.getInt("MechanicalPower");
		this.tmpResistance = tag.getInt("MechanicalResistance");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putDouble("MechanicalPower", this.network.getMachineOutput(this.worldPosition));
		tag.putDouble("MechanicalResistance", this.network.getMachineInput(this.worldPosition));
	}

	@Override
	public void setLevel(@NotNull Level level) {
		super.setLevel(level);
		this.network = ElectricNetwork.Manager.get(level);
	}

	@Override
	public void onLoad() {
		if (this.level != null && !this.level.isClientSide) {
			this.setOutput(this.tmpPower);
			this.setInput(this.tmpResistance);
			network.addOrChangeBlock(this.worldPosition, this::setChanged);
		}
		super.onLoad();
	}

	@Override
	public void onChunkUnloaded() {
		if (this.level != null && !this.level.isClientSide) {
			this.setOutput(0);
			this.setInput(0);
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide) {
			this.setOutput(0);
			this.setInput(0);
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}
}
