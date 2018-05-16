package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera1api

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import kuzhelko.dmitry.mycamera.ROTATE_ANGLE
import java.io.IOException

@Suppress("DEPRECATION")
class CameraPreview constructor(context: Context, private val camera: Camera?, private val cameraId: Int) : SurfaceView(context), SurfaceHolder.Callback {

    private val surfaceHolder: SurfaceHolder = holder

    init {
        surfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

        try {
            camera?.setPreviewDisplay(holder)
            setCameraDisplayOrientation(context as Activity, cameraId, camera)
            camera?.startPreview()
        } catch (e: IOException) {
            Log.d(ContentValues.TAG, "Error setting camera preview: ${e.message}")
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (surfaceHolder.surface == null) {
            return
        }
        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.setDisplayOrientation(ROTATE_ANGLE.toInt());
            setCameraDisplayOrientation(context as Activity, cameraId, camera)
            camera?.startPreview()

        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "Error starting camera preview: ${e.message}")
        }
    }

    private fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: Camera?) {

        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera?.setDisplayOrientation(result)
    }
}