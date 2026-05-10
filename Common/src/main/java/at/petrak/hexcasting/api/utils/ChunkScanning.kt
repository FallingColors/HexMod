package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.api.HexAPI
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkStatus
import net.minecraft.world.level.chunk.ImposterProtoChunk

/**
 * This is a helper class to efficiently scan chunks in ways Minecraft did not intend for. This is for only reading chunks, not writing
 */
class ChunkScanning(var level: ServerLevel) {
    var chunks: Long2ObjectLinkedOpenHashMap<ImposterProtoChunk> = Long2ObjectLinkedOpenHashMap()

    /**
     *  This attempts to cache a chunk to the local [chunks]
     * @param ChunkPos the chunk to try to cache
     * @return If the function could cache the chunk or not
     */
    fun cacheChunk(chunk: ChunkPos): Boolean {
        val chunkLong = chunk.toLong()
        // We have the chunk already, so we can skip it
        if (chunks.contains(chunkLong)){
            return true
        }
        val future = level.chunkSource.getChunkFuture(chunk.x,chunk.z, ChunkStatus.EMPTY,true).get()
        if (future.left().isPresent){
            chunks.put(chunkLong, future.left().get() as ImposterProtoChunk)
            return true
        }
        HexAPI.LOGGER.warn("Failed to get chunk at {}!",chunk)
        return false
    }

    fun cacheChunk(chunk: Long): Boolean{
        return cacheChunk(ChunkPos(chunk))
    }

    fun getBlock(blockPos: BlockPos): BlockState? {
        val chunkPos = ChunkPos(blockPos).toLong()
        if (!cacheChunk(chunkPos)){
            return null
        }
        return chunks.get(chunkPos).getBlockState(blockPos)
    }

    fun getBlockEntity(blockPos: BlockPos): BlockEntity? {
        val chunkPos = ChunkPos(blockPos).toLong()
        if (!cacheChunk(chunkPos)){
            return null
        }
        return chunks.get(chunkPos).getBlockEntity(blockPos)
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