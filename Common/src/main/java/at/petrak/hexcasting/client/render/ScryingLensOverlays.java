package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.lib.HexBlocks;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.MapColor;

import java.util.function.UnaryOperator;

public class ScryingLensOverlays {
    public static void addScryingLensStuff() {
        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction) -> state.getBlock() instanceof BlockAbstractImpetus,
            (lines, state, pos, observer, world, direction) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
                    beai.applyScryingLensOverlay(lines, state, pos, observer, world, direction);
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.NOTE_BLOCK,
            (lines, state, pos, observer, world, direction) -> {
                int note = state.getValue(NoteBlock.NOTE);

                float rCol = Math.max(0.0F, Mth.sin((note / 24F + 0.0F) * Mth.TWO_PI) * 0.65F + 0.35F);
                float gCol = Math.max(0.0F, Mth.sin((note / 24F + 0.33333334F) * Mth.TWO_PI) * 0.65F + 0.35F);
                float bCol = Math.max(0.0F, Mth.sin((note / 24F + 0.6666667F) * Mth.TWO_PI) * 0.65F + 0.35F);

                int noteColor = 0xFF_000000 | Mth.color(rCol, gCol, bCol);

                var instrument = state.getValue(NoteBlock.INSTRUMENT);

                lines.add(new Pair<>(
                    new ItemStack(Items.MUSIC_DISC_CHIRP),
                    Component.literal(String.valueOf(instrument.ordinal()))
                        .withStyle(color(instrumentColor(instrument)))));
                lines.add(new Pair<>(
                    new ItemStack(Items.NOTE_BLOCK),
                    Component.literal(String.valueOf(note))
                        .withStyle(color(noteColor))));
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_BOOKSHELF,
            (lines, state, pos, observer, world, direction) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
                    var iotaTag = tile.getIotaTag();
                    if (iotaTag != null) {
                        var display = IotaType.getDisplay(iotaTag);
                        lines.add(new Pair<>(new ItemStack(Items.BOOK), display));
                    }
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.COMPARATOR,
            (lines, state, pos, observer, world, direction) -> {
                int comparatorValue = state.getAnalogOutputSignal(world, pos);
                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    Component.literal(comparatorValue == -1 ? "" : String.valueOf(comparatorValue))
                        .withStyle(redstoneColor(comparatorValue))));

                boolean compare = state.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE;

                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE_TORCH),
                    Component.literal(compare ? ">=" : "-")
                        .withStyle(redstoneColor(compare ? 0 : 15))));
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.POWERED_RAIL,
            (lines, state, pos, observer, world, direction) -> {
                int power = getPoweredRailStrength(world, pos, state);
                lines.add(new Pair<>(
                    new ItemStack(Items.POWERED_RAIL),
                    Component.literal(String.valueOf(power))
                        .withStyle(redstoneColor(power, 9))));
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REPEATER,
            (lines, state, pos, observer, world, direction) -> lines.add(new Pair<>(
                new ItemStack(Items.CLOCK),
                Component.literal(String.valueOf(state.getValue(RepeaterBlock.DELAY)))
                    .withStyle(ChatFormatting.YELLOW))));

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction) -> state.isSignalSource() && !state.is(
                Blocks.COMPARATOR),
            (lines, state, pos, observer, world, direction) -> {
                int signalStrength = 0;
                if (state.getBlock() instanceof RedStoneWireBlock) {
                    signalStrength = state.getValue(RedStoneWireBlock.POWER);
                } else {
                    for (Direction dir : Direction.values()) {
                        signalStrength = Math.max(signalStrength, state.getSignal(world, pos, dir));
                    }
                }

                lines.add(0, new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    Component.literal(String.valueOf(signalStrength))
                        .withStyle(redstoneColor(signalStrength))));
            });
    }

    private static int getPoweredRailStrength(Level level, BlockPos pos, BlockState state) {
        if (level.hasNeighborSignal(pos))
            return 9;
        int positiveValue = findPoweredRailSignal(level, pos, state, true, 0);
        int negativeValue = findPoweredRailSignal(level, pos, state, false, 0);
        return Math.max(positiveValue, negativeValue);
    }

    // Copypasta from PoweredRailBlock.class
    private static int findPoweredRailSignal(Level level, BlockPos pos, BlockState state, boolean travelPositive,
        int depth) {
        if (depth >= 8) {
            return 0;
        } else {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            boolean descending = true;
            RailShape shape = state.getValue(PoweredRailBlock.SHAPE);
            switch (shape) {
                case NORTH_SOUTH:
                    if (travelPositive) {
                        ++z;
                    } else {
                        --z;
                    }
                    break;
                case EAST_WEST:
                    if (travelPositive) {
                        --x;
                    } else {
                        ++x;
                    }
                    break;
                case ASCENDING_EAST:
                    if (travelPositive) {
                        --x;
                    } else {
                        ++x;
                        ++y;
                        descending = false;
                    }

                    shape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_WEST:
                    if (travelPositive) {
                        --x;
                        ++y;
                        descending = false;
                    } else {
                        ++x;
                    }

                    shape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_NORTH:
                    if (travelPositive) {
                        ++z;
                    } else {
                        --z;
                        ++y;
                        descending = false;
                    }

                    shape = RailShape.NORTH_SOUTH;
                    break;
                case ASCENDING_SOUTH:
                    if (travelPositive) {
                        ++z;
                        ++y;
                        descending = false;
                    } else {
                        --z;
                    }

                    shape = RailShape.NORTH_SOUTH;
            }

            int power = getPowerFromRail(level, new BlockPos(x, y, z), travelPositive, depth,
                shape);

            if (power > 0) {
                return power;
            } else if (descending) {
                return getPowerFromRail(level, new BlockPos(x, y - 1, z), travelPositive, depth,
                    shape);
            } else {
                return 0;
            }
        }
    }


    private static UnaryOperator<Style> color(int color) {
        return (style) -> style.withColor(TextColor.fromRgb(color));
    }

    private static UnaryOperator<Style> redstoneColor(int power) {
        return redstoneColor(power, 15);
    }

    private static UnaryOperator<Style> redstoneColor(int power, int max) {
        return color(RedStoneWireBlock.getColorForPower(Mth.clamp((power * max) / 15, 0, 15)));
    }

    private static int instrumentColor(NoteBlockInstrument instrument) {
        return switch (instrument) {
            case BASEDRUM -> MapColor.STONE.col;
            case SNARE, XYLOPHONE, PLING -> MapColor.SAND.col;
            case HAT -> MapColor.QUARTZ.col;
            case BASS -> MapColor.WOOD.col;
            case FLUTE -> MapColor.CLAY.col;
            case BELL -> MapColor.GOLD.col;
            case GUITAR -> MapColor.WOOL.col;
            case CHIME -> MapColor.ICE.col;
            case IRON_XYLOPHONE -> MapColor.METAL.col;
            case COW_BELL -> MapColor.COLOR_BROWN.col;
            case DIDGERIDOO -> MapColor.COLOR_ORANGE.col;
            case BIT -> MapColor.EMERALD.col;
            case BANJO -> MapColor.COLOR_YELLOW.col;
            default -> -1;
        };
    }

    private static int getPowerFromRail(Level level, BlockPos pos, boolean travelPositive, int depth, RailShape shape) {
        BlockState otherState = level.getBlockState(pos);
        if (!otherState.is(Blocks.POWERED_RAIL)) {
            return 0;
        } else {
            RailShape otherShape = otherState.getValue(PoweredRailBlock.SHAPE);
            if (shape == RailShape.EAST_WEST && (otherShape == RailShape.NORTH_SOUTH || otherShape == RailShape.ASCENDING_NORTH || otherShape == RailShape.ASCENDING_SOUTH)) {
                return 0;
            } else if (shape == RailShape.NORTH_SOUTH && (otherShape == RailShape.EAST_WEST || otherShape == RailShape.ASCENDING_EAST || otherShape == RailShape.ASCENDING_WEST)) {
                return 0;
            } else if (otherState.getValue(PoweredRailBlock.POWERED)) {
                return level.hasNeighborSignal(pos) ? 8 - depth : findPoweredRailSignal(level, pos, otherState,
                    travelPositive, depth + 1);
            } else {
                return 0;
            }
        }
    }
}
