package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.common.loot.PatternScrollFunc;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.datagen.PaucalLootTableProvider;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
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

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexLootTables extends PaucalLootTableProvider {
    public HexLootTables(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> blockTables,
        Map<ResourceLocation, LootTable.Builder> lootTables) {
        dropSelf(blockTables, HexBlocks.EMPTY_IMPETUS,
            HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_STOREDPLAYER,
            HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.EMPTY_DIRECTRIX,
            HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_LIGATURE,
            HexBlocks.SLATE_BLOCK, HexBlocks.AMETHYST_DUST_BLOCK, HexBlocks.AMETHYST_TILES, HexBlocks.SCROLL_PAPER,
            HexBlocks.ANCIENT_SCROLL_PAPER, HexBlocks.SCROLL_PAPER_LANTERN, HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,
            HexBlocks.SCONCE,
            HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG, HexBlocks.EDIFIED_WOOD,
            HexBlocks.STRIPPED_EDIFIED_WOOD,
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


        var noSilkTouchCond = MatchTool.toolMatches(
                ItemPredicate.Builder.item().hasEnchantment(
                    new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.ANY)))
            .invert();
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

        String[] rarities = new String[]{
            "few",
            "some",
            "many"
        };
        for (int i = 0; i < rarities.length; i++) {
            var scrollPool = makeScrollAdder(i + 1);
            lootTables.put(modLoc("inject/scroll_loot_" + rarities[i]), scrollPool);
        }
    }

    private void makeLeafTable(Map<Block, LootTable.Builder> lootTables, Block block) {
        var leafPool = dropThisPool(block, 1)
            .when(new AlternativeLootItemCondition.Builder(
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

    // "stddev"
    private LootTable.Builder makeScrollAdder(float stddev) {
        var pool = LootPool.lootPool()
            .setRolls(UniformGenerator.between(-stddev, stddev))
            .add(LootItem.lootTableItem(HexItems.SCROLL_LARGE))
            .apply(() -> new PatternScrollFunc(new LootItemCondition[0]));
        return LootTable.lootTable().withPool(pool);
    }
}
