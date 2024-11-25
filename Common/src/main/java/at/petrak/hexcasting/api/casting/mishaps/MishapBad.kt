package at.petrak.hexcasting.api.casting.mishaps

import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

// a silly stupid wrapper for throwing arbitrary badness mishaps (the ones that have a .of(thing, stub) constructor.
class MishapBad {
    companion object {
        @JvmStatic
        fun of(thing: Any, stub: String): Mishap {
            return when(thing){
                is Either<*, *> -> thing.map({l -> of(l, stub)}, {r -> of(r, stub)})
                is Entity -> MishapBadEntity.of(thing, stub)
                is BlockPos -> MishapBadBlock.of(thing, stub)
                is Vec3 -> MishapBadLocation(thing, stub)
                is ItemStack -> MishapBadOffhandItem.of(thing, stub)
                else -> throw IllegalArgumentException()
            }
        }
    }
}