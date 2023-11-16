package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.DyeColor

class MishapUnenlightened : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.RED)

    override fun resolutionType(ctx: CastingEnvironment) = ResolvedPatternType.INVALID

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.dropHeldItems()
        env.caster?.sendSystemMessage("hexcasting.message.cant_great_spell".asTranslatedComponent, true)

        // add some non-zero level of juice I guess
        val pos = env.mishapSprayPos()
        env.world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 0.7f)

        HexAdvancementTriggers.FAIL_GREAT_SPELL_TRIGGER.trigger(env.caster)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = null
}
