package com.example.mids.detection

import com.example.mids.features.FlowFeatures
import org.json.JSONObject

object RuleEngine {
    fun evaluate(features: FlowFeatures): List<DetectionAlert> {
        val alerts = mutableListOf<DetectionAlert>()
        // Example rule: very high packet count within window (scaffold)
        if (features.pkts > 500 && features.avgIATMs < 5.0) {
            val evidence = JSONObject()
            evidence.put("pkts", features.pkts)
            evidence.put("avgIATMs", features.avgIATMs)
            alerts.add(
                DetectionAlert(
                    type = "high_packet_rate",
                    message = "High packet rate detected",
                    ts = System.currentTimeMillis(),
                    confidence = 0.8,
                    evidenceJson = evidence.toString()
                )
            )
        }
        // More rules (ARP, DNS redirect, deauth) require parsing of payloads & specialized detectors.
        return alerts
    }
}
