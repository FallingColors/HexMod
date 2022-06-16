package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.ktxt.lastHurt
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

abstract class Mishap : Throwable() {
    /** Mishaps spray half-red, half-this-color. */
    abstract fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer

    open fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray(ctx.position.add(0.0, 0.2, 0.0), Vec3(0.0, 2.0, 0.0), 0.2, Math.PI / 4, 40)
    }

    open fun resolutionType(ctx: CastingContext): ResolvedPatternType = ResolvedPatternType.ERRORED

    /**
     * Execute the actual effect, not any sfx.
     *
     * You can also mess up the stack with this.
     */
    abstract fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>)

    abstract fun errorMessage(ctx: CastingContext, errorCtx: Context): Component

    // Useful helper functions

    protected fun dyeColor(color: DyeColor): FrozenColorizer =
        FrozenColorizer(
            ItemStack(HexItems.DYE_COLORIZERS[color]!!),
            Util.NIL_UUID
        )

    protected fun error(stub: String, vararg args: Any): Component =
        "hexcasting.mishap.$stub".asTranslatedComponent(*args)

    protected fun actionName(action: ResourceLocation?): Component =
        "hexcasting.spell.${action ?: "unknown"}".asTranslatedComponent.lightPurple

    protected fun yeetHeldItemsTowards(ctx: CastingContext, targetPos: Vec3) {
        // Knock the player's items out of their hands
        val items = mutableListOf<ItemStack>()
        for (hand in InteractionHand.values()) {
            if (hand != ctx.castingHand || ctx.caster.getItemInHand(hand).`is`(HexItemTags.STAVES)) {
                items.add(ctx.caster.getItemInHand(hand).copy())
                ctx.caster.setItemInHand(hand, ItemStack.EMPTY)
            }
        }

        val delta = targetPos.subtract(ctx.position).normalize().scale(0.5)

        for (item in items) {
            yeetItem(item, ctx, delta)
        }
    }

    protected fun yeetHeldItem(ctx: CastingContext, hand: InteractionHand) {
        val item = ctx.caster.getItemInHand(hand).copy()
        ctx.caster.setItemInHand(hand, ItemStack.EMPTY)

        val delta = ctx.caster.lookAngle.scale(0.5)
        yeetItem(item, ctx, delta)
    }

    protected fun yeetItem(stack: ItemStack, ctx: CastingContext, delta: Vec3) {
        val entity = ItemEntity(
            ctx.world,
            ctx.position.x, ctx.position.y, ctx.position.z,
            stack,
            delta.x + (Math.random() - 0.5) * 0.1,
            delta.y + (Math.random() - 0.5) * 0.1,
            delta.z + (Math.random() - 0.5) * 0.1
        )
        entity.setPickUpDelay(40)
        ctx.world.addWithUUID(entity)
    }

    protected fun blockAtPos(ctx: CastingContext, pos: BlockPos): Component {
        return ctx.world.getBlockState(pos).block.name
    }

    data class Context(val pattern: HexPattern, val action: ResourceLocation?)

    companion object {
        fun trulyHurt(entity: LivingEntity, source: DamageSource, amount: Float) {
            if (entity.invulnerableTime > 10) {
                val lastHurt = entity.lastHurt
                if (lastHurt < amount)
                    entity.invulnerableTime = 0
                else
                    entity.lastHurt -= amount
            }
            entity.hurt(source, amount)
        }
    }
}
