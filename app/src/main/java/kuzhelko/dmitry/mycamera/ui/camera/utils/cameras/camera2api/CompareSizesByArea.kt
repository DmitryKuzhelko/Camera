package kuzhelko.dmitry.mycamera.ui.camera.utils.cameras.camera2api

import android.annotation.TargetApi
import android.os.Build
import android.util.Size
import java.lang.Long.signum
import java.util.*

class CompareSizesByArea : Comparator<Size> {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}