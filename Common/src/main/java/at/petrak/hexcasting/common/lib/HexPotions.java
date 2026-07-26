package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexPotions {
    private static final IXplatRegister<Potion> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.POTION);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<Potion> ENLARGE_GRID = REGISTER.registerHolder("enlarge_grid", () ->
        new Potion("enlarge_grid", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 3600)));
    public static final Holder<Potion> ENLARGE_GRID_LONG = REGISTER.registerHolder("enlarge_grid_long", () ->
        new Potion("enlarge_grid_long", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 9600)));
    public static final Holder<Potion> ENLARGE_GRID_STRONG = REGISTER.registerHolder("enlarge_grid_strong", () ->
        new Potion("enlarge_grid_strong", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 1800, 1)));

    public static final Holder<Potion> SHRINK_GRID = REGISTER.registerHolder("shrink_grid", () ->
        new Potion("shrink_grid", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 3600)));
    public static final Holder<Potion> SHRINK_GRID_LONG = REGISTER.registerHolder("shrink_grid_long", () ->
        new Potion("shrink_grid_long", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 9600)));
    public static final Holder<Potion> SHRINK_GRID_STRONG = REGISTER.registerHolder("shrink_grid_strong", () ->
        new Potion("shrink_grid_strong", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 1800, 1)));

    public static void addRecipes(PotionBrewing.Builder builder) {
        builder.addMix(Potions.AWKWARD, HexItems.AMETHYST_DUST.get(), ENLARGE_GRID);
        builder.addMix(ENLARGE_GRID, Items.REDSTONE, ENLARGE_GRID_LONG);
        builder.addMix(ENLARGE_GRID, Items.GLOWSTONE_DUST, ENLARGE_GRID_STRONG);

        builder.addMix(ENLARGE_GRID, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID);
        builder.addMix(ENLARGE_GRID_LONG, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID_LONG);
        builder.addMix(ENLARGE_GRID_STRONG, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID_STRONG);

        builder.addMix(SHRINK_GRID, Items.REDSTONE, SHRINK_GRID_LONG);
        builder.addMix(SHRINK_GRID, Items.GLOWSTONE_DUST, SHRINK_GRID_STRONG);
    }
}
