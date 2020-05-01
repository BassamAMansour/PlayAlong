package org.bassamworks.playalong.extentions

import android.app.Application
import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi

object Notification {
    fun NotificationManager.shouldCreateChannel(channelId: String) =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !channelExists(channelId)

    @RequiresApi(Build.VERSION_CODES.O)
    fun NotificationManager.channelExists(channelId: String) = getNotificationChannel(channelId) != null
}

object Application {
    fun Application.isNightModeActive(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

object PlaybackState {
    fun PlaybackStateCompat.isPrepared() =
        (state == PlaybackStateCompat.STATE_BUFFERING) ||
                (state == PlaybackStateCompat.STATE_PLAYING) ||
                (state == PlaybackStateCompat.STATE_PAUSED)

    fun PlaybackStateCompat.isPlaying() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING)

    fun PlaybackStateCompat.isSkipToNextEnabled() = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L

    fun PlaybackStateCompat.isSkipToPreviousEnabled() = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L

    fun PlaybackStateCompat.isPlayEnabled() = (actions and PlaybackStateCompat.ACTION_PLAY != 0L) ||
            ((actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                    (state == PlaybackStateCompat.STATE_PAUSED))

    fun PlaybackStateCompat.isPauseEnabled() = (actions and PlaybackStateCompat.ACTION_PAUSE != 0L) ||
            ((actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                    (state == PlaybackStateCompat.STATE_BUFFERING || state == PlaybackStateCompat.STATE_PLAYING))
}