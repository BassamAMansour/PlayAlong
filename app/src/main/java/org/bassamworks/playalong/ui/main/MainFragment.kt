package org.bassamworks.playalong.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.bassamworks.playalong.databinding.MainFragmentBinding


class MainFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProviders.of(this, MainViewModelFactory(activity!!.application))
            .get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = mainViewModel
        binding.btnSendFile.setOnClickListener { sendFile() }

        return binding.root
    }

    private fun sendFile() {
        mainViewModel.connectedEndpoints.value?.forEach { endpointId ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"
            intent.putExtra(EXTRA_ENDPOINT_ID, endpointId)

            startActivityForResult(intent, RC_OPEN_AUDIO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == RC_OPEN_AUDIO && resultCode == Activity.RESULT_OK) {
            mainViewModel.sendAudio(resultData?.data!!)
        }
    }

    companion object {
        const val EXTRA_ENDPOINT_ID = "org.bassamworks.playalong.EndpointId"
        const val RC_OPEN_AUDIO = 10
    }
}
