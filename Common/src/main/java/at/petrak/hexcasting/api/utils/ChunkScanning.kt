package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.api.HexAPI
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkStatus

/**
 * This is a helper class to efficiently scan chunks in ways Minecraft did not intend for. This is for only reading chunks, not writing
 */
class ChunkScanning(var level: ServerLevel) {
    var chunks: Long2ObjectLinkedOpenHashMap<ChunkAccess> = Long2ObjectLinkedOpenHashMap()

    /**
     *  This attempts to cache a chunk to the local [chunks]
     * @param ChunkPos the chunk to try to cache
     * @return Either the ChunkAccess gained, or null
     */
    fun cacheChunk(chunk: ChunkPos): ChunkAccess? {
        val chunkLong = chunk.toLong()
        // If we have the chunk already, we can skip fetching it
        val existing = chunks.get(chunkLong)
        if (existing != null){
            return existing
        }
        val future = level.chunkSource.getChunkFuture(chunk.x,chunk.z, ChunkStatus.EMPTY,true).get()
        if (future.left().isPresent){
            val next = future.left().get()
            chunks.put(chunkLong, next)
            return next
        }
        HexAPI.LOGGER.warn("Failed to get chunk at {}!",chunk)
        return null
    }

    fun cacheChunk(chunk: Long): ChunkAccess? {
        return cacheChunk(ChunkPos(chunk))
    }

    fun getBlock(blockPos: BlockPos): BlockState? {
        val chunkPos = ChunkPos(blockPos).toLong()
        return cacheChunk(chunkPos)?.getBlockState(blockPos)
    }

    fun getBlockEntity(blockPos: BlockPos): BlockEntity? {
        val chunkPos = ChunkPos(blockPos).toLong()
        return cacheChunk(chunkPos)?.getBlockEntity(blockPos)
    }

    // Maybe not required, but still not a bad idea to have a Clear method
    fun clearCache(){
        chunks.clear()
    }

    // Might not be needed
    fun containsChunk(chunk: ChunkPos): Boolean{
        return chunks.contains(chunk.toLong())
    }
}