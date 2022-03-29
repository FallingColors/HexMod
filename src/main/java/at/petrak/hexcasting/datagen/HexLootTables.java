package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.paucal.api.datagen.PaucalLootTableProvider;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Map;

// https://github.com/XuulMedia/Flint-Age/blob/4638289130ef80dafe9b6a3fdcb461a72688100f/src/main/java/xuul/flint/datagen/BaseLootTableProvider.java#L61
// auugh mojang whyyyy
public class HexLootTables extends PaucalLootTableProvider {
    public HexLootTables(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> lootTables) {
        dropSelfTable(lootTables, HexBlocks.EMPTY_IMPETUS,
            HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_STOREDPLAYER,
            HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.EMPTY_DIRECTRIX,
            HexBlocks.SLATE_BLOCK, HexBlocks.AMETHYST_DUST_BLOCK, HexBlocks.AMETHYST_TILES, HexBlocks.SCROLL_PAPER,
            HexBlocks.ANCIENT_SCROLL_PAPER, HexBlocks.SCROLL_PAPER_LANTERN, HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,
            HexBlocks.SCONCE);

        var slatePool = LootPool.lootPool().name("slate").
            setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE.get())
                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                    .copy(BlockEntitySlate.TAG_PATTERN, "BlockEntityTag." + BlockEntitySlate.TAG_PATTERN)));
        lootTables.put(HexBlocks.SLATE.get(), LootTable.lootTable().withPool(slatePool));

        // Just override the vanilla table
        var amethystPool = LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .add(AlternativesEntry.alternatives(
                LootItem.lootTableItem(Items.AMETHYST_CLUSTER)
                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(
                        new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))),

                EntryGroup.list(
                    LootItem.lootTableItem(HexItems.AMETHYST_DUST.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)),
                    LootItem.lootTableItem(Items.AMETHYST_SHARD)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)),
                    LootItem.lootTableItem(HexItems.CHARGED_AMETHYST.get())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                            0.25f, 0.35f, 0.5f, 0.75f, 1.0f))
                ).when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES))),

                LootItem.lootTableItem(Items.AMETHYST_SHARD)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
            ));
        lootTables.put(Blocks.AMETHYST_CLUSTER, LootTable.lootTable().withPool(amethystPool));
    }
}
