package com.supercilex.robotscouter.shared.scouting

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.CollectionReference
import com.supercilex.robotscouter.common.FIRESTORE_POSITION
import com.supercilex.robotscouter.core.data.LifecycleAwareFirestoreArray
import com.supercilex.robotscouter.core.data.ViewModelBase
import com.supercilex.robotscouter.core.data.model.metricParser
import com.supercilex.robotscouter.core.model.Metric

class MetricListHolder(state: SavedStateHandle) : ViewModelBase<CollectionReference>(state) {
    lateinit var metrics: LifecycleAwareFirestoreArray<Metric<*>>
        private set

    override fun onCreate(args: CollectionReference) {
        metrics = LifecycleAwareFirestoreArray({ args.orderBy(FIRESTORE_POSITION) }, metricParser)
        metrics.keepAlive = true
    }

    override fun onCleared() {
        super.onCleared()
        metrics.keepAlive = false
    }
}
