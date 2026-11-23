package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adds ancient scrolls to the wandering trader by replacing the special trade in the last slot
@Mixin(WanderingTrader.class)
public class MixinWanderingTrader {
	@Inject(method = "updateTrades", at = @At("RETURN"))
	private void addNewTrades(CallbackInfo ci) {
        var self = (WanderingTrader) (Object) this;
        MerchantOffers offerList = self.getOffers();
        if (offerList == null)
            return;
        RandomSource rand = self.getRandom();
        if (rand.nextFloat() < HexConfig.server().traderScrollChance() && self.getServer() != null) {
            var actionID = IXplatAbstractions.INSTANCE.getActionRegistry().getTag(HexTags.Actions.PER_WORLD_PATTERN)
                    .flatMap(set -> set.getRandomElement(rand)).flatMap(Holder::unwrapKey)
                    .map(key -> key.location().toString()).orElseThrow();
            ItemStack scroll = ItemScroll.withPerWorldPattern(new ItemStack(HexItems.SCROLL_LARGE), actionID);
            NBTHelper.putBoolean(scroll, ItemScroll.TAG_NEEDS_PURCHASE, true);
            offerList.set(5, new MerchantOffer(new ItemStack(Items.EMERALD, 12), scroll, 1, 1, 1));
        }
    }
}
