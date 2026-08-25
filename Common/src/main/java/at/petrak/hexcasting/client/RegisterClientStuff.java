package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.client.render.GaslightingTracker;
import at.petrak.hexcasting.client.render.ScryingLensOverlays;
import at.petrak.hexcasting.client.render.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.render.be.BlockEntityQuenchedAllayRenderer;
import at.petrak.hexcasting.client.render.be.BlockEntitySlateRenderer;
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
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class RegisterClientStuff {
    public static Map<ResourceLocation, List<BakedModel>> QUENCHED_ALLAY_VARIANTS = new HashMap<>();
    private static final Map<BlockQuenchedAllay, Boolean> QUENCHED_ALLAY_TYPES = Map.of(
            HexBlocks.QUENCHED_ALLAY.get(), false,
            HexBlocks.QUENCHED_ALLAY_TILES.get(), true,
            HexBlocks.QUENCHED_ALLAY_BRICKS.get(), true,
            HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get(), true);

    public static void init() {
        registerSealableDataHolderOverrides(HexItems.FOCUS.get(),
            stack -> stack.has(HexDataComponents.IOTA_HOLDER_IOTA.get()),
            ItemFocus::isSealed);
        registerSealableDataHolderOverrides(HexItems.SPELLBOOK.get(),
            stack -> HexItems.SPELLBOOK.get().readIota(stack) != null,
            ItemSpellbook::isSealed);
        registerVariantOverrides(HexItems.FOCUS.get(), HexItems.FOCUS.get()::getVariant);
        registerVariantOverrides(HexItems.SPELLBOOK.get(), HexItems.SPELLBOOK.get()::getVariant);
        registerVariantOverrides(HexItems.ANCIENT_CYPHER.get(), HexItems.ANCIENT_CYPHER.get()::getVariant);
        registerVariantOverrides(HexItems.CYPHER.get(), HexItems.CYPHER.get()::getVariant);
        registerVariantOverrides(HexItems.TRINKET.get(), HexItems.TRINKET.get()::getVariant);
        registerVariantOverrides(HexItems.ARTIFACT.get(), HexItems.ARTIFACT.get()::getVariant);
        registerVariantOverrides(HexItems.ROBES_HOOD.get(), HexItems.ROBES_HOOD.get()::getVariant);
        registerVariantOverrides(HexItems.ROBES_TUNIC.get(), HexItems.ROBES_TUNIC.get()::getVariant);
        registerVariantOverrides(HexItems.ROBES_LEGS.get(), HexItems.ROBES_LEGS.get()::getVariant);
        registerVariantOverrides(HexItems.ROBES_BOOTS.get(), HexItems.ROBES_BOOTS.get()::getVariant);
        IClientXplatAbstractions.INSTANCE.registerItemProperty(HexItems.THOUGHT_KNOT.get(), ItemThoughtKnot.WRITTEN_PRED,
            (stack, level, holder, holderID) -> {
                if (stack.has(HexDataComponents.IOTA_HOLDER_IOTA.get())) {
                    return 1;
                } else {
                    return 0;
                }
            });

        registerPackagedSpellOverrides(HexItems.ANCIENT_CYPHER.get());
        registerPackagedSpellOverrides(HexItems.CYPHER.get());
        registerPackagedSpellOverrides(HexItems.TRINKET.get());
        registerPackagedSpellOverrides(HexItems.ARTIFACT.get());

        var x = IClientXplatAbstractions.INSTANCE;
        x.registerItemProperty(HexItems.BATTERY.get(), ItemMediaBattery.MEDIA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (MediaHolderItem) stack.getItem();
                return item.getMediaFullness(stack);
            });
        x.registerItemProperty(HexItems.BATTERY.get(), ItemMediaBattery.MAX_MEDIA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ItemMediaBattery) stack.getItem();
                var max = item.getMaxMedia(stack);
                return 1.049658f * (float) Math.log((float) max / MediaConstants.CRYSTAL_UNIT + 9.06152f) - 2.1436f;
            });

        registerScrollOverrides(HexItems.SCROLL_SMOL.get());
        registerScrollOverrides(HexItems.SCROLL_MEDIUM.get());
        registerScrollOverrides(HexItems.SCROLL_LARGE.get());

        x.registerItemProperty(HexItems.SLATE.get(), ItemSlate.WRITTEN_PRED,
            (stack, level, holder, holderID) -> ItemSlate.hasPattern(stack) ? 1f : 0f);

        registerWandOverrides(HexItems.STAFF_OAK.get());
        registerWandOverrides(HexItems.STAFF_BIRCH.get());
        registerWandOverrides(HexItems.STAFF_SPRUCE.get());
        registerWandOverrides(HexItems.STAFF_JUNGLE.get());
        registerWandOverrides(HexItems.STAFF_DARK_OAK.get());
        registerWandOverrides(HexItems.STAFF_ACACIA.get());
        registerWandOverrides(HexItems.STAFF_EDIFIED.get());
        // purposely skip quenched
        registerWandOverrides(HexItems.STAFF_MINDSPLICE.get());

        registerGaslight4(HexItems.STAFF_QUENCHED.get());
        registerGaslight4(HexBlocks.QUENCHED_ALLAY.get().asItem());
        registerGaslight4(HexBlocks.QUENCHED_ALLAY_TILES.get().asItem());
        registerGaslight4(HexBlocks.QUENCHED_ALLAY_BRICKS.get().asItem());
        registerGaslight4(HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get().asItem());
        registerGaslight4(HexItems.QUENCHED_SHARD.get());

        x.setRenderLayer(HexBlocks.CONJURED_LIGHT.get(), RenderType.cutout());
        x.setRenderLayer(HexBlocks.CONJURED_BLOCK.get(), RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_DOOR.get(), RenderType.cutout());
        x.setRenderLayer(HexBlocks.EDIFIED_TRAPDOOR.get(), RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_BOOKSHELF.get(), RenderType.cutout());
        x.setRenderLayer(HexBlocks.SCONCE.get(), RenderType.cutout());

        x.setRenderLayer(HexBlocks.AMETHYST_EDIFIED_LEAVES.get(), RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AVENTURINE_EDIFIED_LEAVES.get(), RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.CITRINE_EDIFIED_LEAVES.get(), RenderType.cutoutMipped());

        x.setRenderLayer(HexBlocks.AKASHIC_RECORD.get(), RenderType.translucent());
        x.setRenderLayer(HexBlocks.QUENCHED_ALLAY.get(), RenderType.translucent());

        x.registerEntityRenderer(HexEntities.WALL_SCROLL.get(), WallScrollRenderer::new);

//        for (var tex : ResourceLocation.fromNamespaceAndPath[]{
//                PatternTooltipComponent.PRISTINE_BG,
//                PatternTooltipComponent.ANCIENT_BG,
//                PatternTooltipComponent.SLATE_BG
//        }) {
//            Minecraft.getInstance().getTextureManager().bindForSetup(tex);
//        }

        ScryingLensOverlays.addScryingLensStuff();
    }

    private static void registerGaslight4(Item item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item,
            GaslightingTracker.GASLIGHTING_PRED, (stack, level, holder, holderID) ->
                Math.abs(GaslightingTracker.getGaslightingAmount() % 4));
    }

    public static void registerColorProviders(BiConsumer<ItemColor, Item> itemColorRegistry,
        BiConsumer<BlockColor, Block> blockColorRegistry) {
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.FOCUS.get()::getColor), HexItems.FOCUS.get());
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.SPELLBOOK.get()::getColor), HexItems.SPELLBOOK.get());
        itemColorRegistry.accept(makeIotaStorageColorizer(HexItems.THOUGHT_KNOT.get()::getColor), HexItems.THOUGHT_KNOT.get());

        blockColorRegistry.accept((bs, level, pos, idx) -> {
            if (!bs.getValue(BlockAkashicBookshelf.HAS_BOOKS) || level == null || pos == null) {
                return 0xff_ffffff;
            }
            var tile = level.getBlockEntity(pos);
            if (!(tile instanceof BlockEntityAkashicBookshelf beas)) {
                // this gets called for particles for some irritating reason
                return 0xff_ffffff;
            }
            var iota = beas.getIota();
            if (iota == null) {
                return 0xff_ffffff;
            }
            return iota.getType().color();
        }, HexBlocks.AKASHIC_BOOKSHELF.get());
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
                if (!hasIota.test(stack) && !stack.has(HexDataComponents.VISUAL_OVERRIDE.get())) {
                    return 0;
                }
                if (!isSealed.test(stack)) {
                    return 1;
                }
                return 2;
            });
    }

    private static void registerVariantOverrides(VariantItem item, Function<ItemStack, Integer> variant) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.VARIANT_PRED,
                (stack, level, holder, holderID) -> variant.apply(stack));
    }

    private static void registerScrollOverrides(ItemScroll scroll) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(scroll, ItemScroll.ANCIENT_PREDICATE,
            (stack, level, holder, holderID) -> stack.has(HexDataComponents.ACTION.get()) ? 1f : 0f);
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
                if (!stack.has(DataComponents.CUSTOM_NAME)) {
                    return 0;
                }
                var name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (name.contains("old")) {
                    return 1f;
                } else if (name.contains("cherry")) {
                    return 2f;
                } else {
                    return 0f;
                }
            });
    }

    public static void registerBlockEntityRenderers(@NotNull BlockEntityRendererRegisterererer registerer) {
        registerer.registerBlockEntityRenderer(HexBlockEntities.SLATE_TILE.get(), BlockEntitySlateRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.AKASHIC_BOOKSHELF_TILE.get(),
            BlockEntityAkashicBookshelfRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.QUENCHED_ALLAY_TILE.get(),
            BlockEntityQuenchedAllayRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.QUENCHED_ALLAY_TILES_TILE.get(),
                BlockEntityQuenchedAllayRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.QUENCHED_ALLAY_BRICKS_TILE.get(),
                BlockEntityQuenchedAllayRenderer::new);
        registerer.registerBlockEntityRenderer(HexBlockEntities.QUENCHED_ALLAY_BRICKS_SMALL_TILE.get(),
                BlockEntityQuenchedAllayRenderer::new);
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegisterererer {
        <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
            BlockEntityRendererProvider<? super T> berp);
    }

    public static void onModelRegister(ResourceManager recMan, Consumer<ModelResourceLocation> extraModels) {
        for (var type : QUENCHED_ALLAY_TYPES.entrySet()) {
            var blockLoc = BuiltInRegistries.BLOCK.getKey(type.getKey());
            var locStart = "block/";
            if (type.getValue())
                locStart += "deco/";

            for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
                extraModels.accept(new ModelResourceLocation(modLoc( locStart + blockLoc.getPath() + "_" + i), IClientXplatAbstractions.INSTANCE.getModelLocVariant()));
            }
        }
    }

    @FunctionalInterface
    public interface FabricModelContext {
        void add(ResourceLocation id);
    }

    public static void onModelRegister(FabricModelContext context) {
        for (var type : QUENCHED_ALLAY_TYPES.entrySet()) {
            var blockLoc = BuiltInRegistries.BLOCK.getKey(type.getKey());
            var locStart = "block/";
            if (type.getValue())
                locStart += "deco/";

            for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
                context.add(modLoc( locStart + blockLoc.getPath() + "_" + i));
            }
        }
    }

    public static void onModelBake(ModelBakery loader, Map<ModelResourceLocation, BakedModel> map) {
        for (var type : QUENCHED_ALLAY_TYPES.entrySet()) {
            var blockLoc = BuiltInRegistries.BLOCK.getKey(type.getKey());
            var locStart = "block/";
            if (type.getValue())
                locStart += "deco/";

            var list = new ArrayList<BakedModel>();
            for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
                var variantLoc = new ModelResourceLocation(modLoc(locStart + blockLoc.getPath() + "_" + i), IClientXplatAbstractions.INSTANCE.getModelLocVariant());
                var model = map.get(variantLoc);
                list.add(model);
            }
            QUENCHED_ALLAY_VARIANTS.put(blockLoc, list);
        }
    }
}
