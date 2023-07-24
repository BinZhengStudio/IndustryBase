package cn.bzgzs.industrybase.client.renderer.blockentity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.client.renderer.blockentity.TextureTransmissionRodRenderer;
import cn.bzgzs.industrybase.api.client.renderer.blockentity.TransmissionRodRenderer;
import cn.bzgzs.industrybase.api.client.renderer.blockentity.WireConnectableRenderer;
import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererManager {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // 注册方块实体的渲染器
		event.registerBlockEntityRenderer(BlockEntityTypeList.OAK_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/oak.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.SPRUCE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/spruce.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.BIRCH_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/birch.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.JUNGLE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/jungle.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.ACACIA_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/acacia.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.DARK_OAK_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/dark_oak.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.MANGROVE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/mangrove.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.STONE_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/stone.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.IRON_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/iron.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.CHERRY_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/cherry.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.CRIMSON_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/crimson.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.WARPED_TRANSMISSION_ROD.get(), ctx -> new TextureTransmissionRodRenderer(ctx) {
			@Override
			protected ResourceLocation getTexture() {
				return new ResourceLocation(IndustryBaseApi.MODID, "textures/entity/transmission_rod/warped.png");
			}
		});
		event.registerBlockEntityRenderer(BlockEntityTypeList.GOLD_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.DIAMOND_TRANSMISSION_ROD.get(), TransmissionRodRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectableRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(TransmissionRodRenderer.MAIN, TransmissionRodRenderer::createBodyLayer);
		event.registerLayerDefinition(SteamEngineRenderer.MAIN, SteamEngineRenderer::createBodyLayer);
	}
}
