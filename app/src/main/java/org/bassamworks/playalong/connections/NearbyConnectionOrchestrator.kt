package org.bassamworks.playalong.connections

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import timber.log.Timber

abstract class NearbyConnectionOrchestrator(protected val app: Application,
                                            protected val username: String,
                                            connectionCallback: ConnectionCallbacks) {

    private val _connectedEndpoints: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
    val connectedEndpoints: LiveData<Set<String>> = Transformations.map(_connectedEndpoints) { it.toSet() }

    protected val connectionLifecycleCallback: ConnectionCallbacks

    init {
        connectionLifecycleCallback = object : ConnectionCallbacks() {

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                Timber.v("OnConnectionInitiated: EndpointId: $endpointId - Auth Token: ${connectionInfo.authenticationToken}")

                connectionCallback.onConnectionInitiated(endpointId, connectionInfo)
            }

            override fun onConnectionSuccess(endpointId: String) {
                Timber.i("Connection established. EndpointId: $endpointId")

                _connectedEndpoints.value?.let {
                    it.add(endpointId)
                    _connectedEndpoints.value = it
                }

                connectionCallback.onConnectionSuccess(endpointId)
            }

            override fun onConnectionFailed(endpointId: String, resolution: ConnectionResolution) {
                when (resolution.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        Timber.w("Connection Rejected. EndpointId: $endpointId - ${resolution.status}")
                    }
                    else -> {
                        Timber.e(Exception("Error happened. EndpointId: $endpointId - ${resolution.status}"))
                    }
                }

                connectionCallback.onConnectionFailed(endpointId, resolution)
            }

            override fun onDisconnected(endpointId: String) {
                Timber.i("OnDisconnect: $endpointId")

                _connectedEndpoints.value?.let {
                    it.remove(endpointId)
                    _connectedEndpoints.value = it
                }
                connectionCallback.onDisconnected(endpointId)
            }
        }
    }

    fun requestConnection(endpointId: String) {
        if (_connectedEndpoints.value!!.contains(endpointId)) {
            Timber.v("Requesting connection from already connected endpoint with id $endpointId.")
            return
        }

        Nearby.getConnectionsClient(app)
            .requestConnection(username, endpointId, connectionLifecycleCallback)
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { Timber.v("Requesting connection task success: ${it.isSuccessful}") }
    }

    fun acceptConnection(endpointId: String, payloadCallback: PayloadCallback) {
        Nearby.getConnectionsClient(app)
            .acceptConnection(endpointId, payloadCallback)
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { Timber.v("Accepting connection task success: ${it.isSuccessful}") }
    }

    fun sendPayload(endpointId: String, payload: Payload) {
        Nearby.getConnectionsClient(app)
            .sendPayload(endpointId, payload)
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { Timber.v("Sending payload task success: ${it.isSuccessful}") }
    }

    companion object {
        val STRATEGY: Strategy = Strategy.P2P_STAR
        const val SERVICE_ID = "org.bassamworks.playalong"
    }

    abstract class ConnectionCallbacks : ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.statusCode == ConnectionsStatusCodes.SUCCESS) {
                onConnectionSuccess(endpointId)
            } else {
                onConnectionFailed(endpointId, resolution)
            }
        }

        abstract fun onConnectionSuccess(endpointId: String)
        abstract fun onConnectionFailed(endpointId: String, resolution: ConnectionResolution)
    }

    abstract class DiscovererCallbacks : EndpointDiscoveryCallback()
}
