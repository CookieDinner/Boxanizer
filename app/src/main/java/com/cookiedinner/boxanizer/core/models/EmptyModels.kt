package com.cookiedinner.boxanizer.core.models

import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.Item

val emptyBox = Box(id = -1, code = "", name = "", description = null, image = null)

val emptyItem = Item(id = -1, name = "", description = null, image = null, consumable = false)