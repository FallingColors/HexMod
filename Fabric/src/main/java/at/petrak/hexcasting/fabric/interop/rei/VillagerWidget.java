package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static at.petrak.hexcasting.client.RenderLib.renderEntity;

public class VillagerWidget extends Widget {
	protected final VillagerIngredient villager;
	private final int x;
	private final int y;

	private Villager displayVillager;

	public VillagerWidget(VillagerIngredient villager, int x, int y) {
		this.villager = villager;
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			VillagerProfession profession = Registry.VILLAGER_PROFESSION.getOptional(villager.profession())
					.orElse(VillagerProfession.TOOLSMITH);
			VillagerType biome = Registry.VILLAGER_TYPE.getOptional(villager.biome())
					.orElse(VillagerType.PLAINS);
			int minLevel = villager.minLevel();
			if (displayVillager == null) {
				displayVillager = new Villager(EntityType.VILLAGER, level);
			}

			displayVillager.setVillagerData(displayVillager.getVillagerData()
					.setProfession(profession)
					.setType(biome)
					.setLevel(minLevel));

			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			renderEntity(poseStack, displayVillager, level, 50 + x, 62.5f + y, ClientTickCounter.total, 20, 0);
		}

		if (isMouseOver(mouseX, mouseY))
			getTooltip(new Point(mouseX, mouseY)).queue();
	}

	@Override
	public boolean containsMouse(double mouseX, double mouseY) {
		double mX = mouseX - x;
		double mY = mouseY - y;
		return 37 <= mX && mX <= 37 + 26 && 19 <= mY && mY <= 19 + 48;
	}

	@NotNull
	@Override
	public Tooltip getTooltip(Point mouse) {
		return Tooltip.create(mouse, villager.getTooltip());
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}
}
