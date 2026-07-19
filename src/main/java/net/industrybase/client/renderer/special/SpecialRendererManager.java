package net.industrybase.client.renderer.special;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class SpecialRendererManager {
    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Util.withNamespace( "transmission_rod"),
                TransmissionRodSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
