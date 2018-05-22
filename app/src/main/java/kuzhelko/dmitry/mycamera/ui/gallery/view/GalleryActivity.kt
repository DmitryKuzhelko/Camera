package kuzhelko.dmitry.mycamera.ui.gallery.view

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.empty_screen.*
import kotlinx.android.synthetic.main.gallery_control_board.*
import kuzhelko.dmitry.mycamera.*
import kuzhelko.dmitry.mycamera.data.showToast
import kuzhelko.dmitry.mycamera.ui.base.view.BaseActivity
import kuzhelko.dmitry.mycamera.ui.gallery.presenter.GalleryPresenterImpl
import kuzhelko.dmitry.mycamera.ui.gallery.utils.adapter.FadePageTransformer
import kuzhelko.dmitry.mycamera.ui.gallery.utils.adapter.SlideShowAdapter
import kuzhelko.dmitry.mycamera.ui.gallery.utils.fragments.DeleteDialog
import kuzhelko.dmitry.mycamera.ui.gallery.utils.fragments.ResultQRCodeDialog
import kuzhelko.dmitry.mycamera.ui.gallery.utils.qrCodeDetector.QRCodeDetector
import javax.inject.Inject

class GalleryActivity : BaseActivity(), GalleryView, DeleteDialog.DeleteDialogListener {

    @Inject
    lateinit var presenter: GalleryPresenterImpl<GalleryView>

    @Inject
    lateinit var slideShowAdapter: SlideShowAdapter

    @Inject
    lateinit var qrDetector: QRCodeDetector

    private var fabOpen: Animation? = null
    private var fabClose: Animation? = null
    private var rotateForward: Animation? = null
    private var rotateBackward: Animation? = null

    private var isFabOpen: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        presenter.onAttach(this)

        slideShowAdapter = SlideShowAdapter(this)
        viewPager.apply {
            setPageTransformer(false, FadePageTransformer())
            adapter = slideShowAdapter
        }

        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        rotateForward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_backward)

        setUpControlBoard()
        showPhotos()
    }

    private fun setUpControlBoard() {
        setUpToolbar()
        setUpMainFAB()
        setUpQRDetectorFAB()
        setUpFaceDetectorFAB()
    }

    private fun setUpMainFAB() {
        fabMain.setOnClickListener {
            animateFAB()
        }
    }

    private fun setUpQRDetectorFAB() {
        fabDetectQRCode.setOnClickListener {
            val photoPath: String? = presenter.getPathById(viewPager.currentItem)

            if (photoPath != null) {
                Glide.with(this@GalleryActivity)
                        .asBitmap()
                        .load(photoPath)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(photo: Bitmap, transition: Transition<in Bitmap>?) {
                                showResult(qrDetector.detect(photo))
                            }
                        })
            }
        }
    }

    private fun setUpFaceDetectorFAB() {
        fabDetectFace.setOnClickListener {
            presenter.detectFaces(viewPager.currentItem)
        }
    }

    private fun showResult(result: String) {
        ResultQRCodeDialog.newInstance(result).show(supportFragmentManager, QR_CODE_DIALOG)
    }

    private fun animateFAB() {
        isFabOpen = when (isFabOpen) {
            true -> {
                fabMain.startAnimation(rotateBackward)
                fabDetectFace.apply {
                    startAnimation(fabClose)
                    isClickable = false
                }
                fabDetectQRCode.apply {
                    startAnimation(fabClose)
                    isClickable = false
                }
                false
            }
            false -> {
                fabMain.startAnimation(rotateForward)
                fabDetectFace.apply {
                    startAnimation(fabOpen)
                    isClickable = true
                }
                fabDetectQRCode.apply {
                    startAnimation(fabOpen)
                    isClickable = true
                }
                true
            }
        }
    }

    override fun showPhotos() = presenter.getPhotos()

    override fun updateAdapter(photos: ArrayList<String>) {
        slideShowAdapter.updateAdapter(photos)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_gallery, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> sharePhoto()
            R.id.action_delete -> deletePhoto()
            R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setUpToolbar() {
        this.apply {
            setSupportActionBar(toolbarGallery as Toolbar)
            (toolbarGallery as Toolbar).apply {
                title = getString(R.string.gallery_toolbar_title)
                setTitleTextColor(ContextCompat.getColor(this@GalleryActivity, R.color.text_color_secondary))
            }
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }
    }

    override fun showEmptyScreen() {
        viewPager.visibility = View.GONE
        controlBoard.visibility = View.GONE
        emptyScreen.visibility = View.VISIBLE
        btnTakePhoto.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun hideEmptyScreen() {
        emptyScreen.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
        controlBoard.visibility = View.VISIBLE
    }

    override fun sharePhoto() {
        presenter.sharePhoto(viewPager.currentItem)
        Answers.getInstance().logCustom(CustomEvent(SHARE_PHOTO_EVENT)
                .putCustomAttribute(ACTION, "Share photo"))
    }

    override fun deletePhoto() {
        val photoPath: String? = presenter.getPathById(viewPager.currentItem)
        when (photoPath != null) {
            true -> DeleteDialog.newInstance(viewPager.currentItem).show(supportFragmentManager, DELETE_DIALOG)
            false -> showToast(this@GalleryActivity, "Галерея пуста")
        }
    }

    override fun onDeleteDialogPositiveClick(id: Int) {
        slideShowAdapter.delete(id)
        presenter.deleteById(id)
    }

    override fun onDestroy() {
        presenter.onDetach()
        super.onDestroy()
    }
}