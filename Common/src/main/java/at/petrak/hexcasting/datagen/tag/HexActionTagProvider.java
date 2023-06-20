package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexActionTagProvider extends TagsProvider<ActionRegistryEntry> {
    public HexActionTagProvider(DataGenerator generator) {
        super(generator, IXplatAbstractions.INSTANCE.getActionRegistry());
    }

    @Override
    protected void addTags() {
        // In-game almost all great spells are always per-world
        for (var normalGreat : new String[]{
            "lightning", "flight", "create_lava", "teleport/great", "sentinel/create/great",
            "dispel_rain", "summon_rain", "brainsweep", "craft/battery",
            "potion/regeneration", "potion/night_vision", "potion/absorption", "potion/haste", "potion/strength"
        }) {
            var loc = modLoc(normalGreat);
            var key = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), loc);
            tag(HexTags.Actions.REQUIRES_ENLIGHTENMENT).add(key);
            tag(HexTags.Actions.CAN_START_ENLIGHTEN).add(key);
            tag(HexTags.Actions.PER_WORLD_PATTERN).add(key);
        }
        // deciding that akashic write can be just a normal spell (as a treat)
    }
}
