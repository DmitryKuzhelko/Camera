package kuzhelko.dmitry.mycamera.router

interface Router {

    fun openGallery()

    fun openFaceDetector(path: String)

    fun sharePhoto(path: String)
}