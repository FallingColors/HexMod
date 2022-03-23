package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
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
        var brainsweepings = recman.getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE);
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
                var profession = Objects.requireNonNullElse(this.recipe.villagerIn().profession(),
                    new ResourceLocation("toolsmith"));
                var biome = Objects.requireNonNullElse(this.recipe.villagerIn().biome(),
                    new ResourceLocation("plains"));
                var level = this.recipe.villagerIn().minLevel();
                var iHatePatchouli = String.format(
                    "minecraft:villager{VillagerData:{profession:'%s',type:'%s',level:%d}}",
                    profession, biome, level);
                return IVariable.wrap(iHatePatchouli);
            }
            case "profession" -> {
                var profession = this.recipe.villagerIn().profession();
                if (profession == null) {
                    return IVariable.wrap(I18n.get("hexcasting.tooltip.brainsweep.profession.any"));
                }
                // Villager.java:677
                // jesus christ the things i do for this mod
                var probablyTheKeyForTheName = "entity." + (!"minecraft".equals(
                    profession.getNamespace()) ? profession.getNamespace() + '.' : "") + profession.getPath();
                var out = I18n.get("hexcasting.tooltip.brainsweep.profession", I18n.get(probablyTheKeyForTheName));
                return IVariable.wrap(out);
            }
            case "biome" -> {
                var biome = this.recipe.villagerIn().biome();
                if (biome == null) {
                    return IVariable.wrap(I18n.get("hexcasting.tooltip.brainsweep.biome.any"));
                }
                // i fucking give up
                var definitelyProbablyTheKeyWhyDidIMakeThisUpdateSoBig = "biome.minecraft." + biome.getPath();
                var out = I18n.get("hexcasting.tooltip.brainsweep.biome",
                    I18n.get(definitelyProbablyTheKeyWhyDidIMakeThisUpdateSoBig));
                return IVariable.wrap(out);
            }
            case "minLevel" -> {
                return IVariable.wrap(
                    I18n.get("hexcasting.tooltip.brainsweep.min_level", this.recipe.villagerIn().minLevel()));
            }
            default -> {
                return null;
            }
        }
    }
}
