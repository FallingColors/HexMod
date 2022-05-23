package at.petrak.hexcasting.xplat.datagen;

import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HexLootTables extends LootTableProvider {
    // steal it ALL from paucal
    protected final DataGenerator generator;

    public HexLootTables(DataGenerator pGenerator) {
        super(pGenerator);
        this.generator = pGenerator;
    }

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
            HexBlocks.AKASHIC_TRAPDOOR, HexBlocks.AKASHIC_STAIRS, HexBlocks.AKASHIC_PRESSURE_PLATE,
            HexBlocks.AKASHIC_BUTTON);

        makeSlabTable(lootTables, HexBlocks.AKASHIC_SLAB);

        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES1);
        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES2);
        makeLeafTable(lootTables, HexBlocks.AKASHIC_LEAVES3);

        var slatePool = LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE)
                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                    .copy(BlockEntitySlate.TAG_PATTERN, "BlockEntityTag." + BlockEntitySlate.TAG_PATTERN)));
        lootTables.put(HexBlocks.SLATE, LootTable.lootTable().withPool(slatePool));

        var doorPool = dropThisPool(HexBlocks.AKASHIC_DOOR, 1)
            .when(new LootItemBlockStatePropertyCondition.Builder(HexBlocks.AKASHIC_DOOR).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            ));
        lootTables.put(HexBlocks.AKASHIC_DOOR, LootTable.lootTable().withPool(doorPool));
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


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    protected LootPool.Builder dropThisPool(ItemLike item, int count) {
        return dropThisPool(item, ConstantValue.exactly(count));
    }

    protected LootPool.Builder dropThisPool(ItemLike item, NumberProvider count) {
        return LootPool.lootPool()
            .setRolls(count)
            .add(LootItem.lootTableItem(item));
    }

    protected void dropSelf(Map<Block, LootTable.Builder> lootTables, Block... blocks) {
        for (var block : blocks) {
            dropSelf(block, lootTables);
        }
    }

    protected void dropSelf(Block block, Map<Block, LootTable.Builder> lootTables) {
        var table = LootTable.lootTable().withPool(dropThisPool(block, 1));
        lootTables.put(block, table);
    }

    protected void dropThis(Block block, ItemLike drop, Map<Block, LootTable.Builder> lootTables) {
        var table = LootTable.lootTable().withPool(dropThisPool(drop, 1));
        lootTables.put(block, table);
    }

    protected void dropThis(Block block, ItemLike drop, NumberProvider count,
        Map<Block, LootTable.Builder> lootTables) {
        var table = LootTable.lootTable().withPool(dropThisPool(drop, count));
        lootTables.put(block, table);
    }

    @Override
    public void run(HashCache cache) {
        var lootTables = new HashMap<Block, LootTable.Builder>();
        this.makeLootTables(lootTables);

        var tables = new HashMap<ResourceLocation, LootTable>();
        for (var entry : lootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParamSet(LootContextParamSets.BLOCK).build());
        }

        var outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
