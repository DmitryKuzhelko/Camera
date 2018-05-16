package kuzhelko.dmitry.mycamera.ui.gallery.presenter

import kuzhelko.dmitry.mycamera.ui.base.presenter.BasePresenter
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryView

interface GalleryPresenter<V : GalleryView> : BasePresenter<V> {

    fun getPhotos()

    fun detectFaces(id: Int)

    fun deleteById(id: Int)

    fun getPathById(id: Int): String?

    fun sharePhoto(id: Int)
}