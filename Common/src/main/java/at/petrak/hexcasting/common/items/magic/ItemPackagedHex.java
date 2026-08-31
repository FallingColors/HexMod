package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Item that holds a list of patterns in it ready to be cast
 */
public abstract class ItemPackagedHex extends ItemMediaHolder implements HexHolderItem {
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
        return stack.has(HexDataComponents.HEX_HOLDER.get());
    }

    @Override
    public @Nullable List<Iota> getHex(ItemStack stack, ServerLevel level) {
        var hexHolder = stack.get(HexDataComponents.HEX_HOLDER.get());
        return (hexHolder != null) ? hexHolder.hex() : null;
    }

    @Override
    public void writeHex(ItemStack stack, List<Iota> program, FrozenPigment pigment, long media) {
        stack.set(HexDataComponents.HEX_HOLDER.get(), new HexHolder(program, pigment));
        withMedia(stack, media, media);
    }

    @Override
    public void clearHex(ItemStack stack) {
        stack.remove(HexDataComponents.HEX_HOLDER.get());
        stack.remove(HexDataComponents.MEDIA.get());
        stack.remove(HexDataComponents.MEDIA_MAX.get());
    }

    @Override
    public @Nullable FrozenPigment getPigment(ItemStack stack) {
        var hexHolder = stack.get(HexDataComponents.HEX_HOLDER.get());
        return (hexHolder != null) ? hexHolder.pigment() : null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
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
        var env = new PackagedItemCastEnv(sPlayer, usedHand);
        var vm = CastingVM.empty(env);
        var clientView = vm.queueExecuteAndWrapIotas(instrs, sPlayer.serverLevel());

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
                    .sprayParticles(sPlayer.serverLevel(), env.getPigment());
        }

        var sound = env.getSound().sound();
        if (sound != null) {
            var soundPos = sPlayer.position();
            sPlayer.level().playSound(null, soundPos.x, soundPos.y, soundPos.z,
                    sound, SoundSource.PLAYERS, 1f, 1f);
        }

        if (broken) {
            stack.shrink(1);
            sPlayer.onEquippedItemBroken(stack.getItem(), usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    public record HexHolder(@NotNull List<Iota> hex, @NotNull FrozenPigment pigment) {
        public static final Codec<HexHolder> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        IotaType.TYPED_CODEC.listOf().fieldOf("hex").forGetter(HexHolder::hex),
                        FrozenPigment.CODEC.fieldOf("pigment").forGetter(HexHolder::pigment)
                ).apply(inst, HexHolder::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, HexHolder> STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), HexHolder::hex,
                FrozenPigment.STREAM_CODEC, HexHolder::pigment,
                HexHolder::new
        );
    }
}
