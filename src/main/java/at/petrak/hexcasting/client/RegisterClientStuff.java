package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.client.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.be.BlockEntitySlateRenderer;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.client.particles.ConjureParticle;
import at.petrak.hexcasting.common.blocks.HexBlockEntities;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.*;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell;
import at.petrak.hexcasting.common.particles.HexParticles;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

public class RegisterClientStuff {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            ItemProperties.register(HexItems.FOCUS.get(), ItemFocus.DATATYPE_PRED,
                (stack, level, holder, holderID) -> {
                    var tag = stack.getOrCreateTag();
                    var isSealed = tag.getBoolean(ItemFocus.TAG_SEALED);
                    var baseNum = isSealed ? 100f : 0f;
                    var datum = HexItems.FOCUS.get().readDatumTag(stack);
                    if (datum != null) {
                        var typename = datum.getAllKeys().iterator().next();
                        return baseNum + switch (typename) {
                            case SpellDatum.TAG_ENTITY -> 1f;
                            case SpellDatum.TAG_DOUBLE -> 2f;
                            case SpellDatum.TAG_VEC3 -> 3f;
                            case SpellDatum.TAG_WIDGET -> 4f;
                            case SpellDatum.TAG_LIST -> 5f;
                            case SpellDatum.TAG_PATTERN -> 6f;
                            default -> 0f; // uh oh
                        };
                    } else {
                        return baseNum;
                    }
                });
            ItemProperties.register(HexItems.SPELLBOOK.get(), ItemSpellbook.DATATYPE_PRED,
                (stack, level, holder, holderID) -> {
                    var tag = stack.getOrCreateTag();
                    var datum = HexItems.SPELLBOOK.get().readDatumTag(stack);
                    if (datum != null) {
                        var typename = datum.getAllKeys().iterator().next();
                        return switch (typename) {
                            case SpellDatum.TAG_ENTITY -> 1f;
                            case SpellDatum.TAG_DOUBLE -> 2f;
                            case SpellDatum.TAG_VEC3 -> 3f;
                            case SpellDatum.TAG_WIDGET -> 4f;
                            case SpellDatum.TAG_LIST -> 5f;
                            case SpellDatum.TAG_PATTERN -> 6f;
                            default -> 0f; // uh oh
                        };
                    } else {
                        return 0f;
                    }
                });
            for (ItemPackagedSpell packager : new ItemPackagedSpell[]{
                HexItems.CYPHER.get(),
                HexItems.TRINKET.get(),
                HexItems.ARTIFACT.get(),
            }) {
                ItemProperties.register(packager, ItemPackagedSpell.HAS_PATTERNS_PRED,
                    (stack, level, holder, holderID) ->
                        packager.getPatterns(stack) != null ? 1f : 0f
                );
            }

            ItemProperties.register(HexItems.BATTERY.get(), ItemManaBattery.MANA_PREDICATE,
                (stack, level, holder, holderID) -> {
                    var item = (ManaHolderItem) stack.getItem();
                    return item.getManaFullness(stack);
                });
            ItemProperties.register(HexItems.BATTERY.get(), ItemManaBattery.MAX_MANA_PREDICATE,
                (stack, level, holder, holderID) -> {
                    var item = (ItemManaBattery) stack.getItem();
                    var max = item.getMaxMana(stack);
                    return (float) Math.sqrt((float) max / HexConfig.chargedCrystalManaAmount.get() / 10);
                });

            ItemProperties.register(HexItems.SCROLL.get(), ItemScroll.ANCIENT_PREDICATE,
                (stack, level, holder, holderID) -> stack.getOrCreateTag().contains(ItemScroll.TAG_OP_ID) ? 1f : 0f);

            ItemProperties.register(HexItems.SLATE.get(), ItemSlate.WRITTEN_PRED,
                (stack, level, holder, holderID) -> ItemSlate.hasPattern(stack) ? 1f : 0f);

            HexTooltips.init();
        });

        for (var cutout : new Block[]{
            HexBlocks.CONJURED_LIGHT.get(),
            HexBlocks.CONJURED_BLOCK.get(),
            HexBlocks.AKASHIC_DOOR.get(),
            HexBlocks.AKASHIC_TRAPDOOR.get(),
            HexBlocks.SCONCE.get(),
        }) {
            ItemBlockRenderTypes.setRenderLayer(cutout, RenderType.cutout());
        }

        for (var mipped : new Block[]{
            HexBlocks.AKASHIC_LEAVES1.get(),
            HexBlocks.AKASHIC_LEAVES2.get(),
            HexBlocks.AKASHIC_LEAVES3.get(),
        }) {
            ItemBlockRenderTypes.setRenderLayer(mipped, RenderType.cutoutMipped());
        }

        ItemBlockRenderTypes.setRenderLayer(HexBlocks.AKASHIC_RECORD.get(), RenderType.translucent());

        EntityRenderers.register(HexEntities.WALL_SCROLL.get(), WallScrollRenderer::new);

        addScryingLensStuff();
    }

    private static void addScryingLensStuff() {
        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, lensHand) -> state.getBlock() instanceof BlockAbstractImpetus,
            (state, pos, observer, world, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
                    return beai.getScryingLensOverlay(state, pos, observer, world, lensHand);
                } else {
                    return List.of();
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_BOOKSHELF.get(),
            (state, pos, observer, world, lensHand) -> {
                if (!(world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile)) {
                    return List.of();
                }

                var out = new ArrayList<Pair<ItemStack, Component>>();

                var recordPos = tile.getRecordPos();
                var pattern = tile.getPattern();
                if (recordPos != null && pattern != null) {
                    out.add(new Pair<>(new ItemStack(HexBlocks.AKASHIC_RECORD.get()), new TranslatableComponent(
                        "hexcasting.tooltip.lens.akashic.bookshelf.location",
                        recordPos.toShortString()
                    )));
                    if (world.getBlockEntity(recordPos) instanceof BlockEntityAkashicRecord record) {
                        out.add(new Pair<>(new ItemStack(Items.BOOK), record.getDisplayAt(pattern)));
                    }
                }

                return out;
            });
        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_RECORD.get(),
            ((state, pos, observer, world, lensHand) -> {
                if (!(world.getBlockEntity(pos) instanceof BlockEntityAkashicRecord tile)) {
                    return List.of();
                }

                int count = tile.getCount();

                return List.of(new Pair<>(new ItemStack(HexBlocks.AKASHIC_BOOKSHELF.get()), new TranslatableComponent(
                    "hexcasting.tooltip.lens.akashic.record.count" + (count == 1 ? ".single" : ""),
                    count
                )));
            }));

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REDSTONE_WIRE,
            (state, pos, observer, world, lensHand) -> List.of(
                new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    new TextComponent(String.valueOf(state.getValue(BlockStateProperties.POWER)))
                        .withStyle(ChatFormatting.RED))
            ));

        ScryingLensOverlayRegistry.addDisplayer(Blocks.COMPARATOR,
                (state, pos, observer, world, lensHand) -> {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof ComparatorBlockEntity comparator) {
                        return List.of(
                                new Pair<>(
                                        new ItemStack(Items.REDSTONE),
                                        new TextComponent(String.valueOf(comparator.getOutputSignal()))
                                                .withStyle(ChatFormatting.RED)),
                                new Pair<>(
                                        new ItemStack(Items.REDSTONE_TORCH),
                                        new TextComponent(state.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE ? ">" : "-")
                                                .withStyle(ChatFormatting.RED)));
                    } else
                        return List.of();
                });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REPEATER,
                (state, pos, observer, world, lensHand) -> List.of(
                        new Pair<>(
                                new ItemStack(Items.REDSTONE),
                                new TextComponent(String.valueOf(state.getValue(RepeaterBlock.POWERED) ? 15 : 0))
                                        .withStyle(ChatFormatting.RED)),
                        new Pair<>(
                                new ItemStack(Items.CLOCK),
                                new TextComponent(String.valueOf(state.getValue(RepeaterBlock.DELAY)))
                                        .withStyle(ChatFormatting.YELLOW))));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        // does whatever a particle can
        var particleMan = Minecraft.getInstance().particleEngine;
        particleMan.register(HexParticles.LIGHT_PARTICLE.get(), ConjureParticle.Provider::new);
        particleMan.register(HexParticles.CONJURE_PARTICLE.get(), ConjureParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(HexBlockEntities.SLATE_TILE.get(), BlockEntitySlateRenderer::new);
        evt.registerBlockEntityRenderer(HexBlockEntities.AKASHIC_BOOKSHELF_TILE.get(),
            BlockEntityAkashicBookshelfRenderer::new);
    }
}
