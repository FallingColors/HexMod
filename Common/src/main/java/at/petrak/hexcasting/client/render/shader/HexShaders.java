package at.petrak.hexcasting.client.render.shader;

import at.petrak.hexcasting.client.render.overlays.EigengrauOverlay;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.function.Consumer;

// https://github.com/VazkiiMods/Botania/blob/3a43accc2fbc439c9f2f00a698f8f8ad017503db/Common/src/main/java/vazkii/botania/client/core/helper/CoreShaders.java
public class HexShaders {
    public static ShaderInstance GRAYSCALE;

    /**
     * Spins the BZ simulation in the background
     */
    public static ShaderInstance EIGENGRAU_BZ;

    public static void init(ResourceProvider resourceProvider,
        Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registrations) throws IOException {
        registrations.accept(Pair.of(
            new ShaderInstance(resourceProvider, "hexcasting__grayscale", DefaultVertexFormat.NEW_ENTITY),
            inst -> GRAYSCALE = inst
        ));
        registrations.accept(Pair.of(
            new ShaderInstance(resourceProvider, "hexcasting__eigengrau_bz", DefaultVertexFormat.POSITION_TEX),
            inst -> EIGENGRAU_BZ = inst
        ));

        // Good a time as any I guess?
        EigengrauOverlay.initTextures();
    }

    public static ShaderInstance grayscale() {
        return GRAYSCALE;
    }
}
