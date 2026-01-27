package net.kaupenjoe.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.block.entity.ModBlockEntities;
import net.kaupenjoe.tutorialmod.block.entity.renderer.PedestalBlockEntityRenderer;
import net.kaupenjoe.tutorialmod.entity.ModEntities;
import net.kaupenjoe.tutorialmod.entity.client.*;
import net.kaupenjoe.tutorialmod.event.CameraResetHandler;
import net.kaupenjoe.tutorialmod.event.StaminaHudOverlay;
import net.kaupenjoe.tutorialmod.event.ZoomHandler;
import net.kaupenjoe.tutorialmod.event.ZoomHudOverlay;
import net.kaupenjoe.tutorialmod.particle.ModParticles;
import net.kaupenjoe.tutorialmod.particle.PinkGarnetParticle;
import net.kaupenjoe.tutorialmod.screen.ModScreenHandlers;
import net.kaupenjoe.tutorialmod.screen.custom.GrowthChamberScreen;
import net.kaupenjoe.tutorialmod.screen.custom.PedestalScreen;
import net.kaupenjoe.tutorialmod.util.ModKeyBindings;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.kaupenjoe.tutorialmod.network.StaminaSyncPayload;
import net.kaupenjoe.tutorialmod.network.SprintCooldownPayload;
import net.kaupenjoe.tutorialmod.network.ThirstSyncPayload;
import net.kaupenjoe.tutorialmod.network.FreecamCountdownPayload;
import net.kaupenjoe.tutorialmod.event.UnifiedHudOverlay;
import net.kaupenjoe.tutorialmod.event.AnimatedChatHud;
import net.kaupenjoe.tutorialmod.network.LoginStreakPayload;
import net.kaupenjoe.tutorialmod.event.LoginStreakHud;
import net.kaupenjoe.tutorialmod.event.WeatherNotificationHud;
import net.kaupenjoe.tutorialmod.event.BiomeNotificationHud;

public class TutorialModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║  KimDog SMP - Tutorial Mod Client Initialization Start  ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		// Register keybindings
		TutorialMod.LOGGER.info("✓ Registering Key Bindings...");
		ModKeyBindings.registerKeyBindings();

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║           Loading Input & Camera Systems               ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		// Register zoom tick event
		TutorialMod.LOGGER.info("✓ Registering Zoom & Camera Handlers...");
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ZoomHandler.tick();
			CameraResetHandler.tick();
		});

		// Register zoom HUD overlay
		HudRenderCallback.EVENT.register(new ZoomHudOverlay());

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║         Loading Network Payloads & HUD Systems         ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		// Automated network payload registration system
		TutorialMod.LOGGER.info("✓ Registering Network Payloads (Stamina System)...");
		registerStaminaPayloads();

		TutorialMod.LOGGER.info("✓ Registering Network Payloads (Temperature System)...");
		registerTemperaturePayloads();

		TutorialMod.LOGGER.info("✓ Registering Network Payloads (Weather & Environment)...");
		registerWeatherAndEnvironmentPayloads();

		TutorialMod.LOGGER.info("✓ Registering Network Payloads (Chat & Social)...");
		registerChatAndSocialPayloads();

		TutorialMod.LOGGER.info("✓ Registering Network Payloads (Miscellaneous)...");
		registerMiscellaneousPayloads();

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║      Loading Block Rendering & Transparency           ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		TutorialMod.LOGGER.info("✓ Registering Block Render Layers...");
		registerBlockRenderLayers();

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║       Loading Entity Models & Renderers               ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		TutorialMod.LOGGER.info("✓ Registering Entity Models (Mantis)...");
		EntityModelLayerRegistry.registerModelLayer(MantisModel.MANTIS, MantisModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MANTIS, MantisRenderer::new);

		TutorialMod.LOGGER.info("✓ Registering Entity Models (Tomahawk Projectile)...");
		EntityModelLayerRegistry.registerModelLayer(TomahawkProjectileModel.TOMAHAWK, TomahawkProjectileModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.TOMAHAWK, TomahawkProjectileRenderer::new);

		TutorialMod.LOGGER.info("✓ Registering Entity Renderers (Chair)...");
		EntityRendererRegistry.register(ModEntities.CHAIR, ChairRenderer::new);

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║        Loading Particles & Block Entities             ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		TutorialMod.LOGGER.info("✓ Registering Particles...");
		ParticleFactoryRegistry.getInstance().register(ModParticles.PINK_GARNET_PARTICLE, PinkGarnetParticle.Factory::new);

		TutorialMod.LOGGER.info("✓ Registering Block Entity Renderers...");
		BlockEntityRendererFactories.register(ModBlockEntities.PEDESTAL_BE, PedestalBlockEntityRenderer::new);

		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║         Loading Screens & Screen Handlers             ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");

		TutorialMod.LOGGER.info("✓ Registering Screen Handlers...");
		registerScreenHandlers();

		TutorialMod.LOGGER.info("║");
		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║  ✅ Tutorial Mod Client Loaded Successfully            ║");
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════╝");
	}

	/**
	 * Automated stamina system payload registration
	 */
	private static void registerStaminaPayloads() {
		// Stamina sync
		ClientPlayNetworking.registerGlobalReceiver(StaminaSyncPayload.ID, (payload, context) -> {
			double stamina = payload.stamina();
			double max = payload.maxStamina();
			context.client().execute(() -> StaminaHudOverlay.update(stamina, max));
		});

		// Sprint cooldown
		ClientPlayNetworking.registerGlobalReceiver(SprintCooldownPayload.ID, (payload, context) -> {
			int ticks = payload.cooldownTicks();
			context.client().execute(() -> StaminaHudOverlay.updateCooldown(ticks));
		});

		HudRenderCallback.EVENT.register(new StaminaHudOverlay());
	}

	/**
	 * Automated temperature system payload registration
	 */
	private static void registerTemperaturePayloads() {
		// Player temperature
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.TemperatureSyncPayload.ID, (payload, context) -> {
			double temperature = payload.temperature();
			context.client().execute(() -> StaminaHudOverlay.updateTemperature(temperature));
		});

		// World temperature
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.WorldTemperatureSyncPayload.ID, (payload, context) -> {
			double worldTemperature = payload.worldTemperature();
			context.client().execute(() -> StaminaHudOverlay.updateWorldTemperature(worldTemperature));
		});
	}

	/**
	 * Automated weather and environment system payload registration
	 */
	private static void registerWeatherAndEnvironmentPayloads() {
		// Weather notification
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.WeatherNotificationPayload.ID, (payload, context) -> {
			String message = payload.message();
			int color = payload.color();
			context.client().execute(() -> net.kaupenjoe.tutorialmod.event.WeatherNotificationHud.showNotification(message, color));
		});
		HudRenderCallback.EVENT.register(new net.kaupenjoe.tutorialmod.event.WeatherNotificationHud());

		// Biome notification
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.BiomeNotificationPayload.ID, (payload, context) -> {
			context.client().execute(() -> BiomeNotificationHud.showNotification(payload.message(), payload.color()));
		});
		HudRenderCallback.EVENT.register(new BiomeNotificationHud());

		// Wind system
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.WindSyncPayload.ID, (payload, context) -> {
			net.minecraft.util.math.Vec3d direction = new net.minecraft.util.math.Vec3d(payload.dirX(), payload.dirY(), payload.dirZ());
			double strength = payload.strength();
			boolean stormy = payload.stormy();
			context.client().execute(() -> {
				net.kaupenjoe.tutorialmod.event.WindLineRenderer.updateWindData(direction, strength, stormy);
				net.kaupenjoe.tutorialmod.event.StaminaHudOverlay.updateWindSpeed(strength);
			});
		});
		net.kaupenjoe.tutorialmod.event.WindLineRenderer.register();

		// Register swing attack handler
		net.kaupenjoe.tutorialmod.event.SwingAttackHandler.registerClient();

		// Register stamina sound effects
		net.kaupenjoe.tutorialmod.event.StaminaSoundEffects.register();
	}

	/**
	 * Automated chat and social system payload registration
	 */
	private static void registerChatAndSocialPayloads() {
		// Thirst system
		ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.ID, (payload, context) -> {
			context.client().execute(() -> UnifiedHudOverlay.updateThirst(payload.thirst(), payload.maxThirst()));
		});

		// Freecam countdown
		ClientPlayNetworking.registerGlobalReceiver(FreecamCountdownPayload.ID, (payload, context) -> {
			int remaining = payload.ticksRemaining();
			int total = payload.totalTicks();
			context.client().execute(() -> UnifiedHudOverlay.updateFreecamCountdown(remaining, total));
		});

		// Typing indicator
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.ID, (payload, context) -> {
			String playerName = payload.playerName();
			boolean isTyping = payload.isTyping();
			String partialText = payload.partialText();
			context.client().execute(() -> net.kaupenjoe.tutorialmod.event.TypingIndicatorHud.updateTypingState(playerName, isTyping, partialText));
		});

		// Animated chat
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload.ID, (payload, context) -> {
			String role = payload.role();
			String name = payload.name();
			String msg = payload.message();
			context.client().execute(() -> {
				// Modern color scheme matching the mixin
				int roleColor = switch(role) {
					case "MEMBER" -> 0x888888;
					case "SYS" -> 0x5599FF;
					case "GAME" -> 0x7744DD;
					case "VEIN" -> 0x44DD77;
					default -> 0x666666;
				};

				int nameColor = name.equals("Mojang") ? 0xFF3333 :
							   name.equals("System") ? 0x66AAFF :
							   name.equals("VeinMiner") ? 0x55DD88 :
							   0xFFDD55; // Default gold for players

				// Priority: GAME=2, VEIN=1, SYS=1, others=0
				int priority = switch(role) {
					case "GAME" -> 2;
					case "SYS", "VEIN" -> 1;
					default -> 0;
				};

				 var styled = net.minecraft.text.Text.empty()
						.append(net.minecraft.text.Text.literal("[" + role + "]").setStyle(net.minecraft.text.Style.EMPTY.withColor(roleColor)))
						.append(net.minecraft.text.Text.literal(" "))
						.append(net.minecraft.text.Text.literal(name).setStyle(net.minecraft.text.Style.EMPTY.withColor(nameColor).withBold(true)))
						.append(net.minecraft.text.Text.literal(" › ").setStyle(net.minecraft.text.Style.EMPTY.withColor(0x555555)))
						.append(net.minecraft.text.Text.literal(msg).setStyle(net.minecraft.text.Style.EMPTY.withColor(0xEEEEEE)));
				net.kaupenjoe.tutorialmod.event.AnimatedChatHud.push(styled, role, priority);
			});
		});

		// Advancement notification
		ClientPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.AdvancementNotificationPayload.ID, (payload, context) -> {
			String title = payload.title();
			String description = payload.description();
			String advancementId = payload.advancementId();
			net.minecraft.item.ItemStack icon = payload.icon();
			context.client().execute(() -> net.kaupenjoe.tutorialmod.event.AdvancementNotificationHud.showAdvancementNotification(title, description, advancementId, icon));
		});

		// Register all HUD overlays
		HudRenderCallback.EVENT.register(new UnifiedHudOverlay());
		HudRenderCallback.EVENT.register(new AnimatedChatHud());
		HudRenderCallback.EVENT.register(new net.kaupenjoe.tutorialmod.event.TypingIndicatorHud());
		HudRenderCallback.EVENT.register(new net.kaupenjoe.tutorialmod.event.AdvancementNotificationHud());
	}

	/**
	 * Automated miscellaneous system payload registration
	 */
	private static void registerMiscellaneousPayloads() {
		// Login streak system
		ClientPlayNetworking.registerGlobalReceiver(LoginStreakPayload.ID, (payload, context) -> {
			int streak = payload.streak();
			long day = payload.lastDay();
			boolean increased = payload.increased();
			boolean broken = payload.broken();
			int previous = payload.previous();
			context.client().execute(() -> LoginStreakHud.update(streak, day, increased, broken, previous));
		});

		HudRenderCallback.EVENT.register(new LoginStreakHud());
	}

	/**
	 * Automated block render layer registration
	 */
	private static void registerBlockRenderLayers() {
		BlockRenderLayerMap.putBlock(ModBlocks.PINK_GARNET_TRAPDOOR, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CAULIFLOWER_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.HONEY_BERRY_BUSH, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.DRIFTWOOD_SAPLING, BlockRenderLayer.CUTOUT);
	}

	/**
	 * Automated screen handler registration
	 */
	private static void registerScreenHandlers() {
		HandledScreens.register(ModScreenHandlers.PEDESTAL_SCREEN_HANDLER, PedestalScreen::new);
		HandledScreens.register(ModScreenHandlers.GROWTH_CHAMBER_SCREEN_HANDLER, GrowthChamberScreen::new);
	}
}
