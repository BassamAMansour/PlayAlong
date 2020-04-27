package org.bassamworks.playalong.connections

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import timber.log.Timber
import java.util.*

class DiscovererOrchestrator(app: Application,
                             discovererName: String = UUID.randomUUID().toString(),
                             connectionCallbacks: ConnectionCallbacks,
                             discoveryCallback: EndpointDiscoveryCallback) :
    NearbyConnectionOrchestrator(app, discovererName, connectionCallbacks) {

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback
    private val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()

    private val _discoveredEndpoints: MutableLiveData<MutableMap<String, DiscoveredEndpointInfo>> = MutableLiveData()
    val discoveredEndpoints: LiveData<Map<String, DiscoveredEndpointInfo>> =
        Transformations.map(_discoveredEndpoints) { it.toMap() }

    init {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                Timber.v("OnEndpointFound: endpointId: $endpointId - endpointName: ${info.endpointName}")

                if (info.serviceId != SERVICE_ID) {
                    Timber.v("Endpoint: $endpointId has different service id: ${info.serviceId}")
                    return
                }

                _discoveredEndpoints.value?.set(endpointId, info)

                discoveryCallback.onEndpointFound(endpointId, info)
            }

            override fun onEndpointLost(endpointId: String) {
                Timber.i("OnEndpointLost: $endpointId")

                _discoveredEndpoints.value?.remove(endpointId)

                discoveryCallback.onEndpointLost(endpointId)
            }
        }
    }

    fun startDiscovery() {
        Nearby.getConnectionsClient(app)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { Timber.v("Discovery task success: ${it.isSuccessful}") }
    }

    fun stopDiscovery() {
        Timber.v("Stop Discovery")
        Nearby.getConnectionsClient(app).stopDiscovery()
    }
}