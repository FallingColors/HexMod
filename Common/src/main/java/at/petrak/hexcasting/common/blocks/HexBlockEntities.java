package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRightClickImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityStoredPlayerImpetus;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES, HexMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = BLOCK_ENTITIES.register(
        "conjured_tile",
        () -> BlockEntityType.Builder.of(BlockEntityConjured::new, HexBlocks.CONJURED_LIGHT.get(), HexBlocks.CONJURED_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityAkashicBookshelf>> AKASHIC_BOOKSHELF_TILE = BLOCK_ENTITIES.register(
        "akashic_bookshelf_tile",
        () -> BlockEntityType.Builder.of(BlockEntityAkashicBookshelf::new, HexBlocks.AKASHIC_BOOKSHELF.get())
            .build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityAkashicRecord>> AKASHIC_RECORD_TILE = BLOCK_ENTITIES.register(
        "akashic_record_tile",
        () -> BlockEntityType.Builder.of(BlockEntityAkashicRecord::new, HexBlocks.AKASHIC_RECORD.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityStoredPlayerImpetus>> IMPETUS_STOREDPLAYER_TILE =
        BLOCK_ENTITIES.register("impetus_storedplayer_tile",
            () -> BlockEntityType.Builder.of(BlockEntityStoredPlayerImpetus::new, HexBlocks.IMPETUS_STOREDPLAYER.get())
                .build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityLookingImpetus>> IMPETUS_LOOK_TILE =
        BLOCK_ENTITIES.register("impetus_look_tile",
            () -> BlockEntityType.Builder.of(BlockEntityLookingImpetus::new, HexBlocks.IMPETUS_LOOK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityRightClickImpetus>> IMPETUS_RIGHTCLICK_TILE =
        BLOCK_ENTITIES.register("impetus_rightclick_tile",
            () -> BlockEntityType.Builder.of(BlockEntityRightClickImpetus::new, HexBlocks.IMPETUS_RIGHTCLICK.get())
                .build(null));

    public static final RegistryObject<BlockEntityType<BlockEntitySlate>> SLATE_TILE = BLOCK_ENTITIES.register(
        "slate_tile",
        () -> BlockEntityType.Builder.of(BlockEntitySlate::new, HexBlocks.SLATE.get()).build(null));
}
