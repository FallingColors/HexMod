package at.petrak.hexcasting.common.items.pigment;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import at.petrak.paucal.api.PaucalAPI;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Random;
import java.util.UUID;

public class ItemUUIDPigment extends Item implements PigmentItem {
    public ItemUUIDPigment(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return new MyColorProvider(owner);
    }

    /** Get PaucalAPI - supports instance(), getInstance(), INSTANCE, or get() depending on Paucal version */
    private static Object getPaucalAPI() {
        for (var name : new String[]{"instance", "getInstance", "get"}) {
            try {
                var method = PaucalAPI.class.getMethod(name);
                return method.invoke(null);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return PaucalAPI.class.getField("INSTANCE").get(null);
        } catch (Exception e) {
            return null;
        }
    }

    protected static class MyColorProvider extends ColorProvider {
        private final int[] colors;

        MyColorProvider(UUID owner) {
            Object contributor = null;
            var api = getPaucalAPI();
            if (api != null) {
                try {
                    var m = api.getClass().getMethod("getContributor", UUID.class);
                    contributor = m.invoke(api, owner);
                } catch (Exception ignored) {}
            }
            if (contributor != null) {
                com.google.gson.JsonArray colorList = null;
                try {
                    var otherVals = contributor.getClass().getMethod("otherVals").invoke(contributor);
                    if (otherVals instanceof com.google.gson.JsonObject jo) {
                        colorList = jo.getAsJsonArray("hexcasting:colorizer");
                    }
                } catch (Exception ignored) {}
                if (colorList != null) {
                    var colors = new int[colorList.size()];
                    var ok = true;
                    for (int i = 0; i < colorList.size(); i++) {
                        JsonElement elt = colorList.get(i);
                        if (elt instanceof JsonPrimitive n && n.isNumber()) {
                            colors[i] = n.getAsNumber().intValue() | 0xff_000000;
                        } else {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        this.colors = colors;
                        return;
                    }
                }
            }

            // randomly scrungle the bits
            var rand = new Random(owner.getLeastSignificantBits() ^ owner.getMostSignificantBits());
            var hue1 = rand.nextFloat();
            var saturation1 = rand.nextFloat(0.4f, 0.8f);
            var brightness1 = rand.nextFloat(0.7f, 1.0f);
            var hue2 = rand.nextFloat();
            var saturation2 = rand.nextFloat(0.7f, 1.0f);
            var brightness2 = rand.nextFloat(0.2f, 0.7f);

            var col1 = Color.HSBtoRGB(hue1, saturation1, brightness1);
            var col2 = Color.HSBtoRGB(hue2, saturation2, brightness2);
            this.colors = new int[]{col1, col2};
        }


        @Override
        protected int getRawColor(float time, Vec3 position) {
            return ADPigment.morphBetweenColors(this.colors, new Vec3(0.1, 0.1, 0.1), time / 400, position);
        }
    }
}
