package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.be.BlockEntityQuenchedAllayRenderer;
import at.petrak.hexcasting.client.be.BlockEntitySlateRenderer;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.items.storage.*;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class RegisterClientStuff {
    public static List<BakedModel> QUENCHED_ALLAY_VARIANTS = new ArrayList<>();

    public static void init() {
        registerSealableDataHolderOverrides(HexItems.FOCUS,
            stack -> HexItems.FOCUS.readIotaTag(stack) != null,
            ItemFocus::isSealed);
        registerSealableDataHolderOverrides(HexItems.SPELLBOOK,
            stack -> HexItems.SPELLBOOK.readIotaTag(stack) != null,
            ItemSpellbook::isSealed);
        IClientXplatAbstractions.INSTANCE.registerItemProperty(HexItems.THOUGHT_KNOT, ItemThoughtKnot.WRITTEN_PRED,
            (stack, level, holder, holderID) -> {
                if (NBTHelper.contains(stack, ItemThoughtKnot.TAG_DATA)) {
                    return 1;
                } else {
                    return 0;
                }
            });

        registerPackagedSpellOverrides(HexItems.CYPHER);
        registerPackagedSpellOverrides(HexItems.TRINKET);
        registerPackagedSpellOverrides(HexItems.ARTIFACT);

        var x = IClientXplatAbstractions.INSTANCE;
        x.registerItemProperty(HexItems.BATTERY, ItemMediaBattery.MEDIA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (MediaHolderItem) stack.getItem();
                return item.getMediaFullness(stack);
            });
        x.registerItemProperty(HexItems.BATTERY, ItemMediaBattery.MAX_MEDIA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ItemMediaBattery) stack.getItem();
                var max = item.getMaxMedia(stack);
                return (float) Math.sqrt((float) max / MediaConstants.CRYSTAL_UNIT / 10);
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

        x.setRenderLayer(HexBlocks.CONJURED_LIGHT, RenderType.cutout());
        x.setRenderLayer(HexBlocks.CONJURED_BLOCK, RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_DOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_TRAPDOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_BOOKSHELF, RenderType.cutout());
        x.setRenderLayer(HexBlocks.SCONCE, RenderType.cutout());

        x.setRenderLayer(HexBlocks.AMETHYST_EDIFIED_LEAVES, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AVENTURINE_EDIFIED_LEAVES, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.CITRINE_EDIFIED_LEAVES, RenderType.cutoutMipped());

        x.setRenderLayer(HexBlocks.AKASHIC_RECORD, RenderType.translucent());

        x.registerEntityRenderer(HexEntities.WALL_SCROLL, WallScrollRenderer::new);

//        for (var tex : new ResourceLocation[]{
//                PatternTooltipComponent.PRISTINE_BG,
//                PatternTooltipComponent.ANCIENT_BG,
//                PatternTooltipComponent.SLATE_BG
//        }) {
//            Minecraft.getInstance().getTextureManager().bindForSetup(tex);
//        }

        ScryingLensOverlays.addScryingLensStuff();
    }

    public static void registerColorProviders(BiConsumer<ItemColor, Item> itemColorRegistry,
        BiConsumer<BlockColor, Block> blockColorRegistry) {
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.FOCUS::getColor), HexItems.FOCUS);
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.SPELLBOOK::getColor), HexItems.SPELLBOOK);
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.THOUGHT_KNOT::getColor), HexItems.THOUGHT_KNOT);

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
     * 0 = base; 1 = overlay
     */
    public static ItemColor makeIotaStorageColorizer(ToIntFunction<ItemStack> getColor) {
        return (stack, idx) -> {
            if (idx == 1) {
                return getColor.applyAsInt(stack);
            }
            return 0xff_ffffff;
        };
    }

    private static void registerSealableDataHolderOverrides(IotaHolderItem item, Predicate<ItemStack> hasIota,
        Predicate<ItemStack> isSealed) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.OVERLAY_PRED,
            (stack, level, holder, holderID) -> {
                if (!hasIota.test(stack) && !NBTHelper.hasString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
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
                } else {
                    return 0f;
                }
            });
    }

    public static void registerBlockEntityRenderers(@NotNull BlockEntityRendererRegisterererer registerer) {
        registerer.registerBlockEntityRenderer(HexBlockEntities.SLATE_TILE, BlockEntitySlateRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.AKASHIC_BOOKSHELF_TILE,
            BlockEntityAkashicBookshelfRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.QUENCHED_ALLAY_TILE,
            BlockEntityQuenchedAllayRenderer::new);
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegisterererer {
        <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
            BlockEntityRendererProvider<? super T> berp);
    }

    public static void onModelRegister(ResourceManager recMan, Consumer<ResourceLocation> extraModels) {
        for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
            extraModels.accept(modLoc("block/quenched_allay_" + i));
        }
    }

    public static void onModelBake(ModelBakery loader, Map<ResourceLocation, BakedModel> map) {
        for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
            var variantLoc = modLoc("block/quenched_allay_" + i);
            var model = map.get(variantLoc);
            QUENCHED_ALLAY_VARIANTS.add(model);
        }
    }
}
