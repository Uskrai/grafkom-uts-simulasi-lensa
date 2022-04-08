package cermin

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Create range of -this to this
 */
fun Float.rangeToNegative() = if (this < 0) {
    rangeTo(-this)
} else {
    (-this).rangeTo(this)
}

/**
 * Membuat Input Transparant saat sedang dihover atau tidak digunakan.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.showOnHover(
) = composed {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    onPointerEvent(PointerEventType.Enter) {
        isHovered = true
    }.onPointerEvent(PointerEventType.Exit) {
        isHovered = false
    }.onPointerEvent(PointerEventType.Press) {
        isPressed = true
    }.onPointerEvent(PointerEventType.Release) {
        isPressed = false
    }.alpha(
        if (isHovered || isPressed) {
            1f
        } else {
            0.1f
        }
    )
}

/**
 * Membulatkan decimal ke angka yang ditentukan.
 */
fun Float.truncate(decimal: Int): Float = when {
    isNaN() || isInfinite() -> this
    else -> {
        val pow = 10f.pow(decimal)
        (this * pow).roundToInt() / pow
    }

}

enum class MirrorTab(val text: String) {
    ConcaveMirror("Cermin Cekung"), ConvexMirror("Cermin Cembung")
}

/**
 * Data yang menyimpan keadaan dan input dari user untuk lensa cekung maupun
 * cembung.
 */
class AppMirrorState {
    val objectHeightState = FloatTextFieldState(151f)
    val objectDistanceState = FloatTextFieldState(304f)
    val focusState = FloatTextFieldState(154f)
    val mirrorState = MirrorState()
}

/**
 * Data yang menyimpan keadaan aplikasi saat ini seperti tab yang sedang dipilih
 * dan nilai yang ada di lensa cekung maupun cekung.
 */
class AppState {
    val convexState = AppMirrorState()
    val concaveState = AppMirrorState()

    var selectedTab: MirrorTab by mutableStateOf(MirrorTab.ConcaveMirror)


    val state
        get() = when (selectedTab) {
            MirrorTab.ConcaveMirror -> concaveState
            MirrorTab.ConvexMirror -> convexState
        }

    val objectHeightState
        get() = state.objectHeightState
    val objectDistanceState
        get() = state.objectDistanceState
    val focusState
        get() = state.focusState

    val zeroX
        get() = state.mirrorState.zeroX
    val zeroY
        get() = state.mirrorState.zeroY
}


fun main() {
    application {
        MaterialTheme.colors
        MaterialTheme() {
            Window(onCloseRequest = ::exitApplication) {
                val state = remember { AppState() }
                val objectHeightState = state.objectHeightState
                val objectDistanceState = state.objectDistanceState
                val focusState = state.focusState

                Column {
                    // Membuat Tombol untuk mengganti jenis lensa.
                    TabRow(
                        state.selectedTab.ordinal,
                        modifier = Modifier.height(50.dp)
                    ) {
                        for (it in MirrorTab.values()) {
                            Tab(selected = state.selectedTab == it, onClick = {
                                state.selectedTab = it
                            }) {
                                Text(
                                    text = it.text,
                                    modifier = Modifier.align(
                                        Alignment.CenterHorizontally
                                    )
                                )
                            }
                        }
                    }

                    Box {
                        val modifier = Modifier.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.first()
                                    val point = change.position

                                    if (event.buttons.isPrimaryPressed) {
                                        objectHeightState.changeValue(
                                            state.zeroY - point.y
                                        )
                                        objectDistanceState.changeValue(
                                            state.zeroX - point.x
                                        )
                                    }

                                    if (event.buttons.isSecondaryPressed) {
                                        focusState.changeValue(
                                            state.zeroX - point.x
                                        )
                                    }
                                }
                            }
                        }.fillMaxSize().align(
                            Alignment.Center
                        ).border(
                            1f.dp, Color.Black
                        ).padding(1f.dp)

                        // Memanggil Fungsi lensa sesuai dengan yang dipilih pengguna.
                        when (state.selectedTab) {
                            MirrorTab.ConcaveMirror -> {
                                ConcaveMirror(
                                    modifier = modifier,
                                    state = state.state.mirrorState,
                                    objectHeight = objectHeightState.value,
                                    objectDistance = objectDistanceState.value,
                                    focus = focusState.value
                                )
                            }
                            MirrorTab.ConvexMirror -> {
                                ConvexMirror(
                                    modifier = modifier,
                                    /* modifier = Modifier.fillMaxSize(), */
                                    state = state.state.mirrorState,
                                    objectHeight = objectHeightState.value,
                                    objectDistance = objectDistanceState.value,
                                    focus = focusState.value
                                )
                            }
                        }

                        InputMirror(
                            state.selectedTab.text, state.state
                        )
                    }
                }
            }
        }
    }
}

/**
 * Membuat Tempat input data yang bisa digunakan lensa
 */
@Composable
fun InputMirror(
    name: String,
    state: AppMirrorState
) {
    val objectHeightState = state.objectHeightState
    val objectDistanceState = state.objectDistanceState
    val focusState = state.focusState
    val mirrorState = state.mirrorState

    VerticalSlider(
        value = objectHeightState.value,
        onValueChange = {
            objectHeightState.changeValue(
                it.truncate(2)
            )
        },
        valueRange = mirrorState.zeroY.rangeToNegative(),
        modifier = Modifier.showOnHover()
    )

    Column {
        Slider(
            value = objectDistanceState.value,
            onValueChange = {
                objectDistanceState.changeValue(
                    it.truncate(2)
                )
            },
            valueRange = mirrorState.zeroX.rangeToNegative(),
            modifier = Modifier.showOnHover().rotate(180f)
        )
        Slider(
            value = focusState.value,
            onValueChange = {
                focusState.changeValue(
                    it.truncate(2)
                )
            },
            valueRange = mirrorState.zeroX.rangeToNegative(),
            modifier = Modifier.showOnHover().rotate(180f)
        )

        Box(modifier = Modifier.align(Alignment.End)) {
            Column {
                Text(name)

                FloatTextField(
                    state = objectHeightState,
                    label = "Tinggi Benda",
                    onValueChange = {
                        if (mirrorState.maxObjectHeight.rangeToNegative()
                                .contains(it)
                        ) {
                            objectHeightState.value = it
                        }
                    },
                )

                FloatTextField(state = objectDistanceState,
                    label = "Jarak Benda",
                    onValueChange = {
                        if (mirrorState.maxObjectDistance.rangeToNegative()
                                .contains(it)
                        ) {
                            objectDistanceState.value = it
                        }
                    })

                FloatTextField(
                    state = focusState,
                    label = "Fokus",
                    onValueChange = {
                        if (mirrorState.maxFocus.rangeToNegative()
                                .contains(it)
                        ) {
                            focusState.value = it
                        }
                    })

                TextField(
                    value = mirrorState.shadowDistance.toString(),
                    label = { Text("Jarak Bayangan") },
                    onValueChange = {},
                    readOnly = true
                )

                TextField(
                    value = mirrorState.shadowHeight.toString(),
                    label = { Text("Tinggi Bayangan") },
                    onValueChange = {},
                    readOnly = true
                )
            }
        }
    }
}
