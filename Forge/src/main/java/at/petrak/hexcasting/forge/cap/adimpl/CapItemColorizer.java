package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.api.item.ColorizerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record CapItemColorizer(ColorizerItem holder,
                               ItemStack stack) implements ADColorizer {
    @Override
    public int color(UUID owner, float time, Vec3 position) {
        return holder.color(stack, owner, time, position);
    }
}
