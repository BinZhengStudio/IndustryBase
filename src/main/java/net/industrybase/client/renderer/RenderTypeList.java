package net.industrybase.client.renderer;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class RenderTypeList {
    public static final RenderType WIRE = RenderType.create(IndustryBaseApi.MODID + ":wire",
            RenderSetup.builder(RenderPipelineList.WIRE)
                    .useLightmap()
                    .createRenderSetup());

    @SubscribeEvent
    public static void registerEventHandler(final RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(WIRE);
    }
}
