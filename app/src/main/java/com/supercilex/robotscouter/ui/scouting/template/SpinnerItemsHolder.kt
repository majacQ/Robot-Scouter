package com.supercilex.robotscouter.ui.scouting.template

import android.app.Application
import android.os.Bundle
import com.firebase.ui.database.ClassSnapshotParser
import com.firebase.ui.database.FirebaseArray
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.firebase.database.DatabaseReference
import com.supercilex.robotscouter.util.FIREBASE_SELECTED_VALUE_KEY
import com.supercilex.robotscouter.util.data.ChangeEventListenerBase
import com.supercilex.robotscouter.util.data.ViewModelBase
import com.supercilex.robotscouter.util.data.getRef

class SpinnerItemsHolder(app: Application) : ViewModelBase<Bundle>(app) {
    lateinit var ref: DatabaseReference
        private set
    var selectedValueKey: String? = null
        private set
    lateinit var spinnerItems: ObservableSnapshotArray<String>
        private set

    private val listener = object : ChangeEventListenerBase {}

    override fun onCreate(args: Bundle) {
        ref = getRef(args)
        selectedValueKey = args.getString(FIREBASE_SELECTED_VALUE_KEY)
        spinnerItems = FirebaseArray(ref, ClassSnapshotParser(String::class.java))
        spinnerItems.addChangeEventListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        spinnerItems.removeChangeEventListener(listener)
    }
}