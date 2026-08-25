package at.petrak.hexcasting.client.model;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/model/BotaniaModelLayers.java
public class HexModelLayers {
    public static final ModelLayerLocation ALTIORA = make("altiora");

    public static final ModelLayerLocation ROBES_0 = make("robes_0");
    public static final ModelLayerLocation ROBES_1 = make("robes_1");
    public static final ModelLayerLocation ROBES_2 = make("robes_2");

    private static ModelLayerLocation make(String name) {
        return make(name, "main");
    }

    private static ModelLayerLocation make(String name, String layer) {
        // Don't add to vanilla's ModelLayers. It seems to only be used for error checking
        // And would be annoying to do under Forge's parallel mod loading
        return new ModelLayerLocation(modLoc(name), layer);
    }

    // moving this stuff into the same file:
    // https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/model/BotaniaLayerDefinitions.java
    public static void init(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {
        consumer.accept(ALTIORA, ElytraModel::createLayer);
        consumer.accept(ROBES_0, HexRobesModel::variant0);
        consumer.accept(ROBES_1, HexRobesModel::variant1);
        consumer.accept(ROBES_2, HexRobesModel::variant2);
    }
}
