package com.supercilex.robotscouter.core.data.model

import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.supercilex.robotscouter.common.FIRESTORE_ID
import com.supercilex.robotscouter.common.FIRESTORE_NAME
import com.supercilex.robotscouter.common.FIRESTORE_POSITION
import com.supercilex.robotscouter.common.FIRESTORE_SELECTED_VALUE_ID
import com.supercilex.robotscouter.common.FIRESTORE_TYPE
import com.supercilex.robotscouter.common.FIRESTORE_UNIT
import com.supercilex.robotscouter.common.FIRESTORE_VALUE
import com.supercilex.robotscouter.core.data.firestoreBatch
import com.supercilex.robotscouter.core.data.logAdd
import com.supercilex.robotscouter.core.data.logFailures
import com.supercilex.robotscouter.core.data.logUpdate
import com.supercilex.robotscouter.core.model.Metric
import com.supercilex.robotscouter.core.model.MetricType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.asTask
import kotlinx.coroutines.tasks.await

val metricParser = SnapshotParser { parseMetric(checkNotNull(it.data), it.reference) }

@Suppress("UNCHECKED_CAST") // We know what our data types are
internal fun parseMetric(fields: Map<String, Any?>, ref: DocumentReference): Metric<*> {
    val position = (fields[FIRESTORE_POSITION] as Long).toInt()
    val type = (fields[FIRESTORE_TYPE] as Long).toInt()
    val name = (fields[FIRESTORE_NAME] as String?).orEmpty()

    return when (MetricType.valueOf(type)) {
        MetricType.HEADER -> Metric.Header(name, position, ref)
        MetricType.BOOLEAN ->
            Metric.Boolean(name, fields[FIRESTORE_VALUE] as Boolean, position, ref)
        MetricType.NUMBER -> Metric.Number(
                name,
                fields[FIRESTORE_VALUE] as Long,
                fields[FIRESTORE_UNIT] as String?,
                position,
                ref
        )
        MetricType.STOPWATCH -> Metric.Stopwatch(
                name,
                fields[FIRESTORE_VALUE] as List<Long>,
                position,
                ref
        )
        MetricType.TEXT -> Metric.Text(
                name,
                fields[FIRESTORE_VALUE] as String?,
                position,
                ref
        )
        MetricType.LIST -> Metric.List(
                name,
                try {
                    fields[FIRESTORE_VALUE] as List<Map<String, String>>
                } catch (e: ClassCastException) {
                    // TODO remove at some point, used to support old model
                    (fields[FIRESTORE_VALUE] as Map<String, String>).map {
                        mapOf(
                                FIRESTORE_ID to it.key,
                                FIRESTORE_NAME to (it.value as String?).toString()
                        )
                    }
                }.map {
                    Metric.List.Item(it[FIRESTORE_ID] as String, it[FIRESTORE_NAME] as String)
                },
                fields[FIRESTORE_SELECTED_VALUE_ID] as String?,
                position,
                ref
        )
    }
}

fun deleteMetrics(ref: CollectionReference) = GlobalScope.async {
    val metrics = ref.get().logFailures("deleteMetrics:get", ref).await()

    firestoreBatch {
        for (metric in metrics) delete(metric.reference)
    }.logFailures("deleteMetrics:del", metrics.map { it.reference }, metrics)

    metrics
}.asTask()

fun restoreMetrics(metrics: QuerySnapshot) {
    firestoreBatch {
        for (metric in metrics) set(metric.reference, metric.data)
    }.logFailures("restoreMetrics", metrics.map { it.reference }, metrics)
}

fun Metric<*>.add() {
    logAdd()
    ref.set(this).logFailures("addMetric", ref, this)
}

fun <T> Metric<T>.update(new: T) {
    if (value != new) {
        value = new
        logUpdate()
        ref.update(FIRESTORE_VALUE, new).logFailures("updateMetric", ref, new)
    }
}

fun Metric<*>.updateName(new: String) {
    if (name != new) {
        name = new
        ref.update(FIRESTORE_NAME, new).logFailures("updateMetricName", ref, new)
    }
}

fun Metric.Number.updateUnit(new: String?) {
    if (unit != new) {
        unit = new
        ref.update(FIRESTORE_UNIT, new).logFailures("updateMetricUnit", ref, new)
    }
}

fun Metric.Stopwatch.add(index: Int, lap: Long) {
    value = value.toMutableList().apply { add(index, lap) }
    logUpdate()

    ref.update(FIRESTORE_VALUE, if (index == value.lastIndex) { // Append
        FieldValue.arrayUnion(lap)
    } else { // Insert
        value // No APIs for this yet, just rewrite the whole array
    }).logFailures("addMetricLap", ref, "Adding lap at position $index: $lap")
}

fun Metric.Stopwatch.remove(lap: Long) {
    value = value.toMutableList().apply { remove(lap) }
    logUpdate()

    ref.update(FIRESTORE_VALUE, FieldValue.arrayRemove(lap))
            .logFailures("removeMetricLap", ref, "Removing lap: $lap")
}

fun Metric.List.update(items: List<Metric.List.Item>) {
    if (value != items) {
        value = items

        ref.update(FIRESTORE_VALUE, items.map {
            mapOf(FIRESTORE_ID to it.id, FIRESTORE_NAME to it.name)
        }).logFailures("updateMetricItems", ref, "Updating items: $items")
    }
}

fun Metric.List.updateSelectedValueId(new: String) {
    if (selectedValueId != new) {
        selectedValueId = new
        logUpdate()

        ref.update(FIRESTORE_SELECTED_VALUE_ID, new)
                .logFailures("updateMetricSelectedId", ref, "Updated selected value: $new")
    }
}
