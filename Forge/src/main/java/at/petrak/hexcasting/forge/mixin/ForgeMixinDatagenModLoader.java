package at.petrak.hexcasting.forge.mixin;

import net.minecraftforge.data.loading.DatagenModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@Mixin(DatagenModLoader.class)
public abstract class ForgeMixinDatagenModLoader {

    /**
     * Make it so non-vanilla registries can actually be tagged.
     */
    @Inject(method = "begin", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/ModLoader;gatherAndInitializeMods(Lnet/minecraftforge/fml/ModWorkManager$DrivenExecutor;Ljava/util/concurrent/Executor;Ljava/lang/Runnable;)V",
            shift = At.Shift.AFTER
    ))
    private static void begin(final Set<String> mods, final Path path, final Collection<Path> inputs, Collection<Path> existingPacks,
                              Set<String> existingMods, final boolean serverGenerators, final boolean clientGenerators, final boolean devToolGenerators, final boolean reportsGenerator,
                              final boolean structureValidator, final boolean flat, final String assetIndex, final File assetsDir, CallbackInfo ci) {

        if (!mods.contains("forge")) {
            // If we aren't generating data for forge, automatically add forge as an existing so mods can access forge's data
            existingMods.add("forge");
        }
    }
}
