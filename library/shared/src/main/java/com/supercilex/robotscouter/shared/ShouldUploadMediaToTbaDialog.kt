package com.supercilex.robotscouter.shared

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.supercilex.robotscouter.core.data.shouldUploadMediaToTba
import com.supercilex.robotscouter.core.ui.DialogFragmentBase

class ShouldUploadMediaToTbaDialog : DialogFragmentBase(), DialogInterface.OnClickListener {
    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(requireContext())
            .setTitle(R.string.media_should_upload_title)
            .setMessage(getText(R.string.media_should_upload_rationale).trim())
            .setView(R.layout.should_upload_media_dialog)
            .setPositiveButton(R.string.yes, this)
            .setNegativeButton(R.string.no, this)
            .create()

    override fun onStart() {
        super.onStart()
        requireDialog().findViewById<TextView>(android.R.id.message).movementMethod =
                LinkMovementMethod.getInstance()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val isYes: Boolean = which == Dialog.BUTTON_POSITIVE

  <<<<<<< master
        if (requireDialog().save.isChecked) shouldUploadMediaToTba = isYes
        ViewModelProvider(requireParentFragment()).get<TeamMediaCreator>().capture(isYes)
  =======
        if (requireDialog().findViewById<CheckBox>(R.id.save).isChecked) {
            shouldUploadMediaToTba = isYes
        }
        (requireParentFragment() as CaptureTeamMediaListener).startCapture(isYes)
  >>>>>>> view-binding
    }

    companion object {
        private const val TAG = "ShouldUploadMediaToTbaD"

        fun show(manager: FragmentManager) = ShouldUploadMediaToTbaDialog().show(manager, TAG)
    }
}
