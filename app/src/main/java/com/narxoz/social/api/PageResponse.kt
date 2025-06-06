package com.narxoz.social.api

data class PageResponse<T>(
    val count:    Int,
    val next:     String?,
    val previous: String?,
    val results:  List<T>
)