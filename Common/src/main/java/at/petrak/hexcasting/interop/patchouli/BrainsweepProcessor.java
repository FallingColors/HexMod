package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.Objects;

public class BrainsweepProcessor implements IComponentProcessor {
    private BrainsweepRecipe recipe;

    @Override
    public void setup(IVariableProvider vars) {
        var id = new ResourceLocation(vars.get("recipe").asString());

        var recman = Minecraft.getInstance().level.getRecipeManager();
        var brainsweepings = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE);
        for (var poisonApples : brainsweepings) {
            if (poisonApples.getId().equals(id)) {
                this.recipe = poisonApples;
                break;
            }
        }
    }

    @Override
    public IVariable process(String key) {
        if (this.recipe == null) {
            return null;
        }

        switch (key) {
            case "header" -> {
                return IVariable.from(this.recipe.result().getBlock().getName());
            }
            case "input" -> {
                var inputStacks = this.recipe.blockIn().getDisplayedStacks();
                return IVariable.from(inputStacks.toArray(new ItemStack[0]));
            }
            case "result" -> {
                return IVariable.from(new ItemStack(this.recipe.result().getBlock()));
            }

            case "entity" -> {
                var profession = Objects.requireNonNullElse(this.recipe.entityIn().profession(),
                    new ResourceLocation("toolsmith"));
                var biome = Objects.requireNonNullElse(this.recipe.entityIn().biome(),
                    new ResourceLocation("plains"));
                var level = this.recipe.entityIn().minLevel();
                var iHatePatchouli = String.format(
                    "minecraft:villager{VillagerData:{profession:'%s',type:'%s',level:%d}}",
                    profession, biome, level);
                return IVariable.wrap(iHatePatchouli);
            }
            case "entityTooltip" -> {
                Minecraft mc = Minecraft.getInstance();
                return IVariable.wrapList(this.recipe.entityIn()
                    .getTooltip(mc.options.advancedItemTooltips)
                    .stream()
                    .map(IVariable::from)
                    .toList());
            }
            default -> {
                return null;
            }
        }
    }
}
