package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapAlreadyBrainswept
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBrainsweep
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.ktxt.tellWitnessesThatIWasMurdered
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

object OpBrainsweep : SpellAction {
    override val argc = 2

    override val isGreat = true

    // this way you can hear the villager dying more : )
    override fun hasCastingSound(ctx: CastingContext) = false

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val sacrifice = args.getVillager(0, argc)
        val pos = args.getBlockPos(1, argc)
        ctx.assertVecInRange(pos)
        ctx.assertEntityInRange(sacrifice)

        if (Brainsweeping.isBrainswept(sacrifice))
            throw MishapAlreadyBrainswept(sacrifice)

        val state = ctx.world.getBlockState(pos)

        val recman = ctx.world.recipeManager
        val recipes = recman.getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE)
        val recipe = recipes.find { it.matches(state, sacrifice) }
            ?: throw MishapBadBrainsweep(sacrifice, pos)

        return Triple(
            Spell(pos, state, sacrifice, recipe),
            10 * ManaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.cloud(sacrifice.position(), 1.0), ParticleSpray.burst(Vec3.atCenterOf(pos), 0.3, 100))
        )
    }

    private data class Spell(
        val pos: BlockPos,
        val state: BlockState,
        val sacrifice: Villager,
        val recipe: BrainsweepRecipe
    ) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.world.setBlockAndUpdate(pos, BrainsweepRecipe.copyProperties(state, recipe.result))
            Brainsweeping.brainsweep(sacrifice)
            if (HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                sacrifice.tellWitnessesThatIWasMurdered(ctx.caster)
            }

            ctx.world.playSound(null, sacrifice, SoundEvents.VILLAGER_DEATH, SoundSource.AMBIENT, 0.8f, 1f)
            ctx.world.playSound(null, sacrifice, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 0.8f)
        }
    }
}
