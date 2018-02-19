package com.simplypatrick.gattclient

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DeviceLocatingFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DeviceLocatingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceLocatingFragment : Fragment() {
    private var mDevice: BluetoothDevice? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mDevice = arguments.getParcelable(ARG_ITEM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_device_locating, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        val ARG_ITEM = "item"

        fun newInstance(device: BluetoothDevice): DeviceLocatingFragment {
            val fragment = DeviceLocatingFragment()
            val args = Bundle()
            args.putParcelable(ARG_ITEM, device)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
