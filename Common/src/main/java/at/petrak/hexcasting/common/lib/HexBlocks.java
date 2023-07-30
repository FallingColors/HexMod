package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.BlockConjured;
import at.petrak.hexcasting.common.blocks.BlockConjuredLight;
import at.petrak.hexcasting.common.blocks.BlockFlammable;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicLigature;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord;
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockBooleanDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockEmptyDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRedstoneImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRightClickImpetus;
import at.petrak.hexcasting.common.blocks.decoration.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexBlocks {
    public static void registerBlocks(BiConsumer<Block, ResourceLocation> r) {
        for (var e : BLOCKS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    public static void registerBlockItems(BiConsumer<Item, ResourceLocation> r) {
        for (var e : BLOCK_ITEMS.entrySet()) {
            r.accept(new BlockItem(e.getValue().getFirst(), e.getValue().getSecond()), e.getKey());
        }
    }

    public static void registerBlockCreativeTab(Consumer<Block> r, CreativeModeTab tab) {
        for (var block : BLOCK_TABS.getOrDefault(tab, List.of())) {
            r.accept(block);
        }
    }

    private static final Map<ResourceLocation, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Pair<Block, Item.Properties>> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<CreativeModeTab, List<Block>> BLOCK_TABS = new LinkedHashMap<>();


    private static BlockBehaviour.Properties slateish() {
        return BlockBehaviour.Properties
            .copy(Blocks.DEEPSLATE_TILES)
            .strength(4f, 4f);
    }

    private static BlockBehaviour.Properties papery(MapColor color) {
        return BlockBehaviour.Properties
            .of()
            .mapColor(color)
            .sound(SoundType.GRASS)
            .instabreak()
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties akashicWoodyHard() {
        return woodyHard(MapColor.COLOR_PURPLE);
    }

    private static BlockBehaviour.Properties woodyHard(MapColor color) {
        return BlockBehaviour.Properties
            .copy(Blocks.OAK_LOG)
            .mapColor(color)
            .sound(SoundType.WOOD)
            .strength(3f, 4f);
    }

    private static BlockBehaviour.Properties edifiedWoody() {
        return woody(MapColor.COLOR_PURPLE);
    }

    private static BlockBehaviour.Properties woody(MapColor color) {
        return BlockBehaviour.Properties
            .copy(Blocks.OAK_LOG)
            .mapColor(color)
            .sound(SoundType.WOOD)
            .strength(2f);
    }

    private static BlockBehaviour.Properties leaves(MapColor color) {
        return BlockBehaviour.Properties
            .copy(Blocks.OAK_LEAVES)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((bs, level, pos, type) -> type == EntityType.OCELOT || type == EntityType.PARROT)
            .isSuffocating(HexBlocks::never)
            .isViewBlocking(HexBlocks::never);
    }

    // we have to make it emit light because otherwise it occludes itself and is always dark
    private static BlockBehaviour.Properties quenched() {
        return BlockBehaviour.Properties
            .copy(Blocks.AMETHYST_BLOCK)
            .lightLevel($ -> 4)
            .noOcclusion();
    }

    // we give these faux items so Patchi can have an item to view with
    public static final Block CONJURED_LIGHT = blockItem("conjured_light",
        new BlockConjuredLight(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 15)
                .noLootTable()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .pushReaction(PushReaction.DESTROY)
                .noCollission()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)),
        new Item.Properties());
    public static final Block CONJURED_BLOCK = blockItem("conjured_block",
        new BlockConjured(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 2)
                .noLootTable()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .noOcclusion()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)),
        new Item.Properties());

    // "no" item because we add it manually
    public static final BlockSlate SLATE = blockNoItem("slate",
        new BlockSlate(slateish()
            .pushReaction(PushReaction.DESTROY)));

    public static final BlockEmptyImpetus IMPETUS_EMPTY = blockItem("impetus/empty",
        new BlockEmptyImpetus(slateish()
            .pushReaction(PushReaction.BLOCK)));
    public static final BlockRightClickImpetus IMPETUS_RIGHTCLICK = blockItem("impetus/rightclick",
        new BlockRightClickImpetus(slateish()
            .pushReaction(PushReaction.BLOCK)
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final BlockLookingImpetus IMPETUS_LOOK = blockItem("impetus/look",
        new BlockLookingImpetus(slateish()
            .pushReaction(PushReaction.BLOCK)
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final BlockRedstoneImpetus IMPETUS_REDSTONE = blockItem("impetus/redstone",
        new BlockRedstoneImpetus(slateish()
            .pushReaction(PushReaction.BLOCK)
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));


    public static final BlockEmptyDirectrix EMPTY_DIRECTRIX = blockItem("directrix/empty",
        new BlockEmptyDirectrix(slateish()
            .pushReaction(PushReaction.BLOCK)));
    public static final BlockRedstoneDirectrix DIRECTRIX_REDSTONE = blockItem("directrix/redstone",
        new BlockRedstoneDirectrix(slateish()
            .pushReaction(PushReaction.BLOCK)));
    public static final BlockBooleanDirectrix DIRECTRIX_BOOLEAN = blockItem("directrix/boolean",
        new BlockBooleanDirectrix(slateish()
            .pushReaction(PushReaction.BLOCK)));

    public static final BlockAkashicRecord AKASHIC_RECORD = blockItem("akashic_record",
        new BlockAkashicRecord(akashicWoodyHard().lightLevel(bs -> 15)));
    public static final BlockAkashicBookshelf AKASHIC_BOOKSHELF = blockItem("akashic_bookshelf",
        new BlockAkashicBookshelf(akashicWoodyHard()
            .lightLevel(bs -> (bs.getValue(BlockAkashicBookshelf.HAS_BOOKS)) ? 4 : 0)));
    public static final BlockAkashicLigature AKASHIC_LIGATURE = blockItem("akashic_connector",
        new BlockAkashicLigature(akashicWoodyHard().lightLevel(bs -> 4)));

    public static final BlockQuenchedAllay QUENCHED_ALLAY = blockItem("quenched_allay", new BlockQuenchedAllay(quenched()));

    // Decoration?!
    public static final BlockQuenchedAllay QUENCHED_ALLAY_TILES = blockItem("quenched_allay_tiles", new BlockQuenchedAllay(quenched()));
    public static final BlockQuenchedAllay QUENCHED_ALLAY_BRICKS = blockItem("quenched_allay_bricks", new BlockQuenchedAllay(quenched()));
    public static final BlockQuenchedAllay QUENCHED_ALLAY_BRICKS_SMALL = blockItem("quenched_allay_bricks_small", new BlockQuenchedAllay(quenched()));
    public static final Block SLATE_BLOCK = blockItem("slate_block", new Block(slateish().strength(2f, 4f)));
    public static final Block SLATE_TILES = blockItem("slate_tiles", new Block(slateish().strength(2f, 4f)));
    public static final Block SLATE_BRICKS = blockItem("slate_bricks", new Block(slateish().strength(2f, 4f)));
    public static final Block SLATE_BRICKS_SMALL = blockItem("slate_bricks_small", new Block(slateish().strength(2f, 4f)));
    public static final RotatedPillarBlock SLATE_PILLAR = blockItem("slate_pillar", new RotatedPillarBlock(slateish().strength(2f, 4f)));
    public static final SandBlock AMETHYST_DUST_BLOCK = blockItem("amethyst_dust_block",
        new SandBlock(0xff_b38ef3, BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_PURPLE)
            .strength(0.5f).sound(SoundType.SAND)));
    public static final AmethystBlock AMETHYST_TILES = blockItem("amethyst_tiles",
        new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final AmethystBlock AMETHYST_BRICKS = blockItem("amethyst_bricks",
            new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final AmethystBlock AMETHYST_BRICKS_SMALL = blockItem("amethyst_bricks_small",
            new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final BlockAmethystDirectional AMETHYST_PILLAR = blockItem("amethyst_pillar",
            new BlockAmethystDirectional(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final Block SLATE_AMETHYST_TILES = blockItem("slate_amethyst_tiles", new Block(slateish().strength(2f, 4f)));
    public static final Block SLATE_AMETHYST_BRICKS = blockItem("slate_amethyst_bricks", new Block(slateish().strength(2f, 4f)));
    public static final Block SLATE_AMETHYST_BRICKS_SMALL = blockItem("slate_amethyst_bricks_small", new Block(slateish().strength(2f, 4f)));
    public static final RotatedPillarBlock SLATE_AMETHYST_PILLAR = blockItem("slate_amethyst_pillar",
            new RotatedPillarBlock(slateish().strength(2f, 4f)));
    public static final Block SCROLL_PAPER = blockItem("scroll_paper",
        new BlockFlammable(papery(MapColor.TERRACOTTA_WHITE), 100, 60));
    public static final Block ANCIENT_SCROLL_PAPER = blockItem("ancient_scroll_paper",
        new BlockFlammable(papery(MapColor.TERRACOTTA_ORANGE), 100, 60));
    public static final Block SCROLL_PAPER_LANTERN = blockItem("scroll_paper_lantern",
        new BlockFlammable(papery(MapColor.TERRACOTTA_WHITE).lightLevel($ -> 15), 100, 60));
    public static final Block ANCIENT_SCROLL_PAPER_LANTERN = blockItem(
        "ancient_scroll_paper_lantern",
        new BlockFlammable(papery(MapColor.TERRACOTTA_ORANGE).lightLevel($ -> 12), 100, 60));
    public static final BlockSconce SCONCE = blockItem("amethyst_sconce",
        new BlockSconce(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .sound(SoundType.AMETHYST)
            .strength(1f)
            .lightLevel($ -> 15)),
        HexItems.props().rarity(Rarity.RARE));

    public static final BlockAkashicLog EDIFIED_LOG = blockItem("edified_log",
        new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog EDIFIED_LOG_AMETHYST = blockItem("edified_log_amethyst",
            new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog EDIFIED_LOG_AVENTURINE = blockItem("edified_log_aventurine",
            new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog EDIFIED_LOG_CITRINE = blockItem("edified_log_citrine",
            new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog EDIFIED_LOG_PURPLE = blockItem("edified_log_purple",
            new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog STRIPPED_EDIFIED_LOG = blockItem("stripped_edified_log",
        new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog EDIFIED_WOOD = blockItem("edified_wood",
        new BlockAkashicLog(edifiedWoody()));
    public static final BlockAkashicLog STRIPPED_EDIFIED_WOOD = blockItem("stripped_edified_wood",
        new BlockAkashicLog(edifiedWoody()));
    public static final Block EDIFIED_PLANKS = blockItem("edified_planks",
        new BlockFlammable(edifiedWoody(), 20, 5));
    public static final Block EDIFIED_PANEL = blockItem("edified_panel",
        new BlockFlammable(edifiedWoody(), 20, 5));
    public static final Block EDIFIED_TILE = blockItem("edified_tile",
        new BlockFlammable(edifiedWoody(), 20, 5));
    public static final DoorBlock EDIFIED_DOOR = blockItem("edified_door",
        new BlockHexDoor(edifiedWoody().noOcclusion()));
    public static final TrapDoorBlock EDIFIED_TRAPDOOR = blockItem("edified_trapdoor",
        new BlockHexTrapdoor(edifiedWoody().noOcclusion()));
    public static final StairBlock EDIFIED_STAIRS = blockItem("edified_stairs",
        new BlockHexStairs(EDIFIED_PLANKS.defaultBlockState(), edifiedWoody().noOcclusion()));
    public static final SlabBlock EDIFIED_SLAB = blockItem("edified_slab",
        new BlockHexSlab(edifiedWoody().noOcclusion()));
    public static final ButtonBlock EDIFIED_BUTTON = blockItem("edified_button",
        new BlockHexWoodButton(edifiedWoody().noOcclusion().noCollission()));
    public static final PressurePlateBlock EDIFIED_PRESSURE_PLATE = blockItem("edified_pressure_plate",
        new BlockHexPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING,
            edifiedWoody().noOcclusion().noCollission()));
    public static final BlockAkashicLeaves AMETHYST_EDIFIED_LEAVES = blockItem("amethyst_edified_leaves",
        new BlockAkashicLeaves(leaves(MapColor.COLOR_PURPLE)));
    public static final BlockAkashicLeaves AVENTURINE_EDIFIED_LEAVES = blockItem("aventurine_edified_leaves",
        new BlockAkashicLeaves(leaves(MapColor.COLOR_BLUE)));
    public static final BlockAkashicLeaves CITRINE_EDIFIED_LEAVES = blockItem("citrine_edified_leaves",
        new BlockAkashicLeaves(leaves(MapColor.COLOR_YELLOW)));

    private static boolean never(Object... args) {
        return false;
    }

    private static <T extends Block> T blockNoItem(String name, T block) {
        var old = BLOCKS.put(modLoc(name), block);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return block;
    }
    private static <T extends Block> T blockItem(String name, T block) {
        return blockItem(name, block, HexItems.props(), HexCreativeTabs.HEX);
    }

    private static <T extends Block> T blockItem(String name, T block, @Nullable CreativeModeTab tab) {
        return blockItem(name, block, HexItems.props(), tab);
    }
    private static <T extends Block> T blockItem(String name, T block, Item.Properties props) {
        return blockItem(name, block, props, HexCreativeTabs.HEX);
    }

    private static <T extends Block> T blockItem(String name, T block, Item.Properties props, @Nullable CreativeModeTab tab) {
        blockNoItem(name, block);
        var old = BLOCK_ITEMS.put(modLoc(name), new Pair<>(block, props));
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        if (tab != null) {
            BLOCK_TABS.computeIfAbsent(tab, t -> new ArrayList<>()).add(block);
        }
        return block;
    }
}


