package net.kimdog_studios.primal_craft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.*;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.block.entity.ModBlockEntities;
import net.kimdog_studios.primal_craft.component.ModDataComponentTypes;
import net.kimdog_studios.primal_craft.effect.ModEffects;
import net.kimdog_studios.primal_craft.enchantment.ModEnchantmentEffects;
import net.kimdog_studios.primal_craft.entity.ModEntities;
import net.kimdog_studios.primal_craft.entity.custom.MantisEntity;
import net.kimdog_studios.primal_craft.item.ModItemGroups;
import net.kimdog_studios.primal_craft.item.ModItems;
import net.kimdog_studios.primal_craft.network.ChatAnimatedPayload;
import net.kimdog_studios.primal_craft.network.LoginStreakPayload;
import net.kimdog_studios.primal_craft.particle.ModParticles;
import net.kimdog_studios.primal_craft.potion.ModPotions;
import net.kimdog_studios.primal_craft.recipe.ModRecipes;
import net.kimdog_studios.primal_craft.screen.ModScreenHandlers;
import net.kimdog_studios.primal_craft.sound.ModSounds;
import net.kimdog_studios.primal_craft.util.HammerUsageEvent;
import net.kimdog_studios.primal_craft.event.VeinMinerHandler;
import net.kimdog_studios.primal_craft.util.ModLootTableModifiers;
import net.kimdog_studios.primal_craft.villager.ModVillagers;
import net.kimdog_studios.primal_craft.world.gen.ModWorldGeneration;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kimdog_studios.primal_craft.network.StaminaSyncPayload;
import net.kimdog_studios.primal_craft.network.SprintCooldownPayload;
import net.kimdog_studios.primal_craft.network.SwingAttackPayload;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Very important comment
public class PrimalCraft implements ModInitializer {
	public static final String MOD_ID = "primal-craft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        long startTime = System.currentTimeMillis();

        // Helper for animated chat messages
        java.util.function.BiConsumer<net.minecraft.server.network.ServerPlayerEntity, String> sendAnimated = (sp, msg) ->
            ServerPlayNetworking.send(sp, new ChatAnimatedPayload("SYS", "System", msg));

        LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        LOGGER.info("║  INITIALIZING TUTORIALMOD v1.21.X                         ║");
        LOGGER.info("║  © KimDog Studios                                          ║");
        LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        LOGGER.info("  🔧 Starting mod initialization...");

        try {
            LOGGER.info("  📦 Registering item groups...");
            ModItemGroups.registerItemGroups();
            LOGGER.debug("    ✓ Item groups registered");

            LOGGER.info("  📦 Registering items and blocks...");
            ModItems.registerModItems();
            ModBlocks.registerModBlocks();
            LOGGER.debug("    ✓ {} items and blocks registered", "custom count");

            LOGGER.info("  🎨 Registering data components...");
            ModDataComponentTypes.registerDataComponentTypes();
            LOGGER.debug("    ✓ Data component types registered");

            LOGGER.info("  🔊 Registering sounds...");
            ModSounds.registerSounds();
            LOGGER.debug("    ✓ Sounds registered");

            LOGGER.info("  ✨ Registering effects and potions...");
            ModEffects.registerEffects();
            ModPotions.registerPotions();
            LOGGER.debug("    ✓ Effects and potions registered");

            LOGGER.info("  ✨ Registering enchantments...");
            ModEnchantmentEffects.registerEnchantmentEffects();
            net.kimdog_studios.primal_craft.enchantment.LightningTaskManager.init();
            LOGGER.debug("    ✓ Enchantments initialized");

            LOGGER.info("  🌍 Registering world generation...");
            ModWorldGeneration.generateModWorldGen();
            LOGGER.debug("    ✓ World generation configured");

            LOGGER.info("  👹 Registering entities...");
            ModEntities.registerModEntities();
            FabricDefaultAttributeRegistry.register(ModEntities.MANTIS, MantisEntity.createAttributes());
            LOGGER.debug("    ✓ Entities registered");

            LOGGER.info("  🏘 Registering villagers...");
            ModVillagers.registerVillagers();
            LOGGER.debug("    ✓ Villagers registered");

            LOGGER.info("  ✨ Registering particles...");
            ModParticles.registerParticles();
            LOGGER.debug("    ✓ Particles registered");

            LOGGER.info("  📚 Registering loot table modifiers...");
            ModLootTableModifiers.modifyLootTables();
            LOGGER.debug("    ✓ Loot tables modified");

            LOGGER.info("  🧱 Registering block entities and screens...");
            ModBlockEntities.registerBlockEntities();
            ModScreenHandlers.registerScreenHandlers();
            LOGGER.debug("    ✓ Block entities and screens registered");

            LOGGER.info("  👨‍🍳 Registering recipes...");
            ModRecipes.registerRecipes();
            LOGGER.debug("    ✓ Recipes registered");

            LOGGER.info("  🌐 Registering network payloads...");
            int payloadsRegistered = 0;
            // Register stamina payload codec (S2C)
            PayloadTypeRegistry.playS2C().register(StaminaSyncPayload.ID, StaminaSyncPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(SprintCooldownPayload.ID, SprintCooldownPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.TemperatureSyncPayload.ID,
                net.kimdog_studios.primal_craft.network.TemperatureSyncPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.WorldTemperatureSyncPayload.ID,
                net.kimdog_studios.primal_craft.network.WorldTemperatureSyncPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.ThirstSyncPayload.ID, net.kimdog_studios.primal_craft.network.ThirstSyncPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.FreecamCountdownPayload.ID, net.kimdog_studios.primal_craft.network.FreecamCountdownPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.ChatAnimatedPayload.ID, net.kimdog_studios.primal_craft.network.ChatAnimatedPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.ID, net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.BiomeNotificationPayload.ID, net.kimdog_studios.primal_craft.network.BiomeNotificationPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(LoginStreakPayload.ID, LoginStreakPayload.CODEC);
            payloadsRegistered += 9;
            LOGGER.debug("    ✓ {} S2C (Server→Client) payloads registered", payloadsRegistered);

            // Register swing attack payload codec (C2S)
            PayloadTypeRegistry.playC2S().register(net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.ID, net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.CODEC);
            PayloadTypeRegistry.playC2S().register(SwingAttackPayload.ID, SwingAttackPayload.CODEC);
            payloadsRegistered += 2;
            LOGGER.debug("    ✓ {} C2S (Client→Server) payloads registered", payloadsRegistered);

        // Stamina system (server-side regen + API)
        net.kimdog_studios.primal_craft.util.StaminaSystem.register();

        // Default action stamina hooks (sprint/jump/attack/use/break)
        net.kimdog_studios.primal_craft.event.StaminaHooks.register();

        // Stamina restoration system (rest/food)
        net.kimdog_studios.primal_craft.util.StaminaRestoration.register();

        // Swing attack stamina drain handler
        net.kimdog_studios.primal_craft.event.SwingAttackHandler.registerServer();

        // Sleep stamina restoration
        net.kimdog_studios.primal_craft.event.SleepStaminaHandler.register();

        // Mount/Elytra stamina handler
        // net.kimdog_studios.primal_craft.event.MountStaminaHandler.register();

        // Temperature effects handler (applies status effects based on temp)
        net.kimdog_studios.primal_craft.event.TemperatureEffectsHandler.register();

            LOGGER.info("  🎮 Registering Minecraft overhaul systems...");
            // ===== COMPLETE MINECRAFT OVERHAUL =====

        // Hunger Overhaul - Makes food meaningful, faster depletion, affects healing
        net.kimdog_studios.primal_craft.event.HungerOverhaulHandler.register();

        // Exhaustion System - Movement and activity cause fatigue that affects performance
        net.kimdog_studios.primal_craft.event.ExhaustionHandler.register();

        // Sleep Overhaul - Sleep is now CRUCIAL - skipping sleep has severe consequences
        net.kimdog_studios.primal_craft.event.SleepOverhaulHandler.register();

        // Environmental Hazards - Biomes are dangerous (cold, heat, altitude)
        net.kimdog_studios.primal_craft.event.EnvironmentHazardsHandler.register();

        // Day/Night Cycle Overhaul - Day gives benefits, night is threatening
        net.kimdog_studios.primal_craft.event.DayNightOverhaulHandler.register();

        // Threat System - Mobs and darkness create real danger
        net.kimdog_studios.primal_craft.event.ThreatSystemHandler.register();

        // Weather notification system
        net.kimdog_studios.primal_craft.network.WeatherNotificationPayload.register();

        // Biome notification system with wind info
        net.kimdog_studios.primal_craft.event.BiomeNotificationHandler.register();

        // Wind system
        net.kimdog_studios.primal_craft.network.WindSyncPayload.register();
        net.kimdog_studios.primal_craft.event.WindHandler.register();
        net.kimdog_studios.primal_craft.event.WeatherParticleHandler.register();

        // Typing indicator system
        net.kimdog_studios.primal_craft.event.TypingIndicatorHandler.register();
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(net.kimdog_studios.primal_craft.network.TypingIndicatorPayload.ID, (payload, context) -> {
            net.minecraft.server.network.ServerPlayerEntity player = context.player();
            if (player != null) {
                if (payload.isTyping()) {
                    net.kimdog_studios.primal_craft.event.TypingIndicatorHandler.notifyTyping(player, payload.partialText());
                } else {
                    net.kimdog_studios.primal_craft.event.TypingIndicatorHandler.stopTyping(player);
                }
            }
        });

        // Register advancement notification system
        net.kimdog_studios.primal_craft.event.AdvancementNotificationHandler.register();
        PayloadTypeRegistry.playS2C().register(net.kimdog_studios.primal_craft.network.AdvancementNotificationPayload.ID, net.kimdog_studios.primal_craft.network.AdvancementNotificationPayload.CODEC);

        // Register custom weather command on server start
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            var dispatcher = server.getCommandManager().getDispatcher();
            net.kimdog_studios.primal_craft.command.VanishCommand.register(dispatcher);
            net.kimdog_studios.primal_craft.command.FreecamCommand.register(dispatcher);
            net.kimdog_studios.primal_craft.command.DashboardCommand.register(dispatcher);

            // Set up web dashboard endpoints
            net.kimdog_studios.primal_craft.web.api.ChatEndpoint.setServer(server);
            net.kimdog_studios.primal_craft.web.api.StatusEndpoint.setServer(server);

            // Auto-start web dashboard (optional - can be disabled)
            if (System.getenv("PRIMAL_CRAFT_DASHBOARD") == null || !System.getenv("PRIMAL_CRAFT_DASHBOARD").equals("disabled")) {
                net.kimdog_studios.primal_craft.web.server.WebServer.start();
            }
        });

        // Stop web server on shutdown
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            net.kimdog_studios.primal_craft.web.server.WebServer.stop();
        });


		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(ModItems.STARLIGHT_ASHES, 600);
		});

		PlayerBlockBreakEvents.BEFORE.register(new HammerUsageEvent());
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(entity instanceof SheepEntity sheepEntity) {
				if(player.getMainHandStack().getItem() == Items.END_ROD) {
					if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                        sendAnimated.accept(sp, "The Player just hit a sheep with an END ROD! YOU SICK FRICK!");
                    }
					player.getMainHandStack().decrement(1);
					sheepEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600, 6));
				}

				return ActionResult.PASS;
			}

            return ActionResult.PASS;
        });

		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerPotionRecipe(Potions.AWKWARD, Items.SLIME_BALL, ModPotions.SLIMEY_POTION);
		});

		CompostingChanceRegistry.INSTANCE.add(ModItems.CAULIFLOWER, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.CAULIFLOWER_SEEDS, 0.25f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.HONEY_BERRIES, 0.15f);


		StrippableBlockRegistry.register(ModBlocks.DRIFTWOOD_LOG, ModBlocks.STRIPPED_DRIFTWOOD_LOG);
		StrippableBlockRegistry.register(ModBlocks.DRIFTWOOD_WOOD, ModBlocks.STRIPPED_DRIFTWOOD_WOOD);

		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.STRIPPED_DRIFTWOOD_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.STRIPPED_DRIFTWOOD_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_PLANKS, 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_LEAVES, 30, 60);

		FabricDefaultAttributeRegistry.register(ModEntities.MANTIS, MantisEntity.createAttributes());

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 1, factories -> {
			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 3),
					new ItemStack(ModItems.CAULIFLOWER, 8), 7, 2, 0.04f));

			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.DIAMOND, 9),
					new ItemStack(ModItems.CAULIFLOWER_SEEDS, 2), 3, 4, 0.04f));
		});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 2, factories -> {
			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 12),
					new ItemStack(ModItems.HONEY_BERRIES, 5), 4, 7, 0.04f));
		});

		TradeOfferHelper.registerVillagerOffers(ModVillagers.KAPUENGER_KEY, 1, factories -> {
			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 10),
					new ItemStack(ModItems.CHISEL, 1), 4, 7, 0.04f));

			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 16),
					new ItemStack(ModItems.RAW_PINK_GARNET, 1), 4, 7, 0.04f));
		});

		TradeOfferHelper.registerVillagerOffers(ModVillagers.KAPUENGER_KEY, 2, factories -> {
			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 10),
					new ItemStack(ModItems.CHISEL, 1), 4, 7, 0.04f));

			factories.add((world,entity, random) -> new TradeOffer(
					new TradedItem(ModItems.PINK_GARNET, 16),
					new ItemStack(ModItems.TOMAHAWK, 1), 3, 12, 0.09f));
		});

		TradeOfferHelper.registerWanderingTraderOffers(factories -> {
			factories.addAll(Identifier.of(PrimalCraft.MOD_ID, "emerald_for_chisel"), (world, entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 10),
					new ItemStack(ModItems.CHISEL, 1), 4, 7, 0.04f));

			factories.addAll(Identifier.of(PrimalCraft.MOD_ID, "garnet_tomahawk"), (world, entity, random) -> new TradeOffer(
					new TradedItem(ModItems.PINK_GARNET, 16),
					new ItemStack(ModItems.TOMAHAWK, 1), 3, 12, 0.09f));
		});

        // Styled chat formatter
        net.kimdog_studios.primal_craft.event.ChatFormatter.register();

        // Thirst system and water bottle drinking
        net.kimdog_studios.primal_craft.util.ThirstSystem.register();
        net.kimdog_studios.primal_craft.event.DrinkWaterBottleHandler.register();

        // Inventory temperature tracking and modifiers
        net.kimdog_studios.primal_craft.event.InventoryTemperatureHandler.register();

        net.kimdog_studios.primal_craft.event.LoginStreakHandler.register();

        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.info("  ✓ All systems initialized");
        LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        LOGGER.info("║  TUTORIALMOD INITIALIZATION COMPLETE                       ║");
        LOGGER.info("║  Total Initialization Time: {}ms                           ║", elapsed);
        LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            LOGGER.error("❌ CRITICAL ERROR during TutorialMod initialization!", e);
            throw new RuntimeException("Failed to initialize TutorialMod", e);
        }
	}
}
