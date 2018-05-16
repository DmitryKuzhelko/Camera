package kuzhelko.dmitry.mycamera.ui.gallery.utils.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import kuzhelko.dmitry.mycamera.R
import javax.inject.Inject

class SlideShowAdapter
@Inject constructor(private val context: Context) : PagerAdapter() {

    private var photos: MutableList<String> = ArrayList<String>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun updateAdapter(photos: ArrayList<String>) {
        this.photos = photos
        notifyDataSetChanged()
    }

    fun delete(index: Int) {
        photos.removeAt(index)
        notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount() = photos.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = inflater.inflate(R.layout.pager_item, container, false)
        val ivPlaceholder = itemView.findViewById(R.id.ivFaceDetector) as ImageView

        Glide.with(context)
                .load(photos[position])
                .into(ivPlaceholder)

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}