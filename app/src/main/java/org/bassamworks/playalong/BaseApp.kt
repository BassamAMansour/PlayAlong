package org.bassamworks.playalong

import android.app.Application
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
