package at.petrak.hexcasting.datagen.lootmods;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.paucal.api.lootmod.PaucalAddItemModifier;
import at.petrak.paucal.api.lootmod.PaucalLootMods;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
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
        ResourceLocation amethystCluster = new ResourceLocation("minecraft:blocks/amethyst_cluster");
        // 4? that's a lot!
        add("amethyst_cluster_shard_reducer", PaucalLootMods.ADD_ITEM.get(), new PaucalAddItemModifier(
            Items.AMETHYST_SHARD, -2, amethystCluster
        ));

        add("amethyst_cluster_dust", PaucalLootMods.ADD_ITEM.get(), new PaucalAddItemModifier(
            HexItems.AMETHYST_DUST.get(), new LootItemFunction[]{
            SetItemCountFunction.setCount(ConstantValue.exactly(4)).build(),
            ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE).build()
        }, new LootItemCondition[]{
            LootTableIdCondition.builder(amethystCluster).build(),
            MatchTool.toolMatches(
                    ItemPredicate.Builder.item().hasEnchantment(
                        new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.ANY)))
                .invert().build(),
        }
        ));
        add("amethyst_cluster_charged", PaucalLootMods.ADD_ITEM.get(), new PaucalAddItemModifier(
            HexItems.CHARGED_AMETHYST.get(), 1, new LootItemCondition[]{
            LootTableIdCondition.builder(amethystCluster).build(),
            MatchTool.toolMatches(
                    ItemPredicate.Builder.item().hasEnchantment(
                        new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.ANY)))
                .invert().build(),
            BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.5f, 0.8f, 0.9f, 1.0f).build()
        }));


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