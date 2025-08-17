package com.warh.commons

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface Strings {
    fun get(@StringRes id: Int): String
    fun format(@StringRes id: Int, vararg args: Any): String
    fun quantity(@PluralsRes id: Int, quantity: Int, vararg args: Any): String
}

operator fun Strings.get(@StringRes id: Int): String = get(id)

class AndroidStrings(private val appContext: Context) : Strings {
    private val res get() = appContext.resources

    override fun get(id: Int) = res.getString(id)
    override fun format(id: Int, vararg args: Any) = res.getString(id, *args)
    override fun quantity(id: Int, quantity: Int, vararg args: Any) =
        res.getQuantityString(id, quantity, *args)
}