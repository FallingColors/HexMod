package at.petrak.hex.client;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.SpellDatum;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.items.ItemFocus;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HexMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterClientStuff {
    @SubscribeEvent
    public static void init(final FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> ItemProperties.register(HexItems.FOCUS.get(), ItemFocus.PREDICATE,
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
                            case SpellDatum.TAG_SPELL -> 4f;
                            case SpellDatum.TAG_WIDGET -> 5f;
                            case SpellDatum.TAG_LIST -> 6f;
                            case SpellDatum.TAG_PATTERN -> 7f;
                            default -> 0f; // uh oh
                        };
                    } else {
                        return baseNum;
                    }
                }));
    }
}
