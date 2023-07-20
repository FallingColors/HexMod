package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.datagen.PaucalLootTableSubProvider;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Map;

public class HexLootTables extends PaucalLootTableSubProvider {
    public HexLootTables() {
        super(HexAPI.MOD_ID);
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> blockTables,
        Map<ResourceLocation, LootTable.Builder> lootTables) {
        dropSelf(blockTables, HexBlocks.IMPETUS_EMPTY,
            HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_REDSTONE,
            HexBlocks.EMPTY_DIRECTRIX, HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.DIRECTRIX_BOOLEAN,
            HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_LIGATURE,
            HexBlocks.SLATE_BLOCK, HexBlocks.SLATE_TILES, HexBlocks.SLATE_BRICKS, HexBlocks.SLATE_BRICKS_SMALL,
            HexBlocks.SLATE_PILLAR, HexBlocks.AMETHYST_DUST_BLOCK, HexBlocks.AMETHYST_TILES, HexBlocks.AMETHYST_BRICKS,
            HexBlocks.AMETHYST_BRICKS_SMALL, HexBlocks.AMETHYST_PILLAR, HexBlocks.SLATE_AMETHYST_TILES,
            HexBlocks.SLATE_AMETHYST_BRICKS, HexBlocks.SLATE_AMETHYST_BRICKS_SMALL, HexBlocks.SLATE_AMETHYST_PILLAR,
            HexBlocks.QUENCHED_ALLAY_TILES, HexBlocks.QUENCHED_ALLAY_BRICKS, HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL,
            HexBlocks.SCROLL_PAPER, HexBlocks.ANCIENT_SCROLL_PAPER, HexBlocks.SCROLL_PAPER_LANTERN,
            HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, HexBlocks.SCONCE,
            HexBlocks.EDIFIED_LOG, HexBlocks.EDIFIED_LOG_AMETHYST, HexBlocks.EDIFIED_LOG_AVENTURINE,
            HexBlocks.EDIFIED_LOG_CITRINE, HexBlocks.EDIFIED_LOG_PURPLE, HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD,
            HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_TILE, HexBlocks.EDIFIED_PANEL,
            HexBlocks.EDIFIED_TRAPDOOR, HexBlocks.EDIFIED_STAIRS, HexBlocks.EDIFIED_PRESSURE_PLATE,
            HexBlocks.EDIFIED_BUTTON);

        makeSlabTable(blockTables, HexBlocks.EDIFIED_SLAB);

        makeLeafTable(blockTables, HexBlocks.AMETHYST_EDIFIED_LEAVES);
        makeLeafTable(blockTables, HexBlocks.AVENTURINE_EDIFIED_LEAVES);
        makeLeafTable(blockTables, HexBlocks.CITRINE_EDIFIED_LEAVES);

        var slatePool = LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE)
                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                    .copy(BlockEntitySlate.TAG_PATTERN, "BlockEntityTag." + BlockEntitySlate.TAG_PATTERN)));
        blockTables.put(HexBlocks.SLATE, LootTable.lootTable().withPool(slatePool));

        var doorPool = dropThisPool(HexBlocks.EDIFIED_DOOR, 1)
            .when(new LootItemBlockStatePropertyCondition.Builder(HexBlocks.EDIFIED_DOOR).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            ));
        blockTables.put(HexBlocks.EDIFIED_DOOR, LootTable.lootTable().withPool(doorPool));


        var silkTouchCond = MatchTool.toolMatches(
            ItemPredicate.Builder.item().hasEnchantment(
                new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.ANY)));
        var noSilkTouchCond = silkTouchCond.invert();
        var goodAtAmethystingCond = MatchTool.toolMatches(
            ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)
        );

        var dustPoolWhenGood = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.AMETHYST_DUST))
            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
            .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            .when(noSilkTouchCond).when(goodAtAmethystingCond);

        var dustPoolWhenBad = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.AMETHYST_DUST))
            .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond.invert());

        var isThatAnMFingBrandonSandersonReference = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.CHARGED_AMETHYST))
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond)
            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                0.25f, 0.35f, 0.5f, 0.75f, 1.0f));

        var isThatAnMFingBadBrandonSandersonReference = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.CHARGED_AMETHYST))
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond.invert())
            .when(LootItemRandomChanceCondition.randomChance(0.125f));

        lootTables.put(HexLootHandler.TABLE_INJECT_AMETHYST_CLUSTER, LootTable.lootTable()
            .withPool(dustPoolWhenGood)
            .withPool(dustPoolWhenBad)
            .withPool(isThatAnMFingBrandonSandersonReference)
            .withPool(isThatAnMFingBadBrandonSandersonReference));

        // it looks like loot groups are bugged?
        // so instead we add some and then *increment* the amount, gated behind the cond
        var quenchedPool = LootPool.lootPool().add(AlternativesEntry.alternatives(
            LootItem.lootTableItem(HexBlocks.QUENCHED_ALLAY).when(silkTouchCond),
            LootItem.lootTableItem(HexItems.QUENCHED_SHARD)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2f, 4f)))
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1), true)
                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                        0.25f, 0.5f, 0.75f, 1.0f)))
        ));
        blockTables.put(HexBlocks.QUENCHED_ALLAY, LootTable.lootTable().withPool(quenchedPool));
    }

    private void makeLeafTable(Map<Block, LootTable.Builder> lootTables, Block block) {
        var leafPool = dropThisPool(block, 1)
            .when(AnyOfCondition.anyOf(
                IXplatAbstractions.INSTANCE.isShearsCondition(),
                MatchTool.toolMatches(ItemPredicate.Builder.item()
                    .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))
            ));
        lootTables.put(block, LootTable.lootTable().withPool(leafPool));
    }

    private void makeSlabTable(Map<Block, LootTable.Builder> lootTables, Block block) {
        var leafPool = dropThisPool(block, 1)
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))
                .when(new LootItemBlockStatePropertyCondition.Builder(block).setProperties(
                    StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)
                )))
            .apply(ApplyExplosionDecay.explosionDecay());
        lootTables.put(block, LootTable.lootTable().withPool(leafPool));
    }
}
