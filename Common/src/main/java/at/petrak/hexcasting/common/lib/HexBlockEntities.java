package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRightClickImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityConjured;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class HexBlockEntities {
    private static final IXplatRegistrar<BlockEntityType<?>> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(Registries.BLOCK_ENTITY_TYPE);

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Supplier<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = REGISTRAR.register("conjured", () ->
            IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityConjured::new,
                    HexBlocks.CONJURED_LIGHT.get(), HexBlocks.CONJURED_BLOCK.get()));

    public static final Supplier<BlockEntityType<BlockEntityAkashicBookshelf>> AKASHIC_BOOKSHELF_TILE = REGISTRAR.register(
        "akashic_bookshelf",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityAkashicBookshelf::new,
                    HexBlocks.AKASHIC_BOOKSHELF.get()));

    public static final Supplier<BlockEntityType<BlockEntityRedstoneImpetus>> IMPETUS_REDSTONE_TILE = REGISTRAR.register(
        "impetus/redstone",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityRedstoneImpetus::new,
                    HexBlocks.IMPETUS_REDSTONE.get()));
    public static final Supplier<BlockEntityType<BlockEntityLookingImpetus>> IMPETUS_LOOK_TILE = REGISTRAR.register(
        "impetus/look",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityLookingImpetus::new,
                    HexBlocks.IMPETUS_LOOK.get()));
    public static final Supplier<BlockEntityType<BlockEntityRightClickImpetus>> IMPETUS_RIGHTCLICK_TILE = REGISTRAR.register(
        "impetus/rightclick",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityRightClickImpetus::new,
                    HexBlocks.IMPETUS_RIGHTCLICK.get()));

    public static final Supplier<BlockEntityType<BlockEntitySlate>> SLATE_TILE = REGISTRAR.register(
        "slate",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntitySlate::new,
                    HexBlocks.SLATE.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_TILE = REGISTRAR.register(
        "quenched_allay", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY.get()),
                    HexBlocks.QUENCHED_ALLAY.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_TILES_TILE = REGISTRAR.register(
            "quenched_allay_tiles", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                    BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY_TILES.get()),
                    HexBlocks.QUENCHED_ALLAY_TILES.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_BRICKS_TILE = REGISTRAR.register(
            "quenched_allay_bricks", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                    BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY_BRICKS.get()),
                    HexBlocks.QUENCHED_ALLAY_BRICKS.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_BRICKS_SMALL_TILE = REGISTRAR.register(
            "quenched_allay_bricks_small", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                    BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get()),
                    HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get()));

    public static BlockEntityType<BlockEntityQuenchedAllay> typeForQuenchedAllay(BlockQuenchedAllay block) {
        if (block == HexBlocks.QUENCHED_ALLAY.get())
            return QUENCHED_ALLAY_TILE.get();
        if (block == HexBlocks.QUENCHED_ALLAY_TILES.get())
            return QUENCHED_ALLAY_TILES_TILE.get();
        if (block == HexBlocks.QUENCHED_ALLAY_BRICKS.get())
            return QUENCHED_ALLAY_BRICKS_TILE.get();
        if (block == HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get())
            return QUENCHED_ALLAY_BRICKS_SMALL_TILE.get();
        return null;
    }

}
