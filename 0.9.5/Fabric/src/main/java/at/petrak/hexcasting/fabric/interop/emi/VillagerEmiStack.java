package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.client.shader.FakeBufferSource;
import at.petrak.hexcasting.client.shader.HexRenderTypes;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import at.petrak.hexcasting.mixin.accessor.AccessorPoiType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.client.RenderLib.renderEntity;

public class VillagerEmiStack extends EmiStack {
	private final VillagerEntry entry;
	public final VillagerIngredient ingredient;
	public final boolean mindless;

	private final ResourceLocation id;

	public VillagerEmiStack(VillagerIngredient villager) {
		this(villager, false);
	}

	public VillagerEmiStack(VillagerIngredient villager, boolean mindless) {
		this(villager, mindless, 1);
	}

	public VillagerEmiStack(VillagerIngredient villager, boolean mindless, long amount) {
		entry = new VillagerEntry(new VillagerVariant(villager, mindless));
		this.ingredient = villager;
		this.mindless = mindless;
		this.amount = amount;
		// This is so scuffed
		this.id = modLoc((Objects.toString(villager.profession()) + villager.minLevel() + mindless)
				.replace(':', '-'));
	}

	public static EmiIngredient atLevelOrHigher(VillagerIngredient ingredient, boolean remainder) {
		if (ingredient.profession() == null) {
			return EmiIngredient.of(Registry.VILLAGER_PROFESSION.stream()
					.filter(it -> !((AccessorPoiType) it.getJobPoiType()).hex$matchingStates().isEmpty())
					.map(it -> atLevelOrHigher(new VillagerIngredient(Registry.VILLAGER_PROFESSION.getKey(it),
							ingredient.biome(), ingredient.minLevel()), true))
					.toList());
		}

		VillagerEmiStack stack = new VillagerEmiStack(ingredient).orHigher(true);
		if (remainder) {
			stack.setRemainder(new VillagerEmiStack(ingredient, true));
		}
		return stack;
	}

	private boolean orHigher = false;

	public VillagerEmiStack orHigher(boolean orHigher) {
		this.orHigher = orHigher;
		return this;
	}

	@Override
	public EmiStack copy() {
		VillagerEmiStack e = new VillagerEmiStack(ingredient, mindless, amount);
		e.orHigher(orHigher).setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return amount == 0;
	}

	@Override
	public CompoundTag getNbt() {
		return null;
	}

	@Override
	public Object getKey() {
		return id;
	}

	@Override
	public Entry<?> getEntry() {
		return entry;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public List<Component> getTooltipText() {
		Minecraft mc = Minecraft.getInstance();
		boolean advanced = mc.options.advancedItemTooltips;

		if (mindless) {
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.product"));

			if (advanced) {
				if (ingredient.biome() != null) {
					tooltip.add(new TextComponent(ingredient.biome().toString()).withStyle(ChatFormatting.DARK_GRAY));
				}

				ResourceLocation displayId = Objects.requireNonNullElseGet(ingredient.profession(), () -> Registry.ENTITY_TYPE.getKey(EntityType.VILLAGER));
				tooltip.add(new TextComponent(displayId.toString()).withStyle(ChatFormatting.DARK_GRAY));
			}

			tooltip.add(ingredient.getModNameComponent());
			return tooltip;
		}

		return ingredient.getTooltip(advanced, orHigher);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = getTooltipText().stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create)
				.collect(Collectors.toList());
		if (!getRemainder().isEmpty()) {
			list.add(EmiTooltipComponents.getRemainderTooltipComponent(this));
		}
		return list;
	}

	@Override
	public Component getName() {
		return ingredient.name();
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, float delta, int flags) {
		if ((flags & RENDER_ICON) != 0) {
			Minecraft mc = Minecraft.getInstance();
			ClientLevel level = mc.level;
			if (level != null) {
				Villager villager = RenderLib.prepareVillagerForRendering(ingredient, level);

				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				renderEntity(poseStack, villager, level, x + 8, y + 16, ClientTickCounter.getTotal(), 8, 0,
						mindless ? (it) -> new FakeBufferSource(it, HexRenderTypes::getGrayscaleLayer) : it -> it);
			}
		}


		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, poseStack, x, y);
		}
	}

	public static class VillagerEntry extends EmiStack.Entry<VillagerVariant> {

		public VillagerEntry(VillagerVariant variant) {
			super(variant);
		}

		@Override
		public Class<? extends VillagerVariant> getType() {
			return VillagerVariant.class;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof VillagerEntry e)) {
				return false;
			}

			VillagerVariant self = getValue();
			VillagerVariant other = e.getValue();

			ResourceLocation selfBiome = self.ingredient().biome();
			ResourceLocation otherBiome = other.ingredient().biome();
			if (selfBiome != null && otherBiome != null && !selfBiome.equals(otherBiome)) {
				return false;
			}

			ResourceLocation selfProfession = self.ingredient().profession();
			ResourceLocation otherProfession = other.ingredient().profession();
			if (selfProfession != null && otherProfession != null && !selfProfession.equals(otherProfession)) {
				return false;
			}

			return self.ingredient().minLevel() == other.ingredient().minLevel() && self.mindless() == other.mindless();
		}
	}

}
