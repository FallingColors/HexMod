package at.petrak.hexcasting.datagen.lootmods;

import at.petrak.hexcasting.HexMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexLootModifiers extends GlobalLootModifierProvider {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODS = DeferredRegister.create(
        ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, HexMod.MOD_ID);
    private static final RegistryObject<PatternScrollModifier.Serializer> SCROLLS_IN_CHESTS = LOOT_MODS.register(
        "scrolls", PatternScrollModifier.Serializer::new);

    public HexLootModifiers(DataGenerator gen) {
        super(gen, HexMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("scroll_jungle", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(new ResourceLocation("minecraft:chests/jungle_temple")).build()
        }, 1.0));
        add("scroll_bastion", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(new ResourceLocation("minecraft:chests/bastion_treasure")).build()
        }, 2.0));
        add("scroll_dungeon", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(new ResourceLocation("minecraft:chests/simple_dungeon")).build()
        }, 1.0));
        add("scroll_stronghold_library", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(new ResourceLocation("minecraft:chests/stronghold_library")).build()
        }, 3.0));
        // Why is there not a village library chest
        add("scroll_cartographer", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(
                new ResourceLocation("minecraft:chests/village/village_cartographer")).build()
        }, 1.0));
        add("scroll_shipwreck", SCROLLS_IN_CHESTS.get(), new PatternScrollModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(new ResourceLocation("minecraft:chests/shipwreck_map")).build()
        }, 1.0));
    }
}