package org.bassamworks.playalong

import android.app.Application
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import org.bassamworks.playalong.connections.AdvertiserOrchestrator
import org.bassamworks.playalong.connections.DiscovererOrchestrator
import org.bassamworks.playalong.connections.NearbyConnectionOrchestrator
import java.util.*

object Injector {

    fun provideDiscovererOrchestrator(app: Application,
                                      discovererName: String = UUID.randomUUID().toString(),
                                      connectionCallbacks: NearbyConnectionOrchestrator.ConnectionCallbacks,
                                      discoveryCallback: EndpointDiscoveryCallback): DiscovererOrchestrator {
        return DiscovererOrchestrator(app, discovererName, connectionCallbacks, discoveryCallback)
    }

    fun provideAdvertiserOrchestrator(app: Application,
                                      advertiserName: String = UUID.randomUUID().toString(),
                                      connectionCallback: NearbyConnectionOrchestrator.ConnectionCallbacks): AdvertiserOrchestrator {
        return AdvertiserOrchestrator(app, advertiserName, connectionCallback)
    }
}