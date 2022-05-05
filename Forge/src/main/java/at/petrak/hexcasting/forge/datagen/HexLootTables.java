package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.paucal.api.datagen.PaucalLootTableProvider;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.CanToolPerformAction;

import java.util.Map;

// https://github.com/XuulMedia/Flint-Age/blob/4638289130ef80dafe9b6a3fdcb461a72688100f/src/main/java/xuul/flint/datagen/BaseLootTableProvider.java#L61
// auugh mojang whyyyy
public class HexLootTables extends PaucalLootTableProvider {
    public HexLootTables(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> lootTables) {
        dropSelf(lootTables, HexBlocks.EMPTY_IMPETUS,
            HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_STOREDPLAYER,
            HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.EMPTY_DIRECTRIX,
            HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_CONNECTOR,
            HexBlocks.SLATE_BLOCK, HexBlocks.AMETHYST_DUST_BLOCK, HexBlocks.AMETHYST_TILES, HexBlocks.SCROLL_PAPER,
            HexBlocks.ANCIENT_SCROLL_PAPER, HexBlocks.SCROLL_PAPER_LANTERN, HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,
            HexBlocks.SCONCE,
            HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED, HexBlocks.AKASHIC_WOOD,
            HexBlocks.AKASHIC_WOOD_STRIPPED,
            HexBlocks.AKASHIC_PLANKS, HexBlocks.AKASHIC_TILE, HexBlocks.AKASHIC_PANEL,
            HexBlocks.AKASHIC_TRAPDOOR);

        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES1.get());
        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES2.get());
        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES3.get());

        var slatePool = LootPool.lootPool().name("slate").
            setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE.get())
                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                    .copy(BlockEntitySlate.TAG_PATTERN, "BlockEntityTag." + BlockEntitySlate.TAG_PATTERN)));
        lootTables.put(HexBlocks.SLATE.get(), LootTable.lootTable().withPool(slatePool));

        var doorPool = dropThisPool(HexBlocks.AKASHIC_DOOR.get(), 1)
            .when(new LootItemBlockStatePropertyCondition.Builder(HexBlocks.AKASHIC_DOOR.get()).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            ));
        lootTables.put(HexBlocks.AKASHIC_DOOR.get(), LootTable.lootTable().withPool(doorPool));
    }

    private void makeLeafTable(Map<Block, LootTable.Builder> lootTables, Block block) {
        var leafPool = dropThisPool(block, 1)
            .when(new AlternativeLootItemCondition.Builder(
                CanToolPerformAction.canToolPerformAction(ToolActions.SHEARS_DIG),
                MatchTool.toolMatches(ItemPredicate.Builder.item()
                    .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))
            ));
        lootTables.put(block, LootTable.lootTable().withPool(leafPool));
    }
}
