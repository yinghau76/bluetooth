package com.simplypatrick.gattclient

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_device_detail.*
import kotlinx.android.synthetic.main.device_detail.view.*

/**
 * A fragment representing a single Device detail screen.
 * This fragment is either contained in a [DeviceListActivity]
 * in two-pane mode (on tablets) or a [DeviceDetailActivity]
 * on handsets.
 */
class DeviceDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var mItem: ScanResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                mItem = it.getParcelable<ScanResult>(ARG_ITEM)
                activity?.toolbar_layout?.title = mItem?.device.toString()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.device_detail, container, false)

        // Show the dummy content as text in a TextView.
        mItem?.let {
            rootView.device_detail.text = it.toString()
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM = "item"
    }
}
