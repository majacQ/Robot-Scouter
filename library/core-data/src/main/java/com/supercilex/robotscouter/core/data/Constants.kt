package com.supercilex.robotscouter.core.data

import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.supercilex.robotscouter.common.FIRESTORE_DEFAULT_TEMPLATES
import com.supercilex.robotscouter.common.FIRESTORE_DELETION_QUEUE
import com.supercilex.robotscouter.common.FIRESTORE_DUPLICATE_TEAMS
import com.supercilex.robotscouter.common.FIRESTORE_TEAMS
import com.supercilex.robotscouter.common.FIRESTORE_TEMPLATES
import com.supercilex.robotscouter.common.FIRESTORE_USERS
import com.supercilex.robotscouter.core.fullVersionName

val user get() = FirebaseAuth.getInstance().currentUser
val uid get() = user?.uid
val isSignedIn get() = user != null
val isFullUser get() = isSignedIn && !checkNotNull(user).isAnonymous

val teamsRef = Firebase.firestore.collection(FIRESTORE_TEAMS)
val templatesRef = Firebase.firestore.collection(FIRESTORE_TEMPLATES)
internal val usersRef = Firebase.firestore.collection(FIRESTORE_USERS)
internal val defaultTemplatesRef = Firebase.firestore.collection(FIRESTORE_DEFAULT_TEMPLATES)
internal val teamDuplicatesRef = Firebase.firestore.collection(FIRESTORE_DUPLICATE_TEAMS)
internal val deletionQueueRef = Firebase.firestore.collection(FIRESTORE_DELETION_QUEUE)

val debugInfo: String
    get() =
        """
        |- Robot Scouter version: $fullVersionName
        |- Android OS version: ${Build.VERSION.SDK_INT}
        |- User id: $uid
        """.trimMargin()
