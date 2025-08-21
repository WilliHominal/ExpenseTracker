package com.warh.domain.models

data class Category(
    val id: Long,
    val name: String,
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null,
    val type: TxType
)