package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.forge.datagen.TagsProviderEFHSetter;
import net.minecraft.data.tags.TagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagsProvider.class)
public abstract class ForgeMixinTagsProvider implements TagsProviderEFHSetter {
    @Final
    @Shadow(remap = false)
    protected ExistingFileHelper existingFileHelper;

    private ExistingFileHelper actualFileHelper = null;

    @Override
    public void setEFH(ExistingFileHelper efh) {
        actualFileHelper = efh;
    }

    @Redirect(method = "missing(Lnet/minecraft/tags/TagEntry;)Z", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/data/tags/TagsProvider;existingFileHelper:Lnet/minecraftforge/common/data/ExistingFileHelper;",
            opcode = Opcodes.GETFIELD),
            remap = false)
    private ExistingFileHelper hex$missingRedirect(TagsProvider instance) {
        if (actualFileHelper == null)
            return existingFileHelper;
        return actualFileHelper;
    }

    @Redirect(method = "lambda$getOrCreateRawBuilder$9(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/tags/TagBuilder;", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/data/tags/TagsProvider;existingFileHelper:Lnet/minecraftforge/common/data/ExistingFileHelper;",
            opcode = Opcodes.GETFIELD),
            remap = false)
    private ExistingFileHelper hex$getOrCreateRawBuilderRedirect(TagsProvider instance) {
        if (actualFileHelper == null)
            return existingFileHelper;
        return actualFileHelper;
    }
}
