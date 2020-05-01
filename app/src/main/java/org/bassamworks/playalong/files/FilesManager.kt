package org.bassamworks.playalong.files

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*

object FilesManager {

    lateinit var app: Application

    suspend fun moveFileToLocalDirectory(filePayload: Payload.File, filename: String): Uri? {
        val oldUri = Uri.fromFile(filePayload.asJavaFile())
        return withContext(Dispatchers.IO) {
            try {
                val newFile = File(app.cacheDir, filename)
                val inputStream: InputStream = app.contentResolver.openInputStream(oldUri)!!
                copyStream(inputStream, FileOutputStream(newFile))
                Uri.fromFile(newFile)
            } catch (e: IOException) {
                Timber.e(e)
                null
            } finally {
                val delete = filePayload.asJavaFile()?.parentFile?.deleteRecursively()
                Timber.v("Nearby temporary file deletion success: $delete")
            }
        }
    }

    @Throws(IOException::class)
    private fun copyStream(inputStream: InputStream, out: OutputStream) {
        try {
            val buffer = ByteArray(1024 * 8)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            out.flush()
        } finally {
            inputStream.close()
            out.close()
        }
    }

    fun Context.getExtension(fileUri: Uri): String {
        return if (fileUri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(fileUri))!!
        } else {
            //If scheme is a File
            File(fileUri.path!!).extension
        }
    }

    suspend fun Context.getFileName(fileUri: Uri): String {
        return withContext(Dispatchers.IO) {
            contentResolver.query(fileUri, null, null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }.toString()
        }
    }

    fun Context.clearCacheFiles() {
        CoroutineScope(Dispatchers.IO).launch{
            cacheDir.deleteRecursively()
        }
    }
}