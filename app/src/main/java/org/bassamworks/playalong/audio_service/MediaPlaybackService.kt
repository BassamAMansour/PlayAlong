package org.bassamworks.playalong.audio_service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.bassamworks.playalong.audio_service.MediaSessionCallbacks.Companion.FULL_PLAYBACK_ACTIONS
import org.bassamworks.playalong.ui.mediaplayer.MediaPlayerActivity

class MediaPlaybackService : MediaBrowserServiceCompat() {

    lateinit var mediaSession: MediaSessionCompat

    val notificationManager: MediaNotificationManager by lazy { MediaNotificationManager(this) }

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(FULL_PLAYBACK_ACTIONS)

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {
            setPlaybackState(stateBuilder.build())
            setCallback(MediaSessionCallbacks(this@MediaPlaybackService))
            setSessionToken(sessionToken)
            setSessionActivity(getSessionActivityIntent())
        }
    }

    override fun onLoadChildren(parentMediaId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        if (EMPTY_MEDIA_ROOT_ID == parentMediaId) {
            result.sendResult(null)
            return
        }
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
        if (MEDIA_ROOT_ID == parentMediaId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }

        result.sendResult(mediaItems)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaSession.run {
            isActive = false
            release()
        }

        serviceJob.cancel()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return if (allowBrowsing(clientPackageName, clientUid)) {
            BrowserRoot(MEDIA_ROOT_ID, null)
        } else {
            BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    private fun allowBrowsing(clientPackageName: String, clientUid: Int): Boolean {
        //TODO:Implement
        return clientPackageName == application.packageName
    }

    private fun getSessionActivityIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            RC_OPEN_MEDIA_PLAYER,
            Intent(this, MediaPlayerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    companion object {
        private const val MEDIA_ROOT_ID = "media_root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val RC_OPEN_MEDIA_PLAYER = 110
        private val LOG_TAG = this::class.java.simpleName
    }
}

