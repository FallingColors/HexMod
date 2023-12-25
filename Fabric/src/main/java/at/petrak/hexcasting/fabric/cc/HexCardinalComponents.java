package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.fabric.cc.adimpl.*;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;

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
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(i -> i instanceof PigmentItem, PIGMENT, CCPigment.ItemBased::new);

        registry.register(i -> i instanceof IotaHolderItem, IOTA_HOLDER, CCItemIotaHolder.ItemBased::new);
        // oh havoc, you think you're so funny
        // the worst part is you're /right/
        registry.register(Items.PUMPKIN_PIE, IOTA_HOLDER, stack -> new CCItemIotaHolder.Static(stack,
            s -> new DoubleIota(Math.PI * s.getCount())));

        registry.register(i -> i instanceof MediaHolderItem, MEDIA_HOLDER, CCMediaHolder.ItemBased::new);
        registry.register(HexItems.AMETHYST_DUST, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().dustMediaAmount(), ADMediaHolder.AMETHYST_DUST_PRIORITY, s
        ));
        registry.register(Items.AMETHYST_SHARD, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().shardMediaAmount(), ADMediaHolder.AMETHYST_SHARD_PRIORITY, s
        ));
        registry.register(HexItems.CHARGED_AMETHYST, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().chargedCrystalMediaAmount(), ADMediaHolder.CHARGED_AMETHYST_PRIORITY, s
        ));
        registry.register(HexItems.QUENCHED_SHARD.asItem(), MEDIA_HOLDER, s -> new CCMediaHolder.Static(
                () -> MediaConstants.QUENCHED_SHARD_UNIT, ADMediaHolder.QUENCHED_SHARD_PRIORITY, s
        ));
        registry.register(HexBlocks.QUENCHED_ALLAY.asItem(), MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> MediaConstants.QUENCHED_BLOCK_UNIT, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, s
        ));

        registry.register(i -> i instanceof HexHolderItem, HEX_HOLDER, CCHexHolder.ItemBased::new);

        registry.register(i -> i instanceof VariantItem, VARIANT_ITEM, CCVariantItem.ItemBased::new);
    }

    private <E extends Entity> ComponentFactory<E, CCEntityIotaHolder.Wrapper> wrapItemEntityDelegate(Function<E,
        ItemDelegatingEntityIotaHolder> make) {
        return e -> new CCEntityIotaHolder.Wrapper(make.apply(e));
    }
}
