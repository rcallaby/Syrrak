package com.example.mids.detection

import com.example.mids.alerts.AlertEntity
import com.example.mids.flow.AppDatabase
import com.example.mids.features.FlowFeatures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DetectionPipeline {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun onFeatures(features: List<FlowFeatures>) {
        scope.launch {
            for (f in features) {
                // Rule-based detection
                val alerts = RuleEngine.evaluate(f)
                if (alerts.isNotEmpty()) {
                    // persist alerts
                    for (a in alerts) {
                        val entity = AlertEntity(
                            type = a.type,
                            message = a.message,
                            ts = a.ts,
                            confidence = a.confidence,
                            evidence = a.evidenceJson
                        )
                        try {
                            AppDatabase.get().alertDao().insert(entity)
                        } catch (_: Exception) {}
                    }
                }
                // TODO: anomaly detector/inference
            }
        }
    }
}

data class DetectionAlert(val type: String, val message: String, val ts: Long, val confidence: Double, val evidenceJson: String)
