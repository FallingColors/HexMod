package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityConjured;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityLookingImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityRightClickImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityStoredPlayerImpetus;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class HexBlockEntities {
    public static void registerTiles(BiConsumer<BlockEntityType<?>, ResourceLocation> r) {
        for (var e : BLOCK_ENTITIES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITIES = new LinkedHashMap<>();

    public static final BlockEntityType<BlockEntityConjured> CONJURED_TILE = register(
        "conjured_tile",
        BlockEntityConjured::new, HexBlocks.CONJURED_LIGHT, HexBlocks.CONJURED_BLOCK);

    public static final BlockEntityType<BlockEntityAkashicBookshelf> AKASHIC_BOOKSHELF_TILE = register(
        "akashic_bookshelf_tile",
        BlockEntityAkashicBookshelf::new, HexBlocks.AKASHIC_BOOKSHELF);

    public static final BlockEntityType<BlockEntityStoredPlayerImpetus> IMPETUS_STOREDPLAYER_TILE = register(
        "impetus_storedplayer_tile",
        BlockEntityStoredPlayerImpetus::new, HexBlocks.IMPETUS_STOREDPLAYER);
    public static final BlockEntityType<BlockEntityLookingImpetus> IMPETUS_LOOK_TILE = register(
        "impetus_look_tile",
        BlockEntityLookingImpetus::new, HexBlocks.IMPETUS_LOOK);
    public static final BlockEntityType<BlockEntityRightClickImpetus> IMPETUS_RIGHTCLICK_TILE = register(
        "impetus_rightclick_tile",
        BlockEntityRightClickImpetus::new, HexBlocks.IMPETUS_RIGHTCLICK);

    public static final BlockEntityType<BlockEntitySlate> SLATE_TILE = register(
        "slate_tile",
        BlockEntitySlate::new, HexBlocks.SLATE);

    private static <T extends BlockEntity> BlockEntityType<T> register(String id,
        BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
        var ret = IXplatAbstractions.INSTANCE.createBlockEntityType(func, blocks);
        var old = BLOCK_ENTITIES.put(new ResourceLocation(HexAPI.MOD_ID, id), ret);
        if (old != null) {
            throw new IllegalArgumentException("Duplicate id " + id);
        }
        return ret;
    }

}
