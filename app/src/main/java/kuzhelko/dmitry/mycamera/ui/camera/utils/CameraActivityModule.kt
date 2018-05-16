package kuzhelko.dmitry.mycamera.ui.camera.utils

import android.os.Build
import dagger.Module
import dagger.Provides
import kuzhelko.dmitry.mycamera.data.StorageImpl
import kuzhelko.dmitry.mycamera.router.Router
import kuzhelko.dmitry.mycamera.router.RouterImpl
import kuzhelko.dmitry.mycamera.ui.camera.presenter.CameraPresenter
import kuzhelko.dmitry.mycamera.ui.camera.presenter.CameraPresenterImpl
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera1api.Camera1Api
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api.Camera2Api
import kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.common.CameraSource
import kuzhelko.dmitry.mycamera.ui.camera.view.CameraActivity
import kuzhelko.dmitry.mycamera.ui.camera.view.CameraView
import javax.inject.Singleton

@Module
class CameraActivityModule {

    @Singleton
    @Provides
    fun provideCameraPresenter(storage: StorageImpl, router: Router): CameraPresenter<*> = CameraPresenterImpl<CameraView>(storage, router)

    @Provides
    fun provideRouter(context: CameraActivity): Router = RouterImpl(context)

    @Provides
    fun provideCameraApi(context: CameraActivity): CameraSource =
            when (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                true -> Camera1Api(context)
                false -> Camera2Api(context)
            }
}