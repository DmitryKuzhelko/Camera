package kuzhelko.dmitry.mycamera.ui.camera.presenter

import kuzhelko.dmitry.mycamera.ui.base.presenter.BasePresenter
import kuzhelko.dmitry.mycamera.ui.camera.view.CameraView

interface CameraPresenter<V : CameraView> : BasePresenter<V> {
    fun openGallery()
}