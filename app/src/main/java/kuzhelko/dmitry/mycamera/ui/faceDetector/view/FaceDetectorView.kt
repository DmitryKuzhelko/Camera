package kuzhelko.dmitry.mycamera.ui.faceDetector.view

import kuzhelko.dmitry.mycamera.ui.base.view.BaseView

interface FaceDetectorView : BaseView {
    fun detectFaces(path: String)
}