package dev.secondsun.rnc

import dev.secondsun.rnc.dev.secondsun.rnc.RncDecompress
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.File
import javax.swing.*

class Main
@OptIn(ExperimentalStdlibApi::class)
fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val window = JFrame().apply {
        contentPane = JPanel().apply { setSize(256,256);repaint() }
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }

    val bytes = with(File("c:\\Users\\secon\\Projects\\rnc-java\\target\\out.bin.rnc")){this.readBytes()}
    val uncompressed = RncDecompress.decompress(bytes)

    val palette = byteArrayOf(0x00, 0x44, 0x88.toByte(), 0xff.toByte())
    val colorModel = IndexColorModel(
        2, 4,
        palette, palette, palette
    )
    val outputImage = BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, colorModel)
    val frame = Integer.parseInt(JOptionPane.showInputDialog("Which frame"));
    var offset = 0;
    if (frame%2 == 0) {
        offset = 0x4000*frame;
    } else {
        offset = 0x4000*frame + 0x2000;
    }

    for (x in (offset..<offset+0x2000 step 2).withIndex()) {
        val b0 = uncompressed[x.value].toInt()
        val b1 = uncompressed[x.value + 1 ].toInt()


        val p03 = merge(b0,b1)
        println(p03.toHexString(HexFormat.UpperCase ))
        var adjXIdx = x.index * 8

        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 14 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 12 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 10 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 8 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 6 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 4 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 shr 2 and 0x3)
        adjXIdx = adjXIdx++
        outputImage.setRGB(adjXIdx%256,(adjXIdx/256), p03 and 0x3)
    }

    window.contentPane.graphics.drawImage(outputImage,0,0,null)
    window.contentPane.repaint()
    window.size = Dimension(256,256)
    window.repaint()

}

fun merge(high: Int, low: Int): Int {
    var out =0
    for (shift in 0..7) {
        val highTop = (high shl shift) and 0b10000000
        val lowTop = (low   shl shift) and 0b01000000

        val outTop = (highTop or lowTop) shr 6

        out = (out or outTop) shl 2

    }
    return out

}

