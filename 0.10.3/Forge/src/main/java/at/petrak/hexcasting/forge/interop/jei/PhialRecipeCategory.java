package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.casting.operators.spells.OpMakeBattery;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class PhialRecipeCategory implements IRecipeCategory<OpMakeBattery> {
    public static final ResourceLocation UID = modLoc("craft_phial");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final Component localizedName;

    public PhialRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = modLoc("textures/gui/phial_jei.png");
        background = guiHelper.drawableBuilder(location, 0, 0, 113, 40).setTextureSize(128, 128).build();
        var craftPhial = modLoc("craft/battery");
        localizedName = Component.translatable("hexcasting.spell." + craftPhial);
        icon = new PatternDrawable(craftPhial, 12, 12);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull OpMakeBattery recipe,
        @NotNull IFocusGroup focuses) {
        var stacks = PhialRecipeStackBuilder.createStacks();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
            .addItemStacks(stacks.getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 47, 12)
            .addIngredients(Ingredient.of(HexTags.Items.PHIAL_BASE));

        var outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 12)
            .addItemStacks(stacks.getSecond());

        builder.createFocusLink(inputSlot, outputSlot);
    }

    @Override
    public @NotNull RecipeType<OpMakeBattery> getRecipeType() {
        return HexJEIPlugin.PHIAL;
    }
}
