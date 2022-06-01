package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface AccessorVillager {
	@Invoker("tellWitnessesThatIWasMurdered")
	void hex$tellWitnessesThatIWasMurdered(Entity murderer);

	@Invoker("releaseAllPois")
	void hex$releaseAllPois();
}
