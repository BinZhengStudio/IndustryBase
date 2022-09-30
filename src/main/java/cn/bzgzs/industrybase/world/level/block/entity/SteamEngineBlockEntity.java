package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.world.level.block.entity.ContainerTransmitBlockEntity;
import cn.bzgzs.industrybase.world.inventory.SteamEngineMenu;
import cn.bzgzs.industrybase.world.level.block.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlockEntity extends ContainerTransmitBlockEntity {
	private int power;
	private int burnTime;
	private int shrinkTick;
	public static final int MAX_POWER = 100;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private final FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME * 4, fluidStack -> fluidStack.getFluid() instanceof WaterFluid);
	private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);
	private final ContainerData data = new ContainerData() { // 用于向客户端发送服务端的相关数据
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> power;
				case 1 -> burnTime;
				case 2 -> SteamEngineBlockEntity.this.tank.getFluidAmount();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) { // 这个是让在服务端的Menu中也能更改BlockEntity的数据
			switch (index) {
				case 0 -> power = value;
				case 1 -> burnTime = value;
				// 水量不能更改
				default -> {}
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.STEAM_ENGINE.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		boolean flag = false; // 是否需要setChanged

		if (blockEntity.isLit()) { // 输出能量
			--blockEntity.burnTime; // 减少燃烧时间

			if (!blockEntity.tank.isEmpty()) {
				if (blockEntity.shrinkTick <= 0) { // 消耗水
					blockEntity.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
					blockEntity.shrinkTick = 12; // 每12tick减一次waterAmount，这样水不会少的太快
				} else {
					--blockEntity.shrinkTick;
				}

				if (blockEntity.power < MAX_POWER) { // 增加功率，使之达到最大
					++blockEntity.power;
				}
			}
			flag = true;
		}

		if (!blockEntity.isLit() || !blockEntity.tank.isEmpty()) {
			if (blockEntity.power > 0) {
				--blockEntity.power;
				flag = true;
			}
		}

		if (!blockEntity.isLit() && blockEntity.tank.isEmpty()) { // 如果没有燃烧，并且有水，则消耗燃料并燃烧
			ItemStack stack = blockEntity.inventory.get(0);
			int time = ForgeHooks.getBurnTime(stack, null);
			if (time > 0) {
				stack.shrink(1);
				blockEntity.burnTime = time;
				flag = true;
			}
		}

		if (blockEntity.isLit() != state.getValue(SteamEngineBlock.LIT)) { // 如果燃烧状态与state不符，则更新state
			level.setBlock(pos, state.setValue(SteamEngineBlock.LIT, blockEntity.isLit()), 3);
			flag = true;
		}

		if (flag) {
			blockEntity.setChanged();
		}
	}

	public boolean isLit() { // 是否正在燃烧
		return this.burnTime > 0;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.setPower(1);
		this.setResistance(1);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBase.MODID + ".steam_engine");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new SteamEngineMenu(id, inventory, this, this.data);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null) {
			if (side.getAxis() == this.getBlockState().getValue(SteamEngineBlock.AXIS)) {
				return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.getTransmit().cast() : super.getCapability(cap, side);
			} else {
				return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidHandler.cast() : super.getCapability(cap, side);
			}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public int getContainerSize() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return this.inventory.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int amount) {
		return ContainerHelper.removeItem(this.inventory, index, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(this.inventory, index);
	}

	@Override
	public void setItem(int index, ItemStack itemStack) {
		this.inventory.set(index, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void clearContent() {
		this.inventory.clear();
	}
}
