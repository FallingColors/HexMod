package at.petrak.hexcasting.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.ADHexHolder;
import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.api.player.FlightAbility;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.interop.pehkui.PehkuiInterop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * more like IHexplatAbstracts lmaooooooo
 */
public interface IXplatAbstractions {
    Platform platform();

    boolean isModPresent(String id);

    boolean isPhysicalClient();

    void initPlatformSpecific();

    void sendPacketToPlayer(ServerPlayer target, IMessage packet);

    void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet);

    // https://github.com/VazkiiMods/Botania/blob/13b7bcd9cbb6b1a418b0afe455662d29b46f1a7f/Xplat/src/main/java/vazkii/botania/xplat/IXplatAbstractions.java#L157
    Packet<?> toVanillaClientboundPacket(IMessage message);

    double getReachDistance(Player player);

    // Things that used to be caps

    /**
     * Doesn't actually knock out its AI or anything anymore, just sets caps/ccs
     */
    // heheheheh addled data
    void setBrainsweepAddlData(Mob mob);

    boolean isBrainswept(Mob mob);

    void setColorizer(Player target, FrozenPigment colorizer);

    void setSentinel(Player target, @Nullable Sentinel sentinel);

    void setFlight(ServerPlayer target, @Nullable FlightAbility flight);

    void setAltiora(Player target, @Nullable AltioraAbility altiora);

    void setStaffcastImage(ServerPlayer target, @Nullable CastingImage image);

    void setPatterns(ServerPlayer target, List<ResolvedPattern> patterns);

    @Nullable FlightAbility getFlight(ServerPlayer player);

    @Nullable AltioraAbility getAltiora(Player player);

    FrozenPigment getColorizer(Player player);

    @Nullable Sentinel getSentinel(Player player);

    CastingVM getStaffcastVM(ServerPlayer player, InteractionHand hand);

    List<ResolvedPattern> getPatternsSavedInUi(ServerPlayer player);

    void clearCastingData(ServerPlayer player);

    @Nullable
    ADMediaHolder findMediaHolder(ItemStack stack);

    @Nullable
    ADMediaHolder findMediaHolder(ServerPlayer player);

    @Nullable
    ADIotaHolder findDataHolder(ItemStack stack);

    @Nullable
    ADIotaHolder findDataHolder(Entity entity);

    @Nullable
    ADHexHolder findHexHolder(ItemStack stack);

    // coooollooorrrs

    boolean isColorizer(ItemStack stack);

    ColorProvider getColorProvider(FrozenPigment colorizer);

    // Items

    /**
     * No-op on forge (use a SoftImplement)
     */
    Item.Properties addEquipSlotFabric(EquipmentSlot slot);

    // Blocks

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func,
        Block... blocks);

    boolean tryPlaceFluid(Level level, InteractionHand hand, BlockPos pos, Fluid fluid);

    boolean drainAllFluid(Level level, BlockPos pos);

    // misc

    CreativeModeTab getTab();

    boolean isCorrectTierForDrops(Tier tier, BlockState bs);

    Ingredient getUnsealedIngredient(ItemStack stack);

    IXplatTags tags();

    LootItemCondition.Builder isShearsCondition();

    String getModName(String namespace);

    /**
     * Registry for actions.
     * <p>
     * There's some internal caching (so we can directly look up signatures in a map, for example)
     * but this registry is the source of truth.
     */
    Registry<ActionRegistryEntry> getActionRegistry();

    Registry<SpecialHandler.Factory<?>> getSpecialHandlerRegistry();

    Registry<IotaType<?>> getIotaTypeRegistry();

    Registry<EvalSound> getEvalSoundRegistry();

    boolean isBreakingAllowed(Level world, BlockPos pos, BlockState state, Player player);

    boolean isPlacingAllowed(Level world, BlockPos pos, ItemStack blockStack, Player player);

    // interop

    PehkuiInterop.ApiAbstraction getPehkuiApi();

    ///

    IXplatAbstractions INSTANCE = find();

    private static IXplatAbstractions find() {
        var providers = ServiceLoader.load(IXplatAbstractions.class).stream().toList();
        if (providers.size() != 1) {
            var names = providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException(
                "There should be exactly one IXplatAbstractions implementation on the classpath. Found: " + names);
        } else {
            var provider = providers.get(0);
            HexAPI.LOGGER.debug("Instantiating xplat impl: " + provider.type().getName());
            return provider.get();
        }
    }

}
