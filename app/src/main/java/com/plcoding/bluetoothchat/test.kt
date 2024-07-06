package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.UUID


// UUID for SPP (Serial Port Profile)
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@SuppressLint("MissingPermission")
fun createBluetoothConnection(
    deviceAddress: String,
    bluetoothAdapter: BluetoothAdapter
): String {
    val TAG = "createBluetoothConnection"
    Log.i(TAG, "createBluetoothConnection()")


//    if (bluetoothAdapter == null) {
//        // Device doesn't support Bluetooth
//        Log.i(TAG,"Device doesn't support Bluetooth")
//        return
//    }

    if (!bluetoothAdapter.isEnabled) {
        // Bluetooth is not enabled
        // You might want to ask the user to enable it
        Log.i(TAG,"Bluetooth is not enabled")
        return "FAILED: BT not enabled"
    }

    // Get the BluetoothDevice object
    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

    // Create a BluetoothSocket
    var bluetoothSocket: BluetoothSocket? = null
    try {
        Log.i(TAG,"Creating socket from UUID")
//        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.randomUUID())
        Log.i(TAG,"Creating socket from UUID COMPLETE")
    } catch (e: IOException) {
        Log.i(TAG, "IO Exception: Could not create")
        e.printStackTrace()
        return "FAILED: Could not create socket"
    }

    bluetoothAdapter.cancelDiscovery()

    try {
        Log.i(TAG, "Opening socket")
        bluetoothSocket?.connect()
        Log.i(TAG, "Opening socket COMPLETE")
        // Connection successful
        // You can now manage your connection (in a separate thread)
    } catch (connectException: IOException) {
        Log.i(TAG, "IO Exception: Could not open")
        connectException.printStackTrace()
        try {
            Log.i(TAG, "Closing Socket")
            bluetoothSocket?.close()
            Log.i(TAG, "Closing Socket COMPLETE")
        } catch (closeException: IOException) {
            Log.i(TAG, "IO Exception: Could not close")
            closeException.printStackTrace()
            return "FAILED: Could not close socket"
        }
        Log.i(TAG, "FAILED: Could not open socket")
        return "FAILED: Could not open socket"
    }

    // Manage the connection in a separate thread
    return manageConnectedSocket(bluetoothSocket)
}

fun manageConnectedSocket(socket: BluetoothSocket?): String = runBlocking {
    val TAG = "manageConnectedSocket"
    // Code to manage the connection in a separate thread
    Log.i(TAG, "manageConnectedSocket()")
    val obdConnection = socket?.let { ObdDeviceConnection(it.inputStream, it.outputStream) }
    Log.i(TAG, "OBD Connection Secured")
    val response = obdConnection?.run(SpeedCommand())
    Log.i(TAG, "VIN: $response.value")

    return@runBlocking "$response.value"

//    // Example of writing data
//    val message = "Hello, Bluetooth!"
//    try {
//        outputStream?.write(message.toByteArray())
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//
//    // Example of reading data
//    val buffer = ByteArray(1024)
//    var bytes: Int
//    try {
//        bytes = inputStream?.read(buffer) ?: 0
//        val readMessage = String(buffer, 0, bytes)
//        // Process the read message
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
}