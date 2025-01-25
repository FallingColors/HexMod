package at.petrak.hexcasting.mixin;

import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import net.minecraft.server.MinecraftServer;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adds ancient scrolls to the wandering trader by replacing one of the 5 junk trades
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
            ItemStack scroll = new ItemStack(HexItems.SCROLL_LARGE);
            AddPerWorldPatternToScrollFunc.doStatic(scroll, rand, self.getServer().overworld());
            NBTHelper.putBoolean(scroll, ItemScroll.TAG_NEEDS_PURCHASE, true);
            offerList.set(5, new MerchantOffer(new ItemStack(Items.EMERALD, 12), scroll, 1, 1, 1));
        }
    }
}
