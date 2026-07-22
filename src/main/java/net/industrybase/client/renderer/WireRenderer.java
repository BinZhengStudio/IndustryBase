package net.industrybase.client.renderer;

import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.industrybase.capability.IndustryBaseApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class WireRenderer {
    public static final WireRenderer INSTANCE = new WireRenderer();

    private final HashMultimap<BlockPos, BlockPos> wireConn = HashMultimap.create();
    private final OutputTarget outputTarget = OutputTarget.MAIN_TARGET;
    private final RenderPipeline pipeline = RenderPipelineList.WIRE;

    private @Nullable MeshData mesh;

    public void addWire(BlockPos from, BlockPos to) {
        this.wireConn.put(from, to);

        this.buildMesh();
    }

    public void addWire(BlockPos pos, Collection<BlockPos> data) {
        this.wireConn.putAll(pos, data);

        this.buildMesh();
    }

    public void removeWire(BlockPos from, BlockPos to) {
        this.wireConn.remove(from, to);

        this.buildMesh();
    }

    public void removeWires(BlockPos from) {
        this.wireConn.get(from).forEach(to -> this.wireConn.remove(to, from));
        this.wireConn.removeAll(from);

        this.buildMesh();
    }

    private void buildMesh() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        var bufferSize = this.wireConn.size() * 100 * this.pipeline.getVertexFormat().getVertexSize();

        // TODO can buffer be exactly sized?
        var builder = new BufferBuilder(ByteBufferBuilder.exactlySized(bufferSize), this.pipeline.getVertexFormatMode(),
                this.pipeline.getVertexFormat());

        for (Map.Entry<BlockPos, BlockPos> entry : this.wireConn.entries()) {
            var start = entry.getKey();
            var end = entry.getValue();
            renderWire(builder, start, end, level);
        }

        if (this.mesh != null) {
            try {
                this.mesh.close();
            } finally {
            }
        }

        this.mesh = builder.build();
    }

    private static int renderWire(BufferBuilder buffer, BlockPos start, BlockPos end, ClientLevel level) {
        float totalX = end.getX() - start.getX();
        float totalY = end.getY() - start.getY();
        float totalZ = end.getZ() - start.getZ();

        float horizonDistance = Mth.sqrt(totalX * totalX + totalZ * totalZ);

        float width = 0.025F;
        float square = totalX * totalX + totalZ * totalZ;
        float size = Mth.invSqrt(square) * width;
        boolean vertical = square == 0;

        float widthY = width;
        // if the wire is vertical, size will be zero, so special handling is needed
        float widthX = vertical ? width : totalZ * size;
        float widthZ = vertical ? width : totalX * size;

        int[] lights = new int[] {
                level.getBrightness(LightLayer.BLOCK, start),
                level.getBrightness(LightLayer.BLOCK, end),
                level.getBrightness(LightLayer.SKY, start),
                level.getBrightness(LightLayer.SKY, end)
        };

        for (int i = 0; i <= 24; ++i) { // 25 segments in total
            addVertexPair(buffer, Vec3.atCenterOf(start), totalX, totalY, totalZ, horizonDistance, lights, widthY,
                    widthX, widthZ, i);
        }

        for (int j = 24; j >= 0; --j) { // add vertices again to render two intersecting faces
            // negate widthY, otherwise the face will overlap with the previous face
            addVertexPair(buffer, Vec3.atCenterOf(start), totalX, totalY, totalZ, horizonDistance, lights, -widthY,
                    vertical ? -widthX : widthX, widthZ, j);
        }

        return 0;
    }

    private static void addVertexPair(VertexConsumer consumer, Vec3 start, float totalX, float totalY, float totalZ,
            float horizonDistance, int[] lights, float widthY, float widthX, float widthZ, int index) {
        float delta = index / 24.0F;
        int lightLevel = (int) Mth.lerp(delta, lights[0], lights[1]);
        int brightness = (int) Mth.lerp(delta, lights[2], lights[3]);
        int packedLight = LightCoordsUtil.pack(lightLevel, brightness);
        float red = 0.1F;
        float green = 0.1F;
        float blue = 0.1F;

        // sagging factor, tested to be natural at 0.08
        float f = 0.08F;
        // Use a quadratic function
        float y = totalY > 0.0F ? totalY * delta * delta : totalY - totalY * (1.0F - delta) * (1.0F - delta);
        // add another quadratic function for sagging effect
        y += delta > 0.5F ? horizonDistance * f * (delta - 0.5F) * (delta - 0.5F)
                : horizonDistance * f * (0.5F - delta) * (0.5F - delta);
        // correct the vertex position
        y -= horizonDistance * f * 0.5F * 0.5F;

        float x = totalX * delta;
        float z = totalZ * delta;

        consumer.addVertex((float) start.x + x - widthX, (float) start.y + y + widthY, (float) start.z + z + widthZ)
                .setColor(red, green, blue, 1.0F)
                .setLight(packedLight);
        consumer.addVertex((float) start.x + x + widthX, (float) start.y + y - widthY, (float) start.z + z - widthZ)
                .setColor(red, green, blue, 1.0F)
                .setLight(packedLight);
    }

    @SubscribeEvent
    public static void renderWire(final RenderLevelStageEvent.AfterOpaqueBlocks event) {
        var mesh = INSTANCE.mesh;
        if (mesh == null)
            return;

        var renderTarget = INSTANCE.outputTarget.getRenderTarget();
        var dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F,
                        1.0F, 1.0F), new Vector3f(),
                        TextureTransform.DEFAULT_TEXTURING.getMatrix());

        GpuBuffer vertices = INSTANCE.pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (mesh.indexBuffer() == null) {
            RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
            indices = autoIndices.getBuffer(mesh.drawState().indexCount());
            indexType = autoIndices.type();
        } else {
            indices = INSTANCE.pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
            indexType = mesh.drawState().indexType();
        }

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "IndustryBase: wires",
                        renderTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTextureView(),
                        OptionalDouble.empty())) {
            renderPass.setPipeline(INSTANCE.pipeline);
            ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.enabled()) {
                renderPass.enableScissor(scissorState.x(), scissorState.y(),
                        scissorState.width(),
                        scissorState.height());
            }

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);

            renderPass.bindTexture("Sampler2",
                    Minecraft.getInstance().gameRenderer.lightmap(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
        } catch (Throwable err) {
            throw err;
        }
    }

    private static class IndexBufferBuilder implements AutoCloseable {
        private final GpuBuffer buffer;
        private final VertexFormat.IndexType type;

        public IndexBufferBuilder(GpuBuffer buffer, VertexFormat.IndexType type) {
            this.buffer = buffer;
            this.type = type;
        }

        public GpuBuffer getBuffer() {
            return this.buffer;
        }

        public VertexFormat.IndexType getType() {
            return this.type;
        }

        @Override
        public void close() {
            this.buffer.close();
        }
    }
}
