package com.plcoding.bluetoothchat.presentation.components

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.bluetoothchat.createBluetoothConnection
import com.plcoding.bluetoothchat.createNetworkConnection
import com.plcoding.bluetoothchat.createWifiConnection
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

@Composable
fun DeviceScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,
    bluetoothAdapter: BluetoothAdapter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        userIPPortGetter()
        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            bluetoothAdapter = bluetoothAdapter
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
            }
        }
    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
    bluetoothAdapter: BluetoothAdapter
) {
    var temp by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = "Bluetooth Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(pairedDevices) { device ->
            Text(
                text = device.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable { onClick(device) }
                    .padding(16.dp)
            )
            Button(onClick = {
                GlobalScope.launch(Dispatchers.IO){
                    createBluetoothConnection(device.address, bluetoothAdapter) } }
            ){
                Text(text = "Test Connection")
            }
        }

//        item {
//            Text(
//                text = "Scanned Devices",
//                fontWeight = FontWeight.Bold,
//                fontSize = 24.sp,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//        items(scannedDevices) { device ->
//            Text(
//                text = device.name ?: "(No name)",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { onClick(device) }
//                    .padding(16.dp)
//            )
//        }
    }
}

@Composable
fun userIPPortGetter() {
    var defaultIP = "192.168.0.10"
    var defaultPort = "35000"
    var ipState by remember { mutableStateOf(TextFieldValue(defaultIP)) }
    var portState by remember { mutableStateOf(TextFieldValue(defaultPort)) }
    var userIP by remember { mutableStateOf("") }
    var userPort by remember { mutableStateOf("") }

    Text(
        text = "Paired Devices",
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        modifier = Modifier.padding(16.dp)
    )
    Text(text = "Enter device IP Address")
    BasicTextField(
        value = ipState,
        onValueChange = { ipState = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(16.dp),
        textStyle = TextStyle(color = Color.White)
    )

    Text(text = "Enter device port")
    BasicTextField(
        value = portState,
        onValueChange = { portState = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(16.dp),
        textStyle = TextStyle(color = Color.White)
    )

    Button(onClick = { GlobalScope.launch(Dispatchers.IO){
        createWifiConnection(ipState.text, portState.text.toInt())
    } } ) {
        Text(text = "Test Wifi Connection")
    }
}