package app.car.raceboxparser.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager

val Context.hasBle: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

val Context.btManager: BluetoothManager
    get() = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

val Context.btAdapter: BluetoothAdapter
    get() = btManager.adapter

