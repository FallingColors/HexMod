package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.common.blocks.akashic.*;
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockEmptyDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRightClickImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockStoredPlayerImpetus;
import at.petrak.hexcasting.common.blocks.decoration.BlockAxis;
import at.petrak.hexcasting.common.blocks.decoration.BlockSconce;
import at.petrak.hexcasting.common.blocks.decoration.BlockStrippable;
import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class HexBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HexMod.MOD_ID);

    public static final RegistryObject<Block> CONJURED_LIGHT = blockNoItem("conjured",
        () -> new BlockConjuredLight(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 15)
                .noDrops()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .noCollission()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)));

    public static final RegistryObject<Block> CONJURED_BLOCK = blockNoItem("conjured_block",
        () -> new BlockConjured(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 2)
                .noDrops()
                .isValidSpawn(HexBlocks::never)
                .instabreak()
                .noOcclusion()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)));

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

    public static final RegistryObject<BlockSlate> SLATE = BLOCKS.register("slate",
        () -> new BlockSlate(slateish()));

    public static final RegistryObject<BlockEmptyImpetus> EMPTY_IMPETUS = blockItem("empty_impetus",
        () -> new BlockEmptyImpetus(slateish()));

    public static final RegistryObject<BlockRightClickImpetus> IMPETUS_RIGHTCLICK = blockItem(
        "impetus_rightclick",
        () -> new BlockRightClickImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final RegistryObject<BlockLookingImpetus> IMPETUS_LOOK = blockItem(
        "impetus_look",
        () -> new BlockLookingImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));
    public static final RegistryObject<BlockStoredPlayerImpetus> IMPETUS_STOREDPLAYER = blockItem(
        "impetus_storedplayer",
        () -> new BlockStoredPlayerImpetus(slateish()
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));


    public static final RegistryObject<BlockEmptyDirectrix> EMPTY_DIRECTRIX = blockItem("empty_directrix",
        () -> new BlockEmptyDirectrix(slateish()));
    public static final RegistryObject<BlockRedstoneDirectrix> DIRECTRIX_REDSTONE = blockItem("directrix_redstone",
        () -> new BlockRedstoneDirectrix(slateish()));

    public static final RegistryObject<BlockAkashicRecord> AKASHIC_RECORD = blockItem("akashic_record",
        () -> new BlockAkashicRecord(akashicWoodyHard().lightLevel(bs -> 15)));
    public static final RegistryObject<BlockAkashicBookshelf> AKASHIC_BOOKSHELF = blockItem("akashic_bookshelf",
        () -> new BlockAkashicBookshelf(akashicWoodyHard()
            .lightLevel(bs -> (bs.getValue(BlockAkashicBookshelf.DATUM_TYPE) == DatumType.EMPTY) ? 0 : 4)));
    public static final RegistryObject<BlockAkashicFloodfiller> AKASHIC_CONNECTOR = blockItem("akashic_connector",
        () -> new BlockAkashicFloodfiller(akashicWoodyHard().lightLevel(bs -> 10)));

    // Decoration?!
    public static final RegistryObject<Block> SLATE_BLOCK = blockItem("slate_block",
        () -> new Block(slateish().strength(2f, 4f)));
    public static final RegistryObject<SandBlock> AMETHYST_DUST_BLOCK = blockItem("amethyst_dust_block",
        () -> new SandBlock(0xff_b38ef3, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_PURPLE)
            .strength(0.5f).sound(SoundType.SAND)));
    public static final RegistryObject<AmethystBlock> AMETHYST_TILES = blockItem("amethyst_tiles",
        () -> new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final RegistryObject<Block> SCROLL_PAPER = blockItem("scroll_paper",
        () -> new BlockBurns(papery(MaterialColor.TERRACOTTA_WHITE), 100, 60));
    public static final RegistryObject<Block> ANCIENT_SCROLL_PAPER = blockItem("ancient_scroll_paper",
        () -> new BlockBurns(papery(MaterialColor.TERRACOTTA_ORANGE), 100, 60));
    public static final RegistryObject<Block> SCROLL_PAPER_LANTERN = blockItem("scroll_paper_lantern",
        () -> new BlockBurns(papery(MaterialColor.TERRACOTTA_WHITE).lightLevel($ -> 15), 100, 60));
    public static final RegistryObject<Block> ANCIENT_SCROLL_PAPER_LANTERN = blockItem(
        "ancient_scroll_paper_lantern",
        () -> new BlockBurns(papery(MaterialColor.TERRACOTTA_ORANGE).lightLevel($ -> 12), 100, 60));
    public static final RegistryObject<BlockSconce> SCONCE = blockItem("amethyst_sconce",
        () -> new BlockSconce(BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.COLOR_PURPLE)
            .sound(SoundType.AMETHYST)
            .strength(1f)
            .lightLevel($ -> 15)));
    public static final RegistryObject<BlockAxis> AKASHIC_LOG_STRIPPED = blockItem("akashic_log_stripped",
        () -> new BlockAkashicLog(akashicWoody()));
    public static final RegistryObject<BlockStrippable> AKASHIC_LOG = blockItem("akashic_log",
        () -> new BlockAkashicWood(akashicWoody(), AKASHIC_LOG_STRIPPED));
    public static final RegistryObject<Block> AKASHIC_WOOD_STRIPPED = blockItem("akashic_wood_stripped",
        () -> new BlockBurns(akashicWoody(), 5, 5));
    public static final RegistryObject<BlockStrippable> AKASHIC_WOOD = blockItem("akashic_wood",
        () -> new BlockAkashicWood(akashicWoody(), AKASHIC_WOOD_STRIPPED));
    public static final RegistryObject<Block> AKASHIC_PLANKS = blockItem("akashic_planks",
        () -> new BlockBurns(akashicWoody(), 20, 5));
    public static final RegistryObject<Block> AKASHIC_PANEL = blockItem("akashic_panel",
        () -> new BlockBurns(akashicWoody(), 20, 5));
    public static final RegistryObject<Block> AKASHIC_TILE = blockItem("akashic_tile",
        () -> new BlockBurns(akashicWoody(), 20, 5));
    public static final RegistryObject<DoorBlock> AKASHIC_DOOR = blockItem("akashic_door",
        () -> new DoorBlock(akashicWoody().noOcclusion()));
    public static final RegistryObject<TrapDoorBlock> AKASHIC_TRAPDOOR = blockItem("akashic_trapdoor",
        () -> new TrapDoorBlock(akashicWoody().noOcclusion()));
    public static final RegistryObject<StairBlock> AKASHIC_STAIRS = blockItem("akashic_stairs",
        () -> new BlockAkashicStairs(() -> AKASHIC_PLANKS.get().defaultBlockState(), akashicWoody().noOcclusion()));
    public static final RegistryObject<SlabBlock> AKASHIC_SLAB = blockItem("akashic_slab",
        () -> new BlockAkashicSlab(akashicWoody().noOcclusion()));
    public static final RegistryObject<WoodButtonBlock> AKASHIC_BUTTON = blockItem("akashic_button",
        () -> new WoodButtonBlock(akashicWoody().noCollission()));
    public static final RegistryObject<PressurePlateBlock> AKASHIC_PRESSURE_PLATE = blockItem("akashic_pressure_plate",
        () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, akashicWoody().noCollission()));
    public static final RegistryObject<BlockAkashicLeaves> AKASHIC_LEAVES1 = blockItem("akashic_leaves1",
        () -> new BlockAkashicLeaves(leaves(MaterialColor.COLOR_PURPLE)));
    public static final RegistryObject<BlockAkashicLeaves> AKASHIC_LEAVES2 = blockItem("akashic_leaves2",
        () -> new BlockAkashicLeaves(leaves(MaterialColor.COLOR_BLUE)));
    public static final RegistryObject<BlockAkashicLeaves> AKASHIC_LEAVES3 = blockItem("akashic_leaves3",
        () -> new BlockAkashicLeaves(leaves(MaterialColor.COLOR_YELLOW)));

    private static boolean never(Object... args) {
        return false;
    }

    private static <T extends Block> RegistryObject<T> blockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> blockItem(String name, Supplier<T> block) {
        return blockItem(name, block, HexItems.props());
    }

    private static <T extends Block> RegistryObject<T> blockItem(String name, Supplier<T> block,
        Item.Properties props) {
        var out = BLOCKS.register(name, block);
        HexItems.ITEMS.register(name, () -> new BlockItem(out.get(), props));
        return out;
    }
}


