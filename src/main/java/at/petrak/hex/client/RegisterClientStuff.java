package at.petrak.hex.client;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.SpellDatum;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.items.ItemFocus;
import at.petrak.hex.common.items.magic.ItemPackagedSpell;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = HexMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterClientStuff {
    @SubscribeEvent
    public static void init(final FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            ItemProperties.register(HexItems.FOCUS.get(), ItemFocus.DATATYPE_PRED,
                    (stack, level, holder, holderID) -> {
                        var tag = stack.getOrCreateTag();
                        var isSealed = tag.getBoolean(ItemFocus.TAG_SEALED);
                        var baseNum = isSealed ? 100f : 0f;
                        if (stack.hasTag()) {
                            var typename = tag.getCompound(ItemFocus.TAG_DATA).getAllKeys().iterator().next();
                            return baseNum + switch (typename) {
                                case SpellDatum.TAG_ENTITY -> 1f;
                                case SpellDatum.TAG_DOUBLE -> 2f;
                                case SpellDatum.TAG_VEC3 -> 3f;
                                case SpellDatum.TAG_WIDGET -> 4f;
                                case SpellDatum.TAG_LIST -> 5f;
                                case SpellDatum.TAG_PATTERN -> 6f;
                                default -> 0f; // uh oh
                            };
                        } else {
                            return baseNum;
                        }
                    });
            for (RegistryObject<Item> packager : new RegistryObject[]{
                    HexItems.CYPHER,
                    HexItems.TRINKET,
                    HexItems.ARTIFACT,
            }) {
                ItemProperties.register(packager.get(), ItemPackagedSpell.HAS_PATTERNS_PRED,
                        (stack, level, holder, holderID) ->
                                stack.getOrCreateTag().contains(ItemPackagedSpell.TAG_PATTERNS) ? 1f : 0f
                );
            }
        });
    }
}
