package cn.bzgzs.largeprojects.client.renderer.blockentity;

import cn.bzgzs.largeprojects.world.level.block.entity.BlockEntityTypeList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererManager {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(BlockEntityTypeList.TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(TransmissionRodRenderer.MAIN, TransmissionRodRenderer::createBodyLayer);
	}
}
