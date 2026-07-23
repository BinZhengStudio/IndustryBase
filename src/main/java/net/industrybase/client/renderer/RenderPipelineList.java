package net.industrybase.client.renderer;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.util.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class RenderPipelineList {
    public static final RenderPipeline WIRE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Util.withNamespace("pipeline/wire"))
            .withVertexShader(Util.withNamespace("wire"))
            .withFragmentShader(Util.withNamespace("wire"))
            .withSampler("Sampler2") // use the sampler2 for light map
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLES)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    @SubscribeEvent
    public static void registerEventHandler(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(WIRE);
    }
}
