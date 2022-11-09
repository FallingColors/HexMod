package at.petrak.hexcasting.common.items.colorizer;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.paucal.api.PaucalAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ItemUUIDColorizer extends Item implements ColorizerItem {
    public ItemUUIDColorizer(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int color(ItemStack stack, UUID owner, float time, Vec3 position) {
        var contributor = PaucalAPI.instance().getContributor(owner);
        if (contributor != null) {
            List<?> colorList = contributor.get("hexcasting:colorizer");
            if (colorList != null) {
                var colors = new int[colorList.size()];
                var ok = true;
                for (int i = 0; i < colorList.size(); i++) {
                    Object elt = colorList.get(i);
                    if (elt instanceof Number n) {
                        colors[i] = n.intValue() | 0xff_000000;
                    } else {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return ADColorizer.morphBetweenColors(colors, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
                }
            }
        }

        // randomly scrungle the bits
        var rand = new Random(owner.getLeastSignificantBits() ^ owner.getMostSignificantBits());
        var hue = rand.nextFloat();
        var saturation = rand.nextFloat(0.4f, 1.0f);
        var brightness = rand.nextFloat(0.5f, 1.0f);

        return Color.HSBtoRGB(hue, saturation, brightness);
    }
}
