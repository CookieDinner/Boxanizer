package com.cookiedinner.boxanizer.core.api

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseInfo(
    val html_url: String,
    val name: String,
)