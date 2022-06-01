package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

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
            List<Component> tooltip = new ArrayList<>(3);
            var profession = recipe.villagerIn().profession();
            if (profession == null) {
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.profession.any"));
            } else {
                var professionKey = "entity.minecraft.villager." + profession.getPath();
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.profession",
                    new TranslatableComponent(professionKey)));
            }
            var biome = recipe.villagerIn().biome();
            if (biome == null) {
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.biome.any"));
            } else {
                var biomeKey = "biome.minecraft." + biome.getPath();
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.biome",
                    new TranslatableComponent(biomeKey)));
            }

            var minLevel = recipe.villagerIn().minLevel();
            if (minLevel >= 5)
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.level",
                        new TranslatableComponent("merchant.level." + minLevel)));
            else if (minLevel <= 1)
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.level.any"));
            else
                tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.min_level",
                        new TranslatableComponent("merchant.level." + minLevel)));
            return tooltip;
        }

        return Collections.emptyList();
    }

    private Villager villager;

    @Override
    public void draw(@NotNull BrainsweepRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
        @NotNull PoseStack stack, double mouseX, double mouseY) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            VillagerProfession profession = Objects.requireNonNullElse(
                ForgeRegistries.PROFESSIONS.getValue(recipe.villagerIn().profession()),
                VillagerProfession.TOOLSMITH);
            VillagerType biome = Objects.requireNonNullElse(Registry.VILLAGER_TYPE.get(recipe.villagerIn().biome()),
                VillagerType.PLAINS);
            int minLevel = recipe.villagerIn().minLevel();
            if (villager == null) {
                villager = new Villager(EntityType.VILLAGER, level);
            }

            villager.setVillagerData(villager.getVillagerData()
                .setProfession(profession)
                .setType(biome)
                .setLevel(minLevel));

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            renderEntity(stack, villager, level, 50, 62.5f, ClientTickCounter.total, 20, 0);
        }
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
