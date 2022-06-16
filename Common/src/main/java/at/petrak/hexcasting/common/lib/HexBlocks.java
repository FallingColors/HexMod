package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.BlockConjured;
import at.petrak.hexcasting.common.blocks.BlockConjuredLight;
import at.petrak.hexcasting.common.blocks.BlockFlammable;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicLigature;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord;
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockEmptyDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRightClickImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockStoredPlayerImpetus;
import at.petrak.hexcasting.common.blocks.decoration.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

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

    private static final Map<ResourceLocation, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Pair<Block, Item.Properties>> BLOCK_ITEMS = new LinkedHashMap<>();

    private static BlockBehaviour.Properties slateish() {
        return BlockBehaviour.Properties
            .of(Material.STONE, MaterialColor.DEEPSLATE)
            .sound(SoundType.DEEPSLATE_TILES)
            .strength(4f, 4f);
    }

    private static BlockBehaviour.Properties papery(MaterialColor color) {
        return BlockBehaviour.Properties
            .of(Material.PLANT, color)
            .sound(SoundType.GRASS)
            .instabreak();
    }

    private static BlockBehaviour.Properties akashicWoodyHard() {
        return woodyHard(MaterialColor.COLOR_PURPLE);
    }

    private static BlockBehaviour.Properties woodyHard(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.WOOD, color)
            .sound(SoundType.WOOD)
            .strength(3f, 4f);
    }

    private static BlockBehaviour.Properties edifiedWoody() {
        return woody(MaterialColor.COLOR_PURPLE);
    }

    private static BlockBehaviour.Properties woody(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.WOOD, color)
            .sound(SoundType.WOOD)
            .strength(2f);
    }

    private static BlockBehaviour.Properties leaves(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.LEAVES, color)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((bs, level, pos, type) -> type == EntityType.OCELOT || type == EntityType.PARROT)
            .isSuffocating(HexBlocks::never)
            .isViewBlocking(HexBlocks::never);
    }

    // we give these faux items so Patchi can have an item to view with
    public static final Block CONJURED_LIGHT = blockItem("conjured",
        new BlockConjuredLight(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 15)
                .noDrops()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .noCollission()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)),
        new Item.Properties());
    public static final Block CONJURED_BLOCK = blockItem("conjured_block",
        new BlockConjured(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 2)
                .noDrops()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .noOcclusion()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)),
        new Item.Properties());

    // "no" item because we add it manually
    public static final BlockSlate SLATE = blockNoItem("slate", new BlockSlate(slateish()));

    public static final BlockEmptyImpetus EMPTY_IMPETUS = blockItem("empty_impetus", new BlockEmptyImpetus(slateish()));
    public static final BlockRightClickImpetus IMPETUS_RIGHTCLICK = blockItem("impetus_rightclick",
        new BlockRightClickImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final BlockLookingImpetus IMPETUS_LOOK = blockItem("impetus_look",
        new BlockLookingImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final BlockStoredPlayerImpetus IMPETUS_STOREDPLAYER = blockItem("impetus_storedplayer",
        new BlockStoredPlayerImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));


    public static final BlockEmptyDirectrix EMPTY_DIRECTRIX = blockItem("empty_directrix",
        new BlockEmptyDirectrix(slateish()));
    public static final BlockRedstoneDirectrix DIRECTRIX_REDSTONE = blockItem("directrix_redstone",
        new BlockRedstoneDirectrix(slateish()));

    public static final BlockAkashicRecord AKASHIC_RECORD = blockItem("akashic_record",
        new BlockAkashicRecord(akashicWoodyHard().lightLevel(bs -> 15)));
    public static final BlockAkashicBookshelf AKASHIC_BOOKSHELF = blockItem("akashic_bookshelf",
        new BlockAkashicBookshelf(akashicWoodyHard()
            .lightLevel(bs -> (bs.getValue(BlockAkashicBookshelf.HAS_BOOKS)) ? 4 : 0)));
    public static final BlockAkashicLigature AKASHIC_LIGATURE = blockItem("akashic_connector",
        new BlockAkashicLigature(akashicWoodyHard().lightLevel(bs -> 4)));

    // Decoration?!
    public static final Block SLATE_BLOCK = blockItem("slate_block", new Block(slateish().strength(2f, 4f)));
    public static final SandBlock AMETHYST_DUST_BLOCK = blockItem("amethyst_dust_block",
        new SandBlock(0xff_b38ef3, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_PURPLE)
            .strength(0.5f).sound(SoundType.SAND)));
    public static final AmethystBlock AMETHYST_TILES = blockItem("amethyst_tiles",
        new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final Block SCROLL_PAPER = blockItem("scroll_paper",
        new BlockFlammable(papery(MaterialColor.TERRACOTTA_WHITE), 100, 60));
    public static final Block ANCIENT_SCROLL_PAPER = blockItem("ancient_scroll_paper",
        new BlockFlammable(papery(MaterialColor.TERRACOTTA_ORANGE), 100, 60));
    public static final Block SCROLL_PAPER_LANTERN = blockItem("scroll_paper_lantern",
        new BlockFlammable(papery(MaterialColor.TERRACOTTA_WHITE).lightLevel($ -> 15), 100, 60));
    public static final Block ANCIENT_SCROLL_PAPER_LANTERN = blockItem(
        "ancient_scroll_paper_lantern",
        new BlockFlammable(papery(MaterialColor.TERRACOTTA_ORANGE).lightLevel($ -> 12), 100, 60));
    public static final BlockSconce SCONCE = blockItem("amethyst_sconce",
        new BlockSconce(BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.COLOR_PURPLE)
            .sound(SoundType.AMETHYST)
            .strength(1f)
            .lightLevel($ -> 15)));

    public static final BlockAkashicLog EDIFIED_LOG = blockItem("edified_log",
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
    public static final WoodButtonBlock EDIFIED_BUTTON = blockItem("edified_button",
        new BlockHexWoodButton(edifiedWoody().noOcclusion()));
    public static final PressurePlateBlock EDIFIED_PRESSURE_PLATE = blockItem("edified_pressure_plate",
        new BlockHexPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, edifiedWoody().noOcclusion()));
    public static final BlockAkashicLeaves AMETHYST_EDIFIED_LEAVES = blockItem("amethyst_edified_leaves",
        new BlockAkashicLeaves(leaves(MaterialColor.COLOR_PURPLE)));
    public static final BlockAkashicLeaves AVENTURINE_EDIFIED_LEAVES = blockItem("aventurine_edified_leaves",
        new BlockAkashicLeaves(leaves(MaterialColor.COLOR_BLUE)));
    public static final BlockAkashicLeaves CITRINE_EDIFIED_LEAVES = blockItem("citrine_edified_leaves",
        new BlockAkashicLeaves(leaves(MaterialColor.COLOR_YELLOW)));

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
        return blockItem(name, block, HexItems.props());
    }

    private static <T extends Block> T blockItem(String name, T block, Item.Properties props) {
        blockNoItem(name, block);
        var old = BLOCK_ITEMS.put(modLoc(name), new Pair<>(block, props));
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return block;
    }
}


