package at.petrak.hexcasting.forge.mixin;

import net.minecraft.data.tags.TagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagsProvider.class)
public interface ForgeAccessorTagsProvider {
    @Accessor("existingFileHelper")
    @Mutable
    void hex$setExistingFileHelper(ExistingFileHelper h);
}
