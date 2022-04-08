package cermin

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.absoluteValue
import kotlin.math.max

val DrawScope.zeroY: Float
    get() = size.height / 2

val DrawScope.zeroX: Float
    get() = size.width / 2

/**
 * scope yang menyimpan warna yang digunakan
 */
data class DrawingScope(
    val scope: DrawScope,
    var color: Color
) {
    val zeroX: Float
        get() = scope.zeroX

    val zeroY: Float
        get() = scope.zeroY
}

/**
 * @see DrawScope.drawLineDDA
 */
fun DrawingScope.drawLine(
    start: Offset,
    end: Offset
) {
    scope.drawLineDDA(color, start, end)
}

/**
 * @see DrawScope.drawLineInfinite
 */
fun DrawingScope.drawLineInfinite(
    start: Offset,
    end: Offset,
) {
    scope.drawLineInfinite(
        color = color,
        start = start,
        end = end,
    )
}

/**
 * Fungsi yang digunakan untuk memasuki DrawingScope
 */
fun DrawScope.scope(
    color: Color,
    scope: DrawingScope.() -> Unit
) {
    val drawing = DrawingScope(scope = this, color = color)


    drawing.scope()
}

/**
 * Menggambar Pensil
 */
fun DrawingScope.drawPencil(
    distance: Float,
    height: Float
) {
    val bottomEdgeY = zeroY - (height / 5)
    val topEdgeY = zeroY + height / 24 - height
    val insideCenterY = zeroY - height / 4

    val rightEdge = zeroX - (distance - height / 3)
    val leftEdge = zeroX - (distance + height / 3)

    val rightCenter = zeroX - (distance - height / 9)
    val leftCenter = zeroX - (distance + height / 9)

    val x = zeroX - distance
    val y = zeroY - height

    // top center line
    drawLine(
        Offset(rightCenter, y), Offset(leftCenter, y)
    )

    // top center to edge line
    drawLine(
        Offset(rightCenter, y), Offset(rightEdge, topEdgeY)
    )

    drawLine(
        Offset(leftCenter, y), Offset(leftEdge, topEdgeY)
    )

    // top inside center vertical line
    drawLine(
        Offset(rightCenter, y), Offset(rightCenter, insideCenterY)
    )

    drawLine(
        Offset(leftCenter, y), Offset(leftCenter, insideCenterY)
    )

    // top inside center line
    drawLine(
        Offset(leftCenter, insideCenterY), Offset(rightCenter, insideCenterY)
    )

    // top inside center to edge line
    drawLine(
        Offset(rightCenter, insideCenterY), Offset(rightEdge, bottomEdgeY)
    )

    drawLine(
        Offset(leftCenter, insideCenterY), Offset(leftEdge, bottomEdgeY)
    )

    // edge line
    drawLine(
        Offset(rightEdge, topEdgeY), Offset(rightEdge, bottomEdgeY)
    )

    drawLine(
        Offset(leftEdge, topEdgeY), Offset(leftEdge, bottomEdgeY)
    )

    // bottom left line to center line
    drawLine(
        Offset(x, zeroY), Offset(leftEdge, bottomEdgeY)
    )

    // bottom right line to center line
    drawLine(
        Offset(x, zeroY), Offset(rightEdge, bottomEdgeY)
    )
}

/**
 * Mengkalkulasi titik yang harus digambar dari titik `start` sampai `end` didalam rect.
 *
 * bisa digunakan untuk menggambar garis sampai infiniti menggunakan transformStep
 */
inline fun calcDDA(
    start: Offset,
    end: Offset,
    rect: Rect,
    crossinline transformStep: (offset: Offset, index: Int, steps: Int) -> Boolean = { _, index, steps ->
        index < steps
    }
): List<Offset> {
    if (start.isUnspecified || end.isUnspecified) {
        return listOf()
    }

    if (!start.isFinite || !end.isFinite) {
        return listOf()
    }

    val dx = end.x - start.x
    val dy = end.y - start.y

    val steps = max(dx.absoluteValue, dy.absoluteValue)

    val incX = dx / steps
    val incY = dy / steps

    if (Offset(incX, incY).isUnspecified) {
        return listOf()
    }

    var x = start.x
    var y = start.y

    val list = mutableListOf<Offset>()

    var i = 0
    var offset = Offset(x, y)
    while (transformStep(offset, i, steps.toInt())) {

        if (rect.contains(offset)) {
            list.add(offset)
        }

        x += incX
        y += incY

        offset = Offset(x, y)
        i += 1
    }

    return list
}

/**
 * Menggambar garis menggunakan algoritma DDA sampai mencapai tak terhingga.
 */
@Suppress("NAME_SHADOWING")
fun DrawScope.drawLineInfinite(
    color: Color,
    start: Offset,
    end: Offset,
) {
    val rect = Rect(Offset.Zero, size)

    val list = calcDDA(start = start,
        end = end,
        rect = rect,
        transformStep = { offset, index, steps ->

            index < steps || rect.contains(offset)
        })

    drawPoints(
        list, pointMode = PointMode.Polygon, color = color, strokeWidth = 1f
    )
}

/**
 * Menggambar garis menggunakan algoritma DDA.
 */
@Suppress("NAME_SHADOWING")
fun DrawScope.drawLineDDA(
    color: Color,
    start: Offset,
    end: Offset
) {

    val rect = Rect(Offset.Zero, size)


    val list = calcDDA(start, end, rect)

    drawPoints(
        list, pointMode = PointMode.Polygon, color = color, strokeWidth = 1f
    )
}

/**
 * Menggambar garis kartesius
 */
fun DrawScope.drawCartesian() {
    val size = size

    // draw horizontal line
    drawLineDDA(
        color = Color.Black, start = Offset(
            0f, zeroY
        ), end = Offset(
            size.width, zeroY
        )
    )

    // draw vertical line
    drawLineDDA(
        color = Color.Black, start = Offset(
            zeroX, 0f
        ), end = Offset(
            zeroX, size.height
        )
    )

    val limit = Size(size.width - 1f, size.height - 1f)
    val rect = Rect(Offset.Zero, limit)

    drawLineDDA(
        color = Color.Black, start = rect.topLeft, end = rect.bottomLeft
    )

    drawLineDDA(
        color = Color.Black, start = rect.bottomLeft, end = rect.bottomRight
    )
}

fun DrawScope.drawMirror(
    objectX: Float,
    objectY: Float,
    shadowX: Float,
    shadowY: Float,
) {
    /**
     * Start Drawing
     */

    /**
     * light past
     */
    scope(Color.Blue) {

        drawLineInfinite(
            start = Offset(
                zeroX, shadowY
            ), end = Offset(
                shadowX, shadowY
            )
        )

        drawLineInfinite(
            start = Offset(
                zeroX, objectY
            ), end = Offset(
                shadowX, shadowY
            )
        )

    }

    /**
     *
     */

    /**
     * Light Come
     */

    scope(Color.Red) {

        drawLineInfinite(
            start = Offset(zeroX, objectY), end = Offset(objectX, objectY)
        )

        drawLineInfinite(
            start = Offset(zeroX, shadowY), end = Offset(objectX, objectY)
        )
    }

    /**
     *
     */
}
