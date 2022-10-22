package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererManager {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(BlockEntityTypeList.IRON_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.GOLD_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(TransmissionRodRenderer.MAIN, TransmissionRodRenderer::createBodyLayer);
	}
}
