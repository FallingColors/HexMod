package at.petrak.hexcasting.forge.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.forge.loot.ForgeHexScrollLootMod;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeHexLootMods {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(
        ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, HexAPI.MOD_ID);

    public static final RegistryObject<Codec<ForgeHexScrollLootMod>> INJECT_SCROLLS = REGISTRY.register(
        "inject_scrolls", ForgeHexScrollLootMod.CODEC);
}
