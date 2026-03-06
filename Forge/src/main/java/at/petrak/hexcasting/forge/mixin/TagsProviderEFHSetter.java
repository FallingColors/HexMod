package at.petrak.hexcasting.forge.mixin;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Interface for injecting ExistingFileHelper into TagsProvider subclasses that don't receive it
 * via constructor (e.g. Paucal-based providers). Used by ForgeMixinTagsProvider.
 */
public interface TagsProviderEFHSetter {
    void setEFH(ExistingFileHelper efh);
}
