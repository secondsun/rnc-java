package dev.secondsun.rnc.dev.secondsun.rnc

data class RncHeader(val sig: String,val  method: Int,val  uncompressedSize: Int,val  compressedSize: Int,val  uncompressedChecksum: Int,val  compressedChecksum: Int,val  leeway: Int,val  packChunks: Int) {
    companion object {

        val SIZE : Int = 0x12
        fun from(bytes: ByteArray): RncHeader {
             return RncHeader(
                 sig = String(byteArrayOf(bytes[0],bytes[1],bytes[2])),
                 method = bytes[3].toInt(),
                 uncompressedSize = toInt(bytes[4],bytes[5],bytes[6],bytes[7]),
                 compressedSize = toInt(bytes[8],bytes[9],bytes[10],bytes[11]),
                 uncompressedChecksum = toInt(bytes[12],bytes[13]),
                 compressedChecksum = toInt(bytes[14],bytes[15]),
                 leeway = bytes[16].toInt(),
                 packChunks = bytes[17].toInt()
             )
        }

        private fun toInt(byte: Byte, byte1: Byte, byte2: Byte, byte3: Byte): Int {
            return (
                    byte .toUByte().toUInt().shl(24) or
                    byte1.toUByte().toUInt().shl(16) or
                    byte2.toUByte().toUInt().shl(8) or
                    byte3.toUByte().toUInt()).toInt()
        }

        private fun toInt(byte: Byte, byte1: Byte): Int {
            return (byte.toUByte().toUInt().shl(8) or byte1.toUByte().toUInt()).toInt()
        }
    }
}
