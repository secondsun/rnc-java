package dev.secondsun.rnc.test

import dev.secondsun.rnc.Main
import dev.secondsun.rnc.dev.secondsun.rnc.RncDecompress
import dev.secondsun.rnc.dev.secondsun.rnc.RncHeader
import dev.secondsun.rnc.dev.secondsun.rnc.RncUnpack
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.stream.IntStream
import javax.imageio.ImageIO
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class EncDecTest {

    @Test
    fun rncUnpack() {
        val compressed = with(File("/text.bin")){this.readBytes()}
        val uncompressed = RncUnpack.unpack(compressed);
        assertEquals(0x0253f680, uncompressed.size)
    }

    @Test
    fun rncParseHeader() {

        val expected = RncHeader(
            sig = "RNC",
            method = 1,
            uncompressedSize = 342,
            compressedSize = 0x7B,
            uncompressedChecksum = 0x5af6,
            compressedChecksum = 0x4876,
            leeway = 0,
            packChunks = 1
        )

        val bytes = with(EncDecTest::class.java.getResourceAsStream("/text.bin")){this.readBytes()}
        val header = RncHeader.from(bytes)
        assertEquals(expected, header)
    }

    @Test
    fun rncUnpackMethod1() {

        val expected = with(File("c:\\Users\\secon\\Projects\\rnc-java\\target\\out.bin")){this.readBytes()}
        val bytes = with(File("c:\\Users\\secon\\Projects\\rnc-java\\target\\out.bin.rnc")){this.readBytes()}
        val uncompressed = RncDecompress.decompress(bytes)

        assertArrayEquals(expected, uncompressed)
    }


    @Test
    @Ignore
            /**
             * This test will scale, down sample, bitplane, and tar the bad apple raw frames into a binary stream
             * The output frames expect to be displayed a a mosaic and interleaves rows together
             */
    fun shrinkBadApple() {
        val byte = ByteArrayOutputStream()
        //for (index in 1..6562) {
        IntStream.rangeClosed(1, 250)
            .parallel()
            .map()
            .forEachOrdered()
        for (index in 1..250) {




            //ImageIO.write(outputImage2, "PNG", File("$fileprefix-out.png"))

            var byte1 = 0
            var byte2 = 0

            for (y in 0..<outputImage2.height) {
                for (x in 0..<outputImage2.width) {
                    val pixel = outputImage2.getRGB(x, y) and 0x00FF
                    when (pixel) {
                        0x00 -> {
                            byte1 = byte1.shr(1)
                            byte2 = byte2.shr(1)
                        }

                        0x44 -> {
                            byte1 = byte1.shr(1)
                            byte2 = byte2.shr(1)
                            byte2 = byte2 + 1
                        }

                        0x88 -> {
                            byte1 = byte1.shr(1)
                            byte1 = byte1 + 1
                            byte2 = byte2.shr(1)
                        }

                        0xff -> {
                            byte1 = byte1.shr(1)
                            byte1 = byte1 + 1

                            byte2 = byte2.shr(1)
                            byte2 = byte2 + 1
                        }

                        else -> {
                            fail("illegal color")
                        }
                    }
                    if (x % 8 == 0 && x != 0) {
                        byte.write(byte1)
                        byte.write(byte2)
                        byte1 = 0
                        byte2 = 0
                    }

                }
            }

        }
        with(File("target/out.bin")) {
            with(FileOutputStream(this)) {
                this.write(byte.toByteArray())
            }
        }
    }

}