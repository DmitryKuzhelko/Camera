package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api

import android.annotation.TargetApi
import android.content.Context
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import kuzhelko.dmitry.mycamera.data.writeFileToAvailableStorage
import java.io.IOException

class ImageSaver(private val context: Context, private val image: Image) : Runnable {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        try {
            writeFileToAvailableStorage(photo, context)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}