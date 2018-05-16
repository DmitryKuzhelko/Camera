package kuzhelko.dmitry.mycamera.ui.gallery.utils

import android.content.Context
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import dagger.Module
import dagger.Provides
import kuzhelko.dmitry.mycamera.data.StorageImpl
import kuzhelko.dmitry.mycamera.router.Router
import kuzhelko.dmitry.mycamera.router.RouterImpl
import kuzhelko.dmitry.mycamera.ui.gallery.utils.adapter.SlideShowAdapter
import kuzhelko.dmitry.mycamera.ui.gallery.presenter.GalleryPresenter
import kuzhelko.dmitry.mycamera.ui.gallery.presenter.GalleryPresenterImpl
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryActivity
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryView
import javax.inject.Singleton

@Module
class GalleryActivityModule {

    @Singleton
    @Provides
    fun provideGalleryPresenter(storage: StorageImpl, router: Router): GalleryPresenter<*> = GalleryPresenterImpl<GalleryView>(storage, router)

    @Provides
    fun provideRouter(context: GalleryActivity): Router = RouterImpl(context)

    @Provides
    fun provideSlideShowAdapter(context: Context) = SlideShowAdapter(context)

    @Singleton
    @Provides
    fun provideQRCodeDetector(context: GalleryActivity): BarcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
}