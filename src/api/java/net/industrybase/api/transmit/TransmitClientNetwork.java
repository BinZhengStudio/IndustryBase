package net.industrybase.api.transmit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.transmit.network.client.SpeedSubscribePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class TransmitClientNetwork extends TransmitNetwork {
	private final HashMultiset<BlockPos> requiredSyncRoot;
	private final HashMultimap<BlockPos, BlockPos> serverRootReverse; // server root to client roots
	private final HashMap<BlockPos, BlockPos> serverRoot; // client root to server root
	private final HashMap<BlockPos, RotateContext> rotates;

	public TransmitClientNetwork(LevelAccessor level) {
		super(level);
		if (!level.isClientSide()) throw new IllegalArgumentException("server level can't be used in client transmit network");
		this.requiredSyncRoot = HashMultiset.create();
		this.serverRootReverse = HashMultimap.create();
		this.rotates = new HashMap<>();
		this.serverRoot = new HashMap<>();
	}

	public BlockPos serverRoot(BlockPos pos) {
		return this.serverRoot.getOrDefault(this.root(pos), pos);
	}

	public void requireSpeedSync(BlockPos pos) {
		BlockPos root = this.root(pos.immutable());
		this.requiredSyncRoot.add(root);
		if (this.requiredSyncRoot.count(root) <= 1) { // if the network never subscribed before
			ApiNetworkManager.sendToServer(new SpeedSubscribePacket(root, false));
		}
	}

	public void removeSpeedSync(BlockPos pos) {
		BlockPos root = this.root(pos);
		this.requiredSyncRoot.remove(root);
		if (this.requiredSyncRoot.count(root) <= 0) { // if not other block requires sync
			ApiNetworkManager.sendToServer(new SpeedSubscribePacket(root, true));
			BlockPos serverRoot = this.serverRoot.remove(root);
			this.speeds.remove(serverRoot);
			this.serverRootReverse.remove(serverRoot, root);
		}
	}

	public void setClientRootAndSpeed(BlockPos root, BlockPos serverRoot, float speed) {
		this.setClientRoot(root, serverRoot);
		this.updateClientSpeed(serverRoot, speed);
	}

	public void setClientRoot(BlockPos root, BlockPos serverRoot) {
		if (root.equals(serverRoot)) {
			this.serverRoot.remove(root);
			this.serverRootReverse.remove(serverRoot, root);
		} else {
			this.serverRoot.put(root, serverRoot);
			this.serverRootReverse.put(serverRoot, root);
		}
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
		}
	}

	public void updateClientRoots(Collection<BlockPos> targets, BlockPos root) {
		if (this.level.isClientSide()) {
			targets.forEach(target -> {
				if (!target.equals(root)) {
					this.serverRoot.put(target, root);
				} else {
					this.serverRoot.remove(target);
				}
			});
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
	protected void afterSplit(BlockPos primaryRoot, BlockPos secondaryRoot) {
		BlockPos serverRoot = this.serverRoot.get(primaryRoot);
		this.serverRoot.put(secondaryRoot, serverRoot);
		this.serverRootReverse.put(serverRoot, secondaryRoot);
	}

	@Override
	protected void afterMerge(BlockPos primaryRoot, BlockPos secondaryRoot) {
		// TODO
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
			this.oldDegree = this.degree % 360.0F;
			this.degree = this.degree + (speed * 18.0F) % 360.0F;
		}

		public float getOldDegree() {
			return this.oldDegree;
		}

		public float getDegree() {
			return this.degree;
		}
	}
}
