package dev.secondsun.rnc.dev.secondsun.rnc

class FrameReader(compressed: ByteArray) {

    companion object {
        val FRAME_SIZE = 0x1800
    }

    var compressedindex = 0;
    var frameDataIndex = 0;
    val header = RncHeader.from(compressed)
    val frameData = ByteArray(2 * FRAME_SIZE)

    fun nextFrame(): ByteArray {
        //uncompress FRAME_SIZE bytes
        //wrap frameDataIndex
        //update framePointer when done
        TODO()
    }


}
