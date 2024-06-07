package com.cookiedinner.boxanizer.items.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun rememberAmountDialogState() = remember {
    AmountDialogState()
}
class AmountDialogState {
    var visible by mutableStateOf(false)
    var actionType by mutableStateOf<ItemAction?>(null)
    var amount by mutableStateOf(TextFieldValue())
    var amountValid by mutableStateOf(false)

    fun hide() {
        visible = false
    }

    fun show(action: ItemAction) {
        amount = TextFieldValue(
            text = "1",
            selection = TextRange(0, 1)
        )
        amountValid = true
        actionType = action
        visible = true
    }
}