package com.simplypatrick.gattserver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Bundle
import android.os.ParcelUuid

class MainActivity : ConsoleActivity() {

    private val bluetoothManager by lazy {
        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager.adapter?.let { bta ->
            println("Friendly name: ${bta.name}")
            println("Adapter address: ${bta.address}")

            if (bta.isEnabled) {
                startAdvertising()
                startServer()
            } else {
                bta.enable()
            }
        }
    }

    private lateinit var server: BluetoothGattServer
    private lateinit var advertiser: BluetoothLeAdvertiser

    private fun startServer() {
        server = bluetoothManager.openGattServer(this, object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                server.readPhy(device)
                println("onConnectionStateChange: $status, ${ConnectionState.fromInt(newState)}")
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
                println("onCharacteristicReadRequest: reqId=$requestId, offset=$offset, characteristic=$characteristic")
                val now = System.currentTimeMillis()
                when (characteristic.uuid) {
                    TimeProfile.CURRENT_TIME -> server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                            TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE))
                    TimeProfile.LOCAL_TIME_INFO -> server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                            TimeProfile.getLocalTimeInfo(now))
                    else -> server.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }

            override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
                                                      preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                println("onCharacteristicWriteRequest: $requestId, $characteristic, $preparedWrite, $responseNeeded, $offset, $value")
            }

            override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
                println("onDescriptorReadRequest: $requestId, $offset, $descriptor")
            }

            override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?,
                                                  preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                println("onDescriptorWriteRequest: $requestId, $descriptor, $preparedWrite, $responseNeeded, $offset, $value")
            }

            override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
                println("onPhyRead: txPhy=$txPhy, rxPhy=$rxPhy, status=$status")
            }

            override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
                println("onPhyUpdate: txPhy=$txPhy, rxPhy=$rxPhy, status=$status")
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                println("onNotificationSent: status=$status")
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                println("onMtuChanged: mtu=$mtu")
            }

            override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
                println("onExecuteWrite: requestId=$requestId, execute=$execute")
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                println("onServiceAdded: status=$status uuid=${service.uuid} type=${service.type} instanceId=${service.instanceId}")
            }
        })
        server.addService(TimeProfile.createTimeService())
    }

    private fun startAdvertising() {
        advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        advertiser.startAdvertising(
                AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                        .setConnectable(true)
                        .setTimeout(0)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                        .build(),
                AdvertiseData.Builder()
                        .setIncludeDeviceName(true)
                        .setIncludeTxPowerLevel(false)
                        .addServiceUuid(ParcelUuid(TimeProfile.TIME_SERVICE))
                        .build(),
                object : AdvertiseCallback() {
                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                        println("LE Advertising started: settings=$settingsInEffect")
                    }

                    override fun onStartFailure(errorCode: Int) {
                        println("LE Advertising failed: error=$errorCode")
                    }
                }
        )
    }
}

