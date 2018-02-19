package com.simplypatrick.gattclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.design.widget.Snackbar
import android.view.*
import android.widget.TextView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.android.synthetic.main.device_list_content.view.*

import kotlinx.android.synthetic.main.device_list.*
import kotlinx.coroutines.experimental.launch

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [DeviceDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class DeviceListActivity : AppCompatActivity() {
    private lateinit var mBluetoothAdapter: BluetoothAdapter

    // Use an ArrayList to keep a stable list of scan results
    private val mScanResults = ArrayList<ScanResult>()

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        if (device_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothAdapter = bluetoothManager.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        startScanLeDevices()
    }

    override fun onPause() {
        super.onPause()
        stopScanLeDevices()
    }

    class RecyclerViewAdapter(private val mParentActivity: DeviceListActivity,
                              private val mValues: ArrayList<ScanResult>,
                              private val mTwoPane: Boolean) :
            RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        private val mOnClickListener: View.OnClickListener

        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as ScanResult
                if (mTwoPane) {
                    val fragment = DeviceDetailFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable(DeviceDetailFragment.ARG_ITEM, item)
                        }
                    }
                    mParentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.device_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, DeviceDetailActivity::class.java).apply {
                        putExtra(DeviceDetailFragment.ARG_ITEM, item)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.device_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.device.name ?: item.device.address
            holder.mContentView.text = "RSSI: ${item.rssi}"

            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mIdView: TextView = view.title
            val mContentView: TextView = view.content
        }
    }

    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            updateDeviceList(result)
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

    private fun updateDeviceList(scanResult: ScanResult) {
        if (device_list.adapter == null) {
            device_list.adapter = RecyclerViewAdapter(this, mScanResults, mTwoPane)
        }

        // Linear search should be good enough for limited number of nearby devices.
        for ((i, result) in mScanResults.withIndex()) {
            if (result.device == scanResult.device) {
                mScanResults[i] = scanResult

                device_list.adapter.notifyItemChanged(i)
                return
            }
        }

        mScanResults.add(scanResult)
        device_list.adapter.notifyItemInserted(mScanResults.size - 1)
    }
}
