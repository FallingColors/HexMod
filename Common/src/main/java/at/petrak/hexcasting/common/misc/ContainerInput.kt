package at.petrak.hexcasting.common.misc

import net.minecraft.core.RegistryAccess
import net.minecraft.world.Container
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

abstract class ContainerInput<C : Container>(val container: Container) : RecipeInput {


    override fun getItem(i: Int): ItemStack? {
       return container.getItem(i)
    }

    abstract fun assemble(inv: CraftingContainer, access: RegistryAccess): ItemStack

    override fun size(): Int {
        return container.containerSize
    }
}