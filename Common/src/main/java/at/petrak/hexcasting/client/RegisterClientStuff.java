package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.be.BlockEntitySlateRenderer;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.client.particles.ConjureParticle;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.*;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.*;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

public class RegisterClientStuff {
    public static void init() {
        registerDataHolderOverrides(HexItems.FOCUS,
            stack -> HexItems.FOCUS.readIotaTag(stack) != null,
            ItemFocus::isSealed);
        registerDataHolderOverrides(HexItems.SPELLBOOK,
            stack -> HexItems.SPELLBOOK.readIotaTag(stack) != null,
            ItemSpellbook::isSealed);

        registerPackagedSpellOverrides(HexItems.CYPHER);
        registerPackagedSpellOverrides(HexItems.TRINKET);
        registerPackagedSpellOverrides(HexItems.ARTIFACT);

        var x = IClientXplatAbstractions.INSTANCE;
        x.registerItemProperty(HexItems.BATTERY, ItemMediaBattery.MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (MediaHolderItem) stack.getItem();
                return item.getManaFullness(stack);
            });
        x.registerItemProperty(HexItems.BATTERY, ItemMediaBattery.MAX_MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ItemMediaBattery) stack.getItem();
                var max = item.getMaxMedia(stack);
                return (float) Math.sqrt((float) max / ManaConstants.CRYSTAL_UNIT / 10);
            });

        registerScrollOverrides(HexItems.SCROLL_SMOL);
        registerScrollOverrides(HexItems.SCROLL_MEDIUM);
        registerScrollOverrides(HexItems.SCROLL_LARGE);

        x.registerItemProperty(HexItems.SLATE, ItemSlate.WRITTEN_PRED,
            (stack, level, holder, holderID) -> ItemSlate.hasPattern(stack) ? 1f : 0f);

        registerWandOverrides(HexItems.STAFF_OAK);
        registerWandOverrides(HexItems.STAFF_BIRCH);
        registerWandOverrides(HexItems.STAFF_SPRUCE);
        registerWandOverrides(HexItems.STAFF_JUNGLE);
        registerWandOverrides(HexItems.STAFF_DARK_OAK);
        registerWandOverrides(HexItems.STAFF_ACACIA);
        registerWandOverrides(HexItems.STAFF_EDIFIED);

        HexTooltips.init();

        x.setRenderLayer(HexBlocks.CONJURED_LIGHT, RenderType.cutout());
        x.setRenderLayer(HexBlocks.CONJURED_BLOCK, RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_DOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_TRAPDOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.SCONCE, RenderType.cutout());

        x.setRenderLayer(HexBlocks.AMETHYST_EDIFIED_LEAVES, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AVENTURINE_EDIFIED_LEAVES, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.CITRINE_EDIFIED_LEAVES, RenderType.cutoutMipped());

        x.setRenderLayer(HexBlocks.AKASHIC_RECORD, RenderType.translucent());

        x.registerEntityRenderer(HexEntities.WALL_SCROLL, WallScrollRenderer::new);

        addScryingLensStuff();
    }

    public static void registerColorProviders(BiConsumer<ItemColor, Item> itemColorRegistry,
        BiConsumer<BlockColor, Block> blockColorRegistry) {
        itemColorRegistry.accept(makeIotaStorageColorer(HexItems.FOCUS::getColor), HexItems.FOCUS);
        itemColorRegistry.accept(makeIotaStorageColorer(HexItems.SPELLBOOK::getColor), HexItems.SPELLBOOK);

        blockColorRegistry.accept((bs, level, pos, idx) -> {
            if (!bs.getValue(BlockAkashicBookshelf.HAS_BOOKS) || level == null || pos == null) {
                return 0xff_ffffff;
            }
            var tile = level.getBlockEntity(pos);
            if (!(tile instanceof BlockEntityAkashicBookshelf beas)) {
                // this gets called for particles for some irritating reason
                return 0xff_ffffff;
            }
            var iotaTag = beas.getIotaTag();
            if (iotaTag == null) {
                return 0xff_ffffff;
            }
            return HexIotaTypes.getColor(iotaTag);
        }, HexBlocks.AKASHIC_BOOKSHELF);
    }

    /**
     * Helper function to colorize the layers of an item that stores an iota, in the manner of foci and spellbooks.
     * <br>
     * 0 = base; 1 = unsealed overlay; 2 = sealed overlay.
     */
    public static ItemColor makeIotaStorageColorer(ToIntFunction<ItemStack> getColor) {
        return (stack, idx) -> {
            if (idx == 0) {
                return 0xff_ffffff;
            }
            return getColor.applyAsInt(stack);
        };
    }

    private static void addScryingLensStuff() {
        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction, lensHand) -> state.getBlock() instanceof BlockAbstractImpetus,
            (lines, state, pos, observer, world, direction, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
                    beai.applyScryingLensOverlay(lines, state, pos, observer, world, direction, lensHand);
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.NOTE_BLOCK,
            (lines, state, pos, observer, world, direction, lensHand) -> {
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
            (lines, state, pos, observer, world, direction, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
                    var iotaTag = tile.getIotaTag();
                    if (iotaTag != null) {
                        var display = HexIotaTypes.getDisplay(iotaTag);
                        lines.add(new Pair<>(new ItemStack(Items.BOOK), display));
                    }
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.COMPARATOR,
            (lines, state, pos, observer, world, direction, lensHand) -> {
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

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REPEATER,
            (lines, state, pos, observer, world, direction, lensHand) -> lines.add(new Pair<>(
                new ItemStack(Items.CLOCK),
                new TextComponent(String.valueOf(state.getValue(RepeaterBlock.DELAY)))
                    .withStyle(ChatFormatting.YELLOW))));

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction, lensHand) -> state.isSignalSource() && !state.is(
                Blocks.COMPARATOR),
            (lines, state, pos, observer, world, direction, lensHand) -> {
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
            (state, pos, observer, world, direction, lensHand) -> state.hasAnalogOutputSignal(),
            (lines, state, pos, observer, world, direction, lensHand) -> {
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
        return color(RedStoneWireBlock.getColorForPower(Mth.clamp(power, 0, 15)));
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

    private static void registerDataHolderOverrides(IotaHolderItem item, Predicate<ItemStack> hasIota,
        Predicate<ItemStack> isSealed) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.OVERLAY_PRED,
            (stack, level, holder, holderID) -> {
                if (!hasIota.test(stack)) {
                    return 0;
                }
                if (!isSealed.test(stack)) {
                    return 1;
                }
                return 2;
            });
    }

    private static void registerScrollOverrides(ItemScroll scroll) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(scroll, ItemScroll.ANCIENT_PREDICATE,
            (stack, level, holder, holderID) -> NBTHelper.hasString(stack, ItemScroll.TAG_OP_ID) ? 1f : 0f);
    }

    private static void registerPackagedSpellOverrides(ItemPackagedHex item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemPackagedHex.HAS_PATTERNS_PRED,
            (stack, level, holder, holderID) ->
                item.hasHex(stack) ? 1f : 0f
        );
    }

    private static void registerWandOverrides(ItemStaff item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemStaff.FUNNY_LEVEL_PREDICATE,
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
