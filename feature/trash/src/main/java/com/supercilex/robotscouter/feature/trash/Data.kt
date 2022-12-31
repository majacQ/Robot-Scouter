package com.supercilex.robotscouter.feature.trash

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.supercilex.robotscouter.common.DeletionType
import com.supercilex.robotscouter.common.FIRESTORE_BASE_TIMESTAMP
import com.supercilex.robotscouter.common.FIRESTORE_TIMESTAMP
import com.supercilex.robotscouter.common.FIRESTORE_TYPE
import com.supercilex.robotscouter.core.CrashLogger
import com.supercilex.robotscouter.core.InvocationMarker
import com.supercilex.robotscouter.core.data.ListenerRegistrationLifecycleOwner
import com.supercilex.robotscouter.core.data.SimpleViewModelBase
import com.supercilex.robotscouter.core.data.model.userDeletionQueue

internal data class Trash(val id: String, val timestamp: Timestamp, val type: DeletionType)

internal class TrashHolder(state: SavedStateHandle) : SimpleViewModelBase(state),
        DefaultLifecycleObserver, EventListener<DocumentSnapshot> {
    private val _trashListener = MutableLiveData<List<Trash>?>()
    val trashListener: LiveData<List<Trash>?> = _trashListener

    private var registration: ListenerRegistration? = null

    override fun onCreate() {
        ListenerRegistrationLifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        registration = userDeletionQueue.addSnapshotListener(this)
    }

    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            CrashLogger.onFailure(InvocationMarker(e))
            _trashListener.value = null
            return
        }

        _trashListener.value = checkNotNull(snapshot).data?.asSequence()?.mapNotNull { (id, item) ->
            if (id == FIRESTORE_BASE_TIMESTAMP) {
                null
            } else {
                @Suppress("UNCHECKED_CAST") // We know its type
                item as Map<String, Any>
                Trash(
                        id,
                        item[FIRESTORE_TIMESTAMP] as Timestamp,
                        DeletionType.valueOf(item[FIRESTORE_TYPE] as Long)
                )
            }
        }?.filter { (_, _, type) ->
            type == DeletionType.TEAM || type == DeletionType.TEMPLATE
        }?.sortedByDescending {
            it.timestamp
        }?.toList()
    }

    override fun onStop(owner: LifecycleOwner) {
        registration?.remove()
    }

    public override fun onCleared() {
        super.onCleared()
        ListenerRegistrationLifecycleOwner.lifecycle.removeObserver(this)
        onStop(ListenerRegistrationLifecycleOwner)
    }
}
