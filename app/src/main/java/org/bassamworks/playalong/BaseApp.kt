package org.bassamworks.playalong

import android.app.Application
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import org.bassamworks.playalong.files.FilesManager
import timber.log.Timber
import timber.log.Timber.DebugTree


class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())

        FilesManager.app = this
    }
}