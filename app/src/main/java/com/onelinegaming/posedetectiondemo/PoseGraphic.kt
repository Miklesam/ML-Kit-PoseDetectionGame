package com.onelinegaming.posedetectiondemo

import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/** Draw the detected pose in preview.  */
class PoseGraphic internal constructor(
    overlay: GraphicOverlay,
    private val pose: Pose) : GraphicOverlay.Graphic(overlay) {
    private val leftPaint: Paint
    private val rightPaint: Paint
    private val whitePaint: Paint
    private val face: Drawable
    /*private val leftGlove: Drawable
    private val rightGlove: Drawable*/

    init {
        val color = App.context.resources.getColor(R.color.black)
        whitePaint = Paint()
        whitePaint.strokeWidth =
            STROKE_WIDTH
        whitePaint.color = color
        whitePaint.textSize =
            IN_FRAME_LIKELIHOOD_TEXT_SIZE
        leftPaint = Paint()
        leftPaint.strokeWidth =
            STROKE_WIDTH
        leftPaint.color = color
        rightPaint = Paint()
        rightPaint.strokeWidth =
            STROKE_WIDTH
        rightPaint.color = color
        face = App.context.resources.getDrawable(
            R.drawable.ic_cool, null)
        /*leftGlove = App.context.resources.getDrawable(
            R.drawable.glove_left, null)
        rightGlove = App.context.resources.getDrawable(
            R.drawable.glove_right, null)*/
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

        drawLine(canvas, leftShoulder, rightShoulder, whitePaint)
        drawLine(canvas, leftHip, rightHip, whitePaint)
        drawLine(canvas, leftShoulder, leftElbow, leftPaint)
        drawLine(canvas, leftElbow, leftWrist, leftPaint)
        drawLine(canvas, leftShoulder, leftHip, leftPaint)
        drawLine(canvas, leftHip, leftKnee, leftPaint)
        drawLine(canvas, leftKnee, leftAnkle, leftPaint)
//        drawLine(canvas, leftWrist, leftThumb, leftPaint)
//        drawLine(canvas, leftWrist, leftPinky, leftPaint)
//        drawLine(canvas, leftWrist, leftIndex, leftPaint)
//        drawLine(canvas, leftIndex, leftPinky, leftPaint)
        drawLine(canvas, leftAnkle, leftHeel, leftPaint)
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint)

        drawLine(canvas, rightShoulder, rightElbow, rightPaint)
        drawLine(canvas, rightElbow, rightWrist, rightPaint)
        drawLine(canvas, rightShoulder, rightHip, rightPaint)
        drawLine(canvas, rightHip, rightKnee, rightPaint)
        drawLine(canvas, rightKnee, rightAnkle, rightPaint)
//        drawLine(canvas, rightWrist, rightThumb, rightPaint)
//        drawLine(canvas, rightWrist, rightPinky, rightPaint)
//        drawLine(canvas, rightWrist, rightIndex, rightPaint)
//        drawLine(canvas, rightIndex, rightPinky, rightPaint)
        drawLine(canvas, rightAnkle, rightHeel, rightPaint)
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint)

        val faceLeft = (translateX(leftShoulder.position.x)).toInt()
        val faceRight = (translateX(rightShoulder.position.x) ).toInt()
        val faceBottom = (if (leftShoulder.position.y < rightShoulder.position.y)
            translateY(leftShoulder.position.y)
        else
            translateY(rightShoulder.position.y)
                ).let { it }.toInt()
        val faceTop = (faceBottom - (faceRight - faceLeft))

        face.setBounds(
            faceLeft - 25.px,
            faceTop - 50.px,
            faceRight + 25.px,
            faceBottom
        )
        face.draw(canvas)

        /*val leftGloveBitmap = leftGlove.toBitmap()
        val matrixL = Matrix()
        matrixL.postRotate(360 - getAngle(
            leftWrist.position,
            leftPinky.position
        ).toFloat(),
            (leftGloveBitmap.width / 2).toFloat(),
            (leftGloveBitmap.height / 2).toFloat()
        )
        matrixL.postTranslate(translateX(leftWrist.position.x) - (leftGloveBitmap.width / 2),  translateY(leftWrist.position.y) - (leftGloveBitmap.height / 2))
        canvas.drawBitmap(leftGloveBitmap, matrixL, null)

        val rightGloveBitmap = rightGlove.toBitmap()
        val matrixR = Matrix()
        matrixR.postRotate(360 - getAngle(
            rightWrist.position,
            rightPinky.position
        ).toFloat(),
            (leftGloveBitmap.width / 2).toFloat(),
            (leftGloveBitmap.height / 2).toFloat()
        )
        matrixR.postTranslate(translateX(rightWrist.position.x) - (rightGloveBitmap.width / 2),  translateY(rightWrist.position.y) - (rightGloveBitmap.height / 2))
        canvas.drawBitmap(rightGloveBitmap, matrixR, null)*/
    }

    internal fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    ) {
        val start = startLandmark!!.position
        val end = endLandmark!!.position

        canvas.drawLine(
            translateX(start.x),
            translateY(start.y),
            translateX(end.x),
            translateY(end.y) ,
            paint
        )
    }

    companion object {
        private val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
        private val STROKE_WIDTH = 15.0f
    }
}
