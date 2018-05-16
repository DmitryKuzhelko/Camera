package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import java.util.*

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun configureTransform(viewWidth: Int, viewHeight: Int, mPreviewSize: Size, context: Context): Matrix {

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val rotation = windowManager.defaultDisplay.rotation
    val matrix = Matrix()
    val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
    val bufferRect = RectF(0f, 0f, mPreviewSize.height.toFloat(), mPreviewSize.width.toFloat())
    val centerX = viewRect.centerX()
    val centerY = viewRect.centerY()
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
        val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize.height,
                viewWidth.toFloat() / mPreviewSize.width)
        matrix.postScale(scale, scale, centerX, centerY)
        matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
    } else if (Surface.ROTATION_180 == rotation) {
        matrix.postRotate(180f, centerX, centerY)
    }
    return matrix
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun chooseOptimalSize(choices: Array<Size>,
                      textureViewWidth: Int,
                      textureViewHeight: Int,
                      maxWidth: Int,
                      maxHeight: Int,
                      aspectRatio: Size): Size {

    // Collect the supported resolutions that are at least as big as the preview Surface
    val bigEnough = ArrayList<Size>()
    // Collect the supported resolutions that are smaller than the preview Surface
    val notBigEnough = ArrayList<Size>()
    val w = aspectRatio.width
    val h = aspectRatio.height
    for (option in choices) {
        if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * h / w) {
            if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                bigEnough.add(option)
            } else {
                notBigEnough.add(option)
            }
        }
    }

    return when {
        bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
        notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
        else -> {
            Log.e("tag", "Couldn't find any suitable preview size")
            choices[0]
        }
    }
}