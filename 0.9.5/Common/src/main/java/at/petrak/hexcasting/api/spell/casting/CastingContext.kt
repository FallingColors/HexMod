package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.spell.mishaps.MishapEvalTooDeep
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import at.petrak.hexcasting.api.utils.otherHand
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate
import kotlin.math.min

/**
 * Transient info about the moment the spell started being cast.
 */
data class CastingContext(
    val caster: ServerPlayer,
    val castingHand: InteractionHand,
    val spellCircle: SpellCircleContext? = null
) {
    constructor(caster: ServerPlayer, castingHand: InteractionHand) : this(caster, castingHand, null)

    private var depth: Int = 0

    val world: ServerLevel get() = caster.getLevel()
    val otherHand: InteractionHand get() = otherHand(this.castingHand)
    val position: Vec3 get() = caster.position()

    private val entitiesGivenMotion = mutableSetOf<Entity>()

    inline fun getHeldItemToOperateOn(acceptItemIf: (ItemStack) -> Boolean): Pair<ItemStack, InteractionHand> {
        val handItem = caster.getItemInHand(otherHand)
        if (!acceptItemIf(handItem)) {
            val castingItem = caster.getItemInHand(castingHand)
            if (acceptItemIf(castingItem)) {
                return castingItem to castingHand
            }
        }
        return handItem to otherHand
    }

    /**
     * Throws if we get too deep.
     */
    fun incDepth() {
        this.depth++
        val maxAllowedDepth = HexConfig.server().maxRecurseDepth()
        if (this.depth > maxAllowedDepth) {
            throw MishapEvalTooDeep()
        }
    }

    /**
     * Check to make sure a vec is in world.
     */
    fun assertVecInWorld(vec: Vec3) {
        if (!isVecInWorld(vec))
            throw MishapLocationTooFarAway(vec, "out_of_world")
    }

    /**
     * Check to make sure a vec is in range.
     */
    fun assertVecInRange(vec: Vec3) {
        if (!isVecInRange(vec)) throw MishapLocationTooFarAway(vec)
        assertVecInWorld(vec)
    }

    /**
     * Check to make sure an entity is in range. Will not mishap for players.
     */
    fun assertEntityInRange(entity: Entity) {
        if (entity !is Player && !isEntityInRange(entity)) throw MishapEntityTooFarAway(entity)
    }

    fun hasBeenGivenMotion(target: Entity): Boolean {
        return entitiesGivenMotion.contains(target)
    }

    fun isVecInWorld(vec: Vec3) =
        world.isInWorldBounds(BlockPos(vec)) && world.worldBorder.isWithinBounds(vec.x, vec.z, 0.5)

    fun isVecInRange(vec: Vec3): Boolean {
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(caster)
        if (sentinel.hasSentinel
            && sentinel.extendsRange
            && world.dimension() == sentinel.dimension
            && vec.distanceToSqr(sentinel.position) < Operator.MAX_DISTANCE_FROM_SENTINEL * Operator.MAX_DISTANCE_FROM_SENTINEL
        )
            return true


        if (this.spellCircle != null) {
            // we use the eye position cause thats where the caster gets their "position" from
            val range = this.caster.bbHeight
            if (this.spellCircle.activatorAlwaysInRange && vec.distanceToSqr(this.caster.eyePosition) <= range * range)
                return true
            return this.spellCircle.aabb.contains(vec)
        }

        return vec.distanceToSqr(this.caster.eyePosition) <= Operator.MAX_DISTANCE * Operator.MAX_DISTANCE
    }

    fun isEntityInWorld(entity: Entity) = isVecInWorld(entity.position())

    fun isEntityInRange(entity: Entity): Boolean {
        if (this.spellCircle != null && this.spellCircle.activatorAlwaysInRange && this.caster == entity)
            return true
        return isVecInRange(entity.position())
    }

    fun canEditBlockAt(pos: BlockPos): Boolean {
        return this.isVecInRange(Vec3.atCenterOf(pos))
                && this.caster.gameMode.gameModeForPlayer != GameType.ADVENTURE
                && this.world.mayInteract(this.caster, pos)
    }

    /**
     * Return the slot from which to take blocks and items.
     */
    // https://wiki.vg/Inventory is WRONG
    // slots 0-8 are the hotbar
    // for what purpose i cannot imagine
    // http://redditpublic.com/images/b/b2/Items_slot_number.png looks right
    // and offhand is 150 Inventory.java:464
    fun getOperativeSlot(stackOK: Predicate<ItemStack>): ItemStack? {
        val operable = DiscoveryHandlers.collectOperableSlots(this)

        for (stack in operable) {
            if (stackOK.test(stack)) {
                return stack
            }
        }
        return null
    }

    /**
     * Remove the given count of the specified item from somewhere in the inventory, favoring slots not in the hotbar.
     * Return whether the withdrawal was successful.
     */
    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
    fun withdrawItem(item: ItemStack, count: Int, actuallyRemove: Boolean): Boolean {
        if (this.caster.isCreative) return true

        val operativeItem = item.copy()

        // TODO: withdraw from ender chest given a specific ender charm?
        val stacksToExamine = DiscoveryHandlers.collectItemSlots(this)

        fun matches(stack: ItemStack): Boolean =
            !stack.isEmpty && ItemStack.isSameItemSameTags(operativeItem, stack)

        val presentCount = stacksToExamine.fold(0) { acc, stack ->
            acc + if (matches(stack)) stack.count else 0
        }
        if (presentCount < count) return false

        // now that we know we have enough items, if we don't need to remove anything we're through.
        if (!actuallyRemove) return true

        var remaining = count
        for (stack in stacksToExamine) {
            if (matches(stack)) {
                val toWithdraw = min(stack.count, remaining)
                stack.shrink(toWithdraw)

                remaining -= toWithdraw
                if (remaining <= 0) {
                    return true
                }
            }
        }
        throw RuntimeException("unreachable")
    }

    fun markEntityAsMotionAdded(entity: Entity) {
        this.entitiesGivenMotion.add(entity)
    }

    val canOvercast: Boolean
        get() {
            val adv = this.world.server.advancements.getAdvancement(modLoc("y_u_no_cast_angy"))
            val advs = this.caster.advancements
            return advs.getOrStartProgress(adv!!).isDone
        }

    val isCasterEnlightened: Boolean
        get() {
            val adv = this.world.server.advancements.getAdvancement(modLoc("enlightenment"))
            val advs = this.caster.advancements
            return advs.getOrStartProgress(adv!!).isDone
        }

    val debugPatterns: Boolean by lazy {
        !DiscoveryHandlers.findDebugItem(this.caster, ItemCreativeUnlocker.DISPLAY_PATTERNS).isEmpty
    }

    companion object {
        init {
            DiscoveryHandlers.addItemSlotDiscoverer {
                val inv = it.caster.inventory
                inv.items.toMutableList().apply { removeAt(inv.selected) }.asReversed().toMutableList().apply {
                    addAll(inv.offhand)
                    add(inv.getSelected())
                }
            }

            DiscoveryHandlers.addOperativeSlotDiscoverer {
                val slots = mutableListOf<ItemStack>()
                val anchorSlot = if (it.castingHand == InteractionHand.MAIN_HAND) (it.caster.inventory.selected + 1) % 9 else 0

                slots.add(it.caster.getItemInHand(it.otherHand))
                for (delta in 0 until 9) {
                    val slot = (anchorSlot + delta) % 9
                    slots.add(it.caster.inventory.getItem(slot))
                }
                slots
            }
        }
    }
}
