package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(PoiType.class)
public interface AccessorPoiType {
    @Accessor("matchingStates")
    Set<BlockState> hex$matchingStates();
}
