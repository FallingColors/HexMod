package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
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

import static at.petrak.hexcasting.common.items.HexItems.ITEMS;
import static at.petrak.hexcasting.common.items.HexItems.TAB;

public class HexBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HexMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES, HexMod.MOD_ID);

    public static final RegistryObject<Block> CONJURED = registerBlock("conjured",
        new BlockConjured(
            BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.DIAMOND).sound(SoundType.AMETHYST).noDrops()
                .instabreak().noOcclusion().isSuffocating(HexBlocks::never).isViewBlocking(HexBlocks::never)));
    public static final RegistryObject<Block> SLATE = registerBlock("slate",
        new BlockSlate(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.DEEPSLATE)
            .sound(SoundType.DEEPSLATE_TILES)
            .strength(2f, 4f)));

    public static final RegistryObject<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = BLOCK_ENTITIES.register(
        "conjured_tile",
        () -> BlockEntityType.Builder.of(BlockEntityConjured::new, CONJURED.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntitySlate>> SLATE_TILE = BLOCK_ENTITIES.register(
        "slate_tile",
        () -> BlockEntityType.Builder.of(BlockEntitySlate::new, SLATE.get()).build(null));


    private static RegistryObject<Block> registerBlock(String label, Block block) {
        ITEMS.register(label, () -> new BlockItem(block, new Item.Properties().tab(TAB)));
        return BLOCKS.register(label, () -> block);
    }

    private static boolean never(Object... args) {
        return false;
    }
}
