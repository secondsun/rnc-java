package dev.secondsun.rnc.dev.secondsun.rnc

class RncUnpack {
    companion object {
        fun unpack(compressed: ByteArray): ByteArray {

            val header = RncHeader.from(compressed)


            val rncStream =RncMethod1Stream(compressed)

            val packChunkCount = header.packChunks
            val output = ByteArray(header.uncompressedSize)

            (0..packChunkCount).forEach {
                val literalDataSize: HuffmanTree = HuffmanTree(rncStream)
                val distanceValues: HuffmanTree = HuffmanTree(rncStream)
                val lengthValues: HuffmanTree = HuffmanTree(rncStream)



            }





            return output
        }
    }

}

class RncMethod1Stream(compressed: ByteArray) {
    var pointer =  RncHeader.SIZE
}


class HuffmanTree(compressed: RncMethod1Stream) {

}
