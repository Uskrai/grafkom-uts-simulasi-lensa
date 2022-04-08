package cermin

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint

/**
 * Data yang menyimpan keadaan lensa saat ini seperti jarak bayangan dan tinggi bayangan
 */
class MirrorState {
    var shadowDistance by mutableStateOf(0f)
        internal set

    var shadowHeight by mutableStateOf(0f)
        internal set

    var zeroX by mutableStateOf(0f)
    var zeroY by mutableStateOf(0f)

    val maxObjectHeight: Float
        get() = zeroY

    val maxObjectDistance: Float
        get() = zeroX

    val maxFocus: Float
        get() = zeroX

    fun calculateShadow(objectHeight: Float, objectDistance: Float, focus: Float) {
        shadowDistance = objectDistance * focus / (objectDistance - focus)
        shadowHeight = shadowDistance * objectHeight / objectDistance
    }

    fun setLimit(zeroX: Float, zeroY: Float) {
        this.zeroX = zeroX
        this.zeroY = zeroY
    }

    override fun toString(): String {
        return "MirrorState(shadowDistance=$shadowDistance, shadowHeight=$shadowHeight, zeroX=$zeroX, zeroY=$zeroY)"
    }
}

/**
 * Menggambar Lensa Cembung
 */
@Composable
fun ConvexMirror(
    modifier: Modifier,
    state: MirrorState,
    objectHeight: Float,
    objectDistance: Float,
    focus: Float,
) = Canvas(modifier = modifier) {
    state.setLimit(zeroX, zeroY)
    state.calculateShadow(objectHeight, objectDistance, focus)

    val shadowDistance = state.shadowDistance
    val shadowHeight = state.shadowHeight

    val objectX = zeroX - objectDistance
    val objectY = zeroY - objectHeight

    val shadowX = zeroX + shadowDistance
    val shadowY = zeroY + shadowHeight

    drawMirror(objectX, objectY, shadowX, shadowY)

    scope(Color.Blue) {
        if (shadowX.isFinite() && shadowY.isFinite()) {
            drawLineInfinite(Offset(zeroX, objectY), Offset(zeroX + focus, zeroY))
        }
        drawLineInfinite(Offset(zeroX, zeroY), Offset(shadowX, shadowY))
    }

    scope(Color.Red) {
//        drawLineInfinite(Offset(zeroX, shadowY), Offset(zeroX - focus, zeroY))
        drawLineInfinite(Offset(zeroX, zeroY), Offset(objectX, objectY))
    }

    drawFocus(focus)
    drawFocus(-focus)

    //object
    scope(Color.Blue) {
        drawPencil(objectDistance, objectHeight)
    }

    // reflect (color = purple)
    scope(Color(149,53,83)) {
        drawPencil(-shadowDistance, -shadowHeight)
    }


    drawCartesian()
}

/**
 * Menggambar Lensa Cekung
 */
@Composable
fun ConcaveMirror(
    modifier: Modifier,
    state: MirrorState,
    objectHeight: Float,
    objectDistance: Float,
    focus: Float,
) = Canvas(modifier = modifier) {
    state.setLimit(zeroX, zeroY)

    state.calculateShadow(objectHeight, objectDistance, focus)

    val shadowDistance = state.shadowDistance
    val shadowHeight = state.shadowHeight

    val objectX = zeroX - objectDistance
    val objectY = zeroY - objectHeight

    val shadowX = zeroX - shadowDistance
    val shadowY = zeroY + shadowHeight

    /**
     * Start Drawing
     */

    drawMirror(objectX, objectY, shadowX, shadowY)

    /**
     * reflect
     */

    scope(Color.Magenta) {
        drawLine(
            start = Offset(shadowX, zeroY), end = Offset(shadowX, shadowY)
        )

        // purple
        color = Color(149, 53, 83)

        drawPencil(shadowDistance, -shadowHeight)
    }


    /**
     *
     */

    /**
     * Object
     */
    scope(Color.Cyan) {
        val x = objectX
        val y = objectY


        // center vertical line
        drawLine(
            Offset(x, zeroY), Offset(x, y)

        )

        color = Color.Blue
        drawPencil(objectDistance, objectHeight)
    }

    /**
     *
     */
    drawFocus(-focus)

    drawCartesian()
}

/**
 * Menggambar posisi fokus dan radius dengan huruf "f" dan "r"
 */
fun DrawScope.drawFocus(focus: Float) {
    drawIntoCanvas {
        val offsetY = zeroY - 5

        val focusX = zeroX + focus
        val rangeX = 0f..size.width

        if (rangeX.contains(focusX)) {
            it.nativeCanvas.drawString(
                "f", focusX, offsetY, Font(), Paint()
            )
        }

        val rX = zeroX + (focus * 2)
        if (rangeX.contains(rX)) {
            it.nativeCanvas.drawString(
                "r", rX, offsetY, Font(), Paint()
            )
        }
    }
}
