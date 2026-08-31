package com.mpclab.facedetection.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.mpclab.facedetection.R
import com.mpclab.facedetection.domain.model.FaceDetectionResult
import kotlin.math.roundToInt

class FaceGraphic(
    private val overlay: GraphicOverlay,
    private val face: FaceDetectionResult,
    private val faceIndex: Int
) : OverlayGraphic() {

    private val context = overlay.context

    private val boundingBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = overlay.dp(BOX_STROKE_WIDTH_DP)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = overlay.dp(LABEL_TEXT_SIZE_DP)
        textAlign = Paint.Align.LEFT
        setShadowLayer(overlay.dp(2f), 0f, overlay.dp(1f), Color.BLACK)
    }

    private val openEyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.eye_open_cyan)
    }

    private val closedEyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.eye_closed_orange)
    }

    private val eyelidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = overlay.dp(EYELID_STROKE_WIDTH_DP)
        strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        drawBoundingBox(canvas)
        drawLabel(canvas)
        drawEye(canvas, face.leftEyePosition, face.isLeftEyeOpen)
        drawEye(canvas, face.rightEyePosition, face.isRightEyeOpen)
    }

    private fun drawBoundingBox(canvas: Canvas) {
        val box = overlay.translateRect(face.boundingBox)
        boundingBoxPaint.color = if (face.isSmiling) {
            ContextCompat.getColor(context, R.color.smiling_green)
        } else {
            ContextCompat.getColor(context, R.color.not_smiling_red)
        }
        canvas.drawRect(box, boundingBoxPaint)
    }

    private fun drawLabel(canvas: Canvas) {
        val box = overlay.translateRect(face.boundingBox)
        val state = if (face.isSmiling) "SMILING" else "NEUTRAL"
        val label = "#$faceIndex ${smilePercent()} $state"
        val y = (box.top - overlay.dp(LABEL_OFFSET_DP)).coerceAtLeast(
            overlay.dp(LABEL_TEXT_SIZE_DP + LABEL_OFFSET_DP)
        )
        canvas.drawText(label, box.left, y, labelPaint)
    }

    private fun drawEye(canvas: Canvas, position: PointF?, isOpen: Boolean) {
        if (position == null) return
        val x = overlay.translateX(position.x)
        val y = overlay.translateY(position.y)
        val radius = overlay.dp(EYE_RADIUS_DP)

        if (isOpen) {
            canvas.drawCircle(x, y, radius, openEyePaint)
        } else {
            canvas.drawCircle(x, y, radius, closedEyePaint)
            val lidOffset = radius * 0.6f
            canvas.drawLine(x - radius, y - lidOffset, x + radius, y - lidOffset, eyelidPaint)
        }
    }

    private fun smilePercent(): String {
        val probability = face.smilingProbability ?: return "n/a"
        return "${(probability * 100).roundToInt()}%"
    }

    private companion object {
        const val BOX_STROKE_WIDTH_DP = 8f
        const val LABEL_TEXT_SIZE_DP = 12f
        const val LABEL_OFFSET_DP = 8f
        const val EYE_RADIUS_DP = 6f
        const val EYELID_STROKE_WIDTH_DP = 3f
    }
}