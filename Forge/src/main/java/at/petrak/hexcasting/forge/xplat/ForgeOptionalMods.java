package at.petrak.hexcasting.forge.xplat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Reflection-based access to optional mods (Caelus, Pehkui) to avoid compile-time dependency
 * when libs are not present.
 */
public final class ForgeOptionalMods {

    // Caelus: flight attribute for elytra
    private static Class<?> caelusApiClass;
    private static Method caelusGetInstance;
    private static Method caelusGetFlightAttribute;

    static {
        try {
            caelusApiClass = Class.forName("top.theillusivec4.caelus.api.CaelusApi");
            caelusGetInstance = caelusApiClass.getMethod("getInstance");
            caelusGetFlightAttribute = caelusApiClass.getMethod("getFlightAttribute");
        } catch (Exception ignored) {
        }
    }

    public static Attribute getCaelusFlightAttribute() {
        if (caelusApiClass == null) return null;
        try {
            var instance = caelusGetInstance.invoke(null);
            return (Attribute) caelusGetFlightAttribute.invoke(instance);
        } catch (Exception e) {
            return null;
        }
    }

    // Pehkui: scale get/set
    private static Class<?> scaleTypesClass;
    private static Method getScaleData;
    private static Method getScale;
    private static Method setScale;

    static {
        try {
            scaleTypesClass = Class.forName("virtuoel.pehkui.api.ScaleTypes");
            var baseField = scaleTypesClass.getField("BASE");
            var base = baseField.get(null);
            getScaleData = base.getClass().getMethod("getScaleData", Entity.class);
            var scaleDataType = getScaleData.getReturnType();
            getScale = scaleDataType.getMethod("getScale");
            setScale = scaleDataType.getMethod("setScale", float.class);
        } catch (Exception ignored) {
        }
    }

    public static float getPehkuiScale(Entity entity) {
        if (scaleTypesClass == null) return 1f;
        try {
            var baseField = scaleTypesClass.getField("BASE");
            var base = baseField.get(null);
            var scaleData = getScaleData.invoke(base, entity);
            return (Float) getScale.invoke(scaleData);
        } catch (Exception e) {
            return 1f;
        }
    }

    public static void setPehkuiScale(Entity entity, float scale) {
        if (scaleTypesClass == null) return;
        try {
            var baseField = scaleTypesClass.getField("BASE");
            var base = baseField.get(null);
            var scaleData = getScaleData.invoke(base, entity);
            setScale.invoke(scaleData, scale);
        } catch (Exception ignored) {
        }
    }
}
