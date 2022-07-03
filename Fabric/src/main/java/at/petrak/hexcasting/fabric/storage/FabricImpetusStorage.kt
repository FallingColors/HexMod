package at.petrak.hexcasting.fabric.storage

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.common.lib.HexBlocks
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.world.item.ItemStack

@Suppress("UnstableApiUsage")
class FabricImpetusStorage(val impetus: BlockEntityAbstractImpetus) : SingleSlotStorage<ItemVariant> {
    companion object {
        fun registerStorage() {
            ItemStorage.SIDED.registerForBlocks({ _, _, _, blockEntity, _ ->
                (blockEntity as? BlockEntityAbstractImpetus)?.let(::FabricImpetusStorage)
            }, HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_STOREDPLAYER)
        }
    }

    override fun insert(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val stackCount = maxAmount / 64
        val remainder = (maxAmount % 64).toInt()
        var manaToTake = impetus.remainingManaCapacity()
        var itemsConsumed = 0L

        fun insertStack(stack: ItemStack, transaction: TransactionContext) {
            val copied = stack.copy()
            val extractable = impetus.extractManaFromItem(stack, false)
            manaToTake -= extractable
            val taken = 64 - stack.count
            itemsConsumed += taken.toLong()
            copied.count = taken

            if (taken > 0) {
                transaction.addOuterCloseCallback {
                    if (it.wasCommitted()) {
                        impetus.insertMana(copied)
                    }
                }
            }
        }
        for (i in 0 until stackCount) {
            val stack = resource.toStack(64)
            insertStack(stack, transaction)
            if (manaToTake <= 0) {
                return itemsConsumed
            }
        }
        if (remainder > 0) {
            val remainderStack = resource.toStack(remainder)
            insertStack(remainderStack, transaction)
        }
        return itemsConsumed
    }

    override fun supportsExtraction(): Boolean = false

    override fun extract(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long = 0

    override fun isResourceBlank(): Boolean = true

    override fun getResource(): ItemVariant = ItemVariant.blank()

    override fun getAmount(): Long = 0

    override fun getCapacity(): Long = 64
}
