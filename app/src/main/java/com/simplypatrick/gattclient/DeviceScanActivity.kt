package com.simplypatrick.gattclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.device_item_row.*
import kotlinx.android.synthetic.main.device_item_row.view.*
import kotlinx.coroutines.experimental.launch

class DeviceScanActivity : AppCompatActivity() {
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val mDevices = HashMap<BluetoothDevice, ScanResult>()

    private lateinit var linearLayoutManager: LinearLayoutManager

    class DeviceHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindDevice(scanResult: ScanResult) {
            view.name.text = scanResult.device.name ?: scanResult.device.address
        }
    }

    class RecyclerAdapter(val scanResults: Array<ScanResult>) : RecyclerView.Adapter<DeviceHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
            val inflatedView = parent.inflate(R.layout.device_item_row, false)
            return DeviceHolder(inflatedView)
        }

        override fun getItemCount() = scanResults.size

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.bindDevice(scanResults[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        linearLayoutManager = LinearLayoutManager(this)
        deviceList.layoutManager = linearLayoutManager

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
    }

    override fun onPause() {
        super.onPause()
        stopScanLeDevices()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scan -> startScanLeDevices()
            R.id.menu_stop -> stopScanLeDevices()
        }
        return super.onOptionsItemSelected(item)
    }

    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            println("$callbackType $result")
            mDevices[result.device] = result

            updateDeviceList()
        }
    }

    private fun startScanLeDevices() {
        launch {
            mBluetoothAdapter.bluetoothLeScanner.startScan(mLeScanCallback)
        }
    }

    private fun stopScanLeDevices() {
        launch {
            mBluetoothAdapter.bluetoothLeScanner.stopScan(mLeScanCallback)
        }
    }

    private fun updateDeviceList() {
        deviceList.adapter = RecyclerAdapter(mDevices.values.toTypedArray())
    }
}
