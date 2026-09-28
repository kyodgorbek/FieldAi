package org.yodgorbek.fieldai.shared.data.native

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager as AndroidBleManager
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import org.yodgorbek.fieldai.shared.domain.native.BluetoothDevice
import org.yodgorbek.fieldai.shared.domain.native.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

class AndroidBluetoothManager(
    private val context: Context
) : BluetoothManager {
    
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as AndroidBleManager
    private val adapter = bluetoothManager.adapter
    private val scanner = adapter?.bluetoothLeScanner
    
    @SuppressLint("MissingPermission")
    override fun scanDevices(): Flow<List<BluetoothDevice>> = callbackFlow {
        if (scanner == null) {
            close(Exception("Bluetooth not supported or not enabled"))
            return@callbackFlow
        }
        
        val devices = mutableMapOf<String, BluetoothDevice>()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
                    val name = device.name ?: "Unknown Device"
                    val address = device.address
                    devices[address] = BluetoothDevice(id = address, name = name, isConnected = false)
                    trySend(devices.values.toList())
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                close(Exception("Scan failed with error code: $errorCode"))
            }
        }
        
        scanner.startScan(scanCallback)
        
        awaitClose {
            scanner.stopScan(scanCallback)
        }
    }

    override suspend fun connectToDevice(deviceId: String): Boolean {
        // GATT connection logic would go here
        return false
    }

    override suspend fun disconnect(deviceId: String) {
        // GATT disconnect logic would go here
    }
}
