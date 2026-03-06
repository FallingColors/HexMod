package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.api.addldata.ADHexHolder;
import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.addldata.ADVariantItem;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.api.player.FlightAbility;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.forge.cap.CapSyncers;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.forge.mixin.ForgeAccessorBuiltInRegistries;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.forge.recipe.ForgeUnsealedIngredient;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.interop.pehkui.PehkuiInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatTags;
import at.petrak.hexcasting.xplat.Platform;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class ForgeXplatImpl implements IXplatAbstractions {
    @Override
    public Platform platform() {
        return Platform.FORGE;
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public boolean isModPresent(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public void initPlatformSpecific() {
        if (this.isModPresent(HexInterop.Forge.CURIOS_API_ID)) {
            CuriosApiInterop.init();
        }
    }

//    @Override
//    public double getReachDistance(Player player) {
//        return player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
//    }

    @Override
    public void setBrainsweepAddlData(Mob mob) {
        mob.getPersistentData().putBoolean(TAG_BRAINSWEPT, true);

        if (mob.level() instanceof ServerLevel) {
            PacketDistributor.sendToPlayersTrackingEntity(mob, MsgBrainsweepAck.of(mob));
        }
    }

    @Override
    public void setFlight(ServerPlayer player, FlightAbility flight) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_FLIGHT_ALLOWED, flight != null);
        if (flight != null) {
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
    public void setAltiora(Player player, @Nullable AltioraAbility altiora) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_ALTIORA_ALLOWED, altiora != null);
        if (altiora != null) {
            tag.putInt(TAG_ALTIORA_GRACE, altiora.gracePeriod());
        } else {
            tag.remove(TAG_ALTIORA_ALLOWED);
        }

        // The elytra ability is done with an event on fabric (Caelus provides flight attribute)
        var elytraing = ForgeOptionalMods.getCaelusFlightAttribute();
        if (elytraing != null) {
            var holder = BuiltInRegistries.ATTRIBUTE.getResourceKey(elytraing)
                .map(k -> BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(k))
                .orElse(null);
            if (holder != null) {
                var inst = player.getAttributes().getInstance(holder);
                if (altiora != null) {
                    if (inst.getModifier(ALTIORA_ATTRIBUTE_ID) == null) {
                        inst.addTransientModifier(new AttributeModifier(ALTIORA_ATTRIBUTE_ID, 1.0,
                            AttributeModifier.Operation.ADD_VALUE));
                    }
                } else {
                    inst.removeModifier(ALTIORA_ATTRIBUTE_ID);
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CapSyncers.syncAltiora(serverPlayer);
        }
    }

    @Override
    public @Nullable FrozenPigment setPigment(Player player, @Nullable FrozenPigment pigment) {
        var old = getPigment(player);

        CompoundTag tag = player.getPersistentData();
        if (pigment != null)
            tag.put(TAG_PIGMENT, pigment.serializeToNBT());
        else
            tag.remove(TAG_PIGMENT);

        if (player instanceof ServerPlayer serverPlayer) {
            CapSyncers.syncPigment(serverPlayer);
        }

        return old;
    }

    @Override
    public void setSentinel(Player player, @Nullable Sentinel sentinel) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(TAG_SENTINEL_EXISTS, sentinel != null);
        if (sentinel != null) {
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
    public void setStaffcastImage(ServerPlayer player, @Nullable CastingImage image) {
        player.getPersistentData().put(TAG_VM, image == null ? new CompoundTag() : image.serializeToNbt());
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
            var origin = HexUtils.vecFromNBT(tag.getCompound(TAG_FLIGHT_ORIGIN));
            var radius = tag.getDouble(TAG_FLIGHT_RADIUS);
            var dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse(tag.getString(TAG_FLIGHT_DIMENSION)));
            return new FlightAbility(timeLeft, dimension, origin, radius);
        }
        return null;
    }

    @Override
    public AltioraAbility getAltiora(Player player) {
        CompoundTag tag = player.getPersistentData();
        boolean allowed = tag.getBoolean(TAG_ALTIORA_ALLOWED);
        if (allowed) {
            var grace = tag.getInt(TAG_ALTIORA_GRACE);
            return new AltioraAbility(grace);
        }
        return null;
    }

    @Override
    public FrozenPigment getPigment(Player player) {
        return FrozenPigment.fromNBT(player.getPersistentData().getCompound(TAG_PIGMENT));
    }

    @Override
    public Sentinel getSentinel(Player player) {
        CompoundTag tag = player.getPersistentData();
        var exists = tag.getBoolean(TAG_SENTINEL_EXISTS);
        if (!exists) {
            return null;
        }
        var extendsRange = tag.getBoolean(TAG_SENTINEL_GREATER);
        var position = HexUtils.vecFromNBT(tag.getCompound(TAG_SENTINEL_POSITION));
        var dimension = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.parse(tag.getString(TAG_SENTINEL_DIMENSION)));

        return new Sentinel(extendsRange, position, dimension);
    }

    @Override
    public CastingVM getStaffcastVM(ServerPlayer player, InteractionHand hand) {
        var ctx = new StaffCastEnv(player, hand);
        return new CastingVM(CastingImage.loadFromNbt(player.getPersistentData().getCompound(TAG_VM),
            player.serverLevel()), ctx);
    }

    @Override
    public List<ResolvedPattern> getPatternsSavedInUi(ServerPlayer player) {
        ListTag patternsTag = player.getPersistentData().getList(TAG_PATTERNS, Tag.TAG_COMPOUND);

        List<ResolvedPattern> patterns = new ArrayList<>(patternsTag.size());

        for (int i = 0; i < patternsTag.size(); i++) {
            patterns.add(ResolvedPattern.fromNBT(patternsTag.getCompound(i)));
        }
        return patterns;
    }

    @Override
    public void clearCastingData(ServerPlayer player) {
        player.getPersistentData().remove(TAG_VM);
        player.getPersistentData().remove(TAG_PATTERNS);
    }

    @Override
    public @Nullable
    ADMediaHolder findMediaHolder(ItemStack stack) {
        return stack.getCapability(HexCapabilities.MEDIA);
    }

    @Override
    public @Nullable ADMediaHolder findMediaHolder(ServerPlayer player) {
        // Media is only on item stacks; player aggregates from inventory via MediaHelper.scanPlayerForMediaStuff
        return null;
    }

    @Override
    public @Nullable
    ADIotaHolder findDataHolder(ItemStack stack) {
        return stack.getCapability(HexCapabilities.IOTA);
    }

    @Override
    public @Nullable ADIotaHolder findDataHolder(Entity entity) {
        return entity.getCapability(HexCapabilities.IOTA_ENTITY);
    }

    @Override
    public @Nullable
    ADHexHolder findHexHolder(ItemStack stack) {
        return stack.getCapability(HexCapabilities.STORED_HEX);
    }

    @Override
    public @Nullable ADVariantItem findVariantHolder(ItemStack stack) {
        return stack.getCapability(HexCapabilities.VARIANT_ITEM);
    }

    @Override
    public boolean isPigment(ItemStack stack) {
        return stack.getCapability(HexCapabilities.COLOR) != null;
    }

    @Override
    public ColorProvider getColorProvider(FrozenPigment pigment) {
        var cap = pigment.item().getCapability(HexCapabilities.COLOR);
        if (cap != null) {
            return cap.provideColor(pigment.owner());
        }
        return ColorProvider.MISSING;
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer target, IMessage packet) {
        PacketDistributor.sendToPlayer(target, packet);
    }

    @Override
    public void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet) {
        PacketDistributor.sendToPlayersNear(dimension, null, pos.x, pos.y, pos.z, radius, packet);
    }

    @Override
    public void sendPacketTracking(Entity entity, IMessage packet) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
    }

    @Override
    public Packet<ClientGamePacketListener> toVanillaClientboundPacket(IMessage message) {
        // ClientboundCustomPayloadPacket implements Packet<ClientCommonPacketListener>
        // which extends ClientGamePacketListener for play phase
        return (Packet<ClientGamePacketListener>) (Object) new ClientboundCustomPayloadPacket(message);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func,
        Block... blocks) {
        return BlockEntityType.Builder.of(func::apply, blocks).build(null);
    }

    @Override
    public boolean tryPlaceFluid(Level level, InteractionHand hand, BlockPos pos, Fluid fluid) {
        var handler = FluidUtil.getFluidHandler(level, pos, Direction.UP).orElse(null);
        return handler != null &&
            handler.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), EXECUTE) > 0;
    }

    @Override
    public boolean drainAllFluid(Level level, BlockPos pos) {
        var handler = FluidUtil.getFluidHandler(level, pos, Direction.UP).orElse(null);
        if (handler != null) {
            boolean any = false;
            IFluidHandler pool = handler;
            for (int i = 0; i < pool.getTanks(); i++) {
                if (!pool.drain(pool.getFluidInTank(i), EXECUTE).isEmpty()) {
                    any = true;
                }
            }
            return any;
        }
        return false;
    }

    @Override
    public Ingredient getUnsealedIngredient(ItemStack stack) {
        return ForgeUnsealedIngredient.of(stack).toVanilla();
    }

    private static List<ItemStack> harvestStacks(Item... items) {
        return Stream.of(items).map(ItemStack::new).toList();
    }

    private static final List<List<ItemStack>> HARVEST_TOOLS_BY_LEVEL = List.of(
        harvestStacks(Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL),
        harvestStacks(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE, Items.STONE_SHOVEL),
        harvestStacks(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SHOVEL),
        harvestStacks(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SHOVEL),
        harvestStacks(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE, Items.NETHERITE_SHOVEL)
    );

    @Override
    public boolean isCorrectTierForDrops(Tier tier, BlockState bs) {
        if (!bs.requiresCorrectToolForDrops()) return true;
        // TierSortingRegistry removed in NeoForge 21.1; use tool check like Fabric
        int level = HexConfig.server().opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort();
        int idx = Math.min(Math.max(level, 0), HARVEST_TOOLS_BY_LEVEL.size() - 1);
        for (var tool : HARVEST_TOOLS_BY_LEVEL.get(idx)) {
            if (tool.isCorrectToolForDrops(bs)) return true;
        }
        return false;
    }

    @Override
    public Item.Properties addEquipSlotFabric(EquipmentSlot slot) {
        return new Item.Properties();
    }

    private static final IXplatTags TAGS = new IXplatTags() {
        @Override
        public TagKey<Item> amethystDust() {
            return HexTags.Items.create(ResourceLocation.fromNamespaceAndPath("forge", "dusts/amethyst"));
        }

        @Override
        public TagKey<Item> gems() {
            return HexTags.Items.create(ResourceLocation.fromNamespaceAndPath("forge", "gems"));
        }
    };

    @Override
    public IXplatTags tags() {
        return TAGS;
    }

    @Override
    public LootItemCondition.Builder isShearsCondition() {
        return CanItemPerformAbility.canItemPerformAbility(ItemAbilities.SHEARS_DIG);
    }

    @Override
    public String getModName(String namespace) {
        if (namespace.equals("c")) {
            return "Common";
        }
        Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
        if (container.isPresent()) {
            return container.get().getModInfo().getDisplayName();
        }
        return namespace;
    }

    private static final Supplier<Registry<ActionRegistryEntry>> ACTION_REGISTRY = Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerSimple(
                HexRegistries.ACTION, null)
    );
    private static final Supplier<Registry<SpecialHandler.Factory<?>>> SPECIAL_HANDLER_REGISTRY =
        Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerSimple(
                HexRegistries.SPECIAL_HANDLER, null)
        );
    private static final Supplier<Registry<IotaType<?>>> IOTA_TYPE_REGISTRY = Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerDefaulted(
                HexRegistries.IOTA_TYPE,
                modLoc("null").toString(), registry -> HexIotaTypes.NULL)
    );
    private static final Supplier<Registry<Arithmetic>> ARITHMETIC_REGISTRY = Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerSimple(
                HexRegistries.ARITHMETIC, null)
    );
    private static final Supplier<Registry<ContinuationFrame.Type<?>>> CONTINUATION_TYPE_REGISTRY = Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerDefaulted(
                    HexRegistries.CONTINUATION_TYPE,
                    modLoc("end").toString(), registry -> HexContinuationTypes.END)
    );
    private static final Supplier<Registry<EvalSound>> EVAL_SOUND_REGISTRY = Suppliers.memoize(() ->
            ForgeAccessorBuiltInRegistries.hex$registerDefaulted(
                HexRegistries.EVAL_SOUND,
                modLoc("nothing").toString(), registry -> HexEvalSounds.NOTHING)
    );

    @Override
    public Registry<ActionRegistryEntry> getActionRegistry() {
        return ACTION_REGISTRY.get();
    }

    @Override
    public Registry<SpecialHandler.Factory<?>> getSpecialHandlerRegistry() {
        return SPECIAL_HANDLER_REGISTRY.get();
    }

    @Override
    public Registry<IotaType<?>> getIotaTypeRegistry() {
        return IOTA_TYPE_REGISTRY.get();
    }

    @Override
    public Registry<Arithmetic> getArithmeticRegistry() {
        return ARITHMETIC_REGISTRY.get();
    }

    @Override
    public Registry<ContinuationFrame.Type<?>> getContinuationTypeRegistry() {
        return CONTINUATION_TYPE_REGISTRY.get();
    }

    @Override
    public Registry<EvalSound> getEvalSoundRegistry() {
        return EVAL_SOUND_REGISTRY.get();
    }

    @Override
    public boolean isBreakingAllowed(ServerLevel world, BlockPos pos, BlockState state, @Nullable Player player) {
        if (player == null)
            player = FakePlayerFactory.get(world, HEXCASTING);
        var evt = new BlockEvent.BreakEvent(world, pos, state, player);
        NeoForge.EVENT_BUS.post(evt);
        return !evt.isCanceled();
    }

    @Override
    public boolean isPlacingAllowed(ServerLevel world, BlockPos pos, ItemStack blockStack, @Nullable Player player) {
        if (player == null)
            player = FakePlayerFactory.get(world, HEXCASTING);
        ItemStack cached = player.getMainHandItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, blockStack.copy());
        var hitVec = new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, true);
        var evt = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(
            player, InteractionHand.MAIN_HAND, pos, hitVec);
        NeoForge.EVENT_BUS.post(evt);
        player.setItemInHand(InteractionHand.MAIN_HAND, cached);
        return !evt.isCanceled();
    }

    // it's literally the EXACT SAME on fabric aaa
    private static PehkuiInterop.ApiAbstraction PEHKUI_API = null;

    @Override
    public PehkuiInterop.ApiAbstraction getPehkuiApi() {
        if (!this.isModPresent(HexInterop.PEHKUI_ID)) {
            throw new IllegalArgumentException("cannot get the pehkui api without pehkui");
        }

        if (PEHKUI_API == null) {
            PEHKUI_API = new PehkuiInterop.ApiAbstraction() {
                @Override
                public float getScale(Entity e) {
                    return ForgeOptionalMods.getPehkuiScale(e);
                }

                @Override
                public void setScale(Entity e, float scale) {
                    ForgeOptionalMods.setPehkuiScale(e, scale);
                }
            };
        }
        return PEHKUI_API;
    }

    public static final String TAG_BRAINSWEPT = "hexcasting:brainswept";
    public static final String TAG_SENTINEL_EXISTS = "hexcasting:sentinel_exists";
    public static final String TAG_SENTINEL_GREATER = "hexcasting:sentinel_extends_range";
    public static final String TAG_SENTINEL_POSITION = "hexcasting:sentinel_position";
    public static final String TAG_SENTINEL_DIMENSION = "hexcasting:sentinel_dimension";

    public static final String TAG_PIGMENT = "hexcasting:pigment";

    public static final String TAG_FLIGHT_ALLOWED = "hexcasting:flight_allowed";
    public static final String TAG_FLIGHT_TIME = "hexcasting:flight_time";
    public static final String TAG_FLIGHT_ORIGIN = "hexcasting:flight_origin";
    public static final String TAG_FLIGHT_DIMENSION = "hexcasting:flight_dimension";
    public static final String TAG_FLIGHT_RADIUS = "hexcasting:flight_radius";

    public static final String TAG_ALTIORA_ALLOWED = "hexcasting:altiora_allowed";
    public static final String TAG_ALTIORA_GRACE = "hexcasting:altiora_grace_period";

    public static final net.minecraft.resources.ResourceLocation ALTIORA_ATTRIBUTE_ID =
        modLoc("altiora");

    public static final String TAG_VM = "hexcasting:spell_harness";
    public static final String TAG_PATTERNS = "hexcasting:spell_patterns";
}
