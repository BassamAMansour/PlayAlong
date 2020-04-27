package org.bassamworks.playalong.connections

import android.net.Uri
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bassamworks.playalong.files.FilesManager
import timber.log.Timber
import java.nio.charset.StandardCharsets

object IncomingPayloadsManager : PayloadCallback() {

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    private val listeners = mutableSetOf<ReceivedPayloadsListener>()
    private val incomingFiles = mutableMapOf<Long, Payload>()
    private val completedFiles = mutableMapOf<Long, Payload>()
    private val incomingFileNames = mutableMapOf<Long, String>()

    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        when (payload.type) {
            Payload.Type.BYTES -> handleBytes(endpointId, payload.asBytes()!!)
            Payload.Type.STREAM -> handleStream(endpointId, payload.asStream()!!)
            Payload.Type.FILE -> handleFile(payload)
        }
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        when (update.status) {
            PayloadTransferUpdate.Status.SUCCESS -> {
                Timber.i("Payload: ${update.payloadId} transfer success.")

                if (incomingFiles.containsKey(update.payloadId)) {
                    completedFiles[update.payloadId] = incomingFiles.remove(update.payloadId)!!
                    checkIfFileCompleted(endpointId, update.payloadId)
                }
            }
            PayloadTransferUpdate.Status.IN_PROGRESS -> {
                Timber.v("Payload: ${update.payloadId} in progress: ${update.bytesTransferred}/${update.totalBytes}")
            }
            PayloadTransferUpdate.Status.CANCELED -> {
                Timber.i("Payload: ${update.payloadId} got cancelled.")
            }
            PayloadTransferUpdate.Status.FAILURE -> {
                Timber.e("Payload: ${update.payloadId} failed to receive.")
            }
        }
    }


    private fun handleFile(filePayload: Payload) {
        incomingFiles[filePayload.id] = filePayload
    }

    private fun handleStream(endpointId: String, stream: Payload.Stream) {
        listeners.forEach { it.onStreamReceived(endpointId, stream) }
    }

    private fun handleBytes(endpointId: String, bytes: ByteArray) {
        val string = String(bytes, StandardCharsets.UTF_8)

        when (getStringType(string)) {
            StringType.CONTROL -> TODO()
            StringType.FILE_NAME -> addToIncomingFilenames(endpointId, string)
            StringType.OTHER -> listeners.forEach { it.onStringReceived(endpointId, string) }
        }
    }

    private fun addToIncomingFilenames(endpointId: String, string: String) {
        val splitString = string.split(":")

        val payloadId = splitString[1].toLong()
        val fileName = splitString[2]

        incomingFileNames[payloadId] = fileName

        checkIfFileCompleted(endpointId, payloadId)
    }

    private fun checkIfFileCompleted(endpointId: String, payloadId: Long) {
        if (!completedFiles.containsKey(payloadId) || !incomingFileNames.containsKey(payloadId)) return

        val file = completedFiles.remove(payloadId)!!.asFile()!!
        val fileName = incomingFileNames.remove(payloadId)!!

        ioScope.launch {
            FilesManager.moveFileToLocalDirectory(file, fileName)
                ?.let { newUri ->
                    listeners.forEach { it.onFileReceived(endpointId, fileName, newUri) }
                }
        }
    }


    private fun getStringType(string: String): StringType {
        val splitString = string.split(":")

        return when (splitString[0]) {
            OutgoingPayloadsManager.PREFIX_FILE -> StringType.FILE_NAME
            //TODO:Check for control messages
            else -> StringType.OTHER
        }
    }

    fun subscribe(receivedPayloadsListener: ReceivedPayloadsListener) {
        listeners.add(receivedPayloadsListener)
    }

    fun unsubscribe(receivedPayloadsListener: ReceivedPayloadsListener) {
        listeners.remove(receivedPayloadsListener)
    }

    interface ReceivedPayloadsListener {
        fun onFileReceived(endpointId: String, fileName: String, filePath: Uri)
        fun onStreamReceived(endpointId: String, stream: Payload.Stream)
        fun onStringReceived(endpointId: String, string: String)
    }

    enum class StringType { CONTROL, FILE_NAME, OTHER }
}