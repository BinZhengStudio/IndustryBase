package net.industrybase.client.renderer.blockentity;

import java.util.EnumMap;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.client.SubscribeSpeedPacket;
import net.industrybase.client.renderer.blockentity.state.TransmissionRodRenderState;
import net.industrybase.world.level.block.LayeredTransmissionRodBlock;
import net.industrybase.world.level.block.TransmissionRodBlock;
import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TransmissionRodRenderer
        implements BlockEntityRenderer<TransmissionRodBlockEntity, TransmissionRodRenderState> {
    public static final ModelLayerLocation MAIN = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, "transmission_rod"), "main");
    private static final Identifier LAYER_1 = Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID,
            "textures/entity/transmission_rod/layer_1.png");
    private static final Identifier LAYER_2 = Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID,
            "textures/entity/transmission_rod/layer_2.png");
    private static final EnumMap<Direction.Axis, Transformation> TRANSFORMATIONS = new EnumMap<>(Direction.Axis.class);
    private final TransmissionRodRenderer.TransmissionRodModel model;

    static {
        TRANSFORMATIONS.put(Direction.Axis.X, new Transformation(
                new Matrix4f()
                        .translate(0.5F, 0.5F, 0.5F)
                        .rotate(new Quaternionf().rotateZ((float) (Math.PI / 2.0D)))));
        TRANSFORMATIONS.put(Direction.Axis.Y, new Transformation(
                new Matrix4f()
                        .translate(0.5F, 0.5F, 0.5F)));
        TRANSFORMATIONS.put(Direction.Axis.Z, new Transformation(
                new Matrix4f()
                        .translate(0.5F, 0.5F, 0.5F)
                        .rotate(new Quaternionf().rotateX((float) (-Math.PI / 2.0D)))));
    }

    public TransmissionRodRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TransmissionRodRenderer.TransmissionRodModel(context.bakeLayer(MAIN), null);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F,
                6.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TransmissionRodBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.offset(-256, -256, -256).getCenter(), pos.offset(256, 256, 256).getCenter());
    }

    @Override
    public TransmissionRodRenderState createRenderState() {
        return new TransmissionRodRenderState();
    }

    @Override
    public void extractRenderState(TransmissionRodBlockEntity blockEntity, TransmissionRodRenderState state,
            float partialTicks, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        subscribeSpeed(blockEntity);

        var level = blockEntity.getLevel();
        if (level != null) {
            var blockState = blockEntity.getBlockState();
            var context = blockEntity.getRotate();
            var block = blockState.getBlock();
            state.axis = blockState.getValue(TransmissionRodBlock.AXIS);
            state.rotate = Mth.rotLerp(partialTicks, context.getOldDegree(), context.getDegree());

            if (block instanceof LayeredTransmissionRodBlock layeredBlock) {
                state.rgbColor = layeredBlock.getRgbColor();
            } else if (block instanceof TransmissionRodBlock rodBlock) {
                var texture = rodBlock.getTexture();
                if (texture != null) {
                    state.texture = texture.withPath(path -> "textures/entity/" + path + ".png");
                }
            }
        }
    }

    @Override
    public void submit(TransmissionRodRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(TRANSFORMATIONS.get(state.axis));

        this.model.setupAnim(state.rotate);

        if (state.texture != null) {
            // if a texture is provided, render only one layer with the provided texture
            submitNodeCollector.submitModel(this.model, state.rotate, poseStack, RenderTypes.entitySolid(state.texture),
                    state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0, state.breakProgress);
        } else {
            submitNodeCollector.submitModel(this.model, state.rotate, poseStack, RenderTypes.entityCutout(LAYER_1),
                    state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0, state.breakProgress);

            // the lighted layer
            submitNodeCollector.submitModel(this.model, state.rotate, poseStack, RenderTypes.entityCutout(LAYER_2),
                    LightCoordsUtil.pack(15, 15), OverlayTexture.NO_OVERLAY, state.rgbColor, null, 0,
                    state.breakProgress);
        }

        poseStack.popPose();
    }

    public static void subscribeSpeed(TransmissionRodBlockEntity blockEntity) {
        if (blockEntity.hasLevel() && !blockEntity.isSubscribed()) {
            ClientPacketDistributor.sendToServer(new SubscribeSpeedPacket(blockEntity.getBlockPos()));
            blockEntity.setSubscribed();
        }
    }

    private static class TransmissionRodModel extends Model<Float> {
        private final ModelPart main;

        public TransmissionRodModel(ModelPart root, Function<Identifier, RenderType> renderType) {
            super(root, renderType);
            this.main = root.getChild("main");
        }

        @Override
        public void setupAnim(Float state) {
            super.setupAnim(state);
            this.main.yRot = state * (float) (Math.PI / 180.0D);
        }
    }
}
