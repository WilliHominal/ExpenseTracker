package com.warh.commons.di

import com.warh.commons.AndroidStrings
import com.warh.commons.Strings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonsModule = module {
    singleOf(::AndroidStrings) bind Strings::class
}