package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera1api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.util.Log
import android.widget.FrameLayout
import kuzhelko.dmitry.mycamera.ROTATE_ANGLE
import kuzhelko.dmitry.mycamera.data.writeFileToAvailableStorage
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.common.CameraSource

@Suppress("DEPRECATION")
class Camera1Api(private val context: Context) : CameraSource {

    private var camera: Camera? = null
    private var numberOfCameras = 0
    private var isFrontCameraActive = false
    private var isBackCameraActive = false
    private var frontCameraId = 0
    private var backCameraId = 0
    private var currentCameraId = 0
    private var surfaceView: FrameLayout? = null


    private companion object {
        const val TAG = "Camera1Api"
    }

    init {
        getCameras()
    }

    fun setPreview(preview: FrameLayout) {
        surfaceView = preview
    }

    private fun getCameras() {
        numberOfCameras = Camera.getNumberOfCameras()
        for (camera in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(camera, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                isFrontCameraActive = true
                frontCameraId = camera
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                isBackCameraActive = true
                backCameraId = camera
            }
        }

        currentCameraId = when (isBackCameraActive) {
            true -> backCameraId
            false -> frontCameraId
        }
    }

    override fun start() {
        if (camera == null) {
            openCamera(currentCameraId)
        }
    }

    override fun stop() {
        stopCameraPreview()
        releaseCamera()
    }

    private fun releaseCamera() {
        camera?.release()
        camera = null
    }

    private fun openCamera(cameraId: Int) {
        currentCameraId = cameraId
        stopCameraPreview()
        releaseCamera()
        try {
            camera = Camera.open(cameraId)
            cameraPreviewChanged()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to open camera. ${t.message}", t)
        }
        initAutoFocus()
    }

    private fun stopCameraPreview() {
        try {
            camera?.stopPreview()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to tried to stop a non-existent preview. ${t.message}", t)
        }
    }

    private fun cameraPreviewChanged() {
        camera?.apply {
            setDisplayOrientation(ROTATE_ANGLE.toInt())
            CameraPreview(context, camera, currentCameraId).apply {
                surfaceView!!.removeAllViews()
                surfaceView!!.addView(this)
            }
        }
    }

    private fun initAutoFocus() {
        camera?.parameters?.supportedFocusModes?.run {
            if (this.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                camera?.parameters = camera?.parameters?.apply {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                }
            }
        }
    }

    override fun takePhoto() {
        camera?.takePicture(null, null, Camera.PictureCallback { bytes, _ ->
            val rotatedBitmap = rotatePicture(ROTATE_ANGLE, bytes)
            writeFileToAvailableStorage(rotatedBitmap, context)
            cameraPreviewChanged()
        })
    }

    private fun rotatePicture(rotateAngle: Float, bytes: ByteArray): Bitmap {
        val photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Matrix().let {
            return when (isFrontCameraActive) {
                true -> {
                    it.postRotate(rotateAngle)
                    Bitmap.createBitmap(photo, 0, 0, photo.width, photo.height, it, true)
                }
                false -> {
                    it.postRotate(-rotateAngle)
                    Bitmap.createBitmap(photo, 0, 0, photo.width, photo.height, it, true)
                }
            }
        }
    }

    override fun flipCamera() {
        if (isFrontCameraActive && currentCameraId == backCameraId) {
            openCamera(frontCameraId)
        } else if (isBackCameraActive && currentCameraId == frontCameraId) {
            openCamera(backCameraId)
        }
    }
}