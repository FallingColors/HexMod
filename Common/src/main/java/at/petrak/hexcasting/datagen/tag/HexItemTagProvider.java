package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class HexItemTagProvider extends ItemTagsProvider {
    private final IXplatTags xtags;

    public HexItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<Block>> pBlockTagsProvider, IXplatTags xtags) {
        super(output, lookup, pBlockTagsProvider);
        this.xtags = xtags;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        add(tag(xtags.gems()),
            HexItems.CHARGED_AMETHYST.get());
        add(tag(xtags.amethystDust()),
            HexItems.AMETHYST_DUST.get());

        add(tag(HexTags.Items.STAVES),
            HexItems.STAFF_EDIFIED.get(),
            HexItems.STAFF_OAK.get(), HexItems.STAFF_SPRUCE.get(), HexItems.STAFF_BIRCH.get(),
            HexItems.STAFF_JUNGLE.get(), HexItems.STAFF_ACACIA.get(), HexItems.STAFF_DARK_OAK.get(),
            HexItems.STAFF_CRIMSON.get(), HexItems.STAFF_WARPED.get(), HexItems.STAFF_MANGROVE.get(),
            HexItems.STAFF_CHERRY.get(),HexItems.STAFF_BAMBOO.get(),
            HexItems.STAFF_QUENCHED.get(), HexItems.STAFF_MINDSPLICE.get());

        add(tag(HexTags.Items.PHIAL_BASE),
            Items.GLASS_BOTTLE);
        add(tag(HexTags.Items.GRANTS_ROOT_ADVANCEMENT),
            HexItems.AMETHYST_DUST.get(), Items.AMETHYST_SHARD,
            HexItems.CHARGED_AMETHYST.get(), HexItems.CREATIVE_UNLOCKER.get());
        add(tag(HexTags.Items.SEAL_MATERIALS),
            Items.HONEYCOMB);

        add(tag(HexTags.Items.PHIAL_RAW_INGREDIENTS),
                HexItems.AMETHYST_DUST.get(), Items.AMETHYST_SHARD,
                HexItems.CHARGED_AMETHYST.get(), HexItems.QUENCHED_SHARD.get(),
                HexBlocks.QUENCHED_ALLAY.get().asItem());

        this.copy(HexTags.Blocks.EDIFIED_LOGS, HexTags.Items.EDIFIED_LOGS);
        this.copy(HexTags.Blocks.EDIFIED_PLANKS, HexTags.Items.EDIFIED_PLANKS);
        this.copy(HexTags.Blocks.IMPETI, HexTags.Items.IMPETI);
        this.copy(HexTags.Blocks.DIRECTRICES, HexTags.Items.DIRECTRICES);
        this.copy(HexTags.Blocks.MINDFLAYED_CIRCLE_COMPONENTS, HexTags.Items.MINDFLAYED_CIRCLE_COMPONENTS);
        this.copy(HexTags.Blocks.SLATE_BLOCKS, HexTags.Items.SLATE_BLOCKS);
        this.copy(HexTags.Blocks.AMETHYST_BLOCKS, HexTags.Items.AMETHYST_BLOCKS);
        this.copy(HexTags.Blocks.QUENCHED_ALLAY_BLOCKS, HexTags.Items.QUENCHED_ALLAY_BLOCKS);
        this.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        this.copy(BlockTags.LOGS, ItemTags.LOGS);
        this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
        this.copy(BlockTags.SLABS, ItemTags.SLABS);
        this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        this.copy(BlockTags.DOORS, ItemTags.DOORS);
        this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        this.copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
        // Apparently, there's no "Pressure Plates" item tag.
        this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        this.copy(BlockTags.BUTTONS, ItemTags.BUTTONS);
        this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
    }

    void add(TagAppender<Item> appender, Item... items) {
        for (Item item : items) {
            appender.add(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow());
        }
    }
}
