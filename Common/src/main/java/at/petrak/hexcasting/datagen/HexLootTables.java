package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.datagen.PaucalLootTableSubProvider;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.Enchantment;
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
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Map;

public class HexLootTables extends PaucalLootTableSubProvider {
    private final HolderLookup.Provider registries;

    public HexLootTables(HolderLookup.Provider registries) {
        super(HexAPI.MOD_ID);
        this.registries = registries;
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> blockTables,
        Map<ResourceKey<LootTable>, LootTable.Builder> lootTables) {
        dropSelf(blockTables, HexBlocks.IMPETUS_EMPTY.get(),
            HexBlocks.IMPETUS_RIGHTCLICK.get(), HexBlocks.IMPETUS_LOOK.get(), HexBlocks.IMPETUS_REDSTONE.get(),
            HexBlocks.EMPTY_DIRECTRIX.get(), HexBlocks.DIRECTRIX_REDSTONE.get(), HexBlocks.DIRECTRIX_BOOLEAN.get(),
            HexBlocks.AKASHIC_RECORD.get(), HexBlocks.AKASHIC_BOOKSHELF.get(), HexBlocks.AKASHIC_LIGATURE.get(),
            HexBlocks.SLATE_BLOCK.get(), HexBlocks.SLATE_TILES.get(), HexBlocks.SLATE_BRICKS.get(), HexBlocks.SLATE_BRICKS_SMALL.get(),
            HexBlocks.SLATE_PILLAR.get(), HexBlocks.AMETHYST_DUST_BLOCK.get(), HexBlocks.AMETHYST_TILES.get(), HexBlocks.AMETHYST_BRICKS.get(),
            HexBlocks.AMETHYST_BRICKS_SMALL.get(), HexBlocks.AMETHYST_PILLAR.get(), HexBlocks.SLATE_AMETHYST_TILES.get(),
            HexBlocks.SLATE_AMETHYST_BRICKS.get(), HexBlocks.SLATE_AMETHYST_BRICKS_SMALL.get(), HexBlocks.SLATE_AMETHYST_PILLAR.get(),
            HexBlocks.QUENCHED_ALLAY_TILES.get(), HexBlocks.QUENCHED_ALLAY_BRICKS.get(), HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get(),
            HexBlocks.SCROLL_PAPER.get(), HexBlocks.ANCIENT_SCROLL_PAPER.get(), HexBlocks.SCROLL_PAPER_LANTERN.get(),
            HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), HexBlocks.SCONCE.get(),
            HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(), HexBlocks.EDIFIED_LOG_AVENTURINE.get(),
            HexBlocks.EDIFIED_LOG_CITRINE.get(), HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get(),
            HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get(),
            HexBlocks.EDIFIED_PLANKS.get(), HexBlocks.EDIFIED_TILE.get(), HexBlocks.EDIFIED_PANEL.get(),
            HexBlocks.EDIFIED_TRAPDOOR.get(), HexBlocks.EDIFIED_STAIRS.get(), HexBlocks.EDIFIED_FENCE.get(), HexBlocks.EDIFIED_FENCE_GATE.get(), HexBlocks.EDIFIED_PRESSURE_PLATE.get(),
            HexBlocks.EDIFIED_BUTTON.get());

        HolderLookup.RegistryLookup<Enchantment> enchRegistryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        makeSlabTable(blockTables, HexBlocks.EDIFIED_SLAB.get());

        makeLeafTable(blockTables, HexBlocks.AMETHYST_EDIFIED_LEAVES.get(), enchRegistryLookup);
        makeLeafTable(blockTables, HexBlocks.AVENTURINE_EDIFIED_LEAVES.get(), enchRegistryLookup);
        makeLeafTable(blockTables, HexBlocks.CITRINE_EDIFIED_LEAVES.get(), enchRegistryLookup);

        var slatePool = LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE.get())
                .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                        .include(HexDataComponents.PATTERN.get())
                ));
        blockTables.put(HexBlocks.SLATE.get(), LootTable.lootTable().withPool(slatePool));

        var doorPool = dropThisPool(HexBlocks.EDIFIED_DOOR.get(), 1)
            .when(new LootItemBlockStatePropertyCondition.Builder(HexBlocks.EDIFIED_DOOR.get()).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            ));
        blockTables.put(HexBlocks.EDIFIED_DOOR.get(), LootTable.lootTable().withPool(doorPool));

        var silkTouchCond = MatchTool.toolMatches(
            ItemPredicate.Builder.item().withSubPredicate(
                    ItemSubPredicates.ENCHANTMENTS,
                    ItemEnchantmentsPredicate.enchantments(
                            List.of(new EnchantmentPredicate(enchRegistryLookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                    )
            ));
        var noSilkTouchCond = silkTouchCond.invert();
        var goodAtAmethystingCond = MatchTool.toolMatches(
            ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)
        );

        var dustPoolWhenGood = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.AMETHYST_DUST.get()))
            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
            .apply(ApplyBonusCount.addOreBonusCount(enchRegistryLookup.getOrThrow(Enchantments.FORTUNE)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond);

        var dustPoolWhenBad = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.AMETHYST_DUST.get()))
            .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond.invert());

        var isThatAnMFingBrandonSandersonReference = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.CHARGED_AMETHYST.get()))
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond)
            .when(BonusLevelTableCondition.bonusLevelFlatChance(enchRegistryLookup.getOrThrow(Enchantments.FORTUNE),
                0.25f, 0.35f, 0.5f, 0.75f, 1.0f));

        var isThatAnMFingBadBrandonSandersonReference = LootPool.lootPool()
            .add(LootItem.lootTableItem(HexItems.CHARGED_AMETHYST.get()))
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
            .when(noSilkTouchCond).when(goodAtAmethystingCond.invert())
            .when(LootItemRandomChanceCondition.randomChance(0.125f));

        HexAPI.LOGGER.info("Doing amethyst cluster injection shit");

        lootTables.put(HexLootHandler.TABLE_INJECT_AMETHYST_CLUSTER, LootTable.lootTable()
            .withPool(dustPoolWhenGood)
            .withPool(dustPoolWhenBad)
            .withPool(isThatAnMFingBrandonSandersonReference)
            .withPool(isThatAnMFingBadBrandonSandersonReference));

        HexAPI.LOGGER.info("Quenched bugged...?");

        // it looks like loot groups are bugged?
        // so instead we add some and then *increment* the amount, gated behind the cond
        var quenchedPool = LootPool.lootPool().add(AlternativesEntry.alternatives(
            LootItem.lootTableItem(HexBlocks.QUENCHED_ALLAY.get()).when(silkTouchCond),
            LootItem.lootTableItem(HexItems.QUENCHED_SHARD.get())
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2f, 4f)))
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1), true)
                    .when(BonusLevelTableCondition.bonusLevelFlatChance(enchRegistryLookup.getOrThrow(Enchantments.FORTUNE),
                        0.25f, 0.5f, 0.75f, 1.0f)))
        ));
        blockTables.put(HexBlocks.QUENCHED_ALLAY.get(), LootTable.lootTable().withPool(quenchedPool));
    }

    private void makeLeafTable(Map<Block, LootTable.Builder> lootTables, Block block, HolderLookup.RegistryLookup<Enchantment> enchRegistryLookup) {
        var leafPool = dropThisPool(block, 1)
            .when(AnyOfCondition.anyOf(
                IXplatAbstractions.INSTANCE.isShearsCondition(),
                MatchTool.toolMatches(ItemPredicate.Builder.item()
                        .withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS,
                                ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(enchRegistryLookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )
            )));
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
