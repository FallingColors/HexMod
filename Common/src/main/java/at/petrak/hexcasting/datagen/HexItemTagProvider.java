package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatTags;
import at.petrak.paucal.api.datagen.PaucalItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class HexItemTagProvider extends PaucalItemTagProvider {
    private final IXplatTags xtags;

    public HexItemTagProvider(DataGenerator pGenerator, TagsProvider<Block> pBlockTagsProvider, IXplatTags xtags) {
        super(pGenerator, pBlockTagsProvider);
        this.xtags = xtags;
    }

    @Override
    protected void addTags() {
        tag(xtags.gems()).add(HexItems.CHARGED_AMETHYST);
        tag(xtags.amethystDust()).add(HexItems.AMETHYST_DUST);

        tag(HexTags.Items.STAVES).add(HexItems.STAFF_EDIFIED,
            HexItems.STAFF_OAK, HexItems.STAFF_SPRUCE, HexItems.STAFF_BIRCH,
            HexItems.STAFF_JUNGLE, HexItems.STAFF_ACACIA, HexItems.STAFF_DARK_OAK,
            HexItems.STAFF_CRIMSON, HexItems.STAFF_WARPED);

        tag(HexTags.Items.PHIAL_BASE).add(Items.GLASS_BOTTLE);
        tag(HexTags.Items.GRANTS_ROOT_ADVANCEMENT).add(HexItems.AMETHYST_DUST, Items.AMETHYST_SHARD,
            HexItems.CHARGED_AMETHYST);

        this.copy(HexTags.Blocks.EDIFIED_LOGS, HexTags.Items.EDIFIED_LOGS);
        this.copy(HexTags.Blocks.EDIFIED_PLANKS, HexTags.Items.EDIFIED_PLANKS);
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
}
