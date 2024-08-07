package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Chronometer
import com.plcoding.bluetoothchat.kotlinapi.command.ObdCommand
import com.plcoding.bluetoothchat.kotlinapi.command.ObdRawResponse
import com.plcoding.bluetoothchat.kotlinapi.command.ObdResponse
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATAT0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATE0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATL0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATLPCommand
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATS1Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATSP0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ResetAdapterCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.AvailablePIDsCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.VINCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.RPMCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.RelativeThrottlePositionCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.SpeedCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.ThrottlePositionCommand
import com.plcoding.bluetoothchat.kotlinapi.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException
import java.util.UUID
import kotlin.system.measureTimeMillis


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

    Log.i(TAG, "Opening API with Bluetooth Device")
    manageConnectedSocket(inputStream, outputStream)

    inputStream.close()
    outputStream.close()
    bluetoothSocket.close()
}

fun createWifiConnection(
    hostIP: String,
    portAdd: Int
) {
    val TAG = "createWifiConnection"
    try {
        val socket = Socket(hostIP, portAdd)
        val outputStream: OutputStream = socket.getOutputStream()
        val inputStream: InputStream = socket.getInputStream()

        Log.i(TAG, "Opening API with Network Device")
        manageConnectedSocket(inputStream, outputStream)

        inputStream.close()
        outputStream.close()
        socket.close()
    }  catch (e: UnknownHostException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return
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

    // Code to manage the connection in a separate thread create a class that can call a command, then log the responses, optional side quest - update ui
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
        delay(2500)
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
            responseRaw = response.value
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATSP0Command: FAILED")
        }

        //setting up some logging variables
        var successes:Double = 0.00
        var speedSucc:Double = 0.00
        var rpmSucc:Double = 0.00
        var tpSucc:Double = 0.00
        var rtpSucc:Double = 0.00
        var failures:Double = 0.00
        var speedFail:Double = 0.00
        var rpmFail:Double = 0.00
        var tpFail:Double = 0.00
        var rtpFail:Double = 0.00

        //starting the test portion of the app to generate log report
        var duration = measureTimeMillis{
            repeat(100) {
                try {
                    response = obdConnection.run(SpeedCommand())
                    responseCommand = response.command.name
                    responseRaw = response.rawResponse
                    Log.i(TAG, "$responseCommand: $responseRaw")
                    successes++
                    speedSucc++
                } catch (obdException: RuntimeException) {
                    //obdException.printStackTrace()
                    Log.i(TAG, "Speed Command: FAILED")
                    failures++
                    speedFail++
                }
                try {
                    response = obdConnection.run(RPMCommand())
                    responseCommand = response.command.name
                    responseRaw = response.rawResponse
                    Log.i(TAG, "$responseCommand: $responseRaw")
                    successes++
                    rpmSucc++
                } catch (obdException: RuntimeException) {
                    //obdException.printStackTrace()
                    Log.i(TAG, "RPM Command: FAILED")
                    failures++
                    rpmFail++
                }
                try {
                    response = obdConnection.run(ThrottlePositionCommand())
                    responseCommand = response.command.name
                    responseRaw = response.rawResponse
                    Log.i(TAG, "$responseCommand: $responseRaw")
                    successes++
                    tpSucc++
                } catch (obdException: RuntimeException) {
                    //obdException.printStackTrace()
                    Log.i(TAG, "Throttle Position Command: FAILED")
                    failures++
                    tpFail++
                }
                try {
                    response = obdConnection.run(RelativeThrottlePositionCommand())
                    responseCommand = response.command.name
                    responseRaw = response.rawResponse
                    Log.i(TAG, "$responseCommand: $responseRaw")
                    successes++
                    rtpSucc++
                } catch (obdException: RuntimeException) {
                    //obdException.printStackTrace()
                    Log.i(TAG, "Relative Throttle Position Command: FAILED")
                    failures++
                    rtpFail++
                }
            } //repeat closing
        } // duration closing
        try {
            response = obdConnection.run(ATLPCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            //obdException.printStackTrace()
            Log.i(TAG, "ATLPCommand: FAILED")
        }
        //Log Report on tested loop
        var succRatio : Double = successes/(successes + failures)
        var speedSuccRatio : Double = speedSucc/(speedSucc + speedFail)
        var rpmSuccRatio : Double = rpmSucc/(rpmSucc + rpmFail)
        var tpSuccRatio : Double = tpSucc/(tpSucc + tpFail)
        var rtpSuccRatio : Double = rtpSucc/(rtpSucc + rtpFail)
        var succPerSec = successes / (duration/1000)

        Log.i(TAG, "Successes:      $successes")
        Log.i(TAG, "        Speed:      $speedSucc")
        Log.i(TAG, "        RPM:        $rpmSucc")
        Log.i(TAG, "        Throttle:   $tpSucc")
        Log.i(TAG, "        RThrottle:  $rtpSucc")
        Log.i(TAG, "Failures:       $failures")
        Log.i(TAG, "        Speed:      $speedFail")
        Log.i(TAG, "        RPM:        $rpmFail")
        Log.i(TAG, "        Throttle:   $tpFail")
        Log.i(TAG, "        RThrottle:  $rtpFail")
        Log.i(TAG, "Success Ratio:  $succRatio")
        Log.i(TAG, "        Speed:      $speedSuccRatio")
        Log.i(TAG, "        RPM:        $rpmSuccRatio")
        Log.i(TAG, "        Throttle:   $tpSuccRatio")
        Log.i(TAG, "        RThrottle:  $rtpSuccRatio")
        Log.i(TAG, "Duration:       $duration ms")
        Log.i(TAG, "Successes/Second: $succPerSec")
        return@launch
    }
    return@runBlocking
}