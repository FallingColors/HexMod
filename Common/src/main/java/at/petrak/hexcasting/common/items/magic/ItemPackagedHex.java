package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.components.ItemHexHolderComponent;
import at.petrak.hexcasting.common.components.ItemMediaHolderComponent;
import at.petrak.hexcasting.common.components.PigmentItemComponent;
import at.petrak.hexcasting.common.msgs.MsgNewSpiralPatternsS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Item that holds a list of patterns in it ready to be cast
 */
public abstract class ItemPackagedHex extends ItemMediaHolder implements HexHolderItem {
    public static final String TAG_PROGRAM = "patterns";
    public static final String TAG_PIGMENT = "pigment";
    public static final ResourceLocation HAS_PATTERNS_PRED = modLoc("has_patterns");

    public ItemPackagedHex(Properties pProperties) {
        super(pProperties);
    }

    public abstract boolean breakAfterDepletion();

    public abstract int cooldown();

    @Override
    public boolean canRecharge(ItemStack stack) {
        return !breakAfterDepletion();
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasHex(ItemStack stack) {
        return ItemHexHolderComponent.hex(stack).isEmpty();
    }

    @Override
    public @Nullable List<Iota> getHex(ItemStack stack, ServerLevel level) {
        return ItemHexHolderComponent.hex(stack);
    }

    @Override
    public void writeHex(ItemStack stack, List<Iota> program, @Nullable FrozenPigment pigment, long media) {
        ListTag patsTag = new ListTag();
        for (Iota pat : program) {
            patsTag.add(IotaType.serialize(pat));
        }

        NBTHelper.putList(stack, TAG_PROGRAM, patsTag);
        if (pigment != null)
            NBTHelper.putCompound(stack, TAG_PIGMENT, pigment.serializeToNBT());

        withMedia(stack, media, media);
    }

    @Override
    public void clearHex(ItemStack stack) {
        stack.remove(ItemHexHolderComponent.COMPONENT_TYPE);
        stack.remove(PigmentItemComponent.COMPONENT_TYPE);
        stack.remove(ItemMediaHolderComponent.COMPONENT_TYPE);
    }

    @Override
    public @Nullable FrozenPigment getPigment(ItemStack stack) {
        var ctag = NBTHelper.getCompound(stack, TAG_PIGMENT);
        if (ctag == null)
            return null;
        return FrozenPigment.fromNBT(ctag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        var stack = player.getItemInHand(usedHand);
        if (!hasHex(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        List<Iota> instrs = getHex(stack, (ServerLevel) world);
        if (instrs == null) {
            return InteractionResultHolder.fail(stack);
        }
        var sPlayer = (ServerPlayer) player;
        var ctx = new PackagedItemCastEnv(sPlayer, usedHand);
        var harness = CastingVM.empty(ctx);
        var clientView = harness.queueExecuteAndWrapIotas(instrs, sPlayer.serverLevel());

        var patterns = instrs.stream()
                .filter(i -> i instanceof PatternIota)
                .map(i -> ((PatternIota) i).getPattern())
                .toList();
        var packet = new MsgNewSpiralPatternsS2C(sPlayer.getUUID(), patterns, 140);
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(sPlayer, packet);
        IXplatAbstractions.INSTANCE.sendPacketTracking(sPlayer, packet);

        boolean broken = breakAfterDepletion() && getMedia(stack) == 0;

        Stat<?> stat;
        if (broken) {
            stat = Stats.ITEM_BROKEN.get(this);
        } else {
            stat = Stats.ITEM_USED.get(this);
        }
        player.awardStat(stat);

        sPlayer.getCooldowns().addCooldown(this, this.cooldown());

        if (clientView.getResolutionType().getSuccess()) {
            // Somehow we lost spraying particles on each new pattern, so do it here
            // this also nicely prevents particle spam on trinkets
            new ParticleSpray(player.position(), new Vec3(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30)
                    .sprayParticles(sPlayer.serverLevel(), ctx.getPigment());
        }

        var sound = ctx.getSound().sound();
        if (sound != null) {
            var soundPos = sPlayer.position();
            sPlayer.level().playSound(null, soundPos.x, soundPos.y, soundPos.z,
                    sound, SoundSource.PLAYERS, 1f, 1f);
        }

        if (broken) {
            stack.shrink(1);
            player.onEquippedItemBroken(stack.getItem(), slot);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }
}
