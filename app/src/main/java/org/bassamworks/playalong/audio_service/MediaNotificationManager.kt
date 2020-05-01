package org.bassamworks.playalong.audio_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import org.bassamworks.playalong.R
import org.bassamworks.playalong.extentions.Notification.shouldCreateChannel
import org.bassamworks.playalong.extentions.PlaybackState.isPlayEnabled
import org.bassamworks.playalong.extentions.PlaybackState.isPlaying
import org.bassamworks.playalong.extentions.PlaybackState.isSkipToNextEnabled
import org.bassamworks.playalong.extentions.PlaybackState.isSkipToPreviousEnabled
import org.bassamworks.playalong.utils.getBitmapFromUri

class MediaNotificationManager(private val service: MediaPlaybackService) {

    private val skipToPreviousAction = NotificationCompat.Action(
        R.drawable.ic_skip_previous_24dp,
        service.getString(R.string.previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )

    private val playAction = NotificationCompat.Action(
        R.drawable.ic_play_arrow_24dp,
        service.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY)
    )

    private val pauseAction = NotificationCompat.Action(
        R.drawable.ic_pause_24dp,
        service.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val skipToNextAction = NotificationCompat.Action(
        R.drawable.ic_skip_next_24dp,
        service.getString(R.string.next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    )

    private val actionStopIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP)

    private val mediaStyle: androidx.media.app.NotificationCompat.MediaStyle by lazy {
        androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(service.mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setCancelButtonIntent(actionStopIntent)
    }

    private val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(service, NotificationManager::class.java) as NotificationManager
    }

    suspend fun buildNotification(controller: MediaControllerCompat): Notification {
        val description = controller.metadata.description

        if (notificationManager.shouldCreateChannel(NOW_PLAYING_CHANNEL_ID)) createNowPlayingChannel()

        val largeIcon = description.iconUri?.let { getBitmapFromUri(service, it) }

        val notificationBuilder =
            NotificationCompat.Builder(service, NOW_PLAYING_CHANNEL_ID).apply {
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setOnlyAlertOnce(true)
                setDeleteIntent(actionStopIntent)
                setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: Modify the icon
                color = ContextCompat.getColor(service, R.color.colorPrimaryDark) //TODO: Set color from the song

                setContentIntent(controller.sessionActivity)
                setContentText(description.subtitle)
                setContentTitle(description.title)
                setLargeIcon(largeIcon)
            }

        val playPauseIndex = addSuitableActions(notificationBuilder, controller.playbackState)
        mediaStyle.setShowActionsInCompactView(playPauseIndex)

        notificationBuilder.setStyle(mediaStyle)

        return notificationBuilder.build()
    }

    private fun addSuitableActions(builder: NotificationCompat.Builder, playbackState: PlaybackStateCompat): Int {

        var playPauseIndex = 0
        if (playbackState.isSkipToPreviousEnabled()) {
            builder.addAction(skipToPreviousAction)
            playPauseIndex++
        }

        if (playbackState.isPlaying()) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled()) {
            builder.addAction(playAction)
        }

        if (playbackState.isSkipToNextEnabled()) {
            builder.addAction(skipToNextAction)
        }

        return playPauseIndex
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(
            NOW_PLAYING_CHANNEL_ID,
            service.getString(R.string.now_playing),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = service.getString(R.string.now_playing_channel_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            enableLights(false)
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        const val NOW_PLAYING_CHANNEL_ID = "org.bassamworks.playalong.now_playing"
        const val NOTIFICATION_ID = 1000
    }
}