package com.jongmyeong.odga

import android.app.AlertDialog
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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
//            Log.d("CheckCurrentLocation", "현재 내 위치 값: $latitude, $longitude")

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
            if (bluetoothAdapter?.isEnabled == false) { // 블루투스 꺼져 있으면 블루투스 활성화
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else{ // 블루투스 켜져있으면 블루투스 비활성화
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
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action //입력된 action
                if (action != null) {
                    Log.d("Bluetooth action", action)
                }
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var name: String? = null
                if (device != null) {
                    name = device.name //broadcast를 보낸 기기의 이름을 가져온다.

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
                        Toast.makeText(activity,"블루투스 연결 성공",Toast.LENGTH_SHORT).show()

                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        //디바이스가 연결 해제될 경우


                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {

                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        if (!foundDevice) {
                            val device_name = device!!.name
                            val device_Address = device.address
                            Log.d("nameD","${device_name},${device_Address}")
                            //블루투스 기기 이름의 앞글자가 "RNM"으로 시작하는 기기만을 검색한다
                            if (device_name != null && device_name.length > 4) {
                                if (device_name.substring(0, 3) == "ODG") {
                                    targetDevice = device
                                    foundDevice = true
                                    //찾은 디바이스에 연결한다.
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
        //리시버 등록
        context?.applicationContext?.registerReceiver(
            mBluetoothStateReceiver,
            stateFilter
        )

    }

    @ExperimentalUnsignedTypes
    private fun connectToTargetedDevice(targetedDevice: BluetoothDevice?) {


        val thread = Thread {
            //선택된 기기의 이름을 갖는 bluetooth device의 object
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            try {
                // 소켓 생성
                socket = targetedDevice?.createRfcommSocketToServiceRecord(uuid)

                socket?.connect()

                /**
                 * After Connect Device
                 */
//                connected.postValue(true)
                mOutputStream = socket?.outputStream
                mInputStream = socket?.inputStream
                // 데이터 수신
                beginListenForData()


            } catch (e: java.lang.Exception) {
                // 블루투스 연결 중 오류 발생
                e.printStackTrace()

                // connectError.postValue(Event(true))
                try {
                    socket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        //연결 thread를 수행한다
        thread.start()
    }
    fun beginListenForData() {
        val mWorkerThread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bytesAvailable = mInputStream?.available()


                    if (bytesAvailable != null) {
                        if (bytesAvailable > 0) { //데이터가 수신된 경우
                            val packetBytes = ByteArray(bytesAvailable)

                            mInputStream?.read(packetBytes)
                            /**
                             * 한 버퍼 처리
                             */
                            /**
                             * 한 버퍼 처리
                             */

                            // Byte -> String
                            val s = String(packetBytes,Charsets.UTF_8)
                            //수신 String 출력
                            putTxt.postValue(s)

                            /**
                             * 한 바이트씩 처리
                             */
                            /**
                             * 한 바이트씩 처리
                             */
                            for (i in 0 until bytesAvailable) {
                                val b: Byte = packetBytes[i]

                                val b_string = b.toInt()
                                Log.d("b_string", "${b_string}")

                                if(b_string == 70) {
                                    s_state=1

                                }
                                else{
                                    s_state=0
                                }

                                Log.d("s_state",""+s_state)
                                handler.postDelayed({addr = getCurrentLoc()},3000)
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
        //데이터 수신 thread 시작
        mWorkerThread.start()
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

        for (i in phones.indices) {
            try {
                val smsManager: SmsManager =
                    SmsManager.getDefault()
                Log.d("address", "${addr}")
                smsManager?.sendTextMessage(
                    "${phones[i]?.fphone}",
                    null,
                    "사고발생 위치정보 : ${addr}",
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

    // 낙상 확인 팝업
    fun CheckAccident(){
        try{
            var str_tittle = "사고발생"
            var str_message = "사고가 감지되었습니다. 자동 신고를 진행할까요? 미응답시 1분 후 자동 신고됩니다."
            var str_buttonOK = "확인"
            var str_buttonNO = "취소"
            var btnStatn = 0
            Handler().postDelayed({
                if(btnStatn == 0)
                {
                    sendSMS()
                    btnStatn = 1
                }

            }, 60000)
            builder = AlertDialog.Builder(activity)
            builder.setTitle(str_tittle) //팝업창 타이틀 지정

            builder.setMessage(str_message) //팝업창 내용 지정
            builder.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정
            builder.setPositiveButton(str_buttonOK, DialogInterface.OnClickListener { dialog, which ->
                // TODO Auto-generated method stub
                Toast.makeText(activity, "메세지 발송", Toast.LENGTH_SHORT).show()
                Log.d("call1","${call}")
                sendSMS()
                getAlertHidden()
                btnStatn = 1

            })
            builder.setNegativeButton(str_buttonNO, DialogInterface.OnClickListener { dialog, which ->
                // TODO Auto-generated method stub
                Toast.makeText(activity, "취소", Toast.LENGTH_SHORT).show()
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
//
//        initRecycler()


        // Inflate the layout for this fragment
        return view
    }

}