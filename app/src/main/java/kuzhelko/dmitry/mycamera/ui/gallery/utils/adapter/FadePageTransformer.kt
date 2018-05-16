package kuzhelko.dmitry.mycamera.ui.gallery.utils.adapter

import android.support.v4.view.ViewPager
import android.view.View

class FadePageTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {

        if (position <= -1.0f || position >= 1.0f) {        // [-Infinity,-1) OR (1,+Infinity]
            view.alpha = 0.0f
            view.visibility = View.GONE
        } else if (position == 0.0f) {     // [0]
            view.alpha = 1.0f
            view.visibility = View.VISIBLE
        } else {

            // Position is between [-1,1]
            view.alpha = 1.0f - Math.abs(position)
            view.translationX = -position * (view.width / 2)
            view.visibility = View.VISIBLE
        }
    }
}