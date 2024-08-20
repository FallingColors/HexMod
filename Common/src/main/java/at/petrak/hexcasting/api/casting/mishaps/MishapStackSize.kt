package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.lib.HexDamageTypes
import net.minecraft.world.item.DyeColor

class MishapStackSize() : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = env.castingEntity
        if (caster != null) {
            // FIXME: handle null caster case
            trulyHurt(caster, caster.damageSources().source(HexDamageTypes.STACK_SIZE), 2f)
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("stack_size")
}
