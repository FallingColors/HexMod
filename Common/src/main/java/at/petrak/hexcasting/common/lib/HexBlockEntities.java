package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityLookingImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRightClickImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityConjured;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class HexBlockEntities {
    private static final IXplatRegister<BlockEntityType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.BLOCK_ENTITY_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<BlockEntityType<BlockEntityConjured>> CONJURED_TILE = REGISTER.register("conjured", () ->
            IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityConjured::new,
                    HexBlocks.CONJURED_LIGHT.get(), HexBlocks.CONJURED_BLOCK.get()));

    public static final Supplier<BlockEntityType<BlockEntityAkashicBookshelf>> AKASHIC_BOOKSHELF_TILE = REGISTER.register(
        "akashic_bookshelf",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityAkashicBookshelf::new,
                    HexBlocks.AKASHIC_BOOKSHELF.get()));

    public static final Supplier<BlockEntityType<BlockEntityRedstoneImpetus>> IMPETUS_REDSTONE_TILE = REGISTER.register(
        "impetus/redstone",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityRedstoneImpetus::new,
                    HexBlocks.IMPETUS_REDSTONE.get()));
    public static final Supplier<BlockEntityType<BlockEntityLookingImpetus>> IMPETUS_LOOK_TILE = REGISTER.register(
        "impetus/look",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityLookingImpetus::new,
                    HexBlocks.IMPETUS_LOOK.get()));
    public static final Supplier<BlockEntityType<BlockEntityRightClickImpetus>> IMPETUS_RIGHTCLICK_TILE = REGISTER.register(
        "impetus/rightclick",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntityRightClickImpetus::new,
                    HexBlocks.IMPETUS_RIGHTCLICK.get()));

    public static final Supplier<BlockEntityType<BlockEntitySlate>> SLATE_TILE = REGISTER.register(
        "slate",
            () -> IXplatAbstractions.INSTANCE.createBlockEntityType(BlockEntitySlate::new,
                    HexBlocks.SLATE.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_TILE = REGISTER.register(
        "quenched_allay", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY.get()),
                    HexBlocks.QUENCHED_ALLAY.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_TILES_TILE = REGISTER.register(
            "quenched_allay_tiles", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                    BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY_TILES.get()),
                    HexBlocks.QUENCHED_ALLAY_TILES.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_BRICKS_TILE = REGISTER.register(
            "quenched_allay_bricks", () -> IXplatAbstractions.INSTANCE.createBlockEntityType(
                    BlockEntityQuenchedAllay.fromKnownBlock(HexBlocks.QUENCHED_ALLAY_BRICKS.get()),
                    HexBlocks.QUENCHED_ALLAY_BRICKS.get()));

    public static final Supplier<BlockEntityType<BlockEntityQuenchedAllay>> QUENCHED_ALLAY_BRICKS_SMALL_TILE = REGISTER.register(
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
