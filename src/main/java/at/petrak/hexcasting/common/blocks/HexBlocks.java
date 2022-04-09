package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
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
            BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.DIAMOND)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 15)
                .noDrops()
                .isValidSpawn((state, world, pos, entityType) -> false)
                .instabreak()
                .noCollission()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)));

    public static final RegistryObject<Block> CONJURED_BLOCK = blockNoItem("conjured_block",
        () -> new BlockConjured(
            BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.DIAMOND)
                .sound(SoundType.AMETHYST)
                .lightLevel((state) -> 15)
                .noDrops()
                .isValidSpawn((state, world, pos, entityType) -> false)
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

    private static BlockBehaviour.Properties papery() {
        return BlockBehaviour.Properties
            .of(Material.PLANT, MaterialColor.TERRACOTTA_WHITE)
            .sound(SoundType.GRASS)
            .instabreak();
    }

    private static BlockBehaviour.Properties woodyHard() {
        return BlockBehaviour.Properties.of(Material.WOOD)
            .sound(SoundType.WOOD)
            .strength(3f, 4f);
    }

    private static BlockBehaviour.Properties woody() {
        return BlockBehaviour.Properties.of(Material.WOOD)
            .sound(SoundType.WOOD)
            .strength(2f);
    }

    private static BlockBehaviour.Properties leaves() {
        return BlockBehaviour.Properties.of(Material.LEAVES)
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
        () -> new BlockAkashicRecord(woodyHard()));
    public static final RegistryObject<BlockAkashicBookshelf> AKASHIC_BOOKSHELF = blockItem("akashic_bookshelf",
        () -> new BlockAkashicBookshelf(woodyHard()));
    public static final RegistryObject<BlockAkashicFloodfiller> AKASHIC_CONNECTOR = blockItem("akashic_connector",
        () -> new BlockAkashicFloodfiller(woodyHard()));

    // Decoration?!
    public static final RegistryObject<Block> SLATE_BLOCK = blockItem("slate_block",
        () -> new Block(slateish().strength(2f, 4f)));
    public static final RegistryObject<SandBlock> AMETHYST_DUST_BLOCK = blockItem("amethyst_dust_block",
        () -> new SandBlock(0xff_b38ef3, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_PURPLE)
            .strength(0.5f).sound(SoundType.SAND)));
    public static final RegistryObject<AmethystBlock> AMETHYST_TILES = blockItem("amethyst_tiles",
        () -> new AmethystBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final RegistryObject<Block> SCROLL_PAPER = blockItem("scroll_paper",
        () -> new Block(papery()));
    public static final RegistryObject<Block> ANCIENT_SCROLL_PAPER = blockItem("ancient_scroll_paper",
        () -> new Block(papery()));
    public static final RegistryObject<Block> SCROLL_PAPER_LANTERN = blockItem("scroll_paper_lantern",
        () -> new Block(papery().lightLevel($ -> 15)));
    public static final RegistryObject<Block> ANCIENT_SCROLL_PAPER_LANTERN = blockItem(
        "ancient_scroll_paper_lantern",
        () -> new Block(papery().lightLevel($ -> 12)));
    public static final RegistryObject<BlockSconce> SCONCE = blockItem("amethyst_sconce",
        () -> new BlockSconce(BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.COLOR_PURPLE)
            .sound(SoundType.AMETHYST)
            .strength(1f)
            .lightLevel($ -> 15)));
    public static final RegistryObject<BlockAxis> AKASHIC_LOG_STRIPPED = blockItem("akashic_log_stripped",
        () -> new BlockAxis(woody()));
    public static final RegistryObject<BlockStrippable> AKASHIC_LOG = blockItem("akashic_log",
        () -> new BlockStrippable(woody(), AKASHIC_LOG_STRIPPED));
    public static final RegistryObject<Block> AKASHIC_WOOD_STRIPPED = blockItem("akashic_wood_stripped",
        () -> new Block(woody()));
    public static final RegistryObject<BlockStrippable> AKASHIC_WOOD = blockItem("akashic_wood",
        () -> new BlockStrippable(woody(), AKASHIC_WOOD_STRIPPED));
    public static final RegistryObject<Block> AKASHIC_PLANKS = blockItem("akashic_planks",
        () -> new Block(woody()));
    public static final RegistryObject<Block> AKASHIC_PANEL = blockItem("akashic_panel",
        () -> new Block(woody()));
    public static final RegistryObject<Block> AKASHIC_TILE = blockItem("akashic_tile",
        () -> new Block(woody()));
    public static final RegistryObject<DoorBlock> AKASHIC_DOOR = blockItem("akashic_door",
        () -> new DoorBlock(woody().noOcclusion()));
    public static final RegistryObject<TrapDoorBlock> AKASHIC_TRAPDOOR = blockItem("akashic_trapdoor",
        () -> new TrapDoorBlock(woody().noOcclusion()));
    public static final RegistryObject<LeavesBlock> AKASHIC_LEAVES1 = blockItem("akashic_leaves1",
        () -> new LeavesBlock(leaves()));
    public static final RegistryObject<LeavesBlock> AKASHIC_LEAVES2 = blockItem("akashic_leaves2",
        () -> new LeavesBlock(leaves()));
    public static final RegistryObject<LeavesBlock> AKASHIC_LEAVES3 = blockItem("akashic_leaves3",
        () -> new LeavesBlock(leaves()));

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


