package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.github.eltonvs.obd.command.AdaptiveTimingMode
import com.github.eltonvs.obd.command.BadResponseException
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.at.BypassInitializationCommand
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SelectProtocolCommand
import com.github.eltonvs.obd.command.at.SetAdaptiveTimingCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


// UUID for SPP (Serial Port Profile)
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@SuppressLint("MissingPermission")
fun createBluetoothConnection(
    deviceAddress: String,
    bluetoothAdapter: BluetoothAdapter
){
    val TAG = "createBluetoothConnection"
    Log.i(TAG, "createBluetoothConnection()")


    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
        Log.i(TAG,"Device doesn't support Bluetooth")
    }

    if (!bluetoothAdapter.isEnabled) {
        // Bluetooth is not enabled
        // You might want to ask the user to enable it
        Log.i(TAG,"Bluetooth is not enabled")
    }

    // Get the BluetoothDevice object
    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

    //Get the UUID of the bluetooth device
//    val uuid:UUID = device.uuids[0].uuid

    // Create a BluetoothSocket
    var bluetoothSocket: BluetoothSocket? = null
    try {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    bluetoothAdapter.cancelDiscovery()

    try {
        bluetoothSocket!!.connect()
        // Connection successful
        // You can now manage your connection (in a separate thread)
    } catch (connectException: IOException) {
        connectException.printStackTrace()
        try {
            bluetoothSocket!!.close()
        } catch (closeException: IOException) {
            closeException.printStackTrace()
        }
    }

    // Manage the connection in a separate thread
    val inputStream = bluetoothSocket!!.inputStream
    val outputStream = bluetoothSocket!!.outputStream
    manageConnectedSocket(inputStream, outputStream)
}

fun manageConnectedSocket(
    inputStream:InputStream,
    outputStream: OutputStream
) = runBlocking {
    val TAG = "manageConnectedSocket"
    // Code to manage the connection in a separate thread
    launch{
        try {
            val obdConnection = ObdDeviceConnection(inputStream, outputStream)
            Log.i(TAG, "OBD Connection Secured")

            var response = obdConnection.run(ResetAdapterCommand())
            var responseCommand = response.command.name
            var responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")

            response = obdConnection.run(SelectProtocolCommand(ObdProtocols.AUTO))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")

            response = obdConnection.run(SpeedCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")

        }catch (obdException: RuntimeException){
            obdException.printStackTrace()
        }
    }
    return@runBlocking
}

//class PINCommand : ObdCommand() {
//    // Required
//    override val tag = "PIN_COMMAND"
//    override val name = "PIN Command"
//    override val mode = "12"
//    override val pid = "34"
//
//    //Optional
//    override val defaultUnit = ""
//    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
//}