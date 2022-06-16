package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.iota.DoubleIota;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.fabric.cc.adimpl.CCColorizer;
import at.petrak.hexcasting.fabric.cc.adimpl.CCHexHolder;
import at.petrak.hexcasting.fabric.cc.adimpl.CCIotaHolder;
import at.petrak.hexcasting.fabric.cc.adimpl.CCMediaHolder;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCardinalComponents implements EntityComponentInitializer, ItemComponentInitializer {
    // entities
    public static final ComponentKey<CCBrainswept> BRAINSWEPT = ComponentRegistry.getOrCreate(modLoc("brainswept"),
        CCBrainswept.class);
    public static final ComponentKey<CCFavoredColorizer> FAVORED_COLORIZER = ComponentRegistry.getOrCreate(
        modLoc("favored_colorizer"), CCFavoredColorizer.class);
    public static final ComponentKey<CCSentinel> SENTINEL = ComponentRegistry.getOrCreate(modLoc("sentinel"),
        CCSentinel.class);
    public static final ComponentKey<CCFlight> FLIGHT = ComponentRegistry.getOrCreate(modLoc("flight"),
        CCFlight.class);
    public static final ComponentKey<CCHarness> HARNESS = ComponentRegistry.getOrCreate(modLoc("harness"),
        CCHarness.class);
    public static final ComponentKey<CCPatterns> PATTERNS = ComponentRegistry.getOrCreate(modLoc("patterns"),
        CCPatterns.class);

    public static final ComponentKey<CCColorizer> COLORIZER = ComponentRegistry.getOrCreate(modLoc("colorizer"),
        CCColorizer.class);
    public static final ComponentKey<CCIotaHolder> IOTA_HOLDER = ComponentRegistry.getOrCreate(modLoc("iota_holder"),
        CCIotaHolder.class);
    public static final ComponentKey<CCMediaHolder> MEDIA_HOLDER = ComponentRegistry.getOrCreate(modLoc("media_holder"),
        CCMediaHolder.class);
    public static final ComponentKey<CCHexHolder> HEX_HOLDER = ComponentRegistry.getOrCreate(modLoc("hex_holder"),
        CCHexHolder.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Mob.class, BRAINSWEPT, CCBrainswept::new);
        registry.registerForPlayers(FAVORED_COLORIZER, CCFavoredColorizer::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(SENTINEL, CCSentinel::new, RespawnCopyStrategy.ALWAYS_COPY);
        // Fortunately these are all both only needed on the server and don't want to be copied across death
        registry.registerFor(ServerPlayer.class, FLIGHT, CCFlight::new);
        registry.registerFor(ServerPlayer.class, HARNESS, CCHarness::new);
        registry.registerFor(ServerPlayer.class, PATTERNS, CCPatterns::new);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(i -> i instanceof ColorizerItem, COLORIZER, CCColorizer.ItemBased::new);

        registry.register(i -> i instanceof IotaHolderItem, IOTA_HOLDER, CCIotaHolder.ItemBased::new);
        // oh havoc, you think you're so funny
        // the worst part is you're /right/
        registry.register(Items.PUMPKIN_PIE, IOTA_HOLDER, stack ->
            new CCIotaHolder.Static(stack, s -> new DoubleIota(Math.PI * s.getCount())));

        registry.register(i -> i instanceof MediaHolderItem, MEDIA_HOLDER, CCMediaHolder.ItemBased::new);
        registry.register(HexItems.AMETHYST_DUST, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().dustManaAmount(), ADMediaHolder.AMETHYST_DUST_PRIORITY, s
        ));
        registry.register(Items.AMETHYST_SHARD, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().shardManaAmount(), ADMediaHolder.AMETHYST_SHARD_PRIORITY, s
        ));
        registry.register(HexItems.CHARGED_AMETHYST, MEDIA_HOLDER, s -> new CCMediaHolder.Static(
            () -> HexConfig.common().chargedCrystalManaAmount(), ADMediaHolder.CHARGED_AMETHYST_PRIORITY, s
        ));

        registry.register(i -> i instanceof HexHolderItem, HEX_HOLDER, CCHexHolder.ItemBased::new);
    }
}
