package at.petrak.hexcasting.xplat

import at.petrak.hexcasting.api.addldata.ADHexHolder
import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.addldata.ADMediaHolder
import at.petrak.hexcasting.api.addldata.ADVariantItem
import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.pigment.ColorProvider
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.player.AltioraAbility
import at.petrak.hexcasting.api.player.FlightAbility
import at.petrak.hexcasting.api.player.Sentinel
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.common.msgs.IMessage
import at.petrak.hexcasting.interop.pehkui.PehkuiInterop
import com.google.common.base.Suppliers
import com.mojang.serialization.Lifecycle
import net.minecraft.core.BlockPos
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.Vec3
import java.util.function.BiFunction
import java.util.function.Supplier

/** A dummy instance of [IXplatAbstractions] to be used in tests. All methods of this implementation throw unconditionally.
  * @throws IllegalStateException always, no matter what
  */
internal class DummyXplatAbstractions: IXplatAbstractions {
    companion object {
        /** must call .get() at least once before constructing iotas */
        val forbiddenMagics: Supplier<Unit> = Suppliers.memoize {
            try { Bootstrap.bootStrap() } catch (_: Throwable) {}
            HexIotaTypes.registerTypes { t, id -> Registry.register(HexIotaTypes.REGISTRY, id, t) }
        }
    }
    override fun platform(): Platform? = error("Found use of DummyXplatAbstractions.")
    override fun isModPresent(id: String): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun isPhysicalClient(): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun initPlatformSpecific() = error("Found use of DummyXplatAbstractions.")
    override fun sendPacketToPlayer(target: ServerPlayer, packet: IMessage) = error("Found use of DummyXplatAbstractions.")
    override fun sendPacketNear(pos: Vec3, radius: Double, dimension: ServerLevel, packet: IMessage) = error("Found use of DummyXplatAbstractions.")
    override fun sendPacketTracking(entity: Entity, packet: IMessage) = error("Found use of DummyXplatAbstractions.")
    override fun toVanillaClientboundPacket(message: IMessage): Packet<ClientGamePacketListener> = error("Found use of DummyXplatAbstractions.")
    override fun setBrainsweepAddlData(mob: Mob) = error("Found use of DummyXplatAbstractions.")
    override fun isBrainswept(mob: Mob): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun setPigment(target: Player, colorizer: FrozenPigment?): FrozenPigment? = error("Found use of DummyXplatAbstractions.")
    override fun setSentinel(target: Player, sentinel: Sentinel?) = error("Found use of DummyXplatAbstractions.")
    override fun setFlight(target: ServerPlayer, flight: FlightAbility?) = error("Found use of DummyXplatAbstractions.")
    override fun setAltiora(target: Player, altiora: AltioraAbility?) = error("Found use of DummyXplatAbstractions.")
    override fun setStaffcastImage(target: ServerPlayer, image: CastingImage?) = error("Found use of DummyXplatAbstractions.")
    override fun setPatterns(target: ServerPlayer, patterns: List<ResolvedPattern?>) = error("Found use of DummyXplatAbstractions.")
    override fun getFlight(player: ServerPlayer): FlightAbility? = error("Found use of DummyXplatAbstractions.")
    override fun getAltiora(player: Player): AltioraAbility? = error("Found use of DummyXplatAbstractions.")
    override fun getPigment(player: Player): FrozenPigment = error("Found use of DummyXplatAbstractions.")
    override fun getSentinel(player: Player): Sentinel? = error("Found use of DummyXplatAbstractions.")
    override fun getStaffcastVM(player: ServerPlayer, hand: InteractionHand): CastingVM = error("Found use of DummyXplatAbstractions.")
    override fun getPatternsSavedInUi(player: ServerPlayer): List<ResolvedPattern> = error("Found use of DummyXplatAbstractions.")
    override fun clearCastingData(player: ServerPlayer) = error("Found use of DummyXplatAbstractions.")
    override fun findMediaHolder(stack: ItemStack): ADMediaHolder? = error("Found use of DummyXplatAbstractions.")
    override fun findMediaHolder(player: ServerPlayer): ADMediaHolder? = error("Found use of DummyXplatAbstractions.")
    override fun findDataHolder(stack: ItemStack): ADIotaHolder? = error("Found use of DummyXplatAbstractions.")
    override fun findDataHolder(entity: Entity): ADIotaHolder? = error("Found use of DummyXplatAbstractions.")
    override fun findHexHolder(stack: ItemStack): ADHexHolder? = error("Found use of DummyXplatAbstractions.")
    override fun findVariantHolder(stack: ItemStack): ADVariantItem? = error("Found use of DummyXplatAbstractions.")
    override fun isPigment(stack: ItemStack) = error("Found use of DummyXplatAbstractions.")
    override fun getColorProvider(pigment: FrozenPigment): ColorProvider? = error("Found use of DummyXplatAbstractions.")
    override fun addEquipSlotFabric(slot: EquipmentSlot): Item.Properties? = error("Found use of DummyXplatAbstractions.")
    override fun <T : BlockEntity?> createBlockEntityType(func: BiFunction<BlockPos?, BlockState?, T?>, vararg blocks: Block?): BlockEntityType<T?>? = error("Found use of DummyXplatAbstractions.")
    override fun tryPlaceFluid(level: Level, hand: InteractionHand, pos: BlockPos, fluid: Fluid): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun drainAllFluid(level: Level, pos: BlockPos): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun isCorrectTierForDrops(tier: Tier, bs: BlockState): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun getUnsealedIngredient(stack: ItemStack): Ingredient? = error("Found use of DummyXplatAbstractions.")
    override fun tags(): IXplatTags? = error("Found use of DummyXplatAbstractions.")
    override fun isShearsCondition(): LootItemCondition.Builder? = error("Found use of DummyXplatAbstractions.")
    override fun getModName(namespace: String): String? = error("Found use of DummyXplatAbstractions.")
    override fun getActionRegistry(): Registry<ActionRegistryEntry?>? = error("Found use of DummyXplatAbstractions.")
    override fun getSpecialHandlerRegistry(): Registry<SpecialHandler.Factory<*>?>? = error("Found use of DummyXplatAbstractions.")
    override fun getIotaTypeRegistry(): Registry<IotaType<*>> = MappedRegistry(HexRegistries.IOTA_TYPE, Lifecycle.stable())
    override fun getArithmeticRegistry(): Registry<Arithmetic?>? = error("Found use of DummyXplatAbstractions.")
    override fun getContinuationTypeRegistry(): Registry<ContinuationFrame.Type<*>?>? = error("Found use of DummyXplatAbstractions.")
    override fun getEvalSoundRegistry(): Registry<EvalSound?>? = error("Found use of DummyXplatAbstractions.")
    override fun isBreakingAllowed(world: ServerLevel, pos: BlockPos, state: BlockState, player: Player?): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun isPlacingAllowed(world: ServerLevel, pos: BlockPos, blockStack: ItemStack, player: Player?): Boolean = error("Found use of DummyXplatAbstractions.")
    override fun getPehkuiApi(): PehkuiInterop.ApiAbstraction? = error("Found use of DummyXplatAbstractions.")
}
