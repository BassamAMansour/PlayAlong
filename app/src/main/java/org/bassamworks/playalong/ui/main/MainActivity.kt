package org.bassamworks.playalong.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import org.bassamworks.playalong.R
import org.bassamworks.playalong.files.FilesManager.clearCacheFiles

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }

        requestAppPermissions()
    }

    private fun requestAppPermissions() {
        if (checkPermissionsGranted(REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                RC_PERMISSIONS
            )
        }
    }

    private fun checkPermissionsGranted(permissions: Array<String>): Boolean {
        var allGranted = true

        permissions.forEach {
            allGranted = allGranted && ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        return allGranted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            RC_PERMISSIONS -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Denied. Exiting!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Nearby.getConnectionsClient(this).stopAllEndpoints()
        clearCacheFiles()
    }

    companion object {
        private const val RC_PERMISSIONS = 0
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

