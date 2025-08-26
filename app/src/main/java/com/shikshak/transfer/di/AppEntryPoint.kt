package com.shikshak.transfer.di

import com.shikshak.transfer.data.Prefs
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun prefs(): Prefs
}
