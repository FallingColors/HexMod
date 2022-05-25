package at.petrak.hexcasting.xplat;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

// https://fabricmc.net/wiki/tutorial:tags#existing_common_tags
public interface IXplatTags {
    TagKey<Item> amethystDust();

    TagKey<Item> gems();
}
