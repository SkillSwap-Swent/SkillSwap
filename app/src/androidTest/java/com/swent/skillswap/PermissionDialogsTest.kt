package com.swent.skillswap

import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test that verifies the runtime permission dialogs (notification and location) are
 * shown when the app launches and the permissions are revoked.
 *
 * Notes:
 * - Notification permission is only requested on Android 13+ (TIRAMISU).
 * - The test revokes the permissions via shell before launching the activity so the dialogs should
 *   appear on startup.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PermissionDialogsTest {

    private val TIMEOUT_MS = 7_000L

    private fun revokePermission(pkg: String, permission: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val uiAutomation = instrumentation.uiAutomation
        // executeShellCommand returns a ParcelFileDescriptor — close it to avoid fd leaks
        val pfd = uiAutomation.executeShellCommand("pm revoke $pkg $permission")
        pfd?.close()
    }

    @Test
    fun permissionDialogsAppear_notificationAndLocation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val packageName = targetContext.packageName

        // Revoke location permissions so the location dialog will be shown
        revokePermission(packageName, "android.permission.ACCESS_FINE_LOCATION")
        revokePermission(packageName, "android.permission.ACCESS_COARSE_LOCATION")

        // Revoke notification permission only on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            revokePermission(packageName, "android.permission.POST_NOTIFICATIONS")
        }

        // Launch the app's main activity
        ActivityScenario.launch(MainActivity::class.java)

        // Obtain UiDevice instance for interacting with system dialogs
        val device = UiDevice.getInstance(instrumentation)

        // 1) Wait for the location permission dialog and interact with it (choose an allow option)
        val locationMessageSelector = By.textContains("location").clazz("android.widget.TextView")
        val locationMessage = device.wait(Until.findObject(locationMessageSelector), TIMEOUT_MS)

        // Try to find and click common allow button labels (order matters for UX flows)
        fun clickFirstAvailable(vararg selectors: BySelector): Boolean {
            for (sel in selectors) {
                val btn = device.wait(Until.findObject(sel), TIMEOUT_MS / 2)
                if (btn != null) {
                    btn.click()
                    return true
                }
            }
            return false
        }

        val clicked =
            if (locationMessage != null) {
                // Prefer "While using the app" or similar wording, then generic "Allow"
                clickFirstAvailable(
                    By.textContains("While using"),
                    By.textContains("Allow only while using"),
                    By.textContains("Allow")
                )
            } else {
                // If we didn't find a location message, still try to find location buttons directly
                clickFirstAvailable(
                    By.textContains("While using"),
                    By.textContains("Allow only while using"),
                    By.textContains("Allow")
                )
            }

        assertNotNull(
            "Expected to find and interact with the location permission dialog (message or allow buttons)",
            if (locationMessage != null || clicked) locationMessage ?: true else null
        )

        Thread.sleep(600)

        // 2) After choosing location permission, the notification permission dialog should appear
        // (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationSelector =
                By.textContains("notification").clazz("android.widget.TextView")
            val notificationMessage =
                device.wait(Until.findObject(notificationSelector), TIMEOUT_MS)

            // As a fallback, look for common allow button (some devices show only buttons)
            val notificationButton =
                device.wait(Until.findObject(By.textContains("Allow")), TIMEOUT_MS / 2)

            assertNotNull(
                "Expected notification permission dialog to appear after granting/choosing location permission",
                notificationMessage ?: notificationButton
            )
        }
    }
}
