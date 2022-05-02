package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.PatternTooltipGreeble;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.ItemSlate;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Function;

// https://github.com/VazkiiMods/Quark/blob/ace90bfcc26db4c50a179f026134e2577987c2b1/src/main/java/vazkii/quark/content/client/module/ImprovedTooltipsModule.java
public class HexTooltips {
    public static void init() {
        register(PatternTooltipGreeble.class);
    }

    private static <T extends ClientTooltipComponent & TooltipComponent> void register(Class<T> clazz) {
        MinecraftForgeClient.registerTooltipComponentFactory(clazz, Function.identity());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void makeTooltip(RenderTooltipEvent.GatherComponents evt) {
        ItemStack stack = evt.getItemStack();
        if (!stack.isEmpty()) {
            if (stack.is(HexItems.SCROLL.get())) {
                CompoundTag patternTag = NBTHelper.getCompound(stack, ItemScroll.TAG_PATTERN);
                if (patternTag != null) {
                    var pattern = HexPattern.DeserializeFromNBT(patternTag);
                    evt.getTooltipElements().add(Either.right(new PatternTooltipGreeble(
                        pattern,
                        NBTHelper.contains(stack, ItemScroll.TAG_OP_ID, Tag.TAG_STRING)
                            ? PatternTooltipGreeble.ANCIENT_BG : PatternTooltipGreeble.PRISTINE_BG)));
                }
            } else if (stack.is(HexItems.SLATE.get()) && ItemSlate.hasPattern(stack)) {
                var tag = NBTHelper.getCompound(NBTHelper.getOrCreateCompound(stack, "BlockEntityTag"), BlockEntitySlate.TAG_PATTERN);
                if (tag != null) {
                    var pattern = HexPattern.DeserializeFromNBT(tag);
                    evt.getTooltipElements().add(Either.right(new PatternTooltipGreeble(
                        pattern,
                        PatternTooltipGreeble.SLATE_BG)));
                }
            }
        }
    }
}
