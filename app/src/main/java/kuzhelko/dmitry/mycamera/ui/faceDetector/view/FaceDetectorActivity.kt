package kuzhelko.dmitry.mycamera.ui.faceDetector.view

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import kotlinx.android.synthetic.main.activity_face.*
import kuzhelko.dmitry.mycamera.ACTION
import kuzhelko.dmitry.mycamera.DETECT_FACES_EVENT
import kuzhelko.dmitry.mycamera.IMAGE_PATH
import kuzhelko.dmitry.mycamera.R
import kuzhelko.dmitry.mycamera.ui.base.view.BaseActivity
import kuzhelko.dmitry.mycamera.ui.faceDetector.presenter.FaceDetectorPresenterImpl
import kuzhelko.dmitry.mycamera.ui.faceDetector.utils.faceDetector.FacesDetector
import javax.inject.Inject

class FaceDetectorActivity : BaseActivity(), FaceDetectorView {

    @Inject
    lateinit var presenter: FaceDetectorPresenterImpl<FaceDetectorView>

    @Inject
    lateinit var faceDetector: FacesDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face)

        setUpToolbar()
        val photoPath = intent.getStringExtra(IMAGE_PATH)
        detectFaces(photoPath)
    }

    private fun setUpToolbar() {
        this.apply {
            setSupportActionBar(toolbarFaceDetector as Toolbar)
            (toolbarFaceDetector as Toolbar).apply {
                title = getString(R.string.face_detector_toolbar_title)
                setTitleTextColor(ContextCompat.getColor(this@FaceDetectorActivity, R.color.text_color_secondary))
            }
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }
    }

    override fun detectFaces(path: String) {
        Answers.getInstance().logCustom(CustomEvent(DETECT_FACES_EVENT)
                .putCustomAttribute(ACTION, "Detect faces"))
        Glide.with(this)
                .asBitmap()
                .load(path)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(photo: Bitmap, transition: Transition<in Bitmap>?) {

                        val tempBitmap = faceDetector.getDetectedFaces(photo)

                        Glide.with(this@FaceDetectorActivity)
                                .asBitmap()
                                .load(tempBitmap)
                                .into(ivFaceDetector)
                    }
                })
    }

    override fun onDestroy() {
        presenter.onDetach()
        super.onDestroy()
    }
}