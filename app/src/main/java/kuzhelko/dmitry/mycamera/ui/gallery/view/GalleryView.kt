package kuzhelko.dmitry.mycamera.ui.gallery.view

import kuzhelko.dmitry.mycamera.ui.base.view.BaseView

interface GalleryView : BaseView {

    fun sharePhoto()

    fun deletePhoto()

    fun showPhotos()

    fun updateAdapter(photos: ArrayList<String>)

    fun showEmptyScreen()

    fun hideEmptyScreen()
}