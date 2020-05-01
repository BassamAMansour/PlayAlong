package org.bassamworks.playalong.audio_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat


class MediaSessionCallbacks(private val service: MediaPlaybackService) : MediaSessionCompat.Callback() {

    private val becomingNoisyReceiver = BecomingNoisyReceiver(service, service.sessionToken!!)

    override fun onPlay() {
        super.onPlay()

        service.mediaSession.isActive = true
        becomingNoisyReceiver.register()
    }

    override fun onPause() {
        super.onPause()

        service.mediaSession.isActive = true
        becomingNoisyReceiver.unregister()
    }

    override fun onStop() {
        super.onStop()

        service.mediaSession.isActive = false
        service.stopSelf()
        becomingNoisyReceiver.unregister()
    }

    private class BecomingNoisyReceiver(private val context: Context, sessionToken: MediaSessionCompat.Token) :
        BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val controller = MediaControllerCompat(context, sessionToken)

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }

    companion object {
        const val RESTRICTED_PLAYBACK_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_FROM_URI or
                    PlaybackStateCompat.ACTION_PREPARE or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
                    PlaybackStateCompat.ACTION_STOP

        const val FULL_PLAYBACK_ACTIONS =
            RESTRICTED_PLAYBACK_ACTIONS or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT

    }
}
