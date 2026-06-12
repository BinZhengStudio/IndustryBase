package net.industrybase.client.renderer.special;

import java.util.function.Consumer;
import java.util.Optional;

import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.industrybase.client.renderer.blockentity.TransmissionRodRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;

public class TransmissionRodSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TransmissionRodRenderer.TransmissionRodModel model;
    private final Optional<Identifier> texture;
    private final int rgbColor;

    public TransmissionRodSpecialRenderer(SpecialModelRenderer.BakingContext context, Optional<Identifier> texture,
            int rgbColor) {
        this.model = new TransmissionRodRenderer.TransmissionRodModel(
                context.entityModelSet().bakeLayer(TransmissionRodRenderer.MAIN), null);
        this.texture = texture;
        this.rgbColor = rgbColor;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords,
            boolean hasFoil, int outlineColor) {
        TransmissionRodRenderer.submit(this.model, poseStack, submitNodeCollector, 0.0F, this.texture.orElse(null),
                this.rgbColor, null, lightCoords, outlineColor);
    }

    public record Unbaked(Optional<Identifier> texture, int rgbColor) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<TransmissionRodSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                        Identifier.CODEC.optionalFieldOf("texture")
                                .forGetter(TransmissionRodSpecialRenderer.Unbaked::texture),
                        Codec.INT.optionalFieldOf("rgbColor", 0)
                                .forGetter(TransmissionRodSpecialRenderer.Unbaked::rgbColor))
                        .apply(i, TransmissionRodSpecialRenderer.Unbaked::new));

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            var textureLoc = this.texture.map(texture -> texture.withPath(path -> "textures/entity/" + path + ".png"));
            return new TransmissionRodSpecialRenderer(context, textureLoc, this.rgbColor);
        }

        @Override
        public MapCodec<? extends Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
