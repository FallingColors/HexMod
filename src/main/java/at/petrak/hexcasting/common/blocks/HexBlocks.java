package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRightClickImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRightClickImpetus;
import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HexMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES, HexMod.MOD_ID);

    public static final RegistryObject<Block> CONJURED = registerBlock("conjured",
        new BlockConjured(
            BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.DIAMOND)
                .sound(SoundType.AMETHYST)
                .noDrops()
                .instabreak()
                .noOcclusion()
                .isSuffocating(HexBlocks::never)
                .isViewBlocking(HexBlocks::never)));

    public static final RegistryObject<Block> SLATE = BLOCKS.register("slate",
        () -> new BlockSlate(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.DEEPSLATE)
            .sound(SoundType.DEEPSLATE_TILES)
            .strength(4f, 4f)));
    public static final RegistryObject<BlockRightClickImpetus> IMPETUS_RIGHTCLICK = registerBlock("impetus_rightclick",
        new BlockRightClickImpetus(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.DEEPSLATE)
            .sound(SoundType.DEEPSLATE_TILES)
            .strength(4f, 4f)
            .lightLevel(bs -> bs.getValue(BlockAbstractImpetus.ENERGIZED) ? 15 : 0)));

    // Decoration?!
    public static final RegistryObject<Block> SLATE_BLOCK = registerBlock("slate_block",
        new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.DEEPSLATE)
            .sound(SoundType.DEEPSLATE_TILES)
            .strength(2f, 4f)));

    public static final RegistryObject<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = BLOCK_ENTITIES.register(
        "conjured_tile",
        () -> BlockEntityType.Builder.of(BlockEntityConjured::new, CONJURED.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntitySlate>> SLATE_TILE = BLOCK_ENTITIES.register(
        "slate_tile",
        () -> BlockEntityType.Builder.of(BlockEntitySlate::new, SLATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityRightClickImpetus>> IMPETUS_RIGHTCLICK_TILE =
        BLOCK_ENTITIES.register("impetus_rightclick_tile",
            () -> BlockEntityType.Builder.of(BlockEntityRightClickImpetus::new, IMPETUS_RIGHTCLICK.get()).build(null));


    private static <T extends Block> RegistryObject<T> registerBlock(String label, T block) {
        HexItems.ITEMS.register(label, () -> new BlockItem(block, new Item.Properties().tab(HexItems.TAB)));
        return BLOCKS.register(label, () -> block);
    }

    private static boolean never(Object... args) {
        return false;
    }
}
