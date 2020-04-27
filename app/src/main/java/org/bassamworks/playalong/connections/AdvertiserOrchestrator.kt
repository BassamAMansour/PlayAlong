package org.bassamworks.playalong.connections

import android.app.Application
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import timber.log.Timber
import java.util.*

class AdvertiserOrchestrator(app: Application,
                             advertiserName: String = UUID.randomUUID().toString(),
                             connectionCallback: ConnectionCallbacks) :
    NearbyConnectionOrchestrator(app, advertiserName, connectionCallback) {

    private val advertisingOptions = AdvertisingOptions.Builder()
        .setStrategy(STRATEGY)
        .build()

    fun startAdvertising() {
        Nearby.getConnectionsClient(app)
            .startAdvertising(username, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { Timber.v("Advertising task success: ${it.isSuccessful}") }
    }

    fun stopAdvertising() {
        Timber.v("Stop Advertising")
        Nearby.getConnectionsClient(app).stopAdvertising()
    }
}