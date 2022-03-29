package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.paucal.api.datagen.PaucalLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
    }
}
