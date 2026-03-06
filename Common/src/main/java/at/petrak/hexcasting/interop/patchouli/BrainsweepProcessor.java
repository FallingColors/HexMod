package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;
import java.util.Arrays;
import java.util.List;

public class BrainsweepProcessor implements IComponentProcessor {
	private BrainsweepRecipe recipe;
	@Nullable
	private String exampleEntityString;

	@Override
	public void setup(Level level, IVariableProvider vars) {
		var id = ResourceLocation.parse(vars.get("recipe", level.registryAccess()).asString());

		var recman = level.getRecipeManager();
		var brainsweepings = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE);
		for (var holder : brainsweepings) {
			if (holder.id().equals(id)) {
				this.recipe = holder.value();
				break;
			}
		}
	}

	@Override
	public IVariable process(Level level, String key) {
		var provider = level.registryAccess();
		// Always provide entity fallback so Patchouli never gets empty string (causes "Unknown entity id")
		if ("entity".equals(key)) {
			if (this.recipe != null && this.exampleEntityString == null) {
				try {
					var clientLevel = Minecraft.getInstance().level;
					var entity = clientLevel != null
						? this.recipe.entityIn().exampleEntity(clientLevel)
						: null;
					if (entity != null) {
						this.exampleEntityString = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
					}
				} catch (Exception ignored) {
					// Fall through
				}
				if (this.exampleEntityString == null) {
					this.exampleEntityString = "minecraft:villager";
				}
			}
			return IVariable.wrap(this.exampleEntityString != null ? this.exampleEntityString : "minecraft:villager", provider);
		}
		if (this.recipe == null) {
			return null;
		}

		switch (key) {
			case "header" -> {
				return IVariable.from(this.recipe.result().getBlock().getName(), provider);
			}
			case "input" -> {
				var inputStacks = this.recipe.blockIn().getDisplayedStacks();
				return IVariable.from(inputStacks.toArray(new ItemStack[0]), provider);
			}
			case "result" -> {
				return IVariable.from(new ItemStack(this.recipe.result().getBlock()), provider);
			}

			case "entityTooltip" -> {
				Minecraft mc = Minecraft.getInstance();
				return IVariable.wrapList(this.recipe.entityIn()
					.getTooltip(mc.options.advancedItemTooltips)
					.stream()
					.map(c -> IVariable.from(c, provider))
					.toList(), provider);
			}
			case "mediaCost" -> {
				record ItemCost(Item item, int cost) {
					public boolean dividesEvenly (int dividend) {
                        return dividend % cost == 0;
                    }
				}
				ItemCost[] costs  = {
						new ItemCost(HexItems.AMETHYST_DUST, (int)MediaConstants.DUST_UNIT),
						new ItemCost(Items.AMETHYST_SHARD, (int)MediaConstants.SHARD_UNIT),
						new ItemCost(HexItems.CHARGED_AMETHYST, (int)MediaConstants.CRYSTAL_UNIT),
				};

				List<IVariable> validItemStacks = Arrays.stream(costs)
						.filter(itemCost -> itemCost.dividesEvenly((int)this.recipe.mediaCost()))
						.map(validItemCost -> new ItemStack(validItemCost.item, (int) this.recipe.mediaCost() / validItemCost.cost))
						.map(stack -> IVariable.from(stack, provider))
						.toList();

				if (!validItemStacks.isEmpty()) {
					return IVariable.wrapList(validItemStacks, provider);
				}
				// fallback: display in terms of dust
				return IVariable.from(new ItemStack(HexItems.AMETHYST_DUST, (int) (this.recipe.mediaCost() / MediaConstants.DUST_UNIT)), provider);
			}
			default -> {
				return null;
			}
		}
	}
}
