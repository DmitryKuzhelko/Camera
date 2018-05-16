package kuzhelko.dmitry.mycamera.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import kuzhelko.dmitry.mycamera.IMAGE_PATH
import kuzhelko.dmitry.mycamera.IMAGE_TYPE
import kuzhelko.dmitry.mycamera.SHARE_IMAGE
import kuzhelko.dmitry.mycamera.ui.faceDetector.view.FaceDetectorActivity
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryActivity
import java.io.File

class RouterImpl(private val context: Context) : Router {

    override fun openGallery() {
        context.startActivity(Intent(context, GalleryActivity::class.java))
    }

    override fun openFaceDetector(path: String) {
        context.startActivity(Intent(context, FaceDetectorActivity::class.java).apply {
            putExtra(IMAGE_PATH, path)
        })
    }

    override fun sharePhoto(imagePath: String) {
        val imageFile = File(imagePath)
        val uriToImage = Uri.fromFile(imageFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = IMAGE_TYPE
            putExtra(Intent.EXTRA_STREAM, uriToImage)
        }
        context.startActivity(Intent.createChooser(shareIntent, SHARE_IMAGE))
    }
}
