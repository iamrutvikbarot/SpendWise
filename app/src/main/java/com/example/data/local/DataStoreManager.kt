package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = intPreferencesKey("user_id")
        val MONTHLY_BUDGET = floatPreferencesKey("monthly_budget")
    }

    val monthlyBudget: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[MONTHLY_BUDGET] ?: 0f
        }

    suspend fun setMonthlyBudget(amount: Float) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET] = amount
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
        
    val userId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    suspend fun saveLoginState(isLoggedIn: Boolean, userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            preferences[USER_ID] = userId
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USER_ID)
        }
    }
}
