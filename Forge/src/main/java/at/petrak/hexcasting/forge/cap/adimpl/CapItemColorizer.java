package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CapItemColorizer(ColorizerItem holder,
                               ItemStack stack) implements ADColorizer {
    @Override
    public ColorProvider provideColor(UUID owner) {
        return holder.provideColor(this.stack, owner);
    }
}
