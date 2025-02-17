package com.supercilex.robotscouter.feature.settings

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.supercilex.robotscouter.core.CrashLogger
import com.supercilex.robotscouter.core.RobotScouter
import com.supercilex.robotscouter.core.await
import com.supercilex.robotscouter.core.data.ViewModelBase
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

internal class SettingsViewModel : ViewModelBase<Unit?>() {
    private val _signOutListener = MutableLiveData<Any?>()
    val signOutListener: LiveData<Any?> = _signOutListener

    override fun onCreate(args: Unit?) = Unit

    fun signOut() {
        launch(UI) {
            try {
                async { AuthUI.getInstance().signOut(RobotScouter).await() }.await()
                _signOutListener.value = null
            } catch (e: Exception) {
                _signOutListener.value = e
                CrashLogger.onFailure(e)
            }
        }
    }
}
