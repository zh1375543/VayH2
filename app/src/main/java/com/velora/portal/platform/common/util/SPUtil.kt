package com.velora.portal.platform.common.util

import android.content.Context
import android.content.SharedPreferences
import com.velora.portal.platform.common.data.PreferenceSchema
import com.velora.portal.application.MainApplication
import androidx.core.content.edit

object SPUtil {

    private val coreSP: SharedPreferences by lazy {
        getSharedPreferences(PreferenceSchema.Files.CORE)
    }

    private fun getSharedPreferences(name: String): SharedPreferences {
        return MainApplication.appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun newInstance(name: String = PreferenceSchema.Files.APP): SPWrapper {
        return SPWrapper(getSharedPreferences(name))
    }

    fun getInstance(): SPWrapper {
        return SPWrapper(coreSP)
    }

    class SPWrapper(private val sp: SharedPreferences) {

        fun clear(): SPWrapper {
            sp.edit { clear() }
            return this
        }

        fun remove(key: String): SPWrapper {
            sp.edit { remove(key) }
            return this
        }

        fun <T> save(key: String, value: T?): SPWrapper {
            sp.edit {
                when (value) {
                    null -> remove(key)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass.name}")
                }
            }
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> get(key: String, defValue: T): T {
            return when (defValue) {
                is String -> sp.getString(key, defValue) as T
                is Int -> sp.getInt(key, defValue) as T
                is Boolean -> sp.getBoolean(key, defValue) as T
                is Long -> sp.getLong(key, defValue) as T
                is Float -> sp.getFloat(key, defValue) as T
                else -> defValue
            }
        }
    }
}
