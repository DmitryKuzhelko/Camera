package kuzhelko.dmitry.mycamera.ui.faceDetector.presenter

import kuzhelko.dmitry.mycamera.ui.base.presenter.BasePresenterImpl
import kuzhelko.dmitry.mycamera.ui.faceDetector.view.FaceDetectorView
import javax.inject.Inject

class FaceDetectorPresenterImpl<V : FaceDetectorView>
@Inject constructor() : BasePresenterImpl<V>(), FaceDetectorPresenter<V>