package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.common.blocks.BlockConjured;
import at.petrak.hexcasting.common.blocks.BlockConjuredLight;
import at.petrak.hexcasting.common.blocks.BlockFlammable;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicFloodfiller;
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

    private static BlockBehaviour.Properties akashicWoody() {
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
            .lightLevel(bs -> (bs.getValue(BlockAkashicBookshelf.DATUM_TYPE) == DatumType.EMPTY) ? 0 : 4)));
    public static final BlockAkashicFloodfiller AKASHIC_CONNECTOR = blockItem("akashic_connector",
        new BlockAkashicFloodfiller(akashicWoodyHard().lightLevel(bs -> 10)));

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

    public static final BlockAkashicLog AKASHIC_LOG = blockItem("akashic_log",
        new BlockAkashicLog(akashicWoody()));
    public static final BlockAkashicLog AKASHIC_LOG_STRIPPED = blockItem("akashic_log_stripped",
        new BlockAkashicLog(akashicWoody()));
    public static final BlockAkashicLog AKASHIC_WOOD = blockItem("akashic_wood",
        new BlockAkashicLog(akashicWoody()));
    public static final BlockAkashicLog AKASHIC_WOOD_STRIPPED = blockItem("akashic_wood_stripped",
        new BlockAkashicLog(akashicWoody()));
    public static final Block AKASHIC_PLANKS = blockItem("akashic_planks",
        new BlockFlammable(akashicWoody(), 20, 5));
    public static final Block AKASHIC_PANEL = blockItem("akashic_panel",
        new BlockFlammable(akashicWoody(), 20, 5));
    public static final Block AKASHIC_TILE = blockItem("akashic_tile",
        new BlockFlammable(akashicWoody(), 20, 5));
    public static final DoorBlock AKASHIC_DOOR = blockItem("akashic_door",
        new BlockHexDoor(akashicWoody().noOcclusion()));
    public static final TrapDoorBlock AKASHIC_TRAPDOOR = blockItem("akashic_trapdoor",
        new BlockHexTrapdoor(akashicWoody().noOcclusion()));
    public static final StairBlock AKASHIC_STAIRS = blockItem("akashic_stairs",
        new BlockHexStairs(AKASHIC_PLANKS.defaultBlockState(), akashicWoody().noOcclusion()));
    public static final SlabBlock AKASHIC_SLAB = blockItem("akashic_slab",
        new BlockHexSlab(akashicWoody().noOcclusion()));
    public static final WoodButtonBlock AKASHIC_BUTTON = blockItem("akashic_button",
        new BlockHexWoodButton(akashicWoody().noCollission()));
    public static final PressurePlateBlock AKASHIC_PRESSURE_PLATE = blockItem("akashic_pressure_plate",
        new BlockHexPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, akashicWoody().noCollission()));
    public static final BlockAkashicLeaves AKASHIC_LEAVES1 = blockItem("akashic_leaves1",
        new BlockAkashicLeaves(leaves(MaterialColor.COLOR_PURPLE)));
    public static final BlockAkashicLeaves AKASHIC_LEAVES2 = blockItem("akashic_leaves2",
        new BlockAkashicLeaves(leaves(MaterialColor.COLOR_BLUE)));
    public static final BlockAkashicLeaves AKASHIC_LEAVES3 = blockItem("akashic_leaves3",
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


