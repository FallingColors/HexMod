package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.items.HexItems;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    public static final ComponentKey<CCColorizer> COLORIZER = ComponentRegistry.getOrCreate(modLoc("colorizer"),
        CCColorizer.class);
    public static final ComponentKey<CCDataHolder> DATA_HOLDER = ComponentRegistry.getOrCreate(modLoc("data_holder"),
        CCDataHolder.class);
    public static final ComponentKey<CCManaHolder> MANA_HOLDER = ComponentRegistry.getOrCreate(modLoc("mana_holder"),
        CCManaHolder.class);
    public static final ComponentKey<CCHexHolder> HEX_HOLDER = ComponentRegistry.getOrCreate(modLoc("hex_holder"),
        CCHexHolder.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(LivingEntity.class, BRAINSWEPT, CCBrainswept::new);
        registry.registerFor(Player.class, FAVORED_COLORIZER, CCFavoredColorizer::new);
        registry.registerFor(Player.class, SENTINEL, CCSentinel::new);
        registry.registerFor(Player.class, FLIGHT, CCFlight::new);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(i -> i instanceof ColorizerItem, COLORIZER, CCColorizer.ItemBased::new);

        registry.register(i -> i instanceof DataHolderItem, DATA_HOLDER, CCDataHolder.ItemBased::new);
        // oh havoc, you think you're so funny
        // the worst part is you're /right/
        registry.register(Items.PUMPKIN_PIE, DATA_HOLDER, stack -> new CCDataHolder.Static(stack,
            s -> SpellDatum.make(Math.PI * s.getCount())));

        registry.register(i -> i instanceof ManaHolderItem, MANA_HOLDER, CCManaHolder.ItemBased::new);
        registry.register(HexItems.AMETHYST_DUST, MANA_HOLDER, s -> new CCManaHolder.Static(
            () -> HexConfig.common().dustManaAmount(), 30, s
        ));
        registry.register(Items.AMETHYST_SHARD, MANA_HOLDER, s -> new CCManaHolder.Static(
            () -> HexConfig.common().shardManaAmount(), 20, s
        ));
        registry.register(HexItems.CHARGED_AMETHYST, MANA_HOLDER, s -> new CCManaHolder.Static(
            () -> HexConfig.common().chargedCrystalManaAmount(), 10, s
        ));

        registry.register(i -> i instanceof HexHolderItem, HEX_HOLDER, CCHexHolder.ItemBased::new);
    }
}
