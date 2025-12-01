package com.swent.skillswap.utils

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.utils.PermissionHandler
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionHandlerJvmTest {
    @Test
    fun testFineLocationGranted() {
        val permissions =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to true,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
                Manifest.permission.POST_NOTIFICATIONS to false
            )
        PermissionHandler.handlePermissionsResult(permissions)
    }

    @Test
    fun testCoarseLocationGranted() {
        val permissions =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to true,
                Manifest.permission.POST_NOTIFICATIONS to false
            )
        PermissionHandler.handlePermissionsResult(permissions)
    }

    @Test
    fun testNotificationGranted() {
        val permissions =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
                Manifest.permission.POST_NOTIFICATIONS to true
            )
        PermissionHandler.handlePermissionsResult(permissions)
    }

    @Test
    fun testNoneGranted() {
        val permissions =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
                Manifest.permission.POST_NOTIFICATIONS to false
            )
        PermissionHandler.handlePermissionsResult(permissions)
    }
}
