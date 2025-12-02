package com.example.mids.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mids.R
import com.example.mids.flow.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_alerts, container, false)
        listView = v.findViewById(R.id.alert_list)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter
        return v
    }

    override fun onResume() {
        super.onResume()
        loadAlerts()
    }

    private fun loadAlerts() {
        lifecycleScope.launch {
            val alerts = withContext(Dispatchers.IO) {
                AppDatabase.get().alertDao().recent()
            }
            adapter.clear()
            adapter.addAll(alerts.map { "${it.type} @ ${it.ts}: ${it.message} (c=${it.confidence})" })
            adapter.notifyDataSetChanged()
        }
    }
}
