package net.industrybase.client.renderer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
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
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class WireRenderer {
    public static final WireRenderer INSTANCE = new WireRenderer();

    private final HashMultimap<BlockPos, BlockPos> wireConn = HashMultimap.create();
    private final OutputTarget outputTarget = OutputTarget.MAIN_TARGET;
    private final RenderPipeline pipeline = RenderPipelineList.WIRE;

    private @Nullable GpuBuffer vertices;
    private @Nullable GpuBuffer indices;
    private VertexFormat.IndexType indexType = VertexFormat.IndexType.SHORT;
    private int indexCount = 0;

    public void addWire(BlockPos from, BlockPos to) {
        if (this.wireConn.put(from, to))
            this.buildBuffer();
    }

    public void addWire(BlockPos pos, Collection<BlockPos> data) {
        if (this.wireConn.putAll(pos, data))
            this.buildBuffer();
    }

    public void removeWire(BlockPos from, BlockPos to) {
        if (this.wireConn.remove(from, to))
            this.buildBuffer();
    }

    public void removeWires(BlockPos from) {
        var flag = false;
        for (var to : this.wireConn.get(from)) {
            if (this.wireConn.remove(to, from))
                flag = true;
        }
        if (this.wireConn.removeAll(from).size() > 0)
            flag = true;

        if (flag)
            this.buildBuffer();
    }

    private void buildBuffer() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        var wireCount = this.wireConn.size();
        var vertexSize = wireCount * 100 * this.pipeline.getVertexFormat().getVertexSize();

        var builder = new BufferBuilder(ByteBufferBuilder.exactlySized(vertexSize), this.pipeline.getVertexFormatMode(),
                this.pipeline.getVertexFormat());

        for (Map.Entry<BlockPos, BlockPos> entry : this.wireConn.entries()) {
            var start = entry.getKey();
            var end = entry.getValue();
            addWireVertex(builder, start, end, level);
        }

        var mesh = builder.build();
        if (mesh == null) {
            this.releaseBuffer();
            return;
        }

        try {
            var vertexBuffer = mesh.vertexBuffer();
            this.vertices = uploadToBuffer(this.vertices, vertexBuffer,
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    () -> "Vertex buffer for industrybase wire rendering");
        } catch (Exception e) {
            this.releaseBuffer();
        } finally {
            try {
                mesh.close();
            } catch (Exception e) {
            }
        }

        this.indexType = mesh.drawState().indexType();

        this.indexCount = wireCount * 288; // 24 segments * 2 triangles * 3 vertices * 2 per wire
        var indexSize = this.indexCount * this.indexType.bytes;

        var indexBuffer = MemoryUtil.memAlloc(indexSize);
        IntConsumer indexConsumer = switch (this.indexType) {
            case SHORT -> (value -> indexBuffer.putShort((short) value));
            default -> indexBuffer::putInt;
        };

        try {
            for (int i = 0; i < wireCount; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 48; k++) {
                        var index = (i * 100) + (j * 50) + k;
                        indexConsumer.accept(index);
                        indexConsumer.accept(index + 1);
                        indexConsumer.accept(index + 2);
                    }
                }
            }

            indexBuffer.flip();
            this.indices = uploadToBuffer(this.indices, indexBuffer, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                    () -> "Index buffer for industrybase wire rendering");
        } catch (Exception e) {
            this.releaseBuffer();
        } finally {
            MemoryUtil.memFree(indexBuffer);
        }
    }

    private static int addWireVertex(BufferBuilder buffer, BlockPos start, BlockPos end, ClientLevel level) {
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

        for (int i = 0; i <= 24; ++i) { // 24 segments in total
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

    private static GpuBuffer uploadToBuffer(@Nullable GpuBuffer target, ByteBuffer buffer, @GpuBuffer.Usage int usage,
            Supplier<String> label) {
        GpuDevice device = RenderSystem.getDevice();

        if (target == null) {
            target = device.createBuffer(label, usage, buffer);
        } else {
            if (target.size() < buffer.remaining()) {
                target.close();
                target = device.createBuffer(label, usage, buffer);
            } else {
                CommandEncoder encoder = device.createCommandEncoder();
                encoder.writeToBuffer(target.slice(), buffer);
            }
        }

        return target;
    }

    private void releaseBuffer() {
        try {
            this.vertices.close();
        } catch (Exception e) {
            // Ignore exception, include NPE
        } finally {
            this.vertices = null;
        }

        try {
            this.indices.close();
        } catch (Exception e) {
        } finally {
            this.indices = null;
        }
    }

    @SubscribeEvent
    public static void renderWire(final RenderLevelStageEvent.AfterOpaqueBlocks event) {
        var vertices = INSTANCE.vertices;
        var indices = INSTANCE.indices;
        var indexType = INSTANCE.indexType;
        var indexCount = INSTANCE.indexCount;
        if (vertices == null || indices == null)
            return;

        var renderTarget = INSTANCE.outputTarget.getRenderTarget();
        var dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F,
                        1.0F, 1.0F), new Vector3f(),
                        TextureTransform.DEFAULT_TEXTURING.getMatrix());

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "IndustryBase:wires",
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

            renderPass.drawIndexed(0, 0, indexCount, 1);
        } catch (Throwable err) {
            throw err;
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        INSTANCE.wireConn.clear();
        INSTANCE.releaseBuffer();
    }
}
