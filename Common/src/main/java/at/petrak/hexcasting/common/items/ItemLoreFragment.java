package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemLoreFragment extends Item {
    public static final List<ResourceLocation> NAMES = List.of(new ResourceLocation[]{
        modLoc("lore/terabithia1"),
        modLoc("lore/terabithia2"),
        modLoc("lore/terabithia3"),
        modLoc("lore/terabithia4"),
        modLoc("lore/terabithia5"),
        modLoc("lore/experiment1"),
        modLoc("lore/experiment2"),
        modLoc("lore/inventory"),
    });

    public static final String CRITEREON_KEY = "grant";

    public ItemLoreFragment(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.playSound(HexSounds.READ_LORE_FRAGMENT, 1f, 1f);

        var handStack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer splayer)) {
            handStack.shrink(1);
            return InteractionResultHolder.success(handStack);
        }

        Advancement unfoundLore = null;
        var shuffled = new ArrayList<>(NAMES);
        Collections.shuffle(shuffled);
        for (var advID : shuffled) {
            var adv = splayer.level.getServer().getAdvancements().getAdvancement(advID);
            if (adv == null) {
                continue; // uh oh
            }

            if (!splayer.getAdvancements().getOrStartProgress(adv).isDone()) {
                unfoundLore = adv;
                break;
            }
        }

        if (unfoundLore == null) {
            splayer.sendMessage(new TranslatableComponent("item.hexcasting.lore_fragment.all"), ChatType.GAME_INFO,
                Util.NIL_UUID);
            splayer.giveExperiencePoints(20);
            level.playSound(null, player.position().x, player.position().y, player.position().z,
                HexSounds.READ_LORE_FRAGMENT, SoundSource.PLAYERS, 1f, 1f);
        } else {
            // et voila!
            splayer.getAdvancements().award(unfoundLore, CRITEREON_KEY);
        }

        CriteriaTriggers.CONSUME_ITEM.trigger(splayer, handStack);
        splayer.awardStat(Stats.ITEM_USED.get(this));
        handStack.shrink(1);

        return InteractionResultHolder.success(handStack);
    }
}
