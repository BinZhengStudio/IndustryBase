package net.industrybase.client.renderer.blockentity;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, value = Dist.CLIENT)
public class RendererManager {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // 注册方块实体的渲染器
        event.registerBlockEntityRenderer(BlockEntityTypeList.TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeList.CREATIVE_STEAM_ENGINE.get(),
                CreativeSteamEngineRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectableRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeList.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeList.INSULATOR.get(), WireConnectableRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TransmissionRodRenderer.MAIN, TransmissionRodRenderer::createBodyLayer);
    }
}
