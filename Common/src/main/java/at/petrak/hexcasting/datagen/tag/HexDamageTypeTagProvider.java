package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.common.lib.HexDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class HexDamageTypeTagProvider extends DamageTypeTagsProvider {
    public HexDamageTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        add(HexDamageTypes.OVERCAST,
            DamageTypeTags.BYPASSES_ARMOR,
            DamageTypeTags.BYPASSES_EFFECTS,
            DamageTypeTags.BYPASSES_SHIELD
        );
    }

    @SafeVarargs
    private void add(ResourceKey<DamageType> damageType, TagKey<DamageType>... tags) {
        for (var tag : tags) {
            this.tag(tag).add(damageType);
        }
    }
}
