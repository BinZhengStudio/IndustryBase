package cn.bzgzs.industrybase.api.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class FluidRenderer {
	private final TextureAtlasSprite waterOverlay;

	public FluidRenderer() {
		this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
	}

	private static boolean isNeighborSameFluid(FluidState firstState, FluidState secondState) {
		return secondState.getType().isSame(firstState.getType());
	}

	/**
	 * 是否被旁边的方块挡住
	 * @param pLevel TODO
	 * @param pPos TODO
	 * @param pSide TODO
	 * @param height 流体方块高度
	 * @param state TODO
	 * @return TODO
	 */
	private static boolean isFaceOccludedByNeighbor(BlockGetter pLevel, BlockPos pPos, Direction pSide, float height, BlockState state) {
		return isFaceOccludedByState(pLevel, pSide, height, pPos.relative(pSide), state);
	}

	private static boolean isFaceOccludedByState(BlockGetter pLevel, Direction pFace, float pHeight, BlockPos pPos, BlockState pState) {
		if (pState.canOcclude()) {
			VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, pHeight, 1.0D);
			VoxelShape voxelshape1 = pState.getOcclusionShape(pLevel, pPos);
			return Shapes.blockOccudes(voxelshape, voxelshape1, pFace);
		} else {
			return false;
		}
	}

	private float getFluidBlockHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
		if (fluid.isSame(fluidState.getType())) {
			BlockState blockstate = level.getBlockState(pos.above());
			return fluid.isSame(blockstate.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
		} else {
			return !blockState.getMaterial().isSolid() ? 0.0F : -1.0F;
		}
	}

	private float getFluidBlockHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos) {
		BlockState blockstate = level.getBlockState(pos);
		return this.getFluidBlockHeight(level, fluid, pos, blockstate, blockstate.getFluidState());
	}

	private void addWeightedHeight(float[] p_203189_, float fluidHeight) {
		if (fluidHeight >= 0.8F) {
			p_203189_[0] += fluidHeight * 10.0F;
			p_203189_[1] += 10.0F;
		} else if (fluidHeight >= 0.0F) {
			p_203189_[0] += fluidHeight;
			p_203189_[1] += 1.0F;
		}
	}

	/**
	 * 计算当前流体与相邻流体的平均高度
	 * @param level TODO
	 * @param fluid TODO
	 * @param fluidHeight TODO
	 * @param neighborHeight TODO
	 * @param neighborHeight1 TODO
	 * @param pos TODO
	 * @return TODO
	 */
	private float calculateAverageHeight(BlockAndTintGetter level, Fluid fluid, float fluidHeight, float neighborHeight, float neighborHeight1, BlockPos pos) {
		if (!(neighborHeight1 >= 1.0F) && !(neighborHeight >= 1.0F)) {
			float[] afloat = new float[2];
			if (neighborHeight1 > 0.0F || neighborHeight > 0.0F) {
				float height = this.getFluidBlockHeight(level, fluid, pos);
				if (height >= 1.0F) {
					return 1.0F;
				}

				this.addWeightedHeight(afloat, height);
			}

			this.addWeightedHeight(afloat, fluidHeight);
			this.addWeightedHeight(afloat, neighborHeight1);
			this.addWeightedHeight(afloat, neighborHeight);
			return afloat[0] / afloat[1];
		} else {
			return 1.0F;
		}
	}

	private int getLightColor(BlockAndTintGetter level, BlockPos pos) {
		int lightColor = LevelRenderer.getLightColor(level, pos);
		int lightColorAbove = LevelRenderer.getLightColor(level, pos.above());
		int k = lightColor & 255;
		int l = lightColorAbove & 255;
		int i1 = lightColor >> 16 & 255;
		int j1 = lightColorAbove >> 16 & 255;
		return Math.max(k, l) | Math.max(i1, j1) << 16;
	}

	public void tesselate(BlockAndTintGetter level, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		TextureAtlasSprite[] textureAtlasSprites = ForgeHooksClient.getFluidSprites(level, pos, fluidState); // 第一个值是静止材质，第二个是流动材质
		int biomeColor = IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, level, pos);
		float red = (float)(biomeColor >> 16 & 255) / 255.0F;
		float green = (float)(biomeColor >> 8 & 255) / 255.0F;
		float blue = (float)(biomeColor & 255) / 255.0F;
		float alpha = (float)(biomeColor >> 24 & 255) / 255.0F;
		BlockState downBlockstate = level.getBlockState(pos.relative(Direction.DOWN));
		FluidState downFluidstate = downBlockstate.getFluidState();
		BlockState upBlockstate = level.getBlockState(pos.relative(Direction.UP));
		FluidState upFluidstate = upBlockstate.getFluidState();
		BlockState northBlockstate = level.getBlockState(pos.relative(Direction.NORTH));
		FluidState northFluidstate = northBlockstate.getFluidState();
		BlockState southBlockstate = level.getBlockState(pos.relative(Direction.SOUTH));
		FluidState southFluidstate = southBlockstate.getFluidState();
		BlockState westBlockstate = level.getBlockState(pos.relative(Direction.WEST));
		FluidState westFluidstate = westBlockstate.getFluidState();
		BlockState eastBlockstate = level.getBlockState(pos.relative(Direction.EAST));
		FluidState eastFluidstate = eastBlockstate.getFluidState();
		boolean neighborSameFluid = !isNeighborSameFluid(fluidState, upFluidstate);
		boolean shouldRenderDown = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.DOWN, downFluidstate)
				&& !isFaceOccludedByNeighbor(level, pos, Direction.DOWN, 0.8888889F, downBlockstate);
		boolean shouldRenderNorth = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.NORTH, northFluidstate);
		boolean shouldRenderSouth = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.SOUTH, southFluidstate);
		boolean shouldRenderWest = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.WEST, westFluidstate);
		boolean shouldRenderEast = LiquidBlockRenderer.shouldRenderFace(level, pos, fluidState, blockState, Direction.EAST, eastFluidstate);
		if (neighborSameFluid || shouldRenderDown || shouldRenderEast || shouldRenderWest || shouldRenderNorth || shouldRenderSouth) {
			float downShade = level.getShade(Direction.DOWN, true);
			float upShade = level.getShade(Direction.UP, true);
			float northShade = level.getShade(Direction.NORTH, true);
			float westShade = level.getShade(Direction.WEST, true);
			Fluid fluid = fluidState.getType();
			float fluidBlockHeight = this.getFluidBlockHeight(level, fluid, pos, blockState, fluidState);
			float northEastAverageHeight;
			float northWestAverageHeight;
			float southEastAverageHeight;
			float southWestAverageHeight;
			if (fluidBlockHeight >= 1.0F) {
				northEastAverageHeight = 1.0F;
				northWestAverageHeight = 1.0F;
				southEastAverageHeight = 1.0F;
				southWestAverageHeight = 1.0F;
			} else {
				float northFluidHeight = this.getFluidBlockHeight(level, fluid, pos.north(), northBlockstate, northFluidstate);
				float southFluidHeight = this.getFluidBlockHeight(level, fluid, pos.south(), southBlockstate, southFluidstate);
				float eastFluidHeight = this.getFluidBlockHeight(level, fluid, pos.east(), eastBlockstate, eastFluidstate);
				float westFluidHeight = this.getFluidBlockHeight(level, fluid, pos.west(), westBlockstate, westFluidstate);
				northEastAverageHeight = this.calculateAverageHeight(level, fluid, fluidBlockHeight, northFluidHeight, eastFluidHeight, pos.relative(Direction.NORTH).relative(Direction.EAST));
				northWestAverageHeight = this.calculateAverageHeight(level, fluid, fluidBlockHeight, northFluidHeight, westFluidHeight, pos.relative(Direction.NORTH).relative(Direction.WEST));
				southEastAverageHeight = this.calculateAverageHeight(level, fluid, fluidBlockHeight, southFluidHeight, eastFluidHeight, pos.relative(Direction.SOUTH).relative(Direction.EAST));
				southWestAverageHeight = this.calculateAverageHeight(level, fluid, fluidBlockHeight, southFluidHeight, westFluidHeight, pos.relative(Direction.SOUTH).relative(Direction.WEST));
			}

			double d1 = pos.getX() & 15; // TODO WTF ??
			double d2 = pos.getY() & 15;
			double d0 = pos.getZ() & 15;
			float f17 = shouldRenderDown ? 0.001F : 0.0F;

			// 渲染上方材质
			if (neighborSameFluid && !isFaceOccludedByNeighbor(level, pos, Direction.UP,
					Math.min(Math.min(northWestAverageHeight, southWestAverageHeight), Math.min(southEastAverageHeight, northEastAverageHeight)), upBlockstate)) {
				northEastAverageHeight -= 0.001F; // TODO WTF ??
				northWestAverageHeight -= 0.001F;
				southEastAverageHeight -= 0.001F;
				southWestAverageHeight -= 0.001F;
				Vec3 flowVec = fluidState.getFlow(level, pos); // 流体流动方向
				float u;
				float u1;
				float u2;
				float u21;
				float v;
				float v2;
				float v21;
				float v1;
				if (flowVec.x == 0.0D && flowVec.z == 0.0D) { // 如果在水平方向没有流动（竖直流下）
					TextureAtlasSprite stillTexture = textureAtlasSprites[0];
					u = stillTexture.getU(0.0D);
					u1 = u;
					v = stillTexture.getV(0.0D);
					v1 = v;
					u2 = stillTexture.getU(16.0D);
					u21 = u2;
					v2 = stillTexture.getV(16.0D);
					v21 = v2;
				} else { // 水平流动的 UV
					TextureAtlasSprite flowTexture = textureAtlasSprites[1];
					float f26 = (float) Mth.atan2(flowVec.z, flowVec.x) - ((float)Math.PI / 2F);
					float f27 = Mth.sin(f26) * 0.25F;
					float f28 = Mth.cos(f26) * 0.25F;
					u = flowTexture.getU(8.0F + (-f28 - f27) * 16.0F);
					u1 = flowTexture.getU(8.0F + (-f28 + f27) * 16.0F);
					v = flowTexture.getV(8.0F + (-f28 + f27) * 16.0F);
					v1 = flowTexture.getV(8.0F + (-f28 - f27) * 16.0F);
					u2 = flowTexture.getU(8.0F + (f28 + f27) * 16.0F);
					u21 = flowTexture.getU(8.0F + (f28 - f27) * 16.0F);
					v2 = flowTexture.getV(8.0F + (f28 + f27) * 16.0F);
					v21 = flowTexture.getV(8.0F + (f28 - f27) * 16.0F);
				}

				float endU = (u + u1 + u2 + u21) / 4.0F;
				float endV = (v + v2 + v21 + v1) / 4.0F;
				float f51 = (float)textureAtlasSprites[0].getWidth() / (textureAtlasSprites[0].getU1() - textureAtlasSprites[0].getU0());
				float f52 = (float)textureAtlasSprites[0].getHeight() / (textureAtlasSprites[0].getV1() - textureAtlasSprites[0].getV0());
				float delta = 4.0F / Math.max(f52, f51);
				u = Mth.lerp(delta, u, endU);
				u1 = Mth.lerp(delta, u1, endU);
				u2 = Mth.lerp(delta, u2, endU);
				u21 = Mth.lerp(delta, u21, endU);
				v = Mth.lerp(delta, v, endV);
				v2 = Mth.lerp(delta, v2, endV);
				v21 = Mth.lerp(delta, v21, endV);
				v1 = Mth.lerp(delta, v1, endV);
				int lightColor = this.getLightColor(level, pos);
				float r = upShade * red;
				float g = upShade * green;
				float b = upShade * blue;

				this.vertex(vertexConsumer, d1 + 0.0D, d2 + (double)northWestAverageHeight, d0 + 0.0D, r, g, b, alpha, u, v, lightColor);
				this.vertex(vertexConsumer, d1 + 0.0D, d2 + (double)southWestAverageHeight, d0 + 1.0D, r, g, b, alpha, u1, v2, lightColor);
				this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)southEastAverageHeight, d0 + 1.0D, r, g, b, alpha, u2, v21, lightColor);
				this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)northEastAverageHeight, d0 + 0.0D, r, g, b, alpha, u21, v1, lightColor);
				if (fluidState.shouldRenderBackwardUpFace(level, pos.above())) {
					this.vertex(vertexConsumer, d1 + 0.0D, d2 + (double)northWestAverageHeight, d0 + 0.0D, r, g, b, alpha, u, v, lightColor);
					this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)northEastAverageHeight, d0 + 0.0D, r, g, b, alpha, u21, v1, lightColor);
					this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)southEastAverageHeight, d0 + 1.0D, r, g, b, alpha, u2, v21, lightColor);
					this.vertex(vertexConsumer, d1 + 0.0D, d2 + (double)southWestAverageHeight, d0 + 1.0D, r, g, b, alpha, u1, v2, lightColor);
				}
			}

			if (shouldRenderDown) { // 渲染下面
				float f40 = textureAtlasSprites[0].getU0();
				float f41 = textureAtlasSprites[0].getU1();
				float f42 = textureAtlasSprites[0].getV0();
				float f43 = textureAtlasSprites[0].getV1();
				int lightColor = this.getLightColor(level, pos.below());
				float f46 = downShade * red;
				float f47 = downShade * green;
				float f48 = downShade * blue;

				this.vertex(vertexConsumer, d1, d2 + (double)f17, d0 + 1.0D, f46, f47, f48, alpha, f40, f43, lightColor);
				this.vertex(vertexConsumer, d1, d2 + (double)f17, d0, f46, f47, f48, alpha, f40, f42, lightColor);
				this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)f17, d0, f46, f47, f48, alpha, f41, f42, lightColor);
				this.vertex(vertexConsumer, d1 + 1.0D, d2 + (double)f17, d0 + 1.0D, f46, f47, f48, alpha, f41, f43, lightColor);
			}

			int lightColor = this.getLightColor(level, pos);

			for(Direction direction : Direction.Plane.HORIZONTAL) { // 侧面渲染
				float height1;
				float height2;
				double d3;
				double d4;
				double d5;
				double d6;
				boolean shouldRenderFace;
				switch (direction) {
					case NORTH -> {
						height1 = northWestAverageHeight;
						height2 = northEastAverageHeight;
						d3 = d1;
						d5 = d1 + 1.0D;
						d4 = d0 + (double) 0.001F;
						d6 = d0 + (double) 0.001F;
						shouldRenderFace = shouldRenderNorth;
					}
					case SOUTH -> {
						height1 = southEastAverageHeight;
						height2 = southWestAverageHeight;
						d3 = d1 + 1.0D;
						d5 = d1;
						d4 = d0 + 1.0D - (double) 0.001F;
						d6 = d0 + 1.0D - (double) 0.001F;
						shouldRenderFace = shouldRenderSouth;
					}
					case WEST -> {
						height1 = southWestAverageHeight;
						height2 = northWestAverageHeight;
						d3 = d1 + (double) 0.001F;
						d5 = d1 + (double) 0.001F;
						d4 = d0 + 1.0D;
						d6 = d0;
						shouldRenderFace = shouldRenderWest;
					}
					default -> {
						height1 = northEastAverageHeight;
						height2 = southEastAverageHeight;
						d3 = d1 + 1.0D - (double) 0.001F;
						d5 = d1 + 1.0D - (double) 0.001F;
						d4 = d0;
						d6 = d0 + 1.0D;
						shouldRenderFace = shouldRenderEast;
					}
				}

				if (shouldRenderFace && !isFaceOccludedByNeighbor(level, pos, direction, Math.max(height1, height2), level.getBlockState(pos.relative(direction)))) {
					BlockPos blockpos = pos.relative(direction);
					TextureAtlasSprite flowTexture = textureAtlasSprites[1];
					if (textureAtlasSprites[2] != null) {
						if (level.getBlockState(blockpos).shouldDisplayFluidOverlay(level, blockpos, fluidState)) {
							flowTexture = textureAtlasSprites[2];
						}
					}

					float f54 = flowTexture.getU(0.0D);
					float f55 = flowTexture.getU(8.0D);
					float f33 = flowTexture.getV((1.0F - height1) * 16.0F * 0.5F);
					float f34 = flowTexture.getV((1.0F - height2) * 16.0F * 0.5F);
					float f35 = flowTexture.getV(8.0D);
					float horizonShade = direction.getAxis() == Direction.Axis.Z ? northShade : westShade;
					float r = upShade * horizonShade * red;
					float g = upShade * horizonShade * green;
					float b = upShade * horizonShade * blue;

					this.vertex(vertexConsumer, d3, d2 + (double)height1, d4, r, g, b, alpha, f54, f33, lightColor);
					this.vertex(vertexConsumer, d5, d2 + (double)height2, d6, r, g, b, alpha, f55, f34, lightColor);
					this.vertex(vertexConsumer, d5, d2 + (double)f17, d6, r, g, b, alpha, f55, f35, lightColor);
					this.vertex(vertexConsumer, d3, d2 + (double)f17, d4, r, g, b, alpha, f54, f35, lightColor);
					if (flowTexture != this.waterOverlay) {
						this.vertex(vertexConsumer, d3, d2 + (double)f17, d4, r, g, b, alpha, f54, f35, lightColor);
						this.vertex(vertexConsumer, d5, d2 + (double)f17, d6, r, g, b, alpha, f55, f35, lightColor);
						this.vertex(vertexConsumer, d5, d2 + (double)height2, d6, r, g, b, alpha, f55, f34, lightColor);
						this.vertex(vertexConsumer, d3, d2 + (double)height1, d4, r, g, b, alpha, f54, f33, lightColor);
					}
				}
			}
		}
	}

	private void vertex(VertexConsumer consumer, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int packedLight) {
		consumer.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
	}
}
