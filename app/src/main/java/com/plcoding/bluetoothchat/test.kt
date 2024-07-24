package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.plcoding.bluetoothchat.kotlinapi.command.ObdCommand
import com.plcoding.bluetoothchat.kotlinapi.command.ObdRawResponse
import com.plcoding.bluetoothchat.kotlinapi.command.ObdResponse
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATAT0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATE0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATL0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATS1Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATSP0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ResetAdapterCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.AvailablePIDsCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.VINCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.SpeedCommand
import com.plcoding.bluetoothchat.kotlinapi.connection.ObdDeviceConnection
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
        bluetoothSocket?.connect()
        // Connection successful
        // You can now manage your connection (in a separate thread)
    } catch (connectException: IOException) {
        connectException.printStackTrace()
        try {
            bluetoothSocket?.close()
        } catch (closeException: IOException) {
            closeException.printStackTrace()
        }
    }

    // Manage the connection in a separate thread
    val inputStream = bluetoothSocket!!.inputStream
    val outputStream = bluetoothSocket!!.outputStream
    
//    try{
//        outputStream.write(1234)
//    } catch (writeException: IOException) {
//        writeException.printStackTrace()
//    }

    manageConnectedSocket(inputStream, outputStream)
}

fun manageConnectedSocket(
    inputStream:InputStream,
    outputStream: OutputStream
) = runBlocking {
    val TAG = "manageConnectedSocket"

    val obdConnection = ObdDeviceConnection(inputStream, outputStream)
    var response: ObdResponse
    var responseCommand: String
    var responseRaw: ObdRawResponse

    // Code to manage the connection in a separate thread
    launch {
        try {
            response = obdConnection.run(ResetAdapterCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ResetAdapterCommand: FAILED")
        }
        try {
            response = obdConnection.run(ATE0Command())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATE0Command: FAILED")
        }
        try {
            response = obdConnection.run(ATL0Command())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATL0Command: FAILED")
        }
        try {
            response = obdConnection.run(ATS1Command())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATS1Command: FAILED")
        }
        try {
            response = obdConnection.run(ATAT0Command())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATAT0Command: FAILED")
        }
        try {
            response = obdConnection.run(ATSP0Command())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATSP0Command: FAILED")
        }
        try {
            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_01_TO_20))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "AvailablePIDsCommand 1: FAILED")
        }
        try {
            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_21_TO_40))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "AvailablePIDsCommand 2: FAILED")
        }
        try {
            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_41_TO_60))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "AvailablePIDsCommand 3: FAILED")
        }
        try {
            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_61_TO_80))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "AvailablePIDsCommand 4: FAILED")
        }
        try {
            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_81_TO_A0))
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "AvailablePIDsCommand 5: FAILED")
        }
        try {
            response = obdConnection.run(SpeedCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "SpeedCommand: FAILED")
        }
        try {
            response = obdConnection.run(VINCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "VINCommand: FAILED")
        }
    }
    return@runBlocking
}

class CustomCommand : ObdCommand() {
    // Required
    override val tag = "CUSTOM_COMMAND"
    override val name = "Custom Command"
    override val mode = "FF"
    override val pid = "FF"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}