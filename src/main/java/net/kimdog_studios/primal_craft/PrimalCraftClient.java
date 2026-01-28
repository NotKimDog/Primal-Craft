package net.kimdog_studios.primal_craft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.block.entity.ModBlockEntities;
import net.kimdog_studios.primal_craft.block.entity.renderer.PedestalBlockEntityRenderer;
import net.kimdog_studios.primal_craft.client.config.ModMenuConfigScreen;
import net.kimdog_studios.primal_craft.entity.ModEntities;
import net.kimdog_studios.primal_craft.entity.client.*;
import net.kimdog_studios.primal_craft.event.CameraResetHandler;
import net.kimdog_studios.primal_craft.event.StaminaHudOverlay;
import net.kimdog_studios.primal_craft.event.ZoomHandler;
import net.kimdog_studios.primal_craft.event.ZoomHudOverlay;
import net.kimdog_studios.primal_craft.particle.ModParticles;
import net.kimdog_studios.primal_craft.particle.PinkGarnetParticle;
import net.kimdog_studios.primal_craft.screen.ModScreenHandlers;
import net.kimdog_studios.primal_craft.screen.custom.GrowthChamberScreen;
import net.kimdog_studios.primal_craft.screen.custom.PedestalScreen;
import net.kimdog_studios.primal_craft.util.ModKeyBindings;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.kimdog_studios.primal_craft.network.StaminaSyncPayload;
import net.kimdog_studios.primal_craft.network.SprintCooldownPayload;
import net.kimdog_studios.primal_craft.network.ThirstSyncPayload;
import net.kimdog_studios.primal_craft.network.FreecamCountdownPayload;
import net.kimdog_studios.primal_craft.event.UnifiedHudOverlay;
import net.kimdog_studios.primal_craft.event.AnimatedChatHud;
import net.kimdog_studios.primal_craft.network.LoginStreakPayload;
import net.kimdog_studios.primal_craft.event.LoginStreakHud;
import net.kimdog_studios.primal_craft.event.WeatherNotificationHud;
import net.kimdog_studios.primal_craft.event.BiomeNotificationHud;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

@SuppressWarnings({"deprecation", "resource"})
public class PrimalCraftClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		long startTime = System.currentTimeMillis();
		PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
		PrimalCraft.LOGGER.info("║  INITIALIZING TUTORIALMOD CLIENT v1.21.X                   ║");
		PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

		try {
			PrimalCraft.LOGGER.info("  ⚙️  Initializing configuration system...");
			// Initialize config system
			ModMenuConfigScreen.initialize();
			PrimalCraft.LOGGER.debug("    ✓ Configuration system initialized");

			PrimalCraft.LOGGER.info("  ⌨️  Registering key bindings...");
			// Register keybindings
			ModKeyBindings.registerKeyBindings();
			PrimalCraft.LOGGER.debug("    ✓ Key bindings registered");

			PrimalCraft.LOGGER.info("  🎥 Registering camera and rendering systems...");
			// Register zoom tick event
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				ZoomHandler.tick();
				CameraResetHandler.tick();

				// Config menu key handler - toggle on/off
				if (ModKeyBindings.configMenuKey.wasPressed()) {
					if (client.currentScreen instanceof net.kimdog_studios.primal_craft.client.config.ModMenuIntegration.ConfigScreen) {
						// Close menu if already open
						client.setScreen(null);
						PrimalCraft.LOGGER.info("[CONFIG] Config menu closed via hotkey (])");
					} else if (client.currentScreen == null) {
						// Open menu if no other screen is open
						client.setScreen(new net.kimdog_studios.primal_craft.client.config.ModMenuIntegration.ConfigScreen(null));
						PrimalCraft.LOGGER.info("[CONFIG] Config menu opened via hotkey (])");
					}
				}
			});

			// Register zoom HUD overlay
			HudRenderCallback.EVENT.register(new ZoomHudOverlay());
			PrimalCraft.LOGGER.debug("    ✓ Zoom handler registered");

			PrimalCraft.LOGGER.info("  ⚡ Registering stamina system...");
			// Register stamina HUD + network receiver
			ClientPlayNetworking.registerGlobalReceiver(StaminaSyncPayload.ID, (payload, context) -> {
				double stamina = payload.stamina();
				double max = payload.maxStamina();
				context.client().execute(() -> StaminaHudOverlay.update(stamina, max));
			});

			ClientPlayNetworking.registerGlobalReceiver(SprintCooldownPayload.ID, (payload, context) -> {
				int ticks = payload.cooldownTicks();
				context.client().execute(() -> StaminaHudOverlay.updateCooldown(ticks));
			});

			ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.TemperatureSyncPayload.ID, (payload, context) -> {
				double temperature = payload.temperature();
				context.client().execute(() -> StaminaHudOverlay.updateTemperature(temperature));
			});

			ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.WorldTemperatureSyncPayload.ID, (payload, context) -> {
				double worldTemperature = payload.worldTemperature();
				context.client().execute(() -> StaminaHudOverlay.updateWorldTemperature(worldTemperature));
			});

			HudRenderCallback.EVENT.register(new StaminaHudOverlay());
			PrimalCraft.LOGGER.debug("    ✓ Stamina system initialized");

			PrimalCraft.LOGGER.info("  🎮 Registering difficulty system...");
			// Register difficulty HUD overlay
			net.kimdog_studios.primal_craft.event.DifficultyHudOverlay.register();
			PrimalCraft.LOGGER.debug("    ✓ Difficulty system initialized");

			PrimalCraft.LOGGER.info("  🎨 Registering UI improvements...");
			// Register debug HUD remover
			net.kimdog_studios.primal_craft.event.DebugHudRemover.register();
			// Register difficulty color coding
			net.kimdog_studios.primal_craft.event.DifficultyColorHandler.register();
			// Register day transition animation
			net.kimdog_studios.primal_craft.event.DayTransitionHandler.register();
			// Register third-person name display
			net.kimdog_studios.primal_craft.event.ThirdPersonNameHandler.register();
			// Register performance metrics HUD
			net.kimdog_studios.primal_craft.event.PerformanceMetricsHud.register();
			// Register window title customizer
			net.kimdog_studios.primal_craft.event.WindowTitleCustomizer.register();
			// Register custom window icon
			net.kimdog_studios.primal_craft.event.CustomWindowIcon.register();
			// Register fullscreen auto-launch
			net.kimdog_studios.primal_craft.event.FullscreenAutoLaunchHandler.register();
			// Register dynamic FPS optimizer
			net.kimdog_studios.primal_craft.event.DynamicFpsOptimizer.register();
			// Register drop confirmation
			net.kimdog_studios.primal_craft.event.DropConfirmationHandler.register();
			// Register dynamic lights
			net.kimdog_studios.primal_craft.event.DynamicLightsHandler.register();
			// Register Hytale Feel meta feature
			net.kimdog_studios.primal_craft.event.HytaleFeel.register();
			PrimalCraft.LOGGER.debug("    ✓ UI improvements registered");

			PrimalCraft.LOGGER.info("  🌦️  Registering weather and environment systems...");
			// Register weather notification client receiver + HUD
			ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.WeatherNotificationPayload.ID, (payload, context) -> {
				String message = payload.message();
				int color = payload.color();
				context.client().execute(() -> net.kimdog_studios.primal_craft.event.WeatherNotificationHud.showNotification(message, color));
			});
			HudRenderCallback.EVENT.register(new net.kimdog_studios.primal_craft.event.WeatherNotificationHud());

			ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.BiomeNotificationPayload.ID, (payload, context) -> {
				context.client().execute(() -> BiomeNotificationHud.showNotification(payload.message(), payload.color()));
			});
			HudRenderCallback.EVENT.register(new BiomeNotificationHud());

			// Register wind system client receiver + renderer
			ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.WindSyncPayload.ID, (payload, context) -> {
				net.minecraft.util.math.Vec3d direction = new net.minecraft.util.math.Vec3d(payload.dirX(), payload.dirY(), payload.dirZ());
				double strength = payload.strength();
				boolean stormy = payload.stormy();
				context.client().execute(() -> {
					net.kimdog_studios.primal_craft.event.WindLineRenderer.updateWindData(direction, strength, stormy);
					net.kimdog_studios.primal_craft.event.StaminaHudOverlay.updateWindSpeed(strength);
			});
		});
		net.kimdog_studios.primal_craft.event.WindLineRenderer.register();

		// Register swing attack client handler
		net.kimdog_studios.primal_craft.event.SwingAttackHandler.registerClient();

		// Register stamina sound effects
		net.kimdog_studios.primal_craft.event.StaminaSoundEffects.register();

		BlockRenderLayerMap.putBlock(ModBlocks.PINK_GARNET_TRAPDOOR, BlockRenderLayer.CUTOUT);

		BlockRenderLayerMap.putBlock(ModBlocks.CAULIFLOWER_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.HONEY_BERRY_BUSH, BlockRenderLayer.CUTOUT);

		BlockRenderLayerMap.putBlock(ModBlocks.DRIFTWOOD_SAPLING, BlockRenderLayer.CUTOUT);

		EntityModelLayerRegistry.registerModelLayer(MantisModel.MANTIS, MantisModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MANTIS, MantisRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(TomahawkProjectileModel.TOMAHAWK, TomahawkProjectileModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.TOMAHAWK, TomahawkProjectileRenderer::new);

		EntityRendererRegistry.register(ModEntities.CHAIR, ChairRenderer::new);

		ParticleFactoryRegistry.getInstance().register(ModParticles.PINK_GARNET_PARTICLE, PinkGarnetParticle.Factory::new);

		BlockEntityRendererFactories.register(ModBlockEntities.PEDESTAL_BE, PedestalBlockEntityRenderer::new);
		HandledScreens.register(ModScreenHandlers.PEDESTAL_SCREEN_HANDLER, PedestalScreen::new);

		HandledScreens.register(ModScreenHandlers.GROWTH_CHAMBER_SCREEN_HANDLER, GrowthChamberScreen::new);

		ClientPlayNetworking.registerGlobalReceiver(ThirstSyncPayload.ID, (payload, context) -> {
			context.client().execute(() -> UnifiedHudOverlay.updateThirst(payload.thirst(), payload.maxThirst()));
		});

		ClientPlayNetworking.registerGlobalReceiver(FreecamCountdownPayload.ID, (payload, context) -> {
			int remaining = payload.ticksRemaining();
			int total = payload.totalTicks();
			context.client().execute(() -> UnifiedHudOverlay.updateFreecamCountdown(remaining, total));
		});
		HudRenderCallback.EVENT.register(new UnifiedHudOverlay());
		HudRenderCallback.EVENT.register(new AnimatedChatHud());
		HudRenderCallback.EVENT.register(new net.kimdog_studios.primal_craft.event.TypingIndicatorHud());

		ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.ID, (payload, context) -> {
			String playerName = payload.playerName();
			boolean isTyping = payload.isTyping();
			String partialText = payload.partialText();
			context.client().execute(() -> net.kimdog_studios.primal_craft.event.TypingIndicatorHud.updateTypingState(playerName, isTyping, partialText));
		});

		// Register sign editor payload and receiver
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
			net.kimdog_studios.primal_craft.network.OpenSignEditorPayload.ID,
			net.kimdog_studios.primal_craft.network.OpenSignEditorPayload.CODEC
		);
		ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.OpenSignEditorPayload.ID, (payload, context) -> {
			net.minecraft.util.math.BlockPos pos = payload.pos();
			context.client().execute(() -> {
				if (context.client().world != null) {
					net.minecraft.block.entity.BlockEntity blockEntity = context.client().world.getBlockEntity(pos);
					if (blockEntity instanceof net.minecraft.block.entity.SignBlockEntity signEntity) {
						context.client().setScreen(new net.kimdog_studios.primal_craft.screen.custom.CustomSignEditorScreen(pos, signEntity));
					}
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.ChatAnimatedPayload.ID, (payload, context) -> {
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
				net.kimdog_studios.primal_craft.event.AnimatedChatHud.push(styled, role, priority);
			});
		});

		// Register advancement notification receiver
		ClientPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.AdvancementNotificationPayload.ID, (payload, context) -> {
			String title = payload.title();
			String description = payload.description();
			String advancementId = payload.advancementId();
			net.minecraft.item.ItemStack icon = payload.icon();
			context.client().execute(() -> net.kimdog_studios.primal_craft.event.AdvancementNotificationHud.showAdvancementNotification(title, description, advancementId, icon));
		});

		// Register advancement notification HUD
		HudRenderCallback.EVENT.register(new net.kimdog_studios.primal_craft.event.AdvancementNotificationHud());

        ClientPlayNetworking.registerGlobalReceiver(LoginStreakPayload.ID, (payload, context) -> {
            int streak = payload.streak();
            long day = payload.lastDay();
            boolean increased = payload.increased();
            boolean broken = payload.broken();
            int previous = payload.previous();
            context.client().execute(() -> LoginStreakHud.update(streak, day, increased, broken, previous));
        });
        HudRenderCallback.EVENT.register(new LoginStreakHud());

        // Register world join listener to display config
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            net.kimdog_studios.primal_craft.client.config.ConfigDisplayHandler.onClientStart();
        });

        long elapsed = System.currentTimeMillis() - startTime;
        PrimalCraft.LOGGER.info("  ✓ All client systems initialized");
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  TUTORIALMOD CLIENT INITIALIZATION COMPLETE                ║");
        PrimalCraft.LOGGER.info("║  Total Initialization Time: {}ms                           ║", elapsed);
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("❌ CRITICAL ERROR during TutorialModClient initialization!", e);
            throw new RuntimeException("Failed to initialize TutorialModClient", e);
        }
	}
}
