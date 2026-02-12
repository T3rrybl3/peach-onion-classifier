package com.example.mlai_proj

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(
    context: Context,
    assetName: String = "v3_hp1_fp32.tflite"
) {
    private val labels = arrayOf("onion_brown", "onion_purple", "peach", "unknown")
    private val interpreter: Interpreter

    init {
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(loadModelFile(context, assetName), options)
    }

    private fun loadModelFile(context: Context, assetName: String): MappedByteBuffer {
        val fd = context.assets.openFd(assetName)
        FileInputStream(fd.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    fun centreCropSquare(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val side = minOf(w, h)
        val x = (w - side) / 2
        val y = (h - side) / 2
        return Bitmap.createBitmap(src, x, y, side, side)
    }

    fun resizeTo160(src: Bitmap): Bitmap =
        Bitmap.createScaledBitmap(src, 160, 160, true)

    fun predict(bitmap160: Bitmap): Result {
        val input = Array(1) { Array(160) { Array(160) { FloatArray(3) } } }

        val pixels = IntArray(160 * 160)
        bitmap160.getPixels(pixels, 0, 160, 0, 0, 160, 160)

        var idx = 0
        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val p = pixels[idx++]
                val r = ((p shr 16) and 0xFF) / 255.0f
                val g = ((p shr 8) and 0xFF) / 255.0f
                val b = (p and 0xFF) / 255.0f
                input[0][y][x][0] = r
                input[0][y][x][1] = g
                input[0][y][x][2] = b
            }
        }

        val output = Array(1) { FloatArray(4) }
        interpreter.run(input, output)

        val probs = output[0]
        var best = 0
        for (i in 1 until probs.size) if (probs[i] > probs[best]) best = i

        return Result(labels[best], probs[best], probs)
    }

    data class Result(val label: String, val confidence: Float, val probs: FloatArray)
}
