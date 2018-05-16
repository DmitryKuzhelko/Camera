package kuzhelko.dmitry.mycamera.ui.camera.presenter

import kuzhelko.dmitry.mycamera.data.Storage
import kuzhelko.dmitry.mycamera.router.Router
import kuzhelko.dmitry.mycamera.ui.base.presenter.BasePresenterImpl
import kuzhelko.dmitry.mycamera.ui.camera.view.CameraView
import javax.inject.Inject

class CameraPresenterImpl<V : CameraView>
@Inject constructor(private val storage: Storage,
                    private val router: Router): BasePresenterImpl<V>(), CameraPresenter<V> {

    override fun openGallery() {
        router.openGallery()
    }
}