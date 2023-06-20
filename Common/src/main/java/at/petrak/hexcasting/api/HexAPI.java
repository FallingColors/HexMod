package at.petrak.hexcasting.api;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface HexAPI {
    String MOD_ID = "hexcasting";
    Logger LOGGER = LogManager.getLogger(MOD_ID);

    Supplier<HexAPI> INSTANCE = Suppliers.memoize(() -> {
        try {
            return (HexAPI) Class.forName("at.petrak.hexcasting.common.impl.HexAPIImpl")
                .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LogManager.getLogger().warn("Unable to find HexAPIImpl, using a dummy");
            return new HexAPI() {
            };
        }
    });

    /**
     * Return the localization key for the given action.
     * <p>
     * Note we're allowed to have action <em>resource keys</em> on the client, just no actual actions.
     * <p>
     * Special handlers should be calling {@link SpecialHandler#getName()}
     */
    default String getActionI18nKey(ResourceKey<ActionRegistryEntry> action) {
        return "hexcasting.action.%s".formatted(action.location().toString());
    }

    default String getSpecialHandlerI18nKey(ResourceKey<SpecialHandler.Factory<?>> action) {
        return "hexcasting.special.%s".formatted(action.location().toString());
    }

    /**
     * Currently introspection/retrospection/consideration are hardcoded, but at least their names won't be
     */
    default String getRawHookI18nKey(ResourceLocation name) {
        return "hexcasting.rawhook.%s".formatted(name);
    }

    default Component getActionI18n(ResourceKey<ActionRegistryEntry> key, boolean isGreat) {
        return Component.translatable(getActionI18nKey(key))
            .withStyle(isGreat ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE);
    }

    default Component getSpecialHandlerI18n(ResourceKey<SpecialHandler.Factory<?>> key) {
        return Component.translatable(getSpecialHandlerI18nKey(key))
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    default Component getRawHookI18n(ResourceLocation name) {
        return Component.translatable(getRawHookI18nKey(name)).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    /**
     * Register an entity with the given ID to have its velocity as perceived by OpEntityVelocity be different
     * than it's "normal" velocity
     */
    // Should be OK to use the type directly as the key as they're singleton identity objects
    default <T extends Entity> void registerSpecialVelocityGetter(EntityType<T> key, EntityVelocityGetter<T> getter) {
    }

    /**
     * If the entity has had a special getter registered with {@link HexAPI#registerSpecialVelocityGetter} then
     * return that, otherwise return its normal delta movement
     */
    default Vec3 getEntityVelocitySpecial(Entity entity) {
        return entity.getDeltaMovement();
    }

    @FunctionalInterface
    interface EntityVelocityGetter<T extends Entity> {
        Vec3 getVelocity(T entity);
    }

    /**
     * Register an entity type to have a custom behavior when getting brainswept.
     * <p>
     * This knocks out the normal behavior; if you want that behavior you should call
     */
    default <T extends Mob> void registerCustomBrainsweepingBehavior(EntityType<T> key, Consumer<T> hook) {
    }

    /**
     * The default behavior when an entity gets brainswept.
     * <p>
     * Something registered with {@link HexAPI#registerCustomBrainsweepingBehavior} doesn't call this automatically;
     * you can use this to add things on top of the default behavior
     */
    default Consumer<Mob> defaultBrainsweepingBehavior() {
        return mob -> {
        };
    }

    /**
     * If something special's been returned with {@link HexAPI#registerCustomBrainsweepingBehavior}, return that,
     * otherwise return the default behavior
     */
    default <T extends Mob> Consumer<T> getBrainsweepBehavior(EntityType<T> mobType) {
        return mob -> {
        };
    }

    /**
     * Brainsweep (flay the mind of) the given mob.
     * <p>
     * This ignores the unbrainsweepable tag.
     */
    default void brainsweep(Mob mob) {
        var type = (EntityType<? extends Mob>) mob.getType();
        var behavior = this.getBrainsweepBehavior(type);
        var erasedBehavior = (Consumer<Mob>) behavior;
        erasedBehavior.accept(mob);

        IXplatAbstractions.INSTANCE.setBrainsweepAddlData(mob);
    }

    default boolean isBrainswept(Mob mob) {
        return IXplatAbstractions.INSTANCE.isBrainswept(mob);
    }

    //
    @Nullable
    default Sentinel getSentinel(ServerPlayer player) {
        return null;
    }

    @Nullable
    default ADMediaHolder findMediaHolder(ItemStack stack) {
        return null;
    }

    default FrozenPigment getColorizer(Player player) {
        return FrozenPigment.DEFAULT.get();
    }

    ArmorMaterial DUMMY_ARMOR_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return 0;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @NotNull
        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @NotNull
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "missingno";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    };

    default ArmorMaterial robesMaterial() {
        return DUMMY_ARMOR_MATERIAL;
    }

    /**
     * Location in the userdata of the ravenmind
     */
    String RAVENMIND_USERDATA = modLoc("ravenmind").toString();
    /**
     * Location in the userdata of the number of ops executed
     */
    String OP_COUNT_USERDATA = modLoc("op_count").toString();

    String MARKED_MOVED_USERDATA = modLoc("impulsed").toString();

    static HexAPI instance() {
        return INSTANCE.get();
    }

    static ResourceLocation modLoc(String s) {
        return new ResourceLocation(MOD_ID, s);
    }
}
