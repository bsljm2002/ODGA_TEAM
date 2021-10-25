package com.jongmyeong.odga

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.RelativeLayout
import com.jongmyeong.odga.databinding.ActivityMainBinding
import android.content.pm.PackageManager

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission


class MainActivity : AppCompatActivity(), PermissionListener {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mIsListening = false
    private var mListeningThread: ExecutorService? = null
    private var mAudioRecord: AudioRecord? = null
    private var mLabels: ArrayList<String?>? = null
    private var mIsPermissionGranted = false
    private var mActivityMainBinding: ActivityMainBinding? = null
    private val context: AccessibilityService? = null
    private val builder: NotificationCompat.Builder? = null
    var d_data = null
    val fourth: String = "7"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        configureBottomNavigation()
        createNotificationChannel()

        TedPermission
            .with(this)
            .setPermissionListener(this)
            .setDeniedTitle(R.string.app_name)
            .setDeniedMessage(R.string.notice_permission)
            .setPermissions(
                Manifest.permission.VIBRATE,
                Manifest.permission.RECORD_AUDIO
            )
            .check()

//        getHashKey()
    }
    fun configureBottomNavigation() {
        binding.vpAcMainFragPager.adapter = MainFragmentStatePagerAdapter(supportFragmentManager, 3)
        binding.tlAcMainBottomMenu.setupWithViewPager(binding.vpAcMainFragPager)

        val bottomNaviLayout: View = this.layoutInflater.inflate(R.layout.bottom_navigation_tab, null, false)

        binding.tlAcMainBottomMenu.getTabAt(0)!!.customView = bottomNaviLayout.findViewById(R.id.btn_bottom_navi_home_tab) as RelativeLayout
        binding.tlAcMainBottomMenu.getTabAt(1)!!.customView = bottomNaviLayout.findViewById(R.id.btn_bottom_navi_bluetooth_tab) as RelativeLayout
        binding.tlAcMainBottomMenu.getTabAt(2)!!.customView = bottomNaviLayout.findViewById(R.id.btn_bottom_navi_phone_tab) as RelativeLayout
    }
    fun addButtonClick(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.0.$fourth:8000"))
        startActivity(intent)
    }

    var i: Int = 0
    fun ServiceStart(view : View){
        val intent = Intent(this,MusicService::class.java)
        i++
        if (i%2==1){
            startService(intent)
        }else{
            stopService(intent)
        }
//        startService(intent) // 서비스 시작
    }

    fun ServiceStop(view : View){
        val intent = Intent(this,MusicService::class.java)
        stopService(intent) // 서비스 종료
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //SDK_INT 버전에서 조건에 의해 차단
            val importance: Int = NotificationManager.IMPORTANCE_HIGH // IMPORTANCE_HIGH(중요도설정)
            val channel = NotificationChannel(
                App.NOTIFICATON_CHANNEL_ID,
                App.NOTIFICATON_CHANNEL_NAME,
                importance
            )
            channel.description = com.jongmyeong.odga.App.NOTIFICATON_CHANNEL_DESCRIPTION
            val notificationManager: NotificationManager = getSystemService<NotificationManager?>(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }





    fun showNotification() {

        /*
            Android Oreo(SDK Version 26) 부터는 헤드업 알람을 위하여
            채널이 필요하므로 버전에 따라 구분 해준다

            https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=ko
         */
        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
                Oreo 이상 버전,

                NotificationCompat.Builder의 생성자에 App.onCreate에서 생성했던 Channel의 id를 넣어줌
             */
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,0)
            NotificationCompat.Builder(this@MainActivity, App.NOTIFICATON_CHANNEL_ID.toString())
                .setSmallIcon(R.drawable.sharp_notification_important_24) // 아이콘
                .setContentIntent(pendingIntent) // 눌렀을때 알람 사라지게
                .setAutoCancel(true)
                .setContentTitle("Detection Dangerous") // 제목
                .setContentText("위험!! 주위를 둘러보세요!") // 내용
        } else {
            /*
                Oreo 미만 버전,

                NotificationCompat.Builder의 생성자에 App.onCreate에서 생성했던 Channel의 id를 넣어줄 필요가 없음
                (App.onCreate 에서 channel을 생성하지도 않음)
             */
            NotificationCompat.Builder(this@MainActivity)
                .setSmallIcon(R.drawable.sharp_notification_important_24) // 아이콘
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(),
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                ) // 눌렀을때 알람 사라지게
                .setContentTitle("Detection Dangerous") // 제목
                .setContentText("위험!! 주위를 둘러보세요!") // 내용
        }

        /*
            위에서 버전에 따라 만든 builder를 통해 헤드업 알림을 생성하고 보여줌
         */
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build())
    }
    val mHandler: Handler? = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {

            if (msg.what == WHAT_UPDATE_RESULT) {
                when (msg.arg1) {
                    3, 9 -> {
                        /*
                                진동을 울려줌
                             */
                        val vibrator: Vibrator? =
                            getSystemService(VIBRATOR_SERVICE) as Vibrator?
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(2000, 255))
                        } else {
                            vibrator?.vibrate(2000)
                        }

                        /*
                                위험 이미지가 그려진 AppCompatImageView의 alpha값을

                                1.5초간 1.0에서 0.0으로 변경시키는 애니매이션을 동작한다(나타났다가 사라지는 간격(투명도))
                             */
                        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat<View?>(
                            binding?.appCompatImageViewWarning,
                            View.ALPHA,
                            1.0f,
                            0.0f
                        )
                        objectAnimator.interpolator = AccelerateInterpolator()
                        objectAnimator.duration = 1500
                        objectAnimator.start()
                        val objectAnimator1: ObjectAnimator = ObjectAnimator.ofFloat<View?>(
                            binding?.appCompatImageViewflash,
                            View.ALPHA,
                            0.15f,
                            0.0f
                        )
                        objectAnimator1.interpolator = AccelerateInterpolator()
                        objectAnimator1.duration = 500
                        objectAnimator1.repeatCount = 2
                        objectAnimator1.start()


                        /*
                                헤드업 알람을 보여줌
                             */showNotification()
                    }
                    else -> {}
                }

                /*
                        모든 경우에 대해 textView를 업데이트 해줌
                     */
//                mActivityMainBinding?.appCompatTextViewCondition?.text = "Detecting..."
                //                mActivityMainBinding.appCompatTextViewLabel.setText(mLabels.get(msg.arg1));
            }
        }
    }

    fun receiveData(who: Int) {
        Log.d("whodata","${who}")
        when (who) {

            78 -> {
                /*
                        진동을 울려줌
                     */
                val vibrator: Vibrator? =
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(2000, 255))
                } else {
                    vibrator?.vibrate(2000)
                }

                /*
                        위험 이미지가 그려진 AppCompatImageView의 alpha값을

                        1.5초간 1.0에서 0.0으로 변경시키는 애니매이션을 동작한다(나타났다가 사라지는 간격(투명도))
                     */
                val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat<View?>(
                    binding.appCompatImageViewWarning,
                    View.ALPHA,
                    1.0f,
                    0.0f
                )
                objectAnimator.interpolator = AccelerateInterpolator()
                objectAnimator.duration = 1500
                objectAnimator.start()
                val objectAnimator1: ObjectAnimator = ObjectAnimator.ofFloat<View?>(
                    binding.appCompatImageViewflash,
                    View.ALPHA,
                    0.15f,
                    0.0f
                )
                objectAnimator1.interpolator = AccelerateInterpolator()
                objectAnimator1.duration = 500
                objectAnimator1.repeatCount = 2
                objectAnimator1.start()


                /*
                        헤드업 알람을 보여줌
                     */showNotification()

            }
            else -> {}
        }

    }





    override fun onResume() {
        super.onResume()

        /*
            앱을 맨 처음 켜면 onResume이 호출 될것이다
            하지만 이때는
            1. 아직 권한이 있는지 없는지도 모르고
            2. conv_labels.txt도 로드하지 않았으므로
            아무것도 하지 않고 넘어가야 한다

            사용자가 권한을 수락 했다면 onPermissionGranted 이후 onResume이 다시 호출돼는데
            (눈에는 보이지 않지만 권한 관련 Activity가 켜졌다 꺼짐)
            (따라서 위에3번 케이스 (앱에서 다른 액티비티로 이동했다가 돌아올때) 이므로 onResume이 호출돼는거)

            이때는 onPermissionGranted에서 mIsPermissionGranted를 true로 바꿔줬기 떄문에
         */if (mIsPermissionGranted) {

            /*
                이 라인을 수행한다
             */
            startListeningThread()
        }
    }
    override fun onPause() {
        super.onPause()
        stopListeningThread()
    }
    override fun onPermissionGranted() {

        /*
            res/layout/activity_main.xml 뷰를
            이 activity(MainActivity.java에) 바인딩시킨다(붙인다)

            이제부터 MainActivity를 켜면 activity_main.xml에서 정의한 뷰들이 화면에 그려진다
         */
//        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mActivityMainBinding?.run { MainActivity }     // 오류나면 확인해봐야될 곳(with, run)
        if (loadLabels()) {
            /*
                assets/conv_labels.txt파일을 로드하는데 성공하면
             */
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            /*
                mIsPermissionGranted를 true로 변경 해줌

                이 바로 다음에 onResume이 호출돼는데, 거기서 mIsPermissionGranted 이게 true일 경우에만
                Listening을 Thread 시작함
             */mIsPermissionGranted = true
        } else {
            /*
                assets/conv_labels.txt파일을 로드하는데 실패하면
             */

            /*
                실패했다는 메시지를 띄우고 앱을 종료함
             */
            Toast.makeText(this, getString(R.string.notice_cant_load_labels), Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }
    override fun onPermissionDenied(deniedPermissions: MutableList<String?>?) {
        finish()
    }
    private fun loadLabels(): Boolean {
        var reader: BufferedReader? = null
        reader = try {
            BufferedReader(
                InputStreamReader(
                    assets.open(
                        LABEL_FILENAME?.split("file:///android_asset/".toRegex())!!.toTypedArray()[1]
                    )
                )
            )
        } catch (e: IOException) {
            return false
        }
        var line: String? = null
        try {
            while (reader?.readLine().also { line = it } != null) {
                if (mLabels == null) {
                    mLabels = ArrayList()
                }
                mLabels?.add(line)
            }
        } catch (e: IOException) {
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return mLabels?.size!! > 0
        }
    }
    private fun startListeningThread() {
        if (!mIsListening) {

            /*
                청취 Thread를 생성 해줌
             */
            Executors.newSingleThreadExecutor().also { it.also { mListeningThread = it } } // 확인해봐야될 곳

            /*
                청취 Thread를 시작 해줌

                해야 할을은 생성 해줄떄 넘겨준 Runnable의 run메서드를 오버라이드 하여 정의함
             */
            mListeningThread?.execute(Runnable { /*
                                버퍼 크기를 계산함
                             */
                val bufferSize: Int = AudioRecord.getMinBufferSize(
                    App.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                /*
                                        Audio Record를 생성함
                                     */if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@Runnable
                }
                mAudioRecord = AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    App.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                /*
                                                      AudioRecord 생성 실패시 청취 쓰레드 즉시 종료함
                                                   */
                /*
                                            AudioRecord 생성 성공시
                                         */if (mAudioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
                    /*
                                                청취 Thread가 돌고있다는 플래그인 mIsListening를 true로 셋 해주고
                                        */
                    mIsListening = true
                    mAudioRecord!!.startRecording()

                    /*
                                                stopListeningThread를 통해 mIsListening가 false 될때까지 계속 돌면서
                                             */while (mIsListening) {
                        var lengthOfResult = 0
                        val timeStamp = System.currentTimeMillis()
                        val buffer = ShortArray(bufferSize / 2)
                        val result = ShortArray(App.RECORDING_LENGTH)

                        /*
                                                    App.RECORDING_LENGTH만큼 읽어올떄까지 계속
                                                 */while (mIsListening && lengthOfResult + bufferSize / 2 < App.RECORDING_LENGTH) {

                            /*
                                                        AudioRecord의 read를 이용해 마이크에서 소리를 읽어옴
                                                     */
                            val lengthOfReading: Int = mAudioRecord!!.read(buffer, 0, buffer.size)
                            /*
                                                 로그 확인
                                                 */
                            //                                Log.v("lengthOf", Integer.toString(lengthOfResult));
                            //                                Log.v("lengthOf", Integer.toString(lengthOfReading));
                            System.arraycopy(
                                buffer, 0,
                                result, lengthOfResult,
                                lengthOfReading
                            )
                            lengthOfResult += lengthOfReading
                        }

                        /*
                                                    다 읽었고, 그 사이에 stopListeningThread가 호출이 안됐다면
                                                 */
                        if (mIsListening) {
                            /*
                             작업을 독립적으로 수행하는 TensorFlowTask로 읽어들인 데이터를 보내 분석을 요청하고
                            */
                            object :
                                com.jongmyeong.odga.TensorFlowTask(assets, mLabels?.size!!, result,) {
                                override fun onPostExecute(indexOfLabel: Int?) {
                                    if (mIsListening) {
                                        if (indexOfLabel != null) {
                                            mHandler?.obtainMessage(WHAT_UPDATE_RESULT, indexOfLabel, 0)?.sendToTarget()

                                        }
                                    }
                                }
                            }.execute()

                            /*
                                                        다시 397라인으로 이동하여 청취를 계속함
                                                     */
                        }
                    }
                    mAudioRecord!!.stop()
                }
                mAudioRecord!!.release()
                mAudioRecord = null
                mListeningThread = null
            })
            mListeningThread?.run { shutdown() }              // 오류나면 확인해봐야될 곳 (with, run 둘중하나)
        }
    }
    private fun stopListeningThread() {
        mIsListening = false
    }

    companion object {
        private val LABEL_FILENAME: String? = "file:///android_asset/hyunho_conv_labels.txt"
        private const val WHAT_UPDATE_RESULT = 0
    }

}