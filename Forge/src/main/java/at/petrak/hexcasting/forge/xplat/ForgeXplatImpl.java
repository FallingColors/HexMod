package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.Colorizer;
import at.petrak.hexcasting.api.addldata.DataHolder;
import at.petrak.hexcasting.api.addldata.HexHolder;
import at.petrak.hexcasting.api.addldata.ManaHolder;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.player.FlightAbility;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.forge.cap.CapSyncers;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.forge.recipe.ForgeUnsealedIngredient;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ForgeXplatImpl implements IXplatAbstractions {
    @Override
    public Platform platform() {
        return Platform.FORGE;
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public Attribute getReachDistance() {
        return ForgeMod.REACH_DISTANCE.get();
    }

    @Override
    public void brainsweep(Mob mob) {
        mob.getPersistentData().putBoolean(TAG_BRAINSWEPT, true);

        mob.removeFreeWill();

        if (mob.level instanceof ServerLevel) {
            ForgePacketHandler.getNetwork()
                .send(PacketDistributor.TRACKING_ENTITY.with(() -> mob), MsgBrainsweepAck.of(mob));
        }
    }

    @Override
    public void setFlight(ServerPlayer player, FlightAbility flight) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_FLIGHT_ALLOWED, flight.allowed());
        if (flight.allowed()) {
            tag.putInt(TAG_FLIGHT_TIME, flight.timeLeft());
            tag.put(TAG_FLIGHT_ORIGIN, HexUtils.serializeToNBT(flight.origin()));
            tag.putString(TAG_FLIGHT_DIMENSION, flight.dimension().location().toString());
            tag.putDouble(TAG_FLIGHT_RADIUS, flight.radius());
        } else {
            tag.remove(TAG_FLIGHT_TIME);
            tag.remove(TAG_FLIGHT_ORIGIN);
            tag.remove(TAG_FLIGHT_DIMENSION);
            tag.remove(TAG_FLIGHT_RADIUS);
        }
    }

    @Override
    public void setColorizer(Player player, FrozenColorizer colorizer) {
        CompoundTag tag = player.getPersistentData();
        tag.put(TAG_COLOR, colorizer.serialize());

        if (player instanceof ServerPlayer serverPlayer) {
            CapSyncers.syncColorizer(serverPlayer);
        }
    }

    @Override
    public void setSentinel(Player player, Sentinel sentinel) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_SENTINEL_EXISTS, sentinel.hasSentinel());
        if (sentinel.hasSentinel()) {
            tag.putBoolean(TAG_SENTINEL_GREATER, sentinel.extendsRange());
            tag.put(TAG_SENTINEL_POSITION, HexUtils.serializeToNBT(sentinel.position()));
            tag.putString(TAG_SENTINEL_DIMENSION, sentinel.dimension().location().toString());
        } else {
            tag.remove(TAG_SENTINEL_GREATER);
            tag.remove(TAG_SENTINEL_POSITION);
            tag.remove(TAG_SENTINEL_DIMENSION);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CapSyncers.syncSentinel(serverPlayer);
        }
    }

    @Override
    public void setHarness(ServerPlayer player, CastingHarness harness) {
        player.getPersistentData().put(TAG_HARNESS, harness == null ? new CompoundTag() : harness.serializeToNBT());
    }

    @Override
    public void setPatterns(ServerPlayer player, List<ResolvedPattern> patterns) {
        var listTag = new ListTag();
        for (ResolvedPattern pattern : patterns) {
            listTag.add(pattern.serializeToNBT());
        }
        player.getPersistentData().put(TAG_PATTERNS, listTag);
    }

    @Override
    public boolean isBrainswept(Mob e) {
        return e.getPersistentData().getBoolean(TAG_BRAINSWEPT);
    }

    @Override
    public FlightAbility getFlight(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        boolean allowed = tag.getBoolean(TAG_FLIGHT_ALLOWED);
        if (allowed) {
            var timeLeft = tag.getInt(TAG_FLIGHT_TIME);
            var origin = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_FLIGHT_ORIGIN));
            var radius = tag.getDouble(TAG_FLIGHT_RADIUS);
            var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY,
                new ResourceLocation(tag.getString(TAG_FLIGHT_DIMENSION)));
            return new FlightAbility(true, timeLeft, dimension, origin, radius);
        }
        return FlightAbility.deny();
    }

    @Override
    public FrozenColorizer getColorizer(Player player) {
        return FrozenColorizer.deserialize(player.getPersistentData().getCompound(TAG_COLOR));
    }

    @Override
    public Sentinel getSentinel(Player player) {
        CompoundTag tag = player.getPersistentData();
        var exists = tag.getBoolean(TAG_SENTINEL_EXISTS);
        if (!exists) {
            return Sentinel.none();
        }
        var extendsRange = tag.getBoolean(TAG_SENTINEL_GREATER);
        var position = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_SENTINEL_POSITION));
        var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            new ResourceLocation(tag.getString(TAG_SENTINEL_DIMENSION)));

        return new Sentinel(true, extendsRange, position, dimension);
    }

    @Override
    public CastingHarness getHarness(ServerPlayer player, InteractionHand hand) {
        var ctx = new CastingContext(player, hand);
        return CastingHarness.DeserializeFromNBT(player.getPersistentData().getCompound(TAG_HARNESS), ctx);
    }

    @Override
    public List<ResolvedPattern> getPatterns(ServerPlayer player) {
        ListTag patternsTag = player.getPersistentData().getList(TAG_PATTERNS, Tag.TAG_COMPOUND);

        List<ResolvedPattern> patterns = new ArrayList<>(patternsTag.size());

        for (int i = 0; i < patternsTag.size(); i++) {
            patterns.add(ResolvedPattern.DeserializeFromNBT(patternsTag.getCompound(i)));
        }
        return patterns;
    }

    @Override
    public void clearCastingData(ServerPlayer player) {
        player.getPersistentData().remove(TAG_HARNESS);
        player.getPersistentData().remove(TAG_PATTERNS);
    }

    @Override
    public @Nullable
    ManaHolder findManaHolder(ItemStack stack) {
        var maybeCap = stack.getCapability(HexCapabilities.MANA).resolve();
        return maybeCap.orElse(null);
    }

    @Override
    public @Nullable
    DataHolder findDataHolder(ItemStack stack) {
        var maybeCap = stack.getCapability(HexCapabilities.DATUM).resolve();
        return maybeCap.orElse(null);
    }

    @Override
    public @Nullable
    HexHolder findHexHolder(ItemStack stack) {
        var maybeCap = stack.getCapability(HexCapabilities.STORED_HEX).resolve();
        return maybeCap.orElse(null);
    }

    @Override
    public boolean isColorizer(ItemStack stack) {
        return stack.getCapability(HexCapabilities.COLOR).isPresent();
    }

    @Override
    public int getRawColor(FrozenColorizer colorizer, float time, Vec3 position) {
        var maybeColorizer = colorizer.item().getCapability(HexCapabilities.COLOR).resolve();
        if (maybeColorizer.isPresent()) {
            Colorizer col = maybeColorizer.get();
            return col.color(colorizer.owner(), time, position);
        }

        return 0xff_ff00dc; // missing color
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer target, IMessage packet) {
        ForgePacketHandler.getNetwork().send(PacketDistributor.PLAYER.with(() -> target), packet);
    }

    @Override
    public void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet) {
        ForgePacketHandler.getNetwork().send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
            pos.x, pos.y, pos.z, radius * radius, dimension.dimension()
        )), packet);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func,
        Block... blocks) {
        return BlockEntityType.Builder.of(func::apply, blocks).build(null);
    }

    @Override
    public boolean tryPlaceFluid(Level level, InteractionHand hand, BlockPos pos, ItemStack stack, Fluid fluid) {
        return FluidUtil.tryPlaceFluid(null, level, hand, pos, stack, new FluidStack(
            fluid, FluidAttributes.BUCKET_VOLUME)) != FluidActionResult.FAILURE;
    }

    @Override
    public ResourceLocation getID(Block block) {
        return block.getRegistryName();
    }

    @Override
    public ResourceLocation getID(VillagerProfession profession) {
        return profession.getRegistryName();
    }

    @Override
    public Ingredient getUnsealedIngredient(ItemStack stack) {
        return ForgeUnsealedIngredient.of(stack);
    }

    private static CreativeModeTab TAB = null;

    @Override
    public CreativeModeTab getTab() {
        if (TAB == null) {
            TAB = new CreativeModeTab(HexAPI.MOD_ID) {
                @Override
                public ItemStack makeIcon() {
                    return HexItems.tabIcon();
                }

                @Override
                public void fillItemList(NonNullList<ItemStack> p_40778_) {
                    super.fillItemList(p_40778_);
                }
            };
        }

        return TAB;
    }

    @Override
    public boolean isCorrectTierForDrops(Tier tier, BlockState bs) {
        return !bs.requiresCorrectToolForDrops() || TierSortingRegistry.isCorrectTierForDrops(tier, bs);
    }

    @Override
    public Item.Properties addEquipSlotFabric(EquipmentSlot slot) {
        return new Item.Properties();
    }

    public static final String TAG_BRAINSWEPT = "hexcasting:brainswept";
    public static final String TAG_SENTINEL_EXISTS = "hexcasting:sentinel_exists";
    public static final String TAG_SENTINEL_GREATER = "hexcasting:sentinel_extends_range";
    public static final String TAG_SENTINEL_POSITION = "hexcasting:sentinel_position";
    public static final String TAG_SENTINEL_DIMENSION = "hexcasting:sentinel_dimension";

    public static final String TAG_COLOR = "hexcasting:colorizer";

    public static final String TAG_FLIGHT_ALLOWED = "hexcasting:flight_allowed";
    public static final String TAG_FLIGHT_TIME = "hexcasting:flight_time";
    public static final String TAG_FLIGHT_ORIGIN = "hexcasting:flight_origin";
    public static final String TAG_FLIGHT_DIMENSION = "hexcasting:flight_dimension";
    public static final String TAG_FLIGHT_RADIUS = "hexcasting:flight_radius";

    public static final String TAG_HARNESS = "hexcasting:spell_harness";
    public static final String TAG_PATTERNS = "hexcasting:spell_patterns";
}
