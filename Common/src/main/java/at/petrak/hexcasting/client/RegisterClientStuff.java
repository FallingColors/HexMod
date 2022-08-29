package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.be.BlockEntitySlateRenderer;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.client.particles.ConjureParticle;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.ItemSlate;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexParticles;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.UnaryOperator;

public class RegisterClientStuff {
    public static void init() {
        registerDataHolderOverrides(HexItems.FOCUS);
        registerDataHolderOverrides(HexItems.SPELLBOOK);

        registerPackagedSpellOverrides(HexItems.CYPHER);
        registerPackagedSpellOverrides(HexItems.TRINKET);
        registerPackagedSpellOverrides(HexItems.ARTIFACT);

        var x = IClientXplatAbstractions.INSTANCE;
        x.registerItemProperty(HexItems.BATTERY, ItemManaBattery.MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ManaHolderItem) stack.getItem();
                return item.getManaFullness(stack);
            });
        x.registerItemProperty(HexItems.BATTERY, ItemManaBattery.MAX_MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ItemManaBattery) stack.getItem();
                var max = item.getMaxMana(stack);
                return (float) Math.sqrt((float) max / ManaConstants.CRYSTAL_UNIT / 10);
            });

        registerScollOverrides(HexItems.SCROLL_SMOL);
        registerScollOverrides(HexItems.SCROLL_MEDIUM);
        registerScollOverrides(HexItems.SCROLL_LARGE);

        x.registerItemProperty(HexItems.SLATE, ItemSlate.WRITTEN_PRED,
            (stack, level, holder, holderID) -> ItemSlate.hasPattern(stack) ? 1f : 0f);

        registerWandOverrides(HexItems.WAND_OAK);
        registerWandOverrides(HexItems.WAND_BIRCH);
        registerWandOverrides(HexItems.WAND_SPRUCE);
        registerWandOverrides(HexItems.WAND_JUNGLE);
        registerWandOverrides(HexItems.WAND_DARK_OAK);
        registerWandOverrides(HexItems.WAND_ACACIA);
        registerWandOverrides(HexItems.WAND_AKASHIC);

        HexTooltips.init();

        x.setRenderLayer(HexBlocks.CONJURED_LIGHT, RenderType.cutout());
        x.setRenderLayer(HexBlocks.CONJURED_BLOCK, RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_DOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_TRAPDOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.SCONCE, RenderType.cutout());

        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES1, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES2, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES3, RenderType.cutoutMipped());

        x.setRenderLayer(HexBlocks.AKASHIC_RECORD, RenderType.translucent());

        x.registerEntityRenderer(HexEntities.WALL_SCROLL, WallScrollRenderer::new);

        addScryingLensStuff();
    }

    private static void addScryingLensStuff() {
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
                    new TextComponent(String.valueOf(instrument.ordinal()))
                        .withStyle(color(instrumentColor(instrument)))));
                lines.add(new Pair<>(
                    new ItemStack(Items.NOTE_BLOCK),
                    new TextComponent(String.valueOf(note))
                        .withStyle(color(noteColor))));
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_BOOKSHELF,
            (lines, state, pos, observer, world, direction) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
                    var recordPos = tile.getRecordPos();
                    var pattern = tile.getPattern();
                    if (recordPos != null && pattern != null) {
                        lines.add(new Pair<>(new ItemStack(HexBlocks.AKASHIC_RECORD), new TranslatableComponent(
                            "hexcasting.tooltip.lens.akashic.bookshelf.location",
                            recordPos.toShortString()
                        )));
                        if (world.getBlockEntity(recordPos) instanceof BlockEntityAkashicRecord record) {
                            lines.add(new Pair<>(new ItemStack(Items.BOOK), record.getDisplayAt(pattern)));
                        }
                    }
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_RECORD,
            (lines, state, pos, observer, world, direction) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicRecord tile) {
                    int count = tile.getCount();

                    lines.add(new Pair<>(new ItemStack(HexBlocks.AKASHIC_BOOKSHELF), new TranslatableComponent(
                        "hexcasting.tooltip.lens.akashic.record.count" + (count == 1 ? ".single" : ""),
                        count
                    )));
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.COMPARATOR,
            (lines, state, pos, observer, world, direction) -> {
                int comparatorValue = ScryingLensOverlayRegistry.getComparatorValue(true);
                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    new TextComponent(comparatorValue == -1 ? "" : String.valueOf(comparatorValue))
                        .withStyle(redstoneColor(comparatorValue))));

                boolean compare = state.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE;

                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE_TORCH),
                    new TextComponent(
                        compare ? ">=" : "-")
                        .withStyle(redstoneColor(compare ? 0 : 15))));
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.POWERED_RAIL,
            (lines, state, pos, observer, world, direction) -> {
                int power = getPoweredRailStrength(world, pos, state);
                lines.add(new Pair<>(
                    new ItemStack(Items.POWERED_RAIL),
                    new TextComponent(String.valueOf(power))
                        .withStyle(redstoneColor(power, 9))));
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REPEATER,
            (lines, state, pos, observer, world, direction) -> lines.add(new Pair<>(
                new ItemStack(Items.CLOCK),
                new TextComponent(String.valueOf(state.getValue(RepeaterBlock.DELAY)))
                    .withStyle(ChatFormatting.YELLOW))));

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction) -> state.getBlock() instanceof BeehiveBlock,
            (lines, state, pos, observer, world, direction) -> {
                int count = ScryingLensOverlayRegistry.getBeeValue();
                lines.add(new Pair<>(new ItemStack(Items.BEE_NEST), count == -1 ? new TextComponent("") :
                    new TranslatableComponent(
                        "hexcasting.tooltip.lens.bee" + (count == 1 ? ".single" : ""),
                        count
                    )));
            });

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
                    new TextComponent(String.valueOf(signalStrength))
                        .withStyle(redstoneColor(signalStrength))));
            });

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction) -> state.hasAnalogOutputSignal(),
            (lines, state, pos, observer, world, direction) -> {
                int comparatorValue = ScryingLensOverlayRegistry.getComparatorValue(false);
                lines.add(
                    new Pair<>(
                        new ItemStack(Items.COMPARATOR),
                        new TextComponent(comparatorValue == -1 ? "" : String.valueOf(comparatorValue))
                            .withStyle(redstoneColor(comparatorValue))));
            });
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
            case BASEDRUM -> MaterialColor.STONE.col;
            case SNARE, XYLOPHONE, PLING -> MaterialColor.SAND.col;
            case HAT -> MaterialColor.QUARTZ.col;
            case BASS -> MaterialColor.WOOD.col;
            case FLUTE -> MaterialColor.CLAY.col;
            case BELL -> MaterialColor.GOLD.col;
            case GUITAR -> MaterialColor.WOOL.col;
            case CHIME -> MaterialColor.ICE.col;
            case IRON_XYLOPHONE -> MaterialColor.METAL.col;
            case COW_BELL -> MaterialColor.COLOR_BROWN.col;
            case DIDGERIDOO -> MaterialColor.COLOR_ORANGE.col;
            case BIT -> MaterialColor.EMERALD.col;
            case BANJO -> MaterialColor.COLOR_YELLOW.col;
            default -> -1;
        };
    }

    private static int getPoweredRailStrength(Level level, BlockPos pos, BlockState state) {
        if (level.hasNeighborSignal(pos))
            return 9;
        int positiveValue = findPoweredRailSignal(level, pos, state, true, 0);
        int negativeValue = findPoweredRailSignal(level, pos, state, false, 0);
        return Math.max(positiveValue, negativeValue);
    }

    // Copypasta from PoweredRailBlock.class
    private static int findPoweredRailSignal(Level level, BlockPos pos, BlockState state, boolean travelPositive, int depth) {
        if (depth >= 8) {
            return 0;
        } else {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            boolean descending = true;
            RailShape shape = state.getValue(PoweredRailBlock.SHAPE);
            switch(shape) {
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

            int power = getPowerFromRail(level, new BlockPos(x, y, z), travelPositive, depth, shape);

            if (power > 0) {
                return power;
            } else if (descending) {
                return getPowerFromRail(level, new BlockPos(x, y - 1, z), travelPositive, depth, shape);
            } else {
                return 0;
            }
        }
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
                return level.hasNeighborSignal(pos) ? 8 - depth : findPoweredRailSignal(level, pos, otherState, travelPositive, depth + 1);
            } else {
                return 0;
            }
        }
    }

    private static void registerScollOverrides(ItemScroll scroll) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(scroll, ItemScroll.ANCIENT_PREDICATE,
            (stack, level, holder, holderID) -> NBTHelper.hasString(stack, ItemScroll.TAG_OP_ID) ? 1f : 0f);
    }

    private static void registerDataHolderOverrides(DataHolderItem item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.DATATYPE_PRED,
            (stack, level, holder, holderID) -> {
                var datum = item.readDatumTag(stack);
                String override = NBTHelper.getString(stack, DataHolderItem.TAG_OVERRIDE_VISUALLY);
                String typename = null;
                if (override != null) {
                    typename = override;
                } else if (datum != null) {
                    typename = datum.getAllKeys().iterator().next();
                }

                return typename == null ? 0f : switch (typename) {
                    case SpellDatum.TAG_ENTITY -> 1f;
                    case SpellDatum.TAG_DOUBLE -> 2f;
                    case SpellDatum.TAG_VEC3 -> 3f;
                    case SpellDatum.TAG_WIDGET -> 4f;
                    case SpellDatum.TAG_LIST -> 5f;
                    case SpellDatum.TAG_PATTERN -> 6f;
                    default -> 0f; // uh oh
                };
            });
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.SEALED_PRED,
            (stack, level, holder, holderID) -> item.canWrite(stack, SpellDatum.make(Widget.NULL)) ? 0f : 1f);
    }

    private static void registerPackagedSpellOverrides(ItemPackagedHex item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemPackagedHex.HAS_PATTERNS_PRED,
            (stack, level, holder, holderID) ->
                item.hasHex(stack) ? 1f : 0f
        );
    }

    private static void registerWandOverrides(ItemWand item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemWand.FUNNY_LEVEL_PREDICATE,
            (stack, level, holder, holderID) -> {
                if (!stack.hasCustomHoverName()) {
                    return 0;
                }
                var name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (name.contains("old")) {
                    return 1f;
                } else if (name.contains("wand of the forest")) {
                    return 2f;
                } else {
                    return 0f;
                }
            });
    }

    public static void registerParticles() {
        // rip particle man
        IClientXplatAbstractions.INSTANCE.registerParticleType(HexParticles.LIGHT_PARTICLE,
            ConjureParticle.Provider::new);
        IClientXplatAbstractions.INSTANCE.registerParticleType(HexParticles.CONJURE_PARTICLE,
            ConjureParticle.Provider::new);
    }

    public static void registerBlockEntityRenderers(@NotNull BlockEntityRendererRegisterererer registerer) {
        registerer.registerBlockEntityRenderer(HexBlockEntities.SLATE_TILE, BlockEntitySlateRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.AKASHIC_BOOKSHELF_TILE,
            BlockEntityAkashicBookshelfRenderer::new);
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegisterererer {
        <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
            BlockEntityRendererProvider<? super T> berp);
    }
}
