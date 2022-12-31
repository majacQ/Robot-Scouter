package com.supercilex.robotscouter.core.ui

import android.content.Intent
import android.graphics.Rect
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.analytics.FirebaseAnalytics

interface OnActivityResult {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

  <<<<<<< snyk-upgrade-8b98c69bb7423e6986ea50fb8b76167d
abstract class ActivityBase : AppCompatActivity(), OnActivityResult, KeyboardShortcutListener {
  =======
interface Saveable {
    /**
     * @see [androidx.appcompat.app.AppCompatActivity.onSaveInstanceState]
     * @see [androidx.fragment.app.Fragment.onSaveInstanceState]
     */
    fun onSaveInstanceState(outState: Bundle)
}

abstract class ActivityBase : AppCompatActivity(), OnActivityResult, Saveable,
        KeyboardShortcutListener {
    private val shortcutManager = ShortcutManager(this)
  >>>>>>> shortcuts
    private val filteredEvents = mutableMapOf<Long, KeyEvent>()
    private var clearFocus: Runnable? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        event.also { filteredEvents[it.downTime] = it }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (filteredEvents.remove(event.downTime) != null) {
            if (shortcutManager.onEvent(event)) return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun registerShortcut(
            keyCode: Int,
            metaState: Int,
            description: Int,
            action: () -> Unit
    ) = shortcutManager.registerShortcut(keyCode, metaState, description, action)

    @Suppress("RedundantOverride") // Needed to relax visibility
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
            super.onActivityResult(requestCode, resultCode, data)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val v: View? = currentFocus
        if (ev.action == MotionEvent.ACTION_DOWN && v is EditText) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)
            if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                clearFocus = Runnable {
                    if (currentFocus === v || currentFocus !is EditText) {
                        v.clearFocus()
                        v.hideKeyboard()
                    }
                    clearFocus = null
                }.also {
                    v.postDelayed(it, shortAnimationDuration)
                }
            }
        } else if (
            ev.action == MotionEvent.ACTION_MOVE && clearFocus != null &&
            ev.eventTime - ev.downTime > shortAnimationDuration / 2
        ) {
            v?.removeCallbacks(clearFocus)
        }
        return super.dispatchTouchEvent(ev)
    }
}

  <<<<<<< snyk-upgrade-8b98c69bb7423e6986ea50fb8b76167d
abstract class FragmentBase(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId), OnActivityResult {
  =======
abstract class FragmentBase : Fragment(), OnActivityResult, Saveable, KeyboardShortcutListener {
    override fun registerShortcut(
            keyCode: Int,
            metaState: Int,
            description: Int,
            action: () -> Unit
    ) = (activity as ActivityBase).registerShortcut(keyCode, metaState, description, action)

  >>>>>>> shortcuts
    override fun onResume() {
        super.onResume()
        val screenName = javaClass.simpleName
        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), screenName, screenName)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim == 0) return null
        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        val view = view
        if (animation != null && view != null) {
            val prevType = view.layerType
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationEnd(animation: Animation) =
                        view.setLayerType(prevType, null)

                override fun onAnimationRepeat(animation: Animation?) = Unit

                override fun onAnimationStart(animation: Animation?) = Unit
            })
        }

        return animation
    }
}

abstract class PreferenceFragmentBase : PreferenceFragmentCompat(), OnActivityResult {
    override fun onResume() {
        super.onResume()
        val screenName = javaClass.simpleName
        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), screenName, screenName)
    }
}
