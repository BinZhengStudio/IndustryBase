package net.industrybase.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.industrybase.world.level.block.entity.CreativeSteamEngineBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class CreativeSteamEngineRenderer
        implements BlockEntityRenderer<CreativeSteamEngineBlockEntity, BlockEntityRenderState> {
    public CreativeSteamEngineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        poseStack.pushPose();
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(),
                (pose, buffer) -> SteamEngineRenderer.renderWater(this, 1.0F, state.blockPos, poseStack, buffer,
                        state.lightCoords));
        poseStack.popPose();
    }
}
