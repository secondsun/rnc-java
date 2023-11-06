package dev.secondsun.rnc.dev.secondsun.rnc

import java.lang.Exception


class RncDecompress {
    companion object {

        private val twoWordBuffer = object {
            var count = 0
            var index = RncHeader.SIZE
            var word = 0
            var inputBytes:ByteArray = byteArrayOf(0);

            fun read(length:Int):Int {

                var out = 0
                var bitflag = 1;
                for (star in 0..<length) {
                    if (count == 0) {
                        if ((index) >= inputBytes.size) {
                            word = 0
                            index += 2
                            count = 16
                        } else if (index +1 >= inputBytes.size) {
                            val lower = inputBytes[index].toInt()
                            val upper = 0
                            word = ((upper and 0x0FF) shl (8)) or ((lower) and 0x0FF) and 0x0FFFF
                            index += 2
                            count = 16
                        } else {
                        val lower = inputBytes[index].toInt()
                        val upper = inputBytes[index + 1].toInt()
                        word = ((upper and 0x0FF) shl (8)) or ((lower) and 0x0FF) and 0x0FFFF
                        index += 2
                        count = 16
                    }
                    }

                    if ((word and 1) != 0) {
                        out = out or bitflag
                    }

                    word = word shr 1
                    bitflag = bitflag shl 1
                    count -= 1

                }
                return out.toUShort().toInt()

            }

            fun peek(length:Int):Int {

                var peekCount = count
                var peekWord = word
                var peekIndex = index

                var out = 0
                var bitflag = 1;
                for (star in 0..<length) {
                    if (peekCount == 0) {
                        if ((peekIndex) >= inputBytes.size) {
                            peekWord = 0
                            peekIndex += 2
                            peekCount = 16
                        } else if (peekIndex +1 >= inputBytes.size) {
                            val lower = inputBytes[peekIndex].toInt()
                            val upper = 0
                            peekWord = ((upper and 0x0FF) shl (8)) or ((lower) and 0x0FF) and 0x0FFFF
                            peekIndex += 2
                            peekCount = 16
                        } else {
                            val lower = inputBytes[peekIndex].toInt()
                            val upper = inputBytes[peekIndex + 1].toInt()
                            peekWord = ((upper and 0x0FF) shl (8)) or ((lower) and 0x0FF) and 0x0FFFF
                            peekIndex += 2
                            peekCount = 16
                        }
                    }

                    if ((peekWord and 1) != 0) {
                        out = out or bitflag
                    }

                    peekWord = peekWord shr 1
                    bitflag = bitflag shl 1
                    peekCount -= 1

                }
                return  out.toUShort().toInt()

            }


            fun readSourceByte(): Int {
                if (index >= inputBytes.size) {
                    throw ArrayIndexOutOfBoundsException()
                }
                return inputBytes[index++].toInt()
            }

        }
        fun decompress(bytes: ByteArray): ByteArray {
            val rncHeader = RncHeader.from(bytes)
            twoWordBuffer.inputBytes = bytes
            twoWordBuffer.read(2)
            val output = ByteArray(rncHeader.uncompressedSize)
            var outIndex = 0;
            while (outIndex < rncHeader.uncompressedSize) {
                val literalTable = HuffTree();
                val lengthTable = HuffTree();
                val positionTable = HuffTree();
                println("lengthTable ${lengthTable}")
                println("positionTable ${positionTable}")
                var subchunks = twoWordBuffer.read(16)
                println("Subchunks ${subchunks}")
                while (subchunks-- > 0) {
                    var literalLength = decodeNext(literalTable)
                    println ("reading ${literalLength} bytes")
                    if (literalLength > 0u) {
                        while (literalLength-- > 0u) {
                            output[outIndex] = twoWordBuffer.readSourceByte().toByte()
                            outIndex++
                        }
                    }
                    println ("decoding subchunks ${subchunks} in chunk ${outIndex}/${rncHeader.uncompressedSize}")
                    if (subchunks > 0 ) {
                        val offset = decodeNext(lengthTable) + 1u
                        var count :Int = (decodeNext(positionTable) + 2u).toInt()
                        println("Offset ${offset}")
                        println("Count ${count}")
                        while ((count--) !=0) {
                            output[outIndex] = output[outIndex - offset.toInt()]
                            outIndex++
                        }
                    }
                }


            }
            //println(output.joinToString(","))
            return output
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun decodeNext(literalTable: RncDecompress.HuffTree): UInt {
            var i = 0
            while (true) {
                val node = literalTable.nodes[i]
                if (node.bitdepth != 0 && node.encoding == (twoWordBuffer.peek(16) and ((1 shl node.bitdepth) - 1))) {
                    //We peeked the buffer and found a huffmatch, advance the buffer
                    twoWordBuffer.read(node.bitdepth)
                    val value = if (i < 2) i.toUInt() else (twoWordBuffer.read(i - 1) or (1 shl (i - 1))).toUInt()
                    //println("found node[${i}] ${node} with value ${value}")
                    return value
                }
                i++;
            }
        }
    }

    private class Node(val bitdepth :Int) {
        var encoding = 0;


        override fun toString(): String {
            return "Node(bitdepth=$bitdepth, encoding=$encoding)"
        }


    }

    private class HuffTree {

        val nodeCount = twoWordBuffer.read(5)
        val nodes = mutableListOf<Node>()
        init {
            for (index in 0..<nodeCount) {
                nodes.add(index, Node( twoWordBuffer.read(4)))
            }

            var div = 0x80000000u
            var value = 0u;
            var bits_count = 1;

            while (bits_count <= 16) {
                var i = 0
                while (true) {
                    if (i >= nodeCount) {
                        bits_count++
                        div = div shr 1
                        break
                    }
                    if (nodes[i].bitdepth == bits_count) {
                        nodes[i].encoding = inverse_bits(value / div, bits_count)
                        value += div
                    }
                    i++
                }
            }


        }

        private fun inverse_bits(pValue: UInt, pBitsCount: Int): Int {
            var bitsCount = pBitsCount
            var value = pValue

            var i = 0
            //printf("Inverse Start %x, %x\n", value, count);
            //printf("Inverse Start %x, %x\n", value, count);
            while (bitsCount-- != 0) {
                i = i shl 1
                if (value and 1u != 0u) {
                    i = i or 1
                }
                value = value shr 1
            }
            //printf("Inverse End %x\n", i);
            //printf("Inverse End %x\n", i);
            return i
        }

        override fun toString(): String {
            return "HuffTree(nodeCount=$nodeCount, nodes=${nodes})"
        }


    }

}
