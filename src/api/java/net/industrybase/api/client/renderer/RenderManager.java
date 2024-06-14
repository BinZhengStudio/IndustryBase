package net.industrybase.api.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class RenderManager implements ResourceManagerReloadListener {
	private final FluidRenderer fluidRenderer;

	public RenderManager() {
		this.fluidRenderer = new FluidRenderer();
	}

	public void renderFluid(BlockPos pos, BlockAndTintGetter level, VertexConsumer consumer, BlockState blockState, FluidState fluidState, boolean applyBiomeColor) {
		try {
			this.fluidRenderer.render(level, pos, consumer, blockState, fluidState, applyBiomeColor);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering fluid in world");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Fluid being rendered");
			CrashReportCategory.populateBlockDetails(crashreportcategory, level, pos, null);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		this.fluidRenderer.setup();
	}
}
