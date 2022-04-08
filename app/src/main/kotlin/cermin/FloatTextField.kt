package cermin

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Fungsi yang memudahkan untuk membuat input yang membutuhkan nilai float dari user
 */

class FloatTextFieldState(value: Float) {
    var value: Float
        get() = valueState.value
        set(value) {
            this.valueState.value = value
        }

    var string: String
        get() = stringState.value
        set(value) { stringState.value = value }

    fun updateValue() {
        this.string = value.toString()
    }

    fun changeValue(value: Float) {
        this.value = value
        this.string = value.toString()
    }

    private val stringState = mutableStateOf(value.toString())
    private val valueState = mutableStateOf(value)
}


@Composable
fun FloatTextField(
    value: Float,
    label: String,
    onValueChange: (Float) -> Unit,
    readOnly: Boolean = false,
) {
    FloatTextField(
        remember(value) { FloatTextFieldState(value) },
        label,
        onValueChange,
        readOnly,
    )

}

@Composable
fun FloatTextField(
    state: FloatTextFieldState,
    label: String,
    onValueChange: (Float) -> Unit,
    readOnly: Boolean = false,
) {
    TextField(value = state.string,
        singleLine = true,
        readOnly = readOnly,
        label = {
            Text(label)
        },
        onValueChange = {
            state.string = it
            if (it.isBlank()) {
                onValueChange(0f)
            } else {
                onValueChange((it.toFloatOrNull() ?: return@TextField))
            }
        },
        trailingIcon = {
            Text("${state.value}")
        })

}
