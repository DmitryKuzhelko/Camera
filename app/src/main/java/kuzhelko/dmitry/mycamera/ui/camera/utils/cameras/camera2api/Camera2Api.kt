package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.common.CameraSource
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Api(private val context: Context) : CameraSource {

    private var cameraType = 0
    private val orientations = SparseIntArray()
    private val inverseOrientations = SparseIntArray()

    private var textureView: AutoFitTextureView? = null
    private var state = STATE_PREVIEW

    init {
        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)

        inverseOrientations.append(Surface.ROTATION_0, 270)
        inverseOrientations.append(Surface.ROTATION_90, 180)
        inverseOrientations.append(Surface.ROTATION_180, 90)
        inverseOrientations.append(Surface.ROTATION_270, 0)
    }

    companion object {
        private const val TAG = "Camera2Api"
        private const val STATE_PREVIEW = 0
        private const val STATE_WAITING_LOCK = 1
        private const val STATE_WAITING_PRECAPTURE = 2
        private const val STATE_WAITING_NON_PRECAPTURE = 3
        private const val STATE_PICTURE_TAKEN = 4
        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080
        private const val REQUEST_CAMERA_PERMISSION = 255
    }

    private var cameraId: String? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var previewSize: Size? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var imageReader: ImageReader? = null

    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    private val cameraOpenCloseLock = Semaphore(1)

    private var flashSupported: Boolean = false
    private var sensorOrientation: Int = 0

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun setTextureView(preview: AutoFitTextureView) {
        textureView = preview
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun takePhoto() {
        try {
            // This is how to tell the camera to lock focus.
            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the lock.
            state = STATE_WAITING_LOCK
            captureSession!!.capture(previewRequestBuilder!!.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun flipCamera() {
        if (cameraType == 0) {
            closeCamera()
            cameraType = 1
            openCamera(textureView!!.width, textureView!!.height)

        } else {
            closeCamera()
            cameraType = 0
            openCamera(textureView!!.width, textureView!!.height)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.getLooper())
    }

    @SuppressLint("NewApi")
    private fun stopBackgroundThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(previewRequestBuilder!!)

            captureSession!!.capture(previewRequestBuilder!!.build(), captureCallback, backgroundHandler)

            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            captureSession!!.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun captureStillPicture() {
            try {

                // This is the CaptureRequest.Builder that we use to take a picture.
                val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.addTarget(imageReader!!.surface)

                // Use the same AE and AF modes as the preview.
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                setAutoFlash(captureBuilder)

                // Orientation
                val rotation = windowManager.defaultDisplay.rotation

                if (cameraType == 1) {
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, inverseOrientations.get(rotation))
                } else {
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation))
                }

                val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                    override fun onCaptureCompleted(session: CameraCaptureSession,
                                                    request: CaptureRequest,
                                                    result: TotalCaptureResult) {
                        unlockFocus()
                        createCameraPreviewSession()
                    }
                }

                captureSession!!.apply {
                    stopRepeating()
                    capture(captureBuilder.build(), captureCallback, null)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
            process(result)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE
            captureSession!!.capture(previewRequestBuilder!!.build(), captureCallback, backgroundHandler)

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun start() {
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera(textureView!!.width, textureView!!.height)
        } else {
            textureView!!.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun stop() {
        closeCamera()
        stopBackgroundThread()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (null != captureSession) {
                captureSession!!.close()
                captureSession = null
            }
            if (null != cameraDevice) {
                cameraDevice!!.close()
                cameraDevice = null
            }
            if (null != imageReader) {
                imageReader!!.close()
                imageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            if (null != textureView || null == previewSize) {
                textureView!!.setTransform(configureTransform(width, height, previewSize!!, context as Activity))
            }
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera(width: Int, height: Int) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            return
        }
        setUpCameraOutputs(width, height)

        if (null != textureView || null == previewSize) {
            textureView!!.setTransform(configureTransform(width, height, previewSize!!, context))
        }

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager!!.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader -> backgroundHandler!!.post(ImageSaver(context, reader.acquireNextImage())) }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpCameraOutputs(width: Int, height: Int) {

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        try {
            cameraId = manager!!.cameraIdList[cameraType]
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // For still image captures, we use the largest available size.
            val largest = Collections.max(
                    Arrays.asList(*map!!.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea())
            imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, /*maxImages*/2).apply {
                setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            }

            val displayRotation = windowManager.defaultDisplay.rotation

            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
            var swappedDimensions = false
            when (displayRotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
                else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
            }

            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            var rotatedPreviewWidth = width
            var rotatedPreviewHeight = height
            var maxPreviewWidth = displaySize.x
            var maxPreviewHeight = displaySize.y

            if (swappedDimensions) {
                rotatedPreviewWidth = height
                rotatedPreviewHeight = width
                maxPreviewWidth = displaySize.y
                maxPreviewHeight = displaySize.x
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT
            }

            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest)

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView!!.setAspectRatio(
                        previewSize!!.width, previewSize!!.height)
            } else {
                textureView!!.setAspectRatio(
                        previewSize!!.height, previewSize!!.width)
            }
            // Check if the flash is supported.
            val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            flashSupported = available ?: false

            return

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release()
            this@Camera2Api.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@Camera2Api.cameraDevice = null
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@Camera2Api.cameraDevice = null
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView!!.surfaceTexture!!

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(Arrays.asList(surface, imageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                                // Flash is automatically enabled when necessary.
                                setAutoFlash(previewRequestBuilder!!)

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder!!.build()
                                //                            updatePreview();
                                captureSession!!.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)

                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(
                                cameraCaptureSession: CameraCaptureSession) {
                        }
                    }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_STATE_FIRED)
        }
    }
}