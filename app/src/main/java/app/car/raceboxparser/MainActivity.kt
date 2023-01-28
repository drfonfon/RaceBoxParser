@file:OptIn(ExperimentalUnitApi::class)

package app.car.raceboxparser

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.car.raceboxparser.ble.BleScan
import app.car.raceboxparser.ble.BleScanResult
import app.car.raceboxparser.ble.BleScanResult.Devices
import app.car.raceboxparser.ble.btAdapter
import app.car.raceboxparser.racebox.CLIENT_CHARACTERISTIC_CONFIG
import app.car.raceboxparser.racebox.TX_CHARACTERISTIC_UUID
import app.car.raceboxparser.racebox.UART_SERVICE_UUID
import app.car.raceboxparser.ui.theme.PurpleGrey40
import app.car.raceboxparser.ui.theme.RaceBoxParserTheme
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {

    private val parser = RaceboxPacketParser()
    private val parserState = MutableStateFlow<Packet?>(null)
    private val connectState = MutableStateFlow<String>("Not connected")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RaceBoxParserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Connect(connectState) {
                            onDevice(it)
                        }
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(Color.White)
                        )
                        Info(parserState)
                    }
                }
            }
        }
    }

    private fun onDevice(address: String) {
        connectState.value = "Connecting"
        val device = btAdapter.getRemoteDevice(address)
        device.connectGatt(this, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (BluetoothGatt.GATT_SUCCESS == status) {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> onConnected(gatt)
                        BluetoothProfile.STATE_DISCONNECTED -> onDisconnected(gatt)
                    }
                } else {
                    onDisconnected(gatt)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                connectState.value = "Connected, getting data"
                val g = gatt ?: return
                val service = g.services.firstOrNull { it.uuid == UART_SERVICE_UUID } ?: return
                val txCh = service.getCharacteristic(TX_CHARACTERISTIC_UUID)
                g.setCharacteristicNotification(txCh, true)

                val descriptor = txCh.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                if (TX_CHARACTERISTIC_UUID == characteristic.uuid) {
                    try {
                        parserState.value = parser.parse(value)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        })

    }

    private fun onConnected(gatt: BluetoothGatt?) {
        gatt?.discoverServices()
        connectState.value = "Connected"
    }

    private fun onDisconnected(gatt: BluetoothGatt?) {
        gatt?.close()
        connectState.value = "Not connected"
    }
}

val floatFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US)).apply {
    roundingMode = RoundingMode.CEILING
}

@Composable
fun Connect(connectState: MutableStateFlow<String>, onDevice: (String) -> Unit) {
    var scanDialogShow by remember { mutableStateOf(false) }
    var device by remember { mutableStateOf("No device") }
    val state = connectState.collectAsState()
    Text("Don't forget to enable all permissions in the app settings!")
    Button(onClick = {
        scanDialogShow = true
    }) {
        Text(text = "Connect")
    }
    Text(device)
    Text(state.value)
    if (scanDialogShow) {
        ScanDialog(
            { address, name ->
                onDevice(address)
                device = "$name - $address"
                scanDialogShow = false
            },
            {
                scanDialogShow = false
            }
        )
    }
}

@Composable
fun Info(state: MutableStateFlow<Packet?>) {
    val packetState by state.collectAsState()
    if (packetState == null) {
        EmptyData()
    }
    if (packetState != null) {
        val dPacket = packetState!!
        Text(
            text = "speed:",
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )
        Text(
            floatFormat.format(dPacket.speed),
            fontSize = TextUnit(86f, TextUnitType.Sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "location:")
        Text(
            "lat: ${dPacket.latitude}, lon: ${dPacket.longitude}",
        )
        Text(text = "accelerometer:")
        Text(
            "x: ${dPacket.gForceX}, y: ${dPacket.gForceY}, z: ${dPacket.gForceZ}",
        )
        Text(text = "rotation:")
        Text(
            "x: ${dPacket.rotationRateX}, y: ${dPacket.rotationRateY}, z: ${dPacket.rotationRateZ}",
        )
    }
}

@Composable
fun EmptyData() {
    Text(
        "No data",
        fontSize = TextUnit(24f, TextUnitType.Sp)
    )
}

@Composable
fun ScanDialog(
    onDevice: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        properties = DialogProperties(dismissOnClickOutside = true),
        onDismissRequest = onDismiss
    ) {
        var result by remember { mutableStateOf<BleScanResult>(BleScanResult.Idle) }
        BleScan(onResult = { result = it })
        Card(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .background(PurpleGrey40),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column {
                Text(text = "Devices")
                Spacer(modifier = Modifier.size(24.dp))
                if (result is Devices) {
                    LazyColumn(content = {
                        items((result as Devices).data) { item ->
                            Row(
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth()
                                    .clickable { onDevice(item.first, item.second) }
                            ) {
                                Text(text = item.first)
                                Spacer(modifier = Modifier.size(16.dp))
                                Text(text = item.second)
                            }
                        }
                    })
                } else {
                    Text(text = "No devices")
                }
            }
        }
    }
}
