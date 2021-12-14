package com.onelinegaming.posedetectiondemo

import android.content.res.Resources
import android.graphics.PointF
import kotlin.math.atan2

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp : Int
    get() = this.toInt().px

fun getAngle(point1: PointF, point2 : PointF): Double {
    var angle = Math.toDegrees(
        atan2(
            (point1.y - point2.y).toDouble(),
            (point1.x - point2.x).toDouble()
        )
    )
    if (angle < 0) {
        angle += 360f
    }
    return angle
}