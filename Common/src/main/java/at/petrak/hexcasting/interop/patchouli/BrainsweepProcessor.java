package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
    @Nullable private String exampleEntityString;

    @Override
    public void setup(Level level, IVariableProvider vars) {
        var id = new ResourceLocation(vars.get("recipe").asString());

        var recman = level.getRecipeManager();
        var brainsweepings = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE);
        for (var poisonApples : brainsweepings) {
            if (poisonApples.getId().equals(id)) {
                this.recipe = poisonApples;
                break;
            }
        }
    }

    @Override
    public IVariable process(Level level, String key) {
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
                if (this.exampleEntityString == null) {
                    var entity =
                            this.recipe.entityIn().exampleEntity(Minecraft.getInstance().level);
                    if (entity == null) {
                        // oh dear
                        return null;
                    }
                    var bob = new StringBuilder();
                    bob.append(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));

                    var tag = new CompoundTag();
                    entity.save(tag);
                    bob.append(tag.toString());
                    this.exampleEntityString = bob.toString();
                }

                return IVariable.wrap(this.exampleEntityString);
            }
            case "entityTooltip" -> {
                Minecraft mc = Minecraft.getInstance();
                return IVariable.wrapList(
                        this.recipe.entityIn().getTooltip(mc.options.advancedItemTooltips).stream()
                                .map(IVariable::from)
                                .toList());
            }
            case "mediaCost" -> {
                record ItemCost(Item item, int cost) {
                    public boolean dividesEvenly(int dividend) {
                        return dividend % cost == 0;
                    }
                }
                ItemCost[] costs = {
                    new ItemCost(HexItems.AMETHYST_DUST, (int) MediaConstants.DUST_UNIT),
                    new ItemCost(Items.AMETHYST_SHARD, (int) MediaConstants.SHARD_UNIT),
                    new ItemCost(HexItems.CHARGED_AMETHYST, (int) MediaConstants.CRYSTAL_UNIT),
                };

                // get evenly divisible ItemStacks
                List<IVariable> validItemStacks =
                        Arrays.stream(costs)
                                .filter(
                                        itemCost ->
                                                itemCost.dividesEvenly(
                                                        (int) this.recipe.mediaCost()))
                                .map(
                                        validItemCost ->
                                                new ItemStack(
                                                        validItemCost.item,
                                                        (int) this.recipe.mediaCost()
                                                                / validItemCost.cost))
                                .map(IVariable::from)
                                .toList();

                if (!validItemStacks.isEmpty()) {
                    return IVariable.wrapList(validItemStacks);
                }
                // fallback: display in terms of dust
                return IVariable.from(
                        new ItemStack(
                                HexItems.AMETHYST_DUST,
                                (int) (this.recipe.mediaCost() / MediaConstants.DUST_UNIT)));
            }
            default -> {
                return null;
            }
        }
    }
}
