package cryptography

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

const val END_OF_MESSAGE = "\u0000\u0000\u0003"

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val input = readln()) {
            "hide" -> hide()
            "show" -> extractMessage()
            "exit" -> {
                println("Bye!"); break
            }

            else -> println("Wrong task: $input")
        }
    }
}

fun extractMessage() {
    val imageFile = println("Input image file:").run { readln() }
    val password = println("Password:").run { readln() }
    val secretImage = ImageIO.read(File(imageFile))
    var i = 0
    var byte = 0
    val message = StringBuilder()
    loop@ for (y in 0 until secretImage.height) {
        for (x in 0 until secretImage.width) {
            byte = byte shl 1
            byte = (secretImage.getRGB(x, y) and 0x01) or byte
            if (++i == 8) {
                i = 0
                message.append(Char(byte))
                if (message.endsWith(END_OF_MESSAGE)) break@loop
                byte = 0
            }
        }
    }
    message.delete(message.length - 3, message.length)
    val decodedChars = message.mapIndexed { ind, it ->
        xorChar(it, password, ind).toChar()
    }
    println("Message:\n${decodedChars.joinToString("")}")
}

fun hide() {
    val inImageFile = println("Input image file:").run { readln() }
    val outImageFile = println("Output image file:").run { readln() }
    val message = println("Message to hide:").run { readln() + END_OF_MESSAGE }
    val password = println("Password:").run { readln() }
    try {
        val inImage = ImageIO.read(File(inImageFile))
        val secretLength = message.length * 8
        if (secretLength > inImage.width * inImage.height) {
            println("The input image is not large enough to hold this message.")
            return
        }
        val encodedBytes = message.mapIndexed { ind, it ->
            if (ind < message.length - 3)
                xorChar(it, password, ind).toByte()
            else
                it.code.toByte()
        }
        insertMessageInImage(encodedBytes, inImage)
        ImageIO.write(inImage, "png", File(outImageFile))
        println("Message saved in $outImageFile image.")
    } catch (_: IOException) {
        println("Can't read input file!")
    }
}

private fun xorChar(it: Char, password: String, ind: Int) =
    it.code.xor(password[ind % password.length].code)

private fun insertMessageInImage(bytesData: List<Byte>, image: BufferedImage) {
    var x = 0
    var y = 0
    for (byte in bytesData) {
        for (r in 1..8) {
            val currentByte = byte.rotateLeft(r).toInt() and 0x1
            val oldRGB = image.getRGB(x, y) and 0xFFFFFFFE.toInt()
            image.setRGB(x, y, oldRGB or currentByte)
            if (++x == image.width) {
                x = 0; y++
            }
        }
    }
}