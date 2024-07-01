package net.industrybase.api.transmit;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.transmit.network.client.SpeedSubscribePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TransmitClientNetwork extends TransmitNetwork {
	private final HashMultimap<BlockPos, BlockPos> requiredSyncRoot; // client root to blocks
	private final HashMultimap<BlockPos, BlockPos> serverRootReverse; // server root to client roots
	private final HashMap<BlockPos, BlockPos> serverRoot; // client root to server root
	private final HashMap<BlockPos, RotateContext> rotates;

	public TransmitClientNetwork(LevelAccessor level) {
		super(level);
		if (!level.isClientSide()) throw new IllegalArgumentException("server level can't be used in client transmit network");
		this.requiredSyncRoot = HashMultimap.create();
		this.serverRootReverse = HashMultimap.create();
		this.rotates = new HashMap<>();
		this.serverRoot = new HashMap<>();
	}

	public BlockPos serverRoot(BlockPos pos) {
		return this.serverRoot.getOrDefault(this.root(pos), pos);
	}

	public void requireSpeedSync(BlockPos pos) {
		BlockPos root = this.root(pos.immutable());
		this.requiredSyncRoot.put(root, pos);
		if (this.requiredSyncRoot.get(root).size() <= 1) { // if the network never subscribed before
			ApiNetworkManager.sendToServer(new SpeedSubscribePacket(root, false));
		}
	}

	public void removeSpeedSync(BlockPos pos) {
		BlockPos root = this.root(pos);
		if (this.requiredSyncRoot.remove(root, pos)) {
			if (this.requiredSyncRoot.containsKey(root)) { // if not other block requires sync
				ApiNetworkManager.sendToServer(new SpeedSubscribePacket(root, true));
				BlockPos serverRoot = this.serverRoot.remove(root);
				this.speeds.remove(serverRoot);
				this.serverRootReverse.remove(serverRoot, root);
			}
		}
	}

	public void setClientRootAndSpeed(BlockPos root, BlockPos serverRoot, float speed) {
		this.setClientRoot(root, serverRoot);
		this.updateClientSpeed(serverRoot, speed);
	}

	public void setClientRoot(BlockPos root, BlockPos serverRoot) {
		this.serverRoot.put(root, serverRoot);
		this.serverRootReverse.put(serverRoot, root);
	}

	public void updateClientRoot(BlockPos oldServerRoot, BlockPos newServerRoot) {
		Iterator<BlockPos> roots = this.serverRootReverse.get(oldServerRoot).iterator();
		while (roots.hasNext()) {
			BlockPos root = roots.next();
			this.serverRoot.put(root, newServerRoot);
			this.serverRootReverse.put(newServerRoot, root);
			roots.remove();
		}
	}

	public void updateClientSpeed(BlockPos serverRoot, float speed) {
		if (speed > 0.0F) {
			this.speeds.put(serverRoot, speed);
		} else {
			this.speeds.remove(serverRoot);
			this.rotates.get(serverRoot).stop();
		}
	}

	public void afterServerSpilt(BlockPos serverRoot) {
		this.serverRootReverse.get(serverRoot).forEach(root ->
				ApiNetworkManager.sendToServer(new SpeedSubscribePacket(root, false)));
	}

	@Override
	protected void updateSpeed(BlockPos root) {
	}

	@Override
	protected void forEachSecondaryPos(BlockPos primaryRoot, BlockPos secondaryRoot, BlockPos pos) {
		Set<BlockPos> primarySet = this.requiredSyncRoot.get(primaryRoot);
		if (primarySet.contains(pos)) {
			primarySet.remove(pos);
			this.requiredSyncRoot.put(secondaryRoot, pos);
		}
	}

	@Override
	protected void afterSplit(BlockPos primaryRoot, BlockPos secondaryRoot) {
		BlockPos serverRoot = this.serverRoot.get(primaryRoot);
		this.serverRoot.put(secondaryRoot, serverRoot);
		this.serverRootReverse.put(serverRoot, secondaryRoot);
	}

	@Override
	protected void afterMerge(BlockPos primaryRoot, BlockPos secondaryRoot) {
		this.requiredSyncRoot.putAll(primaryRoot, this.requiredSyncRoot.removeAll(secondaryRoot));
		this.serverRootReverse.remove(this.serverRoot.remove(secondaryRoot), secondaryRoot);
	}

	public RotateContext getRotateContext(BlockPos pos) {
		BlockPos root = this.serverRoot(pos);
		return this.rotates.getOrDefault(root, RotateContext.NULL);
	}

	protected void tick() {
		super.tick();
		this.speeds.forEach((serverRoot, speed) -> {
			RotateContext context = this.rotates.computeIfAbsent(serverRoot, blockPos -> new RotateContext(0.0F, 0.0F));
			context.update(speed);
		});
	}

	public static class RotateContext {
		public static final RotateContext NULL = new RotateContext(0.0F, 0.0F);
		private float oldDegree;
		private float degree;

		public RotateContext(float oldDegree, float degree) {
			this.oldDegree = oldDegree;
			this.degree = degree;
		}

		public void update(float speed) {
			this.oldDegree = this.degree;
			this.degree = (degree + (speed * 18.0F)) % 360.0F;
		}

		public void stop() {
			this.oldDegree = this.degree;
		}

		public float getOldDegree() {
			return this.oldDegree;
		}

		public float getDegree() {
			return this.degree;
		}
	}
}
