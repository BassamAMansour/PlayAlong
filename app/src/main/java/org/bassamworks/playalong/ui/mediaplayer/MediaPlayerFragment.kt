package org.bassamworks.playalong.ui.mediaplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.bassamworks.playalong.R

class MediaPlayerFragment : Fragment() {

    companion object {
        fun newInstance() = MediaPlayerFragment()
    }

    private lateinit var viewModel: MediaPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.media_player_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MediaPlayerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
