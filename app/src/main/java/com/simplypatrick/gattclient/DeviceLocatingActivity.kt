package com.simplypatrick.gattclient

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_device_detail.*

class DeviceLocatingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_locating)
        setSupportActionBar(detail_toolbar)

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = DeviceLocatingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DeviceLocatingFragment.ARG_ITEM,
                            intent.getParcelableExtra<ScanResult>(DeviceLocatingFragment.ARG_ITEM))
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.device_detail_container, fragment)
                    .commit()
        }
    }
}
