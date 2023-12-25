package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexActionTagProvider extends TagsProvider<ActionRegistryEntry> {
    public HexActionTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, IXplatAbstractions.INSTANCE.getActionRegistry().key(), provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // In-game almost all great spells are always per-world
        for (var normalGreat : new String[]{
            "lightning", "flight", "create_lava", "teleport/great", "sentinel/create/great",
            "dispel_rain", "summon_rain", "brainsweep", "craft/battery",
            "potion/regeneration", "potion/night_vision", "potion/absorption", "potion/haste", "potion/strength"
        }) {
            var loc = modLoc(normalGreat);
            var key = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), loc);
            tag(ersatzActionTag(HexTags.Actions.REQUIRES_ENLIGHTENMENT)).add(key);
            tag(ersatzActionTag(HexTags.Actions.CAN_START_ENLIGHTEN)).add(key);
            tag(ersatzActionTag(HexTags.Actions.PER_WORLD_PATTERN)).add(key);
        }
        // deciding that akashic write can be just a normal spell (as a treat)
    }

    private static TagKey<ActionRegistryEntry> ersatzActionTag(TagKey<ActionRegistryEntry> real) {
        if (IXplatAbstractions.INSTANCE.platform() == Platform.FABRIC) {
            // Vanilla (and Fabric) has a bug here where it *writes* hexcasting action tags to `.../tags/action`
            // instead of `.../tags/hexcasting/action`.
            // So we pull this bullshit
            var fakeKey = ResourceKey.<ActionRegistryEntry>createRegistryKey(
                new ResourceLocation("foobar", "hexcasting/tags/action"));
            return TagKey.create(fakeKey, real.location());
        } else {
            return real;
        }
    }
}
