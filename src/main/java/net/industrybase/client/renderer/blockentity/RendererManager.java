package net.industrybase.client.renderer.blockentity;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.client.renderer.blockentity.TextureTransmissionRodRenderer;
import net.industrybase.api.client.renderer.blockentity.TransmissionRodRenderer;
import net.industrybase.api.client.renderer.blockentity.WireConnectableRenderer;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererManager {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // 注册方块实体的渲染器
		event.registerBlockEntityRenderer(BlockEntityTypeList.OAK_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/oak.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.SPRUCE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/spruce.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.BIRCH_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/birch.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.JUNGLE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/jungle.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.ACACIA_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/acacia.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.DARK_OAK_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/dark_oak.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.MANGROVE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/mangrove.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.STONE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/stone.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.IRON_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/iron.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.CHERRY_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/cherry.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.CRIMSON_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/crimson.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.WARPED_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "textures/entity/transmission_rod/warped.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.GOLD_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.DIAMOND_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.CREATIVE_STEAM_ENGINE.get(), CreativeSteamEngineRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectableRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.FLUID_TANK.get(), FluidTankRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.INSULATOR.get(), WireConnectableRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(TransmissionRodRenderer.MAIN, TransmissionRodRenderer::createBodyLayer);
	}
}
