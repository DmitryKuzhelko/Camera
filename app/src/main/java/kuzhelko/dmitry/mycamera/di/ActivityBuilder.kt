package kuzhelko.dmitry.mycamera.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import kuzhelko.dmitry.mycamera.ui.camera.utils.CameraActivityModule
import kuzhelko.dmitry.mycamera.ui.camera.view.CameraActivity
import kuzhelko.dmitry.mycamera.ui.faceDetector.utils.FaceDetectorActivityModule
import kuzhelko.dmitry.mycamera.ui.faceDetector.view.FaceDetectorActivity
import kuzhelko.dmitry.mycamera.ui.gallery.utils.GalleryActivityModule
import kuzhelko.dmitry.mycamera.ui.gallery.view.GalleryActivity

@Module
abstract class ActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = [(CameraActivityModule::class)])
    abstract fun providesCameraActivityInjector(): CameraActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(GalleryActivityModule::class)])
    abstract fun providesGalleryActivityInjector(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [(FaceDetectorActivityModule::class)])
    abstract fun providesFaceDetectorActivityInjector(): FaceDetectorActivity
}