package kuzhelko.dmitry.mycamera.ui.faceDetector.utils.faceDetector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.v7.app.AlertDialog
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import kuzhelko.dmitry.mycamera.CORNER_RADIUS
import kuzhelko.dmitry.mycamera.R
import kuzhelko.dmitry.mycamera.data.showToast
import javax.inject.Inject

class FacesDetector
@Inject constructor(private val context: Context) {

    private var tempCanvas: Canvas? = null
    private var tempBitmap: Bitmap? = null

    private val rectPaint = Paint()

    private val faceDetector: FaceDetector
        get() = initializeFaceDetector()

    private fun initializeFaceDetector(): FaceDetector {
        return FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .build()
    }

    fun getDetectedFaces(bitmap: Bitmap): Bitmap? {
        processImage(bitmap)
        when (faceDetector.isOperational) {
            true -> {
                val frame = Frame
                        .Builder()
                        .setBitmap(bitmap)
                        .build()

                val sparseArray = faceDetector.detect(frame)

                if (sparseArray.size() > 0) {
                    loadDetectedResult(sparseArray)
                } else {
                    showToast(context, context.getString(R.string.nothing_found))
                }

                faceDetector.release()
            }
            false -> {
                AlertDialog.Builder(context)
                        .setMessage(context.getString(R.string.could_not_set_up_detector))
                        .show()
            }
        }
        return tempBitmap
    }


    private fun loadDetectedResult(sparseArray: SparseArray<Face>) {
        for (i in 0 until sparseArray.size()) {
            val face = sparseArray.valueAt(i)

            val left = face.position.x
            val top = face.position.y
            val right = left + face.width
            val bottom = top + face.height

            val rectF = RectF(left, top, right, bottom)
            tempCanvas?.drawRoundRect(rectF, CORNER_RADIUS, CORNER_RADIUS, rectPaint)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        rectPaint.createRectanglePaint()
        tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        tempCanvas = Canvas(tempBitmap).apply {
            drawBitmap(bitmap, 0f, 0f, null)
        }
    }
}