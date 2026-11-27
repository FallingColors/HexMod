package at.petrak.hexcasting.fabric.storage

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.common.lib.HexBlocks
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.world.item.ItemStack

@Suppress("UnstableApiUsage")
class FabricImpetusStorage private constructor(val impetus: BlockEntityAbstractImpetus) : SingleSlotStorage<ItemVariant> {
    val snapshotter = object: SnapshotParticipant<Long>() {
        override fun createSnapshot() = impetus.media
        override fun readSnapshot(snapshot: Long) { impetus.media = snapshot }
    }

    companion object {
        fun registerStorage() {
            ItemStorage.SIDED.registerForBlocks({ _, _, _, blockEntity, _ ->
                (blockEntity as? BlockEntityAbstractImpetus)?.run {
                    if (xplatField == null) {
                        xplatField = FabricImpetusStorage(this)
                    }
                    xplatField as FabricImpetusStorage
                }
            }, HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_REDSTONE)
        }
    }

    override fun insert(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
        val stackCount = maxAmount / 64
        val remainder = (maxAmount % 64).toInt()
        var itemsConsumed = 0L

        snapshotter.updateSnapshots(transaction) // safe to call even if nothing happens

        fun insertStack(stack: ItemStack) {
            val size = stack.count
            impetus.insertMedia(stack)
            val taken = size - stack.count
            itemsConsumed += taken.toLong()
        }
        for (i in 0 until stackCount) {
            val stack = resource.toStack(64)
            insertStack(stack)
            if (impetus.remainingMediaCapacity() <= 0) {
                return itemsConsumed
            }
        }
        if (remainder > 0) {
            val remainderStack = resource.toStack(remainder)
            insertStack(remainderStack)
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
