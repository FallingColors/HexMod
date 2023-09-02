package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.client.RenderLib.renderEntity;

public class BrainsweepRecipeCategory implements IRecipeCategory<BrainsweepRecipe> {
    public static final ResourceLocation UID = modLoc("brainsweep");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final Component localizedName;

    public BrainsweepRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = modLoc("textures/gui/brainsweep_jei.png");
        background = guiHelper.drawableBuilder(location, 0, 0, 118, 86).setTextureSize(128, 128).build();
        var brainsweep = modLoc("brainsweep");
        localizedName = new TranslatableComponent("hexcasting.spell." + brainsweep);
        icon = new PatternDrawable(brainsweep, 16, 16);
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
    public @NotNull List<Component> getTooltipStrings(@NotNull BrainsweepRecipe recipe,
        @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (37 <= mouseX && mouseX <= 37 + 26 && 19 <= mouseY && mouseY <= 19 + 48) {
            Minecraft mc = Minecraft.getInstance();
            return recipe.villagerIn().getTooltip(mc.options.advancedItemTooltips);
        }

        return Collections.emptyList();
    }

    @Override
    public void draw(@NotNull BrainsweepRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
        @NotNull PoseStack stack, double mouseX, double mouseY) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Villager villager = RenderLib.prepareVillagerForRendering(recipe.villagerIn(), level);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            renderEntity(stack, villager, level, 50, 62.5f, ClientTickCounter.getTotal(), 20, 0);
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BrainsweepRecipe recipe,
        @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 35)
            .addItemStacks(recipe.blockIn().getDisplayedStacks());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 87, 35)
            .addItemStack(new ItemStack(recipe.result().getBlock()));
    }

    @Override
    public @NotNull RecipeType<BrainsweepRecipe> getRecipeType() {
        return HexJEIPlugin.BRAINSWEEPING;
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull Class<? extends BrainsweepRecipe> getRecipeClass() {
        return BrainsweepRecipe.class;
    }
}
