package net.kaupenjoe.tutorialmod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.*;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.block.entity.ModBlockEntities;
import net.kaupenjoe.tutorialmod.component.ModDataComponentTypes;
import net.kaupenjoe.tutorialmod.effect.ModEffects;
import net.kaupenjoe.tutorialmod.enchantment.ModEnchantmentEffects;
import net.kaupenjoe.tutorialmod.entity.ModEntities;
import net.kaupenjoe.tutorialmod.entity.custom.MantisEntity;
import net.kaupenjoe.tutorialmod.item.ModItemGroups;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload;
import net.kaupenjoe.tutorialmod.network.LoginStreakPayload;
import net.kaupenjoe.tutorialmod.particle.ModParticles;
import net.kaupenjoe.tutorialmod.potion.ModPotions;
import net.kaupenjoe.tutorialmod.recipe.ModRecipes;
import net.kaupenjoe.tutorialmod.screen.ModScreenHandlers;
import net.kaupenjoe.tutorialmod.sound.ModSounds;
import net.kaupenjoe.tutorialmod.util.HammerUsageEvent;
import net.kaupenjoe.tutorialmod.event.VeinMinerHandler;
import net.kaupenjoe.tutorialmod.util.ModLootTableModifiers;
import net.kaupenjoe.tutorialmod.villager.ModVillagers;
import net.kaupenjoe.tutorialmod.world.gen.ModWorldGeneration;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kaupenjoe.tutorialmod.network.StaminaSyncPayload;
import net.kaupenjoe.tutorialmod.network.SprintCooldownPayload;
import net.kaupenjoe.tutorialmod.network.SwingAttackPayload;
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
public class TutorialMod implements ModInitializer {
	public static final String MOD_ID = "tutorialmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("╔════════════════════════════════════════════════════════╗");
		LOGGER.info("║     KimDog SMP - Tutorial Mod Initialization Started   ║");
		LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Helper for animated chat messages
        java.util.function.BiConsumer<net.minecraft.server.network.ServerPlayerEntity, String> sendAnimated = (sp, msg) ->
            ServerPlayNetworking.send(sp, new ChatAnimatedPayload("SYS", "System", msg));

		LOGGER.info("✓ Loading Item Groups...");
		ModItemGroups.registerItemGroups();

		LOGGER.info("✓ Loading Items...");
		ModItems.registerModItems();

		LOGGER.info("✓ Loading Blocks...");
		ModBlocks.registerModBlocks();

		LOGGER.info("✓ Registering Data Component Types...");
		ModDataComponentTypes.registerDataComponentTypes();

		LOGGER.info("✓ Registering Sounds...");
		ModSounds.registerSounds();

		LOGGER.info("✓ Registering Status Effects...");
		ModEffects.registerEffects();

		LOGGER.info("✓ Registering Potions...");
		ModPotions.registerPotions();

		LOGGER.info("✓ Registering Enchantment Effects...");
		ModEnchantmentEffects.registerEnchantmentEffects();

		// Initialize lightning task manager so chained strikes run on server ticks
		LOGGER.info("✓ Initializing Lightning Task Manager...");
		net.kaupenjoe.tutorialmod.enchantment.LightningTaskManager.init();

		LOGGER.info("✓ Generating World Generation Features...");
		ModWorldGeneration.generateModWorldGen();

		LOGGER.info("✓ Registering Entities...");
		ModEntities.registerModEntities();

		LOGGER.info("✓ Registering Villagers...");
		ModVillagers.registerVillagers();

		LOGGER.info("✓ Registering Particles...");
		ModParticles.registerParticles();

		LOGGER.info("✓ Modifying Loot Tables...");
		ModLootTableModifiers.modifyLootTables();

		LOGGER.info("✓ Registering Block Entities...");
		ModBlockEntities.registerBlockEntities();

		LOGGER.info("✓ Registering Screen Handlers...");
		ModScreenHandlers.registerScreenHandlers();

		LOGGER.info("✓ Registering Recipes...");
		ModRecipes.registerRecipes();

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║           Loading Core Systems & Event Handlers        ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Register VeinMiner
        LOGGER.info("✓ Registering Vein Miner...");
        VeinMinerHandler.register();

        // Register stamina payload codec (S2C)
        LOGGER.info("✓ Registering Network Payloads (S2C)...");
        PayloadTypeRegistry.playS2C().register(StaminaSyncPayload.ID, StaminaSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SprintCooldownPayload.ID, SprintCooldownPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.TemperatureSyncPayload.ID,
            net.kaupenjoe.tutorialmod.network.TemperatureSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.WorldTemperatureSyncPayload.ID,
            net.kaupenjoe.tutorialmod.network.WorldTemperatureSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.ThirstSyncPayload.ID, net.kaupenjoe.tutorialmod.network.ThirstSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.FreecamCountdownPayload.ID, net.kaupenjoe.tutorialmod.network.FreecamCountdownPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload.ID, net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.ID, net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.BiomeNotificationPayload.ID, net.kaupenjoe.tutorialmod.network.BiomeNotificationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LoginStreakPayload.ID, LoginStreakPayload.CODEC);

        // Register swing attack payload codec (C2S)
        LOGGER.info("✓ Registering Network Payloads (C2S)...");
        PayloadTypeRegistry.playC2S().register(net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.ID, net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SwingAttackPayload.ID, SwingAttackPayload.CODEC);

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║           Loading Stamina & Survival Systems           ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Stamina system (server-side regen + API)
        LOGGER.info("✓ Registering Stamina System...");
        net.kaupenjoe.tutorialmod.util.StaminaSystem.register();

        // Default action stamina hooks (sprint/jump/attack/use/break)
        LOGGER.info("✓ Registering Stamina Hooks...");
        net.kaupenjoe.tutorialmod.event.StaminaHooks.register();

        // Stamina restoration system (rest/food)
        LOGGER.info("✓ Registering Stamina Restoration...");
        net.kaupenjoe.tutorialmod.util.StaminaRestoration.register();

        // Swing attack stamina drain handler
        LOGGER.info("✓ Registering Swing Attack Handler...");
        net.kaupenjoe.tutorialmod.event.SwingAttackHandler.registerServer();

        // Sleep stamina restoration
        LOGGER.info("✓ Registering Sleep Stamina Restoration...");
        net.kaupenjoe.tutorialmod.event.SleepStaminaHandler.register();

        // Temperature effects handler (applies status effects based on temp)
        LOGGER.info("✓ Registering Temperature Effects Handler...");
        net.kaupenjoe.tutorialmod.event.TemperatureEffectsHandler.register();

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║        Loading Complete Minecraft Overhaul Systems     ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Hunger Overhaul - Makes food meaningful, faster depletion, affects healing
        LOGGER.info("✓ Registering Hunger Overhaul...");
        net.kaupenjoe.tutorialmod.event.HungerOverhaulHandler.register();

        // Exhaustion System - Movement and activity cause fatigue that affects performance
        LOGGER.info("✓ Registering Exhaustion System...");
        net.kaupenjoe.tutorialmod.event.ExhaustionHandler.register();

        // Sleep Overhaul - Sleep is now CRUCIAL - skipping sleep has severe consequences
        LOGGER.info("✓ Registering Sleep Overhaul...");
        net.kaupenjoe.tutorialmod.event.SleepOverhaulHandler.register();

        // Environmental Hazards - Biomes are dangerous (cold, heat, altitude)
        LOGGER.info("✓ Registering Environmental Hazards...");
        net.kaupenjoe.tutorialmod.event.EnvironmentHazardsHandler.register();

        // Day/Night Cycle Overhaul - Day gives benefits, night is threatening
        LOGGER.info("✓ Registering Day/Night Cycle Overhaul...");
        net.kaupenjoe.tutorialmod.event.DayNightOverhaulHandler.register();

        // Threat System - Mobs and darkness create real danger
        LOGGER.info("✓ Registering Threat System...");
        net.kaupenjoe.tutorialmod.event.ThreatSystemHandler.register();

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║       Loading Notification & Environmental Systems     ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Weather notification system
        LOGGER.info("✓ Registering Weather Notification System...");
        net.kaupenjoe.tutorialmod.network.WeatherNotificationPayload.register();

        // Biome notification system with wind info
        LOGGER.info("✓ Registering Biome Notification Handler...");
        net.kaupenjoe.tutorialmod.event.BiomeNotificationHandler.register();

        // Wind system
        LOGGER.info("✓ Registering Wind System...");
        net.kaupenjoe.tutorialmod.network.WindSyncPayload.register();
        net.kaupenjoe.tutorialmod.event.WindHandler.register();
        net.kaupenjoe.tutorialmod.event.WeatherParticleHandler.register();

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║         Loading Chat & Social Systems                  ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Typing indicator system
        LOGGER.info("✓ Registering Typing Indicator System...");
        net.kaupenjoe.tutorialmod.event.TypingIndicatorHandler.register();
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload.ID, (payload, context) -> {
            net.minecraft.server.network.ServerPlayerEntity player = context.player();
            if (player != null) {
                if (payload.isTyping()) {
                    net.kaupenjoe.tutorialmod.event.TypingIndicatorHandler.notifyTyping(player, payload.partialText());
                } else {
                    net.kaupenjoe.tutorialmod.event.TypingIndicatorHandler.stopTyping(player);
                }
            }
        });

        // Register advancement notification system
        LOGGER.info("✓ Registering Advancement Notification System...");
        net.kaupenjoe.tutorialmod.event.AdvancementNotificationHandler.register();
        PayloadTypeRegistry.playS2C().register(net.kaupenjoe.tutorialmod.network.AdvancementNotificationPayload.ID, net.kaupenjoe.tutorialmod.network.AdvancementNotificationPayload.CODEC);

        // Styled chat formatter
        LOGGER.info("✓ Registering Chat Formatter...");
        net.kaupenjoe.tutorialmod.event.ChatFormatter.register();

        // Thirst system and water bottle drinking
        LOGGER.info("✓ Registering Thirst System...");
        net.kaupenjoe.tutorialmod.util.ThirstSystem.register();
        net.kaupenjoe.tutorialmod.event.DrinkWaterBottleHandler.register();

        // Inventory temperature tracking and modifiers
        LOGGER.info("✓ Registering Inventory Temperature Handler...");
        net.kaupenjoe.tutorialmod.event.InventoryTemperatureHandler.register();

        LOGGER.info("✓ Registering Login Streak System...");
        net.kaupenjoe.tutorialmod.event.LoginStreakHandler.register();

        LOGGER.info("╔════════════════════════════════════════════════════════╗");
        LOGGER.info("║         Loading Vanilla Registry Modifications         ║");
        LOGGER.info("╚════════════════════════════════════════════════════════╝");

        // Register custom weather command on server start
        LOGGER.info("✓ Registering Custom Commands...");
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            var dispatcher = server.getCommandManager().getDispatcher();
            net.kaupenjoe.tutorialmod.command.VanishCommand.register(dispatcher);
            net.kaupenjoe.tutorialmod.command.FreecamCommand.register(dispatcher);
        });


		LOGGER.info("✓ Registering Fuel Registry...");
		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(ModItems.STARLIGHT_ASHES, 600);
		});

		LOGGER.info("✓ Registering Event Callbacks...");
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

		LOGGER.info("✓ Registering Brewing Recipes...");
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerPotionRecipe(Potions.AWKWARD, Items.SLIME_BALL, ModPotions.SLIMEY_POTION);
		});

		LOGGER.info("✓ Registering Composting...");
		CompostingChanceRegistry.INSTANCE.add(ModItems.CAULIFLOWER, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.CAULIFLOWER_SEEDS, 0.25f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.HONEY_BERRIES, 0.15f);


		LOGGER.info("✓ Registering Strippable Blocks...");
		StrippableBlockRegistry.register(ModBlocks.DRIFTWOOD_LOG, ModBlocks.STRIPPED_DRIFTWOOD_LOG);
		StrippableBlockRegistry.register(ModBlocks.DRIFTWOOD_WOOD, ModBlocks.STRIPPED_DRIFTWOOD_WOOD);

		LOGGER.info("✓ Registering Flammable Blocks...");
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.STRIPPED_DRIFTWOOD_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.STRIPPED_DRIFTWOOD_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_PLANKS, 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.DRIFTWOOD_LEAVES, 30, 60);

		LOGGER.info("✓ Registering Entity Attributes...");
		FabricDefaultAttributeRegistry.register(ModEntities.MANTIS, MantisEntity.createAttributes());

		LOGGER.info("✓ Registering Villager Trades...");
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

		LOGGER.info("✓ Registering Wandering Trader Offers...");
		TradeOfferHelper.registerWanderingTraderOffers(factories -> {
			factories.addAll(Identifier.of(TutorialMod.MOD_ID, "emerald_for_chisel"), (world, entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 10),
					new ItemStack(ModItems.CHISEL, 1), 4, 7, 0.04f));

			factories.addAll(Identifier.of(TutorialMod.MOD_ID, "garnet_tomahawk"), (world, entity, random) -> new TradeOffer(
					new TradedItem(ModItems.PINK_GARNET, 16),
					new ItemStack(ModItems.TOMAHAWK, 1), 3, 12, 0.09f));
		});

		LOGGER.info("╔════════════════════════════════════════════════════════╗");
		LOGGER.info("║     ✅ KimDog SMP - Tutorial Mod Loaded Successfully   ║");
		LOGGER.info("╚════════════════════════════════════════════════════════╝");
	}
}
