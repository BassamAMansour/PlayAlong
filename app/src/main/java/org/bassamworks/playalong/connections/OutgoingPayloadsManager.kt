package org.bassamworks.playalong.connections

import android.content.Context
import android.net.Uri
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bassamworks.playalong.files.FilesManager.getExtension
import org.bassamworks.playalong.files.FilesManager.getFileName
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.InputStream

object OutgoingPayloadsManager {

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    fun NearbyConnectionOrchestrator.sendFile(endpointId: String, fileUri: Uri, context: Context) {
        ioScope.launch {
            try {
                val payload = Payload.fromFile(context.contentResolver.openFileDescriptor(fileUri, "r")!!)
                val filename = "${context.getFileName(fileUri)}.${context.getExtension(fileUri)}"
                val sentName =
                    "${MessageType.FILE_NAME.prefix}${MESSAGE_DELIMITER}${payload.id}${MESSAGE_DELIMITER}${filename}"

                sendPayload(endpointId, Payload.fromBytes(sentName.toByteArray()))
                sendPayload(endpointId, payload)

            } catch (e: FileNotFoundException) {
                Timber.e(e)
            }
        }
    }

    fun NearbyConnectionOrchestrator.sendBytes(endpointId: String, bytes: ByteArray) {
        sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    fun NearbyConnectionOrchestrator.sendStream(endpointId: String, inputStream: InputStream) {
        sendPayload(endpointId, Payload.fromStream(inputStream))
    }
}