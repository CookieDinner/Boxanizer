package com.cookiedinner.boxanizer.items.models

import androidx.compose.animation.core.MutableTransitionState
import com.cookiedinner.boxanizer.database.ItemInBox

data class ItemInBoxWithTransition(
    val item: ItemInBox,
    val transitionState: MutableTransitionState<Boolean> = MutableTransitionState(true),
)