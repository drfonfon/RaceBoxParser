package app.car.raceboxparser.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import app.car.raceboxparser.ble.BleScanResult.Devices
import app.car.raceboxparser.ble.BleScanResult.NotEnabled
import app.car.raceboxparser.ble.BleScanResult.NotSupported

sealed interface BleScanResult {

    object Idle: BleScanResult

    object NotSupported : BleScanResult

    object NotEnabled : BleScanResult

    data class Devices(
        val data: List<Pair<String, String>>
    ) : BleScanResult
}

@SuppressLint("MissingPermission")
@Composable
fun BleScan(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onResult: (BleScanResult) -> Unit
) {
    val context = LocalContext.current
    val btAdapter = context.btAdapter
    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.scanRecord?.serviceUuids?.map {
                result.device.address to (result.scanRecord?.deviceName ?: "device")
            }?.let {
                onResult(Devices(it))
            }
        }
    }
    DisposableEffect(Unit) {
        if (context.hasBle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (btAdapter.isEnabled) {
                        btAdapter.bluetoothLeScanner.startScan(scanCallback)
                    } else {
                        onResult(NotEnabled)
                    }
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    btAdapter.bluetoothLeScanner.stopScan(scanCallback)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        } else {
            onResult(NotSupported)
            onDispose {}
        }
    }

}
