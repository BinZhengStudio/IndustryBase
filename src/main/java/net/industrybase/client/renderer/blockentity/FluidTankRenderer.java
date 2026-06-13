package net.industrybase.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.industrybase.client.renderer.blockentity.state.FluidTankRenderState;
import net.industrybase.network.client.RequestWaterAmountPayload;
import net.industrybase.world.level.block.entity.FluidTankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderState> {
    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(FluidTankBlockEntity blockEntity, FluidTankRenderState state, float partialTicks,
            Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        requestWaterAmount(blockEntity);

        int oldWaterAmount = blockEntity.getOldWaterAmount();
        int waterAmount = blockEntity.getWaterAmount();
        state.waterAmount = Mth.lerp(partialTicks, oldWaterAmount, waterAmount) / FluidTankBlockEntity.CAPACITY;
    }

    @Override
    public void submit(FluidTankRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (state.waterAmount <= 0.0F)
            return; // exit if there is no water to be rendered

        poseStack.pushPose();
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(),
                (pose, buffer) -> renderWater(buffer, pose, state.waterAmount, state.blockPos, state.lightCoords));
        poseStack.popPose();
    }

    private static void requestWaterAmount(FluidTankBlockEntity blockEntity) {
        if (blockEntity.hasLevel() && !blockEntity.isSubscribed()) {
            ClientPacketDistributor.sendToServer(new RequestWaterAmountPayload(blockEntity.getBlockPos()));
            blockEntity.setSubscribed();
        }
    }

    public static <T extends BlockEntity, S extends BlockEntityRenderState> void renderWater(
            VertexConsumer buffer, PoseStack.Pose pose, float waterAmount, BlockPos pos, int lightCoords) {
        Minecraft mc = Minecraft.getInstance();
        FluidStateModelSet modelSet = mc.getModelManager().getFluidStateModelSet();
        ClientLevel level = mc.level;
        if (level == null)
            return; // exit if level is not available

        Matrix4f matrix4f = pose.pose();

        float minX = 0.001F;
        float minY = 0.001F;
        float minZ = 0.001F;
        float maxX = 0.999F;
        float maxY = Math.max(minY, waterAmount - 0.001F);
        float maxZ = 0.999F;

        FluidState fluidState = Fluids.WATER.defaultFluidState();
        FluidModel model = modelSet.get(Fluids.WATER.defaultFluidState());

        TextureAtlasSprite stillSprite = model.stillMaterial().sprite();
        TextureAtlasSprite flowingSprite = model.flowingMaterial().sprite();
        float u0 = stillSprite.getU(1.0F / 16.0F);
        float u1 = stillSprite.getU(15.0F / 16.0F);
        float v0 = stillSprite.getV(1.0F / 16.0F);
        float v1 = stillSprite.getV(15.0F / 16.0F);
        float u01 = flowingSprite.getU(1.0F / 16.0F);
        float u11 = flowingSprite.getU(8.0F / 16.0F);
        float v01 = flowingSprite.getV((8.0F - 7.0F * waterAmount) / 16.0F);
        float v11 = flowingSprite.getV(8.0F / 16.0F);

        BlockState blockState = Blocks.WATER.defaultBlockState();
        int fluidColor = model.fluidTintSource() != null
                ? model.fluidTintSource().colorInWorld(fluidState, blockState, level, pos)
                : -1; // 获取水的颜色
        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        float alpha = (float) (fluidColor >> 24 & 255) / 255.0F;

        // Up
        buffer.addVertex(matrix4f, minX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);

        // Down
        buffer.addVertex(matrix4f, minX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);

        // West
        buffer.addVertex(matrix4f, minX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);

        // North
        buffer.addVertex(matrix4f, maxX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);

        // South
        buffer.addVertex(matrix4f, minX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, minX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);

        // East
        buffer.addVertex(matrix4f, maxX, maxY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, maxZ)
                .setColor(red, green, blue, alpha)
                .setUv(u01, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, minY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v11)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix4f, maxX, maxY, minZ)
                .setColor(red, green, blue, alpha)
                .setUv(u11, v01)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
