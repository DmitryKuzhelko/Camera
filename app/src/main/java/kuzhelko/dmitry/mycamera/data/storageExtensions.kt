package kuzhelko.dmitry.mycamera.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kuzhelko.dmitry.mycamera.ALBUM_NAME_EXTERNAL
import kuzhelko.dmitry.mycamera.ALBUM_NAME_INTERNAL
import kuzhelko.dmitry.mycamera.TIMESTAMP_PATTERN
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun getExternalStorage(): File = Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME_EXTERNAL")

fun Context.getInternalStorage(): File = File(this.filesDir, ALBUM_NAME_INTERNAL)

fun getAvailableStorage(context: Context): File {
    val storageDir = when (Environment.getExternalStorageState()) {
        Environment.MEDIA_MOUNTED -> getExternalStorage()
        else -> context.getInternalStorage()
    }
    if (!storageDir.exists()) {
        storageDir.mkdir()
    }
    return storageDir
}

fun writeFileToAvailableStorage(bitmap: Bitmap, context: Context) {
    var fos: FileOutputStream? = null
    val imageFile = File(getAvailableStorage(context), createNameByTimestamp())
    try {
        fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    refreshGallery(context, imageFile.absolutePath)
}

fun refreshGallery(context: Context, imagePath: String) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val imageFile = File(imagePath)
    val contentUri = Uri.fromFile(imageFile)
    mediaScanIntent.data = contentUri
    context.sendBroadcast(mediaScanIntent)
}

fun File.getAllPhotos(): ArrayList<String> {
    val listPaths: ArrayList<String> = ArrayList()
    if (this.listFiles().isNotEmpty() && this.listFiles() != null) {
        for (photoFile in this.listFiles().reversed()) {
            listPaths.add(photoFile.absolutePath)
        }
    }
    return listPaths
}

fun File.getPathById(id: Int) = if (this.getAllPhotos().isNotEmpty()) {
    this.getAllPhotos()[id]
} else {
    null
}

fun File.deletePhoto(id: Int) {
    if (this.getAllPhotos().isNotEmpty()) {
        File(this.getAllPhotos()[id]).delete()
    }
}

fun createNameByTimestamp(): String {
    val timeStamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(Date())
    return "JPEG_$timeStamp.jpg"
}

fun File.checkStorageState() = this.getAllPhotos().isEmpty()

fun File.getLastPhoto() = this.getAllPhotos()[0]

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}