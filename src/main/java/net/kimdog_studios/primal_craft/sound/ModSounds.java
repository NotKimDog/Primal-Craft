package net.kimdog_studios.primal_craft.sound;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent CHISEL_USE = registerSoundEvent("chisel_use");

    public static final SoundEvent MAGIC_BLOCK_BREAK = registerSoundEvent("magic_block_break");
    public static final SoundEvent MAGIC_BLOCK_STEP = registerSoundEvent("magic_block_step");
    public static final SoundEvent MAGIC_BLOCK_PLACE = registerSoundEvent("magic_block_place");
    public static final SoundEvent MAGIC_BLOCK_HIT = registerSoundEvent("magic_block_hit");
    public static final SoundEvent MAGIC_BLOCK_FALL = registerSoundEvent("magic_block_fall");

    public static final BlockSoundGroup MAGIC_BLOCK_SOUNDS = new BlockSoundGroup(1f, 1f,
            MAGIC_BLOCK_BREAK, MAGIC_BLOCK_STEP, MAGIC_BLOCK_PLACE, MAGIC_BLOCK_HIT, MAGIC_BLOCK_FALL);

    public static final SoundEvent BAR_BRAWL = registerSoundEvent("bar_brawl");
    public static final RegistryKey<JukeboxSong> BAR_BRAWL_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(PrimalCraft.MOD_ID, "bar_brawl"));

    // Chat & Typing sounds
    public static final SoundEvent TYPING_START = registerSoundEvent("typing_start");
    public static final SoundEvent CHAT_SEND = registerSoundEvent("chat_send");
    public static final SoundEvent CHAT_RECEIVE = registerSoundEvent("chat_receive");
    public static final SoundEvent NOTIFICATION_PING = registerSoundEvent("notification_ping");
    public static final SoundEvent ENERGY_LOW = registerSoundEvent("energy_low");

    public static final SoundEvent CLEAR_WEATHER = registerSoundEvent("clear_weather");
    public static final SoundEvent RAIN_WEATHER = registerSoundEvent("rain_weather");
    public static final SoundEvent THUNDER_WEATHER = registerSoundEvent("thunder_weather");
    public static final SoundEvent BIOME_SOUND = registerSoundEvent("biome_sound");


    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(PrimalCraft.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        PrimalCraft.LOGGER.info("Registering Mod Sounds for " + PrimalCraft.MOD_ID);
    }
}
