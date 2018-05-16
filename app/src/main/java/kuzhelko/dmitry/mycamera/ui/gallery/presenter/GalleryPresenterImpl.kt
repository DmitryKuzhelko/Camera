package kuzhelko.dmitry.mycamera.ui.gallery.presenter

import kuzhelko.dmitry.mycamera.data.Storage
import kuzhelko.dmitry.mycamera.router.Router
import kuzhelko.dmitry.mycamera.ui.base.presenter.BasePresenterImpl
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryView
import javax.inject.Inject

class GalleryPresenterImpl<V : GalleryView>
    @Inject constructor(private val storage: Storage,
                        private val router: Router): BasePresenterImpl<V>(), GalleryPresenter<V> {

    override fun getPhotos() {
        val photoPaths = storage.getAllPhotos()

        when (photoPaths.isEmpty()) {
            true -> getView()?.showEmptyScreen()
            false -> {
                getView()?.hideEmptyScreen()
                getView()?.updateAdapter(photoPaths)
            }
        }
    }

    override fun deleteById(id: Int) {
        storage.deletePhoto(id)
        checkStorageState()
    }

    private fun checkStorageState() {
        if (storage.isEmpty()) {
            getView()?.showEmptyScreen()
        }
    }

    override fun sharePhoto(id: Int) {
        val photoPath: String? = storage.getPathById(id)
        if (photoPath != null) {
            router.sharePhoto(photoPath)
        }
    }

    override fun getPathById(id: Int) = storage.getPathById(id)


    override fun detectFaces(id: Int) {
        val photoPath: String? = getPathById(id)
        if (photoPath != null) {
            router.openFaceDetector(photoPath)
        }
    }
}