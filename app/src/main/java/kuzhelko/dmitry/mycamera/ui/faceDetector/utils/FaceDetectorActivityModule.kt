package kuzhelko.dmitry.mycamera.ui.faceDetector.utils

import com.google.android.gms.vision.face.FaceDetector
import dagger.Module
import dagger.Provides
import kuzhelko.dmitry.mycamera.router.Router
import kuzhelko.dmitry.mycamera.router.RouterImpl
import kuzhelko.dmitry.mycamera.ui.faceDetector.presenter.FaceDetectorPresenter
import kuzhelko.dmitry.mycamera.ui.faceDetector.presenter.FaceDetectorPresenterImpl
import kuzhelko.dmitry.mycamera.ui.faceDetector.view.FaceDetectorActivity
import kuzhelko.dmitry.mycamera.ui.faceDetector.view.FaceDetectorView
import javax.inject.Singleton

@Module
class FaceDetectorActivityModule {

    @Singleton
    @Provides
    fun provideFaceDetectorPresenter(): FaceDetectorPresenter<*> = FaceDetectorPresenterImpl<FaceDetectorView>()

    @Provides
    fun provideRouter(context: FaceDetectorActivity): Router = RouterImpl(context)

    @Singleton
    @Provides
    fun provideFaceDetector(context: FaceDetectorActivity): FaceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .build()
}