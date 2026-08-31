package com.mpclab.facedetection.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.mpclab.facedetection.domain.model.FaceDetectionResult
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

class GraphicOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val graphics = CopyOnWriteArrayList<OverlayGraphic>()

    @Volatile
    private var isImageFlipped = false

    @Volatile
    private var imageWidth = 0

    @Volatile
    private var imageHeight = 0

    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isImageFlipped: Boolean) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.isImageFlipped = isImageFlipped
        postInvalidate()
    }

    fun clear() {
        graphics.clear()
        postInvalidate()
    }

    fun setFaces(faces: List<FaceDetectionResult>) {
        synchronized(this) {
            graphics.clear()
            faces.forEachIndexed { index, face ->
                graphics += FaceGraphic(this, face, index + 1)
            }
        }
        postInvalidate()
    }

    fun dp(value: Float): Float = value * resources.displayMetrics.density

    fun translateX(x: Float): Float {
        val centered = (x - imageWidth / 2f) * previewScale() + width / 2f
        return if (isImageFlipped) width - centered else centered
    }

    fun translateY(y: Float): Float =
        (y - imageHeight / 2f) * previewScale() + height / 2f

    fun translateRect(rect: RectF): RectF = RectF(
        translateX(rect.left),
        translateY(rect.top),
        translateX(rect.right),
        translateY(rect.bottom)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        graphics.forEach { it.draw(canvas) }
    }

    private fun previewScale(): Float {
        if (imageWidth <= 0 || imageHeight <= 0) return 1f
        return max(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
    }
}