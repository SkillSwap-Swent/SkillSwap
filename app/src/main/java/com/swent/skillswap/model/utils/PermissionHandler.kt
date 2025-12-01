package com.swent.skillswap.model.utils

import android.Manifest
import android.os.Build
import android.util.Log

object PermissionHandler {
    fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Log.w("MainActivity", "Fine location permission granted")
        }
        if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            Log.w("MainActivity", "Coarse location permission granted")
        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        ) {
            Log.w("MainActivity", "Notification permission granted")
        }
        if (permissions.values.none { it }) {
            Log.w("MainActivity", "No permissions granted")
        }
    }
}
