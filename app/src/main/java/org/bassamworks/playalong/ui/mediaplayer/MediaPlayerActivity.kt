package org.bassamworks.playalong.ui.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bassamworks.playalong.R

class MediaPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_player_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MediaPlayerFragment.newInstance())
                .commitNow()
        }
    }
}
