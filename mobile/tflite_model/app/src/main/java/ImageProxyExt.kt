package com.example.mlai_proj

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}

fun ImageProxy.toBitmap(): Bitmap {
    if (format != ImageFormat.YUV_420_888) error("Unsupported format: $format")

    val yBytes = planes[0].buffer.toByteArray()
    val uBytes = planes[1].buffer.toByteArray()
    val vBytes = planes[2].buffer.toByteArray()

    val nv21 = ByteArray(yBytes.size + uBytes.size + vBytes.size)
    System.arraycopy(yBytes, 0, nv21, 0, yBytes.size)
    System.arraycopy(vBytes, 0, nv21, yBytes.size, vBytes.size)
    System.arraycopy(uBytes, 0, nv21, yBytes.size + vBytes.size, uBytes.size)

    val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuv.compressToJpeg(Rect(0, 0, width, height), 80, out)
    val jpegBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}
