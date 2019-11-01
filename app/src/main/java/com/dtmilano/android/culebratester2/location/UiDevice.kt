package com.dtmilano.android.culebratester2.location

import android.util.Log
import androidx.test.uiautomator.By
import com.dtmilano.android.culebratester2.Holder
import com.dtmilano.android.culebratester2.ObjectStore
import com.dtmilano.android.culebratester2.convertWindowHierarchyDumpToJson
import com.dtmilano.android.culebratester2.utils.bySelectorBundleFromString
import com.dtmilano.android.culebratester2.utils.uiSelectorBundleFromString
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.server.models.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "UiDevice"
private const val REMOVE_TEMP_FILE_DELAY = 2000L

@KtorExperimentalLocationsAPI
@Location("/uiDevice")
class UiDevice {
    @Location("/dumpWindowHierarchy")
    data class DumpWindowHierarchy(val format: String = "JSON") {
        fun response(): String {
            val output = ByteArrayOutputStream()
            Holder.uiDevice.dumpWindowHierarchy(output)
            return convertWindowHierarchyDumpToJson(output.toString())
        }
    }

    @Location("/screenshot")
    data class Screenshot(val scale: Float = 1.0F, val quality: Int = 90) {
        /**
         * Returns a screenshot as a [File] response.
         */
        fun response(): File {
            //Log.d(TAG, "getting screenshot")
            val tempFile = createTempFile()
            if (Holder.uiDevice.takeScreenshot(tempFile, scale, quality)) {
                //Log.d("UiDevice", "returning screenshot file: " + tempFile.absolutePath)
                GlobalScope.launch { delay(REMOVE_TEMP_FILE_DELAY); removeTempFile(tempFile) }
                return tempFile
            }
            throw RuntimeException("Cannot get screenshot")
        }

        /**
         * Removes a temporary file.
         */
        private fun removeTempFile(tempFile: File) {
            if (!tempFile.delete()) {
                Log.w(TAG, "Temporary file ${tempFile.absolutePath} couldn't be deleted.")
            }
        }

        /**
         * Creates a temporary file to hold the screenshot.
         */
        private fun createTempFile(): File {
            val tempDir = Holder.cacheDir
            return File.createTempFile("screenshot", "png", tempDir)
        }
    }

    @Location("/click")
    data class Click(val x: Int, val y: Int) {
        fun response(): StatusResponse {
            //Log.d("UiDevice", "clicking on ($x,$y)")
            if (Holder.uiDevice.click(x, y)) {
                return StatusResponse(StatusResponse.Status.OK)
            }
            return StatusResponse(StatusResponse.Status.ERROR, errorMessage = "Cannot click")
        }
    }

    /**
     * Gets the current package name
     * Gets the current package name
     */
    @Location("/currentPackageName")
    class CurrentPackageName {
        fun response(): io.swagger.server.models.CurrentPackageName {
            return CurrentPackageName(Holder.uiDevice.currentPackageName)
        }
    }

    /**
     * Gets the display height
     * Gets the display height
     */
    @Location("/displayHeight")
    class DisplayHeight {
        fun response(): io.swagger.server.models.DisplayHeight {
            return DisplayHeight(Holder.uiDevice.displayHeight)
        }
    }

    /**
     * Gets the display rotation
     * Gets the display rotation
     */
    @Location("/displayRotation")
    class DisplayRotation {
        fun response(): io.swagger.server.models.DisplayRotation {
            return DisplayRotation(DisplayRotationEnum.of(Holder.uiDevice.displayRotation))
        }
    }

    /**
     * Gets the display size in DP
     * Gets the display size in DP
     */
    @Location("/displaySizeDp")
    class DisplaySizeDp {
        fun response(): io.swagger.server.models.DisplaySizeDp {
            val dp = Holder.uiDevice.displaySizeDp
            return DisplaySizeDp(dp.x, dp.y)
        }
    }

    /**
     * Gets the display width
     * Gets the display width
     */
    @Location("/displayWidth")
    class DisplayWidth {
        fun response(): io.swagger.server.models.DisplayWidth {
            return DisplayWidth(Holder.uiDevice.displayWidth)
        }
    }

    @Location("/findObject")
    class FindObject {
        /**
         * Finds an object
         * Finds an object. The object found, if any, can be later used in other call like API.click.
         * @param resourceId the resource id (optional)
         * @param uiSelector the selectorStr sets the resource name criteria for matching. A UI element will be considered a match if its resource name exactly matches the selectorStr parameter and all other criteria for this selectorStr are met. The format of the selectorStr string is &#x60;sel@[\$]value,...&#x60; Where &#x60;sel&#x60; can be one of -  clickable -  depth -  desc -  res -  text -  scrollable &#x60;@&#x60; replaces the &#x60;&#x3D;&#x60; sign that is used to separate parameters and values in the URL. If the first character of value is &#x60;$&#x60; then a &#x60;Pattern&#x60; is created. (optional)
         * @param bySelector the selectorStr sets the resource name criteria for matching. A UI element will be considered a match if its resource name exactly matches the selectorStr parameter and all other criteria for this selectorStr are met. The format of the selectorStr string is &#x60;sel@[\$]value,...&#x60; Where &#x60;sel&#x60; can be one of - clickable - depth - desc - res - text - scrollable &#x60;@&#x60; replaces the &#x60;&#x3D;&#x60; sign that is used to separate parameters and values in the URL. If the first character of value is &#x60;$&#x60; then a &#x60;Pattern&#x60; is created. (optional)
         */
        data class Get(
            val resourceId: String? = null,
            val uiSelector: String? = null,
            val bySelector: String? = null
        ) {
            fun response(): Any {
                if (resourceId ?: uiSelector ?: bySelector == null) {
                    return StatusResponse(
                        StatusResponse.Status.ERROR,
                        StatusResponse.StatusCode.ARGUMENT_MISSING.value,
                        errorMessage = "A resource Id or selector must be specified"
                    )
                }

                resourceId?.let {
                    val selector = By.res(resourceId)
                    val obj = Holder.uiDevice.findObject(selector)
                    val oid = ObjectStore.instance.put(it)
                    return@response ObjectRef(oid, obj.className)
                }

                uiSelector?.let {
                    val usb = uiSelectorBundleFromString(it)
                    val obj = Holder.uiDevice.findObject(usb.selector)
                    val oid = ObjectStore.instance.put(it)
                    return@response ObjectRef(oid, obj.className)
                }

                bySelector?.let {
                    val bsb = bySelectorBundleFromString(it)
                    val obj = Holder.uiDevice.findObject(bsb.selector)
                    val oid = ObjectStore.instance.put(it)
                    return@response ObjectRef(oid, obj.className)
                }

                return StatusResponse(
                    StatusResponse.Status.ERROR,
                    StatusResponse.StatusCode.OBJECT_NOT_FOUND.value,
                    StatusResponse.StatusCode.OBJECT_NOT_FOUND.message()
                )
            }
        }

        /**
         * Finds an object
         * Finds an object. The object found, if any, can be later used in other call like API.click.
         * @param body Selector
         */
        // WARNING: ktor is not passing this argument so the '?' and null are needed
        // see https://github.com/ktorio/ktor/issues/190
        data class Post(val body: Selector? = null) {
            fun response(selector: Selector): Any {

                val obj = Holder.uiDevice.findObject(selector.toBySelector())
                println("🔮obj: $obj")

                obj?.let {
                    val oid = ObjectStore.instance.put(it)
                    return ObjectRef(oid, it.className)
                }

                return StatusResponse(
                    StatusResponse.Status.ERROR,
                    StatusResponse.StatusCode.OBJECT_NOT_FOUND.value,
                    StatusResponse.StatusCode.OBJECT_NOT_FOUND.message()
                )
            }
        }
    }

    /**
     * Retrieves the text from the last UI traversal event received.
     * Retrieves the text from the last UI traversal event received.
     */
    @Location("/lastTraversedText")
    class LastTraversedText {
        fun response(): io.swagger.server.models.LastTraversedText {
            return LastTraversedText(Holder.uiDevice.lastTraversedText)
        }
    }

    companion object {
        fun pressKeyResponse(pressAny: () -> Boolean, name: String): StatusResponse {
            if (pressAny()) {
                return StatusResponse(StatusResponse.Status.OK)
            }
            return StatusResponse(StatusResponse.Status.ERROR, errorMessage = "Cannot press $name")
        }
    }

    /**
     * Simulates a short press on the BACK button.
     * Simulates a short press on the BACK button.
     */
    @Location("/pressBack")
    class PressBack {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressBack, "BACK")
        }
    }

    /**
     * Simulates a short press on the DELETE key.
     * Simulates a short press on the DELETE key.
     */
    @Location("/pressDelete")
    class PressDelete {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressDelete, "DELETE")
        }
    }

    /**
     * Simulates a short press on the ENTER key.
     * Simulates a short press on the ENTER key.
     */
    @Location("/pressEnter")
    class PressEnter {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressEnter, "ENTER")
        }
    }

    /**
     * Simulates a short press on the HOME button.
     * Simulates a short press on the HOME button.
     */
    @Location("/pressHome")
    class PressHome {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressHome, "HOME")
        }
    }

    /**
     * Simulates a short press using a key code.
     * Simulates a short press using a key code.
     * @param keyCode the key code of the event.
     * @param metaState an integer in which each bit set to 1 represents a pressed meta key (optional)
     */
    @Location("/pressKeyCode")
    class PressKeyCode(private val keyCode: Int, private val metaState: Int = 0) {
        fun response(): StatusResponse {
            if (Holder.uiDevice.pressKeyCode(keyCode, metaState)) {
                return StatusResponse(StatusResponse.Status.OK)
            }
            return StatusResponse(
                StatusResponse.Status.ERROR,
                StatusResponse.StatusCode.INTERACTION_KEY.value,
                errorMessage = "Cannot press KeyCode"
            )
        }
    }

    /**
     * Retrieves the product name of the device.
     * Retrieves the product name of the device.
     */
    @Location("/productName")
    class ProductName {
        fun response(): io.swagger.server.models.ProductName {
            return ProductName(Holder.uiDevice.productName)
        }
    }

    /**
     * Waits for the current application to idle.
     * Waits for the current application to idle.
     * @param timeout in milliseconds (optional)
     */
    @Location("/waitForIdle")
    data class WaitForIdle(val timeout: Long = 10_000) {
        fun response(): StatusResponse {
            Holder.uiDevice.waitForIdle(timeout)
            return StatusResponse(StatusResponse.Status.OK)
        }
    }

    /**
     * Waits for a window content update event to occur.
     * If a package name for the window is specified, but the current window does not have the same package name, the function returns immediately.
     * @param timeout in milliseconds
     * @param packageName the specified window package name (can be null). If null, a window update from any front-end window will end the wait (optional)
     */
    @Location("/waitForWindowUpdate")
    data class WaitForWindowUpdate(
        val timeout: Long,
        val packageName: String? = null
    ) {
        fun response(): StatusResponse {
            val t0 = System.currentTimeMillis()
            if (Holder.uiDevice.waitForWindowUpdate(packageName, timeout)) {
                return StatusResponse(StatusResponse.Status.OK)
            }
            val t1 = System.currentTimeMillis() - t0
            return StatusResponse(
                StatusResponse.Status.ERROR,
                StatusResponse.StatusCode.TIMEOUT_WINDOW_UPDATE.value,
                if (packageName != null && t1 < timeout) "Current window does not have the same package name" else "Timeout waiting for window update"
            )
        }
    }
}