package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.common

interface CameraSource {
    fun start()
    fun stop()
    fun takePhoto()
    fun flipCamera()
}