package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		List<Component> tooltip = new ArrayList<>(3);
		var profession = villager.profession();
		if (profession == null) {
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.profession.any"));
		} else {
			var professionKey = "entity.minecraft.villager." + profession.getPath();
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.profession",
					new TranslatableComponent(professionKey)));
		}
		var biome = villager.biome();
		if (biome == null) {
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.biome.any"));
		} else {
			var biomeKey = "biome.minecraft." + biome.getPath();
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.biome",
					new TranslatableComponent(biomeKey)));
		}

		var minLevel = villager.minLevel();
		if (minLevel == 5)
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.level",
					new TranslatableComponent("merchant.level." + minLevel)));
		else
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.min_level",
					new TranslatableComponent("merchant.level." + minLevel)));

		return Tooltip.create(mouse, tooltip);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}

	private static void renderEntity(PoseStack ms, Entity entity, Level world, float x, float y, float rotation,
									 float renderScale, float offset) {
		entity.level = world;
		ms.pushPose();
		ms.translate(x, y, 50.0D);
		ms.scale(renderScale, renderScale, renderScale);
		ms.translate(0.0D, offset, 0.0D);
		ms.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		ms.mulPose(Vector3f.YP.rotationDegrees(rotation));
		EntityRenderDispatcher erd = Minecraft.getInstance().getEntityRenderDispatcher();
		MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
		erd.setRenderShadow(false);
		erd.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, ms, immediate, 15728880);
		erd.setRenderShadow(true);
		immediate.endBatch();
		ms.popPose();
	}
}
