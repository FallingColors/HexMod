package at.petrak.hexcasting.common.impl;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class HexAPIImpl implements HexAPI {
    private static final ConcurrentMap<EntityType<?>, EntityVelocityGetter<?>> SPECIAL_VELOCITIES
        = new ConcurrentHashMap<>();
    private static final ConcurrentMap<EntityType<?>, Consumer<?>> SPECIAL_BRAINSWEEPS
        = new ConcurrentHashMap<>();

    public <T extends Entity> void registerSpecialVelocityGetter(EntityType<T> key,
        EntityVelocityGetter<T> getter) {
        if (SPECIAL_VELOCITIES.containsKey(key)) {
            HexAPI.LOGGER.warn("A special velocity getter was already registered to {}, clobbering it!",
                key.toString());
        }
        SPECIAL_VELOCITIES.put(key, getter);
    }

    @Override
    public Vec3 getEntityVelocitySpecial(Entity entity) {
        EntityType<?> type = entity.getType();
        if (SPECIAL_VELOCITIES.containsKey(type)) {
            var velGetter = SPECIAL_VELOCITIES.get(type);
            var erasedGetter = (EntityVelocityGetter) velGetter;
            return erasedGetter.getVelocity(entity);
        }
        return entity.getDeltaMovement();
    }

    //region brainsweeping

    @Override
    public <T extends Mob> void registerCustomBrainsweepingBehavior(EntityType<T> key, Consumer<T> hook) {
        if (SPECIAL_BRAINSWEEPS.containsKey(key)) {
            HexAPI.LOGGER.warn("A special brainsweep hook was already registered to {}, clobbering it!",
                key.toString());
        }
        SPECIAL_BRAINSWEEPS.put(key, hook);
    }

    @Override
    public <T extends Mob> Consumer<T> getBrainsweepBehavior(EntityType<T> mobType) {
        var behavior = SPECIAL_BRAINSWEEPS.getOrDefault(mobType, this.defaultBrainsweepingBehavior());
        return (Consumer<T>) behavior;
    }

    @Override
    public Consumer<Mob> defaultBrainsweepingBehavior() {
        return mob -> {
            mob.removeFreeWill();

            // TODO: do we add this?
//            if (mob instanceof InventoryCarrier inv) {
//                inv.getInventory().removeAllItems().forEach(mob::spawnAtLocation);
//            }
        };
    }

    //endregion
    @Override
    public @Nullable Sentinel getSentinel(ServerPlayer player) {
        return IXplatAbstractions.INSTANCE.getSentinel(player);
    }

    @Override
    public @Nullable ADMediaHolder findMediaHolder(ItemStack stack) {
        return IXplatAbstractions.INSTANCE.findMediaHolder(stack);
    }

    @Override
    public FrozenPigment getColorizer(Player player) {
        return IXplatAbstractions.INSTANCE.getPigment(player);
    }

    ArmorMaterial ARMOR_MATERIAL = new ArmorMaterial() {

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
            return "robes";
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

    @Override
    public ArmorMaterial robesMaterial() {
        return ARMOR_MATERIAL;
    }
}
