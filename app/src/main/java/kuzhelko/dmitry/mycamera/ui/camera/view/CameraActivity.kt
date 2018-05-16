package kuzhelko.dmitry.mycamera.ui.camera.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_camera1api.*
import kotlinx.android.synthetic.main.activity_camera2api.*
import kotlinx.android.synthetic.main.camera_control_board.*
import kuzhelko.dmitry.mycamera.ACTION
import kuzhelko.dmitry.mycamera.FLIP_PHOTO_EVENT
import kuzhelko.dmitry.mycamera.R
import kuzhelko.dmitry.mycamera.TAKE_PHOTO_EVENT
import kuzhelko.dmitry.mycamera.ui.base.view.BaseActivity
import kuzhelko.dmitry.mycamera.ui.camera.presenter.CameraPresenterImpl
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera1api.Camera1Api
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api.Camera2Api
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.common.CameraSource
import javax.inject.Inject

class CameraActivity : BaseActivity(), CameraView {

    @Inject
    lateinit var camera: CameraSource

    @Inject
    lateinit var presenter: CameraPresenterImpl<CameraView>

    private companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.onAttach(this)

        Fabric.with(this, Crashlytics(), Answers())
        sepUpFullScreen()

        when (camera) {
            is Camera1Api -> {
                setContentView(R.layout.activity_camera1api)
                (camera as Camera1Api).setPreview(camera_1api_preview)
            }
            is Camera2Api -> {
                setContentView(R.layout.activity_camera2api)
                (camera as Camera2Api).setTextureView(camera_2api_preview)
            }
        }

        setUpControlBoard()

        val cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        when (cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED) {
            true -> startCamera()
            false -> requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    override fun onPause() {
        stopCamera()
        super.onPause()
    }

    private fun startCamera() {
        camera.start()
    }

    private fun stopCamera() {
        camera.stop()
    }

    private fun setUpControlBoard() {
        takePhoto()
        flipCamera()
        openGallery()
    }

    private fun takePhoto() {
        ivTakePhoto.setOnClickListener {
            camera.takePhoto()
            Answers.getInstance().logCustom(CustomEvent(TAKE_PHOTO_EVENT)
                    .putCustomAttribute(ACTION, "Take photo"))
        }
    }

    private fun flipCamera() {
        ivFlipCamera.setOnClickListener {
            camera.flipCamera()
            Answers.getInstance().logCustom(CustomEvent(FLIP_PHOTO_EVENT)
                    .putCustomAttribute(ACTION, "Flip camera"))
        }
    }

    private fun openGallery() {
        ivGoToGallery.setOnClickListener {
            presenter.openGallery()
        }
    }

    private fun sepUpFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION)
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        presenter.onDetach()
        super.onDestroy()
    }
}