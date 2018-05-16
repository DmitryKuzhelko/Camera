package kuzhelko.dmitry.mycamera.ui.faceDetector.utils.faceDetector

import android.graphics.Color
import android.graphics.Paint
import kuzhelko.dmitry.mycamera.STROKE_WIDTH

fun Paint.createRectanglePaint() {
    this.apply {
        strokeWidth = STROKE_WIDTH
        color = Color.CYAN
        style = Paint.Style.STROKE
    }
}