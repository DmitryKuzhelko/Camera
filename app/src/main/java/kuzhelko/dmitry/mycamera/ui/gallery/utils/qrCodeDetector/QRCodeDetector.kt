package kuzhelko.dmitry.mycamera.ui.gallery.utils.qrCodeDetector

import android.content.Context
import android.graphics.Bitmap
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kuzhelko.dmitry.mycamera.ACTION
import kuzhelko.dmitry.mycamera.DETECT_QR_CODE_EVENT
import kuzhelko.dmitry.mycamera.R
import kuzhelko.dmitry.mycamera.RESULT
import kuzhelko.dmitry.mycamera.data.showToast
import javax.inject.Inject

class QRCodeDetector
@Inject constructor(private val context: Context) {

    private val barcodeDetector: BarcodeDetector
        get() = initializeBarcodeDetector()

    private fun initializeBarcodeDetector(): BarcodeDetector {
        return BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()
    }

    fun detect(bitmap: Bitmap): String {
        when (barcodeDetector.isOperational) {
            true -> {
                val frame = Frame
                        .Builder()
                        .setBitmap(bitmap)
                        .build()

                val sparseArray = barcodeDetector.detect(frame)
                barcodeDetector.release()

                if (sparseArray.size() > 0) {
                    Answers.getInstance().logCustom(CustomEvent(DETECT_QR_CODE_EVENT)
                            .putCustomAttribute(ACTION, "Detect qr code")
                            .putCustomAttribute(RESULT, "Success"))
                    return sparseArray.valueAt(0).rawValue
                }

                Answers.getInstance().logCustom(CustomEvent(DETECT_QR_CODE_EVENT)
                        .putCustomAttribute(ACTION, "Detect qr code")
                        .putCustomAttribute(RESULT, "Failure"))
            }
            false -> showToast(context, context.getString(R.string.could_not_set_up_detector))

        }
        return context.getString(R.string.nothing_found)
    }
}