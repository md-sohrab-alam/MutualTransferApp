package com.shikshak.transfer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class Prefs @Inject constructor(
    private val context: Context
) {
    
    private val disclaimerAcceptedKey = booleanPreferencesKey("disclaimer_accepted")
    
    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[disclaimerAcceptedKey] = accepted
        }
    }
    
    fun disclaimerAcceptedFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[disclaimerAcceptedKey] ?: false
        }
    }
    
    suspend fun isDisclaimerAccepted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[disclaimerAcceptedKey] ?: false
        }.first()
    }
}
