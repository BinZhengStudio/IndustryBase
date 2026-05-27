package net.industrybase.world.level.block.entity;

import org.jspecify.annotations.Nullable;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.api.transmit.TransmitNetwork;
import net.industrybase.network.server.WaterAmountPayload;
import net.industrybase.world.inventory.SteamEngineMenu;
import net.industrybase.world.level.block.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SteamEngineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	private int burnTime;
	private int totalBurnTime;
	private int shrinkTick;
	private boolean subscribed = false;
	public static final int MAX_POWER = 100;
	public static final int MAX_WATER = FluidType.BUCKET_VOLUME * 2;
	private static final AABB AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	private static final FluidResource WATER_RESOURCE = FluidResource.of(Fluids.WATER);
    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, MAX_WATER) {
        public boolean isValid(int index, FluidResource resource) {
            return index == 0 && resource.is(Fluids.WATER);
        };

        protected void onContentsChanged(int index, FluidStack previousContents) {
			if (level != null && !level.isClientSide()) {
				setChanged();
				// send packet to sync the fluid amount
				PacketDistributor.sendToAllPlayers(new WaterAmountPayload(worldPosition, this.getAmountAsInt(index)));
				for (Direction direction : Direction.values()) {
					if (direction == Direction.UP) {
						handler.setPressure(direction, 0.0D);
					} else {
						handler.setPressure(direction,
                                this.getAmountAsInt(index) * 0.5D / this.getCapacity(index, FluidResource.EMPTY));
					}
				}
			}
        };
	};
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private int oldWaterAmount;
	private int waterAmount; // 仅在客户端调用
	private final ContainerData data = new ContainerData() { // 用于双端同步数据
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> SteamEngineBlockEntity.this.transmit.getPower();
				case 1 -> (int) (SteamEngineBlockEntity.this.transmit.getSpeed() * 100);
				case 2 -> burnTime;
				case 3 -> totalBurnTime;
				case 4 -> tank.getAmountAsInt(0);
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> SteamEngineBlockEntity.this.transmit.setPower(value);
				case 2 -> burnTime = value;
				case 3 -> totalBurnTime = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 5;
		}
	};

    public SteamEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

	public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.STEAM_ENGINE.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		blockEntity.oldWaterAmount = blockEntity.waterAmount;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		boolean flag = false; // 是否有数据改变

		if (blockEntity.isLit()) { // 输出能量
			--blockEntity.burnTime; // 减少燃烧时间

			if (blockEntity.tank.getAmountAsInt(0) > 0) {
				if (blockEntity.shrinkTick <= 0) { // 消耗水
                    try (var tx = Transaction.openRoot()) {
                        blockEntity.tank.extract(0, WATER_RESOURCE, 1, tx);
                        tx.commit();
                    }
					blockEntity.shrinkTick = 6; // 每 6tick 减一次 waterAmount，这样水不会少的太快
				} else {
					--blockEntity.shrinkTick;
				}
				if (blockEntity.transmit.getPower() < MAX_POWER) { // 增加功率，使之达到最大
					blockEntity.transmit.setPower(blockEntity.transmit.getPower() + 1);
				}
			} else {
				if (blockEntity.transmit.getPower() > 0) {
					blockEntity.transmit.setPower(blockEntity.transmit.getPower() - 1);
				}
			}
			flag = true;
		} else {
			if (blockEntity.transmit.getPower() > 0) {
				blockEntity.transmit.setPower(blockEntity.transmit.getPower() - 1);
				flag = true;
			}
		}

		if (!blockEntity.isLit() && blockEntity.tank.getAmountAsInt(0) > 0) { // 如果没有燃烧，并且有水，则消耗燃料并燃烧
			ItemStack stack = blockEntity.inventory.getFirst();
			int time = stack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
			if (time > 0) {
				flag = true;
				blockEntity.burnTime = time;
				blockEntity.totalBurnTime = time;

                ItemStackTemplate remainder = stack.getCraftingRemainder();
                stack.shrink(1);
                if (stack.isEmpty()) {
                    blockEntity.inventory.set(0, remainder != null ? remainder.create() : ItemStack.EMPTY);
                }
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

	public static boolean isFuel(ItemStack stack, Level level) {
		return stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setResistance(10);
		this.handler.registerHandler(AABB, new StorageInterface(
                () -> this.tank.getAmountAsInt(0),
                () -> this.tank.getCapacityAsInt(0, WATER_RESOURCE),
                (resource, simulate) -> {
                    try (var tx = Transaction.openRoot()) {
                        int result = this.tank.insert(0, FluidResource.of(resource), resource.getAmount(), tx);
                        if (!simulate) tx.commit();
                        return result;
                    }
                },
                (resource, simulate) -> {
                    try (var rx = Transaction.openRoot()) {
                        int result = this.tank.extract(0, FluidResource.of(resource), resource.getAmount(), rx);
                        if (!simulate) rx.commit();
                        return resource.copyWithAmount(result);
                    }
                }));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBaseApi.MODID + ".steam_engine");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.inventory = items;
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new SteamEngineMenu(id, inventory, this, this.data);
	}

	public int getFluidAmount() {
		return this.tank.getAmountAsInt(0);
	}

	public int getWaterAmount() {
		return this.waterAmount;
	}

	public int getOldWaterAmount() {
		return this.oldWaterAmount;
	}

	public void setClientWaterAmount(int waterAmount) {
		this.oldWaterAmount = this.waterAmount;
		this.waterAmount = waterAmount;
	}

	public boolean isSubscribed() {
		return this.subscribed;
	}

	public void setSubscribed() {
		this.subscribed = true;
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side.getAxis() == this.getBlockState().getValue(SteamEngineBlock.AXIS)) {
			return this.transmit;
		}
		return null;
	}

	@Nullable
	public FluidStacksResourceHandler getTank(Direction side) {
		if (side.getAxis() != this.getBlockState().getValue(SteamEngineBlock.AXIS)) {
			return this.tank;
		}
		return null;
	}

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("Transmit", this.transmit);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.burnTime = input.getIntOr("BurnTime", 0);
        this.totalBurnTime = input.getIntOr("TotalBurnTime", 0);
        this.shrinkTick = input.getIntOr("ShrinkTick", 0);
        input.readChild("Fluid", this.tank);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Transmit", this.transmit);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("BurnTime", this.burnTime);
        output.putInt("TotalBurnTime", this.totalBurnTime);
        output.putInt("ShrinkTick", this.shrinkTick);
        output.putChild("Fluid", this.tank);
    }

	@Override
	public void setRemoved() {
		this.transmit.remove();
		this.handler.removeHandler();
		super.setRemoved();
	}

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState blockState) {
        var oldState = this.getBlockState();
        super.setBlockState(blockState);
        if (oldState.getValue(SteamEngineBlock.AXIS) != blockState.getValue(SteamEngineBlock.AXIS)) {
            TransmitNetwork.Manager.get(level).addOrChangeBlock(this.worldPosition, this::invalidateCapabilities);
        }
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

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return isFuel(stack, this.level) || stack.is(Items.BUCKET);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
	}
}
