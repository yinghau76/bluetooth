package com.simplypatrick.gattserver

import android.arch.lifecycle.ViewModelProviders
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
import android.os.Bundle
import android.os.ParcelUuid
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val bluetoothManager by lazy {
        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bta = bluetoothManager.adapter
        if (bta.isEnabled) {
            startAdvertising()
            startServer()
        } else {
            bta.enable()
        }
    }

    private lateinit var server: BluetoothGattServer

    enum class ConnectionState(val state: Int) {
        Disconnected(0),
        Connecting(1),
        Connected(2),
        Disconnecting(3);

        companion object {
            private val map = ConnectionState.values().associateBy(ConnectionState::state)
            fun fromInt(state: Int) = map[state]
        }
    }

    private fun startServer() {
        server = bluetoothManager.openGattServer(this, object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                println("onConnectionStateChange: $status, ${ConnectionState.fromInt(newState)}")
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
                println("onCharacteristicReadRequest: $requestId, $offset, $characteristic")
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
                println("onPhyRead: $txPhy, $rxPhy, $status")
            }

            override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
                println("onPhyUpdate: $txPhy, $rxPhy, $status")
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                println("onNotificationSent: $status")
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                println("onMtuChanged: $mtu")
            }

            override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
                println("onExecuteWrite: $requestId, $execute")
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                println("onServiceAdded: $service")
            }
        })
        server.addService(TimeProfile.createTimeService())
    }

    private fun startAdvertising() {
        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
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
                        println("LE Advertising started: $settingsInEffect")
                    }

                    override fun onStartFailure(errorCode: Int) {
                        println("LE Advertising failed: $errorCode")
                    }
                }
        )
    }
}

