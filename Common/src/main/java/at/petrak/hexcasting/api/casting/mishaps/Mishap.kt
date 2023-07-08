package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.ktxt.*
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

abstract class Mishap : Throwable() {
    /** Mishaps spray half-red, half-this-color. */
    abstract fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment

    open fun particleSpray(ctx: CastingEnvironment): ParticleSpray {
        return ParticleSpray(
            ctx.mishapSprayPos().add(0.0, 0.2, 0.0),
            Vec3(0.0, 2.0, 0.0),
            0.2, Math.PI / 4, 40)
    }

    open fun resolutionType(ctx: CastingEnvironment): ResolvedPatternType = ResolvedPatternType.ERRORED

    /**
     * Execute the actual effect, not any sfx.
     *
     * You can also mess up the stack with this.
     */
    abstract fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>)

    protected abstract fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component?

    fun executeReturnStack(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>): List<Iota> {
        execute(ctx, errorCtx, stack)
        return stack
    }

    /**
     * Every error message should be prefixed with the name of the action...
     */
    fun errorMessageWithName(ctx: CastingEnvironment, errorCtx: Context): Component? {
        return if (errorCtx.name != null) {
            "hexcasting.mishap".asTranslatedComponent(errorCtx.name, this.errorMessage(ctx, errorCtx) ?: return null)
        } else {
            this.errorMessage(ctx, errorCtx)
        }
    }

    // Useful helper functions

    protected fun dyeColor(color: DyeColor): FrozenPigment =
        FrozenPigment(
            ItemStack(HexItems.DYE_PIGMENTS[color]!!),
            Util.NIL_UUID
        )

    protected fun error(stub: String, vararg args: Any): Component =
        "hexcasting.mishap.$stub".asTranslatedComponent(*args)

    protected fun actionName(name: Component?): Component =
        name ?: "hexcasting.spell.null".asTranslatedComponent.lightPurple

    protected fun blockAtPos(ctx: CastingEnvironment, pos: BlockPos): Component {
        return ctx.world.getBlockState(pos).block.name
    }

    data class Context(val pattern: HexPattern?, val name: Component?)

    companion object {
        @JvmStatic
        fun trulyHurt(entity: LivingEntity, source: DamageSource, amount: Float) {
            entity.setHurtWithStamp(source, entity.level().gameTime)

            val targetHealth = entity.health - amount
            if (entity.invulnerableTime > 10) {
                val lastHurt = entity.lastHurt
                if (lastHurt < amount)
                    entity.invulnerableTime = 0
                else
                    entity.lastHurt -= amount
            }
            if (!entity.hurt(source, amount) &&
                !entity.isInvulnerableTo(source) &&
                !entity.level().isClientSide &&
                !entity.isDeadOrDying
            ) {

                // Ok, if you REALLY don't want to play nice...
                entity.health = targetHealth
                entity.markHurt()

                if (entity.isDeadOrDying) {
                    if (!entity.checkTotemDeathProtection(source)) {
                        val sound = entity.deathSoundAccessor
                        if (sound != null) {
                            entity.playSound(sound, entity.soundVolumeAccessor, entity.voicePitch)
                        }
                        entity.die(source)
                    }
                } else {
                    entity.playHurtSound(source)
                }
            }
        }
    }
}
