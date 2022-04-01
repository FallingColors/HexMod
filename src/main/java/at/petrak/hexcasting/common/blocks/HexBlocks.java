package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.akashic.*;
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockEmptyDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.blocks.circles.impetuses.*;
import at.petrak.hexcasting.common.blocks.decoration.BlockSconce;
import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class HexBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HexMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES, HexMod.MOD_ID);

    public static final RegistryObject<Block> CONJURED = blockItem("conjured",
        () -> new BlockConjured(
            BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.DIAMOND)
                .sound(SoundType.AMETHYST)
                .noDrops()
                .instabreak()
                .noOcclusion()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)),
        new Item.Properties());

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

    public static final RegistryObject<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = BLOCK_ENTITIES.register(
        "conjured_tile",
        () -> BlockEntityType.Builder.of(BlockEntityConjured::new, CONJURED.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntitySlate>> SLATE_TILE = BLOCK_ENTITIES.register(
        "slate_tile",
        () -> BlockEntityType.Builder.of(BlockEntitySlate::new, SLATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityRightClickImpetus>> IMPETUS_RIGHTCLICK_TILE =
        BLOCK_ENTITIES.register("impetus_rightclick_tile",
            () -> BlockEntityType.Builder.of(BlockEntityRightClickImpetus::new, IMPETUS_RIGHTCLICK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityLookingImpetus>> IMPETUS_LOOK_TILE =
        BLOCK_ENTITIES.register("impetus_look_tile",
            () -> BlockEntityType.Builder.of(BlockEntityLookingImpetus::new, IMPETUS_LOOK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityStoredPlayerImpetus>> IMPETUS_STOREDPLAYER_TILE =
        BLOCK_ENTITIES.register("impetus_storedplayer_tile",
            () -> BlockEntityType.Builder.of(BlockEntityStoredPlayerImpetus::new, IMPETUS_STOREDPLAYER.get())
                .build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityAkashicRecord>> AKASHIC_RECORD_TILE = BLOCK_ENTITIES.register(
        "akashic_record_tile",
        () -> BlockEntityType.Builder.of(BlockEntityAkashicRecord::new, AKASHIC_RECORD.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityAkashicBookshelf>> AKASHIC_BOOKSHELF_TILE = BLOCK_ENTITIES.register(
        "akashic_bookshelf_tile",
        () -> BlockEntityType.Builder.of(BlockEntityAkashicBookshelf::new, AKASHIC_BOOKSHELF.get()).build(null));


    private static boolean never(Object... args) {
        return false;
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
