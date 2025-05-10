package com.esteban.ruano.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionManager(
    caller: ActivityResultCaller,
    private val context: Context,
    private val shouldShowPermissionRationale: (permission: String) -> Boolean,
) {
    private var onPermissionsGranted: ((isGranted: Boolean) -> Unit)? = null

    constructor(activity: ComponentActivity) : this(
        caller = activity,
        context = activity,
        shouldShowPermissionRationale = { activity.shouldShowRequestPermissionRationale(it) },
    )

    private val requestPermissionLauncher =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onPermissionsGranted?.invoke(isGranted)
        }

    private val requestMultiplePermissionsLauncher =
        caller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val isGranted = result.values.all { it }
            onPermissionsGranted?.invoke(isGranted)
        }

    fun checkPermissions(
        vararg permissions: String,
        onPermissionsGranted: ((isGranted: Boolean) -> Unit)? = null,
    ) {
        this.onPermissionsGranted = onPermissionsGranted

        val permissionsToBeRequested = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission,
            ) != PackageManager.PERMISSION_GRANTED
        }
        val shouldShowRequestPermissionRationale = permissionsToBeRequested.any {
            shouldShowPermissionRationale.invoke(it)
        }

        when {
            permissionsToBeRequested.isEmpty() -> onPermissionsGranted?.invoke(true)
            shouldShowRequestPermissionRationale -> {
                // Ensure show permission rationale before requesting permission
                onPermissionsGranted?.invoke(false)
            }
            else -> requestPermissions(permissionsToBeRequested)
        }
    }

    fun checkMediaPermissions(
        vararg permissions: MediaPermission,
        onPermissionsGranted: ((isGranted: Boolean) -> Unit)? = null,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(
                *permissions.map { it.getGranularMediaPermission() }.toTypedArray(),
                onPermissionsGranted = onPermissionsGranted,
            )
        } else {
            checkPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                onPermissionsGranted = onPermissionsGranted,
            )
        }
    }

    private fun requestPermissions(permissionsToBeRequested: List<String>) {
        if (permissionsToBeRequested.size > 1) {
            requestMultiplePermissionsLauncher.launch(permissionsToBeRequested.toTypedArray())
        } else {
            requestPermissionLauncher.launch(permissionsToBeRequested.firstOrNull())
        }
    }

    enum class MediaPermission {
        IMAGES {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun getGranularMediaPermission() = Manifest.permission.READ_MEDIA_IMAGES
        },
        VIDEO {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun getGranularMediaPermission() = Manifest.permission.READ_MEDIA_VIDEO
        },
        AUDIO {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun getGranularMediaPermission() = Manifest.permission.READ_MEDIA_AUDIO
        }, ;

        /**
         * Gets the corresponding granular media permission for Android Tiramisu (API level 33) and above.
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        abstract fun getGranularMediaPermission(): String
    }
}
