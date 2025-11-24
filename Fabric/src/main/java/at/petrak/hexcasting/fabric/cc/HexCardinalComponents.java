package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.components.*;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.fabric.cc.adimpl.*;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.item.ItemComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import org.ladysnake.cca.api.v3.item.ItemComponentMigrationRegistry;

import java.util.function.Function;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCardinalComponents implements EntityComponentInitializer, ItemComponentInitializer {
    // entities
    public static final ComponentKey<CCBrainswept> BRAINSWEPT = ComponentRegistry.getOrCreate(modLoc("brainswept"),
        CCBrainswept.class);
    public static final ComponentKey<CCFavoredPigment> FAVORED_PIGMENT = ComponentRegistry.getOrCreate(
        modLoc("favored_pigment"), CCFavoredPigment.class);
    public static final ComponentKey<CCSentinel> SENTINEL = ComponentRegistry.getOrCreate(modLoc("sentinel"),
        CCSentinel.class);
    public static final ComponentKey<CCFlight> FLIGHT = ComponentRegistry.getOrCreate(modLoc("flight"),
        CCFlight.class);

    public static final ComponentKey<CCAltiora> ALTIORA = ComponentRegistry.getOrCreate(modLoc("altiora"),
        CCAltiora.class);
    public static final ComponentKey<CCStaffcastImage> STAFFCAST_IMAGE = ComponentRegistry.getOrCreate(modLoc(
        "harness"),
        CCStaffcastImage.class);
    public static final ComponentKey<CCPatterns> PATTERNS = ComponentRegistry.getOrCreate(modLoc("patterns"),
        CCPatterns.class);

    public static final ComponentKey<CCClientCastingStack> CLIENT_CASTING_STACK = ComponentRegistry.getOrCreate(modLoc("client_casting_stack"),
            CCClientCastingStack.class);

    public static final ComponentKey<CCPigment> PIGMENT = ComponentRegistry.getOrCreate(modLoc("pigment"),
        CCPigment.class);
    public static final ComponentKey<CCIotaHolder> IOTA_HOLDER = ComponentRegistry.getOrCreate(modLoc("iota_holder"),
        CCIotaHolder.class);
    public static final ComponentKey<CCMediaHolder> MEDIA_HOLDER = ComponentRegistry.getOrCreate(modLoc("media_holder"),
        CCMediaHolder.class);
    public static final ComponentKey<CCHexHolder> HEX_HOLDER = ComponentRegistry.getOrCreate(modLoc("hex_holder"),
        CCHexHolder.class);

    public static final ComponentKey<CCVariantItem> VARIANT_ITEM = ComponentRegistry.getOrCreate(modLoc("variant_item"),
        CCVariantItem.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Mob.class, BRAINSWEPT, CCBrainswept::new);
        registry.registerForPlayers(FAVORED_PIGMENT, CCFavoredPigment::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(SENTINEL, CCSentinel::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ALTIORA, CCAltiora::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(CLIENT_CASTING_STACK, CCClientCastingStack::new, RespawnCopyStrategy.NEVER_COPY);
        // Fortunately these are all both only needed on the server and don't want to be copied across death
        registry.registerFor(ServerPlayer.class, FLIGHT, CCFlight::new);
        registry.registerFor(ServerPlayer.class, STAFFCAST_IMAGE, CCStaffcastImage::new);
        registry.registerFor(ServerPlayer.class, PATTERNS, CCPatterns::new);


        registry.registerFor(ItemEntity.class, IOTA_HOLDER, wrapItemEntityDelegate(
            ItemDelegatingEntityIotaHolder.ToItemEntity::new));
        registry.registerFor(ItemFrame.class, IOTA_HOLDER, wrapItemEntityDelegate(
            ItemDelegatingEntityIotaHolder.ToItemFrame::new));
        registry.registerFor(EntityWallScroll.class, IOTA_HOLDER,
            wrapItemEntityDelegate(ItemDelegatingEntityIotaHolder.ToWallScroll::new));
    }

    @Override
    public void registerItemComponentMigrations(ItemComponentMigrationRegistry registry) {
        registry.registerMigration(modLoc("pigment"), PigmentItemComponent.COMPONENT_TYPE);

        registry.registerMigration(modLoc("iota_holder"), ItemIotaHolderComponent.COMPONENT_TYPE);
        // oh havoc, you think you're so funny
        // the worst part is you're /right/
        registry.registerMigration(Items.PUMPKIN_PIE, IOTA_HOLDER, stack -> new CCItemIotaHolder.Static(stack,
            s -> new DoubleIota(Math.PI * s.getCount())));

        registry.registerMigration(modLoc("media_holder"), ItemMediaHolderComponent.COMPONENT_TYPE);
        registry.registerMigration(modLoc("media_holder"), s -> new CCMediaHolder.Static(
            () -> HexConfig.common().dustMediaAmount(), ADMediaHolder.AMETHYST_DUST_PRIORITY, s
        ));
        registry.registerMigration(Items.AMETHYST_SHARD, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().shardMediaAmount(), ADMediaHolder.AMETHYST_SHARD_PRIORITY, s
        ));
        registry.registerMigration(HexItems.CHARGED_AMETHYST, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().chargedCrystalMediaAmount(), ADMediaHolder.CHARGED_AMETHYST_PRIORITY, s
        ));
        registry.registerMigration(HexItems.QUENCHED_SHARD.asItem(), MEDIA_HOLDER, s -> new CCMediaHolder.Static(
                () -> MediaConstants.QUENCHED_SHARD_UNIT, ADMediaHolder.QUENCHED_SHARD_PRIORITY, s
        ));
        registry.registerMigration(HexBlocks.QUENCHED_ALLAY.asItem(), MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> MediaConstants.QUENCHED_BLOCK_UNIT, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, s
        ));

        registry.registerMigration(modLoc("hex_holder"), ItemHexHolderComponent.COMPONENT_TYPE);

        registry.registerMigration(modLoc("variant_item"), VariantItemComponent.COMPONENT_TYPE);
    }

    private <E extends Entity> ComponentFactory<E, CCEntityIotaHolder.Wrapper> wrapItemEntityDelegate(Function<E,
        ItemDelegatingEntityIotaHolder> make) {
        return e -> new CCEntityIotaHolder.Wrapper(make.apply(e));
    }
}
