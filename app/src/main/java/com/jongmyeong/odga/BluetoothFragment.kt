package com.jongmyeong.odga

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.collections.ArrayList


class BluetoothFragment : Fragment() {
    var b_string:Int? = null
    val handler = Handler(Looper.getMainLooper())
    var addr:String? = null
    lateinit var mainActivity: MainActivity
    private val REQUEST_ENABLE_BT=1
    private val REQUEST_ALL_PERMISSION= 2
    private val MY_PERMISSION_REQUEST_SMS = 1001
    var locationManager : LocationManager? = null
    private val REQUEST_CODE_LOCATION : Int = 2
    var currentLocation : String = ""
    var latitude : Double? = null
    var longitude : Double? = null
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH_CONNECT
    )
    var s_state = 0
    var foundDevice:Boolean = false
    private var mBluetoothStateReceiver: BroadcastReceiver? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    var socket: BluetoothSocket? = null
    var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null
    private lateinit var viewManager: RecyclerView.LayoutManager
    val putTxt: MutableLiveData<String> = MutableLiveData("")
    var targetDevice: BluetoothDevice? = null
    var currentLatLng: Location? = null

    lateinit var alertDialog : AlertDialog
    lateinit var builder : AlertDialog.Builder
    var call = 0

    fun getCurrentLoc(): String {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var userLocation: Location = getLatLng()
        if (userLocation != null) {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
//            Log.d("CheckCurrentLocation", "?????? ??? ?????? ???: $latitude, $longitude")

            var mGeocoder = Geocoder(requireContext(), Locale.KOREAN)
            var mResultList: List<Address>? = null
            try {
                mResultList = mGeocoder.getFromLocation(
                    latitude!!, longitude!!, 1
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mResultList != null) {
                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
                currentLocation = mResultList[0].getAddressLine(0)
                currentLocation = currentLocation.substring(5)
                return currentLocation
            }
        }
        return (-1).toString()
    }
    fun getLatLng() : Location {

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), this.REQUEST_CODE_LOCATION)
            getLatLng()
        } else {
            val locationProvider = LocationManager.GPS_PROVIDER
            currentLatLng = locationManager?.getLastKnownLocation(locationProvider)
        }
        return currentLatLng!!
    }

    fun bluetoothOnOff(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("bluetoothAdapter","Device doesn't support Bluetooth")
        }else{
            if (bluetoothAdapter?.isEnabled == false) { // ???????????? ?????? ????????? ???????????? ?????????
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else{ // ???????????? ??????????????? ???????????? ????????????
                bluetoothAdapter?.disable()
            }
        }
    }

    fun scanDevice(){
        registerBluetoothReceiver()
        bluetoothAdapter?.startDiscovery()
    }

    fun registerBluetoothReceiver(){
//        intentfilter

        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : ???????????? ???????????? ??????
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //?????? ??????
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //?????? ?????? ??????
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //?????? ?????????
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //?????? ?????? ??????
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //?????? ?????? ??????
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action //????????? action
                if (action != null) {
                    Log.d("Bluetooth action", action)
                }
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var name: String? = null
                if (device != null) {
                    name = device.name //broadcast??? ?????? ????????? ????????? ????????????.

                }
                when (action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR
                        )
                        when (state) {
                            BluetoothAdapter.STATE_OFF -> {
                            }
                            BluetoothAdapter.STATE_TURNING_OFF -> {
                            }
                            BluetoothAdapter.STATE_ON -> {
                            }
                            BluetoothAdapter.STATE_TURNING_ON -> {
                            }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        Toast.makeText(activity,"???????????? ?????? ??????",Toast.LENGTH_SHORT).show()

                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        //??????????????? ?????? ????????? ??????
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {

                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        if (!foundDevice) {
                            val device_name = device!!.name
                            val device_Address = device.address
                            Log.d("nameD","${device_name},${device_Address}")
                            //???????????? ?????? ????????? ???????????? "ODG"?????? ???????????? ???????????? ????????????
                            if (device_name != null && device_name.length > 4) {
                                if (device_name.substring(0, 3) == "ODG") {
                                    targetDevice = device
                                    foundDevice = true
                                    //?????? ??????????????? ????????????.
                                    connectToTargetedDevice(targetDevice)
                                }
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        if (!foundDevice) {
                            //Toast massage
                        }
                    }

                }
            }
        }
        //????????? ??????
        context?.applicationContext?.registerReceiver(
            mBluetoothStateReceiver,
            stateFilter
        )

    }

    @ExperimentalUnsignedTypes
    private fun connectToTargetedDevice(targetedDevice: BluetoothDevice?) {
        val thread = Thread {
            //????????? ????????? ????????? ?????? bluetooth device??? object
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            try {
                // ?????? ??????
                socket = targetedDevice?.createRfcommSocketToServiceRecord(uuid)

                socket?.connect()
                /**
                 * After Connect Device
                 */
                mOutputStream = socket?.outputStream
                mInputStream = socket?.inputStream
                // ????????? ??????
                beginListenForData()
            } catch (e: java.lang.Exception) {
                // ???????????? ?????? ??? ?????? ??????
                e.printStackTrace()
                try {
                    socket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        //?????? thread??? ????????????
        thread.start()
    }
    fun beginListenForData() {
        val mWorkerThread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bytesAvailable = mInputStream?.available()
                    if (bytesAvailable != null) {
                        if (bytesAvailable > 0) { //???????????? ????????? ??????
                            val packetBytes = ByteArray(bytesAvailable)
                            mInputStream?.read(packetBytes)

                            // Byte -> String
                            val s = String(packetBytes,Charsets.UTF_8)
                            //?????? String ??????
                            putTxt.postValue(s)


                            for (i in 0 until bytesAvailable) {
                                val b: Byte = packetBytes[i]
                                String.format("%02x", b)
                                Log.d("bdata","${b}")
                                b_string = b.toInt()
                                Log.d("b_data","${b_string}")

                                handler.postDelayed({
                                    Log.d("checkb","${b_string}")
                                    val mActivity = activity as MainActivity

                                    mActivity.receiveData(b_string!!)

                                }, 0)


                                Log.d("b","${b_string}")
                                if(b_string == 70) {
                                    s_state=1
                                }
                                else{
                                    s_state=0
                                }
                                Log.d("s_state",""+s_state)


                                if(s_state==1) {
                                    s_state = 0
                                    handler.postDelayed({
                                        CheckAccident()

                                    }, 0)
                                }


                            }
                        }
                    }
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
        //????????? ?????? thread ??????
        mWorkerThread.start()
    }

    fun showNotification() {

        /*
            Android Oreo(SDK Version 26) ????????? ????????? ????????? ?????????
            ????????? ??????????????? ????????? ?????? ?????? ?????????

            https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=ko
         */
        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
                Oreo ?????? ??????,

                NotificationCompat.Builder??? ???????????? App.onCreate?????? ???????????? Channel??? id??? ?????????
             */
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent: PendingIntent = PendingIntent.getActivity(requireActivity(), 0, intent,0)
            NotificationCompat.Builder(requireActivity(), App.NOTIFICATON_CHANNEL_ID.toString())
                .setSmallIcon(R.drawable.sharp_notification_important_24) // ?????????
                .setContentIntent(pendingIntent) // ???????????? ?????? ????????????
                .setAutoCancel(true)
                .setContentTitle("Detection Dangerous") // ??????
                .setContentText("??????!! ????????? ???????????????!") // ??????
        } else {
            /*
                Oreo ?????? ??????,

                NotificationCompat.Builder??? ???????????? App.onCreate?????? ???????????? Channel??? id??? ????????? ????????? ??????
                (App.onCreate ?????? channel??? ??????????????? ??????)
             */
            NotificationCompat.Builder(requireActivity())
                .setSmallIcon(R.drawable.sharp_notification_important_24) // ?????????
                .setContentIntent(
                    PendingIntent.getActivity(
                        requireActivity(),
                        0,
                        Intent(),
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                ) // ???????????? ?????? ????????????
                .setContentTitle("Detection Dangerous") // ??????
                .setContentText("??????!! ???????????? ???????????????!") // ??????
        }

        /*
            ????????? ????????? ?????? ?????? builder??? ?????? ????????? ????????? ???????????? ?????????
         */
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(requireActivity())
        notificationManager.notify(0, builder.build())
    }



    // Permission check
    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (context?.let { ActivityCompat.checkSelfPermission(it, permission) }
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(activity, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun checkSMS(){
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.SEND_SMS
                )
            ) {
                // Android provides a utility method, shouldShowRequestPermissionRationale(), that returns true if the user has previously
                // denied the request, and returns false if a user has denied a permission and selected the Don't ask again option in the
                // permission request dialog, or if a device policy prohibits the permission. If a user keeps trying to use functionality that
                // requires a permission, but keeps denying the permission request, that probably means the user doesn't understand why
                // the app needs the permission to provide that functionality. In a situation like that, it's probably a good idea to show an
                // explanation.
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("info")
                builder.setMessage("This app won't work properly unless you grant SMS permission.")
                builder.setIcon(android.R.drawable.ic_dialog_info)
                builder.setNeutralButton(
                    "OK"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(android.Manifest.permission.SEND_SMS),
                        MY_PERMISSION_REQUEST_SMS
                    )
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.SEND_SMS),
                    MY_PERMISSION_REQUEST_SMS
                )
            }
        }
    }
    fun sendSMS(){
        val DB_NAME = "sqlite.sql"
        val DB_VERSION = 1
        val helper = SqliteHelper(requireContext(), DB_NAME, DB_VERSION)
        val phones = helper.selectPhoneBook()
        addr = getCurrentLoc()

        for (i in phones.indices) {
            try {
                val smsManager: SmsManager =
                    SmsManager.getDefault()
                Log.d("address", "${addr}")
                smsManager?.sendTextMessage(
                    "${phones[i]?.fphone}", null,
                    "???????????? ???????????? : ${addr}",
                    null,
                    null
                )
                Toast.makeText(
                    activity,
                    "Message sent",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    // ?????? ?????? ??????
    fun CheckAccident(){
        try{
            var str_tittle = "????????????"
            var str_message = "????????? ?????????????????????. ?????? ????????? ???????????????? ???????????? 1??? ??? ?????? ???????????????."
            var str_buttonOK = "??????"
            var str_buttonNO = "??????"
            var btnStatn = 0
            Handler().postDelayed({
                if(btnStatn == 0)
                {
                    sendSMS()
                    btnStatn = 1
                }

            }, 60000)
            builder = AlertDialog.Builder(activity)
            builder.setTitle(str_tittle) //????????? ????????? ??????

            builder.setMessage(str_message) //????????? ?????? ??????
            builder.setCancelable(false) //?????? ???????????? ???????????? ???????????? ?????????????????? ??????
            builder.setPositiveButton(str_buttonOK, DialogInterface.OnClickListener { dialog, which ->
                // TODO Auto-generated method stub
                Toast.makeText(activity, "????????? ??????", Toast.LENGTH_SHORT).show()
                Log.d("call1","${call}")
                sendSMS()
                getAlertHidden()
                btnStatn = 1

            })
            builder.setNegativeButton(str_buttonNO, DialogInterface.OnClickListener { dialog, which ->
                // TODO Auto-generated method stub
                Toast.makeText(activity, "??????", Toast.LENGTH_SHORT).show()
                getAlertHidden()
                btnStatn = 1
            })

            alertDialog = builder.create()
            try {
                alertDialog.show()
            }
            catch (e : Exception){
                e.printStackTrace()
            }
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }
    fun getAlertHidden(){
        try {
            alertDialog.dismiss()
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)
        val bleOnOffBtn: ToggleButton = view.findViewById(R.id.ble_on_off_btn)
        val scanBtn: Button = view.findViewById(R.id.scanBtn)
        val bluetoothManager =  activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val appCompatImageViewWarning:ImageView = view.findViewById(R.id.appCompatImageViewWarning)
        val appCompatImageViewflash: ImageView = view.findViewById(R.id.appCompatImageViewflash)
        checkSMS()
        bluetoothAdapter = bluetoothManager.adapter
        viewManager = LinearLayoutManager(activity)

        if(bluetoothAdapter!=null){
            if(bluetoothAdapter?.isEnabled==false){
                bleOnOffBtn.isChecked = true
                scanBtn.isVisible = false
            } else{
                bleOnOffBtn.isChecked = false
                scanBtn.isVisible = true
            }
        }

        bleOnOffBtn.setOnCheckedChangeListener { _, isChecked ->
            bluetoothOnOff()
            scanBtn.visibility = if (scanBtn.visibility == View.VISIBLE){ View.INVISIBLE } else{ View.VISIBLE }
        }

        scanBtn.setOnClickListener { v:View? -> // Scan Button Onclick

            if (!hasPermissions(activity, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
            }

            scanDevice()
        }
        Log.d("b_stringData","${b_string}")
//        initRecycler()




        // Inflate the layout for this fragment
        return view
    }

}