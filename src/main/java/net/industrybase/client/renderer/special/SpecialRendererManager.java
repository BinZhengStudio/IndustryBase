package net.industrybase.client.renderer.special;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class SpecialRendererManager {
    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, "transmission_rod"),
                TransmissionRodSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
