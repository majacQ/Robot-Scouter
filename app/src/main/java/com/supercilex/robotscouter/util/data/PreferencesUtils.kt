package com.supercilex.robotscouter.util.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceDataStore
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.firebase.database.DataSnapshot
import com.supercilex.robotscouter.util.FIREBASE_PREFS
import com.supercilex.robotscouter.util.FIREBASE_PREF_HAS_SHOWN_ADD_TEAM_TUTORIAL
import com.supercilex.robotscouter.util.FIREBASE_PREF_HAS_SHOWN_EXPORT_HINT
import com.supercilex.robotscouter.util.FIREBASE_PREF_HAS_SHOWN_SIGN_IN_TUTORIAL
import com.supercilex.robotscouter.util.FIREBASE_PREF_NIGHT_MODE
import com.supercilex.robotscouter.util.FIREBASE_PREF_UPLOAD_MEDIA_TO_TBA
import com.supercilex.robotscouter.util.data.model.userPrefs
import com.supercilex.robotscouter.util.prefsListener
import kotlin.properties.Delegates

private var localPrefs: SharedPreferences by Delegates.notNull()

val prefs = object : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        if (getString(key, null) != value) {
            userPrefs.child(key).setValue(value)
        }
    }

    override fun getString(key: String, defValue: String?): String? =
            localPrefs.getString(key, defValue)

    override fun putBoolean(key: String, value: Boolean) {
        if (getBoolean(key, false) != value) {
            userPrefs.child(key).setValue(value)
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
            localPrefs.getBoolean(key, defValue)
}

@get:AppCompatDelegate.NightMode
val nightMode: Int get() {
    val mode = prefs.getString(FIREBASE_PREF_NIGHT_MODE, "auto")
    return when (mode) {
        "auto" -> AppCompatDelegate.MODE_NIGHT_AUTO
        "yes" -> AppCompatDelegate.MODE_NIGHT_YES
        "no" -> AppCompatDelegate.MODE_NIGHT_NO
        else -> throw IllegalStateException("Unknown night mode value: $mode")
    }
}

val shouldAskToUploadMediaToTba: Boolean
    get() = prefs.getString(FIREBASE_PREF_UPLOAD_MEDIA_TO_TBA, "ask") == "ask"

var shouldUploadMediaToTba: Boolean
    get() = prefs.getString(FIREBASE_PREF_UPLOAD_MEDIA_TO_TBA, "ask") == "yes"
    set(value) = prefs.putString(FIREBASE_PREF_UPLOAD_MEDIA_TO_TBA, if (value) "yes" else "no")

var hasShownAddTeamTutorial: Boolean
    get() = prefs.getBoolean(FIREBASE_PREF_HAS_SHOWN_ADD_TEAM_TUTORIAL, false)
    set(value) = prefs.putBoolean(FIREBASE_PREF_HAS_SHOWN_ADD_TEAM_TUTORIAL, value)

var hasShownSignInTutorial: Boolean
    get() = prefs.getBoolean(FIREBASE_PREF_HAS_SHOWN_SIGN_IN_TUTORIAL, false)
    set(value) = prefs.putBoolean(FIREBASE_PREF_HAS_SHOWN_SIGN_IN_TUTORIAL, value)

var hasShownExportHint: Boolean
    get() = prefs.getBoolean(FIREBASE_PREF_HAS_SHOWN_EXPORT_HINT, false)
    set(value) = prefs.putBoolean(FIREBASE_PREF_HAS_SHOWN_EXPORT_HINT, value)

fun initPrefs(context: Context) {
    localPrefs = context.getSharedPreferences(FIREBASE_PREFS, Context.MODE_PRIVATE)

    prefsListener.observeForever {
        it?.addChangeEventListener(object : ChangeEventListenerBase {
            override fun onChildChanged(type: ChangeEventListener.EventType,
                                        snapshot: DataSnapshot,
                                        index: Int,
                                        oldIndex: Int) {
                val key = snapshot.key

                if (type == ChangeEventListener.EventType.ADDED
                        || type == ChangeEventListener.EventType.CHANGED) {
                    localPrefs.updatePrefs {
                        when (key) {
                            FIREBASE_PREF_HAS_SHOWN_ADD_TEAM_TUTORIAL,
                            FIREBASE_PREF_HAS_SHOWN_SIGN_IN_TUTORIAL,
                            FIREBASE_PREF_HAS_SHOWN_EXPORT_HINT
                            -> putBoolean(key, it.getObject(index) as Boolean)

                            FIREBASE_PREF_NIGHT_MODE,
                            FIREBASE_PREF_UPLOAD_MEDIA_TO_TBA
                            -> putString(key, it.getObject(index) as String)

                            else -> throw IllegalStateException("Unknown pref key: $key")
                        }
                    }
                } else if (type == ChangeEventListener.EventType.REMOVED) {
                    localPrefs.updatePrefs { remove(key) }
                }
            }
        }) ?: clearLocalPrefs()
    }
}

fun <T> ObservableSnapshotArray<*>.getPrefOrDefault(key: String, defValue: T): T {
    for ((index, snapshot) in this.withIndex()) {
        @Suppress("UNCHECKED_CAST")
        if (snapshot.key == key) return getObject(index) as T
    }
    return defValue
}

fun clearPrefs() {
    for ((key, value) in localPrefs.all.entries) {
        when (value) {
            is Boolean -> prefs.putBoolean(key, false)
            is String -> prefs.putString(key, null)
            else -> throw IllegalStateException(
                    "Unknown value type: ${if (value == null) "null" else value::class.java}")
        }
    }
    clearLocalPrefs()
}

private fun clearLocalPrefs() = localPrefs.updatePrefs { clear() }

@SuppressLint("CommitPrefEdits")
private inline fun SharedPreferences.updatePrefs(transaction: SharedPreferences.Editor.() -> Unit) = edit().run {
    transaction()
    apply()
}