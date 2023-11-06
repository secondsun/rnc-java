package dev.secondsun.rnc.test

import dev.secondsun.rnc.Main
import dev.secondsun.rnc.dev.secondsun.rnc.RncDecompress
import dev.secondsun.rnc.dev.secondsun.rnc.RncHeader
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
    fun rncParseHeader() {

        val expected = RncHeader(
            sig = "RNC",
            method = 1,
            uncompressedSize = 342,
            compressedSize = 0x7B,
            uncompressedChecksum = 0x5af6,
            compressedChecksum = 0x4876,
            leeway = 0,
            packChunks = 1u
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
        IntStream.rangeClosed(1, 6562)
            .parallel()
            .mapToObj { index ->
                val formattedIndex = if (index < 1000) String.format("%03d", index) else String.format("%4d", index)
                val fileprefix = "bad_apple_$formattedIndex"
                println(fileprefix)
                val outWidth = 256;
                val outHeight = 192;
                val file = Main::class.java.getResourceAsStream("/$fileprefix.png")
                val palette = byteArrayOf(0x00, 0x44, 0x88.toByte(), 0xff.toByte())
                val colorModel = IndexColorModel(
                    2, 4,
                    palette, palette, palette
                )
                val outputImage = BufferedImage(outWidth, outHeight, BufferedImage.TYPE_BYTE_INDEXED, colorModel)
                val image = ImageIO.read(file)

                val resultingImage: Image = image.getScaledInstance(outWidth, outHeight, Image.SCALE_DEFAULT)
                val scaledImage = BufferedImage(outWidth, outHeight, BufferedImage.TYPE_INT_RGB)
                scaledImage.graphics.drawImage(resultingImage, 0, 0, null)
                for (xOut in 0..<outWidth) {
                    for (yOut in 0..<outHeight) {
                        val y = yOut
                        val x = xOut
                        val originalPixel = scaledImage.getRGB(x, y)
                        val red = originalPixel.shr(16) and 0x00FF
                        val green = originalPixel.shr(8) and 0x00FF
                        val blue = originalPixel and 0x00FF
                        val mono = ((red + green + blue) / (3))

                        outputImage.setRGB(xOut, yOut, mono.shl(16) or mono.shl(8) or mono)
                    }
                }

                val outputImage2 = BufferedImage(outWidth, outHeight / 2, BufferedImage.TYPE_BYTE_INDEXED, colorModel)

                for (xOut in 0..<outWidth) {
                    for (yOut in 0..<outHeight / 2) {
                        val translatedX = xOut - (xOut % 2)
                        val translatedY = yOut * 2 + (xOut % 2)

                        outputImage2.setRGB(xOut, yOut, outputImage.getRGB(translatedX, translatedY))
                    }
                }
                return@mapToObj outputImage2
            }
            .forEachOrdered { outputImage2 ->
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