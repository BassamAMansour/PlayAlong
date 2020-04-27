package org.bassamworks.playalong.ui.main

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.bassamworks.playalong.PreferenceConstants
import org.bassamworks.playalong.connections.AdvertiserOrchestrator
import org.bassamworks.playalong.connections.DiscovererOrchestrator
import org.bassamworks.playalong.connections.IncomingPayloadsManager
import org.bassamworks.playalong.connections.NearbyConnectionOrchestrator
import org.bassamworks.playalong.connections.NearbyConnectionOrchestrator.ConnectionCallbacks
import org.bassamworks.playalong.connections.NearbyConnectionOrchestrator.DiscovererCallbacks
import org.bassamworks.playalong.connections.OutgoingPayloadsManager.sendFile
import timber.log.Timber
import java.util.*

class MainViewModel(app: Application) : AndroidViewModel(app), IncomingPayloadsManager.ReceivedPayloadsListener {

    var connectedEndpoints: MediatorLiveData<Set<String>> = MediatorLiveData()
        private set

    private val preferences =
        app.getSharedPreferences(PreferenceConstants.DEFAULT_SHARED_PREF_FILE, Context.MODE_PRIVATE)
    private val username = preferences.getString(
        PreferenceConstants.KEY_ADVERTISING_USERNAME,
        UUID.randomUUID().toString().substring(10)
    ).toString()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val advertiserConnectionCallback: ConnectionCallbacks
    private val discovererConnectionCallback: ConnectionCallbacks
    private val discovererCallback: DiscovererCallbacks

    init {
        IncomingPayloadsManager.subscribe(this)

        advertiserConnectionCallback = object : ConnectionCallbacks() {

            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                advertiserOrchestrator.acceptConnection(endpointId, IncomingPayloadsManager)
            }

            override fun onConnectionSuccess(endpointId: String) {
                //TODO("Not yet implemented")
            }

            override fun onConnectionFailed(endpointId: String, resolution: ConnectionResolution) {
                //TODO("Not yet implemented")
            }

            override fun onDisconnected(endpointId: String) {
                //TODO("Not yet implemented")
            }
        }

        discovererConnectionCallback = object : ConnectionCallbacks() {

            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                discovererOrchestrator.acceptConnection(endpointId, IncomingPayloadsManager)
            }

            override fun onConnectionSuccess(endpointId: String) {
                //TODO("Not yet implemented")
            }

            override fun onConnectionFailed(endpointId: String, resolution: ConnectionResolution) {
                //TODO("Not yet implemented")
            }

            override fun onDisconnected(endpointId: String) {
                //TODO("Not yet implemented")
            }
        }

        discovererCallback = object : DiscovererCallbacks() {

            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                discovererOrchestrator.requestConnection(endpointId)
            }

            override fun onEndpointLost(endpointId: String) {
                //TODO("Not yet implemented")
            }
        }
    }

    private lateinit var connectionOrchestrator: NearbyConnectionOrchestrator

    private val advertiserOrchestrator: AdvertiserOrchestrator by lazy {
        val orchestrator = AdvertiserOrchestrator(app, username, advertiserConnectionCallback)

        connectedEndpoints.addSource(orchestrator.connectedEndpoints) { connectedEndpoints.value = it }
        connectionOrchestrator = orchestrator

        orchestrator
    }

    private val discovererOrchestrator: DiscovererOrchestrator by lazy {
        val orchestrator = DiscovererOrchestrator(app, username, discovererConnectionCallback, discovererCallback)

        connectedEndpoints.addSource(orchestrator.connectedEndpoints) { connectedEndpoints.value = it }
        connectionOrchestrator = orchestrator

        orchestrator
    }

    fun startAdvertising() = advertiserOrchestrator.startAdvertising()

    fun startDiscovery() = discovererOrchestrator.startDiscovery()

    fun stopDiscovery() = discovererOrchestrator.stopDiscovery()

    fun stopAdvertising() = advertiserOrchestrator.stopAdvertising()

    fun sendAudio(uri: Uri) {
        connectedEndpoints.value?.forEach {
            connectionOrchestrator?.sendFile(it, uri, getApplication())
        }
    }

    override fun onFileReceived(endpointId: String, fileName: String, filePath: Uri) {
        //TODO("Not yet implemented")
        Timber.v("OnFileReceived: endpointId:$endpointId - fileUri: $filePath")
    }

    override fun onStreamReceived(endpointId: String, stream: Payload.Stream) {
        //TODO("Not yet implemented")
        Timber.v("OnStreamReceived: endpointId:$endpointId - stream: $stream")
    }

    override fun onStringReceived(endpointId: String, string: String) {
        //TODO("Not yet implemented")
        Timber.v("OnStringReceived: endpointId:$endpointId - string: $string")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        IncomingPayloadsManager.unsubscribe(this)
    }

}

class MainViewModelFactory(private val app: Application) :
    ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
