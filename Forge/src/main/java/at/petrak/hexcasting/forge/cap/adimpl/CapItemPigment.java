package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CapItemPigment(PigmentItem holder,
                             ItemStack stack) implements ADPigment {
    @Override
    public ColorProvider provideColor(UUID owner) {
        return holder.provideColor(this.stack, owner);
    }
}
